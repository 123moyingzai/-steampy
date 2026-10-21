package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.SteamAccount;
import com.steampy.entity.SteamLibrary;
import com.steampy.entity.User;
import com.steampy.entity.UserSteamBinding;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.SteamAccountMapper;
import com.steampy.mapper.SteamLibraryMapper;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.UserSteamBindingMapper;
import com.steampy.service.SteamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SteamController
 * 负责 Steam 账号绑定、价格查询、游戏库等业务的 HTTP 接口层
 * 提供的核心接口：
 *   - GET  /api/steam/prices?gameIds=16,17,18  — 批量查询 Steam 实时价格（带内存缓存 + 后台异步刷新）
 *   - POST /api/steam/bind/{userId}            — 模拟绑定 Steam 账号（首次绑定时基于种子生成一致的模拟数据）
 *   - DELETE /api/steam/unbind/{userId}        — 解绑 Steam 账号（清除绑定关系，保留原始 Steam 数据便于重新绑定恢复）
 *   - GET  /api/steam/info/{userId}            — 获取用户 Steam 账号资料及统计信息（账号价值、游戏数、游戏时长等）
 *   - GET  /api/steam/library/{userId}         — 获取用户的 Steam 游戏库列表
 *   - GET  /api/steam/check-ownership          — 查重：判断用户 Steam 库存中是否已拥有某游戏
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/steam")
public class SteamController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SteamLibraryMapper steamLibraryMapper;
    @Autowired
    private SteamAccountMapper steamAccountMapper;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private SteamService steamService;
    @Autowired
    private UserSteamBindingMapper bindingMapper;

    // ======== Steam 实时价格缓存 ========
    private static final Pattern STEAM_APPID_PATTERN = Pattern.compile("/steam/apps/(\\d+)/");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int STEAM_CONNECT_TIMEOUT = 8000;
    private static final int STEAM_READ_TIMEOUT = 10000;
    // appid -> {initial, final, discount_percent, currency, ts}
    private final Map<String, Object[]> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L; // 10 分钟

    // 信任所有 SSL 证书（开发环境用，解决 PKIX 证书链缺失导致 HTTPS 调用 Steam API 失败的问题）
    private static void trustAllSsl() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            }}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
        } catch (Exception ignored) {}
    }
    static { trustAllSsl(); }

    /**
     * 批量查询 Steam 实时价格（支持内存缓存 + 后台线程异步刷新）
     * <p>
     * 流程：
     *   1. 先从 games 表查出指定 gameIds 的游戏 image 字段，用正则提取 Steam appid
     *   2. 命中 10 分钟内的内存缓存则直接返回，标记 from_steam=true
     *   3. 缓存未命中时先用 DB 价兜底返回（from_steam=false），同时启动后台线程异步调用 Steam API 更新缓存
     *   4. 不支持 Steam API 的游戏（无 appid）自动 fallback 到 games 表的 DB 价
     * </p>
     *
     * @param gameIds 可选：逗号分隔的游戏 ID 列表；不传则查全表
     * @return {@link Result} 每项包含 game_id、appid、steam_initial（原价）、steam_final（现价）、discount_percent、currency、from_steam（是否来自 Steam API）
     */
    @GetMapping("/prices")
    public Result<List<Map<String, Object>>> getPrices(@RequestParam(required = false) String gameIds) {
        // 1. 查 games 表
        QueryWrapper<Game> qw = new QueryWrapper<>();
        if (gameIds != null && !gameIds.isBlank()) {
            List<String> ids = Arrays.asList(gameIds.split(","));
            qw.in("id", ids);
        }
        qw.select("id", "image", "price", "original_price");
        List<Game> games = gameMapper.selectList(qw);

        // 2. 提取 appid + 查缓存
        List<Map<String, Object>> result = new ArrayList<>();
        List<Game> needBackgroundFetch = new ArrayList<>();
        for (Game g : games) {
            String appid = extractAppid(g.getImage());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("game_id", g.getId());
            entry.put("appid", appid);
            if (appid == null) {
                entry.put("steam_initial", g.getOriginalPrice() != null ? g.getOriginalPrice() : g.getPrice());
                entry.put("steam_final", g.getPrice());
                entry.put("discount_percent", 0);
                entry.put("currency", "CNY");
                entry.put("from_steam", false);
                result.add(entry);
            } else {
                Object[] cached = priceCache.get(appid);
                if (cached != null && (System.currentTimeMillis() - (Long) cached[4]) < CACHE_TTL_MS) {
                    // 命中缓存 —— from_steam=true
                    entry.put("steam_initial", cached[0]);
                    entry.put("steam_final", cached[1]);
                    entry.put("discount_percent", cached[2]);
                    entry.put("currency", cached[3]);
                    entry.put("from_steam", true);
                    result.add(entry);
                } else {
                    // 缓存未命中 — 先用 DB 价返回，后台异步查 Steam
                    entry.put("steam_initial", null);
                    entry.put("steam_final", null);
                    entry.put("discount_percent", null);
                    entry.put("currency", null);
                    entry.put("from_steam", false);
                    result.add(entry);
                    needBackgroundFetch.add(g);
                }
            }
        }

        // 3. 如果有缓存未命中的，后台线程异步查 Steam（不阻塞 HTTP 响应）
        if (!needBackgroundFetch.isEmpty()) {
            List<Game> bgList = needBackgroundFetch; // 捕获
            new Thread(() -> {
                for (Game g : bgList) {
                    String appid = extractAppid(g.getImage());
                    if (appid == null) continue;
                    try {
                        URL url = new URL("https://store.steampowered.com/api/appdetails?appids=" + appid + "&cc=CN");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(STEAM_CONNECT_TIMEOUT);
                        conn.setReadTimeout(STEAM_READ_TIMEOUT);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "SteamPY/1.0");
                        conn.setRequestProperty("Accept", "application/json");

                        int code = conn.getResponseCode();
                        if (code == 429 || code == 403) { Thread.sleep(3000); code = conn.getResponseCode(); }
                        if (code != 200) { conn.disconnect(); Thread.sleep(400); continue; }
                        StringBuilder sb = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) sb.append(line);
                        }
                        conn.disconnect();

                        JsonNode root = MAPPER.readTree(sb.toString());
                        JsonNode appNode = root.get(appid);
                        if (appNode == null || !appNode.path("success").asBoolean()) { Thread.sleep(400); continue; }
                        JsonNode price = appNode.path("data").path("price_overview");
                        if (price == null || price.isMissingNode()) { Thread.sleep(400); continue; }

                        int initialCents = price.path("initial").asInt(0);
                        int finCents = price.path("final").asInt(0);
                        int disc = price.path("discount_percent").asInt(0);
                        String currency = price.path("currency").asText("CNY");

                        priceCache.put(appid, new Object[]{
                                initialCents / 100.0, finCents / 100.0, disc, currency, System.currentTimeMillis()
                        });
                        System.out.println("[SteamPrices] 后台缓存更新 appid=" + appid + " ✅");
                    } catch (Exception ignored) {}
                    try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                }
                System.out.println("[SteamPrices] 后台查询线程完成，共处理 " + bgList.size() + " 款");
            }, "SteamPriceBG").start();
        }

        return Result.success(result);
    }

    /**
     * 从游戏封面 URL 中用正则提取 Steam appid。
     * URL 格式形如 https://store.steampowered.com/apps/123456/header.jpg 会提取出 "123456"。
     *
     * @param imageUrl 游戏封面图片 URL
     * @return Steam appid 字符串；无法提取时返回 null
     */
    private static String extractAppid(String imageUrl) {
        if (imageUrl == null) return null;
        Matcher m = STEAM_APPID_PATTERN.matcher(imageUrl);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 模拟绑定 Steam 账号
     * <p>
     * 策略：
     *   - 优先使用 users 表已有的 steamAccountId 恢复账号，其次用 hash 查 steam_accounts 表
     *   - 已有账号则直接恢复绑定关系（不重新生成数据）
     *   - 新账号则用 accountHash 作为种子，生成稳定一致的模拟账号数据（SteamID64、昵称、头像、地区、等级、游戏库）
     * 最后更新 users.steamAccountId 外键，并实时刷新 steam_accounts 表的统计数据。
     * </p>
     *
     * @param userId 要绑定 Steam 的用户 ID
     * @return {@link Result} 成功返回 Steam 账号完整资料 + 当前游戏库列表
     */
    @PostMapping("/bind/{userId}")
    public Result<Map<String, Object>> bind(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return Result.error("用户不存在");

        String accountHash = SteamService.hashUserId(userId);

        // ======== 策略：先用 u.steamAccountId，没有再用 hash 查 ========
        SteamAccount account = null;
        if (u.getSteamAccountId() != null) {
            account = steamAccountMapper.selectById(u.getSteamAccountId());
        }
        if (account == null) {
            account = steamService.findAccountByHash(accountHash);
        }

        boolean isNewAccount = (account == null);

        // === 已有账号 → 直接恢复，不重新生成 ===
        if (!isNewAccount) {
            // 绑定关系写入 user_steam_bindings（如不存在）
            QueryWrapper<UserSteamBinding> bw = new QueryWrapper<>();
            bw.eq("user_id", userId).eq("steam_account_id", account.getId());
            if (bindingMapper.selectCount(bw) == 0) {
                UserSteamBinding b = new UserSteamBinding();
                b.setUserId(userId);
                b.setSteamAccountId(account.getId());
                b.setIsCurrent(1);
                bindingMapper.insert(b);
            }
        } else {
            // === 新建账号（只在首次绑定此用户时执行）===
            // 用 accountHash 做种子，保证同一 userId 每次绑定生成的数据完全一致
            long seed = Math.abs(accountHash.hashCode());
            Random rand = new Random(seed);

            String[] suffixes = {"_Gamer", "_Steam", "_Pro", "_Ninja", "_Boss", "_Legend", "_Hunter", "_Master"};
            String steamName = (u.getNickname() != null ? u.getNickname() : u.getUsername())
                    + suffixes[rand.nextInt(suffixes.length)];

            long steamId64 = 76561198L + (seed % 999999999L);
            String steamId = String.valueOf(steamId64);

            String[] regions = {"中国", "美国", "日本", "韩国", "香港", "新加坡", "俄罗斯", "英国"};
            String region = regions[rand.nextInt(regions.length)];

            // 头像用固定种子，保证每次相同
            String avatarUrl = "https://api.dicebear.com/7.x/adventurer/svg?seed=" + accountHash;

            int level = rand.nextInt(200) + 1;

            // 从 games 表选 N 个（同样用 seed，固定）
            List<Game> allGames = gameMapper.selectList(null);
            Map<String, Game> uniqueByBase = new LinkedHashMap<>();
            for (Game g : allGames) {
                String base = g.getName() == null ? "" : g.getName()
                        .replaceAll("\\s*(豪华版|终极版|标准版|Definitive|Deluxe|Ultimate)$", "").trim();
                uniqueByBase.putIfAbsent(base, g);
            }
            List<Game> pool = new ArrayList<>(uniqueByBase.values());
            Collections.shuffle(pool, rand);
            int libCount = Math.min(pool.size(), rand.nextInt(6) + 3);
            List<Game> owned = pool.subList(0, libCount);

            // 新建 SteamAccount
            account = new SteamAccount();
            account.setSteamId64(steamId);
            account.setSteamName(steamName);
            account.setAvatarUrl(avatarUrl);
            account.setRegion(region);
            account.setLevel(level);
            account.setAccountHash(accountHash);
            steamAccountMapper.insert(account);

            // 写入 user_steam_bindings（新账号绑定）
            UserSteamBinding b = new UserSteamBinding();
            b.setUserId(userId);
            b.setSteamAccountId(account.getId());
            b.setIsCurrent(1);
            bindingMapper.insert(b);

            // 写入 steam_libraries
            for (Game g : owned) {
                SteamLibrary sl = new SteamLibrary();
                sl.setSteamAccountId(account.getId());
                sl.setGameId(null);  // 真实 Steam appid 应从 Steam API 获取，造数据先置空
                sl.setGameName(g.getName());
                sl.setGameImage(g.getImage());
                sl.setPlaytime(rand.nextInt(200) + 5);  // 小时，5-204
                steamLibraryMapper.insert(sl);
            }
        }

        // 更新 users 表 —— 只写外键 steam_account_id（绑定状态由 steamAccountId != null 自动推导）
        u.setSteamAccountId(account.getId());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        // 实时刷新 stats（刷新 steam_accounts 表）
        steamService.refreshUserSteamStats(userId);
        account = steamAccountMapper.selectById(account.getId());

        // 返回前端
        List<SteamLibrary> libs = steamService.getLibraryByAccountId(account.getId());

        // 预查全部 games，按英文名建索引（O(1) 匹配，避免 N+1 查询）
        List<Game> allGames = gameMapper.selectList(null);
        java.util.Map<String, Game> gameIndex = new java.util.HashMap<>();
        for (Game g : allGames) {
            if (g.getName() != null) gameIndex.put(g.getName().toLowerCase(), g);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("steam_account_id", account.getId());
        resp.put("steam_id", account.getSteamId64());
        resp.put("steam_name", account.getSteamName());
        resp.put("steam_avatar_url", account.getAvatarUrl());
        resp.put("steam_region", account.getRegion());
        resp.put("steam_level", account.getLevel());
        resp.put("steam_game_count", account.getGameCount());
        resp.put("steam_account_value", account.getAccountValue());
        resp.put("steam_playtime", account.getPlaytime());
        resp.put("steam_bound", true);
        resp.put("library", libs.stream().map(sl -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("game_id", sl.getGameId());
            m.put("game_name", sl.getGameName());

            // 关联本地 games 表：英文名 → 中文名 + 更可靠的封面
            Game localGame = sl.getGameName() != null ? gameIndex.get(sl.getGameName().toLowerCase()) : null;
            if (localGame != null) {
                // 中文名有则用中文名，没有则回退英文名
                m.put("game_name_cn", localGame.getNameCn() != null ? localGame.getNameCn() : sl.getGameName());
                // 封面优先用本地 games.image（本地资源更稳定），否则用 Steam 原始图
                String img = (localGame.getImage() != null && !localGame.getImage().isBlank())
                        ? localGame.getImage() : sl.getGameImage();
                m.put("game_image", img);
            } else {
                m.put("game_name_cn", sl.getGameName());   // 没匹配到就用英文名本身
                m.put("game_image", sl.getGameImage());
            }

            m.put("playtime", sl.getPlaytime());
            return m;
        }).collect(Collectors.toList()));

        return Result.success(resp);
    }

    /**
     * 解绑 Steam 账号
     * <p>
     * 仅解除绑定关系：删除 user_steam_bindings 记录、清空 users.steamAccountId 外键。
     * steam_accounts 和 steam_libraries 的原始数据保留（重新绑定同一 userId 时会恢复关联）。
     * </p>
     *
     * @param userId 要解绑 Steam 的用户 ID
     * @return {@link Result} 成功返回 null
     */
    @DeleteMapping("/unbind/{userId}")
    public Result<Void> unbind(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return Result.error("用户不存在");

        // 1. 从 user_steam_bindings 删除绑定记录
        bindingMapper.delete(
            new QueryWrapper<UserSteamBinding>().eq("user_id", userId)
        );

        // 2. 清 users 表外键（Steam 账号级统计在 steam_accounts 表，解绑不影响）
        u.setSteamAccountId(null);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        // 注意：steam_accounts / steam_libraries 原始数据保留（Steam 官方数据不变）
        // 重新绑定时会重新关联同一 steam_account（steam_accounts.id 不变）

        return Result.success(null);
    }

    /**
     * 获取用户的 Steam 账号资料及统计信息
     * <p>
     * 每次请求都会先调用 steamService.refreshUserSteamStats() 实时刷新账号统计（游戏数、账号价值、总游戏时长等）。
     * 若用户未绑定 Steam，则返回的 steam_bound=false，其余字段为 null 或默认值。
     * </p>
     *
     * @param userId 用户 ID
     * @return {@link Result} 包含 steam_id、steam_name、steam_avatar_url、steam_region、steam_level、
     *         steam_game_count、steam_account_value、steam_playtime、steam_bound、steam_bound_at
     */
    @GetMapping("/info/{userId}")
    public Result<Map<String, Object>> getInfo(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return Result.error("用户不存在");

        if (u.getSteamAccountId() != null) {
            // 实时刷新统计（刷新 steam_accounts 表）
            steamService.refreshUserSteamStats(userId);
        }

        // Steam 资料 + 统计全部从 steam_accounts 实时查
        SteamAccount account = u.getSteamAccountId() != null
                ? steamAccountMapper.selectById(u.getSteamAccountId())
                : null;

        // steam_bound_at 从 user_steam_bindings 取最新绑定时间
        LocalDateTime boundAt = null;
        if (u.getSteamAccountId() != null) {
            QueryWrapper<UserSteamBinding> bw = new QueryWrapper<>();
            bw.eq("user_id", userId).eq("steam_account_id", u.getSteamAccountId())
              .orderByDesc("bound_at").last("LIMIT 1");
            UserSteamBinding latestBinding = bindingMapper.selectOne(bw);
            if (latestBinding != null) boundAt = latestBinding.getBoundAt();
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("steam_id", account != null ? account.getSteamId64() : null);
        resp.put("steam_name", account != null ? account.getSteamName() : null);
        resp.put("steam_avatar_url", account != null ? account.getAvatarUrl() : null);
        resp.put("steam_region", account != null ? account.getRegion() : null);
        resp.put("steam_level", account != null ? account.getLevel() : null);
        resp.put("steam_game_count", account != null && account.getGameCount() != null ? account.getGameCount() : 0);
        resp.put("steam_account_value", account != null && account.getAccountValue() != null ? account.getAccountValue() : BigDecimal.ZERO);
        resp.put("steam_playtime", account != null && account.getPlaytime() != null ? account.getPlaytime() : 0);
        resp.put("steam_bound", u.getSteamAccountId() != null);
        resp.put("steam_bound_at", boundAt);
        return Result.success(resp);
    }

    /**
     * 获取用户的 Steam 游戏库列表
     * <p>
     * 若用户未绑定 Steam（steamAccountId 为 null），则直接返回空数组而非报错。
     * </p>
     *
     * @param userId 用户 ID
     * @return {@link Result} 包含游戏库数组，每项含 game_id、game_name、game_image、playtime（游戏时长，小时）
     */
    @GetMapping("/library/{userId}")
    public Result<List<Map<String, Object>>> getLibrary(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getSteamAccountId() == null) {
            return Result.success(new ArrayList<>());
        }
        List<SteamLibrary> list = steamService.getLibraryByAccountId(u.getSteamAccountId());

        // 预查 games 建索引
        List<Game> allGames = gameMapper.selectList(null);
        java.util.Map<String, Game> gameIndex = new java.util.HashMap<>();
        for (Game g : allGames) {
            if (g.getName() != null) gameIndex.put(g.getName().toLowerCase(), g);
        }

        List<Map<String, Object>> resp = list.stream().map(sl -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("game_id", sl.getGameId());
            m.put("game_name", sl.getGameName());

            Game localGame = sl.getGameName() != null ? gameIndex.get(sl.getGameName().toLowerCase()) : null;
            if (localGame != null) {
                m.put("game_name_cn", localGame.getNameCn() != null ? localGame.getNameCn() : sl.getGameName());
                String img = (localGame.getImage() != null && !localGame.getImage().isBlank())
                        ? localGame.getImage() : sl.getGameImage();
                m.put("game_image", img);
            } else {
                m.put("game_name_cn", sl.getGameName());
                m.put("game_image", sl.getGameImage());
            }

            m.put("playtime", sl.getPlaytime());
            return m;
        }).collect(Collectors.toList());
        return Result.success(resp);
    }

    /**
     * 查重：判断用户的 Steam 库存中是否已拥有某游戏
     * <p>
     * 内部会对 gameName 做"去除版本后缀"的模糊匹配（如"豪华版"、"Definitive Edition"），
     * 兼容用户换不同版本名但实为同一款游戏的情况。
     * </p>
     *
     * @param userId  用户 ID
     * @param gameId  可选：本地 games 表的游戏 ID（Long 类型）
     * @param gameName 可选：游戏名称（优先匹配）；与 gameId 二者至少传一个
     * @return {@link Result} 包含 owned（是否已拥有）、steam_bound（是否已绑定 Steam）、
     *         owned_game_name（可选，已拥有时返回 Steam 库存中实际匹配到的游戏名）
     */
    @GetMapping("/check-ownership")
    public Result<Map<String, Object>> checkOwnership(
            @RequestParam String userId,
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String gameName) {

        Map<String, Object> resp = new LinkedHashMap<>();

        User u = userMapper.selectById(userId);
        if (u == null) {
            resp.put("owned", false);
            resp.put("steam_bound", false);
            return Result.success(resp);
        }
        boolean bound = u.getSteamAccountId() != null;
        if (!bound) {
            resp.put("owned", false);
            resp.put("steam_bound", false);
            return Result.success(resp);
        }

        boolean owned = steamService.isGameOwned(u.getSteamAccountId(), gameId, gameName);

        resp.put("owned", owned);
        resp.put("steam_bound", true);
        if (owned) {
            QueryWrapper<SteamLibrary> qw = new QueryWrapper<>();
            qw.eq("steam_account_id", u.getSteamAccountId());
            if (gameId != null) qw.eq("game_id", gameId);
            if (gameName != null && !gameName.isBlank()) {
                String base = gameName.replaceAll("\\s*(豪华版|终极版|标准版|Definitive|Deluxe|Ultimate)$", "").trim();
                qw.and(w -> w.eq("game_name", gameName).or().like("game_name", base));
            }
            qw.last("LIMIT 1");
            SteamLibrary sl = steamLibraryMapper.selectOne(qw);
            if (sl != null) resp.put("owned_game_name", sl.getGameName());
        }
        return Result.success(resp);
    }
}

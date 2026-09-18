package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.SteamAccount;
import com.steampy.entity.SteamLibrary;
import com.steampy.entity.User;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.SteamAccountMapper;
import com.steampy.mapper.SteamLibraryMapper;
import com.steampy.mapper.UserMapper;
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

    // ======== Steam 实时价格缓存 ========
    private static final Pattern STEAM_APPID_PATTERN = Pattern.compile("/steam/apps/(\\d+)/");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int STEAM_CONNECT_TIMEOUT = 8000;
    private static final int STEAM_READ_TIMEOUT = 10000;
    // appid -> {initial, final, discount_percent, currency, ts}
    private final Map<String, Object[]> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L; // 10 分钟

    // 信任所有 SSL 证书（开发环境用，解决 PKIX 证书链缺失）
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
     * GET /api/steam/prices?gameIds=16,17,18
     * 返回 [{ game_id, appid, steam_initial, steam_final, discount_percent, currency, from_steam }]
     * 不支持 Steam API 的游戏（image 不含 appid）自动跳过或 fallback 传 DB 价
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

    private static String extractAppid(String imageUrl) {
        if (imageUrl == null) return null;
        Matcher m = STEAM_APPID_PATTERN.matcher(imageUrl);
        return m.find() ? m.group(1) : null;
    }

    // ========== 模拟绑定 ==========
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
            // 把 userId 加入 bind_user_ids 列表（如果还没加）
            String bindIds = account.getBindUserIds();
            if (bindIds == null) bindIds = "";
            if (!bindIds.contains(userId)) {
                account.setBindUserIds(bindIds.isEmpty() ? userId : bindIds + "," + userId);
                account.setUpdatedAt(LocalDateTime.now());
                steamAccountMapper.updateById(account);
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
            account.setBindUserIds(userId);
            steamAccountMapper.insert(account);

            // 写入 steam_libraries
            for (Game g : owned) {
                SteamLibrary sl = new SteamLibrary();
                sl.setSteamAccountId(account.getId());
                sl.setGameId(g.getId());
                sl.setGameName(g.getName());
                sl.setGameImage(g.getImageUrl() != null ? g.getImageUrl() : g.getImage());
                sl.setPlaytime(rand.nextInt(500) + 10);
                steamLibraryMapper.insert(sl);
            }
        }

        // 更新 users 表 —— 只写绑定状态和外键，不再双写 Steam 资料（走 steam_accounts 回填）
        u.setSteamAccountId(account.getId());
        u.setSteamBound(true);
        u.setSteamBoundAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        // 实时刷新 stats
        steamService.refreshUserSteamStats(userId);
        User latest = userMapper.selectById(userId);

        // 返回前端
        List<SteamLibrary> libs = steamService.getLibraryByAccountId(account.getId());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("steam_id", account.getSteamId64());
        resp.put("steam_name", account.getSteamName());
        resp.put("steam_avatar_url", account.getAvatarUrl());
        resp.put("steam_region", account.getRegion());
        resp.put("steam_level", account.getLevel());
        resp.put("steam_game_count", latest.getSteamGameCount());
        resp.put("steam_account_value", latest.getSteamAccountValue());
        resp.put("steam_playtime", latest.getSteamPlaytime());
        resp.put("steam_bound", true);
        resp.put("library", libs.stream().map(sl -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("game_id", sl.getGameId());
            m.put("game_name", sl.getGameName());
            m.put("game_image", sl.getGameImage());
            m.put("playtime", sl.getPlaytime());
            return m;
        }).collect(Collectors.toList()));

        return Result.success(resp);
    }

    // ========== 解绑（不删除 steam_account + steam_libraries，仅重置用户绑定标志）==========
    @DeleteMapping("/unbind/{userId}")
    public Result<Void> unbind(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return Result.error("用户不存在");

        // 保留 steam_account_id 和 account_hash，这样重新绑定时会恢复同一账号
        u.setSteamBound(false);
        u.setSteamBoundAt(null);
        u.setSteamGameCount(0);
        u.setSteamAccountValue(BigDecimal.ZERO);
        u.setSteamPlaytime(0);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        // 注意：不删除 steam_accounts 和 steam_libraries 里的数据！
        // 不清理 users.steam_account_id / account_hash —— 重新绑定时能恢复

        return Result.success(null);
    }

    // ========== 获取 Steam 信息 ==========
    @GetMapping("/info/{userId}")
    public Result<Map<String, Object>> getInfo(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null) return Result.error("用户不存在");

        if (Boolean.TRUE.equals(u.getSteamBound())) {
            // 实时刷新统计
            steamService.refreshUserSteamStats(userId);
            u = userMapper.selectById(userId);
        }

        // Steam 资料从 steam_accounts 实时查（users 表已删快照字段）
        SteamAccount account = u.getSteamAccountId() != null
                ? steamAccountMapper.selectById(u.getSteamAccountId())
                : null;

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("steam_id", account != null ? account.getSteamId64() : null);
        resp.put("steam_name", account != null ? account.getSteamName() : null);
        resp.put("steam_avatar_url", account != null ? account.getAvatarUrl() : null);
        resp.put("steam_region", account != null ? account.getRegion() : null);
        resp.put("steam_level", account != null ? account.getLevel() : null);
        resp.put("steam_game_count", u.getSteamGameCount() != null ? u.getSteamGameCount() : 0);
        resp.put("steam_account_value", u.getSteamAccountValue() != null ? u.getSteamAccountValue() : BigDecimal.ZERO);
        resp.put("steam_playtime", u.getSteamPlaytime() != null ? u.getSteamPlaytime() : 0);
        resp.put("steam_bound", Boolean.TRUE.equals(u.getSteamBound()));
        resp.put("steam_bound_at", u.getSteamBoundAt());
        return Result.success(resp);
    }

    // ========== 获取 Steam 游戏库 ==========
    @GetMapping("/library/{userId}")
    public Result<List<Map<String, Object>>> getLibrary(@PathVariable String userId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getSteamAccountId() == null) {
            return Result.success(new ArrayList<>());
        }
        List<SteamLibrary> list = steamService.getLibraryByAccountId(u.getSteamAccountId());

        List<Map<String, Object>> resp = list.stream().map(sl -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("game_id", sl.getGameId());
            m.put("game_name", sl.getGameName());
            m.put("game_image", sl.getGameImage());
            m.put("playtime", sl.getPlaytime());
            m.put("owned_at", sl.getOwnedAt());
            return m;
        }).collect(Collectors.toList());
        return Result.success(resp);
    }

    // ========== 查重 ==========
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
        boolean bound = Boolean.TRUE.equals(u.getSteamBound());
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

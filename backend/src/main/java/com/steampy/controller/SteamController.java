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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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

            // 写入 steam_libraries（用 steam_account_id，不是 user_id）
            for (Game g : owned) {
                SteamLibrary sl = new SteamLibrary();
                sl.setUserId(userId); // 保留 user_id 方便迁移兼容
                sl.setSteamAccountId(account.getId());
                sl.setGameId(g.getId());
                sl.setGameName(g.getName());
                sl.setGameImage(g.getImageUrl() != null ? g.getImageUrl() : g.getImage());
                sl.setPlaytime(rand.nextInt(500) + 10);
                steamLibraryMapper.insert(sl);
            }
        }

        // 更新 users 表（无论新旧账号都要）
        u.setSteamAccountId(account.getId());
        u.setAccountHash(accountHash);
        u.setSteamId(account.getSteamId64());
        u.setSteamName(account.getSteamName());
        u.setSteamAvatarUrl(account.getAvatarUrl());
        u.setSteamRegion(account.getRegion());
        u.setSteamLevel(account.getLevel());
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
            // 实时刷新
            steamService.refreshUserSteamStats(userId);
            u = userMapper.selectById(userId);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("steam_id", u.getSteamId());
        resp.put("steam_name", u.getSteamName());
        resp.put("steam_avatar_url", u.getSteamAvatarUrl());
        resp.put("steam_region", u.getSteamRegion());
        resp.put("steam_level", u.getSteamLevel());
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

package com.steampy.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.entity.Game;
import com.steampy.entity.SteamAccount;
import com.steampy.entity.SteamLibrary;
import com.steampy.entity.User;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.SteamAccountMapper;
import com.steampy.mapper.SteamLibraryMapper;
import com.steampy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Steam 相关共享服务：统计刷新、查重、账号查找等
 *
 * 设计要点：
 *  - 库存查询全部走 steam_account_id（steam_libraries 表存 steam_account_id，不再只靠 user_id）
 *  - Steam 账号数据持久化到 steam_accounts 表，解绑不会删除
 */
@Service
public class SteamService {

    @Autowired
    private SteamLibraryMapper steamLibraryMapper;
    @Autowired
    private SteamAccountMapper steamAccountMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GameMapper gameMapper;

    /**
     * 用 userId 生成稳定的 hash（用于 account_hash 字段查找）
     */
    public static String hashUserId(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(userId.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 32);
        } catch (Exception e) {
            // fallback：直接用 userId
            return userId;
        }
    }

    /**
     * 根据 userId 查找其关联的 SteamAccount（通过 users.steam_account_id）
     */
    public SteamAccount findAccountByUserId(String userId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getSteamAccountId() == null) return null;
        return steamAccountMapper.selectById(u.getSteamAccountId());
    }

    /**
     * 根据 account_hash 查找 SteamAccount
     */
    public SteamAccount findAccountByHash(String accountHash) {
        QueryWrapper<SteamAccount> qw = new QueryWrapper<>();
        qw.eq("account_hash", accountHash).last("LIMIT 1");
        return steamAccountMapper.selectOne(qw);
    }

    /**
     * 根据 steam_account_id 查库存条目
     */
    public List<SteamLibrary> getLibraryByAccountId(Long accountId) {
        QueryWrapper<SteamLibrary> qw = new QueryWrapper<>();
        qw.eq("steam_account_id", accountId).orderByDesc("playtime");
        return steamLibraryMapper.selectList(qw);
    }

    /**
     * 实时刷新用户 stats（game_count / account_value / playtime）
     */
    public void refreshUserSteamStats(String userId) {
        User u = userMapper.selectById(userId);
        if (u == null || !Boolean.TRUE.equals(u.getSteamBound())) return;

        Long accountId = u.getSteamAccountId();
        if (accountId == null) return;

        QueryWrapper<SteamLibrary> qw = new QueryWrapper<>();
        qw.eq("steam_account_id", accountId);
        List<SteamLibrary> libs = steamLibraryMapper.selectList(qw);

        int count = libs.size();
        BigDecimal value = BigDecimal.ZERO;
        int playtime = 0;

        for (SteamLibrary sl : libs) {
            if (sl.getPlaytime() != null) playtime += sl.getPlaytime();

            Game g = null;
            if (sl.getGameId() != null) {
                g = gameMapper.selectById(sl.getGameId());
            } else if (sl.getGameName() != null) {
                QueryWrapper<Game> gq = new QueryWrapper<>();
                gq.eq("name", sl.getGameName());
                g = gameMapper.selectOne(gq);
            }
            if (g != null && g.getPrice() != null) {
                value = value.add(g.getPrice());
            }
        }

        u.setSteamGameCount(count);
        u.setSteamAccountValue(value.setScale(2, RoundingMode.HALF_UP));
        u.setSteamPlaytime(playtime);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    /**
     * 查重：指定 steam_account_id 的 Steam 库中是否已有指定游戏
     */
    public boolean isGameOwned(Long accountId, Long gameId, String gameName) {
        if (accountId == null) return false;
        QueryWrapper<SteamLibrary> qw = new QueryWrapper<>();
        qw.eq("steam_account_id", accountId);
        if (gameId != null) {
            qw.eq("game_id", gameId);
        }
        if (gameName != null && !gameName.isBlank()) {
            String base = gameName.replaceAll("\\s*(豪华版|终极版|标准版|Definitive|Deluxe|Ultimate)$", "").trim();
            qw.and(w -> w.eq("game_name", gameName).or().like("game_name", base));
        }
        Long cnt = steamLibraryMapper.selectCount(qw);
        return cnt != null && cnt > 0;
    }

    /**
     * 查重（从 userId 入口：先查 steam_account_id）
     */
    public boolean isGameOwnedByUser(String userId, Long gameId, String gameName) {
        User u = userMapper.selectById(userId);
        if (u == null) return false;
        return isGameOwned(u.getSteamAccountId(), gameId, gameName);
    }
}

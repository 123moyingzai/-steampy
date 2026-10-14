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
 * 核心职责：提供跨模块复用的 Steam 账号与库存操作能力，包括账号哈希查找、
 *           实时统计刷新、游戏库存查重等底层业务逻辑
 *
 * 设计要点：
 *  - 库存查询全部走 steam_account_id（steam_libraries 表存 steam_account_id，不再只靠 user_id）
 *  - Steam 账号数据持久化到 steam_accounts 表，解绑不会删除
 *
 * @author steampy
 * @date 2026-10-14
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
     * 用 userId 生成稳定的 SHA-256 哈希（截取前 32 位），用于 account_hash 字段查找
     * 若 SHA-256 不可用则 fallback 为原 userId 本身
     *
     * @param userId 待哈希的用户标识
     * @return 32 位十六进制哈希字符串，或 fallback 后的原 userId
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
     * 根据 userId 查找其关联的 SteamAccount（通过 users.steam_account_id 外键）
     * 若用户不存在或未绑定 Steam 账号则返回 null
     *
     * @param userId 用户唯一标识
     * @return 关联的 SteamAccount 实体，或 null
     */
    public SteamAccount findAccountByUserId(String userId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getSteamAccountId() == null) return null;
        return steamAccountMapper.selectById(u.getSteamAccountId());
    }

    /**
     * 根据 account_hash 查找 SteamAccount
     * 哈希值来源于 SteamService.hashUserId 生成的 account_hash 字段
     *
     * @param accountHash Steam 账号哈希值
     * @return 匹配的 SteamAccount 实体，或 null
     */
    public SteamAccount findAccountByHash(String accountHash) {
        QueryWrapper<SteamAccount> qw = new QueryWrapper<>();
        qw.eq("account_hash", accountHash).last("LIMIT 1");
        return steamAccountMapper.selectOne(qw);
    }

    /**
     * 根据 steam_account_id 查询该账号名下的全部游戏库条目
     * 结果按游戏时长 playtime 倒序排列
     *
     * @param accountId Steam 账号唯一标识
     * @return 该账号的 SteamLibrary 列表，可能为空
     */
    public List<SteamLibrary> getLibraryByAccountId(String accountId) {
        QueryWrapper<SteamLibrary> qw = new QueryWrapper<>();
        qw.eq("steam_account_id", accountId).orderByDesc("playtime");
        return steamLibraryMapper.selectList(qw);
    }

    /**
     * 实时刷新 Steam 账号统计字段（game_count / account_value / playtime）
     * 数据直接回写 steam_accounts 表（统计为账号级属性）
     * 若用户未绑定 Steam 账号则静默跳过
     *
     * @param userId 需要刷新统计的用户标识
     */
    public void refreshUserSteamStats(String userId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getSteamAccountId() == null) return;

        String accountId = u.getSteamAccountId();
        SteamAccount account = steamAccountMapper.selectById(accountId);
        if (account == null) return;

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

        account.setGameCount(count);
        account.setAccountValue(value.setScale(2, RoundingMode.HALF_UP));
        account.setPlaytime(playtime);
        account.setUpdatedAt(LocalDateTime.now());
        steamAccountMapper.updateById(account);
    }

    /**
     * 查重：指定 steam_account_id 的 Steam 库中是否已拥有指定游戏
     * 支持通过 gameId 或去版本后缀后的 gameName 进行匹配
     *
     * @param accountId Steam 账号标识（null 直接返回 false）
     * @param gameId    游戏数据库主键（精确匹配）
     * @param gameName  游戏名称（匹配全名或去除"豪华版/终极版/Definitive"等后缀后的名称）
     * @return true 表示已拥有该游戏，false 表示未拥有或 accountId 为 null
     */
    public boolean isGameOwned(String accountId, Long gameId, String gameName) {
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
     * 查重入口（以 userId 为入口）：先通过 userId 查 steam_account_id，
     * 再调用 isGameOwned 判断账号是否已拥有指定游戏
     *
     * @param userId   用户标识
     * @param gameId   游戏数据库主键
     * @param gameName 游戏名称
     * @return true 表示该用户绑定的 Steam 账号已拥有该游戏，false 表示未拥有或用户不存在
     */
    public boolean isGameOwnedByUser(String userId, Long gameId, String gameName) {
        User u = userMapper.selectById(userId);
        if (u == null) return false;
        return isGameOwned(u.getSteamAccountId(), gameId, gameName);
    }
}

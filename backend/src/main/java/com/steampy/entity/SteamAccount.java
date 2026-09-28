package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟 Steam 账号（持久化，保证同一 userId 每次绑定都返回相同数据）
 * Steam 游戏库统计直接存这里，不再冗余到 users 表
 */
@Data
@TableName("steam_accounts")
public class SteamAccount {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** Steam ID64（17位数字，唯一） */
    private String steamId64;

    /** Steam 昵称 */
    private String steamName;

    /** 头像 URL */
    private String avatarUrl;

    /** 地区 */
    private String region;

    /** Steam 等级 */
    private Integer level;

    /** hash(userId)，用于快速查找同一用户之前创建过的账号 */
    private String accountHash;

    // ========== Steam 游戏库统计（缓存值，由 SteamService.refreshUserSteamStats 刷新）==========
    /** Steam 游戏库数量 */
    private Integer gameCount;
    /** Steam 账号资产价值（从 games 表 price 聚合） */
    private BigDecimal accountValue;
    /** Steam 总游戏时长(分钟) */
    private Integer playtime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


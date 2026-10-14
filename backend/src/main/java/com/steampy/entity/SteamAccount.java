package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Steam 账号实体
 * 对应表：steam_accounts
 * 核心职责：模拟并持久化用户绑定的 Steam 账号信息，包括 SteamID、昵称、头像、地区、游戏库统计等
 * 同一用户每次绑定返回相同数据，Steam 游戏库统计也直接存这里，不再冗余到 users 表
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("steam_accounts")
public class SteamAccount {
    /** Steam账号记录ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** Steam ID64（17位数字，Steam 平台唯一标识） */
    private String steamId64;

    /** Steam 平台显示昵称 */
    private String steamName;

    /** Steam 头像 URL */
    private String avatarUrl;

    /** Steam 账号地区（如 CN、US、Global） */
    private String region;

    /** Steam 等级 */
    private Integer level;

    /** hash(userId)，用于快速查找同一用户之前创建过的 Steam 账号 */
    private String accountHash;

    // ========== Steam 游戏库统计（缓存值，由 SteamService.refreshUserSteamStats 刷新）==========
    /** Steam 游戏库游戏数量 */
    private Integer gameCount;
    /** Steam 账号资产总价值（从 games 表 price 聚合，单位：元） */
    private BigDecimal accountValue;
    /** Steam 总游戏时长（单位：分钟） */
    private Integer playtime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


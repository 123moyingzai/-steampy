package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 对应表：users
 * 核心职责：存储平台用户的账号基础信息，包括登录凭证、联系方式、头像、用户类型等
 * Steam 账号级信息和游戏库统计在 steam_accounts 表，通过 steamAccountId 关联
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("users")
public class User {
    /** 用户ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 登录用户名 */
    private String username;
    /** 密码哈希值（加密存储，不存明文） */
    private String passwordHash;
    /** 昵称（优先展示，未设置时回退为 username） */
    private String nickname;
    /** 手机号（可选） */
    private String phone;
    /** 电子邮箱（可选） */
    private String email;
    /** 头像 URL */
    private String avatarUrl;
    /** 用户类型：buyer=普通买家、seller=卖家、admin=管理员 */
    private String userType;
    /** 账号是否启用（true=正常，false=已封禁） */
    private Boolean isActive;

    /** Steam 账号外键（DB 列，关联 steam_accounts.id）—— Steam 账号级信息在 steam_accounts 表 */
    private String steamAccountId;

    // ========== 以下字段不是 DB 列，查询时动态回填 ==========
    /** 是否已绑定 Steam 账号（由 steamAccountId != null 实时推导，非 DB 字段） */
    @TableField(exist = false)
    private Boolean steamBound;
    /** Steam 绑定时间（从 user_steam_bindings.bound_at 最新值回填，非 DB 字段） */
    @TableField(exist = false)
    private LocalDateTime steamBoundAt;
    /** Steam 游戏库数量（从 steam_accounts.gameCount 回填，非 DB 字段） */
    @TableField(exist = false)
    private Integer steamGameCount;
    /** Steam 账号资产价值（从 steam_accounts.accountValue 回填，单位：元，非 DB 字段） */
    @TableField(exist = false)
    private BigDecimal steamAccountValue;
    /** Steam 总游戏时长（从 steam_accounts.playtime 回填，单位：分钟，非 DB 字段） */
    @TableField(exist = false)
    private Integer steamPlaytime;
    /** Steam ID64（从 steam_accounts.steamId64 回填，非 DB 字段） */
    @TableField(exist = false)
    private String steamId;
    /** Steam 昵称（从 steam_accounts.steamName 回填，非 DB 字段） */
    @TableField(exist = false)
    private String steamName;
    /** Steam 头像 URL（从 steam_accounts.avatarUrl 回填，非 DB 字段） */
    @TableField(exist = false)
    private String steamAvatarUrl;
    /** Steam 账号地区（从 steam_accounts.region 回填，非 DB 字段） */
    @TableField(exist = false)
    private String steamRegion;
    /** Steam 等级（从 steam_accounts.level 回填，非 DB 字段） */
    @TableField(exist = false)
    private Integer steamLevel;

    /** 注册时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


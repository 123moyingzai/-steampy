package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.INPUT)
    private String id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private String userType;
    private Boolean isActive;

    // Steam 外键（DB 列）—— Steam 账号级信息（含游戏库统计）在 steam_accounts 表
    private String steamAccountId;

    // ========== 以下字段不是 DB 列，查询时回填 ==========
    @TableField(exist = false)
    private Boolean steamBound;       // 由 steamAccountId != null 实时推导
    @TableField(exist = false)
    private LocalDateTime steamBoundAt; // 由 user_steam_bindings.bound_at 最新值回填
    @TableField(exist = false)
    private Integer steamGameCount;   // 从 steam_accounts.gameCount 回填
    @TableField(exist = false)
    private BigDecimal steamAccountValue; // 从 steam_accounts.accountValue 回填
    @TableField(exist = false)
    private Integer steamPlaytime;    // 从 steam_accounts.playtime 回填
    @TableField(exist = false)
    private String steamId;
    @TableField(exist = false)
    private String steamName;
    @TableField(exist = false)
    private String steamAvatarUrl;
    @TableField(exist = false)
    private String steamRegion;
    @TableField(exist = false)
    private Integer steamLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


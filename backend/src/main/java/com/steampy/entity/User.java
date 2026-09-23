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

    // Steam 外键（DB 列）
    private Long steamAccountId;

    // Steam 统计缓存（DB 列，从 steam_libraries 聚合后刷新；可由 SteamService.refreshUserSteamStats 重算）
    private Integer steamGameCount;
    private BigDecimal steamAccountValue;
    private Integer steamPlaytime;

    // ========== 以下字段不是 DB 列，查询时回填 ==========
    @TableField(exist = false)
    private Boolean steamBound;       // 由 steamAccountId != null 实时推导
    @TableField(exist = false)
    private LocalDateTime steamBoundAt; // 由 user_steam_bindings.bound_at 最新值回填
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

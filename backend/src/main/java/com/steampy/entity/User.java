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

    // Steam 绑定状态（DB 列）
    private Boolean steamBound;
    private LocalDateTime steamBoundAt;
    private Long steamAccountId;

    // Steam 统计值（DB 列，从 steam_libraries 实时/准实时刷新）
    private Integer steamGameCount;
    private BigDecimal steamAccountValue;
    private Integer steamPlaytime;

    // ========== 以下字段不是 DB 列，查询时从 steam_accounts 回填 ==========
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

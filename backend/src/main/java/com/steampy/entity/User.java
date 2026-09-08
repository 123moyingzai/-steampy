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

    // Steam 绑定相关
    private String steamId;
    private String steamName;
    private String steamAvatarUrl;
    private String steamRegion;
    private Integer steamLevel;
    private Integer steamGameCount;
    private BigDecimal steamAccountValue;
    private Integer steamPlaytime;
    private Boolean steamBound;
    private LocalDateTime steamBoundAt;

    // Steam 账号关联（新架构）
    private Long steamAccountId;
    private String accountHash;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

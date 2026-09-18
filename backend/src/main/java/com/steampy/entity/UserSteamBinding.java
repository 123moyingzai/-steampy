package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户 ↔ Steam 账户 绑定关系（替代 steam_accounts.bind_user_ids 逗号分隔列）
 */
@Data
@TableName("user_steam_bindings")
public class UserSteamBinding {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID → users.id */
    private String userId;

    /** Steam账户ID → steam_accounts.id */
    private Long steamAccountId;

    /** 绑定时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime boundAt;

    /** 是否当前活跃绑定 */
    private Integer isCurrent;
}

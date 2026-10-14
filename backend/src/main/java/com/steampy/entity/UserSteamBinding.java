package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户 ↔ Steam 账号绑定关系实体
 * 对应表：user_steam_bindings
 * 核心职责：替代 steam_accounts.bind_user_ids 逗号分隔列，实现用户与 Steam 账号的多对多绑定关系
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("user_steam_bindings")
public class UserSteamBinding {
    /** 绑定记录ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 用户ID（关联 users.id） */
    private String userId;

    /** Steam 账号ID（关联 steam_accounts.id） */
    private String steamAccountId;

    /** 绑定时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime boundAt;

    /** 是否当前活跃绑定：1=当前使用中的绑定，0=历史绑定 */
    private Integer isCurrent;
}


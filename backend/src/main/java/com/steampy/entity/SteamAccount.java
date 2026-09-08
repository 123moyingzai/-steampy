package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模拟 Steam 账号（持久化，保证同一 userId 每次绑定都返回相同数据）
 */
@Data
@TableName("steam_accounts")
public class SteamAccount {
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 曾经绑定过的 userId 列表（逗号分隔） */
    private String bindUserIds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

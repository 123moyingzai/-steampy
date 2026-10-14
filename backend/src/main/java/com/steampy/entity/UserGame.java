package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户已购游戏实体
 * 对应表：user_games
 * 核心职责：记录每位用户已购买/拥有的游戏明细，包含来源类型、CDKey、激活状态等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("user_games")
public class UserGame {
    /** 已购记录ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 所属用户ID */
    private String userId;
    /** 关联订单ID（关联 orders.id） */
    private String orderId;
    /** 关联游戏ID（关联 games.id） */
    private String gameId;
    /** 游戏名称（冗余存储） */
    private String gameName;
    /** 游戏封面图（冗余存储） */
    private String gameImage;
    /** CDKey 激活码（CDKey 订单发货内容，加密存储） */
    private String cdkey;
    /** 游戏版本（如 标准版、豪华版） */
    private String version;
    /** 激活状态（如 pending=未激活、activated=已激活、expired=已过期） */
    private String status;
    /** 购买时间 */
    private LocalDateTime purchaseDate;
    /** 来源渠道：store=PY代购、cdkey=玩家购、official=官方购 */
    private String source;
    /** 激活时间（实际在 Steam 上兑换成功的时间） */
    private LocalDateTime activationDate;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}


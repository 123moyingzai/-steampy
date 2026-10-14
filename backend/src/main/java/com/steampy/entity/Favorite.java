package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 游戏收藏实体
 * 对应表：favorites
 * 核心职责：记录用户收藏的游戏，用于"我的收藏"列表和降价提醒
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("favorites")
public class Favorite {
    /** 收藏记录ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /** 收藏用户ID */
    private String userId;
    /** 被收藏的游戏ID（关联 games.id） */
    private String gameId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}


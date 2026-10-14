package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户通知实体
 * 对应表：user_notifications
 * 核心职责：存储系统给用户推送的通知消息，包括回复通知、点赞通知等，支持已读状态管理
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("user_notifications")
public class Notification {
    /** 通知ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 接收通知的用户ID */
    private String userId;
    /** 通知类型：reply=评论回复，like_review=点赞评论，like_reply=点赞回复 */
    private String type;
    /** 触发人用户ID（谁发送了这条通知） */
    private String actorId;
    /** 目标对象类型：review=评论，reply=回复 */
    private String targetType;
    /** 目标对象ID */
    private String targetId;
    /** 关联游戏ID，用于前端跳转到对应游戏详情页 */
    private String gameId;
    /** 通知内容片段，最多 200 字 */
    private String contentSnippet;
    /** 被回复/被点赞的原评论或原回复的内容（前端显示"引用块"用） */
    private String targetContent;
    /** 是否已读（true=已读，false=未读） */
    private Boolean isRead;
    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 触发人昵称（查询时实时回填，非数据库字段） */
    @TableField(exist = false)
    private String actorName;
    /** 触发人头像URL（查询时实时回填，非数据库字段） */
    @TableField(exist = false)
    private String actorAvatarUrl;
}

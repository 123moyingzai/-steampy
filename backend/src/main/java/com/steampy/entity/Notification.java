package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_notifications")
public class Notification {
    @TableId(type = IdType.INPUT)
    private String id;
    /** 接收通知的用户 */
    private String userId;
    /** reply | like_review | like_reply */
    private String type;
    /** 触发人 */
    private String actorId;
    private String actorName;
    /** review | reply */
    private String targetType;
    private String targetId;
    /** 用于前端跳转 GameDetail */
    private String gameId;
    /** 内容片段，最多 200 字 */
    private String contentSnippet;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

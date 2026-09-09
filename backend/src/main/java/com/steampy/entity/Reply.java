package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_replies")
public class Reply {
    @TableId(type = IdType.INPUT)
    private String id;
    private String reviewId;
    /** 非空时表示回复的是某条子评论（也展示在父评论下，保持扁平） */
    private String parentReplyId;
    private String replyToUserId;
    private String replyToUserName;
    private String userId;
    private String userName;
    private String content;
    private Integer likesCount;
    /** 被举报次数 */
    private Integer reportCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    /** 当前登录用户是否已点赞 */
    private Boolean liked;
}

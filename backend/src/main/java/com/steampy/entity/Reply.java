package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论回复实体
 * 对应表：review_replies
 * 核心职责：存储用户对评论的回复内容，支持二级回复（回复某条子回复），并在查询时聚合点赞数和头像昵称
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("review_replies")
public class Reply {
    /** 回复ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 所属评论ID（关联 reviews.id） */
    private String reviewId;
    /** 父回复ID，非空时表示这条回复是回复某条子回复（展示时扁平挂在父评论下） */
    private String parentReplyId;
    /** 被回复的目标用户ID */
    private String replyToUserId;
    /** 回复发起用户ID */
    private String userId;
    /** 回复内容 */
    private String content;
    /** 点赞数（查询时从 review_reply_likes 表 COUNT 得出，不映射 DB 列） */
    @TableField(exist = false)
    private Integer likesCount;
    /** 被举报次数，达到阈值进入审核队列 */
    private Integer reportCount;
    /** 回复状态：0=正常，1=已删除，2=被屏蔽 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 当前登录用户是否已点赞（仅查询时填充，不映射 DB） */
    @TableField(exist = false)
    private Boolean liked;
    /** 回复者实时头像（查询时从 users 表回填） */
    @TableField(exist = false)
    private String avatarUrl;
    /** 回复者实时昵称（查询时从 users 表回填） */
    @TableField(exist = false)
    private String displayName;
    /** 被回复者实时昵称（查询时从 users 表回填） */
    @TableField(exist = false)
    private String replyToDisplayName;
    /** 被回复者实时头像（查询时从 users 表回填） */
    @TableField(exist = false)
    private String replyToAvatarUrl;
}

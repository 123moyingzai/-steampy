package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏评论实体
 * 对应表：reviews
 * 核心职责：存储用户对游戏的评价内容，支持推荐/不推荐、配图、审核状态、点赞数等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("reviews")
@JsonInclude(JsonInclude.Include.ALWAYS)   // 所有字段（含 null 和空 List）都序列化给前端
public class Review {
    /** 评论ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 关联游戏ID（关联 games.id） */
    private String gameId;
    /** 评论用户ID */
    private String userId;
    /** 是否推荐：1=推荐，0=不推荐（DB ENUM('0','1')，MyBatis-Plus 自动转 Integer） */
    private Integer recommend;
    /** 评论正文 */
    private String content;
    /** 评论配图URL列表（逗号分隔） */
    private String images;
    /** 审核状态：0=待审核，1=已通过，2=已拒绝 */
    private Integer status;
    /** 点赞数（查询时从 review_likes 表 COUNT 得出，不映射 DB 列） */
    @TableField(exist = false)
    private Integer likesCount;
    /** 回复数（查询时从 review_replies 表 COUNT 得出） */
    private Integer repliesCount;
    /** 被举报次数，达到阈值进入审核队列 */
    private Integer reportCount;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 游戏名称（查询时从 games 表回填，不映射 DB） */
    @TableField(exist = false)
    private String gameName;
    /** 评论者实时头像（查询时从 users 表回填） */
    @TableField(exist = false)
    private String avatarUrl;
    /** 评论者实时昵称（查询时从 users 表回填，优先 nickname，fallback username） */
    @TableField(exist = false)
    private String displayName;
    /** 当前登录用户是否已点赞（仅查询时填充，不映射 DB） */
    @TableField(exist = false)
    private Boolean liked;
    /** 举报理由列表（仅管理员查询时从 reports 表回填，不映射 DB）
     *  每项是一个 ReportReason DTO，含 reporterName / reason / createdAt
     */
    @TableField(exist = false)
    private List<ReportReason> reportReasons;

    /**
     * 举报理由 DTO（内部类，避免 List<Map> 的 Jackson 序列化问题）
     */
    @Data
    public static class ReportReason {
        /** 举报人昵称 */
        private String reporterName;
        /** 举报理由 */
        private String reason;
        /** 举报时间 */
        private String createdAt;
    }
}


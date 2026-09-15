package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    @TableId(type = IdType.INPUT)
    private String id;
    private Long gameId;
    private String userId;
    /** 1=推荐 0=不推荐 */
    private Integer recommend;
    private String content;
    /** 逗号分隔的图片 URL 列表 */
    private String images;
    /** 0=待审核 1=已通过 2=被拒 */
    private Integer status;
    private Integer likesCount;
    private Integer repliesCount;
    /** 被举报次数，达到阈值进入审核队列 */
    private Integer reportCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String gameName;
    /** 实时头像（查询时从 users 表回填） */
    @TableField(exist = false)
    private String avatarUrl;
    /** 实时昵称（查询时从 users 表回填，优先 nickname，fallback username） */
    @TableField(exist = false)
    private String displayName;
    /** 当前登录用户是否已点赞（仅查询时填充，不映射 DB） */
    @TableField(exist = false)
    private Boolean liked;
}

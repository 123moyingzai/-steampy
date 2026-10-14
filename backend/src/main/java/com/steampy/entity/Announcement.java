package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告实体
 * 对应表：announcements
 * 核心职责：存储平台发布的公告信息，包括标题、内容、发布时间、是否置顶、是否生效等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("announcements")
public class Announcement {
    /** 公告ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /** 公告标题 */
    private String title;
    /** 公告正文 */
    private String content;
    /** 发布日期（字符串格式，如 2026-10-14） */
    private String publishDate;
    /** 是否生效（true=生效，false=已下线） */
    private Boolean isActive;
    /** 是否置顶（true=置顶显示在列表顶部） */
    private Boolean isTop;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}


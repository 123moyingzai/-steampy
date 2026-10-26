package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcements")
public class Announcement {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String title;
    private String content;
    private String publishDate;
    private Boolean isActive;
    private Boolean isTop;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}


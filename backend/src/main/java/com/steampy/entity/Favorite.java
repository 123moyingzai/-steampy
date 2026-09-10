package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorites")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private Long gameId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

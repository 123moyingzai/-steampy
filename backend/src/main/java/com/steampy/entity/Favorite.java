package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorites")
public class Favorite {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String gameId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}


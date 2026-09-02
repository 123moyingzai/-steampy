package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_games")
public class UserGame {
    @TableId(type = IdType.INPUT)
    private String id;
    private String userId;
    private String orderId;
    private Long gameId;
    private String gameName;
    private String gameImage;
    private String cdkey;
    private String version;
    private String status;
    private String purchaseDate;
    private String activationDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

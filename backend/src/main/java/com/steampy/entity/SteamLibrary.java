package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("steam_libraries")
public class SteamLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 steam_accounts.id */
    private Long steamAccountId;

    private Long gameId;
    private String gameName;
    private String gameImage;
    private Integer playtime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime ownedAt;
}

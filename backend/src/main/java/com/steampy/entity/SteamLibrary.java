package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("steam_libraries")
public class SteamLibrary {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 关联 steam_accounts.id */
    private String steamAccountId;

    private Integer gameId;
    private String gameName;
    private String gameImage;
    /** 游戏时长（小时） */
    private Integer playtime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime ownedAt;
}


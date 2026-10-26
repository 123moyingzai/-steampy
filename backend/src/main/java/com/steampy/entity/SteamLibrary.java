package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("steam_libraries")
public class SteamLibrary {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 关联 steam_accounts.id */
    private String steamAccountId;

    /** Steam 平台 appid（⚠️ 不是本地 games.id！） */
    private Integer gameId;
    private String gameName;
    private String gameImage;
    /** 游戏时长（小时） */
    private Integer playtime;
}

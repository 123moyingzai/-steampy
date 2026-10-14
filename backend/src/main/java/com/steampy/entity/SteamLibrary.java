package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * Steam 游戏库实体
 * 对应表：steam_libraries
 * 核心职责：存储单个 Steam 账号拥有的游戏明细，包括 Steam 平台 appid、游戏名、封面、游玩时长
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("steam_libraries")
public class SteamLibrary {
    /** 游戏库记录ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 关联 Steam 账号ID（关联 steam_accounts.id） */
    private String steamAccountId;

    /** Steam 平台 appid（注意：不是本地 games.id！） */
    private Integer gameId;
    /** Steam 平台上的游戏名称 */
    private String gameName;
    /** 游戏封面图 URL */
    private String gameImage;
    /** 在该 Steam 账号下的游戏时长（单位：小时） */
    private Integer playtime;
}

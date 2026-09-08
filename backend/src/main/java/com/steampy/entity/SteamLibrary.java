package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("steam_libraries")
public class SteamLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 绑定该游戏的用户 ID（保留用于兼容旧查询） */
    private String userId;

    /** 关联 steam_accounts.id（新结构主键） */
    private Long steamAccountId;

    private Long gameId;
    private String gameName;
    private String gameImage;
    private Integer playtime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime ownedAt;
}

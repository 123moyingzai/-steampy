package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 游戏实体
 * 对应表：games
 * 核心职责：存储平台在售游戏的基础信息，包括中英文名称、价格、折扣、库存、开发商等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("games")
public class Game {
    /** 游戏ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /** 游戏英文名 */
    private String name;
    /** 游戏中文名 */
    private String nameCn;
    /** 当前售价（单位：元） */
    private BigDecimal price;
    /** 原价（单位：元） */
    private BigDecimal originalPrice;
    /** 折扣（百分比数值，如 89.00 表示 89%） */
    private BigDecimal discount;
    /** 游戏封面图URL */
    private String image;
    /** Steam商店链接 */
    private String link;
    /** 游戏描述 */
    private String description;
    /** 游戏发售日期 */
    private LocalDate releaseDate;
    /** 开发商名称 */
    private String developer;
    /** 是否预售（true=尚未正式发售） */
    private Boolean isPresale;
    /** 库存数量（单位：份） */
    private Integer stock;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


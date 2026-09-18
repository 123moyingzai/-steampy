package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("games")
public class Game {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String nameCn;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discount; // 折扣（百分比数值，如 89.00 表示 89%）
    private String image;
    private String link;
    private String description;
    private LocalDate releaseDate;
    private String developer;
    private Boolean isPresale;
    private Integer stock;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

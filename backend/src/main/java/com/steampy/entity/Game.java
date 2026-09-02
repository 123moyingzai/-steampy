package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("games")
public class Game {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discount;
    private String image;
    private String imageUrl;
    private String link;
    private String description;
    private String releaseDate;
    private String developer;
    private Boolean isPresale;
    private Integer stock;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

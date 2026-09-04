package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("listings")
public class Listing {
    @TableId(type = IdType.INPUT)
    private String id;
    private String sellerId;
    private Long gameId;
    private String gameName;
    private String gameImage;
    private String version;
    private String cdkey;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String region;
    private String status; // available / sold / pending_activation
    private String type;   // cdkey / py
    private BigDecimal quota;    // 代购可用额度 (仅 py)
    private Boolean autoDeliver; // 是否自动发货 (仅 py)
    private String orderId;
    private LocalDateTime soldAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String sellerName; // 脱敏后的卖家显示名，非数据库字段
}

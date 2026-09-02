package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.INPUT)
    private String id;
    private String orderNo;
    private String buyerId;
    private Long gameId;
    private String gameName;
    private String gameImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String deliveryMethod;
    private String version;
    private String cdkey;
    private String status;
    private String orderType;
    private String paymentMethod;
    private String paidAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

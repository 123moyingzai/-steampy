package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seller_quota")
public class SellerQuota {
    @TableId(type = IdType.INPUT)
    private String id;
    private String sellerId;
    private BigDecimal quota;
    private BigDecimal used;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

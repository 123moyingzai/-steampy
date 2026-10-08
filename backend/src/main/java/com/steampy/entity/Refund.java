package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refunds")
public class Refund {
    @TableId(type = IdType.INPUT)
    private String id;
    private String refundNo;
    private String orderId;
    private String orderNo;
    private String buyerId;
    private String sellerId;
    private String gameName;
    private String gameImage;
    private BigDecimal amount;
    private String reason;
    private String status;       // pending / approved / rejected
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

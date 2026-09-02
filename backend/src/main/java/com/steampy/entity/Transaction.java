package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transactions")
public class Transaction {
    @TableId(type = IdType.INPUT)
    private String id;
    private String transactionNo;
    private String userId;
    private String type;
    private String title;
    private String subtitle;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private String referenceType;
    private String referenceId;
    private String orderId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

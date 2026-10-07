package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_methods")
public class PaymentMethod {
    @TableId(type = IdType.INPUT)
    private String id;
    private String methodCode;
    private String methodName;
    private String type;        // withdraw / recharge
    private BigDecimal feeRate;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private Boolean isActive;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

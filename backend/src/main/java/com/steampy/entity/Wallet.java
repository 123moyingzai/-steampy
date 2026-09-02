package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_wallets")
public class Wallet {
    @TableId(type = IdType.INPUT)
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal totalRecharged;
    private BigDecimal totalSpent;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

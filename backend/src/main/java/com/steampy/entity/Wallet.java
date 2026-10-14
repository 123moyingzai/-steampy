package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体
 * 对应表：user_wallets
 * 核心职责：存储每位用户的钱包余额信息，包括可用余额、冻结金额、累计充值/消费统计
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("user_wallets")
public class Wallet {
    /** 钱包记录ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 所属用户ID */
    private String userId;
    /** 可用余额（单位：元，可直接用于支付） */
    private BigDecimal balance;
    /** 冻结金额（单位：元，订单待处理或退款审核中时暂存） */
    private BigDecimal frozenBalance;
    /** 累计充值总额（单位：元） */
    private BigDecimal totalRecharged;
    /** 累计消费总额（单位：元） */
    private BigDecimal totalSpent;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

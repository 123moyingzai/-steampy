package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易流水实体
 * 对应表：transactions
 * 核心职责：记录用户钱包的每一笔资金变动，包括充值、消费、退款、提现等，含变动前后余额
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("transactions")
public class Transaction {
    /** 流水记录ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 交易流水号（对外展示，如 TXN2026101400001） */
    private String transactionNo;
    /** 所属用户ID */
    private String userId;
    /** 交易类型：recharge=充值、spend=消费、refund=退款、withdraw=提现、freeze=冻结 */
    private String type;
    /** 交易标题（简要说明，如"订单支付"、"钱包充值"） */
    private String title;
    /** 交易副标题（补充说明，如订单号） */
    private String subtitle;
    /** 交易金额（单位：元，收入为正，支出为负） */
    private BigDecimal amount;
    /** 交易前钱包余额（单位：元） */
    private BigDecimal balanceBefore;
    /** 交易后钱包余额（单位：元） */
    private BigDecimal balanceAfter;
    /** 交易状态：pending=处理中、success=成功、failed=失败 */
    private String status;
    /** 关联业务对象类型（如 order、refund、withdraw_record） */
    private String referenceType;
    /** 关联业务对象ID */
    private String referenceId;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

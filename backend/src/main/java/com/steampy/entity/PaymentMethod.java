package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付/提现方式配置实体
 * 对应表：payment_methods
 * 核心职责：存储平台支持的支付方式和提现方式配置，包括费率、限额、排序等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("payment_methods")
public class PaymentMethod {
    /** 支付方式ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 方式编码（如 alipay、wechat、bank） */
    private String methodCode;
    /** 方式展示名称（如 支付宝、微信支付、银行卡） */
    private String methodName;
    /** 方式用途：withdraw=提现渠道，recharge=充值渠道 */
    private String type;
    /** 费率（百分比，如 0.01 表示 1%） */
    private BigDecimal feeRate;
    /** 最低手续费（单位：元） */
    private BigDecimal minFee;
    /** 最高手续费（单位：元） */
    private BigDecimal maxFee;
    /** 是否启用（true=启用） */
    private Boolean isActive;
    /** 排序权重（数值越小越靠前） */
    private Integer sortOrder;
    /** 备注说明 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}

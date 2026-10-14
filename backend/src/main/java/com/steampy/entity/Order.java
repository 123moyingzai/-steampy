package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 对应表：orders
 * 核心职责：存储交易订单信息，涵盖 CDKey 购买和 PY 代购两种订单类型
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("orders")
public class Order {
    /** 订单ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 订单编号（对外展示，如 ORD2026101400001） */
    private String orderNo;
    /** 买家用户ID */
    private String buyerId;
    /** 卖家用户ID */
    private String sellerId;
    /** 关联挂单ID（关联 listings.id） */
    private String listingId;
    /** 关联游戏ID（关联 games.id） */
    private String gameId;
    /** 游戏名称（冗余存储） */
    private String gameName;
    /** 游戏封面图（冗余存储） */
    private String gameImage;
    /** 单价（单位：元） */
    private BigDecimal price;
    /** 购买数量（单位：份） */
    private Integer quantity;
    /** 订单总价（单位：元 = price × quantity） */
    private BigDecimal totalPrice;
    /** 发货方式（如 auto=自动发货、manual=手动发货） */
    private String deliveryMethod;
    /** 游戏版本 */
    private String version;
    /** CDKey 激活码（CDKey 订单的发货内容） */
    private String cdkey;
    /** 订单状态（如 pending_payment=待付款、paid=已付款、completed=已完成、refunded=已退款） */
    private String status;
    /** 订单类型（如 cdkey=CDKey 订单、py=PY代购订单） */
    private String orderType;
    /** 支付方式（如 alipay、wechat、wallet） */
    private String paymentMethod;
    /** 支付完成时间 */
    private LocalDateTime paidAt;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


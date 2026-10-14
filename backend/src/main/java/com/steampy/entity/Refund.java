package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款实体
 * 对应表：refunds
 * 核心职责：存储买家发起的退款申请记录，包含审核状态、退款金额、审核备注等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("refunds")
public class Refund {
    /** 退款记录ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 退款单号（对外展示） */
    private String refundNo;
    /** 关联订单ID（关联 orders.id） */
    private String orderId;
    /** 关联订单编号（冗余存储） */
    private String orderNo;
    /** 买家用户ID */
    private String buyerId;
    /** 卖家用户ID */
    private String sellerId;
    /** 游戏名称（冗余存储） */
    private String gameName;
    /** 游戏封面图（冗余存储） */
    private String gameImage;
    /** 退款金额（单位：元） */
    private BigDecimal amount;
    /** 退款原因说明 */
    private String reason;
    /** 退款状态：pending=待审核，approved=已通过，rejected=已拒绝 */
    private String status;
    /** 申请时间 */
    private LocalDateTime appliedAt;
    /** 审核完成时间 */
    private LocalDateTime reviewedAt;
    /** 审核备注（拒绝原因等） */
    private String reviewRemark;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

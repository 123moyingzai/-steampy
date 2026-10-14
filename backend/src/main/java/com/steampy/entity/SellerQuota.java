package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卖家代购额度实体
 * 对应表：seller_quota
 * 核心职责：存储每位卖家可用于 PY 代购挂单的额度上限和已使用额度，用于额度风控
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("seller_quota")
public class SellerQuota {
    /** 额度记录ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 卖家用户ID */
    private String sellerId;
    /** 可用总额度（单位：元） */
    private BigDecimal quota;
    /** 已使用额度（单位：元） */
    private BigDecimal used;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

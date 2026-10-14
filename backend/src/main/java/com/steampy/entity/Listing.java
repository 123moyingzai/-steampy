package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在售商品（挂单）实体
 * 对应表：listings
 * 核心职责：存储卖家上架的商品信息，支持 CDKey 和 PY 代购两种类型
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("listings")
public class Listing {
    /** 挂单ID（UUID，由业务层生成） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 卖家用户ID */
    private String sellerId;
    /** 关联游戏ID（关联 games.id） */
    private String gameId;
    /** 游戏名称（冗余存储，避免联表查询） */
    private String gameName;
    /** 游戏封面图（冗余存储） */
    private String gameImage;
    /** 游戏版本描述（如 标准版、豪华版） */
    private String version;
    /** CDKey 激活码（type=cdkey 时使用，加密存储） */
    private String cdkey;
    /** 挂单售价（单位：元） */
    private BigDecimal price;
    /** 原价 / 参考价（单位：元） */
    private BigDecimal originalPrice;
    /** 适用地区（如 CN、US、全球区） */
    private String region;
    /** 挂单状态：available=在售，sold=已售出，pending_activation=待激活 */
    private String status;
    /** 挂单类型：cdkey=CDKey 模式，py=PY代购模式 */
    private String type;
    /** 代购可用额度（单位：元，仅 type=py 时使用） */
    private BigDecimal quota;
    /** 是否自动发货（true=付款后自动处理，仅 type=py 时使用） */
    private Boolean autoDeliver;
    /** 售出时间 */
    private LocalDateTime soldAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 脱敏后的卖家显示名（查询时实时回填，非数据库字段） */
    @TableField(exist = false)
    private String sellerName;
}


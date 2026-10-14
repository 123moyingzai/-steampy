package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录实体
 * 对应表：withdraw_records
 * 核心职责：存储用户从钱包发起的提现申请，包括提现方式、银行卡信息、审核状态、手续费等
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("withdraw_records")
public class WithdrawRecord {
    /** 提现记录ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /** 提现单号（对外展示，如 WD2026101400001） */
    private String orderNo;
    /** 申请提现的用户ID */
    private String userId;
    /** 提现方式编码（如 alipay、wechat、bank） */
    private String payMethod;
    /** 提现收款账号（银行卡号或支付宝账号等） */
    private String account;
    /** 提现收款人真实姓名 */
    private String realName;
    /** 关联银行卡记录ID（关联 bank_cards.id，可空） */
    private String bankCardId;
    /** 银行名称（冗余存储，如中国工商银行） */
    private String bankName;
    /** 提现申请金额（单位：元） */
    private BigDecimal amount;
    /** 手续费（单位：元） */
    private BigDecimal fee;
    /** 实际到账金额 = amount - fee（单位：元） */
    private BigDecimal netAmount;
    /** 提现状态：pending=待审核，success=已打款，failed=打款失败 */
    private String status;
    /** 申请时间 */
    private LocalDateTime appliedAt;
    /** 审核 / 打款完成时间 */
    private LocalDateTime reviewedAt;
    /** 审核备注（如失败原因） */
    private String reviewRemark;
}

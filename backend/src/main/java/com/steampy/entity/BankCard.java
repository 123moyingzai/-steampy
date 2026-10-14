package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 银行卡实体
 * 对应表：bank_cards
 * 核心职责：存储用户绑定的银行卡信息，用于提现等资金操作，卡号需脱敏保存
 * @author steampy
 * @date 2026-10-14
 */
@Data
@TableName("bank_cards")
public class BankCard {
    /** 银行卡记录ID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /** 所属用户ID */
    private String userId;
    /** 银行名称 */
    private String bankName;
    /** 银行卡号（明文，仅后端使用） */
    private String cardNumber;
    /** 脱敏后的银行卡号（如 **** **** **** 1234，前端展示用） */
    private String cardNumberMasked;
    /** 持卡人姓名 */
    private String cardHolder;
    /** 银行卡状态：active=启用，disabled=已禁用 */
    private String status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}

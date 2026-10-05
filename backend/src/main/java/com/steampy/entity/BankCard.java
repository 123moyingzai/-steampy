package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bank_cards")
public class BankCard {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String bankName;
    private String cardNumber;
    private String cardNumberMasked;
    private String cardHolder;
    private String status;       // active / disabled
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

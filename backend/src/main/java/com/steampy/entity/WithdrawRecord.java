package com.steampy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("withdraw_records")
public class WithdrawRecord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String orderNo;
    private String userId;
    private String payMethod;
    private String account;
    private String realName;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String status;
    private LocalDateTime appliedAt;
}

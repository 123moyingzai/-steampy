package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Order;
import com.steampy.entity.Refund;
import com.steampy.mapper.OrderMapper;
import com.steampy.mapper.RefundMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private OrderMapper orderMapper;

    // 用户申请退款（仅已完成订单；同一订单同时只允许一条待审核申请）
    @PostMapping
    public Result<Refund> applyRefund(@RequestBody Map<String, String> body) {
        String orderId = body.get("order_id");
        String reason = body.getOrDefault("reason", "");
        if (orderId == null || orderId.isBlank()) return Result.error("订单不存在");
        if (reason == null || reason.isBlank()) return Result.error("请填写退款原因");

        Order o = orderMapper.selectById(orderId);
        if (o == null) return Result.error("订单不存在");
        if (!"completed".equals(o.getStatus())) return Result.error("仅已完成的订单可申请退款");

        QueryWrapper<Refund> dup = new QueryWrapper<>();
        dup.eq("order_id", orderId).eq("status", "pending");
        if (refundMapper.selectCount(dup) > 0) return Result.error("该订单已有退款申请，请等待审核");

        Refund r = new Refund();
        r.setId(UUID.randomUUID().toString());
        r.setRefundNo("RF" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        r.setOrderId(o.getId());
        r.setOrderNo(o.getOrderNo());
        r.setBuyerId(o.getBuyerId());
        r.setSellerId(o.getSellerId());
        r.setGameName(o.getGameName());
        r.setGameImage(o.getGameImage());
        r.setAmount(o.getTotalPrice() != null ? o.getTotalPrice() : o.getPrice());
        r.setReason(reason);
        r.setStatus("pending");
        r.setAppliedAt(LocalDateTime.now());
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        refundMapper.insert(r);
        return Result.success(r);
    }

    // 我的退款记录
    @GetMapping("/user/{userId}")
    public Result<List<Refund>> getUserRefunds(@PathVariable String userId) {
        QueryWrapper<Refund> qw = new QueryWrapper<>();
        qw.eq("buyer_id", userId).orderByDesc("applied_at");
        return Result.success(refundMapper.selectList(qw));
    }
}

package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Order;
import com.steampy.entity.Transaction;
import com.steampy.entity.UserGame;
import com.steampy.mapper.OrderMapper;
import com.steampy.mapper.TransactionMapper;
import com.steampy.mapper.UserGameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private UserGameMapper userGameMapper;

    @PostMapping
    @Transactional
    public Result<Order> createOrder(@RequestBody Order order) {
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        if (order.getStatus() == null) order.setStatus("completed");
        if (order.getOrderType() == null) order.setOrderType("cdkey");
        if (order.getQuantity() == null) order.setQuantity(1);
        if (order.getTotalPrice() == null) order.setTotalPrice(order.getPrice());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // 同步写交易记录（type 和 amount 格式与原 Supabase 数据统一）
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID().toString());
        t.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        t.setUserId(order.getBuyerId());
        t.setType("purchase");
        t.setTitle("购买 " + order.getGameName());
        // 消费类交易：负数金额（与原有 Supabase 数据保持一致）
        t.setAmount(order.getTotalPrice().negate());
        t.setBalanceBefore(java.math.BigDecimal.ZERO);
        t.setBalanceAfter(java.math.BigDecimal.ZERO);
        t.setStatus("completed");
        t.setReferenceType("order");
        t.setReferenceId(order.getId());
        t.setOrderId(order.getId());
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        // 同步写用户游戏库
        UserGame ug = new UserGame();
        ug.setId(UUID.randomUUID().toString());
        ug.setUserId(order.getBuyerId());
        ug.setOrderId(order.getId());
        ug.setGameId(order.getGameId());
        ug.setGameName(order.getGameName());
        ug.setGameImage(order.getGameImage());
        ug.setCdkey(order.getCdkey());
        ug.setVersion(order.getVersion() != null ? order.getVersion() : "标准版");
        ug.setStatus("pending");
        ug.setPurchaseDate(LocalDateTime.now().toString());
        userGameMapper.insert(ug);

        return Result.success(order);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable String userId) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("buyer_id", userId).orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    @GetMapping
    public Result<List<Order>> getAllOrders() {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable String id) {
        return Result.success(orderMapper.selectById(id));
    }
}

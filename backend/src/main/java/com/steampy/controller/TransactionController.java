package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Transaction;
import com.steampy.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionMapper transactionMapper;

    @GetMapping("/user/{userId}")
    public Result<List<Transaction>> getUserTransactions(@PathVariable String userId) {
        QueryWrapper<Transaction> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at");
        return Result.success(transactionMapper.selectList(qw));
    }

    @PostMapping
    public Result<Transaction> createTransaction(@RequestBody Transaction t) {
        if (t.getTransactionNo() == null) {
            t.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        }
        if (t.getCreatedAt() == null) t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);
        return Result.success(t);
    }
}

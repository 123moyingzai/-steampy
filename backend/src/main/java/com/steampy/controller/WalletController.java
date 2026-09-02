package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Transaction;
import com.steampy.entity.Wallet;
import com.steampy.mapper.TransactionMapper;
import com.steampy.mapper.WalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @GetMapping("/user/{userId}")
    public Result<Wallet> getWallet(@PathVariable String userId) {
        QueryWrapper<Wallet> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        Wallet w = walletMapper.selectOne(qw);
        if (w == null) {
            w = new Wallet();
            w.setId(UUID.randomUUID().toString());
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            w.setFrozenBalance(BigDecimal.ZERO);
            w.setCreatedAt(LocalDateTime.now());
            w.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(w);
        }
        return Result.success(w);
    }

    // 充值
    @PostMapping("/user/{userId}/recharge")
    public Result<Wallet> recharge(@PathVariable String userId, @RequestBody java.util.Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        QueryWrapper<Wallet> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        Wallet w = walletMapper.selectOne(qw);
        if (w == null) {
            w = new Wallet();
            w.setId(UUID.randomUUID().toString());
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            w.setFrozenBalance(BigDecimal.ZERO);
            w.setCreatedAt(LocalDateTime.now());
        }
        w.setBalance(w.getBalance().add(amount));
        w.setUpdatedAt(LocalDateTime.now());
        if (w.getId() != null) walletMapper.updateById(w);
        else walletMapper.insert(w);

        // 自动写充值交易记录
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID().toString());
        t.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        t.setUserId(userId);
        t.setType("recharge");
        t.setTitle("账户充值");
        t.setAmount(amount); // 正数：充值
        t.setBalanceBefore(w.getBalance().subtract(amount));
        t.setBalanceAfter(w.getBalance());
        t.setStatus("completed");
        t.setReferenceType("recharge");
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        return Result.success(w);
    }
}

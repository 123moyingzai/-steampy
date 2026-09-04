package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Transaction;
import com.steampy.entity.Wallet;
import com.steampy.entity.WithdrawRecord;
import com.steampy.mapper.TransactionMapper;
import com.steampy.mapper.WalletMapper;
import com.steampy.mapper.WithdrawRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

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
    public Result<Wallet> recharge(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Wallet w = getOrCreateWallet(userId);
        BigDecimal before = w.getBalance();
        w.setBalance(before.add(amount));
        w.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(w);

        Transaction t = new Transaction();
        t.setId(UUID.randomUUID().toString());
        t.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        t.setUserId(userId);
        t.setType("recharge");
        t.setTitle("账户充值");
        t.setAmount(amount);
        t.setBalanceBefore(before);
        t.setBalanceAfter(w.getBalance());
        t.setStatus("completed");
        t.setReferenceType("recharge");
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        return Result.success(w);
    }

    // 提现（1% 手续费）
    @PostMapping("/user/{userId}/withdraw")
    public Result<Map<String, Object>> withdraw(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String payMethod = (String) body.getOrDefault("pay_method", "alipay");
        String account = (String) body.getOrDefault("account", "");
        String realName = (String) body.getOrDefault("real_name", "");

        BigDecimal fee = amount.multiply(new BigDecimal("0.01")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal netAmount = amount.subtract(fee);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("提现金额必须大于 0");
        }

        Wallet w = getOrCreateWallet(userId);
        if (w.getBalance().compareTo(amount) < 0) {
            return Result.error("余额不足，当前余额 ¥" + w.getBalance());
        }

        BigDecimal before = w.getBalance();
        w.setBalance(before.subtract(amount));
        w.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(w);

        LocalDateTime now = LocalDateTime.now();
        String txnNo = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        // 提现交易（支出，负数）
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID().toString());
        t.setTransactionNo(txnNo);
        t.setUserId(userId);
        t.setType("withdraw");
        t.setTitle("余额提现");
        t.setAmount(amount.negate());
        t.setBalanceBefore(before);
        t.setBalanceAfter(w.getBalance());
        t.setStatus("completed");
        t.setReferenceType("withdraw");
        t.setCreatedAt(now);
        transactionMapper.insert(t);

        // 手续费单独一条记录
        Transaction feeTx = new Transaction();
        feeTx.setId(UUID.randomUUID().toString());
        feeTx.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        feeTx.setUserId(userId);
        feeTx.setType("fee");
        feeTx.setTitle("提现手续费（1%）");
        feeTx.setAmount(fee.negate());
        feeTx.setBalanceBefore(w.getBalance());
        feeTx.setBalanceAfter(w.getBalance());
        feeTx.setStatus("completed");
        feeTx.setReferenceType("withdraw");
        feeTx.setCreatedAt(now);
        transactionMapper.insert(feeTx);

        // 提现记录（用于前端提现记录表格）
        WithdrawRecord wr = new WithdrawRecord();
        wr.setId(UUID.randomUUID().toString());
        wr.setOrderNo("WD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        wr.setUserId(userId);
        wr.setPayMethod(payMethod);
        wr.setAccount(account);
        wr.setRealName(realName);
        wr.setAmount(amount);
        wr.setFee(fee);
        wr.setNetAmount(netAmount);
        wr.setStatus("success");
        wr.setAppliedAt(now);
        withdrawRecordMapper.insert(wr);

        return Result.success(Map.of(
            "balance", w.getBalance(),
            "amount", amount,
            "fee", fee,
            "net_amount", netAmount,
            "order_no", wr.getOrderNo()
        ));
    }

    // 提现记录列表
    @GetMapping("/user/{userId}/withdraw-records")
    public Result<List<WithdrawRecord>> getWithdrawRecords(@PathVariable String userId) {
        QueryWrapper<WithdrawRecord> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("applied_at");
        List<WithdrawRecord> list = withdrawRecordMapper.selectList(qw);
        return Result.success(list != null ? list : new ArrayList<>());
    }

    private Wallet getOrCreateWallet(String userId) {
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
            walletMapper.insert(w);
        }
        return w;
    }
}

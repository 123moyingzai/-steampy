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

/**
 * TransactionController
 * 负责交易流水查询与创建的 HTTP 接口层
 * 提供的核心接口：
 *   - GET  /api/transactions/user/{userId} — 查询指定用户的全部交易流水（按时间倒序）
 *   - POST /api/transactions               — 创建一条交易流水（内部主要用于订单/钱包服务，也支持前端直接调用）
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionMapper transactionMapper;

    /**
     * 查询指定用户的全部交易流水列表（按创建时间倒序）
     *
     * @param userId 用户 ID
     * @return {@link Result} 包含交易流水列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Transaction>> getUserTransactions(@PathVariable String userId) {
        QueryWrapper<Transaction> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at");
        return Result.success(transactionMapper.selectList(qw));
    }

    /**
     * 创建一条交易流水
     * <p>
     * 若 transactionNo 或 createdAt 未传，会自动生成。
     * 注：实际业务中的交易流水大多由 OrderController / WalletController 内部直接调用 mapper 创建，
     * 此接口主要用于管理端直接插入或调试场景。
     * </p>
     *
     * @param t 交易对象（JSON 传入，需包含 userId、type、title、amount 等必要字段）
     * @return {@link Result} 成功返回创建好的交易对象（含生成的 transactionNo）
     */
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

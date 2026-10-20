package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.BankCard;
import com.steampy.entity.PaymentMethod;
import com.steampy.entity.Transaction;
import com.steampy.entity.Wallet;
import com.steampy.entity.WithdrawRecord;
import com.steampy.mapper.BankCardMapper;
import com.steampy.mapper.PaymentMethodMapper;
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

/**
 * WalletController
 * 负责钱包（余额）管理、充值、提现的 HTTP 接口层
 * 提供的核心接口：
 *   - GET  /api/wallets/user/{userId}                — 查询用户钱包（不存在则自动创建一个初始余额为 0 的钱包）
 *   - POST /api/wallets/user/{userId}/recharge       — 用户充值（从外部向钱包入账，内部只加余额）
 *   - POST /api/wallets/user/{userId}/withdraw        — 用户提现（支持已绑银行卡快捷提现和手输支付宝/银行卡号两种路径）
 *   - GET  /api/wallets/user/{userId}/withdraw-records — 查询用户的提现申请记录列表
 *
 * 提现核心流程：
 *   1. 校验金额 > 0 + 渠道开关启用 + 收款信息格式校验 + 姓名正则
 *   2. 手续费按 payment_methods 配置：金额 × 费率，夹在 [最低, 最高] 区间
 *   3. 立即扣余额（非冻结，直接减 balance）
 *   4. 写 WithdrawRecord（状态 pending）+ 两条 Transaction（amount 支出 + fee 手续费，均 pending 等审核）
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    /**
     * 查询指定用户的钱包；若钱包不存在则自动创建一个初始余额为 0、冻结余额为 0 的钱包并入库
     *
     * @param userId 用户 ID
     * @return {@link Result} 包含钱包对象（保证非 null）
     */
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

    /**
     * 用户充值（从外部向钱包入账）
     * <p>
     * 内部仅做余额增加 + 写入一条 type=recharge 的 Transaction，
     * 真实外部支付流程需由前端/支付网关回调完成（此处为简化版）。
     * </p>
     *
     * @param userId 用户 ID
     * @param body   JSON 请求体，需包含 amount（充值金额，BigDecimal）
     * @return {@link Result} 包含更新后的钱包对象
     */
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

    /**
     * 用户申请提现
     * <p>
     * 收款信息有两种路径：
     *   1. 银行卡快捷提现（pay_method=bank + bank_card_id）：直接使用已绑卡信息为唯一事实来源，
     *      不再接收 account / real_name / bank_name 手输值
     *   2. 手动填写路径（兼容旧版）：前端直接传 account、real_name，需做正则格式校验
     *
     * 格式校验规则：
     *   - 支付宝账号：11 位手机号（1 开头）或有效邮箱
     *   - 银行卡号：16-19 位纯数字 + Luhn 算法校验
     *   - 真实姓名：2-20 个中文字符（含间隔号 ·）
     *
     * 手续费按 payment_methods 配置：金额 × feeRate，夹在 [minFee, maxFee] 区间。
     * 成功后立即扣减余额，写入 WithdrawRecord + 两条 Transaction（均为 pending 状态，等后台审核）。
     * </p>
     *
     * @param userId 用户 ID
     * @param body   JSON 请求体，包含：amount（提现金额）、pay_method（alipay / bank）、
     *               account、real_name、bank_name（手动填路径）或 bank_card_id（绑卡路径）
     * @return {@link Result} 包含 balance（剩余余额）、amount、fee（手续费）、net_amount（实际到账）、order_no（提现单号）
     */
    @PostMapping("/user/{userId}/withdraw")
    public Result<Map<String, Object>> withdraw(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String payMethod = (String) body.getOrDefault("pay_method", "alipay");
        String account = (String) body.getOrDefault("account", "");
        String realName = (String) body.getOrDefault("real_name", "");
        String bankCardId = (String) body.getOrDefault("bank_card_id", "");
        String bankName = (String) body.getOrDefault("bank_name", "");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) return Result.error("提现金额必须大于 0");

        // 渠道开关校验（payment_methods 配置：停用渠道直接拒绝；both 类型也可用于提现）
        PaymentMethod pm = paymentMethodMapper.selectOne(
                new QueryWrapper<PaymentMethod>()
                        .eq("method_code", payMethod)
                        .in("type", List.of("withdraw", "both")));
        if (pm == null || !Boolean.TRUE.equals(pm.getIsActive()))
            return Result.error("该提现渠道已停用，请选择其他方式");

        // 银行卡提现优先走已绑定银行卡（多卡提现，卡数据为唯一事实来源）
        if ("bank".equals(payMethod) && bankCardId != null && !bankCardId.isBlank()) {
            BankCard card = bankCardMapper.selectById(bankCardId);
            if (card == null) return Result.error("银行卡不存在，请重新选择");
            if (!userId.equals(card.getUserId())) return Result.error("无权使用该银行卡");
            if (!"active".equals(card.getStatus())) return Result.error("该银行卡已停用");
            account = card.getCardNumber();
            realName = card.getCardHolder();
            bankName = card.getBankName();
        } else {
            // 格式校验（手输收款信息路径，兼容旧版）
            if (account == null || account.isBlank()) return Result.error("请填写收款账号");
            if (realName == null || realName.isBlank()) return Result.error("请填写真实姓名");

            if ("alipay".equals(payMethod)) {
                if (!account.matches("^1[3-9]\\d{9}$") && !account.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    return Result.error("支付宝账号必须是 11 位手机号（1开头）或有效邮箱地址");
            } else if ("bank".equals(payMethod)) {
                if (!account.matches("^\\d{16,19}$")) return Result.error("银行卡号必须为 16-19 位纯数字");
                if (!luhnCheck(account)) return Result.error("银行卡号校验失败，请检查是否输入正确");
                if (bankName == null || bankName.isBlank())
                    return Result.error("请填写开户银行");
            } else {
                return Result.error("不支持的提现方式: " + payMethod);
            }
        }

        if (!realName.matches("^[\\u4e00-\\u9fa5·]{2,20}$"))
            return Result.error("真实姓名必须为 2-20 个中文字符");

        // 手续费按 payment_methods 配置：金额 × 费率，并夹在 [最低, 最高] 区间
        BigDecimal feeRate = pm.getFeeRate() != null ? pm.getFeeRate() : new BigDecimal("0.01");
        BigDecimal minFee = pm.getMinFee() != null ? pm.getMinFee() : BigDecimal.ONE;
        BigDecimal maxFee = pm.getMaxFee() != null ? pm.getMaxFee() : new BigDecimal("50.00");
        BigDecimal fee = amount.multiply(feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (fee.compareTo(minFee) < 0) fee = minFee;
        if (fee.compareTo(maxFee) > 0) fee = maxFee;
        BigDecimal netAmount = amount.subtract(fee);

        Wallet w = getOrCreateWallet(userId);
        if (w.getBalance().compareTo(amount) < 0)
            return Result.error("余额不足，当前余额 ¥" + w.getBalance());

        // ① 立即扣余额（冻结）
        BigDecimal before = w.getBalance();
        w.setBalance(before.subtract(amount));
        w.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(w);

        LocalDateTime now = LocalDateTime.now();

        // ② 先写 WithdrawRecord 拿到 id（reference）
        WithdrawRecord wr = new WithdrawRecord();
        wr.setOrderNo("WD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        wr.setUserId(userId);
        wr.setPayMethod(payMethod);
        wr.setAccount(account);
        wr.setRealName(realName);
        wr.setBankCardId("bank".equals(payMethod) && bankCardId != null && !bankCardId.isBlank() ? bankCardId : null);
        wr.setBankName("bank".equals(payMethod) ? bankName : null);
        wr.setAmount(amount);
        wr.setFee(fee);
        wr.setNetAmount(netAmount);
        wr.setStatus("pending");
        wr.setAppliedAt(now);
        withdrawRecordMapper.insert(wr);
        // insert 后 wr.getId() 有值了

        // ③ 写两条 Transaction，referenceId 指向 WithdrawRecord（status=pending 等审核）
        String txnNo = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        Transaction t = new Transaction();
        t.setId(UUID.randomUUID().toString());
        t.setTransactionNo(txnNo);
        t.setUserId(userId);
        t.setType("withdraw");
        t.setTitle("余额提现");
        t.setAmount(amount.negate());
        t.setBalanceBefore(before);
        t.setBalanceAfter(w.getBalance());
        t.setStatus("pending");
        t.setReferenceType("withdraw");
        t.setReferenceId(wr.getId());
        t.setCreatedAt(now);
        transactionMapper.insert(t);

        Transaction feeTx = new Transaction();
        feeTx.setId(UUID.randomUUID().toString());
        feeTx.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        feeTx.setUserId(userId);
        feeTx.setType("fee");
        feeTx.setTitle("提现手续费（" + feeRate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%）");
        feeTx.setAmount(fee.negate());
        feeTx.setBalanceBefore(w.getBalance());
        feeTx.setBalanceAfter(w.getBalance());
        feeTx.setStatus("pending");
        feeTx.setReferenceType("withdraw");
        feeTx.setReferenceId(wr.getId());
        feeTx.setCreatedAt(now);
        transactionMapper.insert(feeTx);

        return Result.success(Map.of(
            "balance", w.getBalance(),
            "amount", amount,
            "fee", fee,
            "net_amount", netAmount,
            "order_no", wr.getOrderNo()
        ));
    }

    /**
     * 查询指定用户的提现申请记录列表（按申请时间倒序）
     *
     * @param userId 用户 ID
     * @return {@link Result} 包含提现记录列表；无记录时返回空数组而非 null
     */
    @GetMapping("/user/{userId}/withdraw-records")
    public Result<List<WithdrawRecord>> getWithdrawRecords(@PathVariable String userId) {
        QueryWrapper<WithdrawRecord> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("applied_at");
        List<WithdrawRecord> list = withdrawRecordMapper.selectList(qw);
        return Result.success(list != null ? list : new ArrayList<>());
    }

    /**
     * 私有工具：Luhn 算法校验银行卡号（ISO/IEC 7812:1983 标准）。
     * 从右向左，偶数位乘 2（大于 9 则减 9），总和能被 10 整除则通过。
     *
     * @param num 银行卡号纯数字字符串
     * @return 合法则返回 true；非纯数字或校验失败返回 false
     */
    private static boolean luhnCheck(String num) {
        if (num == null || !num.matches("^\\d+$")) return false;
        int sum = 0;
        boolean even = false;
        for (int i = num.length() - 1; i >= 0; i--) {
            int d = num.charAt(i) - '0';
            if (even) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            even = !even;
        }
        return sum % 10 == 0;
    }

    /**
     * 私有工具：根据 userId 获取钱包；若不存在则自动创建一个初始余额为 0 的钱包并立即入库。
     * 与 OrderController 中的同名工具方法不同，本方法会直接写入 DB。
     *
     * @param userId 用户 ID
     * @return 钱包对象（保证非 null，且已在 DB 中存在）
     */
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

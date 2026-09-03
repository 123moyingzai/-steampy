package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Listing;
import com.steampy.entity.Order;
import com.steampy.entity.Transaction;
import com.steampy.entity.UserGame;
import com.steampy.entity.Wallet;
import com.steampy.mapper.ListingMapper;
import com.steampy.mapper.OrderMapper;
import com.steampy.mapper.TransactionMapper;
import com.steampy.mapper.UserGameMapper;
import com.steampy.mapper.WalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    @Autowired
    private ListingMapper listingMapper;
    @Autowired
    private WalletMapper walletMapper;

    @PostMapping
    @Transactional
    public Result<Order> createOrder(@RequestBody Order order,
                                     @RequestParam(required = false) BigDecimal balance_amount) {
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        if (order.getStatus() == null) order.setStatus("completed");
        if (order.getOrderType() == null) order.setOrderType("cdkey");
        if (order.getQuantity() == null) order.setQuantity(1);
        if (order.getTotalPrice() == null) order.setTotalPrice(order.getPrice());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        boolean isSellerListing = false;
        BigDecimal sellerIncome = BigDecimal.ZERO;
        String sellerId = null;

        // ===== 混合支付：扣买家余额（无手续费，直接从钱包扣） =====
        boolean usedBalance = false;
        if (balance_amount != null && balance_amount.compareTo(BigDecimal.ZERO) > 0) {
            Wallet bw = getOrCreateWallet(order.getBuyerId());
            if (bw.getBalance().compareTo(balance_amount) < 0) {
                return Result.error("余额不足，无法完成支付");
            }
            bw.setBalance(bw.getBalance().subtract(balance_amount));
            bw.setUpdatedAt(LocalDateTime.now());
            if (bw.getId() == null) walletMapper.insert(bw);
            else walletMapper.updateById(bw);
            usedBalance = true;
        }

        // 如果前端传了 listingId，自动从 listings 表发货
        if (order.getListingId() != null && !order.getListingId().isEmpty()) {
            Listing listing = listingMapper.selectById(order.getListingId());
            if (listing == null) {
                return Result.error("CDKey 不存在");
            }
            if (!"available".equals(listing.getStatus())) {
                return Result.error("该 CDKey 已售出");
            }
            // 禁止卖家自购
            if (listing.getSellerId().equals(order.getBuyerId())) {
                return Result.error("不能购买您自己上架的 CDKey");
            }
            // 发货：把 listing 的 cdkey 和 seller_id 填入订单
            order.setCdkey(listing.getCdkey());
            order.setSellerId(listing.getSellerId());
            // 标记 listing 已售出
            listing.setStatus("sold");
            listing.setOrderId(order.getId());
            listing.setSoldAt(LocalDateTime.now());
            listing.setUpdatedAt(LocalDateTime.now());
            listingMapper.updateById(listing);
            // 记录卖家收入
            isSellerListing = true;
            sellerId = listing.getSellerId();
            sellerIncome = order.getTotalPrice();
        }

        orderMapper.insert(order);

        // 买家：支出交易记录（一条，不管有没有用余额）
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID().toString());
        t.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        t.setUserId(order.getBuyerId());
        t.setType("purchase");
        String title = "购买 " + order.getGameName();
        if (usedBalance) title += "（余额支付 ¥" + balance_amount.setScale(2) + "）";
        t.setTitle(title);
        t.setAmount(order.getTotalPrice().negate());
        t.setBalanceBefore(BigDecimal.ZERO);
        t.setBalanceAfter(BigDecimal.ZERO);
        t.setStatus("completed");
        t.setReferenceType("order");
        t.setReferenceId(order.getId());
        t.setOrderId(order.getId());
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        // 用户游戏库（标记 source=cdkey，区分 PY代购 source=store）
        UserGame ug = new UserGame();
        ug.setId(UUID.randomUUID().toString());
        ug.setUserId(order.getBuyerId());
        ug.setOrderId(order.getId());
        ug.setGameId(order.getGameId());
        ug.setGameName(order.getGameName());
        ug.setGameImage(order.getGameImage());
        ug.setCdkey(order.getCdkey()); // ← 确保 cdkey 入库
        ug.setVersion(order.getVersion() != null ? order.getVersion() : "标准版");
        ug.setStatus("pending");
        ug.setPurchaseDate(LocalDateTime.now().toString());
        ug.setSource("cdkey"); // ← 标记来源是 CDKey 购买（不是 PY代购）
        userGameMapper.insert(ug);

        // ===== 如果是卖家上架的 CDKey，给卖家加余额 + 写卖家交易记录 =====
        if (isSellerListing && sellerId != null && sellerIncome.compareTo(BigDecimal.ZERO) > 0) {
            // 给卖家钱包加钱
            Wallet sw = getOrCreateWallet(sellerId);
            BigDecimal before = sw.getBalance();
            sw.setBalance(before.add(sellerIncome));
            sw.setUpdatedAt(LocalDateTime.now());
            if (sw.getId() == null) walletMapper.insert(sw);
            else walletMapper.updateById(sw);

            // 写卖家交易记录（收入，正数）
            Transaction st = new Transaction();
            st.setId(UUID.randomUUID().toString());
            st.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            st.setUserId(sellerId);
            st.setType("sale");
            st.setTitle("售出 " + order.getGameName());
            st.setAmount(sellerIncome); // 正数：收入
            st.setBalanceBefore(before);
            st.setBalanceAfter(sw.getBalance());
            st.setStatus("completed");
            st.setReferenceType("order");
            st.setReferenceId(order.getId());
            st.setOrderId(order.getId());
            st.setCreatedAt(LocalDateTime.now());
            transactionMapper.insert(st);
        }

        return Result.success(order);
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
        }
        return w;
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable String userId) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("buyer_id", userId).orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    @GetMapping("/seller/{sellerId}")
    public Result<List<Order>> getSellerOrders(@PathVariable String sellerId) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("seller_id", sellerId).orderByDesc("created_at");
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

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable String id) {
        Order o = orderMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        o.setStatus("cancelled");
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        if (o.getListingId() != null) {
            Listing l = listingMapper.selectById(o.getListingId());
            if (l != null) {
                l.setStatus("available");
                l.setOrderId(null);
                l.setSoldAt(null);
                l.setUpdatedAt(LocalDateTime.now());
                listingMapper.updateById(l);
            }
        }
        return Result.success(null);
    }
}

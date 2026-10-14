package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.constant.OrderStatus;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.Listing;
import com.steampy.entity.Order;
import com.steampy.entity.SteamLibrary;
import com.steampy.entity.Transaction;
import com.steampy.entity.User;
import com.steampy.entity.UserGame;
import com.steampy.entity.Wallet;
import com.steampy.mapper.ListingMapper;
import com.steampy.mapper.OrderMapper;
import com.steampy.mapper.SteamLibraryMapper;
import com.steampy.mapper.TransactionMapper;
import com.steampy.mapper.UserGameMapper;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.WalletMapper;
import com.steampy.service.SteamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * OrderController
 * 负责订单业务的 HTTP 接口层
 * 提供的核心接口：
 *   - POST /api/orders                    — 创建订单（混合支付 + 自动发货 + 自动入库）
 *   - GET  /api/orders/user/{userId}      — 查询用户的购买订单列表
 *   - GET  /api/orders/seller/{sellerId}  — 查询卖家的售出订单列表
 *   - GET  /api/orders                    — 管理员查询全部订单
 *   - GET  /api/orders/{id}              — 查询订单详情
 *   - PUT  /api/orders/{id}/cancel        — 取消订单（并恢复商品上架状态）
 *
 * @author steampy
 * @date 2026-10-14
 */
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
    @Autowired
    private SteamLibraryMapper steamLibraryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SteamService steamService;

    /**
     * 创建游戏订单
     * <p>
     * 核心流程（事务内）：
     *   1. 校验买家身份（必须绑定 Steam 账号，且 Steam 库存中未拥有该游戏）
     *   2. 生成订单号、填充默认值
     *   3. 混合支付：若前端传入 balance_amount 则从买家钱包余额扣减
     *   4. 若订单关联 listing：根据上架类型（py 代购 / cdkey 卡密）自动发货并下架商品
     *   5. 写入订单 + 买家支出交易记录
     *   6. CDKey 类型自动写入用户游戏库（PY 代购待商家发货）
     *   7. 卖家上架商品：自动给卖家钱包加收入 + 写卖家收入交易记录
     *   8. 自动激活到 Steam 账号（steam_libraries 表）并刷新 Steam 统计
     * </p>
     *
     * @param order          订单对象（JSON 传入，包含 gameId、gameName、price、buyerId、listingId 等）
     * @param balance_amount 可选：使用钱包余额支付的金额（单位元），不传或传 0 则不扣余额
     * @return {@link Result} 成功时返回创建好的订单对象（含生成的 id、orderNo、status 等），失败时返回错误信息
     */
    @PostMapping
    @Transactional
    public Result<Order> createOrder(@RequestBody Order order,
                                     @RequestParam(required = false) BigDecimal balance_amount) {
        // ===== Steam 绑定检查：未绑定禁止购买 =====
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer == null) {
            return Result.error("用户不存在");
        }
        if (buyer.getSteamAccountId() == null) {
            return Result.error("请先绑定 Steam 账号后再购买");
        }

        // ===== Steam 库存查重：已拥有禁止重复购买 =====
        // 注：用 gameName 匹配（steam_libraries 里存的是 Steam appid，不是本地 games.id）
        String accountId = buyer.getSteamAccountId();
        if (accountId != null
                && order.getGameName() != null && !order.getGameName().isBlank()
                && steamService.isGameOwned(accountId, null, order.getGameName())) {
            return Result.error("您的 Steam 库存中已拥有该游戏，无法重复购买");
        }

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
                return Result.error("上架记录不存在");
            }
            if (!"available".equals(listing.getStatus())) {
                return Result.error("该商品已售出/下架");
            }
            // 禁止卖家自购
            if (listing.getSellerId().equals(order.getBuyerId())) {
                return Result.error("不能购买您自己上架的商品");
            }
            order.setSellerId(listing.getSellerId());

            if ("py".equals(listing.getType())) {
                // ===== PY代购：扣额度 + 下架 listing（额度用完才下架，否则只扣 quota）=====
                BigDecimal cost = order.getTotalPrice();
                BigDecimal remain = listing.getQuota() != null ? listing.getQuota().subtract(cost) : BigDecimal.ZERO;
                if (remain.compareTo(BigDecimal.ZERO) < 0) {
                    return Result.error("卖家代购额度不足");
                }
                listing.setQuota(remain);
                if (remain.compareTo(BigDecimal.ZERO) == 0) {
                    listing.setStatus("sold"); // 额度用完下架
                    listing.setSoldAt(LocalDateTime.now());
                }
                listing.setUpdatedAt(LocalDateTime.now());
                listingMapper.updateById(listing);
                order.setOrderType("py");
                order.setCdkey(null); // py 无 cdkey
            } else {
                // ===== CDKey：扣 cdkey + 下架 =====
                order.setCdkey(listing.getCdkey());
                listing.setStatus("sold");
                listing.setSoldAt(LocalDateTime.now());
                listing.setUpdatedAt(LocalDateTime.now());
                listingMapper.updateById(listing);
            }

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
        t.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(t);

        // 用户游戏库：CDKey 购买才入库（cdkey 非空），PY代购等商家发货后再加
        // source: cdkey=玩家购 / store=PY代购（后续商家发货时补）
        if (order.getCdkey() != null && !order.getCdkey().isEmpty() && !"py".equals(order.getOrderType())) {
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
            ug.setPurchaseDate(LocalDateTime.now());
            ug.setSource("cdkey");
            userGameMapper.insert(ug);
        }

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
            st.setCreatedAt(LocalDateTime.now());
            transactionMapper.insert(st);
        }

        // ===== 购买后自动激活到 Steam 账号（steam_libraries）=====
        // 不管是 cdkey 还是 py 代购，只要绑定了 Steam 且订单有游戏名，都进库存
        if (accountId != null
                && order.getGameName() != null && !order.getGameName().isBlank()
                && !"cancelled".equalsIgnoreCase(order.getStatus())) {
            if (!steamService.isGameOwned(accountId, null, order.getGameName())) {
                SteamLibrary sl = new SteamLibrary();
                sl.setSteamAccountId(accountId);
                sl.setGameId(null);  // steam_libraries.game_id 是 Steam appid，本地 games.id 不匹配
                sl.setGameName(order.getGameName());
                sl.setGameImage(order.getGameImage());
                sl.setPlaytime(new Random().nextInt(100) + 1);  // 小时
                steamLibraryMapper.insert(sl);
            }
            steamService.refreshUserSteamStats(order.getBuyerId());
        }

        return Result.success(order);
    }

    /**
     * 私有工具：根据 userId 获取钱包，不存在则创建一个初始余额为 0 的钱包（不立即入库，由调用方决定）。
     *
     * @param userId 用户 ID
     * @return 钱包对象（保证非 null）
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
        }
        return w;
    }

    /**
     * 查询指定用户作为买家的所有订单（按创建时间倒序）
     *
     * @param userId 买家用户 ID
     * @return {@link Result} 包含订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable String userId) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("buyer_id", userId).orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    /**
     * 查询指定用户作为卖家的所有售出订单（按创建时间倒序）
     *
     * @param sellerId 卖家用户 ID
     * @return {@link Result} 包含订单列表
     */
    @GetMapping("/seller/{sellerId}")
    public Result<List<Order>> getSellerOrders(@PathVariable String sellerId) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("seller_id", sellerId).orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    /**
     * 管理员查询系统中全部订单（按创建时间倒序）
     *
     * @return {@link Result} 包含全部订单列表
     */
    @GetMapping
    public Result<List<Order>> getAllOrders() {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    /**
     * 根据订单 ID 查询订单详情
     *
     * @param id 订单 ID
     * @return {@link Result} 包含订单对象（若不存在则 data 为 null）
     */
    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable String id) {
        return Result.success(orderMapper.selectById(id));
    }

    /**
     * 取消订单
     * <p>
     * 校验订单状态是否允许流转到 cancelled（通过 {@link OrderStatus#canTransition} 判断），
     * 同时将关联的 listing 恢复为 available 状态（释放商品供他人购买）。
     * </p>
     *
     * @param id 订单 ID
     * @return {@link Result} 成功返回 null；失败返回错误信息（订单不存在 / 状态不可取消）
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable String id) {
        Order o = orderMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        if (!OrderStatus.canTransition(o.getStatus(), OrderStatus.CANCELLED)) {
            return Result.error("当前订单状态不可取消");
        }
        o.setStatus(OrderStatus.CANCELLED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        if (o.getListingId() != null) {
            Listing l = listingMapper.selectById(o.getListingId());
            if (l != null) {
                l.setStatus("available");
                l.setSoldAt(null);
                l.setUpdatedAt(LocalDateTime.now());
                listingMapper.updateById(l);
            }
        }
        return Result.success(null);
    }
}


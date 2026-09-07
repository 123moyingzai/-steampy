package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.*;
import com.steampy.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private GameMapper gameMapper;
    @Autowired private ListingMapper listingMapper;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private WithdrawRecordMapper withdrawMapper;
    @Autowired private AnnouncementMapper announcementMapper;

    // ======== 仪表盘统计 ========
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> m = new HashMap<>();
        m.put("userCount", userMapper.selectCount(null));
        m.put("orderCount", orderMapper.selectCount(null));
        m.put("gameCount", gameMapper.selectCount(null));

        QueryWrapper<Listing> lq = new QueryWrapper<>();
        lq.eq("status", "available");
        m.put("availableListings", listingMapper.selectCount(lq));

        QueryWrapper<Review> rq = new QueryWrapper<>();
        rq.eq("status", 0);
        m.put("pendingReviews", reviewMapper.selectCount(rq));

        QueryWrapper<WithdrawRecord> wq = new QueryWrapper<>();
        wq.eq("status", "pending");
        m.put("pendingWithdrawals", withdrawMapper.selectCount(wq));

        // 总收入（已完成订单）
        QueryWrapper<Order> completedQw = new QueryWrapper<>();
        completedQw.eq("status", "completed");
        List<Order> completed = orderMapper.selectList(completedQw);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order o : completed) {
            if (o.getTotalPrice() != null) totalRevenue = totalRevenue.add(o.getTotalPrice());
        }
        m.put("totalRevenue", totalRevenue);

        // 今日订单 / 今日收入
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        QueryWrapper<Order> todayQw = new QueryWrapper<>();
        todayQw.ge("created_at", todayStart);
        List<Order> todayOrders = orderMapper.selectList(todayQw);
        BigDecimal todayRevenue = BigDecimal.ZERO;
        for (Order o : todayOrders) {
            if ("completed".equals(o.getStatus()) && o.getTotalPrice() != null) {
                todayRevenue = todayRevenue.add(o.getTotalPrice());
            }
        }
        m.put("todayOrderCount", todayOrders.size());
        m.put("todayRevenue", todayRevenue);

        // 订单状态分布 + 热销榜（需要 game_name 字段，不能只 select status）
        Map<String, Long> statusBreakdown = new HashMap<>();
        Map<String, Long> gameCount = new HashMap<>();
        QueryWrapper<Order> allOq = new QueryWrapper<>();
        allOq.select("status", "game_name");
        List<Order> all = orderMapper.selectList(allOq);
        for (Order o : all) {
            String s = o.getStatus() == null ? "unknown" : o.getStatus();
            statusBreakdown.merge(s, 1L, Long::sum);
            String name = o.getGameName();
            if (name != null && !name.isBlank()) gameCount.merge(name, 1L, Long::sum);
        }
        m.put("orderStatusBreakdown", statusBreakdown);

        // 最近 8 条订单（完整字段）
        QueryWrapper<Order> recentQw = new QueryWrapper<>();
        recentQw.orderByDesc("created_at").last("LIMIT 8");
        List<Order> recent = orderMapper.selectList(recentQw);
        m.put("recentOrders", recent);

        // 热销 Top 5 游戏（gameCount 已在上面 all 循环里聚合）
        List<Map<String, Object>> topGames = gameCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("gameName", e.getKey());
                    row.put("orderCount", e.getValue());
                    return row;
                }).toList();
        m.put("topGames", topGames);

        return Result.success(m);
    }

    // ======== 用户管理 ========
    @GetMapping("/users")
    public Result<List<User>> getAllUsers() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        List<User> users = userMapper.selectList(qw);
        users.forEach(u -> u.setPasswordHash(null));
        return Result.success(users);
    }

    @PutMapping("/users/{id}/ban")
    public Result<User> banUser(@PathVariable String id, @RequestBody Map<String, String> body) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.error("用户不存在");
        String action = body.getOrDefault("action", "ban");
        if ("ban".equals(action)) u.setUserType("已封禁");
        else if ("unban".equals(action)) u.setUserType("普通用户");
        else if ("admin".equals(action)) u.setUserType("管理员");
        else if ("normal".equals(action)) u.setUserType("普通用户");
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        u.setPasswordHash(null);
        return Result.success(u);
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable String id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    // ======== 游戏管理 ========
    @GetMapping("/games")
    public Result<List<Game>> getAllGames() {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(gameMapper.selectList(qw));
    }

    @PostMapping("/games")
    public Result<Game> createGame(@RequestBody Game g) {
        if (g.getName() == null || g.getName().isBlank()) return Result.error("游戏名不能为空");
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        gameMapper.insert(g);
        return Result.success(g);
    }

    @PutMapping("/games/{id}")
    public Result<Game> updateGame(@PathVariable Long id, @RequestBody Game body) {
        Game g = gameMapper.selectById(id);
        if (g == null) return Result.error("游戏不存在");
        body.setId(id);
        body.setUpdatedAt(LocalDateTime.now());
        gameMapper.updateById(body);
        return Result.success(body);
    }

    @DeleteMapping("/games/{id}")
    public Result<?> deleteGame(@PathVariable Long id) {
        gameMapper.deleteById(id);
        return Result.success();
    }

    // ======== 订单管理 ========
    @GetMapping("/orders")
    public Result<List<Order>> getAllOrders() {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }

    @PutMapping("/orders/{id}/status")
    public Result<Order> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Order o = orderMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        String status = body.get("status");
        if (status != null && (status.equals("pending") || status.equals("completed") || status.equals("cancelled") || status.equals("refunded"))) {
            o.setStatus(status);
            o.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(o);
        }
        return Result.success(o);
    }

    // ======== 上架 Listings 管理 ========
    @GetMapping("/listings")
    public Result<List<Listing>> getAllListings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String type) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq("status", status);
        if (gameId != null) qw.eq("game_id", gameId);
        if (type != null && !type.isBlank()) qw.eq("type", type);
        qw.orderByDesc("created_at");
        return Result.success(listingMapper.selectList(qw));
    }

    @PutMapping("/listings/{id}/status")
    public Result<Listing> updateListingStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("上架记录不存在");
        String status = body.get("status");
        if (status != null) {
            l.setStatus(status);
            l.setUpdatedAt(LocalDateTime.now());
            if (!"available".equals(status)) {
                l.setOrderId(null);
                l.setSoldAt(null);
            }
            listingMapper.updateById(l);
        }
        return Result.success(l);
    }

    @DeleteMapping("/listings/{id}")
    public Result<?> deleteListing(@PathVariable String id) {
        listingMapper.deleteById(id);
        return Result.success();
    }

    // ======== 提现审核 Withdrawals ========
    @GetMapping("/withdrawals")
    public Result<List<WithdrawRecord>> getAllWithdrawals(
            @RequestParam(required = false) String status) {
        QueryWrapper<WithdrawRecord> qw = new QueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByDesc("applied_at");
        return Result.success(withdrawMapper.selectList(qw));
    }

    @PutMapping("/withdrawals/{id}/review")
    public Result<WithdrawRecord> reviewWithdrawal(@PathVariable String id, @RequestBody Map<String, String> body) {
        WithdrawRecord w = withdrawMapper.selectById(id);
        if (w == null) return Result.error("提现记录不存在");
        if (!"pending".equals(w.getStatus())) return Result.error("该提现已审核过");

        String action = body.getOrDefault("action", "approve");
        String remark = body.getOrDefault("remark", "");
        LocalDateTime now = LocalDateTime.now();
        w.setReviewedAt(now);
        w.setReviewRemark(remark);

        if ("approve".equals(action)) {
            w.setStatus("success");
        } else if ("reject".equals(action)) {
            w.setStatus("failed");
            // 拒绝 → 退回买家余额
            Wallet ww = walletMapper.selectOne(new QueryWrapper<Wallet>().eq("user_id", w.getUserId()));
            if (ww != null) {
                ww.setBalance(ww.getBalance().add(w.getAmount()));
                ww.setUpdatedAt(now);
                walletMapper.updateById(ww);
            }
        }
        withdrawMapper.updateById(w);
        return Result.success(w);
    }

    // ======== 评测审核 Reviews ========
    @GetMapping("/reviews")
    public Result<List<Review>> getAllReviews(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long gameId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        if (status != null) qw.eq("status", status);
        if (gameId != null) qw.eq("game_id", gameId);
        qw.orderByDesc("created_at");
        return Result.success(reviewMapper.selectList(qw));
    }

    @PutMapping("/reviews/{id}/review")
    public Result<Review> reviewReview(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return Result.error("评测不存在");
        Integer status = Integer.valueOf(body.get("status").toString());
        r.setStatus(status);
        r.setUpdatedAt(LocalDateTime.now());
        reviewMapper.updateById(r);
        return Result.success(r);
    }

    @DeleteMapping("/reviews/{id}")
    public Result<?> deleteReview(@PathVariable String id) {
        reviewMapper.deleteById(id);
        return Result.success();
    }

    // ======== 公告管理 ========
    @GetMapping("/announcements")
    public Result<List<Announcement>> getAllAnnouncements() {
        QueryWrapper<Announcement> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(announcementMapper.selectList(qw));
    }

    @PostMapping("/announcements")
    public Result<Announcement> createAnnouncement(@RequestBody Announcement a) {
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.insert(a);
        return Result.success(a);
    }

    @PutMapping("/announcements/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement body) {
        body.setId(id);
        body.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(body);
        return Result.success(body);
    }

    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }
}

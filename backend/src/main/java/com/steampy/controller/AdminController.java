package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.constant.OrderStatus;
import com.steampy.dto.Result;
import com.steampy.entity.*;
import com.steampy.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminController
 * 负责管理员后台的 HTTP 接口层，覆盖仪表盘、用户、游戏、订单、上架、提现、退款、评测、公告、支付渠道等全量管理能力。
 *
 * 提供的核心接口：
 *   - GET    /api/admin/stats             — 仪表盘统计（用户数/订单数/总收入/热销榜等）
 *   - GET    /api/admin/users             — 用户列表（分页 + 搜索 + 角色筛选，LEFT JOIN 钱包）
 *   - PUT    /api/admin/users/{id}/ban    — 用户封禁/解封/设为管理员
 *   - PUT    /api/admin/users/batch       — 批量封禁/解禁/设角色（复用单条通知逻辑）
 *   - DELETE /api/admin/users/batch       — 批量删除用户（跳过管理员与操作者本人）
 *   - DELETE /api/admin/users/{id}        — 删除用户
 *   - GET    /api/admin/games             — 游戏列表
 *   - POST   /api/admin/games             — 创建游戏
 *   - PUT    /api/admin/games/{id}        — 更新游戏
 *   - DELETE /api/admin/games/{id}        — 删除游戏
 *   - GET    /api/admin/orders            — 订单列表
 *   - PUT    /api/admin/orders/{id}/status — 修改订单状态（校验 OrderStatus 枚举）
 *   - PUT    /api/admin/orders/batch-status — 批量修改订单状态
 *   - GET    /api/admin/export/{users|orders|games|withdrawals} — 数据导出为 CSV（UTF-8+BOM）
 *   - GET    /api/admin/listings          — 上架列表（可按 status/gameId/type 过滤）
 *   - PUT    /api/admin/listings/{id}/status — 修改上架状态
 *   - DELETE /api/admin/listings/{id}     — 删除上架
 *   - GET    /api/admin/withdrawals       — 提现审核列表
 *   - PUT    /api/admin/withdrawals/{id}/review — 提现审核（通过改 Transaction；拒绝退回余额 + reversal 流水）
 *   - GET    /api/admin/refunds           — 退款列表（LEFT JOIN 买家信息）
 *   - PUT    /api/admin/refunds/{id}/review — 退款审核（事务：订单置已退款 + 退款买家 + 扣回卖家）
 *   - GET    /api/admin/reviews           — 评测审核（只显示 report_count > 0 的）
 *   - PUT    /api/admin/reviews/{id}/review — 评测审核（保留/拒绝 + 清零举报）
 *   - DELETE /api/admin/reviews/{id}      — 删除评测
 *   - GET    /api/admin/announcements     — 公告列表
 *   - POST   /api/admin/announcements     — 创建公告
 *   - PUT    /api/admin/announcements/{id} — 更新公告
 *   - DELETE /api/admin/announcements/{id} — 删除公告
 *   - GET    /api/admin/payment-methods   — 支付渠道配置列表
 *   - PUT    /api/admin/payment-methods/{id} — 更新支付渠道（费率/最小值/最大值/启停/排序）
 *
 * @author steampy
 * @date 2026-10-14
 */
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
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private WalletMapper walletMapper;
    @Autowired private PaymentMethodMapper paymentMethodMapper;
    @Autowired private RefundMapper refundMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    // ======== 仪表盘统计 ========

    /**
     * 管理员仪表盘统计数据聚合接口。
     *
     * 一次性返回平台关键指标：用户总数、订单总数、游戏总数、在售上架数、待审核评测数、待处理提现数、已完成订单总收入、今日订单数及收入、订单状态分布、热销 Top5、最近 8 条订单。
     * 收入统计基于 status=completed 的订单 totalPrice 累加。
     *
     * @return {@link Result} 包含各指标 Map；键名：userCount / orderCount / gameCount / availableListings / pendingReviews / pendingWithdrawals / totalRevenue / todayOrderCount / todayRevenue / orderStatusBreakdown / recentOrders / topGames
     */
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

    // ======== 用户管理（分页 + 搜索 + LEFT JOIN wallet 一次查完） ========

    /**
     * 分页查询用户列表（支持关键词搜索 + 角色筛选）。
     *
     * 使用原生 SQL LEFT JOIN user_wallets 一次性带出钱包余额，避免 N+1 查询。
     * 搜索匹配 username / nickname / phone 三字段的 LIKE。
     * 列表中的 password_hash 字段统一置为 null 脱敏，不回传给前端。
     * 做了 size 上限 100 的防刷保护。
     *
     * @param page    页码，默认 1
     * @param size    每页条数，默认 10，最大 100
     * @param keyword 可选，用户名/昵称/手机号模糊搜索
     * @param role    可选，按 user_type 精确过滤（如 "普通用户" / "管理员" / "已封禁"）
     * @return {@link Result} 包含 { total, page, size, list }，list 每项含 users 字段 + wallet_balance
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {

        // 防刷：size 最多 100
        if (size > 100) size = 100;
        int offset = (page - 1) * size;

        // --- 构建 WHERE 条件 ---
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.username LIKE ? OR u.nickname LIKE ? OR u.phone LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (role != null && !role.isBlank()) {
            where.append(" AND u.user_type = ?");
            params.add(role);
        }

        // --- 总数 ---
        String countSql = "SELECT COUNT(*) FROM users u " + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        // --- 分页数据（LEFT JOIN wallet 一次带出来，避免 N+1）---
        String dataSql = "SELECT u.*, w.balance AS wallet_balance " +
                "FROM users u LEFT JOIN user_wallets w ON w.user_id = u.id " +
                where +
                " ORDER BY u.created_at DESC LIMIT ? OFFSET ?";
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(offset);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql, dataParams.toArray());

        // 敏感字段脱敏
        for (Map<String, Object> r : rows) r.put("password_hash", null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("total", total == null ? 0 : total);
        resp.put("page", page);
        resp.put("size", size);
        resp.put("list", rows);
        return Result.success(resp);
    }

    /**
     * 修改用户状态（封禁/解封/升管理员/降普通）。
     *
     * 通过 body 中的 action 字段决定：ban→已封禁、unban→普通用户、admin→管理员、normal→普通用户。
     * 未找到用户返回错误。更新后清空 password_hash 再返回。
     *
     * @param id   用户主键
     * @param body 至少包含 action 字段（ban / unban / admin / normal）
     * @return {@link Result} 更新后的 User 实体（已脱敏）；用户不存在返回 error
     */
    @PutMapping("/users/{id}/ban")
    public Result<User> banUser(@PathVariable String id, @RequestBody Map<String, String> body) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.error("用户不存在");
        applyUserAction(u, body.getOrDefault("action", "ban"), body.get("operatorId"));
        u.setPasswordHash(null);
        return Result.success(u);
    }

    /**
     * 批量修改用户状态（封禁 / 解禁 / 升管理员 / 降普通），单个与批量接口共用同一套通知逻辑。
     *
     * 请求体：{ ids: ["u1","u2"], action: "ban" | "unban" | "admin" | "normal", operatorId: "当前管理员id" }
     * ids 为空返回错误；不存在的用户自动跳过，返回 affected / total 便于前端提示。
     *
     * @param body 见上
     * @return {@link Result} { affected, total, action }
     */
    @PutMapping("/users/batch")
    public Result<Map<String, Object>> batchUpdateUsers(@RequestBody Map<String, Object> body) {
        List<String> ids = toStringList(body.get("ids"));
        if (ids.isEmpty()) return Result.error("请先选择用户");
        String action = str(body.getOrDefault("action", "ban"));
        String operatorId = str(body.get("operatorId"));
        int affected = 0;
        for (String id : ids) {
            User u = userMapper.selectById(id);
            if (u == null) continue;
            applyUserAction(u, action, operatorId);
            affected++;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("affected", affected);
        m.put("total", ids.size());
        m.put("action", action);
        return Result.success(m);
    }

    /**
     * 批量删除用户（物理删除，风险操作）。
     *
     * 请求体：{ ids: ["u1","u2"] }
     * 安全护栏：自动跳过管理员账号与当前操作者自身，避免误删管理权限。
     *
     * @param body 含 ids 数组
     * @return {@link Result} { affected, skipped, total }
     */
    @DeleteMapping("/users/batch")
    public Result<Map<String, Object>> batchDeleteUsers(@RequestBody Map<String, Object> body) {
        List<String> ids = toStringList(body.get("ids"));
        if (ids.isEmpty()) return Result.error("请先选择用户");
        String operatorId = str(body.get("operatorId"));
        List<String> deletable = new ArrayList<>();
        int skipped = 0;
        for (String id : ids) {
            User u = userMapper.selectById(id);
            // 管理员与操作者本人不允许被批量删除
            if (u == null || "管理员".equals(u.getUserType()) || (operatorId != null && operatorId.equals(id))) {
                skipped++;
                continue;
            }
            deletable.add(id);
        }
        int affected = deletable.isEmpty() ? 0 : userMapper.deleteBatchIds(deletable);
        Map<String, Object> m = new HashMap<>();
        m.put("affected", affected);
        m.put("skipped", skipped);
        m.put("total", ids.size());
        return Result.success(m);
    }

    /**
     * 应用单个用户的状态变更并写入站内通知，供单个/批量接口复用。
     *
     * @param u          目标用户实体（方法内会更新 userType 并落库）
     * @param action     ban / unban / admin / normal
     * @param operatorId 执行操作的管理员 id（用于站内通知标记发送人）
     */
    private void applyUserAction(User u, String action, String operatorId) {
        String newType;
        if ("ban".equals(action)) newType = "已封禁";
        else if ("unban".equals(action)) newType = "普通用户";
        else if ("admin".equals(action)) newType = "管理员";
        else newType = "普通用户";
        u.setUserType(newType);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);

        // 状态变更写入站内通知（消息中心可见；actorId = 操作的管理员，显示其昵称与头像）
        User operator = (operatorId != null && !operatorId.isBlank()) ? userMapper.selectById(operatorId) : null;
        boolean fromAdmin = operator != null && "管理员".equals(operator.getUserType());
        String notice;
        if ("已封禁".equals(newType)) {
            notice = "您的账号已被封禁，购买、出售、充值、提现、评价等功能暂时无法使用；账户余额仍正常保留，解封后即可恢复全部功能。如有疑问请联系管理员。";
        } else if ("unban".equals(action)) {
            notice = "您的账号已解封，全部功能已恢复使用，感谢您的理解与配合。";
        } else if ("admin".equals(action)) {
            notice = "您已被提升为管理员，可访问管理后台执行平台管理操作。";
        } else {
            notice = null; // 降为普通用户/普通状态变更不通知
        }
        if (notice == null) return;
        Notification n = new Notification();
        n.setId(UUID.randomUUID().toString());
        n.setUserId(u.getId());
        n.setType("system");
        // 有合法操作管理员则标记发送人，前端显示其昵称/头像；否则为系统通知
        n.setActorId(fromAdmin ? operator.getId() : null);
        n.setTargetType("system");
        n.setTargetId(u.getId());
        n.setContentSnippet(notice);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    /**
     * 物理删除用户（风险操作，直接从 users 表移除）。
     *
     * @param id 用户主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable String id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    // ======== 游戏管理 ========

    /**
     * 查询全部游戏，按创建时间倒序。
     *
     * @return {@link Result} Game 列表
     */
    @GetMapping("/games")
    public Result<List<Game>> getAllGames() {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(gameMapper.selectList(qw));
    }

    /**
     * 新增游戏。
     *
     * 校验 name 非空；自动设置 createdAt / updatedAt 为当前时间。
     *
     * @param g Game 请求体（至少要有 name）
     * @return {@link Result} 插入后的 Game 实体（含服务端补全的时间戳）
     */
    @PostMapping("/games")
    public Result<Game> createGame(@RequestBody Game g) {
        if (g.getName() == null || g.getName().isBlank()) return Result.error("游戏名不能为空");
        String name = g.getName().trim();
        if (countByName(name, null) > 0) {
            return Result.error("游戏「" + name + "」已存在，不能重复添加");
        }
        g.setName(name);
        if (g.getId() == null || g.getId().isBlank()) {
            Long maxId = gameMapper.selectMaxNumericId();
            g.setId(String.valueOf((maxId == null ? 0L : maxId) + 1));
        }
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        gameMapper.insert(g);
        return Result.success(g);
    }

    /**
     * 新增游戏时预检查游戏名是否已存在（前端失焦即提示，避免填完才报错）。
     *
     * @param name 游戏名称
     * @return {@link Result} data 为 { exists: true/false, id, name }
     */
    @GetMapping("/games/exists")
    public Result<Map<String, Object>> gameExists(@RequestParam String name) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exists", false);
        if (name == null || name.isBlank()) return Result.success(data);
        Game exist = findByExactName(name.trim());
        if (exist != null) {
            data.put("exists", true);
            data.put("id", exist.getId());
            data.put("name", exist.getName());
        }
        return Result.success(data);
    }

    /**
     * 各游戏的在售 CDKey 数量（按 listings 中 status=available 聚合）。
     * <p>
     * 管理端「库存」不再手工填写，统一展示这个聚合值，避免 games.stock 与卖家真实上架数量对不上。
     * </p>
     *
     * @return {@link Result} [{game_id, cnt}] 列表
     */
    @GetMapping("/games/stock-stats")
    public Result<List<Map<String, Object>>> gameStockStats() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT game_id, COUNT(*) AS cnt FROM listings WHERE status = 'available' GROUP BY game_id");
        return Result.success(rows);
    }

    /**
     * 按名称查重（忽略大小写与首尾空格）。
     *
     * @param name      游戏名称
     * @param excludeId 需要排除的游戏 id（编辑场景传自身 id，新增传 null）
     * @return 命中的记录数
     */
    private long countByName(String name, String excludeId) {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.apply("LOWER(TRIM(name)) = {0}", name.toLowerCase());
        if (excludeId != null && !excludeId.isBlank()) qw.ne("id", excludeId);
        Long c = gameMapper.selectCount(qw);
        return c == null ? 0 : c;
    }

    /**
     * 按名称精确查询一条游戏记录（忽略大小写与首尾空格）。
     *
     * @param name 游戏名称
     * @return 命中的游戏，未命中返回 null
     */
    private Game findByExactName(String name) {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.apply("LOWER(TRIM(name)) = {0}", name.toLowerCase());
        qw.last("LIMIT 1");
        return gameMapper.selectOne(qw);
    }

    /**
     * 更新游戏信息（按路径 id 覆盖 body.id）。
     *
     * 自动更新 updatedAt。游戏不存在时返回错误。
     *
     * @param id   游戏主键
     * @param body Game 更新内容
     * @return {@link Result} 更新后的 Game 实体
     */
    @PutMapping("/games/{id}")
    public Result<Game> updateGame(@PathVariable String id, @RequestBody Game body) {
        Game g = gameMapper.selectById(id);
        if (g == null) return Result.error("游戏不存在");
        body.setId(id);
        body.setUpdatedAt(LocalDateTime.now());
        gameMapper.updateById(body);
        return Result.success(body);
    }

    /**
     * 删除指定游戏（物理删除）。
     *
     * @param id 游戏主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/games/{id}")
    public Result<?> deleteGame(@PathVariable String id) {
        gameMapper.deleteById(id);
        return Result.success();
    }

    // ======== 订单管理 ========

    /**
     * 查询全部订单，按创建时间倒序。
     *
     * @return {@link Result} Order 列表
     */
    @GetMapping("/orders")
    public Result<List<Order>> getAllOrders() {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        List<Order> list = orderMapper.selectList(qw);
        // 回填买家昵称（批量查询用户表，避免 N+1）
        Set<String> buyerIds = list.stream()
                .map(Order::getBuyerId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!buyerIds.isEmpty()) {
            QueryWrapper<User> uq = new QueryWrapper<>();
            uq.in("id", buyerIds);
            Map<String, String> nameMap = new HashMap<>();
            for (User u : userMapper.selectList(uq)) {
                String name = u.getNickname();
                if (name == null || name.isBlank()) name = u.getUsername();
                nameMap.put(u.getId(), name);
            }
            for (Order o : list) {
                o.setBuyerName(nameMap.get(o.getBuyerId()));
            }
        }
        return Result.success(list);
    }

    /**
     * 修改订单状态（需通过 {@link com.steampy.constant.OrderStatus} 合法性校验）。
     *
     * 非法状态会返回错误。更新成功时同步 updatedAt。
     *
     * @param id   订单主键
     * @param body 包含 status 字段的 Map
     * @return {@link Result} 更新后的 Order 实体；状态非法返回 error
     */
    @PutMapping("/orders/{id}/status")
    public Result<Order> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Order o = orderMapper.selectById(id);
        if (o == null) return Result.error("订单不存在");
        String status = body.get("status");
        if (status != null && OrderStatus.isValid(status)) {
            o.setStatus(status);
            o.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(o);
        } else {
            return Result.error("非法订单状态: " + status);
        }
        return Result.success(o);
    }

    /**
     * 批量修改订单状态（逐条校验 {@link OrderStatus} 合法性）。
     *
     * 请求体：{ ids: ["o1","o2"], status: "completed" }
     * 状态非法/为空返回错误；不存在的订单自动跳过，返回 affected / total。
     *
     * @param body 见上
     * @return {@link Result} { affected, total, status }
     */
    @PutMapping("/orders/batch-status")
    public Result<Map<String, Object>> batchUpdateOrderStatus(@RequestBody Map<String, Object> body) {
        List<String> ids = toStringList(body.get("ids"));
        if (ids.isEmpty()) return Result.error("请先选择订单");
        String status = str(body.get("status"));
        if (status == null || !OrderStatus.isValid(status)) return Result.error("非法订单状态: " + status);
        LocalDateTime now = LocalDateTime.now();
        int affected = 0;
        for (String id : ids) {
            Order o = orderMapper.selectById(id);
            if (o == null) continue;
            o.setStatus(status);
            o.setUpdatedAt(now);
            orderMapper.updateById(o);
            affected++;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("affected", affected);
        m.put("total", ids.size());
        m.put("status", status);
        return Result.success(m);
    }

    // ======== 上架 Listings 管理 ========

    /**
     * 查询上架列表（可按 status / gameId / type 三条件自由过滤）。
     *
     * @param status  可选，上架状态（如 available / sold / pending_activation）
     * @param gameId  可选，按游戏 ID 过滤
     * @param type    可选，上架类型（如 cdk / py / balance）
     * @return {@link Result} Listing 列表
     */
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

    /**
     * 修改上架记录状态。
     *
     * 当状态变更为非 available（如 sold / pending_activation）时，自动清空 soldAt 时间戳。
     *
     * @param id   上架主键
     * @param body 包含 status 字段
     * @return {@link Result} 更新后的 Listing；不存在返回 error
     */
    @PutMapping("/listings/{id}/status")
    public Result<Listing> updateListingStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("上架记录不存在");
        String status = body.get("status");
        if (status != null) {
            l.setStatus(status);
            l.setUpdatedAt(LocalDateTime.now());
            if (!"available".equals(status)) {
                l.setSoldAt(null);
            }
            listingMapper.updateById(l);
        }
        return Result.success(l);
    }

    /**
     * 物理删除上架记录。
     *
     * @param id 上架主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/listings/{id}")
    public Result<?> deleteListing(@PathVariable String id) {
        listingMapper.deleteById(id);
        return Result.success();
    }

    // ======== 提现审核 Withdrawals ========

    /**
     * 查询提现申请列表（可按 status 过滤）。
     *
     * @param status 可选，提现状态（pending / success / failed）
     * @return {@link Result} WithdrawRecord 列表，按申请时间倒序
     */
    @GetMapping("/withdrawals")
    public Result<List<WithdrawRecord>> getAllWithdrawals(
            @RequestParam(required = false) String status) {
        QueryWrapper<WithdrawRecord> qw = new QueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq("status", status);
        qw.orderByDesc("applied_at");
        return Result.success(withdrawMapper.selectList(qw));
    }

    /**
     * 审核提现申请（approve / reject）。
     *
     * 审核通过：w.status → success，关联的 Transaction 状态改为 completed。
     * 审核拒绝：w.status → failed，① 关联的 pending Transaction 改 failed；② 退回用户钱包余额；③ 追加一条 reversal 类型的 Transaction 记录退回流水。
     * 仅 pending 状态的申请可以被审核一次，已审核过的返回错误。
     *
     * @param id   提现申请主键
     * @param body 包含 action（approve/reject）及可选 remark 审核备注
     * @return {@link Result} 审核后的 WithdrawRecord；非 pending 状态返回 error
     */
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

        // 找出本次提现关联的两条 Transaction
        QueryWrapper<Transaction> tq = new QueryWrapper<>();
        tq.eq("reference_type", "withdraw").eq("reference_id", w.getId());
        List<Transaction> relatedTxns = transactionMapper.selectList(tq);

        if ("approve".equals(action)) {
            w.setStatus("success");
            // 审核通过 → Transaction 改 completed（真的打款了）
            for (Transaction tx : relatedTxns) { tx.setStatus("completed"); transactionMapper.updateById(tx); }
        } else if ("reject".equals(action)) {
            w.setStatus("failed");
            // 审核拒绝 → 退回用户余额（原本冻结的扣回）
            Wallet ww = walletMapper.selectOne(new QueryWrapper<Wallet>().eq("user_id", w.getUserId()));
            if (ww != null) {
                ww.setBalance(ww.getBalance().add(w.getAmount()));
                ww.setUpdatedAt(now);
                walletMapper.updateById(ww);
            }
            // 退回时还了钱，需要记录一条 reversal Transaction
            if (ww != null) {
                Transaction rev = new Transaction();
                rev.setId(java.util.UUID.randomUUID().toString());
                rev.setTransactionNo("TXN" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
                rev.setUserId(w.getUserId());
                rev.setType("reversal");
                rev.setTitle("提现审核拒绝 — 余额退回");
                rev.setAmount(w.getAmount());
                rev.setBalanceBefore(ww.getBalance().subtract(w.getAmount()));
                rev.setBalanceAfter(ww.getBalance());
                rev.setStatus("completed");
                rev.setReferenceType("withdraw");
                rev.setReferenceId(w.getId());
                rev.setCreatedAt(now);
                transactionMapper.insert(rev);
            }
            // 原两条 pending Transaction 也改 failed（状态同步）
            for (Transaction tx : relatedTxns) { tx.setStatus("failed"); transactionMapper.updateById(tx); }
        }
        withdrawMapper.updateById(w);
        return Result.success(w);
    }

    // ======== 退款审核 Refunds ========

    /**
     * 查询退款申请列表（原生 SQL，LEFT JOIN 带出买家 username/nickname）。
     *
     * 支持按 status 过滤和 keyword（refund_no / order_no / game_name 模糊搜索）。
     *
     * @param status  可选，退款状态（pending / approved / rejected）
     * @param keyword 可选，关键词模糊搜索（退款单号/订单号/游戏名）
     * @return {@link Result} Map 列表，含 refunds 全字段 + buyer_username / buyer_nickname
     */
    @GetMapping("/refunds")
    public Result<List<Map<String, Object>>> getAllRefunds(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND r.status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (r.refund_no LIKE ? OR r.order_no LIKE ? OR r.game_name LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like); params.add(like); params.add(like);
        }
        String sql = "SELECT r.*, u.username AS buyer_username, u.nickname AS buyer_nickname " +
                "FROM refunds r LEFT JOIN users u ON u.id = r.buyer_id " +
                where + " ORDER BY r.applied_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        return Result.success(rows);
    }

    /**
     * 审核退款申请（approve / reject），带 @Transactional 保障资金一致性。
     *
     * 审核通过将依次执行：① 关联订单置为 refunded；② 买家钱包退回金额 + 写一条 refund 类型 Transaction（含资金快照 before/after）；③ 卖家余额扣回 + 写一条 refund_deduct 类型 Transaction；④ 卖家余额不足时平台兜底，仍保证退款成功。
     * 审核拒绝仅修改 refund.status 为 rejected。
     * 仅 pending 状态可被审核一次。
     *
     * @param id   退款申请主键
     * @param body 包含 action（approve/reject）及可选 remark 审核备注
     * @return {@link Result} 审核后的 Refund 实体；非 pending 或未知 action 返回 error
     */
    @PutMapping("/refunds/{id}/review")
    @Transactional
    public Result<Refund> reviewRefund(@PathVariable String id, @RequestBody Map<String, String> body) {
        Refund r = refundMapper.selectById(id);
        if (r == null) return Result.error("退款申请不存在");
        if (!"pending".equals(r.getStatus())) return Result.error("该退款申请已审核过");

        String action = body.getOrDefault("action", "approve");
        String remark = body.getOrDefault("remark", "");
        LocalDateTime now = LocalDateTime.now();
        r.setReviewedAt(now);
        r.setReviewRemark(remark);

        if ("approve".equals(action)) {
            r.setStatus("approved");
            // ① 订单置为已退款
            Order o = orderMapper.selectById(r.getOrderId());
            if (o != null) {
                o.setStatus("refunded");
                o.setUpdatedAt(now);
                orderMapper.updateById(o);
            }
            // ② 退款金额退回买家钱包 + 流水（资金快照：变动前后余额）
            Wallet ww = walletMapper.selectOne(new QueryWrapper<Wallet>().eq("user_id", r.getBuyerId()));
            if (ww == null) {
                ww = new Wallet();
                ww.setId(UUID.randomUUID().toString());
                ww.setUserId(r.getBuyerId());
                ww.setBalance(BigDecimal.ZERO);
                ww.setFrozenBalance(BigDecimal.ZERO);
                ww.setCreatedAt(now);
                walletMapper.insert(ww);
            }
            BigDecimal before = ww.getBalance();
            ww.setBalance(before.add(r.getAmount()));
            ww.setUpdatedAt(now);
            walletMapper.updateById(ww);

            Transaction t = new Transaction();
            t.setId(UUID.randomUUID().toString());
            t.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            t.setUserId(r.getBuyerId());
            t.setType("refund");
            t.setTitle("订单退款 " + (r.getGameName() != null ? r.getGameName() : ""));
            t.setAmount(r.getAmount());
            t.setBalanceBefore(before);
            t.setBalanceAfter(ww.getBalance());
            t.setStatus("completed");
            t.setReferenceType("refund");
            t.setReferenceId(r.getId());
            t.setCreatedAt(now);
            transactionMapper.insert(t);

            // ③ 卖家收入扣回（有卖家且余额充足才扣，不足则平台兜底，保证审核必成）
            if (r.getSellerId() != null && !r.getSellerId().isBlank()) {
                Wallet sw = walletMapper.selectOne(new QueryWrapper<Wallet>().eq("user_id", r.getSellerId()));
                if (sw != null && sw.getBalance() != null && sw.getBalance().compareTo(r.getAmount()) >= 0) {
                    BigDecimal sb = sw.getBalance();
                    sw.setBalance(sb.subtract(r.getAmount()));
                    sw.setUpdatedAt(now);
                    walletMapper.updateById(sw);

                    Transaction st = new Transaction();
                    st.setId(UUID.randomUUID().toString());
                    st.setTransactionNo("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
                    st.setUserId(r.getSellerId());
                    st.setType("refund_deduct");
                    st.setTitle("订单退款扣回 " + (r.getGameName() != null ? r.getGameName() : ""));
                    st.setAmount(r.getAmount().negate());
                    st.setBalanceBefore(sb);
                    st.setBalanceAfter(sw.getBalance());
                    st.setStatus("completed");
                    st.setReferenceType("refund");
                    st.setReferenceId(r.getId());
                    st.setCreatedAt(now);
                    transactionMapper.insert(st);
                }
            }
        } else if ("reject".equals(action)) {
            r.setStatus("rejected");
        } else {
            return Result.error("未知操作: " + action);
        }
        r.setUpdatedAt(now);
        refundMapper.updateById(r);
        return Result.success(r);
    }

    // ======== 评测审核 Reviews（仅显示被举报过的） ========

    /**
     * 查询待处理的评测列表（只取 report_count > 0 的，即被举报过的）。
     *
     * 额外批量回填评测作者的 nickname/avatar 到 Review 的 displayName / avatarUrl 字段，避免 N+1。
     *
     * @param status  可选，评测状态（0 待审核 / 1 保留 / 2 拒绝）
     * @param gameId  可选，按游戏 ID 过滤
     * @return {@link Result} 带实时用户资料回填的 Review 列表，按举报数和创建时间倒序
     */
    @GetMapping("/reviews")
    public Result<List<Review>> getAllReviews(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long gameId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        // 只显示被举报过的（report_count > 0）
        qw.gt("report_count", 0);
        if (status != null) qw.eq("status", status);
        if (gameId != null) qw.eq("game_id", gameId);
        qw.orderByDesc("report_count").orderByDesc("created_at");
        List<Review> list = reviewMapper.selectList(qw);
        // 回填实时用户资料
        Set<String> ids = new HashSet<>();
        for (Review r : list) ids.add(r.getUserId());
        if (!ids.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(ids);
            Map<String, User> userMap = new HashMap<>();
            for (User u : users) userMap.put(u.getId(), u);
            for (Review r : list) {
                User u = userMap.get(r.getUserId());
                if (u != null) {
                    String name = (u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : u.getUsername();
                    r.setDisplayName(name != null ? name : "匿名用户");
                    r.setAvatarUrl(u.getAvatarUrl());
                } else {
                    r.setDisplayName("已注销用户");
                }
            }
        }
        // 回填举报理由（从 reports 表批量查，避免 N+1）
        if (!list.isEmpty()) {
            // 1. 批量查 reports
            List<String> reviewIds = new ArrayList<>();
            for (Review r : list) reviewIds.add(r.getId());
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < reviewIds.size(); i++) {
                if (i > 0) inClause.append(',');
                inClause.append('?');
            }
            List<Map<String, Object>> allReports = jdbcTemplate.queryForList(
                    "SELECT r.target_id, r.reason, r.reporter_id, r.created_at " +
                    "FROM reports r WHERE r.target_type = 'review' AND r.target_id IN (" + inClause + ") " +
                    "ORDER BY r.created_at DESC",
                    reviewIds.toArray());

            // 2. 收集举报人 ID，批量查昵称
            Set<String> reporterIds = new HashSet<>();
            for (Map<String, Object> rp : allReports) {
                Object rid = rp.get("reporter_id");
                if (rid != null) reporterIds.add(rid.toString());
            }
            Map<String, String> reporterNameMap = new HashMap<>();
            if (!reporterIds.isEmpty()) {
                List<User> reporters = userMapper.selectBatchIds(reporterIds);
                for (User u : reporters) {
                    String n = (u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : u.getUsername();
                    reporterNameMap.put(u.getId(), n != null ? n : "匿名用户");
                }
            }

            // 3. 按 target_id 分组塞进各 Review.reportReasons
            Map<String, List<Review.ReportReason>> reasonMap = new HashMap<>();
            for (Map<String, Object> rp : allReports) {
                String tid = rp.get("target_id").toString();
                List<Review.ReportReason> bucket = reasonMap.computeIfAbsent(tid, k -> new ArrayList<>());
                Review.ReportReason item = new Review.ReportReason();
                item.setReporterName(reporterNameMap.getOrDefault(rp.get("reporter_id").toString(), "已注销用户"));
                Object r = rp.get("reason");
                item.setReason(r != null ? r.toString() : "");
                Object ca = rp.get("created_at");
                item.setCreatedAt(ca != null ? ca.toString() : "");
                bucket.add(item);
            }
            for (Review r : list) {
                List<Review.ReportReason> reasons = reasonMap.get(r.getId());
                r.setReportReasons(reasons != null ? reasons : new ArrayList<>());
            }
        }
        return Result.success(list);
    }

    /**
     * 审核评测：保留（status=1）或拒绝（status=2）。
     *
     * 任何处理都会清零 report_count 并物理删除 reports 表中针对该评测的全部举报记录，使其彻底出队。
     * 评测不存在返回错误。
     *
     * @param id   评测主键
     * @param body 必须包含 status 字段（Integer，1 保留 / 2 拒绝）
     * @return {@link Result} 更新后的 Review；评测不存在返回 error
     */
    @PutMapping("/reviews/{id}/review")
    public Result<Review> reviewReview(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return Result.error("评测不存在");
        Integer status = Integer.valueOf(body.get("status").toString());
        r.setStatus(status);
        r.setUpdatedAt(LocalDateTime.now());
        // 不管保留还是拒绝 → 清零举报数 + 清空 reports 记录，彻底出队
        r.setReportCount(0);
        jdbcTemplate.update("DELETE FROM reports WHERE target_type = 'review' AND target_id = ?", id);
        reviewMapper.updateById(r);
        return Result.success(r);
    }

    /**
     * 物理删除评测。
     *
     * @param id 评测主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/reviews/{id}")
    public Result<?> deleteReview(@PathVariable String id) {
        reviewMapper.deleteById(id);
        return Result.success();
    }

    // ======== 公告管理 ========

    /**
     * 查询全部公告（管理员视角，包含所有激活/未激活状态）。
     *
     * @return {@link Result} Announcement 列表，按创建时间倒序
     */
    @GetMapping("/announcements")
    public Result<List<Announcement>> getAllAnnouncements() {
        QueryWrapper<Announcement> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(announcementMapper.selectList(qw));
    }

    /**
     * 新增公告。
     *
     * 自动补 createdAt；其他字段（is_active / is_top / publish_date）由前端传入，不做默认值补全。
     *
     * @param a Announcement 请求体
     * @return {@link Result} 插入后的 Announcement
     */
    @PostMapping("/announcements")
    public Result<Announcement> createAnnouncement(@RequestBody Announcement a) {
        a.setCreatedAt(LocalDateTime.now());
        announcementMapper.insert(a);
        return Result.success(a);
    }

    /**
     * 更新公告（按路径 id 覆盖 body.id）。
     *
     * @param id 公告主键
     * @param a  更新内容
     * @return {@link Result} 更新后的 Announcement
     */
    @PutMapping("/announcements/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable String id, @RequestBody Announcement body) {
        body.setId(id);
        announcementMapper.updateById(body);
        return Result.success(body);
    }

    /**
     * 删除公告（物理删除）。
     *
     * @param id 公告主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable String id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }

    // ======== 支付渠道配置 ========

    /**
     * 查询全部支付渠道配置，按 type 和 sort_order 升序。
     *
     * @return {@link Result} PaymentMethod 列表
     */
    @GetMapping("/payment-methods")
    public Result<List<PaymentMethod>> listPaymentMethods() {
        QueryWrapper<PaymentMethod> qw = new QueryWrapper<>();
        qw.orderByAsc("type").orderByAsc("sort_order");
        return Result.success(paymentMethodMapper.selectList(qw));
    }

    /**
     * 更新支付渠道配置（局部字段更新，空值字段不覆盖原值）。
     *
     * feeRate / minFee / maxFee 必须 >= 0 才允许设置；其他字段仅做非空非空串检查。
     * 渠道不存在返回错误。
     *
     * @param id   支付渠道主键
     * @param body 可包含 methodName / feeRate / minFee / maxFee / isActive / sortOrder / remark
     * @return {@link Result} 更新后的 PaymentMethod；不存在返回 error
     */
    @PutMapping("/payment-methods/{id}")
    public Result<PaymentMethod> updatePaymentMethod(@PathVariable String id, @RequestBody PaymentMethod body) {
        PaymentMethod pm = paymentMethodMapper.selectById(id);
        if (pm == null) return Result.error("支付渠道不存在");
        if (body.getMethodName() != null && !body.getMethodName().isBlank()) pm.setMethodName(body.getMethodName());
        if (body.getFeeRate() != null && body.getFeeRate().compareTo(BigDecimal.ZERO) >= 0) pm.setFeeRate(body.getFeeRate());
        if (body.getMinFee() != null && body.getMinFee().compareTo(BigDecimal.ZERO) >= 0) pm.setMinFee(body.getMinFee());
        if (body.getMaxFee() != null && body.getMaxFee().compareTo(BigDecimal.ZERO) >= 0) pm.setMaxFee(body.getMaxFee());
        if (body.getIsActive() != null) pm.setIsActive(body.getIsActive());
        if (body.getSortOrder() != null) pm.setSortOrder(body.getSortOrder());
        if (body.getRemark() != null) pm.setRemark(body.getRemark());
        paymentMethodMapper.updateById(pm);
        return Result.success(pm);
    }

    /** 内置渠道编码：这些渠道被充值/提现主流程硬编码使用，只允许停用不允许删除 */
    private static final Set<String> BUILTIN_PAY_CODES = Set.of("alipay", "wechat", "bank", "balance");

    /**
     * 新增支付渠道（自定义渠道）。
     *
     * 编码必填且全局唯一；费率/手续费为空时按 0 处理，未指定用途时默认 both。
     *
     * @param body 渠道配置（methodCode / methodName / type / feeRate / minFee / maxFee / sortOrder / remark）
     * @return {@link Result} 新增后的渠道；编码重复返回 error
     */
    @PostMapping("/payment-methods")
    public Result<PaymentMethod> createPaymentMethod(@RequestBody PaymentMethod body) {
        String code = body.getMethodCode() == null ? "" : body.getMethodCode().trim();
        String name = body.getMethodName() == null ? "" : body.getMethodName().trim();
        if (code.isEmpty() || name.isEmpty()) return Result.error("渠道编码和渠道名称不能为空");
        Long exists = paymentMethodMapper.selectCount(new QueryWrapper<PaymentMethod>().eq("method_code", code));
        if (exists != null && exists > 0) return Result.error("渠道编码「" + code + "」已存在");
        body.setId(UUID.randomUUID().toString());
        body.setMethodCode(code);
        body.setMethodName(name);
        if (body.getType() == null || body.getType().isBlank()) body.setType("both");
        if (body.getFeeRate() == null || body.getFeeRate().compareTo(BigDecimal.ZERO) < 0) body.setFeeRate(BigDecimal.ZERO);
        if (body.getMinFee() == null || body.getMinFee().compareTo(BigDecimal.ZERO) < 0) body.setMinFee(BigDecimal.ZERO);
        if (body.getMaxFee() == null || body.getMaxFee().compareTo(BigDecimal.ZERO) < 0) body.setMaxFee(BigDecimal.ZERO);
        if (body.getSortOrder() == null) body.setSortOrder(0);
        body.setCreatedAt(LocalDateTime.now());
        body.setUpdatedAt(LocalDateTime.now());
        paymentMethodMapper.insert(body);
        return Result.success(body);
    }

    /**
     * 删除支付渠道。
     *
     * 内置渠道（alipay/wechat/bank/balance）不允许删除；已有订单或提现记录引用的渠道
     * 也不允许删除，避免历史数据失去渠道含义——这两种情况都应改用「停用」。
     *
     * @param id 渠道主键
     * @return {@link Result} 删除结果
     */
    @DeleteMapping("/payment-methods/{id}")
    public Result<String> deletePaymentMethod(@PathVariable String id) {
        PaymentMethod pm = paymentMethodMapper.selectById(id);
        if (pm == null) return Result.error("支付渠道不存在");
        String code = pm.getMethodCode();
        if (BUILTIN_PAY_CODES.contains(code)) {
            return Result.error("内置渠道「" + pm.getMethodName() + "」不可删除，请改用停用");
        }
        long refs = paymentMethodMapper.countOrderRefs(code) + paymentMethodMapper.countWithdrawRefs(code);
        if (refs > 0) {
            return Result.error("该渠道已有 " + refs + " 条历史记录引用，不可删除，请改用停用");
        }
        paymentMethodMapper.deleteById(id);
        return Result.success("已删除");
    }

    // ======== 数据导出（CSV，UTF-8 + BOM 保证 Excel 直接双击不乱码） ========

    /**
     * 导出用户列表为 CSV（支持与列表页相同的关键词/角色筛选）。
     *
     * 导出列：ID / 用户名 / 昵称 / 手机号 / 邮箱 / 角色 / 钱包余额 / 注册时间。
     * 始终导出 password_hash 之外的字段，不导出任何密码信息。
     *
     * @param keyword 可选，用户名/昵称/手机号模糊筛选
     * @param role    可选，按 user_type 精确筛选
     * @return CSV 文件流（attachment）
     */
    @GetMapping("/export/users")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (u.username LIKE ? OR u.nickname LIKE ? OR u.phone LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (role != null && !role.isBlank()) {
            where.append(" AND u.user_type = ?");
            params.add(role);
        }
        String sql = "SELECT u.id, u.username, u.nickname, u.phone, u.email, u.user_type, u.created_at, " +
                "w.balance AS wallet_balance FROM users u " +
                "LEFT JOIN user_wallets w ON w.user_id = u.id " +
                where + " ORDER BY u.created_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        return csvResponse("users",
                new String[]{"ID", "用户名", "昵称", "手机号", "邮箱", "角色", "钱包余额", "注册时间"},
                new String[]{"id", "username", "nickname", "phone", "email", "user_type", "wallet_balance", "created_at"},
                rows);
    }

    /**
     * 导出订单列表为 CSV（可按状态筛选）。
     *
     * 导出列：订单号 / 买家ID / 游戏 / 类型 / 金额 / CDKey / 状态 / 创建时间。
     *
     * @param status 可选，订单状态
     * @return CSV 文件流（attachment）
     */
    @GetMapping("/export/orders")
    public ResponseEntity<byte[]> exportOrders(@RequestParam(required = false) String status) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND status = ?");
            params.add(status);
        }
        String sql = "SELECT id, order_no, buyer_id, game_name, order_type, total_price, cdkey, status, created_at " +
                "FROM orders " + where + " ORDER BY created_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        return csvResponse("orders",
                new String[]{"订单号", "买家ID", "游戏", "类型", "金额", "CDKey", "状态", "创建时间"},
                new String[]{"order_no", "buyer_id", "game_name", "order_type", "total_price", "cdkey", "status", "created_at"},
                rows);
    }

    /**
     * 导出游戏列表为 CSV。
     *
     * @return CSV 文件流（attachment）
     */
    @GetMapping("/export/games")
    public ResponseEntity<byte[]> exportGames() {
        String sql = "SELECT id, name, name_cn, developer, release_date, price, stock, is_presale " +
                "FROM games ORDER BY created_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return csvResponse("games",
                new String[]{"ID", "名称", "中文名", "开发商", "发行日期", "价格", "库存", "是否预售"},
                new String[]{"id", "name", "name_cn", "developer", "release_date", "price", "stock", "is_presale"},
                rows);
    }

    /**
     * 导出提现申请为 CSV（可按状态筛选）。
     *
     * @param status 可选，提现状态（pending / success / failed）
     * @return CSV 文件流（attachment）
     */
    @GetMapping("/export/withdrawals")
    public ResponseEntity<byte[]> exportWithdrawals(@RequestParam(required = false) String status) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            where.append(" AND w.status = ?");
            params.add(status);
        }
        String sql = "SELECT w.id, w.order_no, w.user_id, u.username, w.pay_method, w.account, w.real_name, " +
                "w.bank_name, w.amount, w.fee, w.net_amount, w.status, w.applied_at FROM withdraw_records w " +
                "LEFT JOIN users u ON u.id = w.user_id " + where + " ORDER BY w.applied_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        return csvResponse("withdrawals",
                new String[]{"提现单号", "用户ID", "用户名", "收款方式", "收款账号", "开户名", "银行", "金额", "手续费", "实际到账", "状态", "申请时间"},
                new String[]{"order_no", "user_id", "username", "pay_method", "account", "real_name", "bank_name", "amount", "fee", "net_amount", "status", "applied_at"},
                rows);
    }

    /**
     * 统一构造 CSV 响应（UTF-8 + BOM，文件名带导出日期）。
     *
     * @param name    导出名（用于文件名 steampy-{name}-yyyyMMdd.csv）
     * @param headers 中文表头
     * @param keys    与表头一一对应的 Map 键（原生 SQL 查询结果的列名）
     * @param rows    数据行
     * @return HTTP 附件响应
     */
    private ResponseEntity<byte[]> csvResponse(String name, String[] headers, String[] keys,
                                               List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM：Excel 打开不乱码
        sb.append(String.join(",", headers)).append("\r\n");
        for (Map<String, Object> row : rows) {
            for (int i = 0; i < keys.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(csvCell(row.get(keys[i])));
            }
            sb.append("\r\n");
        }
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "steampy-" + name + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        h.setContentDispositionFormData("attachment", filename);
        h.setContentLength(body.length);
        return new ResponseEntity<>(body, h, org.springframework.http.HttpStatus.OK);
    }

    /**
     * 单个 CSV 单元格转义：含逗号/引号/换行时用双引号包裹并转义内部引号；
     * 以 = + - @ 开头的内容前置单引号，避免 Excel 公式注入。
     */
    private String csvCell(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    /** 安全取字符串（兼容前端传 String / Number / null）。 */
    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /** 把前端传入的 ids 数组（List<?>）安全转为 List&lt;String&gt;，非法元素自动忽略。 */
    private List<String> toStringList(Object o) {
        List<String> list = new ArrayList<>();
        if (o instanceof List<?> raw) {
            for (Object item : raw) {
                if (item == null) continue;
                String s = String.valueOf(item);
                if (!s.isBlank()) list.add(s);
            }
        }
        return list;
    }
}


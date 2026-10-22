package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Listing;
import com.steampy.entity.Order;
import com.steampy.entity.User;
import com.steampy.entity.Game;
import com.steampy.mapper.ListingMapper;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.UserGameMapper;
import com.steampy.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ListingController
 * 负责游戏上架记录的 HTTP 接口层，覆盖 CDKey 上架/查询/改价/下架/激活/批量操作等全生命周期。
 *
 * 状态机：available（在售）↔ pending_activation（待激活/下架）→ sold（已售出，不可删、不可改）。
 * 用户名脱敏：所有 seller_name 通过 {@link #maskUsername} / {@link #maskStr} 处理为 "首***尾" 格式，避免泄露真实昵称。
 * 分组聚合：列表查询提供按 seller+price 分组、按 game+seller+price 分组两种模式，前端可直接拿到库存数，避免前端二次聚合。
 *
 * 提供的核心接口：
 *   - POST   /api/listings                   — 单条上架一个 CDKey
 *   - POST   /api/listings/batch             — 批量上架
 *   - GET    /api/listings/available         — 查询可售上架（可按 gameId/region 过滤）
 *   - GET    /api/listings/available-grouped — 按 seller+price 聚合某游戏的可售上架
 *   - GET    /api/listings/available-grouped-all — 全平台所有可售上架按 game+seller+price 聚合，跨游戏混合排序
 *   - GET    /api/listings/py-sellers        — 查询礼物代购（py）卖家列表
 *   - GET    /api/listings/check-cdkey       — CDKey 查重
 *   - GET    /api/listings/seller/{sellerId} — 查询某卖家的全部上架
 *   - GET    /api/listings/{id}              — 查询单条上架详情
 *   - DELETE /api/listings/{id}              — 删除上架（sold 不可删）
 *   - PUT    /api/listings/{id}/sold         — 内部标记为已售出
 *   - PUT    /api/listings/{id}/price        — 改单个上架价格（available/pending_activation）
 *   - PUT    /api/listings/batch-price       — 批量改价
 *   - PUT    /api/listings/{id}/soft-delete  — 下架 → pending_activation（available / sold 都可）
 *   - PUT    /api/listings/{id}/relist       — pending_activation → 重新上架
 *   - PUT    /api/listings/batch-soft-delete — 批量下架
 *   - PUT    /api/listings/batch-relist      — 批量上架
 *   - DELETE /api/listings/batch             — 批量删除（sold 跳过）
 *   - PUT    /api/listings/{id}/self-activate — 卖家自己激活 pending_activation 的 CDK（加入 user_games + 删除 listing）
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingMapper listingMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private UserGameMapper userGameMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 为一批 Listing 填充脱敏后的 seller_name（批量查询用户表，避免 N+1）。
     *
     * 查出所有 seller_id 对应的 User，取 nickname（为空则取 username），再调用 {@link #maskUsername} 脱敏后写入 Listing.sellerName 字段。
     *
     * @param listings Listing 列表（原地修改 sellerName）
     */
    private void fillSellerNames(List<Listing> listings) {
        if (listings.isEmpty()) return;
        Set<String> sellerIds = listings.stream()
            .map(Listing::getSellerId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (sellerIds.isEmpty()) return;

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.in("id", sellerIds);
        List<User> users = userMapper.selectList(qw);
        Map<String, String> idToUser = new HashMap<>();
        for (User u : users) {
            idToUser.put(u.getId(), u.getNickname() != null ? u.getNickname() : u.getUsername());
        }
        for (Listing l : listings) {
            String raw = idToUser.getOrDefault(l.getSellerId(), l.getSellerId());
            l.setSellerName(maskUsername(raw));
        }
    }

    /**
     * 用户名脱敏：首***尾，至少保留首尾各 1 字。
     *
     * 示例："123456"→"1***6"、"admin"→"a***n"、"a"→"a"、"ab"→"a*b"。
     *
     * @param name 原始昵称/用户名
     * @return 脱敏后的字符串
     */
    private String maskUsername(String name) {
        if (name == null || name.isEmpty()) return "";
        int len = name.length();
        if (len <= 2) {
            if (len == 1) return name;
            return name.charAt(0) + "*" + name.charAt(1);
        }
        return name.charAt(0) + "***" + name.charAt(len - 1);
    }

    /**
     * 上架一个 CDKey（单条）。
     *
     * 自动补 id（UUID）、status（默认 available）、version（默认 "标准版"）、region（默认 "国区"）、createdAt / updatedAt（当前时间）。
     *
     * @param listing Listing 请求体（需含 sellerId / gameId / cdkey / price 等业务字段）
     * @return {@link Result} 插入后的 Listing 实体
     */
    @PostMapping
    public Result<Listing> createListing(@RequestBody Listing listing) {
        if (!fillGameId(listing)) {
            return Result.error("游戏「" + safeName(listing.getGameName()) + "」不存在，无法上架");
        }
        listing.setId(UUID.randomUUID().toString());
        if (listing.getStatus() == null) listing.setStatus("available");
        if (listing.getVersion() == null) listing.setVersion("标准版");
        if (listing.getRegion() == null) listing.setRegion("国区");
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        listingMapper.insert(listing);
        return Result.success(listing);
    }

    /**
     * 批量上架多条 CDKey（循环逐条入库）。
     *
     * 自动补全同 {@link #createListing}：UUID id、status/version/region 默认值、时间戳。
     * 入库前先校验游戏是否存在，全部通过才写库，避免只写进去一半。
     *
     * @param listings Listing 数组
     * @return {@link Result} 插入后的 Listing 列表
     */
    @PostMapping("/batch")
    public Result<List<Listing>> createBatch(@RequestBody List<Listing> listings) {
        if (listings == null || listings.isEmpty()) return Result.error("上架内容为空");
        for (Listing l : listings) {
            if (!fillGameId(l)) {
                return Result.error("游戏「" + safeName(l.getGameName()) + "」不存在，无法上架");
            }
        }
        for (Listing l : listings) {
            l.setId(UUID.randomUUID().toString());
            if (l.getStatus() == null) l.setStatus("available");
            if (l.getVersion() == null) l.setVersion("标准版");
            if (l.getRegion() == null) l.setRegion("国区");
            l.setCreatedAt(LocalDateTime.now());
            l.setUpdatedAt(LocalDateTime.now());
            listingMapper.insert(l);
        }
        return Result.success(listings);
    }

    /**
     * 补全 gameId：前端没传（或传成 null）时，按游戏名回查 games 表。
     * <p>
     * listings.game_id 是 NOT NULL 列，缺值会直接抛 500，这里提前兜住。
     * </p>
     *
     * @param l 上架记录
     * @return true 表示 gameId 可用；false 表示游戏名查不到
     */
    private boolean fillGameId(Listing l) {
        if (l.getGameId() != null && !l.getGameId().isBlank()) return true;
        if (l.getGameName() == null || l.getGameName().isBlank()) return false;
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.eq("name", l.getGameName().trim()).last("LIMIT 1");
        Game g = gameMapper.selectOne(qw);
        if (g == null) return false;
        l.setGameId(g.getId());
        l.setGameName(g.getName());
        if (l.getGameImage() == null || l.getGameImage().isBlank()) l.setGameImage(g.getImage());
        return true;
    }

    /**
     * 取用于错误提示的游戏名，空值给个占位符。
     *
     * @param name 游戏名
     * @return 可读名称
     */
    private String safeName(String name) {
        return name == null || name.isBlank() ? "未知游戏" : name.trim();
    }

    /**
     * 查询全部可售上架（status=available），按价格升序。
     *
     * 可选 gameId / region 过滤。返回前会调 {@link #fillSellerNames} 批量填充脱敏后的 seller_name。
     *
     * @param gameId 可选，游戏 ID 过滤
     * @param region 可选，区域过滤（如 "国区" / "港区"）
     * @return {@link Result} 带 seller_name 的 Listing 列表
     */
    @GetMapping("/available")
    public Result<List<Listing>> getAvailable(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String region) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("status", "available");
        if (gameId != null) qw.eq("game_id", gameId);
        if (region != null && !region.isEmpty()) qw.eq("region", region);
        qw.orderByAsc("price");
        List<Listing> list = listingMapper.selectList(qw);
        fillSellerNames(list);
        return Result.success(list);
    }

    /**
     * 按 seller + price 分组聚合某游戏的可售上架，返回库存统计。
     *
     * game_id 或 game_name 必填其一，都不传返回错误。
     * 分组键：seller_id + price；聚合结果含 stock（数量）、version、region、listing_ids（完整 CDK 列表）。
     * 返回前会脱敏 seller_name。
     *
     * @param game_id  可选（与 game_name 二选一），游戏 ID
     * @param game_name 可选，游戏英文名
     * @return {@link Result} 聚合后的 Map 列表，按价格升序
     */
    @GetMapping("/available-grouped")
    public Result<List<Map<String, Object>>> getAvailableGrouped(
            @RequestParam(required = false) String game_id,
            @RequestParam(required = false) String game_name) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("status", "available");
        if (game_id != null && !game_id.isEmpty()) {
            try { qw.eq("game_id", game_id.toString()); } catch (Exception e) { return Result.error("game_id 格式错误"); }
        } else if (game_name != null && !game_name.isEmpty()) {
            qw.eq("game_name", game_name);
        } else {
            return Result.error("game_id 或 game_name 必填一个");
        }
        qw.orderByAsc("price");
        List<Listing> all = listingMapper.selectList(qw);
        fillSellerNames(all);

        // 按 seller_id + price 分组
        Map<String, List<Listing>> groups = new LinkedHashMap<>();
        for (Listing l : all) {
            String key = l.getSellerId() + "|" + l.getPrice();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(l);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Listing>> entry : groups.entrySet()) {
            List<Listing> group = entry.getValue();
            Listing first = group.get(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("seller_id", first.getSellerId());
            row.put("seller_name", first.getSellerName());
            row.put("price", first.getPrice());
            row.put("stock", group.size());
            row.put("version", first.getVersion());
            row.put("region", first.getRegion());
            row.put("listing_ids", group.stream().map(Listing::getId).collect(Collectors.toList()));
            result.add(row);
        }
        return Result.success(result);
    }

    /**
     * 全平台所有可售上架按 game + seller + price 分组聚合，跨游戏混合排序。
     *
     * 分组键：game_id + seller_id + price；结果行含 game_image / original_price / discount 等额外游戏信息。
     * 最终按 price 升序，同价按 game_name 升序。
     *
     * @return {@link Result} 聚合后的 Map 列表
     */
    @GetMapping("/available-grouped-all")
    public Result<List<Map<String, Object>>> getAvailableGroupedAll() {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("status", "available");
        List<Listing> all = listingMapper.selectList(qw);
        fillSellerNames(all);

        // 查所有游戏的 original_price 做索引
        QueryWrapper<Game> gqw = new QueryWrapper<>();
        List<Game> games = gameMapper.selectList(gqw);
        Map<String, BigDecimal> originalPriceMap = new HashMap<>();
        for (Game g : games) {
            if (g.getOriginalPrice() != null) {
                originalPriceMap.put(g.getId(), g.getOriginalPrice());
            }
        }

        // 按 game_id + seller_id + price 分组
        Map<String, List<Listing>> groups = new LinkedHashMap<>();
        for (Listing l : all) {
            String key = (l.getGameId() == null ? "" : l.getGameId()) + "|" + l.getSellerId() + "|" + l.getPrice();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(l);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Listing>> entry : groups.entrySet()) {
            List<Listing> group = entry.getValue();
            Listing first = group.get(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("game_id", first.getGameId());
            row.put("game_name", first.getGameName());
            row.put("game_image", first.getGameImage());
            row.put("seller_id", first.getSellerId());
            row.put("seller_name", first.getSellerName());
            row.put("price", first.getPrice());
            row.put("original_price", originalPriceMap.getOrDefault(first.getGameId(), BigDecimal.ZERO));
            row.put("stock", group.size());
            row.put("version", first.getVersion());
            row.put("region", first.getRegion());
            row.put("listing_ids", group.stream().map(Listing::getId).collect(Collectors.toList()));
            result.add(row);
        }

        // 价格升序，同价按 game_name
        result.sort((a, b) -> {
            int cmp = ((java.math.BigDecimal) a.get("price")).compareTo((java.math.BigDecimal) b.get("price"));
            if (cmp != 0) return cmp;
            return String.valueOf(a.get("game_name")).compareTo(String.valueOf(b.get("game_name")));
        });
        return Result.success(result);
    }

    // ======== 查代购卖家列表（py 类型，支持按 game_id 过滤）=======

    /**
     * 查询礼物代购（type=py）卖家列表，含实时成交数/流畅度/折扣计算等派生字段。
     *
     * 返回字段：listing_id / seller_id / seller_name（脱敏）/ seller_avatar / game_id / game_name / price / quota / auto_deliver / region / sale_price / original_price / discount_pct / deals / fluency / fluency_class / deliver_type。
     * deals 通过 {@link #countPyDeals} 实时按订单表聚合。
     * discount_pct = price / original_price * 100（保留整数）。
     *
     * @param gameId 可选，游戏 ID 过滤
     * @param region 可选，区域模糊匹配
     * @return {@link Result} 卖家行 Map 列表
     */
    @GetMapping("/py-sellers")
    public Result<List<Map<String, Object>>> listPySellers(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String region) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("type", "py").eq("status", "available");
        if (gameId != null) qw.eq("game_id", gameId);
        if (region != null && !region.isEmpty()) qw.like("region", region);
        qw.orderByAsc("price");
        List<Listing> listings = listingMapper.selectList(qw);

        // 查卖家用户信息
        Set<String> sellerIds = listings.stream().map(Listing::getSellerId).collect(Collectors.toSet());
        Map<String, User> userMap = new HashMap<>();
        if (!sellerIds.isEmpty()) {
            userMapper.selectBatchIds(sellerIds).forEach(u -> userMap.put(u.getId(), u));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Listing l : listings) {
            Map<String, Object> row = new HashMap<>();
            row.put("listing_id", l.getId());
            row.put("seller_id", l.getSellerId());
            User u = userMap.get(l.getSellerId());
            String rawName = u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "未知";
            row.put("seller_name", maskStr(rawName));
            row.put("seller_avatar", u != null ? u.getAvatarUrl() : null);
            row.put("game_id", l.getGameId());
            row.put("game_name", l.getGameName());
            row.put("price", l.getPrice());
            row.put("quota", l.getQuota());
            row.put("auto_deliver", l.getAutoDeliver() != null && l.getAutoDeliver());
            row.put("region", l.getRegion());
            row.put("sale_price", l.getPrice());
            row.put("original_price", l.getOriginalPrice());
            // 折扣
            if (l.getOriginalPrice() != null && l.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = l.getPrice().divide(l.getOriginalPrice(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                row.put("discount_pct", pct.setScale(0, java.math.RoundingMode.HALF_UP).intValue());
            } else {
                row.put("discount_pct", 100);
            }
            // mock 成交数（从 orders 表按 seller_id + py 类型统计）
            row.put("deals", countPyDeals(l.getSellerId()));
            // 流畅度（quota > 0 ? 顺畅 : 一般）
            row.put("fluency", l.getQuota() != null && l.getQuota().compareTo(BigDecimal.ZERO) > 0 ? "顺畅" : "一般");
            row.put("fluency_class", l.getQuota() != null && l.getQuota().compareTo(BigDecimal.ZERO) > 0 ? "dot-green" : "dot-yellow");
            // 发货类型
            row.put("deliver_type", l.getAutoDeliver() != null && l.getAutoDeliver() ? "自动发货" : "手动发货");
            result.add(row);
        }
        return Result.success(result);
    }

    /**
     * 统计某卖家的礼物代购（py 类型）已完成订单数。
     *
     * @param sellerId 卖家用户 ID
     * @return 已完成 py 订单数；sellerId 为 null 时返回 0
     */
    private int countPyDeals(String sellerId) {
        if (sellerId == null) return 0;
        QueryWrapper<com.steampy.entity.Order> qw = new QueryWrapper<>();
        qw.eq("seller_id", sellerId).eq("order_type", "py").eq("status", "completed");
        return orderMapper.selectCount(qw).intValue();
    }

    /**
     * 简短版用户名脱敏：取首字 + *** + 尾字（长度 <= 2 时只取首字 + ***）。
     *
     * @param s 原始字符串
     * @return 脱敏后的字符串
     */
    private String maskStr(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.length() <= 2) return s.charAt(0) + "***";
        return s.charAt(0) + "***" + s.charAt(s.length() - 1);
    }

    /**
     * CDKey 查重接口（上架前校验）。
     *
     * 将传入的 cdkey trim 后转大写，与数据库中所有 Listing.cdkey（全量，不限状态）做不区分大小写比对。
     * 已 sold 的也视为重复，防止重新上架。
     *
     * @param cdkey 待校验的 CDKey
     * @return {@link Result} 含 { exists: boolean } 的 Map
     */
    @GetMapping("/check-cdkey")
    public Result<Map<String, Object>> checkCdkey(@RequestParam String cdkey) {
        String normalized = cdkey == null ? "" : cdkey.trim().toUpperCase();
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("UPPER(cdkey)", normalized);
        long count = listingMapper.selectCount(qw);
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("exists", count > 0);
        return Result.success(r);
    }

    /**
     * 查询某卖家的全部上架记录（按创建时间倒序）。
     *
     * @param sellerId 卖家用户 ID
     * @return {@link Result} Listing 列表
     */
    @GetMapping("/seller/{sellerId}")
    public Result<List<Listing>> getBySeller(@PathVariable String sellerId) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("seller_id", sellerId);
        qw.orderByDesc("created_at");
        return Result.success(listingMapper.selectList(qw));
    }

    /**
     * 查询单条上架详情。
     *
     * 返回前会调 {@link #fillSellerNames} 填充脱敏后的 seller_name。
     *
     * @param id Listing 主键
     * @return {@link Result} Listing 实体（可能为 null）
     */
    @GetMapping("/{id}")
    public Result<Listing> getById(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l != null) fillSellerNames(Collections.singletonList(l));
        return Result.success(l);
    }

    /**
     * 删除上架记录。
     *
     * available / pending_activation 状态可直接物理删除；sold 状态禁止删除，防止丢失交易记录。
     *
     * @param id Listing 主键
     * @return {@link Result} 成功返回 null；已售出返回 error
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteListing(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if ("sold".equals(l.getStatus())) return Result.error("已售出的 CDK 不能删除");
        listingMapper.deleteById(id);
        return Result.success(null);
    }

    /**
     * 内部接口：标记 Listing 为已售出。
     *
     * 供订单交易完成后内部调用，设置 status=sold、soldAt=当前时间、updatedAt=当前时间。
     *
     * @param id Listing 主键
     * @return {@link Result} 通用成功响应；不存在返回 error
     */
    @PutMapping("/{id}/sold")
    public Result<Void> markSold(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        l.setStatus("sold");
        l.setSoldAt(LocalDateTime.now());
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 改单个价格 ========

    /**
     * 改单个上架的价格。
     *
     * 仅 available / pending_activation 状态可以改价；sold 状态禁止。
     * body 必须含 price 字段。
     *
     * @param id   Listing 主键
     * @param body 含 price（数字）
     * @return {@link Result} 通用成功响应；状态不允许或缺少 price 返回 error
     */
    @PutMapping("/{id}/price")
    public Result<Void> updatePrice(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        String s = l.getStatus();
        if (!"available".equals(s) && !"pending_activation".equals(s)) {
            return Result.error("只有在售/待激活的可以改价（当前状态: " + s + "）");
        }
        Object p = body.get("price");
        if (p == null) return Result.error("缺少 price");
        l.setPrice(new BigDecimal(p.toString()));
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 批量改价 ========

    /**
     * 批量改上架价格。
     *
     * body.updates 为 { listingId: newPrice } 的 Map；逐条校验仅 available 状态允许改价，其他状态跳过不计入 ok。
     *
     * @param body 含 updates（Map<String, Number>）
     * @return {@link Result} 含 { updated: int } 的 Map，updated 为实际改价成功条数
     */
    @PutMapping("/batch-price")
    public Result<Map<String, Object>> batchUpdatePrice(@RequestBody Map<String, Object> body) {
        Map<String, Object> updates = (Map<String, Object>) body.get("updates");
        if (updates == null || updates.isEmpty()) return Result.error("updates 为空");
        int ok = 0;
        for (Map.Entry<String, Object> e : updates.entrySet()) {
            Listing l = listingMapper.selectById(e.getKey());
            if (l != null && "available".equals(l.getStatus())) {
                l.setPrice(new BigDecimal(e.getValue().toString()));
                l.setUpdatedAt(LocalDateTime.now());
                listingMapper.updateById(l);
                ok++;
            }
        }
        Map<String, Object> r = new HashMap<>();
        r.put("updated", ok);
        return Result.success(r);
    }

    // ======== 下架 → 待激活（available 和 sold 都可以变成 pending_activation）=======

    /**
     * 下架（软删除）上架记录：available / sold → pending_activation。
     *
     * 两者区别：available 下架时清掉 soldAt；sold 下架时保留原 soldAt 记录（用于审计）。
     * 其他状态（如 pending_activation 本身）禁止下架。
     *
     * @param id Listing 主键
     * @return {@link Result} 通用成功响应；当前状态不允许返回 error
     */
    @PutMapping("/{id}/soft-delete")
    public Result<Void> softDelete(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        String cur = l.getStatus();
        if (!"available".equals(cur) && !"sold".equals(cur)) {
            return Result.error("当前状态(" + cur + ")不允许下架");
        }
        // 不管 available 还是 sold → 都变成 pending_activation
        // available: 清掉 order_id/sold_at；sold: 保留原 order_id/sold_at 记录
        l.setStatus("pending_activation");
        if ("available".equals(cur)) {
            l.setSoldAt(null);
        }
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 待激活 → 重新上架 ========

    /**
     * 重新上架（relist）：pending_activation → available。
     *
     * 同时清空 soldAt 时间戳，重置为全新在售状态。
     *
     * @param id Listing 主键
     * @return {@link Result} 通用成功响应；非 pending_activation 返回 error
     */
    @PutMapping("/{id}/relist")
    public Result<Void> relistPending(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if (!"pending_activation".equals(l.getStatus())) return Result.error("只有待激活的可以重新上架");
        // 清除 sold_at，重新变成 available
        l.setStatus("available");
        l.setSoldAt(null);
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 批量下架: available → pending_activation ========

    /**
     * 批量下架（仅 available 状态允许）。
     *
     * @param body 含 ids（List<String>）
     * @return {@link Result} 含 { updated: int, skipped: int } 的 Map
     */
    @PutMapping("/batch-soft-delete")
    public Result<Map<String, Object>> batchSoftDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = (java.util.List<String>) body.get("ids");
        if (ids == null || ids.isEmpty()) return Result.error("未选择");
        int ok = 0, skip = 0;
        for (String id : ids) {
            Listing l = listingMapper.selectById(id);
            if (l == null || !"available".equals(l.getStatus())) { skip++; continue; }
            l.setStatus("pending_activation");
            l.setSoldAt(null);
            l.setUpdatedAt(LocalDateTime.now());
            listingMapper.updateById(l);
            ok++;
        }
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("updated", ok); r.put("skipped", skip);
        return Result.success(r);
    }

    // ======== 批量上架: pending_activation → available ========

    /**
     * 批量上架（仅 pending_activation 状态允许）。
     *
     * @param body 含 ids（List<String>）
     * @return {@link Result} 含 { updated: int, skipped: int } 的 Map
     */
    @PutMapping("/batch-relist")
    public Result<Map<String, Object>> batchRelist(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = (java.util.List<String>) body.get("ids");
        if (ids == null || ids.isEmpty()) return Result.error("未选择");
        int ok = 0, skip = 0;
        for (String id : ids) {
            Listing l = listingMapper.selectById(id);
            if (l == null || !"pending_activation".equals(l.getStatus())) { skip++; continue; }
            l.setStatus("available");
            l.setSoldAt(null);
            l.setUpdatedAt(LocalDateTime.now());
            listingMapper.updateById(l);
            ok++;
        }
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("updated", ok); r.put("skipped", skip);
        return Result.success(r);
    }

    // ======== 批量删除: available + pending_activation 可删，sold 跳过 ========

    /**
     * 批量删除上架记录。
     *
     * available + pending_activation 可直接物理删除；sold 状态跳过保留交易记录。
     *
     * @param body 含 ids（List<String>）
     * @return {@link Result} 含 { deleted: int, skipped: int } 的 Map
     */
    @DeleteMapping("/batch")
    public Result<Map<String, Object>> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = (java.util.List<String>) body.get("ids");
        if (ids == null || ids.isEmpty()) return Result.error("未选择");
        int ok = 0, skip = 0;
        for (String id : ids) {
            Listing l = listingMapper.selectById(id);
            if (l == null) { skip++; continue; }
            if ("sold".equals(l.getStatus())) { skip++; continue; }
            listingMapper.deleteById(id);
            ok++;
        }
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("deleted", ok); r.put("skipped", skip);
        return Result.success(r);
    }

    // ======== 待激活 → 自己激活（加入 user_games 并删除 listing）=======

    /**
     * 卖家自己激活待激活状态的 CDK：加入 user_games 库 + 删除原 listing。
     *
     * 仅 pending_activation 状态允许执行。激活时创建一条 UserGame 记录，包含 gameId / cdkey / version 等字段，status=activated，source=cdkey；然后物理删除这条 Listing（CDK 已被消耗，不可再售）。
     * 会尝试反查关联 Order 回填 orderId。
     *
     * @param id Listing 主键
     * @return {@link Result} 通用成功响应；非 pending_activation 返回 error
     */
    @PutMapping("/{id}/self-activate")
    public Result<Void> selfActivate(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if (!"pending_activation".equals(l.getStatus())) return Result.error("只有待激活的可以自己激活");
        // 加入 user_games
        com.steampy.entity.UserGame ug = new com.steampy.entity.UserGame();
        ug.setId(UUID.randomUUID().toString());
        ug.setUserId(l.getSellerId());
        // 反查是否有订单关联此 listing（listing.order_id 已删）
        Order relatedOrder = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("listing_id", l.getId())
        );
        ug.setOrderId(relatedOrder != null ? relatedOrder.getId() : null);
        ug.setGameId(l.getGameId());
        ug.setGameName(l.getGameName());
        ug.setGameImage(l.getGameImage());
        ug.setCdkey(l.getCdkey());
        ug.setVersion(l.getVersion());
        ug.setStatus("activated");
        ug.setSource("cdkey"); // 自己激活也算 cdkey 来源
        ug.setPurchaseDate(LocalDateTime.now());
        ug.setActivationDate(LocalDateTime.now());
        ug.setCreatedAt(LocalDateTime.now());
        userGameMapper.insert(ug);
        // 删除 listing（这个 CDK 已经被消耗了）
        listingMapper.deleteById(id);
        return Result.success(null);
    }
}



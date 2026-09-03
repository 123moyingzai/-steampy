package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Listing;
import com.steampy.entity.User;
import com.steampy.entity.Game;
import com.steampy.mapper.ListingMapper;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.UserGameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    // 给 listing 填充 seller_name（脱敏）
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

    // 脱敏：首***尾，至少保留首尾各1字
    // "123456"  → "1***6"
    // "admin"    → "a***n"
    // "a"        → "a"
    // "ab"       → "a*b"
    private String maskUsername(String name) {
        if (name == null || name.isEmpty()) return "";
        int len = name.length();
        if (len <= 2) {
            if (len == 1) return name;
            return name.charAt(0) + "*" + name.charAt(1);
        }
        return name.charAt(0) + "***" + name.charAt(len - 1);
    }

    // 上架一个 CDKey
    @PostMapping
    public Result<Listing> createListing(@RequestBody Listing listing) {
        listing.setId(UUID.randomUUID().toString());
        if (listing.getStatus() == null) listing.setStatus("available");
        if (listing.getVersion() == null) listing.setVersion("标准版");
        if (listing.getRegion() == null) listing.setRegion("国区");
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        listingMapper.insert(listing);
        return Result.success(listing);
    }

    // 批量上架
    @PostMapping("/batch")
    public Result<List<Listing>> createBatch(@RequestBody List<Listing> listings) {
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

    // 查全部可售（带脱敏的 seller_name）
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

    // 查某个游戏的可售（含按 卖家+价格 聚合的库存统计）
    @GetMapping("/available-grouped")
    public Result<List<Map<String, Object>>> getAvailableGrouped(
            @RequestParam(required = false) String game_id,
            @RequestParam(required = false) String game_name) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("status", "available");
        if (game_id != null && !game_id.isEmpty()) {
            try { qw.eq("game_id", Long.parseLong(game_id)); } catch (Exception e) { return Result.error("game_id 格式错误"); }
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

    // 查所有游戏的可售 listings（按 game_id + seller_id + price 聚合，跨游戏混合排序）
    @GetMapping("/available-grouped-all")
    public Result<List<Map<String, Object>>> getAvailableGroupedAll() {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("status", "available");
        List<Listing> all = listingMapper.selectList(qw);
        fillSellerNames(all);

        // 查所有游戏的 original_price 做索引
        QueryWrapper<Game> gqw = new QueryWrapper<>();
        List<Game> games = gameMapper.selectList(gqw);
        Map<Long, BigDecimal> originalPriceMap = new HashMap<>();
        for (Game g : games) {
            if (g.getOriginalPrice() != null) {
                originalPriceMap.put(g.getId(), g.getOriginalPrice());
            }
        }

        // 按 game_id + seller_id + price 分组
        Map<String, List<Listing>> groups = new LinkedHashMap<>();
        for (Listing l : all) {
            String key = (l.getGameId() == null ? 0 : l.getGameId()) + "|" + l.getSellerId() + "|" + l.getPrice();
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

    /** 查重接口 —— 给定 cdkey，返回是否已存在于 listings 表（不区分状态，sold 的也算重复）*/
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

    // 查某个卖家的所有上架
    @GetMapping("/seller/{sellerId}")
    public Result<List<Listing>> getBySeller(@PathVariable String sellerId) {
        QueryWrapper<Listing> qw = new QueryWrapper<>();
        qw.eq("seller_id", sellerId);
        qw.orderByDesc("created_at");
        return Result.success(listingMapper.selectList(qw));
    }

    // 查单个
    @GetMapping("/{id}")
    public Result<Listing> getById(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l != null) fillSellerNames(Collections.singletonList(l));
        return Result.success(l);
    }

    // 下架 / 删除（只有 available 可以硬删除；sold 用 soft-delete；pending_activation 用 relist/self-activate）
    @DeleteMapping("/{id}")
    public Result<Void> deleteListing(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if ("sold".equals(l.getStatus())) return Result.error("已售出的 CDK 不能直接删除，请使用'下架拿回'");
        if ("pending_activation".equals(l.getStatus())) return Result.error("待激活的 CDK 不能直接删除，请使用'自己激活'或'重新上架'");
        listingMapper.deleteById(id);
        return Result.success(null);
    }

    // 标记已售出（内部调用）
    @PutMapping("/{id}/sold")
    public Result<Void> markSold(@PathVariable String id, @RequestParam String orderId) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        l.setStatus("sold");
        l.setOrderId(orderId);
        l.setSoldAt(LocalDateTime.now());
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 改单个价格 ========
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
            l.setOrderId(null);
            l.setSoldAt(null);
        }
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 待激活 → 重新上架 ========
    @PutMapping("/{id}/relist")
    public Result<Void> relistPending(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if (!"pending_activation".equals(l.getStatus())) return Result.error("只有待激活的可以重新上架");
        // 清除原 order_id/sold_at，重新变成 available
        l.setStatus("available");
        l.setOrderId(null);
        l.setSoldAt(null);
        l.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(l);
        return Result.success(null);
    }

    // ======== 待激活 → 自己激活（加入 user_games 并删除 listing）=======
    @PutMapping("/{id}/self-activate")
    public Result<Void> selfActivate(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if (!"pending_activation".equals(l.getStatus())) return Result.error("只有待激活的可以自己激活");
        // 加入 user_games
        com.steampy.entity.UserGame ug = new com.steampy.entity.UserGame();
        ug.setId(UUID.randomUUID().toString());
        ug.setUserId(l.getSellerId());
        ug.setOrderId(l.getOrderId());
        ug.setGameId(l.getGameId());
        ug.setGameName(l.getGameName());
        ug.setGameImage(l.getGameImage());
        ug.setCdkey(l.getCdkey());
        ug.setVersion(l.getVersion());
        ug.setStatus("activated");
        ug.setSource("cdkey"); // 自己激活也算 cdkey 来源
        ug.setPurchaseDate(java.time.LocalDate.now().toString());
        ug.setActivationDate(java.time.LocalDate.now().toString());
        ug.setCreatedAt(LocalDateTime.now());
        userGameMapper.insert(ug);
        // 删除 listing（这个 CDK 已经被消耗了）
        listingMapper.deleteById(id);
        return Result.success(null);
    }
}

package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Listing;
import com.steampy.entity.User;
import com.steampy.entity.Game;
import com.steampy.mapper.ListingMapper;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.GameMapper;
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

    // 下架 / 删除
    @DeleteMapping("/{id}")
    public Result<Void> deleteListing(@PathVariable String id) {
        Listing l = listingMapper.selectById(id);
        if (l == null) return Result.error("不存在");
        if ("sold".equals(l.getStatus())) return Result.error("已售出不能删除");
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
}

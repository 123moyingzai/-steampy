package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.Order;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private OrderMapper orderMapper;

    // 获取所有游戏（带销量 sales_count）
    @GetMapping
    public Result<List<Map<String, Object>>> getGames(@RequestParam(required = false) Boolean isPresale) {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        if (isPresale != null) {
            qw.eq("is_presale", isPresale);
        }
        qw.orderByAsc("name");
        List<Game> games = gameMapper.selectList(qw);

        // 聚合每个游戏的已完成订单数（销量）
        QueryWrapper<Order> oq = new QueryWrapper<>();
        oq.eq("status", "completed");
        List<Order> allOrders = orderMapper.selectList(oq);
        Map<Long, Long> salesMap = allOrders.stream()
            .filter(o -> o.getGameId() != null)
            .collect(Collectors.groupingBy(Order::getGameId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Game g : games) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("name", g.getName());
            m.put("developer", g.getDeveloper());
            m.put("price", g.getPrice());
            m.put("original_price", g.getOriginalPrice());
            m.put("discount", g.getDiscount());
            m.put("image", g.getImage());
            m.put("description", g.getDescription());
            m.put("release_date", g.getReleaseDate());
            m.put("is_presale", g.getIsPresale());
            m.put("stock", g.getStock());
            m.put("sales_count", salesMap.getOrDefault(g.getId(), 0L));
            result.add(m);
        }
        return Result.success(result);
    }

    // 按 ID 获取游戏
    @GetMapping("/{id}")
    public Result<Game> getGameById(@PathVariable Long id) {
        return Result.success(gameMapper.selectById(id));
    }

    // 获取预售游戏
    @GetMapping("/presale")
    public Result<List<Game>> getPresaleGames() {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.eq("is_presale", true).orderByAsc("name");
        return Result.success(gameMapper.selectList(qw));
    }

    // 获取现货游戏
    @GetMapping("/normal")
    public Result<List<Game>> getNormalGames() {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.eq("is_presale", false).orderByAsc("name");
        return Result.success(gameMapper.selectList(qw));
    }

    // 搜索游戏
    @GetMapping("/search")
    public Result<List<Game>> searchGames(@RequestParam String keyword) {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.like("name", keyword).or().like("developer", keyword);
        return Result.success(gameMapper.selectList(qw));
    }

    // 创建游戏（管理员）
    @PostMapping
    public Result<Game> createGame(@RequestBody Game game) {
        game.setCreatedAt(java.time.LocalDateTime.now());
        game.setUpdatedAt(java.time.LocalDateTime.now());
        gameMapper.insert(game);
        return Result.success(game);
    }

    // 更新游戏
    @PutMapping("/{id}")
    public Result<Game> updateGame(@PathVariable Long id, @RequestBody Game game) {
        game.setId(id);
        game.setUpdatedAt(java.time.LocalDateTime.now());
        gameMapper.updateById(game);
        return Result.success(game);
    }

    // 删除游戏
    @DeleteMapping("/{id}")
    public Result<?> deleteGame(@PathVariable Long id) {
        gameMapper.deleteById(id);
        return Result.success();
    }
}

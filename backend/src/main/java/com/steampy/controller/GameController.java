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

/**
 * GameController
 * 负责游戏商品的 HTTP 接口层，覆盖 C 端查询、搜索、预售/现货过滤，以及管理员增删改。
 *
 * 销量统计：getGames 接口会聚合已完成订单（status=completed）按 gameId 分组计数，作为 sales_count 附加到每个游戏返回。
 *
 * 提供的核心接口：
 *   - GET    /api/games              — 游戏列表（可按 isPresale 过滤，带 sales_count 销量）
 *   - GET    /api/games/{id}         — 游戏详情
 *   - GET    /api/games/presale      — 预售游戏列表
 *   - GET    /api/games/normal       — 现货游戏列表
 *   - GET    /api/games/search?keyword= — 搜索游戏（按 name / developer 模糊匹配）
 *   - POST   /api/games              — 管理员创建游戏
 *   - PUT    /api/games/{id}         — 管理员更新游戏
 *   - DELETE /api/games/{id}         — 管理员删除游戏
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取全部游戏（带实时销量 sales_count）。
     *
     * 可选参数 isPresale=true/false 过滤预售/现货；不传则返回全部。
     * 销量统计基于已完成订单（status=completed）按 gameId 分组聚合。
     * 排序规则：按 name 字母升序。
     *
     * @param isPresale 可选，true 只返回预售、false 只返回现货、null 返回全部
     * @return {@link Result} Map 列表，每项包含 Game 全字段 + sales_count（Long，可能为 0）
     */
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
        Map<String, Long> salesMap = allOrders.stream()
            .filter(o -> o.getGameId() != null)
            .collect(Collectors.groupingBy(Order::getGameId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Game g : games) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("name", g.getName());
            m.put("name_cn", g.getNameCn());
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

    /**
     * 按 ID 获取单个游戏详情。
     *
     * @param id 游戏主键
     * @return {@link Result} Game 实体（不存在时为 null）
     */
    @GetMapping("/{id}")
    public Result<Game> getGameById(@PathVariable String id) {
        return Result.success(gameMapper.selectById(id));
    }

    /**
     * 获取预售游戏列表（is_presale=true）。
     *
     * @return {@link Result} 预售 Game 列表，按 name 升序
     */
    @GetMapping("/presale")
    public Result<List<Game>> getPresaleGames() {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.eq("is_presale", true).orderByAsc("name");
        return Result.success(gameMapper.selectList(qw));
    }

    /**
     * 获取现货游戏列表（is_presale=false）。
     *
     * @return {@link Result} 现货 Game 列表，按 name 升序
     */
    @GetMapping("/normal")
    public Result<List<Game>> getNormalGames() {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.eq("is_presale", false).orderByAsc("name");
        return Result.success(gameMapper.selectList(qw));
    }

    /**
     * 游戏搜索（按 name 或 developer 模糊匹配，OR 关系）。
     *
     * @param keyword 搜索关键词
     * @return {@link Result} 匹配的 Game 列表
     */
    @GetMapping("/search")
    public Result<List<Game>> searchGames(@RequestParam String keyword) {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.like("name", keyword).or().like("developer", keyword);
        return Result.success(gameMapper.selectList(qw));
    }

    /**
     * 创建游戏（管理员）。
     *
     * 自动补 createdAt / updatedAt 为当前时间。
     *
     * @param game Game 请求体
     * @return {@link Result} 插入后的 Game 实体
     */
    @PostMapping
    public Result<Game> createGame(@RequestBody Game game) {
        game.setCreatedAt(java.time.LocalDateTime.now());
        game.setUpdatedAt(java.time.LocalDateTime.now());
        gameMapper.insert(game);
        return Result.success(game);
    }

    /**
     * 更新游戏信息。
     *
     * 按路径 id 覆盖 body.id；自动更新 updatedAt。
     *
     * @param id   游戏主键
     * @param game 更新内容
     * @return {@link Result} 更新后的 Game 实体
     */
    @PutMapping("/{id}")
    public Result<Game> updateGame(@PathVariable String id, @RequestBody Game game) {
        game.setId(id);
        game.setUpdatedAt(java.time.LocalDateTime.now());
        gameMapper.updateById(game);
        return Result.success(game);
    }

    /**
     * 删除游戏（物理删除）。
     *
     * @param id 游戏主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteGame(@PathVariable String id) {
        gameMapper.deleteById(id);
        return Result.success();
    }
}


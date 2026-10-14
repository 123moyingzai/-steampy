package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Favorite;
import com.steampy.entity.Game;
import com.steampy.mapper.FavoriteMapper;
import com.steampy.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FavoriteController
 * 负责用户游戏收藏的 HTTP 接口层，提供收藏/取消/状态查询/我的收藏列表四个能力。
 *
 * 幂等设计：重复收藏同一游戏不会报错，直接返回已存在的那条记录。
 * 收藏列表会批量带出游戏详情（当前价格、折扣、原价、图片、开发商、发行日期），游戏被下架时会标注 "游戏已下架"。
 *
 * 提供的核心接口：
 *   - POST   /api/favorites          — 添加收藏（幂等）
 *   - DELETE /api/favorites/{gameId} — 取消收藏
 *   - GET    /api/favorites/check    — 判断用户是否已收藏某游戏
 *   - GET    /api/favorites/user/{userId} — 查询用户全部收藏（带游戏详情）
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private GameMapper gameMapper;

    /**
     * 添加游戏收藏（幂等）。
     *
     * userId 或 gameId 缺失时返回错误。若已存在同样的 (userId, gameId) 组合则直接返回那条已有记录，不重复插入。
     *
     * @param fav Favorite 请求体，至少要有 userId 和 gameId
     * @return {@link Result} 插入或已有的 Favorite 实体
     */
    @PostMapping
    public Result<Favorite> addFavorite(@RequestBody Favorite fav) {
        if (fav.getUserId() == null || fav.getGameId() == null) {
            return Result.error("参数缺失");
        }
        // 查重
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", fav.getUserId()).eq("game_id", fav.getGameId());
        Favorite exist = favoriteMapper.selectOne(qw);
        if (exist != null) {
            return Result.success(exist); // 已收藏，幂等返回
        }
        fav.setId(null);
        favoriteMapper.insert(fav);
        return Result.success(fav);
    }

    /**
     * 取消游戏收藏。
     *
     * 按 (userId, gameId) 联合条件删除；不存在也不报错（幂等）。
     *
     * @param userId  用户主键
     * @param gameId  游戏主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/{gameId}")
    public Result<Void> removeFavorite(@RequestParam String userId, @PathVariable String gameId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("game_id", gameId);
        favoriteMapper.delete(qw);
        return Result.success(null);
    }

    /**
     * 查询某用户是否已收藏某游戏。
     *
     * @param userId  用户主键
     * @param gameId  游戏主键
     * @return {@link Result} true 已收藏 / false 未收藏
     */
    @GetMapping("/check")
    public Result<Boolean> isFavorited(@RequestParam String userId, @RequestParam String gameId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("game_id", gameId);
        Long cnt = favoriteMapper.selectCount(qw);
        return Result.success(cnt > 0);
    }

    /**
     * 查询用户全部收藏列表（带游戏详情）。
     *
     * 实现：先取 favorites 列表，再批量查出关联的 games（避免 N+1），组装成 Map 返回。
     * 若关联游戏已下架（games 表里不存在），对应 item 的 name 字段会标记为 "游戏已下架"。
     * 返回字段：favorite_id / game_id / favorited_at + 游戏详情（name / name_cn / price / original_price / discount / image / developer / release_date）。
     *
     * @param userId 用户主键
     * @return {@link Result} 收藏列表 Map；空列表时返回 []
     */
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserFavorites(@PathVariable String userId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at");
        List<Favorite> favs = favoriteMapper.selectList(qw);
        if (favs.isEmpty()) return Result.success(new ArrayList<>());

        // 批量查 games
        List<String> gameIds = favs.stream().map(Favorite::getGameId).collect(Collectors.toList());
        QueryWrapper<Game> gqw = new QueryWrapper<>();
        gqw.in("id", gameIds);
        List<Game> games = gameMapper.selectList(gqw);
        Map<String, Game> gameMap = new HashMap<>();
        for (Game g : games) gameMap.put(g.getId(), g);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite f : favs) {
            Game g = gameMap.get(f.getGameId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("favorite_id", f.getId());
            item.put("game_id", f.getGameId());
            item.put("favorited_at", f.getCreatedAt());
            if (g != null) {
                item.put("name", g.getName());
                item.put("name_cn", g.getNameCn());
                item.put("price", g.getPrice());
                item.put("original_price", g.getOriginalPrice());
                item.put("discount", g.getDiscount());
                item.put("image", g.getImage());
                item.put("developer", g.getDeveloper());
                item.put("release_date", g.getReleaseDate());
            } else {
                item.put("name", "游戏已下架");
            }
            result.add(item);
        }
        return Result.success(result);
    }
}



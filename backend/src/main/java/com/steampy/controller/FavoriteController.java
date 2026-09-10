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

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private GameMapper gameMapper;

    // 添加收藏
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

    // 取消收藏
    @DeleteMapping("/{gameId}")
    public Result<Void> removeFavorite(@RequestParam String userId, @PathVariable Long gameId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("game_id", gameId);
        favoriteMapper.delete(qw);
        return Result.success(null);
    }

    // 查用户是否收藏某游戏
    @GetMapping("/check")
    public Result<Boolean> isFavorited(@RequestParam String userId, @RequestParam Long gameId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("game_id", gameId);
        Long cnt = favoriteMapper.selectCount(qw);
        return Result.success(cnt > 0);
    }

    // 查用户所有收藏（带游戏详情：当前价格/折扣/原价）
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserFavorites(@PathVariable String userId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at");
        List<Favorite> favs = favoriteMapper.selectList(qw);
        if (favs.isEmpty()) return Result.success(new ArrayList<>());

        // 批量查 games
        List<Long> gameIds = favs.stream().map(Favorite::getGameId).collect(Collectors.toList());
        QueryWrapper<Game> gqw = new QueryWrapper<>();
        gqw.in("id", gameIds);
        List<Game> games = gameMapper.selectList(gqw);
        Map<Long, Game> gameMap = new HashMap<>();
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
                item.put("image_url", g.getImageUrl());
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

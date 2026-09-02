package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameMapper gameMapper;

    // 获取所有游戏
    @GetMapping
    public Result<List<Game>> getGames(@RequestParam(required = false) Boolean isPresale) {
        QueryWrapper<Game> qw = new QueryWrapper<>();
        if (isPresale != null) {
            qw.eq("is_presale", isPresale);
        }
        qw.orderByAsc("name");
        List<Game> list = gameMapper.selectList(qw);
        return Result.success(list);
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

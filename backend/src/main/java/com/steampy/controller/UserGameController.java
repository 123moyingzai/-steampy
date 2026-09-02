package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.UserGame;
import com.steampy.mapper.UserGameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-games")
public class UserGameController {

    @Autowired
    private UserGameMapper userGameMapper;

    @GetMapping("/user/{userId}")
    public Result<List<UserGame>> getUserGames(@PathVariable String userId) {
        QueryWrapper<UserGame> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("purchase_date");
        return Result.success(userGameMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public Result<UserGame> getById(@PathVariable String id) {
        return Result.success(userGameMapper.selectById(id));
    }

    @PostMapping
    public Result<UserGame> createUserGame(@RequestBody UserGame ug) {
        if (ug.getPurchaseDate() == null) ug.setPurchaseDate(java.time.LocalDateTime.now().toString());
        userGameMapper.insert(ug);
        return Result.success(ug);
    }

    // 激活：待激活 → 已入库
    @PutMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable String id) {
        UserGame ug = userGameMapper.selectById(id);
        if (ug == null) return Result.error("游戏记录不存在");
        ug.setStatus("activated");
        ug.setActivationDate(java.time.LocalDateTime.now().toString());
        userGameMapper.updateById(ug);
        return Result.success(null);
    }

    // 删除游戏库记录
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        userGameMapper.deleteById(id);
        return Result.success(null);
    }
}

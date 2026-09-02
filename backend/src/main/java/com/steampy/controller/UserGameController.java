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

    @PostMapping
    public Result<UserGame> createUserGame(@RequestBody UserGame ug) {
        if (ug.getPurchaseDate() == null) ug.setPurchaseDate(java.time.LocalDateTime.now().toString());
        userGameMapper.insert(ug);
        return Result.success(ug);
    }
}

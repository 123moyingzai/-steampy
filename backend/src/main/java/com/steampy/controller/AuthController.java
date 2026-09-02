package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.User;
import com.steampy.entity.Wallet;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.WalletMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Data
    public static class RegisterReq {
        private String username;
        private String password;
        private String phone;
        private String nickname;
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }

    // 注册
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterReq req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", req.getUsername());
        if (userMapper.selectCount(qw) > 0) {
            return Result.error("用户名已被注册");
        }
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setUsername(req.getUsername());
        u.setPasswordHash(req.getPassword());  // 明文（后续可改 bcrypt）
        u.setPhone(req.getPhone());
        u.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        u.setUserType("普通用户");
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(u);

        // 自动创建钱包
        Wallet w = new Wallet();
        w.setId(UUID.randomUUID().toString());
        w.setUserId(u.getId());
        w.setBalance(java.math.BigDecimal.ZERO);
        w.setFrozenBalance(java.math.BigDecimal.ZERO);
        w.setCreatedAt(LocalDateTime.now());
        w.setUpdatedAt(LocalDateTime.now());
        walletMapper.insert(w);

        u.setPasswordHash(null);
        return Result.success(u);
    }

    // 登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", req.getUsername());
        User u = userMapper.selectOne(qw);
        if (u == null) return Result.error("用户名或密码错误");
        if (!u.getPasswordHash().equals(req.getPassword())) return Result.error("用户名或密码错误");
        if ("已封禁".equals(u.getUserType())) return Result.error("您的账号已被封禁，无法登录");

        Map<String, Object> data = new HashMap<>();
        u.setPasswordHash(null);
        data.put("user", u);
        return Result.success(data);
    }

    // 获取用户
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable String id) {
        User u = userMapper.selectById(id);
        if (u != null) u.setPasswordHash(null);
        return Result.success(u);
    }

    // 更新用户
    @PutMapping("/user/{id}")
    public Result<User> updateUser(@PathVariable String id, @RequestBody User update) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.error("用户不存在");
        if (update.getNickname() != null) u.setNickname(update.getNickname());
        if (update.getPhone() != null) u.setPhone(update.getPhone());
        if (update.getAvatarUrl() != null) u.setAvatarUrl(update.getAvatarUrl());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        u.setPasswordHash(null);
        return Result.success(u);
    }
}

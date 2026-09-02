package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Order;
import com.steampy.entity.User;
import com.steampy.mapper.OrderMapper;
import com.steampy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    // 获取所有用户
    @GetMapping("/users")
    public Result<List<User>> getAllUsers() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        List<User> users = userMapper.selectList(qw);
        users.forEach(u -> u.setPasswordHash(null));
        return Result.success(users);
    }

    // 封禁/解封用户
    @PutMapping("/users/{id}/ban")
    public Result<User> banUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.error("用户不存在");
        String action = body.getOrDefault("action", "ban");
        if ("ban".equals(action)) {
            u.setUserType("已封禁");
        } else if ("unban".equals(action)) {
            u.setUserType("普通用户");
        } else if ("admin".equals(action)) {
            u.setUserType("管理员");
        }
        u.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(u);
        u.setPasswordHash(null);
        return Result.success(u);
    }

    // 管理员删除用户
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable String id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    // 管理员获取全部订单
    @GetMapping("/orders")
    public Result<List<Order>> getAllOrders() {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.orderByDesc("created_at");
        return Result.success(orderMapper.selectList(qw));
    }
}

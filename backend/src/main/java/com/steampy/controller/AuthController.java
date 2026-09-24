package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.User;
import com.steampy.entity.Wallet;
import com.steampy.mapper.UserMapper;
import com.steampy.mapper.WalletMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器 — 密码策略：BCrypt 单向哈希
 *
 * 兼容旧数据：历史 5 条用户密码是明文，未加 BCrypt。
 * 登录时若 BCrypt matches 失败，fallback 明文比对——
 * 匹配成功则立刻升级为 BCrypt hash（lazy migration，用户无感知）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
        private String phone;
        private String password;
    }

    // ========== 密码校验核心方法（含旧明文 lazy 升级）==========

    /**
     * 校验密码。如果是旧明文匹配成功，会自动升级为 BCrypt hash。
     *
     * @return true 密码正确（可能触发了 DB 侧的 hash 升级）
     */
    private boolean verifyPassword(User user, String rawPassword) {
        String storedHash = user.getPasswordHash();
        if (storedHash == null || rawPassword == null) return false;

        // 1) 标准路径：BCrypt matches
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return encoder.matches(rawPassword, storedHash);
        }

        // 2) 旧数据 fallback：明文比对
        if (storedHash.equals(rawPassword)) {
            // 立刻升级为 BCrypt
            String newHash = encoder.encode(rawPassword);
            user.setPasswordHash(newHash);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            return true;
        }

        return false;
    }

    // ========== 注册 ==========
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterReq req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }
        if (req.getPassword().length() < 4) {
            return Result.error("密码不少于 4 位");
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", req.getUsername());
        if (userMapper.selectCount(qw) > 0) {
            return Result.error("用户名已被注册");
        }

        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setUsername(req.getUsername());
        u.setPasswordHash(encoder.encode(req.getPassword()));  // BCrypt 单向哈希
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

        u.setPasswordHash(null);  // 返回前清空敏感字段
        return Result.success(u);
    }

    // ========== 登录（支持用户名或手机号）==========
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        User u;
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            u = userMapper.selectOne(new QueryWrapper<User>().eq("phone", req.getPhone()));
            if (u == null) return Result.error("手机号未注册");
        } else if (req.getUsername() != null && !req.getUsername().isBlank()) {
            u = userMapper.selectOne(new QueryWrapper<User>().eq("username", req.getUsername()));
            if (u == null) return Result.error("用户名或密码错误");
        } else {
            return Result.error("请输入用户名或手机号");
        }

        if (!verifyPassword(u, req.getPassword())) return Result.error("用户名或密码错误");
        if ("已封禁".equals(u.getUserType())) return Result.error("您的账号已被封禁，无法登录");

        Map<String, Object> data = new HashMap<>();
        u.setPasswordHash(null);  // 返回前清空敏感字段
        data.put("user", u);
        return Result.success(data);
    }

    // ========== 获取用户 ==========
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable String id) {
        User u = userMapper.selectById(id);
        if (u != null) u.setPasswordHash(null);
        return Result.success(u);
    }

    // ========== 更新基础资料 ==========
    @PutMapping("/user/{id}")
    public Result<User> updateUser(@PathVariable String id, @RequestBody Map<String, Object> body) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.error("用户不存在");

        Object nick = body.get("nickname");
        Object avatar = body.get("avatar_url");
        Object phone = body.get("phone");
        Object email = body.get("email");

        if (nick != null) u.setNickname(nick.toString());
        if (avatar != null) u.setAvatarUrl(avatar.toString());
        if (phone != null) u.setPhone(phone.toString());
        if (email != null) u.setEmail(email.toString());

        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        u.setPasswordHash(null);
        return Result.success(u);
    }

    // ========== 验证密码（敏感操作二次校验）==========
    @PostMapping("/verify-password")
    public Result<?> verifyPassword(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String username = body.get("username");
        String password = body.get("password");
        if (userId == null || password == null) return Result.error("请填写完整");

        User u = userMapper.selectById(userId);
        if (u == null) return Result.error("用户不存在");
        if (username != null && !username.isBlank() && !u.getUsername().equals(username)) {
            return Result.error("账号不匹配");
        }
        if (!verifyPassword(u, password)) return Result.error("密码错误");
        return Result.success(null);
    }

    // ========== 修改密码 ==========
    @PostMapping("/user/{id}/change-password")
    public Result<?> changePassword(@PathVariable String id,
                                    @RequestBody Map<String, String> body) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.error("用户不存在");

        String oldPwd = body.getOrDefault("old_password", body.get("oldPassword"));
        String newPwd = body.getOrDefault("new_password", body.get("newPassword"));
        if (oldPwd == null || newPwd == null) return Result.error("请填写完整");
        if (newPwd.length() < 4) return Result.error("新密码不少于 4 位");

        if (!verifyPassword(u, oldPwd)) return Result.error("原密码错误");

        u.setPasswordHash(encoder.encode(newPwd));
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return Result.success(null);
    }
}

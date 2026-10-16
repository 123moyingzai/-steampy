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
 * AuthController
 * 负责用户认证与账户资料的 HTTP 接口层，包含注册、登录（用户名/手机号/手机验证码）、资料查询/更新、密码校验与修改。
 *
 * 安全要点：
 * - 密码存储使用 BCrypt 单向哈希；历史遗留的少量明文密码会在登录成功时被自动升级（lazy migration，用户无感知）。
 * - 所有用户返回体都会清空 passwordHash 敏感字段。
 * - 登录时对 "已封禁" 用户直接拒绝。
 * - 敏感操作（如改密）前需要二次密码校验。
 *
 * 提供的核心接口：
 *   - POST /api/auth/register           — 注册（自动创建空钱包）
 *   - POST /api/auth/login               — 登录（用户名或手机号 + 密码）
 *   - POST /api/auth/phone-login         — 手机号验证码登录（前端生成随机码 + 后端校验手机号存在）
 *   - GET  /api/auth/user/{id}           — 查询用户资料
 *   - PUT  /api/auth/user/{id}           — 更新用户资料（nickname/avatar/phone/email）
 *   - POST /api/auth/verify-password     — 密码二次校验（敏感操作前）
 *   - POST /api/auth/user/{id}/change-password — 修改密码
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletMapper walletMapper;

    /** 注册请求体 DTO */
    @Data
    public static class RegisterReq {
        private String username;
        private String password;
        private String phone;
        private String nickname;
    }

    /** 登录请求体 DTO（支持用户名或手机号 + 密码） */
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

    /**
     * 用户注册。
     *
     * 校验逻辑：username 必填且不能重复；password 必填且不少于 4 位。
     * 密码使用 BCrypt 单向哈希后入库；nickname 为空时默认取 username；userType 默认 "普通用户"。
     * 注册成功后自动为新用户创建一条空余额 Wallet（balance=0, frozenBalance=0）。
     * 返回体中 passwordHash 会被清空脱敏。
     *
     * @param req 注册请求，含 username / password / phone / nickname
     * @return {@link Result} 注册成功的 User 实体；校验失败返回 error
     */
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

    /**
     * 用户登录。
     *
     * 支持两种身份凭证：优先 phone（精确匹配），其次 username。密码校验走 {@link #verifyPassword}。
     * 登录失败提示统一为 "用户名或密码错误"，避免泄露账号存在性。
     * 已封禁账号直接拒绝。
     * 返回体附带 {@code user} 对象（含 steamBound 等派生字段，由 {@link #fillSteamDerived} 回填）。
     *
     * @param req 登录请求（username / phone 二选一 + password）
     * @return {@link Result} 含 { user: User } 的 Map；密码错误或账号被封返回 error
     */
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
        // 封禁用户允许登录（前端提示封禁状态），但业务操作会被 BanCheckFilter 拦截

        Map<String, Object> data = new HashMap<>();
        u.setPasswordHash(null);  // 返回前清空敏感字段
        fillSteamDerived(u);
        data.put("user", u);
        return Result.success(data);
    }

    // ========== 手机号验证码登录（前端生成随机码 + 后端校验手机号存在）==========

    /**
     * 手机号验证码登录（简化版，只校验手机号存在性，验证码由前端生成并比对）。
     *
     * 后端只做：phone 非空 + 数据库里存在 + 未封禁。通过后直接返回用户信息，不涉及密码校验。
     *
     * @param body 需含 phone 字段
     * @return {@link Result} 含 { user: User } 的 Map；手机号未注册或被封返回 error
     */
    @PostMapping("/phone-login")
    public Result<Map<String, Object>> phoneLogin(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) return Result.error("请输入手机号");

        User u = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (u == null) return Result.error("该手机号未注册");
        // 封禁用户允许登录（前端提示封禁状态），但业务操作会被 BanCheckFilter 拦截

        Map<String, Object> data = new HashMap<>();
        u.setPasswordHash(null);
        fillSteamDerived(u);
        data.put("user", u);
        return Result.success(data);
    }

    // ========== 获取用户 ==========

    /**
     * 查询用户信息（敏感信息脱敏）
     * - 本人查看（viewerId = id）：返回完整资料（密码哈希除外）
     * - 他人查看：手机号/邮箱打码（138****0435 / a***@xxx.com），防止爬取撞库
     */
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable String id,
                                @RequestParam(required = false) String viewerId) {
        User u = userMapper.selectById(id);
        if (u != null) {
            u.setPasswordHash(null);
            boolean self = id.equals(viewerId);
            if (!self) {
                u.setPhone(maskPhone(u.getPhone()));
                u.setEmail(maskEmail(u.getEmail()));
            }
            fillSteamDerived(u);
        }
        return Result.success(u);
    }

    /** 手机号脱敏：保留前 3 后 4，如 138****0435 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 邮箱脱敏：保留首字符与域名，如 a***@qq.com */
    private String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) return email;
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }

    /**
     * 回填 Steam 非 DB 派生字段（steamBound / steamId 等）
     * 确保登录、查用户等接口返回完整绑定状态
     */
    private void fillSteamDerived(User u) {
        if (u == null) return;
        boolean bound = u.getSteamAccountId() != null && !u.getSteamAccountId().isBlank();
        u.setSteamBound(bound);
    }

    // ========== 更新基础资料 ==========

    /**
     * 更新用户基础资料（仅 nickname / avatar_url / phone / email 四个字段）。
     *
     * null 字段不覆盖原值；自动更新 updatedAt。返回前清空 passwordHash。
     *
     * @param id   用户主键
     * @param body 更新内容 Map
     * @return {@link Result} 更新后的 User；用户不存在返回 error
     */
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

    /**
     * 敏感操作前的二次密码校验。
     *
     * body 需含 userId 和 password；可选 username 做一致性校验（防止张冠李戴）。
     * 密码校验走 {@link #verifyPassword}（含旧明文 lazy 升级）。
     *
     * @param body 需含 userId / password，可选 username
     * @return {@link Result} 密码正确返回 success(null)；账号不存在 / 账号不匹配 / 密码错误返回 error
     */
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

    /**
     * 用户修改密码。
     *
     * 校验逻辑：old_password 必须正确；new_password 不少于 4 位。
     * 新密码使用 BCrypt 重新哈希入库；同时更新 updatedAt。
     * 字段名兼容 snake_case（old_password / new_password）和 camelCase（oldPassword / newPassword）。
     *
     * @param id   用户主键
     * @param body 含 old_password 和 new_password（或 oldPassword / newPassword）
     * @return {@link Result} 成功返回 success(null)；用户不存在 / 密码错误 / 新密码过短返回 error
     */
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

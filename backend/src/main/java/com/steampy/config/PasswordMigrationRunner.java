package com.steampy.config;

import com.steampy.entity.User;
import com.steampy.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时把所有明文密码统一 BCrypt 加密。
 * 核心职责：幂等迁移用户表中的明文密码——只处理不以 $2a$ / $2b$ 开头的密码
 *
 * @author steampy
 * @date 2026-10-14
 */
@Slf4j
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;

    /**
     * 构造注入 UserMapper，并创建 BCryptPasswordEncoder 实例
     *
     * @param userMapper 用户数据访问层
     */
    public PasswordMigrationRunner(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * 应用启动后自动执行的密码加密迁移
     * 遍历所有用户，对非 BCrypt 格式的密码进行加密并回写数据库
     *
     * @param args Spring Boot 传入的命令行参数（未使用）
     */
    @Override
    @Transactional
    public void run(String... args) {
        int migrated = 0, already = 0;
        for (User u : userMapper.selectList(null)) {
            String hash = u.getPasswordHash();
            if (hash == null) continue;
            if (hash.startsWith("$2a$") || hash.startsWith("$2b$")) {
                already++;
            } else {
                u.setPasswordHash(encoder.encode(hash));
                userMapper.updateById(u);
                migrated++;
                log.info("加密用户 {} 的原密码 [{}]", u.getUsername(), hash);
            }
        }
        log.info("密码加密完成: {} 个已加密, {} 个本次迁移", already, migrated);
    }
}

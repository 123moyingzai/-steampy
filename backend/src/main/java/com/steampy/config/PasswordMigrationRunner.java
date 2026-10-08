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
 * 幂等——只处理不以 $2a$/ $2b$ 开头的密码。
 */
@Slf4j
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;

    public PasswordMigrationRunner(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.encoder = new BCryptPasswordEncoder();
    }

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

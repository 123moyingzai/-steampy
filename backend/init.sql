-- ============================================
-- SteamPY 平台数据库初始化脚本
-- MySQL 8.0+
-- ============================================

DROP DATABASE IF EXISTS steampy;
CREATE DATABASE steampy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE steampy;

-- ========== 1. 用户表 ==========
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    nickname VARCHAR(100),
    user_type VARCHAR(20) NOT NULL DEFAULT '普通用户',  -- 普通用户 / 管理员 / 已封禁
    avatar VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB;

-- ========== 2. 游戏表 ==========
CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    discount VARCHAR(50),
    image_url VARCHAR(500),
    image VARCHAR(500),
    description TEXT,
    link VARCHAR(500),
    release_date DATE,
    developer VARCHAR(200),
    is_presale TINYINT(1) DEFAULT 0,
    stock INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_is_presale (is_presale)
) ENGINE=InnoDB;

-- ========== 3. 订单表 ==========
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    game_id BIGINT,
    game_name VARCHAR(200),
    game_image VARCHAR(500),
    price DECIMAL(10, 2),
    quantity INT DEFAULT 1,
    total_price DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'completed',
    order_type VARCHAR(20) DEFAULT 'cdkey',  -- cdkey / gift / balance
    cdkey TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- ========== 4. 用户钱包表 ==========
CREATE TABLE user_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(12, 2) DEFAULT 0,
    frozen_balance DECIMAL(12, 2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ========== 5. 交易记录表 ==========
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,        -- 充值 / 消费 / 退款 / 提现
    title VARCHAR(200),
    amount DECIMAL(12, 2),
    balance_before DECIMAL(12, 2),
    balance_after DECIMAL(12, 2),
    status VARCHAR(20) DEFAULT 'completed',
    reference_type VARCHAR(50),
    reference_id VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_no (transaction_no)
) ENGINE=InnoDB;

-- ========== 6. 用户游戏库 ==========
CREATE TABLE user_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    game_id BIGINT,
    game_name VARCHAR(200),
    game_image VARCHAR(500),
    cdkey TEXT,
    version VARCHAR(50) DEFAULT '标准版',
    status VARCHAR(20) DEFAULT 'pending',   -- pending / activated
    purchase_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    activation_date DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB;

-- ========== 7. 公告表 ==========
CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    publish_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_top TINYINT(1) DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    INDEX idx_active (is_active)
) ENGINE=InnoDB;

-- ========== 8. 卖家额度表 ==========
CREATE TABLE seller_quota (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL UNIQUE,
    quota DECIMAL(12, 2) DEFAULT 0,
    used DECIMAL(12, 2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ========== 初始数据 ==========

-- 管理员账号（密码: admin123，明文，bcrypt 加密后存储）
INSERT INTO users (username, password_hash, nickname, user_type) VALUES
('admin', 'admin123', '超级管理员', '管理员'),
('testuser', '123456', '测试用户', '普通用户');

-- 初始钱包
INSERT INTO user_wallets (user_id, balance) VALUES
(1, 9999.00),
(2, 100.00);

-- 全部游戏（从 cdk_games.json 迁移，共 12 个）
INSERT INTO games (name, price, original_price, discount, image_url, image, description, developer, is_presale, stock) VALUES
('生化危机:安魂曲', 309.00, 348.00, '-11%', '../picture/安魂曲.jpg', '../picture/安魂曲.jpg', '一款史诗级的动作冒险游戏，带你进入一个充满神秘和危险的世界。玩家将扮演主角在末日世界中生存，解开各种谜题，与丧尸战斗。719cjx', 'Epic Games', 1, 50),
('生化危机:安魂曲 豪华版', 354.00, 398.00, '-11%', '../picture/安魂曲.jpg', '../picture/安魂曲.jpg', '豪华版包含游戏本体、季票、独家皮肤包和数字原声带。最完整的安魂曲游戏体验。719cjx', 'Epic Games', 1, 50),
('Fullbright Pres', 6.00, 26.00, '-77%', '../picture/Fullbright Pres.jpg', '../picture/Fullbright Pres.jpg', '一款独特的探索冒险游戏，玩家需要在神秘的空间站中探索，解开隐藏在环境中的故事。719cjx', 'Fullbright', 0, 200),
('你的另一个老婆', 14.70, 18.00, '-18%', '../picture/你的另一个老婆.jpg', '../picture/你的另一个老婆.jpg', '轻松有趣的恋爱模拟游戏，体验与众不同的恋爱故事。719cjx', '独立游戏工作室', 0, 200),
('东方奇缘记', 2.50, 18.00, '-86%', '../picture/东方奇缘记.jpg', '../picture/东方奇缘记.jpg', '东方Project同人游戏，结合了弹幕射击和RPG元素。719cjx', '东方同人社团', 0, 200),
('银河守卫战', 18.90, 41.00, '-54%', '../picture/银河守卫战.jpg', '../picture/银河守卫战.jpg', '太空策略射击游戏，保卫银河系免受外星入侵。719cjx', 'Galaxy Studio', 0, 200),
('我与她们的大学画像', 16.55, 20.00, '-17%', '../picture/我与她们的大学画像.jpg', '../picture/我与她们的大学画像.jpg', '校园恋爱视觉小说，体验大学生活中的甜蜜与感动。719cjx', '校园游戏社', 0, 200),
('三更', 4.38, 6.00, '-27%', '../picture/三更.jpg', '../picture/三更.jpg', '恐怖解谜游戏，在深夜探索诡异的老宅，揭开尘封的秘密。719cjx', 'Night Studio', 0, 200),
('神经鹅', 22.98, 28.00, '-18%', '../picture/神经鹅.jpg', '../picture/神经鹅.jpg', '搞怪模拟游戏，扮演一只制造混乱的大鹅。719cjx', 'House House', 0, 200),
('RollBot', 18.00, 33.00, '-45%', '../picture/RollBot.jpg', '../picture/RollBot.jpg', '机器人滚球益智游戏，控制机器人通过各种复杂关卡。719cjx', 'Indie Dev', 0, 200),
('剑与魔法的女主角们2', 34.00, 42.00, '-19%', '../picture/剑与魔法的女主角们2.jpg', '../picture/剑与魔法的女主角们2.jpg', '日系RPG游戏，与众多女主角一起展开冒险。719cjx', 'JRPG Studio', 0, 200),
('高球王者', 12.90, 39.00, '-67%', '../picture/高球王者.jpg', '../picture/高球王者.jpg', '高尔夫模拟游戏，体验真实的高尔夫球场和竞技。719cjx', 'Sports Games Inc', 0, 200);

-- 示例公告
INSERT INTO announcements (title, content, is_top, is_active) VALUES
('欢迎使用 SteamPY 平台', 'SteamPY 是一个安全可靠的 Steam 游戏交易平台，支持礼物代购、余额购、CDKey 激活等多种购买方式。', 1, 1),
('平台使用说明', '注册账号 → 浏览游戏 → 选择购买方式 → 完成支付 → 领取游戏。如有问题请联系官方QQ群：807662430', 0, 1);

SELECT '✅ 数据库初始化完成！' AS result;

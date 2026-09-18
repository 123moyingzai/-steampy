-- Table structure for table `announcement_reads`
--

CREATE TABLE `announcement_reads` (
  `announcement_id` bigint NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `read_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`announcement_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `announcements`
--

CREATE TABLE `announcements` (
  `id` int NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `publish_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `is_top` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `favorites`
--

CREATE TABLE `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game` (`user_id`,`game_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `games`
--

CREATE TABLE `games` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_cn` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `discount` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `release_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `developer` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_presale` tinyint(1) DEFAULT '0',
  `stock` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=165 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `listings`
--

CREATE TABLE `listings` (
  `id` varchar(36) NOT NULL,
  `seller_id` varchar(36) NOT NULL,
  `game_id` int NOT NULL,
  `game_name` varchar(255) NOT NULL,
  `game_image` varchar(500) DEFAULT '',
  `version` varchar(50) DEFAULT '标准版',
  `cdkey` varchar(200) DEFAULT '' COMMENT 'CDKey (cdkey类型必填, py类型为空)',
  `price` decimal(12,2) NOT NULL,
  `original_price` decimal(12,2) DEFAULT '0.00',
  `region` varchar(50) DEFAULT '国区',
  `status` varchar(20) DEFAULT 'available',
  `order_id` varchar(36) DEFAULT NULL,
  `sold_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` varchar(10) DEFAULT 'cdkey' COMMENT 'cdkey=玩家挂 CDK, py=PY代购额度',
  `quota` decimal(12,2) DEFAULT NULL COMMENT '代购可用额度 (仅 py 类型)',
  `auto_deliver` tinyint(1) DEFAULT '0' COMMENT '是否自动发货 (仅 py)',
  PRIMARY KEY (`id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_game` (`game_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyer_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seller_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `listing_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_id` int DEFAULT NULL,
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `quantity` int DEFAULT '1',
  `total_price` decimal(10,2) DEFAULT NULL,
  `delivery_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cdkey` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `order_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paid_at` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_buyer` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `id` varchar(36) NOT NULL,
  `target_type` varchar(20) NOT NULL COMMENT 'review/reply',
  `target_id` varchar(36) NOT NULL,
  `reporter_id` varchar(36) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_reporter` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `review_likes`
--

CREATE TABLE `review_likes` (
  `review_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `review_replies`
--

CREATE TABLE `review_replies` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `review_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_reply_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reply_to_user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `likes_count` int NOT NULL DEFAULT '0',
  `report_count` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `review_reply_likes`
--

CREATE TABLE `review_reply_likes` (
  `reply_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`reply_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
  `id` varchar(36) NOT NULL,
  `game_id` int NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `recommend` tinyint NOT NULL COMMENT '1推荐 0不推荐',
  `content` text NOT NULL,
  `images` mediumtext COMMENT '逗号分隔的图片 URL 或 base64 列表',
  `status` tinyint DEFAULT '0' COMMENT '0待审核 1已通过 2被拒',
  `likes_count` int NOT NULL DEFAULT '0',
  `replies_count` int NOT NULL DEFAULT '0',
  `report_count` int NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game` (`user_id`,`game_id`),
  KEY `idx_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `seller_quota`
--

CREATE TABLE `seller_quota` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `seller_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quota` decimal(12,2) DEFAULT '0.00',
  `used` decimal(12,2) DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `steam_accounts`
--

CREATE TABLE `steam_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `steam_id64` varchar(64) NOT NULL COMMENT 'Steam ID64',
  `steam_name` varchar(100) NOT NULL COMMENT 'Steam昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像',
  `region` varchar(50) DEFAULT NULL COMMENT '地区',
  `level` int DEFAULT '1' COMMENT 'Steam等级',
  `bind_user_ids` text COMMENT '曾绑定过的 userId 列表（逗号分隔）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `account_hash` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `steam_id64` (`steam_id64`),
  KEY `idx_steam_id64` (`steam_id64`),
  KEY `idx_account_hash` (`account_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `steam_libraries`
--

CREATE TABLE `steam_libraries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_id` bigint DEFAULT NULL,
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `playtime` int DEFAULT '0',
  `owned_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `steam_account_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_game` (`user_id`,`game_name`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `subtitle` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` decimal(12,2) DEFAULT NULL,
  `balance_before` decimal(12,2) DEFAULT NULL,
  `balance_after` decimal(12,2) DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'completed',
  `reference_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reference_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_no` (`transaction_no`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `user_games`
--

CREATE TABLE `user_games` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_id` int DEFAULT NULL,
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cdkey` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `purchase_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'store' COMMENT 'store/py代购, cdkey/玩家购, official/官方购',
  `activation_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `user_notifications`
--

CREATE TABLE `user_notifications` (
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `type` varchar(32) NOT NULL COMMENT 'reply|like_review|like_reply',
  `actor_id` varchar(36) DEFAULT NULL,
  `actor_name` varchar(100) DEFAULT NULL,
  `target_type` varchar(32) DEFAULT NULL,
  `target_id` varchar(36) DEFAULT NULL,
  `game_id` varchar(36) DEFAULT NULL,
  `content_snippet` varchar(200) DEFAULT NULL,
  `target_content` varchar(500) DEFAULT NULL,
  `is_read` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `user_wallets`
--

CREATE TABLE `user_wallets` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `balance` decimal(12,2) DEFAULT '0.00',
  `frozen_balance` decimal(12,2) DEFAULT '0.00',
  `total_recharged` decimal(12,2) DEFAULT '0.00',
  `total_spent` decimal(12,2) DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '普通用户',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `steam_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam ID64',
  `steam_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam昵称',
  `steam_avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam头像',
  `steam_region` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam地区',
  `steam_level` int DEFAULT NULL COMMENT 'Steam等级',
  `steam_game_count` int DEFAULT NULL COMMENT '游戏数量',
  `steam_account_value` decimal(12,2) DEFAULT NULL COMMENT '账号价值',
  `steam_playtime` int DEFAULT NULL COMMENT '游戏时长(小时)',
  `steam_bound` tinyint(1) DEFAULT '0' COMMENT '是否已绑定Steam',
  `steam_bound_at` datetime DEFAULT NULL COMMENT '绑定时间',
  `steam_account_id` bigint DEFAULT NULL,
  `account_hash` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `withdraw_records`
--

CREATE TABLE `withdraw_records` (
  `id` varchar(36) NOT NULL,
  `order_no` varchar(32) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `pay_method` varchar(20) NOT NULL,
  `account` varchar(128) NOT NULL,
  `real_name` varchar(64) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `fee` decimal(12,2) NOT NULL,
  `net_amount` decimal(12,2) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'success',
  `applied_at` datetime NOT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `review_remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;





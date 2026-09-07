-- ============================================================
-- 追加 30 款热门 Steam 游戏（现有 50 → 80）
-- 图片统一用 Steam 官方 CDN: cdn.akamai.steamstatic.com/steam/apps/{appid}/header.jpg
-- ============================================================

INSERT INTO games (id, name, developer, price, original_price, discount, image, description, release_date, is_presale, stock, created_at, updated_at) VALUES
(58, 'Elden Ring', 'BANDAI NAMCO', 198.00, 298.00, '-34%', 'https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg', '艾尔登法环是由宫崎英高制作、乔治·R·R·马丁负责世界观创作的开放世界魂类RPG。探索广阔的交界地，成为艾尔登之王。', '2022-02-25', 0, 50, NOW(), NOW()),
(59, 'Red Dead Redemption 2', 'Rockstar Games', 189.00, 299.00, '-37%', 'https://cdn.akamai.steamstatic.com/steam/apps/1174180/header.jpg', '美国西部的史诗叙事，亚瑟·摩根和范德林德帮的亡命生涯，沉浸在开放世界的辉煌与残酷之中。', '2019-12-06', 0, 40, NOW(), NOW()),
(60, 'The Witcher 3: Wild Hunt', 'CD PROJEKT RED', 139.00, 258.00, '-46%', 'https://cdn.akamai.steamstatic.com/steam/apps/292030/header.jpg', '猎魔人杰洛特的传奇旅程。昆特牌、狩魔委托、分支剧情，300+小时的开放世界体验。', '2015-05-19', 0, 60, NOW(), NOW()),
(61, 'Grand Theft Auto V', 'Rockstar Games', 129.00, 199.00, '-35%', 'https://cdn.akamai.steamstatic.com/steam/apps/271590/header.jpg', '洛圣都五城记，三主角切换，开放世界犯罪模拟。GTA Online 持续更新。', '2015-04-13', 0, 80, NOW(), NOW()),
(62, 'Elden Ring: Shadow of the Erdtree', 'BANDAI NAMCO', 228.00, 298.00, '-23%', 'https://cdn.akamai.steamstatic.com/steam/apps/2778580/header.jpg', '艾尔登法环黄金树幽影 DLC，全新地图暗影树影之地，米凯拉的故事。', '2024-06-21', 0, 30, NOW(), NOW()),
(63, 'Counter-Strike 2', 'Valve', 0.00, 0.00, '免费', 'https://cdn.akamai.steamstatic.com/steam/apps/730/header.jpg', 'Valve 最新一代 CS，采用 Source 2 引擎，全新的反作弊系统和视觉升级。', '2023-09-27', 0, 999, NOW(), NOW()),
(64, 'Hollow Knight: Silksong', 'Team Cherry', 68.00, 98.00, '-30%', 'https://cdn.akamai.steamstatic.com/steam/apps/1030300/header.jpg', '空洞骑士丝之歌，全新的王国，控制黄蜂女霍恩特，挑战新的敌人和 BOSS。', '2025-06-04', 0, 45, NOW(), NOW()),
(65, 'Black Myth: Wukong', 'Game Science', 268.00, 298.00, '-10%', 'https://cdn.akamai.steamstatic.com/steam/apps/2358720/header.jpg', '黑神话悟空，国产单机动作角色扮演游戏，以西游记为背景的第三部神话作品。', '2024-08-20', 0, 100, NOW(), NOW()),
(66, 'Stardew Valley', 'ConcernedApe', 48.00, 76.00, '-37%', 'https://cdn.akamai.steamstatic.com/steam/apps/413150/header.jpg', '星露谷物语，逃离都市继承农场，种植、钓鱼、采矿、交友，像素牧场模拟巅峰之作。', '2016-02-26', 0, 200, NOW(), NOW()),
(67, 'Terraria', 'Re-Logic', 36.00, 48.00, '-25%', 'https://cdn.akamai.steamstatic.com/steam/apps/105600/header.jpg', '泰拉瑞亚，2D 沙盒冒险，你能挖掘、建造、探索、战斗。无尽的可能性。', '2011-05-16', 0, 150, NOW(), NOW()),
(68, 'Don\'t Starve Together', 'Klei Entertainment', 48.00, 68.00, '-29%', 'https://cdn.akamai.steamstatic.com/steam/apps/322330/header.jpg', '饥荒联机版，威尔逊和他的朋友们在诡异的黑暗世界中挣扎求生。', '2016-04-21', 0, 100, NOW(), NOW()),
(69, 'Fallout 4', 'Bethesda Game Studios', 99.00, 199.00, '-50%', 'https://cdn.akamai.steamstatic.com/steam/apps/374320/header.jpg', '辐射4，末日后的波士顿废土，你是唯一一个幸存的避难所居民，寻找失踪的儿子。', '2015-11-10', 0, 55, NOW(), NOW()),
(70, 'Doom Eternal', 'id Software', 149.00, 199.00, '-25%', 'https://cdn.akamai.steamstatic.com/steam/apps/782330/header.jpg', '毁灭永恒，恶魔猎人再次归来，这次战场扩大到地球、火星和地狱。', '2020-03-20', 0, 40, NOW(), NOW()),
(71, 'Control', 'Remedy Entertainment', 158.00, 233.00, '-32%', 'https://cdn.akamai.steamstatic.com/steam/apps/870780/header.jpg', '控制，杰西·法登成为联邦控制局新任局长，调查超自然事件。', '2019-08-27', 0, 35, NOW(), NOW()),
(72, 'Death Stranding', 'Kojima Productions', 198.00, 298.00, '-34%', 'https://cdn.akamai.steamstatic.com/steam/apps/1192650/header.jpg', '死亡搁浅，小岛秀夫的全新 IP，山姆穿越后末日美国，连接分裂的文明。', '2020-07-14', 0, 38, NOW(), NOW()),
(73, 'Assassin\'s Creed Valhalla', 'Ubisoft', 148.00, 298.00, '-50%', 'https://cdn.akamai.steamstatic.com/steam/apps/2208920/header.jpg', '刺客信条瓦尔哈拉，维京战士艾沃尔征服英格兰，2022 年最大刺客信条开放世界。', '2020-11-10', 0, 60, NOW(), NOW()),
(74, 'Cyberpunk 2077: Ultimate Edition', 'CD PROJEKT RED', 248.00, 398.00, '-38%', 'https://cdn.akamai.steamstatic.com/steam/apps/1852140/header.jpg', '赛博朋克2077终极版，包含本体和往日之影 DLC，夜之城完整体验。', '2023-12-05', 0, 42, NOW(), NOW()),
(75, 'Starfield', 'Bethesda Game Studios', 199.00, 299.00, '-33%', 'https://cdn.akamai.steamstatic.com/steam/apps/1716740/header.jpg', '星空，探索浩瀚宇宙，建造飞船，在 1000+ 星球上书写你的传奇。', '2023-09-06', 0, 48, NOW(), NOW()),
(76, 'Lies of P', 'NEOWIZ', 198.00, 298.00, '-34%', 'https://cdn.akamai.steamstatic.com/steam/apps/1627720/header.jpg', '匹诺曹的谎言，魂类游戏，你是被制造的木偶，寻找人类的道路。', '2023-09-19', 0, 33, NOW(), NOW()),
(77, 'Wo Long: Fallen Dynasty', 'KOEI TECMO', 188.00, 298.00, '-37%', 'https://cdn.akamai.steamstatic.com/steam/apps/1904580/header.jpg', '卧龙苍天陨落，三国魂类动作游戏，忍龙组开发，以邪术乱世为背景。', '2023-03-03', 0, 36, NOW(), NOW()),
(78, 'Dome Keeper', 'Bippinbits', 68.00, 96.00, '-29%', 'https://cdn.akamai.steamstatic.com/steam/apps/1632350/header.jpg', '穹顶守护者，塔防+Roguelike 采矿，保护你的穹顶。', '2022-09-06', 0, 80, NOW(), NOW()),
(79, 'RimWorld', 'Ludeon Studios', 108.00, 149.00, '-28%', 'https://cdn.akamai.steamstatic.com/steam/apps/294100/header.jpg', '环世界，殖民地管理模拟，管理一群殖民者在外星球建立家园。', '2018-10-17', 0, 52, NOW(), NOW()),
(80, 'Factorio', 'Wube Software', 138.00, 199.00, '-31%', 'https://cdn.akamai.steamstatic.com/steam/apps/427520/header.jpg', '异星工厂，自动化建造，你将被游戏循环吞噬，天亮才发现。', '2020-08-14', 0, 46, NOW(), NOW()),
(81, 'Hades II', 'Supergiant Games', 158.00, 199.00, '-21%', 'https://cdn.akamai.steamstatic.com/steam/apps/1145360/header.jpg', '哈迪斯2，超巨人工作室 roguelike 续作，控制扎格瑞的妹妹墨利诺厄对抗时间泰坦。', '2024-05-06', 0, 54, NOW(), NOW()),
(82, 'Marvel\'s Spider-Man 2', 'Insomniac Games', 248.00, 299.00, '-17%', 'https://cdn.akamai.steamstatic.com/steam/apps/2909160/header.jpg', '漫威蜘蛛侠2，彼得·帕克和迈尔斯·莫拉莱斯双主角，毒液登场。', '2023-10-20', 0, 44, NOW(), NOW()),
(83, 'Metaphor: ReFantazio', 'ATLUS', 268.00, 298.00, '-10%', 'https://cdn.akamai.steamstatic.com/steam/apps/1947890/header.jpg', '暗喻幻想，女神异闻录团队全新 IP，宏大的幻想世界观。', '2024-10-11', 0, 32, NOW(), NOW()),
(84, 'Like a Dragon: Infinite Wealth', 'Ryu Ga Gotoku Studio', 198.00, 298.00, '-34%', 'https://cdn.akamai.steamstatic.com/steam/apps/2256310/header.jpg', '如龙无限财富，春日一番和桐生一马双主角，夏威夷+日本双地图。', '2024-01-26', 0, 30, NOW(), NOW()),
(85, 'Nintendo eShop Gift Card', 'Nintendo', 100.00, 100.00, '0%', 'https://cdn.akamai.steamstatic.com/steam/apps/1493710/header.jpg', '任天堂 eShop 充值卡，可以为 Nintendo Switch 充值余额。', '2020-01-01', 0, 999, NOW(), NOW()),
(86, 'PlayStation Store Gift Card', 'Sony Interactive Entertainment', 100.00, 100.00, '0%', 'https://cdn.akamai.steamstatic.com/steam/apps/1615860/header.jpg', 'PlayStation Store 充值卡，可以为 PS5/PS4 充值余额。', '2020-01-01', 0, 999, NOW(), NOW()),
(87, 'Xbox Gift Card', 'Microsoft', 100.00, 100.00, '0%', 'https://cdn.akamai.steamstatic.com/steam/apps/1679770/header.jpg', 'Xbox 充值卡，可以为 Xbox Series X/S / Xbox One 充值余额。', '2020-01-01', 0, 999, NOW(), NOW());

-- ============================================================
-- 修复重复 + 图片/名称错配
-- ============================================================

-- ① 删除重复游戏（保留 seed_v1 旧版本，删 seed_v2 新加的重复项）
DELETE FROM games WHERE id IN (58, 59, 60, 61, 65, 69, 70, 74);

-- ② 删除图片/appid 错配严重的（RE Village/Horizon/充值卡/HadesII 冲突）
DELETE FROM games WHERE id IN (28, 44, 81, 85, 86, 87);

-- ③ 用正确的 Steam appid 重新 INSERT 回去
INSERT INTO games (name, developer, price, original_price, discount, image, description, release_date, is_presale, stock) VALUES

('Resident Evil Village', 'CAPCOM', 148.00, 228.00, '-35%',
 'https://cdn.akamai.steamstatic.com/steam/apps/1196590/header.jpg',
 '生化危机8：村庄，伊森·温特斯在神秘村庄寻找被绑架的女儿，恐怖求生冒险。', '2021-05-07', 0, 50),

('Horizon Forbidden West Complete Edition', 'Guerrilla Games', 178.00, 298.00, '-40%',
 'https://cdn.akamai.steamstatic.com/steam/apps/2420110/header.jpg',
 '地平线：西部禁域完整版，包含 Burning Shores 资料片，艾洛伊的冒险继续。', '2024-03-21', 0, 40),

('Dead Cells', 'Motion Twin', 88.00, 169.00, '-48%',
 'https://cdn.akamai.steamstatic.com/steam/apps/588650/header.jpg',
 '死亡细胞，roguelike 动作游戏，在不断变化的城堡中探索。', '2018-08-07', 0, 60),

('Counter-Strike 2 Prime Status Upgrade', 'Valve', 108.00, 108.00, '0%',
 'https://cdn.akamai.steamstatic.com/steam/apps/730/header.jpg',
 'CS2 Prime 状态升级，获得官方匹配资格、掉落物品和成就。', '2023-09-27', 0, 999),

('Team Fortress 2 Premium', 'Valve', 0.00, 0.00, '免费',
 'https://cdn.akamai.steamstatic.com/steam/apps/440/header.jpg',
 '军团要塞2，经典团队射击游戏，免费游玩。', '2007-10-10', 0, 999),

('Portal 2', 'Valve', 39.00, 99.00, '-61%',
 'https://cdn.akamai.steamstatic.com/steam/apps/620/header.jpg',
 '传送门2，经典解谜益智游戏，支持双人合作。', '2011-04-18', 0, 100);

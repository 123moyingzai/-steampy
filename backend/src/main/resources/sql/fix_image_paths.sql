-- ============================================================
-- 图片路径修复脚本（SteamPY）
-- 适用：orders / listings / games 三张表的 game_image 字段
-- 执行：mysql -u root -p --default-character-set=utf8mb4 steampy < fix_image_paths.sql
--
-- 背景：早期用命令行直接拼接中文字面量执行 UPDATE，中文字符被客户端
--       转成 ASCII 问号(0x3F)落库，导致 ../picture/games/神经鹅.jpg
--       变成 ../picture/games/???.jpg，图片永久 404。
--       本脚本全部通过 JOIN 从 games 表回填，不出现任何中文字面量，
--       因此可以在任意终端编码环境下安全执行。
-- ============================================================

-- ------------------------------------------------------------
-- 0. 执行前检查：看看有多少条被污染
-- ------------------------------------------------------------
SELECT 'orders' AS tbl, COUNT(*) AS corrupted FROM orders   WHERE game_image LIKE '%?%'
UNION ALL
SELECT 'listings',      COUNT(*) FROM listings WHERE game_image LIKE '%?%'
UNION ALL
SELECT 'games',         COUNT(*) FROM games    WHERE image      LIKE '%?%';

-- ------------------------------------------------------------
-- 1. 修复被污染成 '?' 的路径
--    优先按游戏名匹配（同一游戏名），再按 game_id 兜底匹配
-- ------------------------------------------------------------
UPDATE orders o
JOIN games g ON o.game_name = g.name
SET o.game_image = g.image
WHERE o.game_image LIKE '%?%' AND g.image IS NOT NULL AND g.image <> '';

UPDATE orders o
JOIN games g ON CAST(o.game_id AS UNSIGNED) = g.id
SET o.game_image = g.image
WHERE o.game_image LIKE '%?%' AND g.image IS NOT NULL AND g.image <> '';

UPDATE listings l
JOIN games g ON l.game_name = g.name
SET l.game_image = g.image
WHERE l.game_image LIKE '%?%' AND g.image IS NOT NULL AND g.image <> '';

UPDATE listings l
JOIN games g ON CAST(l.game_id AS UNSIGNED) = g.id
SET l.game_image = g.image
WHERE l.game_image LIKE '%?%' AND g.image IS NOT NULL AND g.image <> '';

-- ------------------------------------------------------------
-- 2. 修正 games 表里填错的 Steam AppID（原地址 404）
--    只按主键 id 更新，不依赖中文，编码安全
-- ------------------------------------------------------------
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/601150/header.jpg'  WHERE id = 32;   -- DMC5
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/480490/header.jpg'  WHERE id = 43;   -- Prey
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/1601580/header.jpg' WHERE id = 49;   -- Frostpunk 2
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/2651280/header.jpg' WHERE id = 82;   -- Spider-Man 2
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/2072450/header.jpg' WHERE id = 84;   -- Yakuza: Infinite Wealth
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/2375550/header.jpg' WHERE id = 110;  -- Yakuza Gaiden
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/954850/header.jpg'  WHERE id = 122;  -- KSP2
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/2239550/header.jpg' WHERE id = 135;  -- Watch Dogs Legion
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/2369390/header.jpg' WHERE id = 138;  -- Far Cry 6
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/627270/header.jpg'  WHERE id = 147;  -- Injustice 2
UPDATE games SET image = 'https://cdn.akamai.steamstatic.com/steam/apps/2221490/header.jpg' WHERE id = 154;  -- The Division 2

-- 同步已经下单的历史快照（orders / listings 是冗余快照，不会自动跟着 games 变）
UPDATE orders o   JOIN games g ON CAST(o.game_id AS UNSIGNED) = g.id
SET o.game_image = g.image
WHERE o.game_image LIKE 'http%' AND g.image LIKE 'http%' AND o.game_image <> g.image;

UPDATE listings l JOIN games g ON CAST(l.game_id AS UNSIGNED) = g.id
SET l.game_image = g.image
WHERE l.game_image LIKE 'http%' AND g.image LIKE 'http%' AND l.game_image <> g.image;

-- ------------------------------------------------------------
-- 3. 执行后校验：以下三项应全部为 0 / 无行
-- ------------------------------------------------------------
SELECT 'orders' AS tbl, COUNT(*) AS remaining_bad FROM orders
  WHERE game_image LIKE '%?%' OR game_image IS NULL OR game_image = ''
UNION ALL
SELECT 'listings',       COUNT(*) FROM listings
  WHERE game_image LIKE '%?%' OR game_image IS NULL OR game_image = ''
UNION ALL
SELECT 'games',          COUNT(*) FROM games
  WHERE image LIKE '%?%' OR image IS NULL OR image = '';

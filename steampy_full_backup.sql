-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: steampy
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `steampy`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `steampy` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `steampy`;

--
-- Table structure for table `announcement_reads`
--

DROP TABLE IF EXISTS `announcement_reads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement_reads` (
  `announcement_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → announcements.id',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`announcement_id`,`user_id`),
  CONSTRAINT `fk_reads_announcement` FOREIGN KEY (`announcement_id`) REFERENCES `announcements` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement_reads`
--

LOCK TABLES `announcement_reads` WRITE;
/*!40000 ALTER TABLE `announcement_reads` DISABLE KEYS */;
INSERT INTO `announcement_reads` VALUES ('1','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-09 11:22:18'),('1','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-09 10:27:05'),('1','b29de658-9260-44a6-91db-3733d9033965','2026-09-15 09:32:47'),('2','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-09 11:22:18'),('2','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-09 10:27:05'),('2','b29de658-9260-44a6-91db-3733d9033965','2026-09-15 09:32:47'),('3','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-09 11:22:18'),('3','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-09 10:27:05'),('3','b29de658-9260-44a6-91db-3733d9033965','2026-09-15 09:32:47'),('4','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-09 11:22:18'),('4','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-09 10:27:05'),('4','b29de658-9260-44a6-91db-3733d9033965','2026-09-15 09:32:47'),('5','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-09 11:22:18'),('5','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-09 10:27:05'),('5','b29de658-9260-44a6-91db-3733d9033965','2026-09-15 09:32:47'),('89ccb09df2b6450fd31175ab4418b6e2','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-10-26 12:45:14'),('89ccb09df2b6450fd31175ab4418b6e2','41773925-6164-4e8c-9dc6-be142038cf3f','2026-10-27 11:07:25'),('f99791c33e6783b649b84b6a30e9c603','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-10-28 11:32:19');
/*!40000 ALTER TABLE `announcement_reads` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '公告正文（可空，只有标题也行）',
  `publish_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '⚠️ 历史 varchar(50)，后续应改为 datetime',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活: 1=显示, 0=下架（软删除）',
  `is_top` tinyint(1) DEFAULT '0' COMMENT '是否置顶: 1=置顶',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES ('1','系统维护通知','预计今晚20:00-22:00进行升级。','2025-12-10',1,1,'2026-05-21 14:05:45'),('2','新游戏《艾尔登法环》DLC现已上架','限时优惠中。','2025-12-09',1,0,'2026-05-21 14:05:45'),('3','圣诞节特惠活动即将开启','敬请期待。','2025-12-08',1,0,'2026-05-21 14:05:45'),('4','用户安全提醒','请勿共享账号密码。','2025-12-07',1,0,'2026-05-21 14:05:45'),('5','新增支付方式','支持支付宝、微信支付。','2025-12-06',1,0,'2026-05-21 14:05:45'),('89ccb09df2b6450fd31175ab4418b6e2','公告 发布','修复已知问题 ','2026-10-26',1,1,NULL),('f99791c33e6783b649b84b6a30e9c603','12345678','第409章 特勤队员介绍，人格投影覆盖\n（新故乡，射命丸文的寂静的故乡线，接前面庇护所妖梦的剧情，新故乡本地人之后就用全名，主世界用名字）\n\n碰！\n\n碰碰！\n\n寂静的山林被突兀的噪响唤醒。那声音在树木之间来回碰撞，惊起了几只栖息在枝头的飞鸟，也惊动了游荡在原野上的面目破碎的无心智怪物。它们歪斜着脑袋，浑浊的眼珠里映不出任何神采，却唯独对声音有着本能的追逐。一个接一个，那些曾经是人的东西拖着残破的躯体，踩着杂草与碎石，向着声音的来源循声奔去。\n\n磅磅——！\n\n“呃啊啊啊啊啊！”\n\n火药爆发推动着弹头，将扑击过来的怪奇尸体撕裂击倒。多枚弹丸的同时喷射所携带的巨大动能，让两人无惧于这些在山间零散游荡的尸体们的侵扰。冲在最前面的那一具被正面命中胸口，整个上半身几乎炸开，剩余的部分被冲击力掀翻在地，滚了两圈后便不再动弹。紧随其后的几具也被后续的弹丸依次点名，一个接一个地栽倒在草丛中，发出沉闷的噗通声。\n\n身穿护甲、佩戴头盔，有着一条蓬松的白色大尾巴的少女是犬走椛，一位曾是妖怪之山天狗聚落一员的白狼天狗。护甲在阳光下泛着暗哑的金属光泽，头盔两侧特意为她的耳朵留出了活动空间。那条白色的大尾巴在身后轻轻摆动，既是平衡器，也是她情绪的晴雨表——此刻，尾巴尖微微绷紧，说明她并未完全放松警惕。在那场波及了整个幻想乡的异变发生之后，少女经历了诸多的磨练，如今正以一位出色的特勤队员的身份活跃在139号避难所。\n\n“目标已击毙，风险全部清除。”\n\n她的同伴将枪口放下，目光扫视着眼前的山林。青翠的树木和绿油油的草甸中没有可供那些该死的尸变体躲藏的地方，脚下有着的就只是它们不自量力的袭击之后，所剩下的交战后的残骸。早已凝固的血液和暗淡苍白的肉块在被子弹撕裂后散落在地上，失去行动能力的碎尸已经不足以对两人造成威胁。远处的树影里也许还藏着几具，但没有声音的指引，它们很快就会重新陷入无目的的游荡，不会构成麻烦。\n\n射命丸文，曾经作为妖怪之山天狗部落中的一只鸦天狗，以一位记者的身份活跃在这片土地的天空中。你可以在除了天狗聚落以外的任何地方见到这位迅速而又富有想象力与文字艺术的可爱的小姐。她的笔尖曾经记录过幻想乡无数的日常与异变，她的相机曾捕捉过数不清的面孔与事件。而如今，她手中的笔杆子变成了枪杆子，但她那双善于发现的眼睛却从未改变。\n\n在那场异变发生之后，射命丸文就因为那场异变所带来的影响而从空中坠落，并','2026-10-28',1,0,NULL);
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bank_cards`
--

DROP TABLE IF EXISTS `bank_cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank_cards` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行名称',
  `card_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行卡号(明文,仅后端使用)',
  `card_number_masked` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '脱敏卡号(前端展示)',
  `card_holder` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '持卡人姓名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '状态',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_card` (`user_id`,`card_number`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='银行卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_cards`
--

LOCK TABLES `bank_cards` WRITE;
/*!40000 ALTER TABLE `bank_cards` DISABLE KEYS */;
/*!40000 ALTER TABLE `bank_cards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorites`
--

DROP TABLE IF EXISTS `favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorites` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → games.id',
  `created_at` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game` (`user_id`,`game_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏的游戏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorites`
--

LOCK TABLES `favorites` WRITE;
/*!40000 ALTER TABLE `favorites` DISABLE KEYS */;
INSERT INTO `favorites` VALUES ('16cca544459b2ecd3ccdcf23f01533ee','20ce5e5c-751b-405d-98f0-060baf9dffdd','71',NULL),('3','20ce5e5c-751b-405d-98f0-060baf9dffdd','23',NULL),('4','41773925-6164-4e8c-9dc6-be142038cf3f','8',NULL),('d27182a1a958a05c2bc43a9c123635ec','20ce5e5c-751b-405d-98f0-060baf9dffdd','5',NULL);
/*!40000 ALTER TABLE `favorites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `games` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游戏英文名',
  `name_cn` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏中文名',
  `price` decimal(10,2) DEFAULT NULL COMMENT '现价（元）',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价（元）',
  `discount` decimal(5,2) DEFAULT NULL COMMENT '折扣百分比（如 11.00=89 折）',
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏封面 URL',
  `link` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏链接（Steam 商店页）',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '游戏介绍',
  `release_date` date DEFAULT NULL COMMENT '发行日期',
  `developer` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开发商',
  `is_presale` tinyint(1) DEFAULT '0' COMMENT '是否预售: 1=是',
  `stock` int DEFAULT NULL COMMENT '库存数量',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台收录的游戏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `games`
--

LOCK TABLES `games` WRITE;
/*!40000 ALTER TABLE `games` DISABLE KEYS */;
INSERT INTO `games` VALUES ('1','Resident Evil Requiem','生化危机：安魂曲',309.00,348.00,11.00,'/picture/安魂曲.jpg','三级项目安魂曲详情.html','一款史诗级的动作冒险游戏，带你进入一个充满神秘和危险的世界。玩家将扮演主角在末日世界中生存，解开各种谜题，与丧尸战斗。','2024-12-20','Epic Games',1,99,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('10','RollBot','RollBot 机器人之路',18.00,33.00,45.00,'/picture/games/RollBot.jpg','','机器人滚球益智游戏，控制机器人通过各种复杂关卡。','2023-07-20','Indie Dev',0,160,'2026-05-21 14:05:45','2026-09-18 10:04:22',0),('100','Age of Empires IV','帝国时代4',98.00,199.00,51.00,'/uploads/games/34bd6b49f1da4312902b12f586460390.jpg',NULL,'帝国时代4，经典 RTS 回归，中世纪文明对抗。','2021-10-28','Relic Entertainment',0,60,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('101','Age of Empires II: Definitive Edition','帝国时代2终极版',78.00,199.00,61.00,'/uploads/games/21b1ce09ad27447dbf6f773b6be72b56.jpg',NULL,'帝国时代2终极版，高清重制经典 RTS。','2019-11-14','Forgotten Empires',0,70,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('102','Total War: Warhammer III','全面战争：战锤3',168.00,298.00,44.00,'/uploads/games/1ced996a24e24b49a21f5934d8a35b4b.jpg',NULL,'全面战争：战锤3，战锤奇幻世界的史诗战略。','2022-02-17','Creative Assembly',0,55,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('103','Stellaris','群星',138.00,199.00,31.00,'/uploads/games/78cd6bc0ecb54a4aabfe6c9bde0daadf.jpg',NULL,'群星，宇宙沙盒策略，管理你的银河帝国。','2016-05-09','Paradox Development Studio',0,65,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('104','Hearts of Iron IV','钢铁雄心4',168.00,200.00,16.00,'/uploads/games/a752130f194b45659ad466d04d6a4a30.jpg',NULL,'钢铁雄心4，二战大战略模拟。','2016-06-06','Paradox Development Studio',0,58,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('105','Crusader Kings III','十字军之王3',168.00,200.00,16.00,'/uploads/games/0b067c43a2e143afa1276fd72c374605.jpg',NULL,'十字军之王3，中世纪王朝经营，角色扮演大战略。','2020-09-01','Paradox Development Studio',0,62,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('106','Victoria 3','维多利亚3',178.00,200.00,11.00,'/uploads/games/bc89df4ce1a34ac98bb7fca66f171ac8.jpg',NULL,'维多利亚3，19世纪帝国主义大战略。','2022-10-25','Paradox Development Studio',0,48,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('107','Europa Universalis IV','欧陆风云4',118.00,199.00,41.00,'/uploads/games/38b37105142348029765d9e209ed5a30.jpg',NULL,'欧陆风云4，大航海时代全球战略。','2013-08-13','Paradox Development Studio',0,72,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('108','Naraka: Bladepoint','永劫无间',198.00,298.00,34.00,'/uploads/games/c18bcab3988a43fab0b359451f8db904.jpg',NULL,'永劫无间，国产武侠大逃杀动作竞技。','2021-08-12','24 Entertainment',0,80,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('109','The Legend of Heroes: Trails Through Daybreak','英雄传说：黎之轨迹',268.00,298.00,10.00,'/uploads/games/99419be684b443449ed5b10e147a561a.jpg',NULL,'英雄传说：黎之轨迹，Falcom 最新轨迹系列。','2024-03-01','Falcom',0,35,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('11','Heroines of Swords & Spells 2','剑与魔法的女主角们2',34.00,42.00,19.00,'/picture/games/剑与魔法的女主角们2.jpg','','日系RPG游戏，与众多女主角一起展开冒险。','2023-04-10','JRPG Studio',0,140,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('110','Like a Dragon: Gaiden - The Man Who Erased His Name','如龙7外传：无名之龙',198.00,298.00,34.00,'/uploads/games/47e992b5a87e4bbabb423ad2416935d0.jpg',NULL,'如龙7外传：无名之龙，桐生一马的故事。','2023-11-09','Ryu Ga Gotoku Studio',0,38,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('111','The Last of Us Part I','最后生还者 Part I',298.00,298.00,0.00,'/uploads/games/f7d1f797d0434db5b720e0ac9b92988f.jpg',NULL,'最后生还者Part I，末日生存恐怖经典重制。','2023-03-28','Naughty Dog',0,42,'2026-09-07 10:27:24','2026-09-18 10:04:58',0),('112','God of War','战神',198.00,298.00,34.00,'/uploads/games/1134213644874b1ba426be9b38d9e855.jpg',NULL,'战神2018，北欧神话动作冒险。','2022-01-14','Santa Monica Studio',0,44,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('113','Horizon Zero Dawn Complete Edition','地平线：零之曙光',78.00,198.00,61.00,'/uploads/games/e64838ccbfcf49eda1067d1044f2402a.jpg',NULL,'地平线：零之曙光完整版，机械恐龙狩猎开放世界。','2020-08-07','Guerrilla Games',0,60,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('114','Deathloop','死亡循环',148.00,299.00,50.00,'/uploads/games/124963f624b94fd8a8715739de241f54.jpg',NULL,'死亡循环，Arkane 时间循环射击冒险。','2021-09-14','Arkane Lyon',0,40,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('115','Ghostwire: Tokyo','幽灵线：东京',128.00,298.00,57.00,'/uploads/games/09f1a92f243f41bb8148e8426c76cb9e.jpg',NULL,'幽灵线：东京，三上真司的都市灵异动作。','2022-03-25','Tango Gameworks',0,38,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('116','Hi-Fi RUSH','完美音浪',118.00,198.00,40.00,'/uploads/games/edf96c8d6cb843cc830d5708da437b8f.jpg',NULL,'完美音浪，Tango Gameworks 节奏动作。','2023-01-25','Tango Gameworks',0,42,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('117','The Quarry','采石场',128.00,298.00,57.00,'/uploads/games/b7d0772143c94508a5817cf10c34f961.jpg',NULL,'采石场，Supermassive 互动恐怖电影游戏。','2022-06-10','Supermassive Games',0,48,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('118','Until Dawn','直到黎明',88.00,198.00,55.00,'/uploads/games/5121add828454509b21b3e81a90d87b3.jpg',NULL,'直到黎明，经典互动恐怖电影游戏 PC 版。','2024-09-24','Supermassive Games',0,52,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('119','Detroit: Become Human','底特律：成为人类',118.00,198.00,40.00,'/uploads/games/1ce58153de0a4ac98b46d088c8f357bf.jpg',NULL,'底特律：成为人类，互动叙事经典。','2020-06-18','Quantic Dream',0,55,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('12','GolfKings','高球王者',12.90,39.00,67.00,'/picture/games/高球王者.jpg','','高尔夫模拟游戏，体验真实的高尔夫球场和竞技。','2023-08-30','Sports Games Inc',0,220,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('120','Astroneer','异星探险家',68.00,128.00,47.00,'/uploads/games/1956e328a62a4d21b21c95ba089c691a.jpg',NULL,'异星探险家，轻松的太空探索建造。','2019-02-06','System Era Softworks',0,100,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('121','No Man\'s Sky','无人深空',198.00,398.00,50.00,'/uploads/games/366f2ecbd0ed497eaa3f71b3aa389614.jpg',NULL,'无人深空，无限宇宙探索冒险。','2016-08-09','Hello Games',0,75,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('122','Kerbal Space Program 2','坎巴拉太空计划2',198.00,298.00,34.00,'/uploads/games/1238a57d878744a08daff4e79ee9924a.jpg',NULL,'坎巴拉太空计划2，航天模拟沙盒续作。','2023-02-24','Intercept Games',0,45,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('123','Frostpunk','寒霜朋克',78.00,199.00,61.00,'/uploads/games/14a6a5b9a67543dc872b0c504fcf1bed.jpg',NULL,'寒霜朋克，末日城市建造+道德抉择。','2018-04-24','11 bit studios',0,68,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('124','This War of Mine','这是我的战争',38.00,99.00,62.00,'/uploads/games/3ee2b184658f4f138d668b8b75897e89.jpg',NULL,'这是我的战争，生存策略经典。','2014-11-14','11 bit studios',0,85,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('125','Children of Morta','莫塔守山人',48.00,98.00,51.00,'/uploads/games/ea973f07c85f48158033056325e52cea.jpg',NULL,'莫塔守山人，家族 roguelike 动作。','2019-09-03','Dead Mage',0,55,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('126','Warframe','Warframe',108.00,198.00,45.00,'/uploads/games/2a3a29d8763e4f0b8c764218b737aff5.jpg',NULL,'星际战甲白金版，数字极限动作射击。','2013-03-25','Digital Extremes',0,80,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('127','Phasmophobia','恐鬼症',28.00,48.00,42.00,'/uploads/games/f6ded92c9f2d4e6fa17e83e9770c5455.jpg',NULL,'恐鬼症，四人联机捉鬼恐怖游戏。','2020-09-18','Kinetic Games',0,150,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('128','Backpack Hero','背包英雄',68.00,98.00,31.00,'/uploads/games/0267c4ae07f94b42a3a874415ea3e92a.jpg',NULL,'背包英雄，物品管理 roguelike。','2023-08-15','Jaspel',0,60,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('129','Lethal Company','致命公司',32.00,40.00,20.00,'/uploads/games/11ee5d92242542179c5770c59ba27af1.jpg',NULL,'致命公司，四人联机恐怖探索。','2023-10-23','Zeekerss',0,200,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('131','Shadow of the Tomb Raider','古墓丽影：暗影',98.00,299.00,67.00,'/uploads/games/c8b8a36e949d4cd3b8af47e4d458d5db.jpg',NULL,'古墓丽影：暗影，劳拉的最终冒险。','2018-09-14','Eidos-Montreal',0,60,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('132','Rise of the Tomb Raider','古墓丽影：崛起',78.00,299.00,74.00,'/uploads/games/14c3c75c11644003b36093e13e03e22b.jpg',NULL,'古墓丽影：崛起，西伯利亚冒险。','2016-02-09','Crystal Dynamics',0,70,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('133','Tomb Raider','古墓丽影',58.00,199.00,71.00,'/uploads/games/8d0285f4467f410685082defdaa0d7c1.jpg',NULL,'古墓丽影 2013 重启作。','2013-03-05','Crystal Dynamics',0,80,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('134','Just Cause 4','正当防卫4',78.00,298.00,74.00,'/uploads/games/fafeeced7c044be2b9bce541ed9fe3d7.jpg',NULL,'正当防卫4，开放世界搞事。','2018-12-04','Avalanche Studios',0,55,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('135','Watch Dogs: Legion','看门狗：军团',98.00,298.00,67.00,'/uploads/games/daa3aca4d6654a20a9632348616bbf57.jpg',NULL,'看门狗：军团，任何人都能成为主角。','2020-10-29','Ubisoft Toronto',0,50,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('136','Assassin\'s Creed Odyssey','刺客信条：奥德赛',88.00,298.00,70.00,'/uploads/games/36dca2490db441348b9b94df706ac5db.jpg',NULL,'刺客信条：奥德赛，古希腊冒险。','2018-10-05','Ubisoft Quebec',0,65,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('137','Assassin\'s Creed Origins','刺客信条：起源',78.00,298.00,74.00,'/uploads/games/9b358f0beb4f47e3b79dd3d6e52f8ca6.jpg',NULL,'刺客信条：起源，古埃及冒险。','2017-10-27','Ubisoft Montreal',0,70,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('138','Far Cry 6','孤岛惊魂6',78.00,298.00,74.00,'/uploads/games/2333fb9e68654788aa11069458683b7f.jpg',NULL,'孤岛惊魂6，雅拉岛开放世界。','2021-10-07','Ubisoft Toronto',0,55,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('139','Far Cry 5','孤岛惊魂5',68.00,298.00,77.00,'/uploads/games/afcba153fdef4b25ae1d9c1ed538adb3.jpg',NULL,'孤岛惊魂5，蒙大拿希望郡。','2018-03-27','Ubisoft Montreal',0,60,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('140','Tomp Clancy\'s Ghost Recon Breakpoint','幽灵行动：断点',68.00,298.00,77.00,'/uploads/games/be7af5e613894dfcaf3f5db45b7c77f9.jpg',NULL,'幽灵行动：断点，圣岛群生存。','2019-10-04','Ubisoft Paris',0,50,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('141','Hitman 3','杀手3',88.00,298.00,70.00,'/uploads/games/4e55871a820d4b9b8e466f8c0b916613.jpg',NULL,'杀手3，三部曲最终作。','2021-01-20','IO Interactive',0,55,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('142','HITMAN 2','杀手2',78.00,298.00,74.00,'/uploads/games/fe56794b9c6842d38b746b13e4ff86a5.jpg',NULL,'杀手2，潜行暗杀。','2018-11-13','IO Interactive',0,60,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('143','Middle-earth: Shadow of War','中土世界：战争之影',68.00,298.00,77.00,'/uploads/games/f9763b65dbd24006b6cd10e27b42118c.jpg',NULL,'中土世界：战争之影，控制兽人军团。','2017-10-10','Monolith Productions',0,58,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('144','Middle-earth: Shadow of Mordor','中土世界：魔多之影',48.00,198.00,76.00,'/uploads/games/e82c0069ef774e4fb8ffcdd572b2c1a0.jpg',NULL,'中土世界：魔多之影，复仇动作。','2014-09-30','Monolith Productions',0,65,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('145','Batman: Arkham Knight','蝙蝠侠：阿卡姆骑士',68.00,199.00,66.00,'/uploads/games/1189aee3f25f460d8a02e34938e714ea.jpg',NULL,'蝙蝠侠：阿卡姆骑士，系列完结。','2015-06-23','Rocksteady Studios',0,70,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('146','Batman: Arkham City','Batman: Arkham City',48.00,99.00,52.00,'/uploads/games/0f11664fe14d446fba5ec68282043b7b.jpg',NULL,'蝙蝠侠：阿卡姆之城。','2011-11-22','Rocksteady Studios',0,80,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('147','Injustice 2','不义联盟2',58.00,198.00,71.00,'/uploads/games/8f0c625a0be84237b07feb455eb84573.jpg',NULL,'不义联盟2，DC 英雄格斗。','2017-11-14','NetherRealm Studios',0,60,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('148','Mortal Kombat 11','真人快打11',78.00,199.00,61.00,'/uploads/games/6e769351e0cf4eb3bb9f61748e9ec171.jpg',NULL,'真人快打11，血腥格斗。','2019-04-23','NetherRealm Studios',0,55,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('149','Tekken 7','铁拳7',68.00,199.00,66.00,'/uploads/games/371373178a1c46139521c48d557b0469.jpg',NULL,'铁拳7，3D 格斗经典。','2017-06-02','BANDAI NAMCO',0,62,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('150','Street Fighter 5','街头霸王5',48.00,199.00,76.00,'/uploads/games/26d3e55507344647b6b77b7f64229236.jpg',NULL,'街头霸王5，格斗竞技。','2016-02-16','Capcom',0,70,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('151','For Honour','For Honour',68.00,199.00,66.00,'/uploads/games/617d1470a9294ae5849913cbad3367bf.jpg',NULL,'荣耀战魂，中世纪武士对决。','2017-02-14','Ubisoft Montreal',0,55,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('152','Overwatch','Overwatch',58.00,199.00,71.00,'/uploads/games/34a00614940d4fcf8d14b8d57f6793a6.jpg',NULL,'守望先锋1（历史包）。','2016-05-24','Blizzard',0,65,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('153','Rainbow Six Siege','彩虹六号：围攻',78.00,198.00,61.00,'/uploads/games/33bd3595f8d94d40a2cb786c21d85567.jpg',NULL,'彩虹六号：围攻，战术射击。','2015-12-01','Ubisoft Montreal',0,72,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('154','Tom Clancy\'s The Division 2','全境封锁2',68.00,298.00,77.00,'/uploads/games/60fa8b4d47d042e58fc559a98747440f.jpg',NULL,'全境封锁2，华盛顿特区 TPS。','2019-03-15','Ubisoft Massive',0,58,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('155','Destiny 2: Lightfall','命运2：光陨之秋',128.00,299.00,57.00,'/uploads/games/ac79f8357b6240e6a747bda2877b5ef8.jpg',NULL,'命运2：光陨之秋，科幻射击。','2023-02-28','Bungie',0,50,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('156','War Thunder','战争雷霆',88.00,198.00,55.00,'/uploads/games/aca45e1a1fcf4da5a38c6cf7a55cbd41.jpg',NULL,'战争雷霆，载具战斗模拟器。','2013-11-01','Gaijin Entertainment',0,60,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('157','World of Tanks','坦克世界',68.00,98.00,31.00,'/uploads/games/ea3a911409c84fbdb44bd3cbc6c16b39.jpg',NULL,'坦克世界，多人坦克对战。','2017-08-23','Wargaming',0,75,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('158','Arma 3','武装突袭3',88.00,149.00,41.00,'/uploads/games/2867a37c5fef400eb5b7eb9dadbded2a.jpg',NULL,'武装突袭3，硬核军事模拟。','2013-09-12','Bohemia Interactive',0,68,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('159','Squad','战术小队',98.00,149.00,34.00,'/uploads/games/26937f4017d24df89282a3243b2a0461.jpg',NULL,'战术小队，50v50 军事模拟。','2020-09-23','Offworld Industries',0,62,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('16','EA SPORTS FC 25','EA SPORTS FC 25',348.00,448.00,22.00,'/uploads/games/2b775972409448dda715e29ca0d5c317.jpg',NULL,'EA 年度足球游戏，全新 HyperMotionV 技术','2024-09-27','Electronic Arts',0,300,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('160','Insurgency: Sandstorm','叛乱：沙漠风暴',78.00,148.00,47.00,'/uploads/games/2543aed06e804351b33b079e2090adbf.jpg',NULL,'叛乱：沙漠风暴，硬核战术射击。','2018-12-12','Focus Entertainment',0,55,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('161','Ready or Not','严阵以待',118.00,138.00,14.00,'/uploads/games/c961746050374825ba2eb58801a01c1a.jpg',NULL,'严阵以待，SWAT 战术破门。','2023-12-14','VOID Interactive',0,60,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('162','Payday 3','收获日3',128.00,198.00,35.00,'/uploads/games/1241f4be213c4fb0836daa12c7172c3f.jpg',NULL,'收获日3，四人抢劫。','2023-09-21','Starbreeze Studios',0,50,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('163','Payday 2','收获日2',38.00,98.00,61.00,'/uploads/games/33dec5afb5214c02bc3e92ae90951f58.jpg',NULL,'收获日2，经典四人抢劫。','2013-08-13','Overkill - a Starbreeze Studio',0,80,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('164','Left 4 Dead 2','求生之路2',38.00,78.00,51.00,'/uploads/games/6d80fb0848b9488b87612326fd4cd3c3.jpg',NULL,'求生之路2，四人僵尸合作。','2009-11-17','Valve',0,100,'2026-09-07 10:39:00','2026-09-18 10:04:22',0),('165','Lobotomy Corporation','脑叶公司',80.00,80.00,0.00,'/uploads/games/f4fb993257344866be480c371bd18851.jpg',NULL,'一款管理怪物的模拟经营游戏，玩家需管理收容所中的异想体并应对危机。','2018-04-09','Project Moon',0,NULL,'2026-10-27 10:09:32','2026-10-28 08:13:23',0),('166','Library of Ruina','废墟图书馆',80.00,80.00,0.00,'/uploads/games/ed720d6694c644da805823169ec204ea.jpg',NULL,'一款以图书馆为舞台的卡牌战斗RPG，探索都市与书籍的秘密。','2021-08-10','Project Moon',0,NULL,'2026-10-27 10:59:57','2026-10-28 08:13:23',0),('167','废墟图书馆',NULL,148.00,148.00,0.00,'/uploads/games/0f05b3f7e11b44b8ae8d137c3d3b4f66.jpg',NULL,'在废墟图书馆中探索、战斗并揭开谜团的策略角色扮演游戏。','2021-08-10','Kadokawa',0,NULL,'2026-10-27 11:30:00','2026-10-27 11:30:00',1),('168','脑叶公司',NULL,80.00,80.00,0.00,'/uploads/games/0dbcafb37ace4e30bb3b332180096f12.jpg',NULL,'一款管理怪物的模拟经营游戏，管理异常收容设施。','2018-04-09','Project Moon',0,NULL,'2026-10-27 11:30:40','2026-10-27 16:43:05',1),('169','中英测试中文名','中英测试中文名',10.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-10-28 08:15:31','2026-10-28 08:15:31',1),('17','Grand Theft Auto V','侠盗猎车手V',59.70,149.90,60.00,'/uploads/games/1e81a061f4b44da296fb0bf79c68d612.jpg',NULL,'Rockstar 开放世界神作，洛圣都风云再起','2015-04-14','Rockstar Games',0,800,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('170','ZZ Only English Test',NULL,10.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-10-28 08:15:31','2026-10-28 08:15:31',1),('171','ZZ Both Test','双语都填',10.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-10-28 08:15:31','2026-10-28 08:15:31',1),('18','Cyberpunk 2077','赛博朋克2077',149.00,298.00,50.00,'/uploads/games/ff87c9c7ff9c43ba92f9fe60a8a59a59.jpg',NULL,'CD Projekt Red 开放世界科幻 RPG，夜之城冒险','2020-12-10','CD Projekt Red',0,600,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('19','Red Dead Redemption 2','荒野大镖客：救赎2',124.50,249.00,50.00,'/uploads/games/e7aa4ebb99834f1491576dfba3ea8aee.jpg',NULL,'西部题材开放世界动作冒险游戏，亚瑟摩根的传奇','2019-12-05','Rockstar Games',0,550,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('2','Resident Evil Requiem Deluxe Edition','生化危机：安魂曲 豪华版',354.00,398.00,11.00,'/picture/安魂曲.jpg','三级项目安魂曲豪华版详情.html','豪华版包含游戏本体、季票、独家皮肤包和数字原声带。最完整的安魂曲游戏体验。','2024-12-20','Epic Games',1,50,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('20','Elden Ring','艾尔登法环',198.00,298.00,34.00,'/uploads/games/deb835b3ebd34c1eabc981a7ded5ca5a.jpg',NULL,'宫崎英高 × 乔治RR马丁联手打造的开放世界魂类游戏','2022-02-25','FromSoftware',0,700,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('21','Baldurs Gate 3','博德之门3',298.00,298.00,0.00,'/uploads/games/29f06c8a362248a6b405bd8d850b8823.jpg',NULL,'拉瑞安工作室 CRPG 巨制，年度游戏大满贯','2023-08-03','Larian Studios',0,400,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('22','Hades','哈迪斯',71.40,128.00,44.00,'/uploads/games/47c778379b914853b7f1aec6b45ac885.jpg',NULL,'Supergiant 肉鸽动作游戏，逃出冥界之旅','2020-09-17','Supergiant Games',0,450,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('23','Hollow Knight','空洞骑士',48.00,98.00,51.00,'/uploads/games/b66c61e3616c4f2489654935ca2315cd.jpg',NULL,'Team Cherry 银河恶魔城神作，德特茅斯王国探索','2017-02-24','Team Cherry',0,500,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('24','Slay the Spire','杀戮尖塔',52.00,98.00,47.00,'/uploads/games/7384c25735b24780bf50496ca0cf287b.jpg',NULL,'Mega Crit 卡牌肉鸽神作，爬塔之旅','2019-01-23','Mega Crit Games',0,550,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('25','It Takes Two','It Takes Two',98.00,198.00,51.00,'/uploads/games/4c18127b9a1a46919635c9d7777c8065.jpg',NULL,'Hazelight 双人合作冒险游戏，年度游戏','2021-03-26','Hazelight Studios',0,600,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('26','A Way Out','A Way Out 逃出生天',78.00,148.00,47.00,'/uploads/games/81f76bd295f146fcb30211e753bd05a9.jpg',NULL,'Hazelight 双人合作越狱冒险','2018-03-23','Hazelight Studios',0,350,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('27','Resident Evil 4 Remake','生化危机4 重制版',198.00,298.00,34.00,'/uploads/games/84cad5095aa84ba389e81a2276b8adae.jpg',NULL,'卡普空重制版，里昂西班牙村庄冒险','2023-03-24','CAPCOM',0,480,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('29','Monster Hunter: World','怪物猎人：世界',158.00,298.00,47.00,'/uploads/games/0de55737f07942aebffa1e1888c49f71.jpg',NULL,'卡普空怪物猎人正统续作，新大陆探索','2018-08-09','CAPCOM',0,400,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('3','Fullbright Pres','Fullbright 全英展',6.00,26.00,77.00,'/picture/games/Fullbright Pres.jpg','','一款独特的探索冒险游戏，玩家需要在神秘的空间站中探索，解开隐藏在环境中的故事。','2023-08-15','Fullbright',0,200,'2026-05-21 14:05:45','2026-09-18 10:04:22',0),('30','Monster Hunter Rise','怪物猎人：崛起',198.00,298.00,34.00,'/uploads/games/1da288b350e9458eab3451cac5a0b98e.jpg',NULL,'卡普空怪物猎人崛起，翔虫新动作','2022-01-12','CAPCOM',0,380,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('31','Street Fighter 6','街头霸王6',248.00,298.00,17.00,'/uploads/games/a0fd0e1b3d8e49c4b7abb51a420e3e07.jpg',NULL,'街头霸王6，格斗新时代','2023-06-02','CAPCOM',0,300,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('32','Devil May Cry 5','鬼泣5',148.00,298.00,50.00,'/uploads/games/61b632a36c554495a1019470c1e2abcb.jpg',NULL,'鬼泣5，尼禄但丁V 三叉戟恶魔猎人','2019-03-08','CAPCOM',0,350,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('37','Mount & Blade II: Bannerlord','骑砍2：领主',148.00,198.00,25.00,'/uploads/games/38074b2a6b064db09eadebeb8d6f0538.jpg',NULL,'TaleWorlds 中世纪骑砍续作，领主冒险','2022-10-25','TaleWorlds Entertainment',0,400,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('38','The Witcher 3: Wild Hunt','巫师3：狂猎',124.50,249.00,50.00,'/uploads/games/21df1a322ca14189ae6b7866bb809cd6.jpg',NULL,'巫师3，杰洛特大陆冒险','2015-05-18','CD Projekt Red',0,550,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('39','Cyberpunk 2077: Ultimate Edition','赛博朋克2077 终极版',238.00,447.00,47.00,'/uploads/games/73c92f84595746d7a69271f47b445af6.jpg',NULL,'赛博朋克2077 终极版，含往日之影 DLC','2023-12-05','CD Projekt Red',0,280,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('4','Your Second Wife','你的另一个老婆',14.70,18.00,18.00,'/picture/games/你的另一个老婆.jpg','','轻松有趣的恋爱���拟游戏，体验与众不同的恋爱故事。','2023-11-20','独立游戏工作室',0,150,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('40','The Elder Scrolls V: Skyrim','The Elder Scrolls V: Skyrim',79.20,198.00,60.00,'/uploads/games/f4057d6d035a4d759a2feaeb7f06d71d.jpg',NULL,'上古卷轴5：天际','2011-11-10','Bethesda',0,600,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('41','Fallout 4','辐射4',99.60,198.00,50.00,'/uploads/games/102b05c5b8c944459f70aa07e9e2a168.jpg',NULL,'辐射4，废土冒险','2015-11-10','Bethesda',0,380,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('42','DOOM Eternal','命运永恒',99.00,198.00,50.00,'/uploads/games/f47f231742e54873a685e8a520f9f7c2.jpg',NULL,'毁灭战士永恒','2020-03-20','id Software',0,380,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('43','Prey','掠食',69.00,198.00,65.00,'/uploads/games/ccaab30b833046bbb1c9430210ed7e0d.jpg',NULL,'Arkane 掠食，太空站超自然冒险','2017-05-05','Arkane Studios',0,300,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('45','God of War Ragnarok','战神：诸神黄昏',348.00,448.00,22.00,'/uploads/games/84308efc3d2e4609af36df2fd8aa1201.jpg',NULL,'战神：诸神黄昏，北欧神话终章','2024-12-12','Santa Monica Studio',0,250,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('46','Marvels Spider-Man Remastered','Marvels Spider-Man Remastered',248.00,298.00,17.00,'/uploads/games/97108a8a86da4bc7bdaeefbe6d2e1ea1.jpg',NULL,'漫威蜘蛛侠重制版','2022-08-12','Insomniac Games',0,300,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('47','Marvels Spider-Man: Miles Morales','Marvels Spider-Man: Miles Morales',248.00,298.00,17.00,'/uploads/games/d019a07e90c941c3a03f588192861376.jpg',NULL,'漫威蜘蛛侠：迈尔斯莫拉莱斯','2022-11-18','Insomniac Games',0,280,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('48','EA SPORTS FC 24','EA SPORTS FC 24',248.00,298.00,17.00,'/uploads/games/bfb9bf4a6f174876bf5d0e79ad4f8fc3.jpg',NULL,'EA FC 24，HyperMotion 真实球员动画','2023-09-29','Electronic Arts',0,320,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('49','Frostpunk 2','Frostpunk 2 寒霜朋克2',248.00,298.00,17.00,'/uploads/games/ce87008f821a4da693e9aaad65cdef75.jpg',NULL,'11 bit studios 冰汽时代2，寒霜生存策略','2024-09-20','11 bit studios',0,260,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('5','Touhou Fantasy','东方奇缘记',2.50,18.00,86.00,'/picture/games/东方奇缘记.jpg','','东方Project同人游戏，结合了弹幕射击和RPG元素。','2023-05-10','东方同人社团',0,300,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('50','Silent Hill 2 Remake','寂静岭2 重制版',348.00,448.00,22.00,'/uploads/games/8c9b382adb124bccb2ef791e342b19d1.jpg',NULL,'寂静岭2 重制版，Bloober Team 心理恐怖','2024-10-08','Bloober Team',0,240,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('51','Black Myth: Wukong','黑神话：悟空',268.00,268.00,0.00,'/uploads/games/ee5ef64fc2844b97832153ff0a0d5be1.jpg',NULL,'游戏科学黑神话：悟空，国产3A动作神作','2024-08-20','游戏科学',0,999,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('52','GTA Trilogy Definitive Edition','GTA 三部曲终极版',148.00,228.00,35.00,'/uploads/games/99a0358b776747cbb069bdf89bae02a2.jpg',NULL,'GTA 三部曲最终版，罪恶都市+圣安地列斯+自由城','2021-11-11','Rockstar Games',0,350,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('53','Hitman: World of Assassination','杀手：暗杀世界',348.00,448.00,22.00,'/uploads/games/79bb638a353e4f8f99421099cdb69eee.jpg',NULL,'杀手：暗杀世界，代号47 全新三部曲合集','2023-01-26','IO Interactive',0,220,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('54','Persona 5 Royal','女神异闻录5 皇家版',248.00,298.00,17.00,'/uploads/games/464a04e994c041b59b9d30bb5798e26a.jpg',NULL,'女神异闻录5 皇家版，JRPG 殿堂级作品','2022-10-21','Atlus',0,340,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('55','Persona 4 Golden','女神异闻录4 黄金版',128.00,158.00,19.00,'/uploads/games/506c2e325d094e43874b5cc9bb5ad657.jpg',NULL,'女神异闻录4 黄金版','2020-06-13','Atlus',0,280,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('56','NieR: Automata','尼尔：机械纪元',139.00,278.00,50.00,'/uploads/games/7f8d035f6f4d49f9b2bf33c4332cd853.jpg',NULL,'尼尔：机械纪元，横尾太郎 × 白金工作室','2017-02-23','PlatinumGames',0,400,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('57','Nier Replicant ver.1.22','仁王',248.00,298.00,17.00,'/uploads/games/290ddd0749a34da3bcc364c3761c23d0.jpg',NULL,'尼尔：人工生命 加强版','2021-04-23','Toylogic',0,260,'2026-09-04 09:53:30','2026-09-18 10:04:22',0),('6','Galaxy Defense War','银河守卫战',18.90,41.00,54.00,'/picture/games/银河守卫战.jpg','','太空策略射击游戏，保卫银河系免受外星入侵。','2023-09-01','Galaxy Studio',0,120,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('62','Elden Ring: Shadow of the Erdtree','艾尔登法环：黄金树幽影 DLC',228.00,298.00,23.00,'/uploads/games/f138bda6bd0f40f0b2c1c630762e6283.jpg',NULL,'艾尔登法环黄金树幽影 DLC，全新地图暗影树影之地，米凯拉的故事。','2024-06-21','BANDAI NAMCO',0,30,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('64','Hollow Knight: Silksong','空洞骑士：丝之歌',68.00,98.00,30.00,'/uploads/games/c8507779cf4e4bd08374dafe4700ab8e.jpg',NULL,'空洞骑士丝之歌，全新的王国，控制黄蜂女霍恩特，挑战新的敌人和 BOSS。','2025-06-04','Team Cherry',0,45,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('66','Stardew Valley','星露谷物语',48.00,76.00,37.00,'/uploads/games/b733868b319042ab819261fdde4ba3e4.jpg',NULL,'星露谷物语，逃离都市继承农场，种植、钓鱼、采矿、交友，像素牧场模拟巅峰之作。','2016-02-26','ConcernedApe',0,200,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('67','Terraria','泰拉瑞亚',36.00,48.00,25.00,'/uploads/games/4522d865cee7479faa95acc3b0f57b5c.jpg',NULL,'泰拉瑞亚，2D 沙盒冒险，你能挖掘、建造、探索、战斗。无尽的可能性。','2011-05-16','Re-Logic',0,150,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('68','Don\'t Starve Together','饥荒联机版',48.00,68.00,29.00,'/uploads/games/e7afdf88d6d341e99c2e0a589bd12b0b.jpg',NULL,'饥荒联机版，威尔逊和他的朋友们在诡异的黑暗世界中挣扎求生。','2016-04-21','Klei Entertainment',0,100,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('7','我与她们的大学画像','我与她们的大学画像',16.55,20.00,17.00,'/picture/games/我与她们的大学画像.jpg','','校园恋爱视觉小说，体验大学生活中的甜蜜与感动。','2023-12-01','校园游戏社',0,180,'2026-05-21 14:05:45','2026-09-18 10:04:22',0),('71','Control','控制',158.00,233.00,32.00,'/uploads/games/f201b85001944bf28976e797dd5d9e81.jpg',NULL,'控制，杰西·法登成为联邦控制局新任局长，调查超自然事件。','2019-08-27','Remedy Entertainment',0,35,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('72','Death Stranding','死亡搁浅',198.00,298.00,34.00,'/uploads/games/95db752bf49d4507a78509083d6004fb.jpg',NULL,'死亡搁浅，小岛秀夫的全新 IP，山姆穿越后末日美国，连接分裂的文明。','2020-07-14','Kojima Productions',0,38,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('73','Assassin\'s Creed Valhalla','刺客信条：瓦尔哈拉',148.00,298.00,50.00,'/uploads/games/99d410d28d2649cc8b939b4dd30fbf2c.jpg',NULL,'刺客信条瓦尔哈拉，维京战士艾沃尔征服英格兰，2022 年最大刺客信条开放世界。','2020-11-10','Ubisoft',0,60,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('75','Starfield','星空',199.00,299.00,33.00,'/uploads/games/388993c9dc4f4279ab15318805b32654.jpg',NULL,'星空，探索浩瀚宇宙，建造飞船，在 1000+ 星球上书写你的传奇。','2023-09-06','Bethesda Game Studios',0,48,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('76','Lies of P','匹诺曹的谎言',198.00,298.00,34.00,'/uploads/games/84795431cb7642d0bd9bc6dbd73784de.jpg',NULL,'匹诺曹的谎言，魂类游戏，你是被制造的木偶，寻找人类的道路。','2023-09-19','NEOWIZ',0,33,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('77','Wo Long: Fallen Dynasty','卧龙：苍天陨落',188.00,298.00,37.00,'/uploads/games/0a0ca0b89698421bb66451fa199281cc.jpg',NULL,'卧龙苍天陨落，三国魂类动作游戏，忍龙组开发，以邪术乱世为背景。','2023-03-03','KOEI TECMO',0,36,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('78','Dome Keeper','穹顶守护者',68.00,96.00,29.00,'/uploads/games/a539317422954bef87d51918d9e02379.jpg',NULL,'穹顶守护者，塔防+Roguelike 采矿，保护你的穹顶。','2022-09-06','Bippinbits',0,80,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('79','RimWorld','环世界',108.00,149.00,28.00,'/uploads/games/3b30e16d8b5c4eb3a56815d250987157.jpg',NULL,'环世界，殖民地管理模拟，管理一群殖民者在外星球建立家园。','2018-10-17','Ludeon Studios',0,52,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('8','San Geng','三更',4.38,6.00,27.00,'/picture/games/三更.jpg','','恐怖解谜游戏，在深夜探索诡异的老宅，揭开尘封的秘密。','2023-10-31','Night Studio',0,250,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('80','Factorio','异星工厂',138.00,199.00,31.00,'/uploads/games/d5d0159b46a04460896cae10fdf8f413.jpg',NULL,'异星工厂，自动化建造，你将被游戏循环吞噬，天亮才发现。','2020-08-14','Wube Software',0,46,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('82','Marvel\'s Spider-Man 2','漫威蜘蛛侠2',248.00,299.00,17.00,'/uploads/games/b71a6e6178df4d15ad1851a10a48e2bc.jpg',NULL,'漫威蜘蛛侠2，彼得·帕克和迈尔斯·莫拉莱斯双主角，毒液登场。','2023-10-20','Insomniac Games',0,44,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('83','Metaphor: ReFantazio','暗喻幻想 ReFantazio',268.00,298.00,10.00,'/uploads/games/9b110dc0e70748cf8b035ec116f199da.jpg',NULL,'暗喻幻想，女神异闻录团队全新 IP，宏大的幻想世界观。','2024-10-11','ATLUS',0,32,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('84','Like a Dragon: Infinite Wealth','如龙：无限财富',198.00,298.00,34.00,'/uploads/games/836c432decb449cda8788ca1218a25ea.jpg',NULL,'如龙无限财富，春日一番和桐生一马双主角，夏威夷+日本双地图。','2024-01-26','Ryu Ga Gotoku Studio',0,30,'2026-09-07 10:09:03','2026-09-18 10:04:22',0),('88','Resident Evil Village','生化危机：村庄',148.00,228.00,35.00,'/uploads/games/42e60aff1f9c43aeb5180d26aaa8256b.jpg',NULL,'生化危机8：村庄，伊森·温特斯在神秘村庄寻找被绑架的女儿，恐怖求生冒险。','2021-05-07','CAPCOM',0,50,'2026-09-07 10:13:38','2026-09-18 10:04:22',0),('89','Horizon Forbidden West Complete Edition','地平线：西之绝境 完整版',178.00,298.00,40.00,'/uploads/games/10e112e07d6b4700ab0cc9e67b3a0bc4.jpg',NULL,'地平线：西部禁域完整版，包含 Burning Shores 资料片，艾洛伊的冒险继续。','2024-03-21','Guerrilla Games',0,40,'2026-09-07 10:13:38','2026-09-18 10:04:22',0),('9','Tingus Goose','神经鹅',22.98,28.00,18.00,'/picture/games/神经鹅.jpg','','搞怪模拟游戏，扮演一只制造混乱的大鹅。','2023-06-15','House House',0,100,'2026-05-21 14:05:45','2026-10-28 08:13:44',0),('90','Dead Cells','死亡细胞',88.00,169.00,48.00,'/uploads/games/136baf79ad104e52a97b337c24037a4e.jpg',NULL,'死亡细胞，roguelike 动作游戏，在不断变化的城堡中探索。','2018-08-07','Motion Twin',0,60,'2026-09-07 10:13:38','2026-09-18 10:04:22',0),('91','Counter-Strike 2 Prime Status Upgrade','Counter-Strike 2 Prime Status Upgrade',108.00,108.00,0.00,'/uploads/games/b88a4accf34a4aa78b2e31845580166a.jpg',NULL,'CS2 Prime 状态升级，获得官方匹配资格、掉落物品和成就。','2023-09-27','Valve',0,999,'2026-09-07 10:13:38','2026-09-18 10:04:58',0),('93','Portal 2','Portal 2',39.00,99.00,61.00,'/uploads/games/b8a951a1e5be406bbc5db456e6c562fa.jpg',NULL,'传送门2，经典解谜益智游戏，支持双人合作。','2011-04-18','Valve',0,100,'2026-09-07 10:13:38','2026-09-18 10:04:22',0),('94','Monster Hunter Wilds','怪物猎人：荒野',298.00,298.00,0.00,'/uploads/games/9a7d669810fe4aa0b14f71bd687284cd.jpg',NULL,'怪物猎人：荒野，CAPCOM 最新开放世界狩猎动作。','2025-02-28','CAPCOM',0,45,'2026-09-07 10:27:24','2026-09-18 10:04:58',0),('95','Battlefield 2042','战地2042',78.00,298.00,74.00,'/uploads/games/3f35565210a1475a94c7c9000af6f6b8.jpg',NULL,'战地 2042，128 人大规模战场，海陆空全方位战争。','2021-11-19','DICE',0,55,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('96','The Sims 4','模拟人生4',99.00,199.00,50.00,'/uploads/games/e4df1866519c490692db95714ea0046e.jpg',NULL,'模拟人生4，人生模拟经典，创造你的虚拟家庭。','2014-09-02','Maxis',0,80,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('97','Cities: Skylines II','城市天际线2',178.00,298.00,40.00,'/uploads/games/5fdb17b9324a48b1b22d518b5b7f0e2e.jpg',NULL,'城市天际线2，开放世界城市建设模拟。','2023-10-24','Colossal Order',0,45,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('98','Cities: Skylines','城市天际线',39.00,88.00,56.00,'/uploads/games/2366a5b27a544746966fad4f08690f9d.jpg',NULL,'城市天际线1，经典城市规划模拟游戏。','2015-03-10','Colossal Order',0,120,'2026-09-07 10:27:24','2026-09-18 10:04:22',0),('99','Football Manager 2024','足球经理2024',228.00,298.00,23.00,'/uploads/games/dd587d9fe9494016aa49e3b299f9479c.jpg',NULL,'足球经理2024，最真实的足球经营模拟。','2023-11-06','Sports Interactive',0,50,'2026-09-07 10:27:24','2026-09-18 10:04:22',0);
/*!40000 ALTER TABLE `games` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `listings`
--

DROP TABLE IF EXISTS `listings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `listings` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `seller_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id（卖家）',
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → games.id',
  `game_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游戏名称（上架快照，固化）',
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏图片（快照）',
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '版本描述（如 豪华版/终极版）',
  `cdkey` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'CDKey 激活码（type=cdkey 时有值）',
  `price` decimal(12,2) NOT NULL COMMENT '挂单价',
  `original_price` decimal(12,2) DEFAULT NULL COMMENT '原价（卖家参考）',
  `region` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售地区',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态: active=在售, sold=已售, cancelled=已取消',
  `sold_at` datetime DEFAULT NULL COMMENT '售出时间',
  `created_at` datetime DEFAULT NULL COMMENT '上架时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  `type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: cdkey, py',
  `quota` decimal(12,2) DEFAULT NULL COMMENT '卖家额度限制',
  `auto_deliver` tinyint(1) DEFAULT '0' COMMENT '是否自动发货: 1=自动',
  PRIMARY KEY (`id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_game` (`game_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_listings_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_listings_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卖家挂单出售的商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `listings`
--

LOCK TABLES `listings` WRITE;
/*!40000 ALTER TABLE `listings` DISABLE KEYS */;
INSERT INTO `listings` VALUES ('28631e8f-1f75-4a13-909e-44620ef72f99','20ce5e5c-751b-405d-98f0-060baf9dffdd','8','三更','/picture/games/三更.jpg','标准版','5684-OUDK-U9LX-SYQ6',4.37,6.00,'国区','available',NULL,'2026-09-03 10:10:39','2026-10-12 10:10:26','cdkey',NULL,0),('5c2ffd5b-7a6e-4d70-966a-13647c06f26a','20ce5e5c-751b-405d-98f0-060baf9dffdd','8','三更','/picture/games/三更.jpg','标准版','MU0M-3SR5-VLRK-LO77',88.00,6.00,'国区','available',NULL,'2026-09-03 10:10:40','2026-10-12 10:10:26','cdkey',NULL,0),('7fbbdbf6-3480-41b3-8554-f04df5a060e1','20ce5e5c-751b-405d-98f0-060baf9dffdd','5','东方奇缘记','/picture/games/东方奇缘记.jpg','标准版','11111111111',12.00,18.00,'国区','sold','2026-09-07 10:44:23','2026-09-03 09:49:21','2026-09-07 10:44:23','cdkey',NULL,0),('874a8b41-c431-4092-9ac1-d8fd26bb12bd','20ce5e5c-751b-405d-98f0-060baf9dffdd','8','三更','/picture/games/三更.jpg','标准版','NFYH-SYHL-309R-ERE4',88.00,6.00,'国区','available',NULL,'2026-09-03 10:10:40','2026-10-12 10:10:26','cdkey',NULL,0),('8e241392-6c26-40a8-9ff3-4961c5ecebfd','20ce5e5c-751b-405d-98f0-060baf9dffdd','165','脑叶公司','/uploads/games/f8bd084c525e4a49b166cf4d79b8a51e.jpg','标准版','ALDR-E0NG-4NCG-VZO6',79.99,80.00,'国区','available',NULL,'2026-10-27 16:45:08','2026-10-27 16:45:41','cdkey',NULL,0),('d7829141-5595-4e33-9db7-d970544b95ca','20ce5e5c-751b-405d-98f0-060baf9dffdd','9','神经鹅','/picture/games/神经鹅.jpg','标准版','DMKI-L0E2-5NTC-3G5X',0.77,28.00,'国区','available',NULL,'2026-09-03 10:36:17','2026-10-20 11:06:26','cdkey',NULL,0),('f722fe84-8a43-4d95-a833-d4dc89b2038d','20ce5e5c-751b-405d-98f0-060baf9dffdd','26','A Way Out','/uploads/games/81f76bd295f146fcb30211e753bd05a9.jpg','标准版','CU50-LFSQ-9UNW-H9PU',77.99,148.00,'国区','sold','2026-09-07 09:08:22','2026-09-07 09:07:48','2026-09-07 09:08:22','cdkey',NULL,0),('list-001','20ce5e5c-751b-405d-98f0-060baf9dffdd','8','三更','/picture/games/三更.jpg','标准版','AAA1-BBB2-CCC3-DDD4',25.00,39.00,'国区','sold','2026-09-02 10:47:53','2026-09-02 10:37:51','2026-09-03 08:59:56','cdkey',NULL,0),('list-002','20ce5e5c-751b-405d-98f0-060baf9dffdd','5','东方奇缘记','/picture/games/东方奇缘记.jpg','标准版','EEE5-FFF6-GGG7-HHH8',58.00,99.00,'国区','sold','2026-09-03 09:29:31','2026-09-02 10:37:51','2026-09-03 09:29:31','cdkey',NULL,0),('list-003','20ce5e5c-751b-405d-98f0-060baf9dffdd','1','生化危机安魂曲','/picture/安魂曲.jpg','豪华版','III9-JJJ0-KKK1-LLL2',189.00,309.00,'国区','available',NULL,'2026-09-02 10:37:51','2026-10-12 10:10:26','cdkey',NULL,0),('list-fold-01','20ce5e5c-751b-405d-98f0-060baf9dffdd','8','三更','/picture/games/三更.jpg','标准版','FOLD-KEY-001-AABB',25.00,38.00,'国区','sold','2026-09-02 11:11:07','2026-09-02 11:07:00','2026-09-03 08:59:56','cdkey',NULL,0),('list-fold-02','20ce5e5c-751b-405d-98f0-060baf9dffdd','8','三更','/picture/games/三更.jpg','标准版','FOLD-KEY-002-CCDD',25.00,38.00,'国区','sold','2026-09-02 11:25:08','2026-09-02 11:07:00','2026-09-03 08:59:56','cdkey',NULL,0),('py-003','41773925-6164-4e8c-9dc6-be142038cf3f','5','东方奇缘记','/picture/games/东方奇缘记.jpg','标准版','',38.00,56.00,'中国','available',NULL,'2026-09-04 08:47:03','2026-09-04 09:03:12','py',762.00,0),('py-004','41773925-6164-4e8c-9dc6-be142038cf3f','8','三更','/picture/games/三更.jpg','标准版','',4.20,6.00,'中国','available',NULL,'2026-09-04 08:47:03','2026-10-20 11:05:17','py',174.80,0);
/*!40000 ALTER TABLE `listings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号，UNIQUE',
  `buyer_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → users.id（买家，NOT NULL 但历史数据有 NULL）',
  `seller_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → users.id（卖家）',
  `listing_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联上架 id',
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → games.id',
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏名称（订单快照，历史数据有 NULL）',
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏图片（订单快照）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '单价（快照）',
  `quantity` int DEFAULT '1' COMMENT '数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '成交总额（快照）',
  `delivery_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付方式: cdkey, 代购, 直充',
  `version` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '版本（快照）',
  `cdkey` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'CDKey（快照）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态: pending, completed, cancelled, refunded',
  `order_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单类型: cdkey, py',
  `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime DEFAULT NULL COMMENT '下单时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `fk_orders_seller` (`seller_id`),
  KEY `fk_orders_game` (`game_id`),
  CONSTRAINT `fk_orders_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='买家下单的订单（商品快照固化）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES ('0d75de36-0971-49c4-a781-49afcfd38fea','ORDDBBA74FEB305','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-004','8','三更','/picture/games/三更.jpg',4.20,1,4.20,'gift','标准版',NULL,'completed','py',NULL,NULL,'2026-09-04 09:01:54','2026-09-04 09:01:54'),('139b6f21-44bb-4380-bc7b-600b8e0d7d1b','ORDMPUYAMYR','21e82751-fab1-4d8d-9958-745f1f6e1666',NULL,NULL,NULL,'生化危机:安魂曲 豪华版','/picture/安魂曲.jpg',354.00,1,354.00,'cdkey','标准版','UMDO6-97CGU-46I0I','completed','cdkey',NULL,NULL,'2026-06-01 08:32:37','2026-06-01 08:32:36'),('1424bf6c-924e-4db9-8c34-d1118305c9af','ORD0D5BDB0ADA2B','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'5','东方奇缘记','/picture/games/东方奇缘记.jpg',1.88,1,1.88,'gift','标准版','','completed','py',NULL,NULL,'2026-09-04 08:30:28','2026-09-04 08:30:28'),('18276ea0-10ca-4eec-8397-5db929fe512b','ORDE80B7E44B39C',NULL,'20ce5e5c-751b-405d-98f0-060baf9dffdd','list-fold-02','1','三更','/picture/games/三更.jpg',25.00,1,25.00,'cdkey','标准版','FOLD-KEY-002-CCDD','completed','cdkey','balance',NULL,'2026-09-02 11:25:08','2026-09-18 11:25:06'),('1ba7af10-bf87-453d-af79-4812ed31f8ff','ORD6C582AD9C91B','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'98','Cities: Skylines','/uploads/games/2366a5b27a544746966fad4f08690f9d.jpg',39.00,1,39.00,'cdkey','标准版','0H55I-A49T2-4NR82','completed','cdkey','alipay',NULL,'2026-10-15 09:27:48','2026-10-15 09:27:48'),('282b4ce4-d75e-4c02-86f5-5ed5b4c0c2fd','ORDFDF85A316F57','41773925-6164-4e8c-9dc6-be142038cf3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','list-002','5','东方奇缘记','/picture/games/东方奇缘记.jpg',58.00,1,58.00,'cdkey','标准版','EEE5-FFF6-GGG7-HHH8','completed','cdkey','alipay',NULL,'2026-09-03 09:29:31','2026-09-03 09:29:31'),('3e8f0497-c0b6-4803-a02c-c1b42ae4c9c1','ORDD7931E629242','41773925-6164-4e8c-9dc6-be142038cf3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','f722fe84-8a43-4d95-a833-d4dc89b2038d','26','A Way Out','/uploads/games/81f76bd295f146fcb30211e753bd05a9.jpg',77.99,1,77.99,'cdkey','标准版','CU50-LFSQ-9UNW-H9PU','completed','cdkey','alipay',NULL,'2026-09-07 09:08:22','2026-09-07 09:08:22'),('44a2f391-549f-4a79-a1e6-06470f9d90cd','ORDMQ4Z5M1U','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,NULL,'三更','/picture/games/三更.jpg',4.38,1,4.38,'cdkey','标准版','FXOKH-CKEZD-A9PMY','completed','cdkey',NULL,NULL,'2026-06-08 08:54:23','2026-06-08 08:54:24'),('4e6815bd-66d7-46c0-ae3b-1bd055a3f04b','ORDEAF4CD0D0234',NULL,'20ce5e5c-751b-405d-98f0-060baf9dffdd','list-001','1','三更','/picture/games/三更.jpg',25.00,1,25.00,'cdkey','标准版','AAA1-BBB2-CCC3-DDD4','completed','cdkey','balance',NULL,'2026-09-02 10:47:53','2026-09-18 11:25:06'),('50ef112c-0915-4426-9626-e741aa1413cc','ORDMTGNEAWI','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,NULL,'东方奇缘记','/picture/games/东方奇缘记.jpg',2.50,1,2.50,'cdkey','标准版','GKVOI-WYXL6-HOR5W','completed','cdkey',NULL,NULL,'2026-08-31 02:57:35','2026-08-31 02:57:34'),('5fe4f026-3e88-45c3-9386-07641e9576d7','ORDDE9CB33A240A','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'136','Assassin\'s Creed Odyssey','/uploads/games/36dca2490db441348b9b94df706ac5db.jpg',88.00,1,88.00,'cdkey','标准版','1B740-DMM53-WX8FM','completed','cdkey','alipay',NULL,'2026-10-15 10:04:34','2026-10-15 10:04:34'),('61df3c22-7a2a-4c25-87ba-f1d31a92cba0','ORDMPGFK45V',NULL,NULL,NULL,NULL,'Fullbright Pres','/picture/games/Fullbright Pres.jpg',6.00,1,6.00,'cdkey','标准版','6GK0T-8VVE3-1TUNM','completed','cdkey',NULL,'2026-05-22 05:14:10','2026-05-22 04:39:20','2026-09-18 11:25:06'),('67685c60-3d4b-4c28-afe4-e3c3188779e3','ORD521DC7ED9EE3','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'158','Arma 3','/uploads/games/2867a37c5fef400eb5b7eb9dadbded2a.jpg',88.00,1,88.00,'cdkey','标准版','BRNY8-NPGRP-OLBF6','completed','cdkey','alipay',NULL,'2026-09-10 08:23:12','2026-09-10 08:23:12'),('6dc86e25-6d21-41b2-b089-82d21e001a9d','ORDF994F3A3E35D','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'1','生化危机:安魂曲','/picture/安魂曲.jpg',9.99,1,9.99,NULL,NULL,'TEST1-TEST2-TEST3','completed','cdkey',NULL,NULL,'2026-09-02 09:57:25','2026-09-07 08:06:01'),('6f53b55d-9517-4708-9aa9-784fc34ff8c7','ORDMQ3G63F2','f0f9424a-1963-4cb3-9c89-fa5461b04b0b',NULL,NULL,NULL,'三更','/picture/games/三更.jpg',4.38,1,4.38,'cdkey','标准版','ZI1EK-AEVAJ-QADHJ','completed','cdkey',NULL,NULL,'2026-06-07 07:15:07','2026-06-07 07:15:08'),('7b449627-4b13-4d63-9628-4437eea536af','ORDE9BD46E1CC06','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-003','4','你的另一个老婆','/picture/games/你的另一个老婆.jpg',38.00,1,38.00,'gift','标准版',NULL,'completed','py',NULL,NULL,'2026-09-04 09:03:12','2026-09-04 09:03:12'),('7cae3a75-1286-4c11-90a7-c3d5487ee433','ORDBA8C001F66DB','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-004','100','Age of Empires IV','/uploads/games/34bd6b49f1da4312902b12f586460390.jpg',4.20,1,4.20,'gift','标准版',NULL,'closed','py',NULL,NULL,'2026-10-20 11:05:17','2026-10-20 11:09:37'),('7e94426c-e9b3-4056-87ae-aff37e895f07','ORDB27BAF918F6B','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'18','Cyberpunk 2077','/uploads/games/ff87c9c7ff9c43ba92f9fe60a8a59a59.jpg',149.00,1,149.00,'cdkey','标准版','YXXXR-QLOFD-R0CHE','completed','cdkey','alipay',NULL,'2026-09-07 08:12:34','2026-09-07 08:12:34'),('82d84fda-cf62-48b5-b9b9-f9751c2a1fc4','ORDMPGFV3T8',NULL,NULL,NULL,NULL,'生化危机:安魂曲','/picture/安魂曲.jpg',309.00,1,309.00,'cdkey','标准版','OXATV-4RI3F-EDEVO','completed','cdkey',NULL,'2026-05-22 05:14:11','2026-05-22 04:47:52','2026-09-18 11:25:06'),('8a020c4c-21f4-4f08-9988-ac7cc131a3a7','ORD0CF654AC1A65',NULL,'20ce5e5c-751b-405d-98f0-060baf9dffdd','list-fold-01','1','三更','/picture/games/三更.jpg',25.00,1,25.00,'cdkey','标准版','FOLD-KEY-001-AABB','completed','cdkey','balance',NULL,'2026-09-02 11:11:07','2026-09-18 11:25:06'),('8a7f7067-a625-4726-aaca-a6b69f6602a4','ORD836CCB59C24C','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'1','生化危机:安魂曲','/picture/安魂曲.jpg',88.88,1,88.88,NULL,NULL,'FMT-EST-TEST','completed','cdkey',NULL,NULL,'2026-09-02 10:02:29','2026-09-07 08:06:01'),('909a1e7d-5995-473e-b171-b94ed93af607','ORD8212DD7FF284','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'114','Deathloop','/uploads/games/124963f624b94fd8a8715739de241f54.jpg',148.00,1,148.00,'cdkey','标准版','7B894-755Q8-XTK6T','completed','cdkey','alipay',NULL,'2026-10-12 09:53:09','2026-10-12 09:53:09'),('a42c4d82-ffb6-4986-8686-39b966ca2647','ORD9FF8F5E4C8F5','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'5','东方奇缘记','/picture/games/东方奇缘记.jpg',2.50,1,2.50,'cdkey','标准版','W4U9Q-7113S-W31CQ','completed','cdkey','alipay',NULL,'2026-09-08 10:27:23','2026-09-08 10:27:23'),('a501925f-a426-4868-a7e1-7be44eeb792f','ORDDF7F4B3F268E','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-004','8','三更','/picture/games/三更.jpg',4.20,1,4.20,'gift','标准版',NULL,'completed','py',NULL,NULL,'2026-09-04 09:02:10','2026-09-04 09:02:10'),('a7c05217-2068-4962-b9e5-1d400db36aa0','ORD7EED9131C8D8','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'9','神经鹅','/picture/games/神经鹅.jpg',22.98,1,22.98,'cdkey','标准版','HWHE8-FATFV-Z88P5','completed','cdkey','alipay',NULL,'2026-09-08 09:17:33','2026-09-08 09:17:33'),('a812fca9-e886-4c41-83e8-5cb3e985a2e6','ORDMPGLAIFG','21e82751-fab1-4d8d-9958-745f1f6e1666',NULL,NULL,NULL,'东方奇缘记','/picture/games/东方奇缘记.jpg',2.50,1,2.50,'cdkey','标准版','FAYGN-81QJ9-HWDJR','completed','cdkey',NULL,NULL,'2026-05-22 07:19:49','2026-05-22 07:19:52'),('a8f92732-a172-4281-aa51-876fd9ac5018','ORD86C8AC0EBDE5','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-004','5','东方奇缘记','/picture/games/东方奇缘记.jpg',4.20,1,4.20,'gift','标准版',NULL,'completed','py',NULL,NULL,'2026-09-04 09:02:53','2026-09-04 09:02:53'),('ace24637-395e-4a1f-afc1-b48d2732379e','ORD631A50F214BF','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'23','Hollow Knight','/uploads/games/b66c61e3616c4f2489654935ca2315cd.jpg',48.00,1,48.00,'cdkey','标准版','AV5SM-4KUVW-IRVLG','completed','cdkey','alipay',NULL,'2026-09-10 08:10:05','2026-09-10 08:10:05'),('adb48b47-997d-4383-bf9d-edceff81da00','ORDMPGL8RGH','21e82751-fab1-4d8d-9958-745f1f6e1666',NULL,NULL,NULL,'生化危机:安魂曲','/picture/安魂曲.jpg',309.00,1,309.00,'cdkey','标准版','HMTGA-YKAH7-HS8P5','completed','cdkey',NULL,NULL,'2026-05-22 07:18:28','2026-05-22 07:18:30'),('b7a4f34e-2b28-408e-8f18-0d8e7344bd50','ORDMQ3G5R39','f0f9424a-1963-4cb3-9c89-fa5461b04b0b',NULL,NULL,NULL,'东方奇缘记','/picture/games/东方奇缘记.jpg',2.50,1,2.50,'cdkey','标准版','1WHHR-STP9J-00ISJ','completed','cdkey',NULL,NULL,'2026-06-07 07:14:51','2026-06-07 07:14:52'),('bf43087f-49fb-45b6-be1a-3c71688710eb','ORD6310E2471033','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'9','神经鹅','/picture/games/神经鹅.jpg',22.98,1,22.98,'cdkey','标准版','F13ZX-GLTQE-VI23C','completed','cdkey','alipay',NULL,'2026-09-08 09:17:47','2026-09-08 09:17:47'),('c3b9b98b-cba8-42ee-9167-07dcba252e3f','ORDB9AFAA77BD71','41773925-6164-4e8c-9dc6-be142038cf3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','py-001','101','Age of Empires II: Definitive Edition','/uploads/games/21b1ce09ad27447dbf6f773b6be72b56.jpg',40.00,1,40.00,'gift','标准版',NULL,'completed','py',NULL,NULL,'2026-09-11 11:32:04','2026-09-11 11:32:04'),('ccd45fd3-84f9-4624-b3fa-6c9f82f66e52','ORDMQ3G27S8','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,NULL,'生化危机:安魂曲','/picture/安魂曲.jpg',309.00,1,309.00,'cdkey','标准版','UXX4K-NLHBU-P2GK0','completed','cdkey',NULL,NULL,'2026-06-07 07:12:06','2026-06-07 07:12:07'),('cd3a0884-b678-4324-8f0d-6323c6db0a66','ORDMQ3GD13F','ef8caf9b-917a-4356-a7c6-1d01f9388b7e',NULL,NULL,NULL,'东方奇缘记','/picture/games/东方奇缘记.jpg',2.50,1,2.50,'cdkey','标准版','YI29U-ELR9I-1B9SW','completed','cdkey',NULL,NULL,'2026-06-07 07:20:31','2026-06-07 07:20:31'),('d0925ecf-7444-40dd-af57-831cf32a6ba0','ORDMPXUT6TE','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,NULL,'生化危机:安魂曲','/picture/安魂曲.jpg',309.00,1,309.00,'cdkey','标准版','Z44EM-TG7DL-CJ2ET','completed','cdkey',NULL,NULL,'2026-06-03 09:18:22','2026-06-03 09:18:23'),('d4fc7582-51f6-436d-9f44-f98d6c9e2d6e','ORD78BD570940AB','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'9','神经鹅','/picture/games/神经鹅.jpg',22.98,1,22.98,'cdkey','标准版','L5G6G-G3604-X7X17','completed','cdkey','alipay',NULL,'2026-09-15 08:02:58','2026-09-15 08:02:58'),('d851933a-245f-4a5c-b3de-5d019b87d0c8','ORD17075B27E5B5','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'68','Don\'t Starve Together','/uploads/games/e7afdf88d6d341e99c2e0a589bd12b0b.jpg',48.00,1,48.00,'cdkey','标准版','78NEY-98H3H-TZF0W','completed','cdkey','alipay',NULL,'2026-10-20 11:04:53','2026-10-20 11:04:53'),('e0dd570f-52dc-44ba-be4a-4585616488b0','ORD147888D835FF','41773925-6164-4e8c-9dc6-be142038cf3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','7fbbdbf6-3480-41b3-8554-f04df5a060e1','5',NULL,NULL,42.00,1,42.00,NULL,NULL,'11111111111','completed','cdkey','balance',NULL,'2026-09-07 10:44:23','2026-09-07 10:44:23'),('e1eb3d7f-d4ec-42cb-beee-ebcd47fc0a81','ORDMPV60192','21e82751-fab1-4d8d-9958-745f1f6e1666',NULL,NULL,NULL,'三更','/picture/games/三更.jpg',4.38,1,4.38,'cdkey','标准版','7JL6P-LC072-9RKSP','completed','cdkey',NULL,NULL,'2026-06-01 12:08:19','2026-06-01 12:08:19'),('ef55f53b-e537-4cf5-9675-f05748a13f69','ORDMQ3G5QNS','f0f9424a-1963-4cb3-9c89-fa5461b04b0b',NULL,NULL,NULL,'东方奇缘记','/picture/games/东方奇缘记.jpg',2.50,1,2.50,'cdkey','标准版','QNU19-XL40X-ZB1QR','completed','cdkey',NULL,NULL,'2026-06-07 07:14:51','2026-06-07 07:14:51'),('f1c75439-6587-4133-8380-24ab7bd1bb36','ORDE9C800325E84','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-004','8','三更','/picture/games/三更.jpg',4.20,1,4.20,'gift','标准版',NULL,'completed','py',NULL,NULL,'2026-09-04 09:02:20','2026-09-04 09:02:20'),('f563d7dd-d94d-4812-98a6-f3589ac44f93','ORDB6AE356449EA','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','py-004','8','三更','/picture/games/三更.jpg',4.20,1,4.20,'cdkey','标准版',NULL,'completed','py','alipay',NULL,'2026-09-08 09:06:58','2026-09-08 09:06:58'),('f6753501-093d-4359-b401-27799b89dab1','ORD31189D33555B','f0f9424a-1963-4cb3-9c89-fa5461b04b0b',NULL,NULL,NULL,'高球王者','/picture/games/高球王者.jpg',12.90,1,12.90,'cdkey','标准版','9U1X0-4IFZC-GRGLY','completed','cdkey','alipay',NULL,'2026-09-02 09:59:20','2026-09-02 09:59:20'),('fdc81bbd-dfd5-4a01-b9f3-2c24965ac051','ORD4710BC8094DA','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,NULL,'71','Control','/uploads/games/f201b85001944bf28976e797dd5d9e81.jpg',158.00,1,158.00,'cdkey','标准版','PB0GJ-VE5TD-74B9U','completed','cdkey','alipay',NULL,'2026-10-21 10:11:52','2026-10-21 10:11:52');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_methods`
--

DROP TABLE IF EXISTS `payment_methods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_methods` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `method_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码: alipay/wechat/bank/balance',
  `method_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道显示名',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: recharge=充值 withdraw=提现 both=都支持',
  `fee_rate` decimal(6,4) DEFAULT '0.0000' COMMENT '手续费率 如 0.0060 = 0.6%',
  `min_fee` decimal(10,2) DEFAULT '0.00' COMMENT '最低手续费',
  `max_fee` decimal(10,2) DEFAULT '0.00' COMMENT '最高手续费',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用: 1=启用',
  `sort_order` int DEFAULT '0' COMMENT '排序，小的在前',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_method_code` (`method_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付渠道配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_methods`
--

LOCK TABLES `payment_methods` WRITE;
/*!40000 ALTER TABLE `payment_methods` DISABLE KEYS */;
INSERT INTO `payment_methods` VALUES ('pm-alipay','alipay','支付宝','both',0.0060,1.00,50.00,1,1,'扫码支付，实时到账','2026-10-08 09:18:24','2026-10-21 08:30:11'),('pm-balance','balance','平台余额','recharge',0.0000,0.00,0.00,1,4,'账户余额直接支付','2026-10-08 09:18:24','2026-10-20 11:31:33'),('pm-bank','bank','银行卡转账','withdraw',0.0100,1.00,50.00,1,3,'提现到银行卡，1-3个工作日','2026-10-08 09:18:24','2026-10-21 08:30:11'),('pm-wechat','wechat','微信支付','both',0.0060,1.00,50.00,0,2,'扫码支付，实时到账','2026-10-08 09:18:24','2026-10-21 08:30:11');
/*!40000 ALTER TABLE `payment_methods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refunds`
--

DROP TABLE IF EXISTS `refunds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refunds` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `refund_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款单号',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → orders.id',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联订单号',
  `buyer_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '买家用户ID',
  `seller_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '卖家用户ID',
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏名称（冗余）',
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏封面（冗余）',
  `amount` decimal(12,2) NOT NULL COMMENT '退款金额',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款理由',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `applied_at` datetime DEFAULT NULL COMMENT '申请时间',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `review_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  `admin_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '管理员备注',
  `admin_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理管理员',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refunds`
--

LOCK TABLES `refunds` WRITE;
/*!40000 ALTER TABLE `refunds` DISABLE KEYS */;
/*!40000 ALTER TABLE `refunds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `target_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报对象类型: review, reply, user, order',
  `target_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报对象 UUID',
  `reporter_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id（举报人）',
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '举报理由',
  `created_at` datetime DEFAULT NULL COMMENT '举报时间',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_reporter` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES ('47dcec32-b634-4ddb-a7a5-3baf26efb8df','review','9da8d6f5-cc64-4062-89ad-a54ca658dcff','41773925-6164-4e8c-9dc6-be142038cf3f','违法违规','2026-10-27 11:01:24'),('b312bf35-6b4f-4bed-8c40-364d105ae40f','review','e044df4b-c378-4618-ac46-5d5480186947','20ce5e5c-751b-405d-98f0-060baf9dffdd','1','2026-09-09 09:53:10');
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_likes`
--

DROP TABLE IF EXISTS `review_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_likes` (
  `review_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → reviews.id',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `created_at` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`review_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_likes`
--

LOCK TABLES `review_likes` WRITE;
/*!40000 ALTER TABLE `review_likes` DISABLE KEYS */;
INSERT INTO `review_likes` VALUES ('22d704ec-6744-4185-b856-8bed2655cd6e','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-09 10:35:40'),('22d704ec-6744-4185-b856-8bed2655cd6e','ef8caf9b-917a-4356-a7c6-1d01f9388b7e',NULL),('76323f2a-d705-4c99-8f91-e282680088ff','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-09 10:34:33'),('9da8d6f5-cc64-4062-89ad-a54ca658dcff','41773925-6164-4e8c-9dc6-be142038cf3f',NULL),('e4b6a628-63ec-4424-84fe-f106a8afbc1a','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-15 09:33:49'),('e4b6a628-63ec-4424-84fe-f106a8afbc1a','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-15 09:32:58');
/*!40000 ALTER TABLE `review_likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_replies`
--

DROP TABLE IF EXISTS `review_replies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_replies` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `review_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → reviews.id',
  `parent_reply_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自引用父回复 id，NULL=顶级',
  `reply_to_user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '楼中楼回复的目标用户',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id（回复者）',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容，上限 500 字',
  `report_count` int NOT NULL DEFAULT '0' COMMENT '被举报次数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=隐藏, 1=正常',
  `created_at` datetime DEFAULT NULL COMMENT '回复时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`),
  CONSTRAINT `fk_reply_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_replies`
--

LOCK TABLES `review_replies` WRITE;
/*!40000 ALTER TABLE `review_replies` DISABLE KEYS */;
INSERT INTO `review_replies` VALUES ('0442e61e-7268-405b-bce7-6932678234c8','76323f2a-d705-4c99-8f91-e282680088ff',NULL,NULL,'20ce5e5c-751b-405d-98f0-060baf9dffdd','?????',0,1,'2026-09-09 10:19:54','2026-09-09 10:19:54'),('26d9fc3d-0673-4d6e-bba5-6c437d945a18','76323f2a-d705-4c99-8f91-e282680088ff',NULL,NULL,'20ce5e5c-751b-405d-98f0-060baf9dffdd','test sub reply',0,1,'2026-09-09 11:26:40','2026-09-09 11:26:40'),('3284de0b-fdb9-4a01-b0dc-c4b4e444ba99','22d704ec-6744-4185-b856-8bed2655cd6e',NULL,NULL,'41773925-6164-4e8c-9dc6-be142038cf3f','好！',0,1,'2026-09-09 11:22:12','2026-09-10 09:25:16'),('434d794f-fbad-4c94-aaac-ac82ad1efba7','e4b6a628-63ec-4424-84fe-f106a8afbc1a',NULL,NULL,'41773925-6164-4e8c-9dc6-be142038cf3f','很没瘾了',0,1,'2026-09-15 10:02:24','2026-09-15 10:02:24'),('5e7bebf2-ca13-466e-aec6-92e257ced854','9da8d6f5-cc64-4062-89ad-a54ca658dcff',NULL,NULL,'41773925-6164-4e8c-9dc6-be142038cf3f','111',0,1,'2026-10-20 11:11:34','2026-10-20 11:11:34'),('75be97b8-4345-4e9d-8664-fea4330204f3','22d704ec-6744-4185-b856-8bed2655cd6e','c32acd55-5275-4e52-b220-86a560339c43','20ce5e5c-751b-405d-98f0-060baf9dffdd','41773925-6164-4e8c-9dc6-be142038cf3f','111',0,1,'2026-09-15 09:13:29','2026-09-15 09:13:29'),('779b07b7-1d06-476a-94d5-db1189b61bd6','9da8d6f5-cc64-4062-89ad-a54ca658dcff',NULL,NULL,'41773925-6164-4e8c-9dc6-be142038cf3f','实验',0,1,'2026-10-22 08:16:49','2026-10-22 08:16:49'),('a75ffeba-dcf5-4159-b4b3-454316499dda','e4b6a628-63ec-4424-84fe-f106a8afbc1a','c47bc1c3-3dd3-4373-a218-f2c44888e70e','41773925-6164-4e8c-9dc6-be142038cf3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','他都这么了，那你就信他呗',0,1,'2026-09-15 09:34:12','2026-09-15 09:34:12'),('b6f65a56-eb82-4e13-bf5b-fc0de6426a53','76323f2a-d705-4c99-8f91-e282680088ff',NULL,NULL,'41773925-6164-4e8c-9dc6-be142038cf3f','???? - 09/15/2026 09:41:57',0,1,'2026-09-15 09:41:58','2026-09-15 09:41:58'),('c32acd55-5275-4e52-b220-86a560339c43','22d704ec-6744-4185-b856-8bed2655cd6e','3284de0b-fdb9-4a01-b0dc-c4b4e444ba99','41773925-6164-4e8c-9dc6-be142038cf3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','可爱',0,1,'2026-09-09 11:23:03','2026-09-11 10:33:59'),('c47bc1c3-3dd3-4373-a218-f2c44888e70e','e4b6a628-63ec-4424-84fe-f106a8afbc1a',NULL,NULL,'41773925-6164-4e8c-9dc6-be142038cf3f','保真吗？？？',0,1,'2026-09-15 09:33:10','2026-09-15 10:00:54'),('c4eff85f-6ce7-4fd1-b7f2-ad5e1edf17e5','22d704ec-6744-4185-b856-8bed2655cd6e',NULL,NULL,'123456','这是一条测试回复内容',0,1,'2026-09-11 10:26:22','2026-09-11 10:26:22');
/*!40000 ALTER TABLE `review_replies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_reply_likes`
--

DROP TABLE IF EXISTS `review_reply_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_reply_likes` (
  `reply_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → review_replies.id',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `created_at` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`reply_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复点赞（多对多）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_reply_likes`
--

LOCK TABLES `review_reply_likes` WRITE;
/*!40000 ALTER TABLE `review_reply_likes` DISABLE KEYS */;
INSERT INTO `review_reply_likes` VALUES ('0442e61e-7268-405b-bce7-6932678234c8','41773925-6164-4e8c-9dc6-be142038cf3f',NULL),('3284de0b-fdb9-4a01-b0dc-c4b4e444ba99','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL),('5e7bebf2-ca13-466e-aec6-92e257ced854','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL),('c32acd55-5275-4e52-b220-86a560339c43','41773925-6164-4e8c-9dc6-be142038cf3f','2026-09-11 10:33:59'),('c47bc1c3-3dd3-4373-a218-f2c44888e70e','20ce5e5c-751b-405d-98f0-060baf9dffdd','2026-09-15 10:00:54');
/*!40000 ALTER TABLE `review_reply_likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → games.id',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `recommend` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1=推荐 0=不推荐',
  `content` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价正文，硬上限 2000 字',
  `images` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '逗号分隔本地路径，如 /uploads/reviews/xxx.jpg',
  `status` tinyint DEFAULT '1' COMMENT '审核状态: 0=待审, 1=通过, 2=拒绝',
  `replies_count` int NOT NULL DEFAULT '0' COMMENT '回复数（Counter Cache）',
  `report_count` int NOT NULL DEFAULT '0' COMMENT '被举报次数（Counter Cache）',
  `created_at` datetime DEFAULT NULL COMMENT '评价创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game` (`user_id`,`game_id`),
  KEY `idx_game` (`game_id`),
  CONSTRAINT `fk_reviews_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES ('22d704ec-6744-4185-b856-8bed2655cd6e','5','20ce5e5c-751b-405d-98f0-060baf9dffdd',1,'bakabaka','/uploads/reviews/7e244dccda0041159172e05e7e8fb682.jpg',1,4,0,'2026-09-08 10:35:22','2026-10-19 11:28:17'),('2a4dbd1f-24d3-4333-b0ec-a2f78eb93e32','11','20ce5e5c-751b-405d-98f0-060baf9dffdd',1,'test edit back 12345','/uploads/reviews/de32ffa67f1541ebaf6a4c61d7dcc688.png',1,0,0,'2026-09-08 10:33:49','2026-10-09 09:32:37'),('56b4605d-e475-44cb-9273-482a0c01c5fa','23','20ce5e5c-751b-405d-98f0-060baf9dffdd',1,'11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111','/uploads/reviews/5f1cfff9a74c48c98ab2e5678a921642.png',1,0,0,'2026-10-09 08:28:51','2026-10-09 10:04:33'),('76323f2a-d705-4c99-8f91-e282680088ff','5','41773925-6164-4e8c-9dc6-be142038cf3f',1,'七协会射命丸还不阴啊','/uploads/reviews/ea0058d9efda4ef79b76b0f55aefc991.jpg',1,3,0,'2026-09-09 09:53:37','2026-10-19 10:35:27'),('9da8d6f5-cc64-4062-89ad-a54ca658dcff','100','20ce5e5c-751b-405d-98f0-060baf9dffdd',1,'111111111','/uploads/reviews/8a0037acd6cf40dd8d2aa7d287f9be50.png',1,2,1,'2026-10-20 11:10:20','2026-10-21 08:12:33'),('e4b6a628-63ec-4424-84fe-f106a8afbc1a','67','b29de658-9260-44a6-91db-3733d9033965',1,'天天玩也没感觉有瘾啊','',1,3,0,'2026-09-15 09:31:16','2026-09-15 10:02:23'),('eea24c39-8e37-417a-b9f2-615c9f7b127b','9','20ce5e5c-751b-405d-98f0-060baf9dffdd',1,'更新后的测试评测内容','',1,0,0,'2026-09-08 10:29:48','2026-09-08 10:32:02'),('f41ed3e7-a9fa-4aea-a58c-d12fe30a1d82','71','20ce5e5c-751b-405d-98f0-060baf9dffdd',1,'zsbd啊','',1,0,0,'2026-10-21 10:12:11','2026-10-21 10:12:11');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seller_quota`
--

DROP TABLE IF EXISTS `seller_quota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_quota` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `seller_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id，UNIQUE',
  `quota` decimal(12,2) DEFAULT NULL COMMENT '总额度',
  `used` decimal(12,2) DEFAULT NULL COMMENT '已用额度',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seller_quota` (`seller_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seller_quota`
--

LOCK TABLES `seller_quota` WRITE;
/*!40000 ALTER TABLE `seller_quota` DISABLE KEYS */;
/*!40000 ALTER TABLE `seller_quota` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `steam_accounts`
--

DROP TABLE IF EXISTS `steam_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `steam_accounts` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `steam_id64` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam 平台 64 位 ID',
  `steam_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam 昵称',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam 头像 URL',
  `region` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam 地区',
  `level` int DEFAULT NULL COMMENT 'Steam 等级',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  `account_hash` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Steam 账号 hash（缓存验证用）',
  `game_count` int DEFAULT '0' COMMENT 'Steam 游戏库数量（缓存）',
  `account_value` decimal(12,2) DEFAULT '0.00' COMMENT '账号资产价值（缓存）',
  `playtime` int DEFAULT '0' COMMENT '总游戏时长（小时，缓存）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `steam_id64` (`steam_id64`),
  KEY `idx_steam_id64` (`steam_id64`),
  KEY `idx_account_hash` (`account_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='持久化的 Steam 模拟账号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `steam_accounts`
--

LOCK TABLES `steam_accounts` WRITE;
/*!40000 ALTER TABLE `steam_accounts` DISABLE KEYS */;
INSERT INTO `steam_accounts` VALUES ('1','740608853','123456_Steam','https://api.dicebear.com/7.x/adventurer/svg?seed=3a7f17158781','日本',72,'2026-09-08 09:46:18','2026-10-27 08:41:47','2ad1f898c6244ea73df551788fc40998',14,1217.56,374),('3','613008908','aaa_Master','https://api.dicebear.com/7.x/adventurer/svg?seed=491a898eb8c3fc9da6ecb69e745418fa','中国',70,NULL,'2026-10-20 11:17:40','491a898eb8c3fc9da6ecb69e745418fa',9,1257.90,26),('4','125929933','456_Steam','https://api.dicebear.com/7.x/adventurer/svg?seed=9e378ff702273e3ccc04f7c3edaa0ec4','英国',106,NULL,'2026-09-23 11:05:46','9e378ff702273e3ccc04f7c3edaa0ec4',3,312.00,8),('5','581989929','追风少年_Master','https://api.dicebear.com/7.x/adventurer/svg?seed=c01ecde1b13fe016931d5f14cfc278e6','新加坡',91,NULL,'2026-09-23 11:11:51','c01ecde1b13fe016931d5f14cfc278e6',4,732.00,14);
/*!40000 ALTER TABLE `steam_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `steam_libraries`
--

DROP TABLE IF EXISTS `steam_libraries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `steam_libraries` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `game_id` int DEFAULT NULL COMMENT 'Steam 平台 appid（⚠️ 不是本地 games.id！int 够 Steam 游戏 13 万款）',
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '游戏名称（Steam 源快照）',
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏图片 URL（快照）',
  `playtime` int DEFAULT '0' COMMENT '游戏时长（小时）',
  `steam_account_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → steam_accounts.id',
  PRIMARY KEY (`id`),
  KEY `idx_user_game` (`game_name`),
  KEY `fk_steamlib_account` (`steam_account_id`),
  CONSTRAINT `fk_steamlib_account` FOREIGN KEY (`steam_account_id`) REFERENCES `steam_accounts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Steam 账号拥有的游戏列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `steam_libraries`
--

LOCK TABLES `steam_libraries` WRITE;
/*!40000 ALTER TABLE `steam_libraries` DISABLE KEYS */;
INSERT INTO `steam_libraries` VALUES ('25',88,'Resident Evil Village','https://cdn.akamai.steamstatic.com/steam/apps/1196590/header.jpg',7,NULL),('26',96,'The Sims 4','https://cdn.akamai.steamstatic.com/steam/apps/1222670/header.jpg',2,NULL),('27',146,'Batman: Arkham City','https://cdn.akamai.steamstatic.com/steam/apps/200260/header.jpg',7,NULL),('28',16,'EA SPORTS FC 25','https://cdn.akamai.steamstatic.com/steam/apps/2195250/header.jpg',2,NULL),('29',10,'RollBot','../picture/RollBot.jpg',0,NULL),('2e4fe5f37bdd20d224994069ea20507a',NULL,'Age of Empires IV','https://cdn.akamai.steamstatic.com/steam/apps/1466810/header.jpg',26,'1'),('30',71,'Control','https://cdn.akamai.steamstatic.com/steam/apps/870780/header.jpg',2,NULL),('31',95,'Battlefield 2042','https://cdn.akamai.steamstatic.com/steam/apps/1517890/header.jpg',0,NULL),('32',9,'神经鹅','../picture/神经鹅.jpg',8,NULL),('3a0178810448f1f977b0b9147c39a3fe',NULL,'Control','https://cdn.akamai.steamstatic.com/steam/apps/870780/header.jpg',58,'1'),('41',5,'东方奇缘记','../picture/东方奇缘记.jpg',0,'1'),('42',23,'Hollow Knight','https://cdn.akamai.steamstatic.com/steam/apps/367520/header.jpg',0,'1'),('43',158,'Arma 3','https://cdn.akamai.steamstatic.com/steam/apps/107410/header.jpg',0,'1'),('44',8,'三更','../picture/三更.jpg',0,'1'),('46',1,'生化危机:安魂曲','test.jpg',0,'1'),('47',4,'你的另一个老婆','../picture/你的另一个老婆.jpg',0,'1'),('48',18,'Cyberpunk 2077','https://cdn.akamai.steamstatic.com/steam/apps/1091500/header.jpg',0,'1'),('4b320beb17fbcfc09f2e7191438e553a',NULL,'Don\'t Starve Together','https://cdn.akamai.steamstatic.com/steam/apps/322330/header.jpg',93,'1'),('54',1,'生化危机:安魂曲','../picture/安魂曲.jpg',5,'3'),('55',12,'高球王者','../picture/高球王者.jpg',5,'3'),('56',121,'No Man\'s Sky','https://cdn.akamai.steamstatic.com/steam/apps/275850/header.jpg',0,'3'),('57',10,'RollBot','../picture/RollBot.jpg',2,'3'),('58',64,'Hollow Knight: Silksong','https://cdn.akamai.steamstatic.com/steam/apps/1030300/header.jpg',5,'3'),('59',39,'Cyberpunk 2077: Ultimate Edition','https://cdn.akamai.steamstatic.com/steam/apps/1837420/header.jpg',3,'3'),('60',80,'Factorio','https://cdn.akamai.steamstatic.com/steam/apps/427520/header.jpg',6,'3'),('61',84,'Like a Dragon: Infinite Wealth','https://cdn.akamai.steamstatic.com/steam/apps/2256310/header.jpg',0,'3'),('62',138,'Far Cry 6','https://cdn.akamai.steamstatic.com/steam/apps/222490/header.jpg',0,'4'),('63',67,'Terraria','https://cdn.akamai.steamstatic.com/steam/apps/105600/header.jpg',1,'4'),('64',110,'Like a Dragon: Gaiden - The Man Who Erased His Name','https://cdn.akamai.steamstatic.com/steam/apps/2057730/header.jpg',6,'4'),('65',101,'Age of Empires II: Definitive Edition','https://cdn.akamai.steamstatic.com/steam/apps/813780/header.jpg',0,'3'),('66',9,'神经鹅','../picture/神经鹅.jpg',0,'1'),('67',151,'For Honour','https://cdn.akamai.steamstatic.com/steam/apps/304430/header.jpg',7,'5'),('68',161,'Ready or Not','https://cdn.akamai.steamstatic.com/steam/apps/1144200/header.jpg',2,'5'),('69',46,'Marvels Spider-Man Remastered','https://cdn.akamai.steamstatic.com/steam/apps/1811260/header.jpg',1,'5'),('70',111,'The Last of Us Part I','https://cdn.akamai.steamstatic.com/steam/apps/1888930/header.jpg',2,'5'),('74b490b4c2c3414d2a2cc45d6fc0aa46',NULL,'Deathloop','https://cdn.akamai.steamstatic.com/steam/apps/1252290/header.jpg',46,'1'),('8d73736c0ae403ae40883c2dcf909f86',NULL,'Cities: Skylines','https://cdn.akamai.steamstatic.com/steam/apps/255710/header.jpg',75,'1'),('8da1a962a5e91253e32056feeaecdfe1',NULL,'Assassin\'s Creed Odyssey','https://cdn.akamai.steamstatic.com/steam/apps/812140/header.jpg',76,'1');
/*!40000 ALTER TABLE `steam_libraries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `transaction_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流水号',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → users.id',
  `type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: recharge, consume, withdraw, refund',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题（展示用）',
  `subtitle` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '副标题/备注（展示用）',
  `amount` decimal(12,2) NOT NULL COMMENT '金额（元）',
  `balance_before` decimal(12,2) DEFAULT NULL COMMENT '交易前余额',
  `balance_after` decimal(12,2) DEFAULT NULL COMMENT '交易后余额',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态: pending, success, failed',
  `reference_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联对象类型: order, withdraw, recharge',
  `reference_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联对象 UUID',
  `created_at` datetime DEFAULT NULL COMMENT '交易时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_no` (`transaction_no`),
  KEY `idx_user` (`user_id`),
  CONSTRAINT `fk_txn_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES ('00011b04-7c77-45e9-b84b-b5079da5c4ac','TXNMPGL8S1K','21e82751-fab1-4d8d-9958-745f1f6e1666','purchase','购买 生化危机:安魂曲',NULL,-309.00,0.00,0.00,'completed','order',NULL,'2026-05-22 07:18:28'),('03c4dad8-86e8-4ca3-b682-32fe3686cb37','TXN996248A405CA','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 三更',NULL,4.20,54.80,59.00,'completed','order','f563d7dd-d94d-4812-98a6-f3589ac44f93','2026-09-08 09:06:58'),('04fd7afe-735f-4aa5-a79c-704a27a172ee','TXN814D5E2C5EFB','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 东方奇缘记',NULL,4.20,12.60,16.80,'completed','order','a8f92732-a172-4281-aa51-876fd9ac5018','2026-09-04 09:02:53'),('076d5bd3-6a41-410a-bab5-65de5bebf763','TXN9F325B4C8615','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Deathloop',NULL,-148.00,0.00,0.00,'completed','order','909a1e7d-5995-473e-b171-b94ed93af607','2026-10-12 09:53:09'),('13815d79-3773-4efd-823c-1701528fe47a','TXN9D019DC6B224',NULL,'purchase','购买 三更',NULL,-25.00,0.00,0.00,'completed','order','18276ea0-10ca-4eec-8397-5db929fe512b','2026-09-02 11:25:08'),('1444b1b2-cf75-4798-bbc5-34026d152ca0','TXNEC92B832AF41','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 三更',NULL,25.00,25.00,50.00,'completed','order','8a020c4c-21f4-4f08-9988-ac7cc131a3a7','2026-09-02 11:11:07'),('17f1a4b3-4d97-49ac-a544-5abc936ef5f9','TXNBCC57105E06E','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 神经鹅',NULL,-22.98,0.00,0.00,'completed','order','a7c05217-2068-4962-b9e5-1d400db36aa0','2026-09-08 09:17:33'),('1b4d9eef-ba1f-485d-9dc1-8ef680785d0f','TXN302FCE6345DB','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 三更',NULL,-4.20,0.00,0.00,'completed','order','f563d7dd-d94d-4812-98a6-f3589ac44f93','2026-09-08 09:06:58'),('2cc10f8f-5765-4c4d-89f1-3e17758c1366','TXNBBE7979A1CC4','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（0.6%）',NULL,-1.00,940.00,940.00,'completed','withdraw','f1f4ad546fb1d9c12c03c42cb931c2bc','2026-10-27 11:00:56'),('2ceeac09-c48c-4ac5-a1f3-f14975f483e1','TXN8845CDA8BE0B','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 三更（余额支付 ¥4.20）',NULL,-4.20,0.00,0.00,'completed','order','0d75de36-0971-49c4-a781-49afcfd38fea','2026-09-04 09:01:54'),('2d0a4a40-539b-4a6d-a616-5258996dc753','TXNC7972D44E0F9','20ce5e5c-751b-405d-98f0-060baf9dffdd','reversal','提现审核拒绝 — 余额退回',NULL,2.00,55.99,57.99,'completed','withdraw','885587162cfb401f05c49c0867de7ddb','2026-09-07 09:42:03'),('30914fa8-6ee3-4e2f-9cbc-648e04981303','TXN86588E8C0352','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 三更（余额支付 ¥4.20）',NULL,-4.20,0.00,0.00,'completed','order','f1c75439-6587-4133-8380-24ab7bd1bb36','2026-09-04 09:02:20'),('31b0a477-3bd6-4005-aec9-eb3be980a29d','TXN466A34D243F9','20ce5e5c-751b-405d-98f0-060baf9dffdd','withdraw','余额提现',NULL,-2.00,57.99,55.99,'failed','withdraw','885587162cfb401f05c49c0867de7ddb','2026-09-07 09:41:16'),('3233b6b9-dc66-4ac8-b631-257366a1dda8','TXN031715EF7DAC','41773925-6164-4e8c-9dc6-be142038cf3f','purchase','购买 东方奇缘记（余额支付 ¥42.00）',NULL,-58.00,0.00,0.00,'completed','order','282b4ce4-d75e-4c02-86f5-5ed5b4c0c2fd','2026-09-03 09:29:31'),('32aaf73a-e05b-4ca6-b273-4fdea90dea45','TXNE3DD14F10ADA','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（0.6%）',NULL,-0.60,1.20,1.20,'failed','withdraw','e57f4aa9167bbfdf3c6c0e075e7b31ee','2026-10-21 08:28:02'),('3343757a-b198-4965-8507-563f1940f3a2','TXN12A603200550','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 Age of Empires II: Definitive Edition',NULL,40.00,51.99,91.99,'completed','order','c3b9b98b-cba8-42ee-9167-07dcba252e3f','2026-09-11 11:32:04'),('34fe3ae4-c56b-4de6-8fed-3cd5add6a6e0','TXN9A9AC8BC3A3B','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 三更',NULL,25.00,0.00,25.00,'completed','order','4e6815bd-66d7-46c0-ae3b-1bd055a3f04b','2026-09-02 10:47:53'),('35dd0fc2-123e-4a71-84f1-89774618c0dd','TXNMPV6024A','21e82751-fab1-4d8d-9958-745f1f6e1666','purchase','购买 三更',NULL,-4.38,0.00,0.00,'completed','order',NULL,'2026-06-01 12:08:20'),('375c8ce2-8cf2-4441-adf4-5141b6193ee6','TXN5A93D6B83EC7','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买游戏：????',NULL,-9.99,0.00,0.00,'completed','order','6dc86e25-6d21-41b2-b089-82d21e001a9d','2026-09-02 09:57:25'),('37a02241-f87b-44a7-8d28-e607c60d2498','TXN6D4C9C648942','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 Age of Empires IV',NULL,4.20,59.00,63.20,'completed','order','7cae3a75-1286-4c11-90a7-c3d5487ee433','2026-10-20 11:05:17'),('3dd2947c-1969-41b0-bdd9-6ece5e37b44b','TXNMPGF5J0B',NULL,'purchase','购买 Fullbright Pres',NULL,-6.00,0.00,0.00,'completed','order',NULL,'2026-05-22 04:27:59'),('3ded9e45-ea2f-4539-a7d5-331c31fccbb1','TXN3E2186F3993F','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 东方奇缘记',NULL,58.00,75.00,133.00,'completed','order','282b4ce4-d75e-4c02-86f5-5ed5b4c0c2fd','2026-09-03 09:29:31'),('408659d8-0ccb-4fb8-a7ce-a9bff743cc11','TXN15FF6277EA1D','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Cyberpunk 2077',NULL,-149.00,0.00,0.00,'completed','order','7e94426c-e9b3-4056-87ae-aff37e895f07','2026-09-07 08:12:34'),('410e6eae-5afd-4ef3-84d6-4acec0f36aec','TXNMQ3G28O2','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 生化危机:安魂曲',NULL,-309.00,0.00,0.00,'completed','order',NULL,'2026-06-07 07:12:07'),('427d7288-6770-4bd7-9290-35beca182907','TXN147A60A11C8D','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（0.6%）',NULL,-0.60,3.20,3.20,'failed','withdraw','bb3d9f87cd3e73d97a9b8c10a4af0a33','2026-10-21 08:14:19'),('434f7f31-e2d2-4557-95cb-0a0a79a79bf8','TXN354CED23EECC','41773925-6164-4e8c-9dc6-be142038cf3f','reversal','提现审核拒绝 — 余额退回',NULL,10.00,0.20,10.20,'completed','withdraw','bb3d9f87cd3e73d97a9b8c10a4af0a33','2026-10-21 08:42:49'),('47ea6b1f-b9de-48e7-bc84-d5965dff7e5a','TXN91D7EDBBD3B8','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Don\'t Starve Together',NULL,-48.00,0.00,0.00,'completed','order','d851933a-245f-4a5c-b3de-5d019b87d0c8','2026-10-20 11:04:53'),('4853218f-b4c9-4bae-b57f-d5e6b9e21a04','TXN8F75266ED98C','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（1%）',NULL,-1.00,100.00,100.00,'completed','withdraw',NULL,'2026-09-02 11:08:13'),('4b8f987b-4033-4779-a907-49c0f1ad144f','TXNF18B0C342558','20ce5e5c-751b-405d-98f0-060baf9dffdd','fee','提现手续费（1%）',NULL,-0.02,6.20,6.20,'completed','withdraw',NULL,'2026-09-04 09:45:50'),('4e0979a5-4c9d-48ee-8aa7-4c2831af6738','TXNC8408BED6DFB','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 ????',NULL,-88.88,0.00,0.00,'completed','order','8a7f7067-a625-4726-aaca-a6b69f6602a4','2026-09-02 10:02:29'),('4e644357-2fb9-4506-b37e-d1796d167faf','TXNE9BC742C77FF','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 三更（余额支付 ¥4.20）',NULL,-4.20,0.00,0.00,'completed','order','a501925f-a426-4868-a7e1-7be44eeb792f','2026-09-04 09:02:10'),('4ecc63a9-3e0e-4aa3-908e-270b2cdb12bf','TXND801883BAA25',NULL,'purchase','购买 三更',NULL,-25.00,0.00,0.00,'completed','order','02896579-0894-496e-a029-bd9a994f1579','2026-09-02 10:32:59'),('4f266ec3-5cd9-42b9-9240-4c212483382e','TXNF86CF9853971','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 null',NULL,42.00,57.99,99.99,'completed','order','e0dd570f-52dc-44ba-be4a-4585616488b0','2026-09-07 10:44:23'),('4f6b9b19-1d2a-49dc-acd7-d3b16a58467b','TXNMQ3G5RNT','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','purchase','购买 东方奇缘记',NULL,-2.50,0.00,0.00,'completed','order',NULL,'2026-06-07 07:14:52'),('56934ecc-45c5-483e-9870-0c92a9b85434','TXNMQ3G5RBN','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','purchase','购买 东方奇缘记',NULL,-2.50,0.00,0.00,'completed','order',NULL,'2026-06-07 07:14:51'),('570cf86e-e7a3-4cb4-b6cd-ff80e0520ad4','TXNA2B818C66EC9','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Age of Empires IV',NULL,-4.20,0.00,0.00,'completed','order','7cae3a75-1286-4c11-90a7-c3d5487ee433','2026-10-20 11:05:17'),('57d84595-0617-4f71-959d-2c242fed1b03','TXNMTGNEBFC','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 东方奇缘记',NULL,-2.50,0.00,0.00,'completed','order',NULL,'2026-08-31 02:57:35'),('57f808f1-43a3-4699-aa35-378241427e4e','TXN177D4B31F9CC','20ce5e5c-751b-405d-98f0-060baf9dffdd','fee','提现手续费（1%）',NULL,-0.70,8.20,8.20,'completed','withdraw',NULL,'2026-09-04 09:45:39'),('60ed9c3e-e7a9-45b0-a29a-221de869da4e','TXN944A56378E78','21e82751-fab1-4d8d-9958-745f1f6e1666','purchase','购买 ???',NULL,-22.98,0.00,0.00,'completed','order','57cffc41-b8ea-41d0-9d85-0b645125a0d4','2026-09-08 09:23:58'),('63df9798-96b1-4f9c-bcc4-74a0c5596779','TXNMPGLAITD','21e82751-fab1-4d8d-9958-745f1f6e1666','purchase','购买 东方奇缘记',NULL,-2.50,0.00,0.00,'completed','order',NULL,'2026-05-22 07:19:50'),('63e4b568-defc-49ad-aa55-daffa33f303d','TXNMQ3G643G','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','purchase','购买 三更',NULL,-4.38,0.00,0.00,'completed','order',NULL,'2026-06-07 07:15:08'),('6bd6a65f-d816-4d4a-88d5-bac99e251d42','TXN0FADB8AB3E7F','41773925-6164-4e8c-9dc6-be142038cf3f','reversal','提现审核拒绝 — 余额退回',NULL,2.00,10.20,12.20,'completed','withdraw','e57f4aa9167bbfdf3c6c0e075e7b31ee','2026-10-21 08:42:52'),('7214e6b5-feff-4e9c-a81c-e7390fb167bf','TXND8EDAFB3BB32','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-2.00,3.20,1.20,'failed','withdraw','e57f4aa9167bbfdf3c6c0e075e7b31ee','2026-10-21 08:28:02'),('7454c206-ccf4-4a22-be42-15fcb5ff4292','TXN3818BB67D9CD','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 三更',NULL,4.20,0.00,4.20,'completed','order','0d75de36-0971-49c4-a781-49afcfd38fea','2026-09-04 09:01:54'),('74ea6b41-2d6b-4395-9c86-b2050c8ef9b5','TXNBA4B3596503A','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 A Way Out',NULL,77.99,0.00,77.99,'completed','order','3e8f0497-c0b6-4803-a02c-c1b42ae4c9c1','2026-09-07 09:08:22'),('76a13e0e-c51d-4d0a-9b5b-27cb651c832f','TXN92936FE390FE','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Cities: Skylines',NULL,-39.00,0.00,0.00,'completed','order','1ba7af10-bf87-453d-af79-4812ed31f8ff','2026-10-15 09:27:48'),('77a55a0d-0040-4361-9344-7d23db73429d','TXNMPUYANI6','21e82751-fab1-4d8d-9958-745f1f6e1666','purchase','购买 生化危机:安魂曲 豪华版',NULL,-354.00,0.00,0.00,'completed','order',NULL,'2026-06-01 08:32:37'),('7aaeb49e-5061-45b4-8cda-50f3d85b4ffa','TXN3324F421E918','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Hollow Knight（余额支付 ¥48.00）',NULL,-48.00,0.00,0.00,'completed','order','ace24637-395e-4a1f-afc1-b48d2732379e','2026-09-10 08:10:05'),('7c418ba1-c85c-4c79-903d-e7504b3f5af5','TXN31FEDB783845','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（0.6%）',NULL,-1.00,900.00,900.00,'failed','withdraw','6f39c7ff45108ec0ddb58c2c2790c391','2026-10-21 08:58:47'),('7c99c893-9658-44d9-a395-b56a17059ccc','TXN1F264085B152','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 三更',NULL,4.20,4.20,8.40,'completed','order','a501925f-a426-4868-a7e1-7be44eeb792f','2026-09-04 09:02:10'),('7f9bc275-cd45-4a02-b224-88182aac9b7e','TXNC323683B830B','41773925-6164-4e8c-9dc6-be142038cf3f','purchase','购买 null',NULL,-42.00,0.00,0.00,'completed','order','e0dd570f-52dc-44ba-be4a-4585616488b0','2026-09-07 10:44:23'),('83b7280c-31a3-4432-9177-b5f494d2fef0','TXNMQ3GD1FI','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','purchase','购买 东方奇缘记',NULL,-2.50,0.00,0.00,'completed','order',NULL,'2026-06-07 07:20:31'),('83c8cd98-8516-4873-9998-48c8bf652aab','TXN222F744CA0AE','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 你的另一个老婆',NULL,38.00,16.80,54.80,'completed','order','7b449627-4b13-4d63-9628-4437eea536af','2026-09-04 09:03:12'),('854d7dda-ba4b-48c8-bd27-ccc1a9d47b38','TXNC1CB0343C38A','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 东方奇缘记',NULL,-1.88,0.00,0.00,'completed','order','1424bf6c-924e-4db9-8c34-d1118305c9af','2026-09-04 08:30:28'),('8ce091dd-946e-418f-b2fa-fc60e73b28d3','TXN6874F2A71F33','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 ???',NULL,-22.98,0.00,0.00,'completed','order','3c22e6c8-039b-48ba-a397-728da41fe4f5','2026-09-08 09:49:14'),('91fb148f-a921-4e11-9382-20c50fb99191','TXN874CC1084E08','20ce5e5c-751b-405d-98f0-060baf9dffdd','withdraw','余额提现',NULL,-2.00,8.20,6.20,'completed','withdraw',NULL,'2026-09-04 09:45:50'),('96e9be14-d069-42b0-98cf-2cec4f0b69ac','TXN7F6F960E0559','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 神经鹅',NULL,-22.98,0.00,0.00,'completed','order','bf43087f-49fb-45b6-be1a-3c71688710eb','2026-09-08 09:17:47'),('a1671999-0834-402f-abcb-eaa2bb407a33','TXN493628E6AD53','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（0.6%）',NULL,-1.00,990.00,990.00,'completed','withdraw','48937bec4422bd4e42a5c1be838ae590','2026-10-21 09:11:41'),('a69281d5-a608-4f9b-bd11-27f6a575202b','TXN06A530EB28FC','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','purchase','购买游戏：高球王者',NULL,-12.90,0.00,0.00,'completed','order','f6753501-093d-4359-b401-27799b89dab1','2026-09-02 09:59:20'),('acb67205-ef5b-4eda-aca4-45edc1a54aea','TXN58788CA729DA','41773925-6164-4e8c-9dc6-be142038cf3f','purchase','购买 Age of Empires II: Definitive Edition',NULL,-40.00,0.00,0.00,'completed','order','c3b9b98b-cba8-42ee-9167-07dcba252e3f','2026-09-11 11:32:04'),('b1acdcd0-afce-4061-a4cc-5e374674f4a7','TXNMPXUT7NQ','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 生化危机:安魂曲',NULL,-309.00,0.00,0.00,'completed','order',NULL,'2026-06-03 09:18:23'),('b25731e2-53bc-4cbe-8753-04ed88d32fbb','TXN780459A03730','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 东方奇缘记（余额支付 ¥4.20）',NULL,-4.20,0.00,0.00,'completed','order','a8f92732-a172-4281-aa51-876fd9ac5018','2026-09-04 09:02:53'),('b3253a7d-ccd5-4b82-883b-923b8bdaa314','TXN2257504F4FC6','41773925-6164-4e8c-9dc6-be142038cf3f','reversal','提现审核拒绝 — 余额退回',NULL,100.00,900.00,1000.00,'completed','withdraw','6f39c7ff45108ec0ddb58c2c2790c391','2026-10-21 09:01:21'),('b3310e84-322f-4e7d-86c0-28a9af7b0ddf','TXNC31E44034280','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-10.00,13.20,3.20,'failed','withdraw','bb3d9f87cd3e73d97a9b8c10a4af0a33','2026-10-21 08:14:19'),('b755f5fe-7f0c-42c6-8c50-c018c61fd649','TXNCFFD54FCB23E','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-100.00,200.00,100.00,'completed','withdraw',NULL,'2026-09-02 11:08:13'),('b933d559-d999-44d4-a099-e576d26ce1e9','TXN529221A78070','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 ???',NULL,-22.98,0.00,0.00,'completed','order','f661e761-8984-4078-af8c-0d17b728c29c','2026-09-08 09:23:43'),('bcb060c9-8f48-4872-9411-1678542389b5','TXN7B47765BB3DC','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 神经鹅',NULL,-22.98,0.00,0.00,'completed','order','d4fc7582-51f6-436d-9f44-f98d6c9e2d6e','2026-09-15 08:02:58'),('bec37226-a889-43cb-bff6-2e62bc402197','TXNFA9747FC78CA',NULL,'purchase','购买 三更',NULL,-25.00,0.00,0.00,'completed','order','4e6815bd-66d7-46c0-ae3b-1bd055a3f04b','2026-09-02 10:47:53'),('c2d41d33-7bbb-45ac-958f-34f74aa5d3c5','TXN622F781B829B','20ce5e5c-751b-405d-98f0-060baf9dffdd','withdraw','余额提现',NULL,-70.00,78.20,8.20,'completed','withdraw',NULL,'2026-09-04 09:45:39'),('c3cfc679-85d8-4fa8-9a9d-aa04ad2906d9','TXN04B0AA9A6180',NULL,'purchase','购买 三更',NULL,-25.00,0.00,0.00,'completed','order','8a020c4c-21f4-4f08-9988-ac7cc131a3a7','2026-09-02 11:11:07'),('c81803d7-4201-4816-96a9-a72dcd23147e','TXN4D811CC098FF','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Assassin\'s Creed Odyssey',NULL,-88.00,0.00,0.00,'completed','order','5fe4f026-3e88-45c3-9386-07641e9576d7','2026-10-15 10:04:34'),('ca079e2c-b095-46f4-b5f6-375ce03e75cf','TXN006E0ABE19CC','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-50.00,990.00,940.00,'completed','withdraw','f1f4ad546fb1d9c12c03c42cb931c2bc','2026-10-27 11:00:56'),('ca57cae4-626d-4fe0-ae51-e26d99469dda','TXNEE76C5123E71','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-100.00,1000.00,900.00,'failed','withdraw','6f39c7ff45108ec0ddb58c2c2790c391','2026-10-21 08:58:47'),('cbe7dcfe-d240-4639-81c5-4895c0ca833c','TXNBF3D995A01E5','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Arma 3',NULL,-88.00,0.00,0.00,'completed','order','67685c60-3d4b-4c28-afe4-e3c3188779e3','2026-09-10 08:23:12'),('d10aded0-10c9-4109-bf29-8cdd04b85bd8','TXNMPGFK53E',NULL,'purchase','购买 Fullbright Pres',NULL,-6.00,0.00,0.00,'completed','order',NULL,'2026-05-22 04:39:21'),('d43b0525-6626-4559-ae85-d39bfc770f6f','TXN4306EED5CAB2','20ce5e5c-751b-405d-98f0-060baf9dffdd','fee','提现手续费（1%）',NULL,-0.20,57.99,57.99,'completed','withdraw',NULL,'2026-09-07 09:10:11'),('d4bf71bf-a029-49b9-932a-c0b2be1b917c','TXN03D5BD05961F','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 东方奇缘记',NULL,-2.50,0.00,0.00,'completed','order','a42c4d82-ffb6-4986-8686-39b966ca2647','2026-09-08 10:27:23'),('de581fc9-a6ff-42fb-9b23-bd327d10d869','TXNDE7CD112DF76','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-10.00,1000.00,990.00,'completed','withdraw','48937bec4422bd4e42a5c1be838ae590','2026-10-21 09:11:41'),('deb43811-2772-43ba-a43a-68f3e8f38a22','TXNMPGFV44V',NULL,'purchase','购买 生化危机:安魂曲',NULL,-309.00,0.00,0.00,'completed','order',NULL,'2026-05-22 04:47:53'),('e260eb04-f703-4261-99d2-fe73ffa374e9','TXN6BD45681EF40','41773925-6164-4e8c-9dc6-be142038cf3f','sale','售出 三更',NULL,4.20,8.40,12.60,'completed','order','f1c75439-6587-4133-8380-24ab7bd1bb36','2026-09-04 09:02:20'),('e3738ca6-5aa0-413d-a35c-6796bae3ec60','TXNCF207F7AE16C','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 Control',NULL,-158.00,0.00,0.00,'completed','order','fdc81bbd-dfd5-4a01-b9f3-2c24965ac051','2026-10-21 10:11:52'),('e465f65c-b40f-4d33-a3f0-d8b48bdea7cf','TXNDB077CD7011D','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 你的另一个老婆（余额支付 ¥38.00）',NULL,-38.00,0.00,0.00,'completed','order','7b449627-4b13-4d63-9628-4437eea536af','2026-09-04 09:03:12'),('e4a6029a-4399-4961-843c-d964f6e10b4c','TXN2B3692CFB6E0','41773925-6164-4e8c-9dc6-be142038cf3f','fee','提现手续费（0.6%）',NULL,-1.00,0.20,0.20,'completed','withdraw','31b4b41f24cbe3a77afb697eb8c0f69d','2026-10-21 08:42:10'),('e64f381e-8f00-4894-8377-1caa76ef6895','TXN3740D3F5EF94','20ce5e5c-751b-405d-98f0-060baf9dffdd','sale','售出 三更',NULL,25.00,50.00,75.00,'completed','order','18276ea0-10ca-4eec-8397-5db929fe512b','2026-09-02 11:25:09'),('e836a952-b671-47b7-80a4-de0ef76515a2','TXN783353EF47B5','41773925-6164-4e8c-9dc6-be142038cf3f','purchase','购买 A Way Out',NULL,-77.99,0.00,0.00,'completed','order','3e8f0497-c0b6-4803-a02c-c1b42ae4c9c1','2026-09-07 09:08:22'),('e8ed472d-5a34-4e26-bb5b-522b26096088','TXN1C1ACF493389','41773925-6164-4e8c-9dc6-be142038cf3f','withdraw','余额提现',NULL,-1.00,1.20,0.20,'completed','withdraw','31b4b41f24cbe3a77afb697eb8c0f69d','2026-10-21 08:42:10'),('e9654633-50f4-4a64-b142-765f7adcaa30','TXN41A28688915B','20ce5e5c-751b-405d-98f0-060baf9dffdd','fee','提现手续费（1%）',NULL,-0.02,55.99,55.99,'failed','withdraw','885587162cfb401f05c49c0867de7ddb','2026-09-07 09:41:16'),('eac7dc8d-806e-426e-91cb-d009da5548bd','TXNEE4FB12C94EE','41773925-6164-4e8c-9dc6-be142038cf3f','recharge','账户充值',NULL,200.00,0.00,200.00,'completed','recharge',NULL,'2026-09-02 11:08:13'),('fa549766-1100-4a65-a423-f506dcb89367','TXNC0EF5780BB4A','20ce5e5c-751b-405d-98f0-060baf9dffdd','withdraw','余额提现',NULL,-20.00,77.99,57.99,'completed','withdraw',NULL,'2026-09-07 09:10:11'),('fabe8f33-4865-4736-a550-dab038971e9e','TXNMQ4Z5MM3','20ce5e5c-751b-405d-98f0-060baf9dffdd','purchase','购买 三更',NULL,-4.38,0.00,0.00,'completed','order',NULL,'2026-06-08 08:54:24');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_games`
--

DROP TABLE IF EXISTS `user_games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_games` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → users.id',
  `order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源订单 id',
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → games.id',
  `game_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏名称（购买快照）',
  `game_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '游戏图片（购买快照）',
  `cdkey` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'CDKey 快照',
  `version` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '版本快照',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态: active, revoked',
  `purchase_date` datetime DEFAULT NULL COMMENT '购买日期',
  `source` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源: order, steam',
  `activation_date` datetime DEFAULT NULL COMMENT '激活日期',
  `created_at` datetime DEFAULT NULL COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `fk_usergame_game` (`game_id`),
  CONSTRAINT `fk_usergame_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_usergame_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_games`
--

LOCK TABLES `user_games` WRITE;
/*!40000 ALTER TABLE `user_games` DISABLE KEYS */;
INSERT INTO `user_games` VALUES ('0364213b-9010-41c0-b407-fc97db801f3e','20ce5e5c-751b-405d-98f0-060baf9dffdd','bf43087f-49fb-45b6-be1a-3c71688710eb','9','神经鹅','../picture/神经鹅.jpg','F13ZX-GLTQE-VI23C','标准版','pending','2026-09-08 09:17:46','cdkey',NULL,NULL),('07df03dc-049f-43f4-8eb1-5e71c6dffcda','20ce5e5c-751b-405d-98f0-060baf9dffdd','50ef112c-0915-4426-9626-e741aa1413cc',NULL,'东方奇缘记','../picture/东方奇缘记.jpg','GKVOI-WYXL6-HOR5W','标准版','activated','2026-08-31 02:57:36','store','2026-09-02 10:33:25','2026-08-31 02:57:36'),('0b45407c-489e-4200-9a9d-7f55d2551031','21e82751-fab1-4d8d-9958-745f1f6e1666','adb48b47-997d-4383-bf9d-edceff81da00',NULL,'生化危机:安魂曲','../picture/安魂曲.jpg','HMTGA-YKAH7-HS8P5','标准版','pending','2026-05-22 07:18:29','store',NULL,'2026-05-22 07:18:32'),('0baaf27a-fb6d-4639-80ef-c6681164435b','20ce5e5c-751b-405d-98f0-060baf9dffdd','3c22e6c8-039b-48ba-a397-728da41fe4f5','9','???',NULL,'DUP-TEST-XXXX','标准版','pending','2026-09-08 09:49:13','cdkey',NULL,NULL),('0c9ae1df-0a83-4e9a-85ed-07aad28b8355','20ce5e5c-751b-405d-98f0-060baf9dffdd','1ba7af10-bf87-453d-af79-4812ed31f8ff','98','Cities: Skylines','https://cdn.akamai.steamstatic.com/steam/apps/255710/header.jpg','0H55I-A49T2-4NR82','标准版','pending','2026-10-15 09:27:48','cdkey',NULL,NULL),('1bce639b-a4b5-4dbf-a489-ae647ce544d5',NULL,'82d84fda-cf62-48b5-b9b9-f9751c2a1fc4',NULL,'生化危机:安魂曲','../picture/安魂曲.jpg','OXATV-4RI3F-EDEVO','标准版','pending','2026-05-22 04:47:53','store',NULL,'2026-05-22 04:47:56'),('1c4905ca-f253-4699-8d5c-9895a848e296','20ce5e5c-751b-405d-98f0-060baf9dffdd','67685c60-3d4b-4c28-afe4-e3c3188779e3','158','Arma 3','https://cdn.akamai.steamstatic.com/steam/apps/107410/header.jpg','BRNY8-NPGRP-OLBF6','标准版','pending','2026-09-10 08:23:12','cdkey',NULL,NULL),('1f60b6c8-7fa6-424f-bd77-0d1933b874d4','20ce5e5c-751b-405d-98f0-060baf9dffdd','ccd45fd3-84f9-4624-b3fa-6c9f82f66e52',NULL,'生化危机:安魂曲','../picture/安魂曲.jpg','UXX4K-NLHBU-P2GK0','标准版','pending','2026-06-07 07:12:08','store',NULL,'2026-06-07 07:12:08'),('292f54d3-72b1-4cc0-854d-0dd4f197e671','41773925-6164-4e8c-9dc6-be142038cf3f','282b4ce4-d75e-4c02-86f5-5ed5b4c0c2fd','5','东方奇缘记','../picture/东方奇缘记.jpg','EEE5-FFF6-GGG7-HHH8','标准版','pending','2026-09-03 09:29:31','cdkey',NULL,NULL),('330d022f-c592-48e0-8679-437f555cf404','20ce5e5c-751b-405d-98f0-060baf9dffdd','d4fc7582-51f6-436d-9f44-f98d6c9e2d6e','9','神经鹅','../picture/神经鹅.jpg','L5G6G-G3604-X7X17','标准版','pending','2026-09-15 08:02:58','cdkey',NULL,NULL),('347736eb-0f42-4748-8d9c-a5b5c9d3dc1d','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','ef55f53b-e537-4cf5-9675-f05748a13f69',NULL,'东方奇缘记','../picture/东方奇缘记.jpg','QNU19-XL40X-ZB1QR','标准版','pending','2026-06-07 07:14:52','store',NULL,'2026-06-07 07:14:52'),('37534bf8-8462-4412-8c27-af328f8c4759','20ce5e5c-751b-405d-98f0-060baf9dffdd','d851933a-245f-4a5c-b3de-5d019b87d0c8','68','Don\'t Starve Together','https://cdn.akamai.steamstatic.com/steam/apps/322330/header.jpg','78NEY-98H3H-TZF0W','标准版','pending','2026-10-20 11:04:53','cdkey',NULL,NULL),('3fe6dc1c-0954-4878-93b4-05e4ae11a593','20ce5e5c-751b-405d-98f0-060baf9dffdd','f661e761-8984-4078-af8c-0d17b728c29c','9','???',NULL,'TEST-TEST-TEST','标准版','pending','2026-09-08 09:23:42','cdkey',NULL,NULL),('40679322-c179-4ab3-8f8e-b880ab27891c','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','6f53b55d-9517-4708-9aa9-784fc34ff8c7',NULL,'三更','../picture/三更.jpg','ZI1EK-AEVAJ-QADHJ','标准版','pending','2026-06-07 07:15:08','store',NULL,'2026-06-07 07:15:09'),('43246d32-b260-44f9-9c27-354546c44292','20ce5e5c-751b-405d-98f0-060baf9dffdd','909a1e7d-5995-473e-b171-b94ed93af607','114','Deathloop','https://cdn.akamai.steamstatic.com/steam/apps/1252290/header.jpg','7B894-755Q8-XTK6T','标准版','pending','2026-10-12 09:53:09','cdkey',NULL,NULL),('4a954c43-296b-4e52-869c-566b2da7808d','41773925-6164-4e8c-9dc6-be142038cf3f','e0dd570f-52dc-44ba-be4a-4585616488b0','5',NULL,NULL,'11111111111','标准版','pending','2026-09-07 10:44:22','cdkey',NULL,NULL),('61806ae8-4c84-468e-8624-729691c1678f','20ce5e5c-751b-405d-98f0-060baf9dffdd','6dc86e25-6d21-41b2-b089-82d21e001a9d','1','????','test.jpg','TEST1-TEST2-TEST3','标准版','pending','2026-09-02 09:57:24','store',NULL,NULL),('659b6ba9-b05f-4e11-90fb-654d4b354531','41773925-6164-4e8c-9dc6-be142038cf3f','5a4c2831-dd6f-4997-981e-7dad01488dd9','5','东方奇缘记','../picture/东方奇缘记.jpg','','标准版','pending','2026-09-03 09:13:39','store',NULL,NULL),('6c124343-e019-4077-a9e7-66e866ab1c80','20ce5e5c-751b-405d-98f0-060baf9dffdd','44a2f391-549f-4a79-a1e6-06470f9d90cd',NULL,'三更','../picture/三更.jpg','FXOKH-CKEZD-A9PMY','标准版','pending','2026-06-08 08:54:25','store',NULL,'2026-06-08 08:54:25'),('6ebd8f78-5025-4b03-9d5a-723e3bbca320',NULL,'8a020c4c-21f4-4f08-9988-ac7cc131a3a7','1','三更','/picture/三更.jpg','FOLD-KEY-001-AABB','标准版','pending','2026-09-02 11:11:07','store',NULL,NULL),('6f195ced-5099-492c-a719-19f444c17556','41773925-6164-4e8c-9dc6-be142038cf3f','3e8f0497-c0b6-4803-a02c-c1b42ae4c9c1','26','A Way Out','https://cdn.akamai.steamstatic.com/steam/apps/578080/header.jpg','CU50-LFSQ-9UNW-H9PU','标准版','pending','2026-09-07 09:08:21','cdkey',NULL,NULL),('746fe642-0bf2-49c7-8a19-69412a702c3f','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','b7a4f34e-2b28-408e-8f18-0d8e7344bd50',NULL,'东方奇缘记','../picture/东方奇缘记.jpg','1WHHR-STP9J-00ISJ','标准版','pending','2026-06-07 07:14:52','store',NULL,'2026-06-07 07:14:52'),('7e1d52f7-a4db-465c-bef5-2103d0c3be43',NULL,'4e6815bd-66d7-46c0-ae3b-1bd055a3f04b','1','三更','/picture/三更.jpg','AAA1-BBB2-CCC3-DDD4','标准版','pending','2026-09-02 10:47:52','store',NULL,NULL),('85144abc-e3ea-4fd6-bc6e-0125422d6a7b','f0f9424a-1963-4cb3-9c89-fa5461b04b0b','f6753501-093d-4359-b401-27799b89dab1',NULL,'高球王者','../picture/高球王者.jpg','9U1X0-4IFZC-GRGLY','标准版','pending','2026-09-02 09:59:19','store',NULL,NULL),('8ad2d9b8-5d56-422c-88ff-6b9b857c9196','20ce5e5c-751b-405d-98f0-060baf9dffdd','7e94426c-e9b3-4056-87ae-aff37e895f07','18','Cyberpunk 2077','https://cdn.akamai.steamstatic.com/steam/apps/1091500/header.jpg','YXXXR-QLOFD-R0CHE','标准版','pending','2026-09-07 08:12:33','cdkey',NULL,NULL),('96e46e3d-f09e-4ea1-82d2-0e6fb5ecad52',NULL,'61df3c22-7a2a-4c25-87ba-f1d31a92cba0',NULL,'Fullbright Pres','../picture/Fullbright Pres.jpg','6GK0T-8VVE3-1TUNM','标准版','pending','2026-05-22 04:39:21','store',NULL,'2026-05-22 04:39:24'),('9b2df316-3691-49d1-88e8-9bf08f6f331c','20ce5e5c-751b-405d-98f0-060baf9dffdd','ace24637-395e-4a1f-afc1-b48d2732379e','23','Hollow Knight','https://cdn.akamai.steamstatic.com/steam/apps/367520/header.jpg','AV5SM-4KUVW-IRVLG','标准版','pending','2026-09-10 08:10:04','cdkey',NULL,NULL),('9c004e39-63b3-4504-9c18-280773280a86','20ce5e5c-751b-405d-98f0-060baf9dffdd','5fe4f026-3e88-45c3-9386-07641e9576d7','136','Assassin\'s Creed Odyssey','https://cdn.akamai.steamstatic.com/steam/apps/812140/header.jpg','1B740-DMM53-WX8FM','标准版','pending','2026-10-15 10:04:34','cdkey',NULL,NULL),('9e42dcb7-57e3-479c-b6e2-78380e7a5b49','20ce5e5c-751b-405d-98f0-060baf9dffdd','fdc81bbd-dfd5-4a01-b9f3-2c24965ac051','71','Control','https://cdn.akamai.steamstatic.com/steam/apps/870780/header.jpg','PB0GJ-VE5TD-74B9U','标准版','pending','2026-10-21 10:11:52','cdkey',NULL,NULL),('af3f8746-6d6a-491d-8444-c735bddc8968','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','cd3a0884-b678-4324-8f0d-6323c6db0a66',NULL,'东方奇缘记','../picture/东方奇缘记.jpg','YI29U-ELR9I-1B9SW','标准版','pending','2026-06-07 07:20:32','store',NULL,'2026-06-07 07:20:32'),('b1916a2f-8981-4b08-a0e2-ef6f7bee56af','21e82751-fab1-4d8d-9958-745f1f6e1666','139b6f21-44bb-4380-bc7b-600b8e0d7d1b',NULL,'生化危机:安魂曲 豪华版','../picture/安魂曲.jpg','UMDO6-97CGU-46I0I','豪华版','pending','2026-06-01 08:32:38','store',NULL,'2026-06-01 08:32:38'),('c0416d71-bec3-4083-8662-53ebd46b88df','21e82751-fab1-4d8d-9958-745f1f6e1666','e1eb3d7f-d4ec-42cb-beee-ebcd47fc0a81',NULL,'三更','../picture/三更.jpg','7JL6P-LC072-9RKSP','标准版','pending','2026-06-01 12:08:21','store',NULL,'2026-06-01 12:08:20'),('c5e08c1d-a234-4b96-8b2d-e8bae47ec888','20ce5e5c-751b-405d-98f0-060baf9dffdd','a7c05217-2068-4962-b9e5-1d400db36aa0','9','神经鹅','../picture/神经鹅.jpg','HWHE8-FATFV-Z88P5','标准版','pending','2026-09-08 09:17:32','cdkey',NULL,NULL),('d017bd96-56f7-4562-a506-14c40f0af1ff',NULL,'02896579-0894-496e-a029-bd9a994f1579','1','三更','/picture/三更.jpg','AAA1-BBB2-CCC3-DDD4','标准版','pending','2026-09-02 10:32:59','store',NULL,NULL),('da5ab15a-1189-482f-a056-1dc26b966abc',NULL,'18276ea0-10ca-4eec-8397-5db929fe512b','1','三更','/picture/三更.jpg','FOLD-KEY-002-CCDD','标准版','pending','2026-09-02 11:25:08','store',NULL,NULL),('dbe45909-6f49-47c3-ba3f-d6faa4653d72','21e82751-fab1-4d8d-9958-745f1f6e1666','57cffc41-b8ea-41d0-9d85-0b645125a0d4','9','???',NULL,'TEST-TEST-TEST','标准版','pending','2026-09-08 09:23:58','cdkey',NULL,NULL),('ddf160fd-14b7-43c4-839d-2072a0187f3f','20ce5e5c-751b-405d-98f0-060baf9dffdd','8a7f7067-a625-4726-aaca-a6b69f6602a4','1','????',NULL,'FMT-EST-TEST','标准版','pending','2026-09-02 10:02:29','store',NULL,NULL),('e989437b-e37e-4e94-990d-582a60782501','20ce5e5c-751b-405d-98f0-060baf9dffdd','d0925ecf-7444-40dd-af57-831cf32a6ba0',NULL,'生化危机:安魂曲','../picture/安魂曲.jpg','Z44EM-TG7DL-CJ2ET','标准版','pending','2026-06-03 09:18:24','store',NULL,'2026-06-03 09:18:24'),('eac817d3-b41b-4569-933e-cad22dbd3f19','21e82751-fab1-4d8d-9958-745f1f6e1666','a812fca9-e886-4c41-83e8-5cb3e985a2e6',NULL,'东方奇缘记','../picture/东方奇缘记.jpg','FAYGN-81QJ9-HWDJR','标准版','pending','2026-05-22 07:19:50','store',NULL,'2026-05-22 07:19:53'),('f1375d7e-b2c0-4040-bec6-24102d844a86','41773925-6164-4e8c-9dc6-be142038cf3f','4499ce4b-927a-423e-b52d-2ebe364d3ea3','5','东方奇缘记','','EEE5-FFF6-GGG7-HHH8','标准版','pending','2026-09-03 09:22:30','cdkey',NULL,NULL),('f2f00fea-6751-4404-877f-a174cec64fc6','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,'8','三更','/picture/三更.jpg','FOLD-KEY-003-EEFF','标准版','activated','2026-09-03 00:00:00','cdkey','2026-09-03 00:00:00','2026-09-03 11:20:42'),('fb45a688-6e8c-422f-a9ea-31bb060cf454','20ce5e5c-751b-405d-98f0-060baf9dffdd','a42c4d82-ffb6-4986-8686-39b966ca2647','5','东方奇缘记','../picture/东方奇缘记.jpg','W4U9Q-7113S-W31CQ','标准版','pending','2026-09-08 10:27:22','cdkey',NULL,NULL);
/*!40000 ALTER TABLE `user_games` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_notifications`
--

DROP TABLE IF EXISTS `user_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_notifications` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id（接收者）',
  `type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: reply, like_review, like_reply',
  `actor_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → users.id（触发者）',
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被操作对象类型: review, reply',
  `target_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被操作对象 UUID',
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联游戏 id',
  `content_snippet` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容摘要',
  `target_content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被操作对象内容快照',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '0=未读, 1=已读',
  `created_at` datetime DEFAULT NULL COMMENT '通知时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_notifications`
--

LOCK TABLES `user_notifications` WRITE;
/*!40000 ALTER TABLE `user_notifications` DISABLE KEYS */;
INSERT INTO `user_notifications` VALUES ('081f72ee-8df8-42c3-8142-c4a6ce22c01c','41773925-6164-4e8c-9dc6-be142038cf3f','like_reply','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','5e7bebf2-ca13-466e-aec6-92e257ced854','100','','111',1,'2026-10-22 08:12:18'),('0f576aa5-7763-48a1-8ee0-936afefdcdbd','20ce5e5c-751b-405d-98f0-060baf9dffdd','like_review','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','review','22d704ec-6744-4185-b856-8bed2655cd6e','5','','bakabaka [图片]',1,'2026-10-22 08:27:36'),('105d6aeb-3d72-41ef-8ecd-c3b1706087e0','b29de658-9260-44a6-91db-3733d9033965','reply','41773925-6164-4e8c-9dc6-be142038cf3f','review','e4b6a628-63ec-4424-84fe-f106a8afbc1a','67','保真吗？？？','天天玩也没感觉有瘾啊',1,'2026-09-15 09:33:10'),('162b7af1-04f4-475d-8de6-bc62feb981fb','c2a88892-acc8-11f1-ba45-e88088b0e710','system',NULL,'system','c2a88892-acc8-11f1-ba45-e88088b0e710',NULL,'您的账号已被封禁，购买、出售、充值、提现、评价等功能暂时无法使用；账户余额仍正常保留，解封后即可恢复全部功能。如有疑问请联系管理员。',NULL,0,'2026-10-27 11:07:13'),('17d285c4-efb2-44cd-986e-38c47c1a3a8f','20ce5e5c-751b-405d-98f0-060baf9dffdd','like_reply','41773925-6164-4e8c-9dc6-be142038cf3f','reply','c32acd55-5275-4e52-b220-86a560339c43','5','','可爱',1,'2026-09-11 10:33:59'),('1dff4fbb-bdaa-430e-9615-69af5fefd540','b29de658-9260-44a6-91db-3733d9033965','reply','41773925-6164-4e8c-9dc6-be142038cf3f','review','e4b6a628-63ec-4424-84fe-f106a8afbc1a','67','很没瘾了','天天玩也没感觉有瘾啊',1,'2026-09-15 10:02:23'),('245a1b9a-b78e-4fe6-aeaf-305d1d886d67','20ce5e5c-751b-405d-98f0-060baf9dffdd','like_review','41773925-6164-4e8c-9dc6-be142038cf3f','review','9da8d6f5-cc64-4062-89ad-a54ca658dcff','100','','111111111',1,'2026-10-22 08:12:18'),('297150ce-9822-4fc2-8d4f-83a4b6fb60ec','b29de658-9260-44a6-91db-3733d9033965','reply','20ce5e5c-751b-405d-98f0-060baf9dffdd','review','e4b6a628-63ec-4424-84fe-f106a8afbc1a','67','他都这么了，那你就信他呗','天天玩也没感觉有瘾啊',1,'2026-09-15 09:34:11'),('43d5ff96-52ec-4aab-9b1e-6526fa2eca2f','41773925-6164-4e8c-9dc6-be142038cf3f','like_review','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','review','76323f2a-d705-4c99-8f91-e282680088ff','5','','七协会射命丸还不阴啊',1,'2026-10-22 08:12:18'),('801b3528-0eb3-4f3f-9668-0242ac182f49','20ce5e5c-751b-405d-98f0-060baf9dffdd','system',NULL,'system','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,'您的账号已被封禁，购买、出售、充值、提现、评价等功能暂时无法使用；账户余额仍正常保留，解封后即可恢复全部功能。如有疑问请联系管理员。',NULL,1,'2026-10-26 11:03:58'),('8104d979-fcce-40a7-8f34-5de6238ce8d2','41773925-6164-4e8c-9dc6-be142038cf3f','like_reply','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','c47bc1c3-3dd3-4373-a218-f2c44888e70e','67','','保真吗？？？',1,'2026-09-15 10:00:54'),('99e3ebc9-2392-4ba8-9688-c446a3c7a56b','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','41773925-6164-4e8c-9dc6-be142038cf3f','review','9da8d6f5-cc64-4062-89ad-a54ca658dcff','100','111','111111111',1,'2026-10-22 08:12:18'),('9e7fc267-68e9-4fea-b354-1c40b983daf2','41773925-6164-4e8c-9dc6-be142038cf3f','reply','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','c47bc1c3-3dd3-4373-a218-f2c44888e70e','67','他都这么了，那你就信他呗','保真吗？？？',1,'2026-09-15 09:34:11'),('a0621f5d-94f8-4378-a970-e3124c1bd1aa','20ce5e5c-751b-405d-98f0-060baf9dffdd','like_reply','41773925-6164-4e8c-9dc6-be142038cf3f','reply','0442e61e-7268-405b-bce7-6932678234c8','5','','?????',1,'2026-10-22 08:12:18'),('a2c66eac-bb88-4636-a3a6-dee6ef735a02','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','123456','review','22d704ec-6744-4185-b856-8bed2655cd6e','5','这是一条测试回复内容','bakabaka',1,'2026-09-11 10:26:22'),('a756242f-3dcf-431f-857a-37cdaf64dd2b','b29de658-9260-44a6-91db-3733d9033965','like_review','41773925-6164-4e8c-9dc6-be142038cf3f','review','e4b6a628-63ec-4424-84fe-f106a8afbc1a','67','','天天玩也没感觉有瘾啊',1,'2026-09-15 09:32:58'),('a8318c11-e9e2-4dc8-a7d6-1b7e12a55d5b','20ce5e5c-751b-405d-98f0-060baf9dffdd','like_review','41773925-6164-4e8c-9dc6-be142038cf3f','review','9da8d6f5-cc64-4062-89ad-a54ca658dcff','100','','111111111',1,NULL),('aa204fef-1f4c-432e-9de0-bc32c226f808','20ce5e5c-751b-405d-98f0-060baf9dffdd','system',NULL,'system','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,'您的账号已被封禁，购买、出售、充值、提现、评价等功能暂时无法使用；账户余额仍正常保留，解封后即可恢复全部功能。如有疑问请联系管理员。',NULL,1,'2026-10-26 11:04:29'),('b12756e7-40fa-4fd8-b2ac-e1c2816a2426','b29de658-9260-44a6-91db-3733d9033965','like_review','20ce5e5c-751b-405d-98f0-060baf9dffdd','review','e4b6a628-63ec-4424-84fe-f106a8afbc1a','67','','天天玩也没感觉有瘾啊',1,'2026-09-15 09:33:49'),('bafd7f14-dfe8-4106-874c-c11fd8ba1f09','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','41773925-6164-4e8c-9dc6-be142038cf3f','review','9da8d6f5-cc64-4062-89ad-a54ca658dcff','100','实验','111111111',1,'2026-10-22 08:16:49'),('d5392d5b-1977-45b1-84bc-bd3137566f7e','41773925-6164-4e8c-9dc6-be142038cf3f','like_reply','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','3284de0b-fdb9-4a01-b0dc-c4b4e444ba99','5','','好！',1,'2026-10-22 08:12:18'),('dd7bd7ad-a58f-48a1-a50d-a5c5512d8c09','20ce5e5c-751b-405d-98f0-060baf9dffdd','system',NULL,'system','20ce5e5c-751b-405d-98f0-060baf9dffdd',NULL,'您的账号已被封禁，购买、出售、充值、提现、评价等功能暂时无法使用；账户余额仍正常保留，解封后即可恢复全部功能。如有疑问请联系管理员。',NULL,1,'2026-10-27 10:59:25'),('ea59b17f-f02a-48b8-b20a-d81bf3a58bbe','20ce5e5c-751b-405d-98f0-060baf9dffdd','reply','41773925-6164-4e8c-9dc6-be142038cf3f','review','22d704ec-6744-4185-b856-8bed2655cd6e','5','111','bakabaka',1,'2026-09-15 09:13:28'),('seed-like-reply-001','20ce5e5c-751b-405d-98f0-060baf9dffdd','like_reply','41773925-6164-4e8c-9dc6-be142038cf3f','reply','22d704ec-6744-4185-b856-8bed2655cd70','5',NULL,'这游戏还不错',1,'2026-09-11 08:32:18'),('seed-like-review-001','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','like_review','41773925-6164-4e8c-9dc6-be142038cf3f','review','22d704ec-6744-4185-b856-8bed2655cd6e','5','bakabaka',NULL,0,'2026-09-10 10:32:18'),('seed-reply-001','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','reply','41773925-6164-4e8c-9dc6-be142038cf3f','review','22d704ec-6744-4185-b856-8bed2655cd6e','5','好！',NULL,0,'2026-09-10 10:32:18'),('seed-reply-002','ef8caf9b-917a-4356-a7c6-1d01f9388b7e','reply','20ce5e5c-751b-405d-98f0-060baf9dffdd','review','22d704ec-6744-4185-b856-8bed2655cd6e','5','这款游戏我也玩过，画面确实不错','bakabaka',0,'2026-09-11 07:32:18');
/*!40000 ALTER TABLE `user_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_steam_bindings`
--

DROP TABLE IF EXISTS `user_steam_bindings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_steam_bindings` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `steam_account_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → steam_accounts.id',
  `bound_at` datetime DEFAULT NULL COMMENT '绑定时间',
  `is_current` tinyint(1) DEFAULT '1' COMMENT '是否当前活跃绑定: 1=是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_account` (`user_id`,`steam_account_id`),
  KEY `idx_steam_account` (`steam_account_id`),
  CONSTRAINT `fk_binding_account` FOREIGN KEY (`steam_account_id`) REFERENCES `steam_accounts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_binding_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与 Steam 账号多对多绑定';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_steam_bindings`
--

LOCK TABLES `user_steam_bindings` WRITE;
/*!40000 ALTER TABLE `user_steam_bindings` DISABLE KEYS */;
INSERT INTO `user_steam_bindings` VALUES ('0824db68681af458b4c6465fa0fdde50','41773925-6164-4e8c-9dc6-be142038cf3f','3',NULL,1),('1','b29de658-9260-44a6-91db-3733d9033965','4','2026-09-18 10:11:02',1),('3','20ce5e5c-751b-405d-98f0-060baf9dffdd','1','2026-09-08 09:46:18',1),('4','c2a72532-acc8-11f1-ba45-e88088b0e710','5',NULL,1);
/*!40000 ALTER TABLE `user_steam_bindings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_wallets`
--

DROP TABLE IF EXISTS `user_wallets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_wallets` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id，唯一',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '可用余额（元）',
  `frozen_balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额（提现处理中）',
  `total_recharged` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '历史累计充值（缓存，可刷新）',
  `total_spent` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '历史累计消费（缓存，可刷新）',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wallet_user` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表（每个用户一条，UNIQUE 约束 user_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_wallets`
--

LOCK TABLES `user_wallets` WRITE;
/*!40000 ALTER TABLE `user_wallets` DISABLE KEYS */;
INSERT INTO `user_wallets` VALUES ('0229cf72-9f31-425a-85ea-52d940a693fb','20ce5e5c-751b-405d-98f0-060baf9dffdd',0.00,0.00,0.00,0.00,'2026-06-03 08:31:41','2026-10-20 11:08:34'),('30eb4121-8bba-40fa-8ad2-81d47d6c6a19','1779421521687',0.00,0.00,0.00,0.00,'2026-05-22 04:27:38','2026-05-22 04:27:40'),('524b14ec-6503-458c-b0b2-bba2abd7e921','21e82751-fab1-4d8d-9958-745f1f6e1666',0.00,0.00,0.00,0.00,'2026-05-21 14:05:45','2026-05-21 14:05:45'),('64d5eed0-8c0b-4977-b2ed-fb92cb966fe0','41773925-6164-4e8c-9dc6-be142038cf3f',940.00,0.00,0.00,0.00,'2026-06-08 04:23:12','2026-10-27 11:00:56'),('7a4c0342-5b8a-4d8a-b77b-a509a16c4e1a','ef8caf9b-917a-4356-a7c6-1d01f9388b7e',0.00,0.00,0.00,0.00,'2026-06-07 07:20:02','2026-06-07 07:20:02'),('ae65df43-35bd-4b5c-99f1-e053f330a4dc','b29de658-9260-44a6-91db-3733d9033965',0.00,0.00,0.00,0.00,'2026-05-22 07:16:23','2026-05-22 07:16:26'),('c2a7761a-acc8-11f1-ba45-e88088b0e710','c2a72532-acc8-11f1-ba45-e88088b0e710',1488.28,0.00,3395.76,490.00,'2026-03-09 11:36:08','2026-09-07 11:36:08'),('c2a7ea17-acc8-11f1-ba45-e88088b0e710','c2a7b4d2-acc8-11f1-ba45-e88088b0e710',489.98,0.00,735.31,1.03,'2025-11-30 11:36:08','2026-08-29 11:36:08'),('c2a85661-acc8-11f1-ba45-e88088b0e710','c2a82062-acc8-11f1-ba45-e88088b0e710',455.35,0.00,794.33,333.89,'2026-02-18 11:36:08','2026-08-17 11:36:08'),('c2a8bb08-acc8-11f1-ba45-e88088b0e710','c2a88892-acc8-11f1-ba45-e88088b0e710',486.86,0.00,4765.64,106.06,'2026-08-12 11:36:08','2026-09-09 11:36:08'),('c2a91770-acc8-11f1-ba45-e88088b0e710','c2a8e6d7-acc8-11f1-ba45-e88088b0e710',1636.02,0.00,3668.60,643.69,'2026-05-18 11:36:08','2026-08-27 11:36:08'),('c2a970ad-acc8-11f1-ba45-e88088b0e710','c2a94241-acc8-11f1-ba45-e88088b0e710',1291.33,0.00,1773.52,2509.58,'2025-10-27 11:36:08','2026-08-20 11:36:08'),('c2a9fe32-acc8-11f1-ba45-e88088b0e710','c2a9c7f7-acc8-11f1-ba45-e88088b0e710',97.32,0.00,323.37,532.16,'2026-07-29 11:36:08','2026-09-08 11:36:08'),('c2aa643d-acc8-11f1-ba45-e88088b0e710','c2aa2c60-acc8-11f1-ba45-e88088b0e710',238.92,0.00,4236.35,2633.91,'2026-01-01 11:36:08','2026-08-14 11:36:08'),('c2aacad8-acc8-11f1-ba45-e88088b0e710','c2aa99a5-acc8-11f1-ba45-e88088b0e710',1235.27,0.00,3131.38,835.45,'2025-11-05 11:36:08','2026-08-23 11:36:08'),('c2ab318f-acc8-11f1-ba45-e88088b0e710','c2aafd6d-acc8-11f1-ba45-e88088b0e710',822.02,0.00,3449.81,650.34,'2026-03-07 11:36:08','2026-08-20 11:36:08'),('c2ab9239-acc8-11f1-ba45-e88088b0e710','c2ab5d3d-acc8-11f1-ba45-e88088b0e710',46.43,0.00,4733.41,1991.33,'2026-09-05 11:36:08','2026-08-29 11:36:08'),('c2ac2035-acc8-11f1-ba45-e88088b0e710','c2abbeda-acc8-11f1-ba45-e88088b0e710',1359.94,0.00,3333.81,881.68,'2026-03-20 11:36:08','2026-08-29 11:36:08'),('c2ac888a-acc8-11f1-ba45-e88088b0e710','c2ac5228-acc8-11f1-ba45-e88088b0e710',349.69,0.00,668.78,432.73,'2026-03-23 11:36:08','2026-08-28 11:36:08'),('c2ace73a-acc8-11f1-ba45-e88088b0e710','c2acb556-acc8-11f1-ba45-e88088b0e710',1750.40,0.00,3751.23,376.87,'2026-05-17 11:36:08','2026-09-05 11:36:08'),('c2ad7127-acc8-11f1-ba45-e88088b0e710','c2ad26c1-acc8-11f1-ba45-e88088b0e710',1120.73,0.00,2737.53,169.33,'2026-04-26 11:36:08','2026-08-26 11:36:08'),('c2adef55-acc8-11f1-ba45-e88088b0e710','c2adbb1a-acc8-11f1-ba45-e88088b0e710',91.00,0.00,2738.78,1806.87,'2026-01-20 11:36:08','2026-09-10 11:36:08'),('c2ae5567-acc8-11f1-ba45-e88088b0e710','c2ae1c45-acc8-11f1-ba45-e88088b0e710',452.19,0.00,4667.75,2968.38,'2026-04-29 11:36:08','2026-09-09 11:36:08'),('c2aebdc4-acc8-11f1-ba45-e88088b0e710','c2ae88a0-acc8-11f1-ba45-e88088b0e710',1256.51,0.00,4937.33,157.72,'2026-07-19 11:36:08','2026-08-19 11:36:08'),('c2af2278-acc8-11f1-ba45-e88088b0e710','c2aeebff-acc8-11f1-ba45-e88088b0e710',149.23,0.00,4539.93,948.25,'2026-05-24 11:36:08','2026-08-31 11:36:08'),('c2af8d28-acc8-11f1-ba45-e88088b0e710','c2af51cf-acc8-11f1-ba45-e88088b0e710',1005.01,0.00,1048.53,1623.04,'2025-11-02 11:36:08','2026-08-31 11:36:08'),('c2b01bf4-acc8-11f1-ba45-e88088b0e710','c2afd092-acc8-11f1-ba45-e88088b0e710',1016.04,0.00,4366.22,2526.47,'2026-08-14 11:36:08','2026-08-19 11:36:08'),('c2b08f3f-acc8-11f1-ba45-e88088b0e710','c2b0520e-acc8-11f1-ba45-e88088b0e710',1134.82,0.00,3624.03,2765.38,'2026-02-07 11:36:08','2026-08-29 11:36:08'),('c2b112d0-acc8-11f1-ba45-e88088b0e710','c2b0d2cb-acc8-11f1-ba45-e88088b0e710',889.08,0.00,113.19,2338.73,'2026-04-05 11:36:08','2026-08-29 11:36:08'),('c2b18404-acc8-11f1-ba45-e88088b0e710','c2b14635-acc8-11f1-ba45-e88088b0e710',791.11,0.00,1401.93,645.77,'2025-11-12 11:36:08','2026-08-17 11:36:08'),('c2b20e6e-acc8-11f1-ba45-e88088b0e710','c2b1c50c-acc8-11f1-ba45-e88088b0e710',261.22,0.00,4104.44,2137.82,'2026-06-17 11:36:08','2026-08-26 11:36:08'),('c2b2bd01-acc8-11f1-ba45-e88088b0e710','c2b24875-acc8-11f1-ba45-e88088b0e710',1006.63,0.00,4796.60,859.95,'2026-08-05 11:36:08','2026-08-31 11:36:08'),('c2b369c9-acc8-11f1-ba45-e88088b0e710','c2b30b98-acc8-11f1-ba45-e88088b0e710',1667.98,0.00,2099.10,1791.39,'2026-02-20 11:36:08','2026-08-14 11:36:08'),('c2b3dcfe-acc8-11f1-ba45-e88088b0e710','c2b3a5ab-acc8-11f1-ba45-e88088b0e710',1154.67,0.00,4145.43,1240.27,'2025-12-19 11:36:08','2026-08-16 11:36:08'),('c2b44d6e-acc8-11f1-ba45-e88088b0e710','c2b419c7-acc8-11f1-ba45-e88088b0e710',1599.78,0.00,1666.75,801.24,'2026-02-11 11:36:08','2026-08-22 11:36:08'),('c2b4ca23-acc8-11f1-ba45-e88088b0e710','c2b4855b-acc8-11f1-ba45-e88088b0e710',462.27,0.00,205.31,1535.72,'2026-05-11 11:36:08','2026-08-15 11:36:08'),('c2b53a81-acc8-11f1-ba45-e88088b0e710','c2b504bb-acc8-11f1-ba45-e88088b0e710',1333.19,0.00,2851.26,2554.40,'2026-04-04 11:36:08','2026-08-22 11:36:08'),('c2b5e34f-acc8-11f1-ba45-e88088b0e710','c2b582bd-acc8-11f1-ba45-e88088b0e710',1455.55,0.00,4404.80,664.43,'2026-02-23 11:36:08','2026-09-05 11:36:08'),('c2b67427-acc8-11f1-ba45-e88088b0e710','c2b62f06-acc8-11f1-ba45-e88088b0e710',1020.65,0.00,4304.37,2320.20,'2026-03-25 11:36:08','2026-08-22 11:36:08'),('c2b70eb0-acc8-11f1-ba45-e88088b0e710','c2b6c013-acc8-11f1-ba45-e88088b0e710',1942.62,0.00,4453.71,1619.33,'2026-05-30 11:36:08','2026-09-07 11:36:08'),('c2b7dcd3-acc8-11f1-ba45-e88088b0e710','c2b77797-acc8-11f1-ba45-e88088b0e710',1815.41,0.00,343.61,1861.50,'2026-09-01 11:36:08','2026-08-26 11:36:08'),('c2b85eb2-acc8-11f1-ba45-e88088b0e710','c2b80aa3-acc8-11f1-ba45-e88088b0e710',398.02,0.00,3796.52,598.48,'2025-10-18 11:36:08','2026-08-23 11:36:08'),('c2b8cd62-acc8-11f1-ba45-e88088b0e710','c2b898a2-acc8-11f1-ba45-e88088b0e710',381.58,0.00,2203.90,1894.60,'2025-12-22 11:36:08','2026-08-12 11:36:08'),('c2b92f55-acc8-11f1-ba45-e88088b0e710','c2b8fda0-acc8-11f1-ba45-e88088b0e710',1349.65,0.00,3307.86,850.16,'2025-11-10 11:36:08','2026-09-02 11:36:08'),('c2b9b290-acc8-11f1-ba45-e88088b0e710','c2b969e0-acc8-11f1-ba45-e88088b0e710',712.96,0.00,44.53,2925.26,'2026-04-06 11:36:08','2026-09-01 11:36:08'),('c2ba33fd-acc8-11f1-ba45-e88088b0e710','c2b9f45d-acc8-11f1-ba45-e88088b0e710',545.47,0.00,1135.08,950.63,'2025-11-05 11:36:08','2026-09-01 11:36:08'),('c2baa1bb-acc8-11f1-ba45-e88088b0e710','c2ba71d5-acc8-11f1-ba45-e88088b0e710',1805.53,0.00,766.47,174.55,'2025-10-16 11:36:08','2026-08-25 11:36:08'),('c2bb16da-acc8-11f1-ba45-e88088b0e710','c2bad1d5-acc8-11f1-ba45-e88088b0e710',215.15,0.00,1541.37,655.93,'2025-11-11 11:36:08','2026-08-12 11:36:08'),('c2bb8214-acc8-11f1-ba45-e88088b0e710','c2bb5060-acc8-11f1-ba45-e88088b0e710',1132.87,0.00,2787.51,264.61,'2026-07-11 11:36:08','2026-09-05 11:36:08'),('c2bbf500-acc8-11f1-ba45-e88088b0e710','c2bbb1f9-acc8-11f1-ba45-e88088b0e710',373.82,0.00,910.59,1049.58,'2025-12-04 11:36:08','2026-08-24 11:36:08'),('c2bc637d-acc8-11f1-ba45-e88088b0e710','c2bc228a-acc8-11f1-ba45-e88088b0e710',378.36,0.00,1475.58,2724.13,'2026-06-28 11:36:08','2026-08-13 11:36:08'),('c2bcf206-acc8-11f1-ba45-e88088b0e710','c2bcb1e7-acc8-11f1-ba45-e88088b0e710',562.86,0.00,239.65,1186.09,'2026-01-14 11:36:08','2026-08-25 11:36:08'),('c2bd564d-acc8-11f1-ba45-e88088b0e710','c2bd1f3a-acc8-11f1-ba45-e88088b0e710',85.80,0.00,132.72,12.06,'2025-11-10 11:36:08','2026-08-12 11:36:08'),('c2bdd88b-acc8-11f1-ba45-e88088b0e710','c2bd919a-acc8-11f1-ba45-e88088b0e710',157.28,0.00,2517.84,845.75,'2025-10-02 11:36:08','2026-08-21 11:36:08'),('c2be4bc8-acc8-11f1-ba45-e88088b0e710','c2be1454-acc8-11f1-ba45-e88088b0e710',1573.32,0.00,1468.89,326.71,'2025-10-17 11:36:08','2026-08-22 11:36:08'),('c2bea74e-acc8-11f1-ba45-e88088b0e710','c2be77da-acc8-11f1-ba45-e88088b0e710',1631.27,0.00,1040.76,1781.53,'2026-01-11 11:36:08','2026-08-12 11:36:08'),('c85c7ec3-3f85-48da-96b5-67681e3e23c2','f0f9424a-1963-4cb3-9c89-fa5461b04b0b',0.00,0.00,0.00,0.00,'2026-06-07 07:14:21','2026-06-07 07:14:21'),('f6da1f33-b11c-4458-b0ff-cddb7c727153','41f6881a-9595-47ee-b999-e9ebceb96a61',0.00,0.00,0.00,0.00,'2026-05-21 14:12:42','2026-05-21 14:12:42');
/*!40000 ALTER TABLE `user_wallets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号，唯一 UNIQUE',
  `password_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt 加密密码（60字符 $2a$xxx）；无前缀=明文待 lazy 升级',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示昵称',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像路径，本地存 /uploads/avatars/xxx，外链 URL 也可',
  `user_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'buyer' COMMENT '用户类型: buyer=买家, seller=卖家, admin=管理员',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '账号是否活跃: 1=正常, 0=禁用',
  `created_at` datetime DEFAULT NULL COMMENT '注册时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后修改时间',
  `steam_account_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'FK → steam_accounts.id，绑定后才有',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('20ce5e5c-751b-405d-98f0-060baf9dffdd','123456','$2a$10$.Z/ofWwN2EzmyzrWdn.8zOicmShicNTTeHWmNoB7WeBAitYsJtay.','冰雪聪明大baka','13877777777','','/uploads/avatars/ecd52b362a3243a2aafa67f9961ada51.png','普通用户',1,'2026-06-03 08:31:23','2026-10-27 11:06:29','1'),('21e82751-fab1-4d8d-9958-745f1f6e1666','123456789','$2a$10$I26zu0ByMMJZblpwMmDwY.XzBUaqBSsqo5ujVGeEDewwmDduG1422','测试用户',NULL,NULL,'https://via.placeholder.com/60x60/2c3e50/ffffff?text=头像','普通用户',1,'2026-05-21 14:05:45','2026-05-21 14:05:45',NULL),('41773925-6164-4e8c-9dc6-be142038cf3f','aaa','$2a$10$g0iDqGKq7ToVCJm.8ZQT1OPx19TaaJPUreC7tG6r/sJ9N/iKxdvLm','aaa自爆步兵','13855555555','','/uploads/avatars/055c68fbf62c45db841a7286ac819a7e.jpg','普通用户',1,'2026-06-08 04:22:51','2026-10-20 11:17:39','3'),('b29de658-9260-44a6-91db-3733d9033965','456','$2a$10$tM427FTMEnVu35qqrJPKYeYn2cOmv9Ngte0NVhkyt0ipUt55.6XD6','断头台高高手','13888888888','','/uploads/avatars/92a29b399d9a4ab1893f40a5193f8fa8.jpg','普通用户',1,'2026-05-22 07:16:05','2026-09-15 11:18:10','4'),('c2a72532-acc8-11f1-ba45-e88088b0e710','seed_001','$2a$10$dT/IuDZBfCG5/1z5ZQBpe.ApgtvtrCR/dWdkUmHVYG93O8pKMmeze','追风少年','13801385129','seed1@steampy.test','/uploads/avatars/d2ae570880a745afb4071c61698b31fa.jpg','普通用户',1,'2026-03-09 11:36:08','2026-09-24 11:10:12','5'),('c2a7b4d2-acc8-11f1-ba45-e88088b0e710','seed_002','$2a$10$G5TJMfBimendjuq96r2w4utQbMVPomecmj6nZ2zfbT8LsfDlDCL0K','深夜不睡的猫','13869262903','seed2@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U002','普通用户',1,'2025-11-30 11:36:08','2026-09-10 11:38:44',NULL),('c2a82062-acc8-11f1-ba45-e88088b0e710','seed_003','$2a$10$0pEX7nuTCKdT6IgqsNxY1.o6Y8lw0ldmwXaJvGU9saG6tsjm232Ie','赛博朋克','13832650715','seed3@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U003','普通用户',1,'2026-02-18 11:36:08','2026-09-10 11:38:31',NULL),('c2a88892-acc8-11f1-ba45-e88088b0e710','seed_004','$2a$10$9PqA.YcO9J7WdJuN1IHY9.zjFax5hzVnTk5Xo8af6pLqHFOAKSjZq','想养只猫','13808800435','seed4@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U004','已封禁',0,'2026-08-12 11:36:08','2026-10-27 11:07:13',NULL),('c2a8e6d7-acc8-11f1-ba45-e88088b0e710','seed_005','$2a$10$zQFAa9SCReTG/Ht7C5VxUuwafbLuQdX08C9aWmX5SrBxHZUex3xBa','代码诗人','13845211359','seed5@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U005','已封禁',0,'2026-05-18 11:36:08','2026-08-27 11:36:08',NULL),('c2a94241-acc8-11f1-ba45-e88088b0e710','seed_006','$2a$10$e2Q25n5rLANeCdL/4wk4CuS/owNt6HBaXmfPzOg2zVGMl5s3TFmHm','周末不加班','13895787111','seed6@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U006','普通用户',1,'2025-10-27 11:36:08','2026-08-20 11:36:08',NULL),('c2a9c7f7-acc8-11f1-ba45-e88088b0e710','seed_007','$2a$10$64W4DBbF1N8EalKmd37EAebCWONM0QRzj3jinUMnlDvpNvi8FzyjW','喝奶茶不加糖','13805954478','seed7@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U007','普通用户',1,'2026-07-29 11:36:08','2026-09-08 11:36:08',NULL),('c2aa2c60-acc8-11f1-ba45-e88088b0e710','seed_008','$2a$10$MMw/qZPKCaYmkfQ7CJ1qLujuCv8ww49wGw7cSzxriEF1bnLc8kAQ6','月光族','13858334308','seed8@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U008','普通用户',1,'2026-01-01 11:36:08','2026-08-14 11:36:08',NULL),('c2aa99a5-acc8-11f1-ba45-e88088b0e710','seed_009','$2a$10$DhTJN7tHOyqMhG9Bcnz9Vu7sPmq8PlF7gxZ.Hd8cOyMijQyD/nzfu','Steam上瘾','13848729571','seed9@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U009','普通用户',1,'2025-11-05 11:36:08','2026-08-23 11:36:08',NULL),('c2aafd6d-acc8-11f1-ba45-e88088b0e710','seed_010','$2a$10$5tVbBQkVc5J7bNwvTTmZs.Jr72DpXlXiBiMjJBUsH7NW98PAAgo62','只玩单机','13812169789','seed10@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U010','普通用户',1,'2026-03-07 11:36:08','2026-08-20 11:36:08',NULL),('c2ab5d3d-acc8-11f1-ba45-e88088b0e710','seed_011','$2a$10$u9F5/B.dHEDkgf.RT7gINuUpPvVqZ4v34VknnlkNmfkMlw57emBSe','FPS苦手','13805646002','seed11@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U011','普通用户',1,'2026-09-05 11:36:08','2026-09-10 11:39:14',NULL),('c2abbeda-acc8-11f1-ba45-e88088b0e710','seed_012','$2a$10$mMRgof33t.szKMJFs5ZayOY5W5wH/wHaGMGiX02.GQIry3ImKz/O6','RPG真爱','13857769817','seed12@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U012','普通用户',1,'2026-03-20 11:36:08','2026-08-29 11:36:08',NULL),('c2ac5228-acc8-11f1-ba45-e88088b0e710','seed_013','$2a$10$ggbzlG4M7PtgX79euZvBwOGznAMkFvluiuYUP.FAawNLW/nW/xjtK','独立游戏控','13891349149','seed13@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U013','普通用户',1,'2026-03-23 11:36:08','2026-08-28 11:36:08',NULL),('c2acb556-acc8-11f1-ba45-e88088b0e710','seed_014','$2a$10$A9DY/AlmwZzXUbztPLyGm.eGksKu0YxgMtXQkPZFlZrydtbMWcx3S','云玩家','13887525262','seed14@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U014','普通用户',1,'2026-05-17 11:36:08','2026-09-05 11:36:08',NULL),('c2ad26c1-acc8-11f1-ba45-e88088b0e710','seed_015','$2a$10$/hF6dafsYi9hswXR5V1qJeUBkXjoTxPn9tn.HPQ8tPixsLZfCg/Ym','首发购入','13841810361','seed15@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U015','普通用户',1,'2026-04-26 11:36:08','2026-08-26 11:36:08',NULL),('c2adbb1a-acc8-11f1-ba45-e88088b0e710','seed_016','$2a$10$HH8om8iKbuHBVFpB5K5w0uyFKr1izCc/GxTCm52K9IqlrfhvJxIqS','等等党','13822657784','seed16@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U016','普通用户',1,'2026-01-20 11:36:08','2026-09-10 11:36:08',NULL),('c2ae1c45-acc8-11f1-ba45-e88088b0e710','seed_017','$2a$10$Nk.RG5kw02bFSPgVtIGwaOkglNtC50zVqTLfTELl0QuzhckaPu4g.','2077粉丝','13806564129','seed17@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U017','普通用户',1,'2026-04-29 11:36:08','2026-09-09 11:36:08',NULL),('c2ae88a0-acc8-11f1-ba45-e88088b0e710','seed_018','$2a$10$GQSQI1Tx0JYm74TqQTC6B.g.i8.L7RyO4ta7JU/7ZH4QUsu0GH63.','辐射老炮','13838460037','seed18@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U018','普通用户',1,'2026-07-19 11:36:08','2026-08-19 11:36:08',NULL),('c2aeebff-acc8-11f1-ba45-e88088b0e710','seed_019','$2a$10$5fzlrJk6WKP5TxMehxgaDe1gWQ0AOTWISfofU/2xtAiu.a.nmPiZ6','老滚信徒','13882169467','seed19@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U019','普通用户',1,'2026-05-24 11:36:08','2026-08-31 11:36:08',NULL),('c2af51cf-acc8-11f1-ba45-e88088b0e710','seed_020','$2a$10$FZ.KPdb1jNjZkUX3KRLSm.F3NrIaMhhl.OuB2BjZxaTMnjrNlw1MW','打工人','13810094088','seed20@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U020','普通用户',1,'2025-11-02 11:36:08','2026-08-31 11:36:08',NULL),('c2afd092-acc8-11f1-ba45-e88088b0e710','seed_021','$2a$10$HzUA4FZ2mqAW7DXMZuQoyOr39WG.KWPvaK2VgrKA71of2989cVP8e','学生党','13855561872','seed21@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U021','普通用户',1,'2026-08-14 11:36:08','2026-08-19 11:36:08',NULL),('c2b0520e-acc8-11f1-ba45-e88088b0e710','seed_022','$2a$10$ItnGOefYFvAORl.5joqMhu5v8R/cZt3U.AYpBddabdQX8C3O7ms3.','社畜','13837074995','seed22@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U022','普通用户',1,'2026-02-07 11:36:08','2026-08-29 11:36:08',NULL),('c2b0d2cb-acc8-11f1-ba45-e88088b0e710','seed_023','$2a$10$IwvYRGvni01tZv68r8obJOBJz/ApJ3IxfqeXpeffrrVxvWrsw4JO6','应届生','13873335050','seed23@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U023','普通用户',1,'2026-04-05 11:36:08','2026-08-29 11:36:08',NULL),('c2b14635-acc8-11f1-ba45-e88088b0e710','seed_024','$2a$10$SVIfk.KWy5Wp9dwmM6Nh..ML824nSvnwMgD/Xhw8JtCxmnNVJQOfa','摸鱼达人','13856579835','seed24@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U024','普通用户',1,'2025-11-12 11:36:08','2026-08-17 11:36:08',NULL),('c2b1c50c-acc8-11f1-ba45-e88088b0e710','seed_025','$2a$10$X1dCeIVJ4z/C0Dnetpkp3OtCD0K3WouqCS5eJdQCmGv583jPWN2ie','喝可乐长不高','13894405702','seed25@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U025','普通用户',1,'2026-06-17 11:36:08','2026-08-26 11:36:08',NULL),('c2b24875-acc8-11f1-ba45-e88088b0e710','seed_026','$2a$10$PYcQJyY1QQB.UF4hZt3sguGop.qfJ8QFF70.UyfEJcisOUNNENyAe','追风少年','13851908825','seed26@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U026','普通用户',1,'2026-08-05 11:36:08','2026-08-31 11:36:08',NULL),('c2b30b98-acc8-11f1-ba45-e88088b0e710','seed_027','$2a$10$BsB1xYUkzZqPukou03SJn.E.KqhVLnidsrZN5zTWmv0.Di2mWXysO','深夜不睡的猫','13891670884','seed27@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U027','普通用户',1,'2026-02-20 11:36:08','2026-08-14 11:36:08',NULL),('c2b3a5ab-acc8-11f1-ba45-e88088b0e710','seed_028','$2a$10$ElL/zBUcySpegpOYo9j4x.Cq.fzVQZ/.R8v9oeKlAJ5vwEpGH7Ob2','赛博朋克','13801919885','seed28@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U028','普通用户',1,'2025-12-19 11:36:08','2026-08-16 11:36:08',NULL),('c2b419c7-acc8-11f1-ba45-e88088b0e710','seed_029','$2a$10$q.6K17tXjUBeGilRUY5XpuPA8e1.KMY8cpFwdx4qnk5PGdBafnd32','想养只猫','13855536831','seed29@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U029','普通用户',1,'2026-02-11 11:36:08','2026-08-22 11:36:08',NULL),('c2b4855b-acc8-11f1-ba45-e88088b0e710','seed_030','$2a$10$9X65KIAg5yCH8yVq2e36Beilqocu7vu9u./7fkMA1ESbx0ifaeVpy','代码诗人','13837153864','seed30@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U030','普通用户',1,'2026-05-11 11:36:08','2026-08-15 11:36:08',NULL),('c2b504bb-acc8-11f1-ba45-e88088b0e710','seed_031','$2a$10$5HLGlNXWImcxxkQVwQ1vFufaQE7agJvWp3LpZx8bvez1z3d0xo/x6','周末不加班','13892091145','seed31@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U031','普通用户',1,'2026-04-04 11:36:08','2026-08-22 11:36:08',NULL),('c2b582bd-acc8-11f1-ba45-e88088b0e710','seed_032','$2a$10$0asyMS5K4jb5w0VzSN2GSemsXU/tNkGYMo9df2DFPVqRH3Rd5Rb36','喝奶茶不加糖','13825263806','seed32@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U032','普通用户',1,'2026-02-23 11:36:08','2026-09-05 11:36:08',NULL),('c2b62f06-acc8-11f1-ba45-e88088b0e710','seed_033','$2a$10$WCx8xvjTngGcFTQaGZu0Qefjk9sAKu5aVgf3Oyrk9Ltpx66EbcobG','月光族','13889691588','seed33@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U033','普通用户',1,'2026-03-25 11:36:08','2026-08-22 11:36:08',NULL),('c2b6c013-acc8-11f1-ba45-e88088b0e710','seed_034','$2a$10$Z3qgi1tgTx0bIp/xmaTxveO19VN4Moxdovxu4QrZ9m9ZfrUjqsTza','Steam上瘾','13865526968','seed34@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U034','普通用户',1,'2026-05-30 11:36:08','2026-09-07 11:36:08',NULL),('c2b77797-acc8-11f1-ba45-e88088b0e710','seed_035','$2a$10$otlEmaOvhIcayyfVRNaDxOj0uvSTCyAzYkIkFiUFpWGknw9FWZYaq','只玩单机','13848993118','seed35@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U035','普通用户',1,'2026-09-01 11:36:08','2026-08-26 11:36:08',NULL),('c2b80aa3-acc8-11f1-ba45-e88088b0e710','seed_036','$2a$10$CTq2DKPaXaPVO.y0SwVYl.p88f7E9zxBR92U.gqo8OKWbSqCvT3/G','FPS苦手','13841191260','seed36@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U036','普通用户',1,'2025-10-18 11:36:08','2026-08-23 11:36:08',NULL),('c2b898a2-acc8-11f1-ba45-e88088b0e710','seed_037','$2a$10$sLGTVyPOfWYZwdPuiYZPOeTZG1Q70XiR8sOoaBGaKFcKQe3s4m4My','RPG真爱','13883772269','seed37@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U037','普通用户',1,'2025-12-22 11:36:08','2026-08-12 11:36:08',NULL),('c2b8fda0-acc8-11f1-ba45-e88088b0e710','seed_038','$2a$10$e6qFArwq2Kj.Ut6NDPg0yuS1hy0xtn/rT.qrXCEYZgK9U1U90CPdq','独立游戏控','13890418491','seed38@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U038','普通用户',1,'2025-11-10 11:36:08','2026-09-02 11:36:08',NULL),('c2b969e0-acc8-11f1-ba45-e88088b0e710','seed_039','$2a$10$oLYS6LmNVmJtrTFkH/5gKupffCBvcTYdRc5JDTL4KBgIoQW4kFiuq','云玩家','13825783110','seed39@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U039','普通用户',1,'2026-04-06 11:36:08','2026-09-01 11:36:08',NULL),('c2b9f45d-acc8-11f1-ba45-e88088b0e710','seed_040','$2a$10$eLAiCt0QUtY8FCNMiK1gUe/oMCY3R8kePts5Sqg61zmW22F/IEQo.','首发购入','13804555292','seed40@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U040','普通用户',1,'2025-11-05 11:36:08','2026-09-01 11:36:08',NULL),('c2ba71d5-acc8-11f1-ba45-e88088b0e710','seed_041','$2a$10$y7b3mFwVK9GralpxKau4Iu2lBBdaumd5Q5jvMsAfi3Grzb3JLgqr.','等等党','13812017298','seed41@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U041','普通用户',1,'2025-10-16 11:36:08','2026-08-25 11:36:08',NULL),('c2bad1d5-acc8-11f1-ba45-e88088b0e710','seed_042','$2a$10$wiuwwNH847A7GQljH86AFee7moMzcC9eJMnrf7Cq3PgR444fOC2xO','2077粉丝','13840986933','seed42@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U042','普通用户',1,'2025-11-11 11:36:08','2026-08-12 11:36:08',NULL),('c2bb5060-acc8-11f1-ba45-e88088b0e710','seed_043','$2a$10$x0IkP1XWy5Z/0iusTyuhz.vM0UsNRsnD9lAIi7SiAOBVsLRNzdqCW','辐射老炮','13842489421','seed43@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U043','普通用户',1,'2026-07-11 11:36:08','2026-09-05 11:36:08',NULL),('c2bbb1f9-acc8-11f1-ba45-e88088b0e710','seed_044','$2a$10$0JtgO6VQLgkCMJ8jxMobzeTZcmuvMLP1TtL9mJQrn1GFTlH.wid3y','老滚信徒','13858414191','seed44@steampy.test','https://via.placeholder.com/60x60/e74c3c/ffffff?text=U044','普通用户',1,'2025-12-04 11:36:08','2026-08-24 11:36:08',NULL),('c2bc228a-acc8-11f1-ba45-e88088b0e710','seed_045','$2a$10$XmxZQRZlQ6PDlI/Y6N9NheCQdexU4MUNV//EI5/YLZOXzD/dgNTAy','打工人','13821692809','seed45@steampy.test','https://via.placeholder.com/60x60/27ae60/ffffff?text=U045','普通用户',1,'2026-06-28 11:36:08','2026-08-13 11:36:08',NULL),('c2bcb1e7-acc8-11f1-ba45-e88088b0e710','seed_046','$2a$10$ZO7o7pBgT6l/ViacvRKhwuWZHRLlEO9ud2mW.9bjvtfWgK6gImAqK','学生党','13878640660','seed46@steampy.test','https://via.placeholder.com/60x60/8e44ad/ffffff?text=U046','普通用户',1,'2026-01-14 11:36:08','2026-08-25 11:36:08',NULL),('c2bd1f3a-acc8-11f1-ba45-e88088b0e710','seed_047','$2a$10$L7fZ2OPnhiw/pLS.iV3VCe35xS8sHYpttWfhjQjsV7W01rAntpAAe','社畜','13839598488','seed47@steampy.test','https://via.placeholder.com/60x60/d35400/ffffff?text=U047','普通用户',1,'2025-11-10 11:36:08','2026-08-12 11:36:08',NULL),('c2bd919a-acc8-11f1-ba45-e88088b0e710','seed_048','$2a$10$4laHHrzYsrb/i/1I5eE3pORnoVsZk/WKUZOHuJc7VaSHKiqUmAZ0e','应届生','13862988012','seed48@steampy.test','https://via.placeholder.com/60x60/34495e/ffffff?text=U048','普通用户',1,'2025-10-02 11:36:08','2026-08-21 11:36:08',NULL),('c2be1454-acc8-11f1-ba45-e88088b0e710','seed_049','$2a$10$XEIEv.JMsJX1Ur7EZfyW..FGRPcoiB2J.LoykmtMY8b4CFmUzUT5y','摸鱼达人','13854650789','seed49@steampy.test','https://via.placeholder.com/60x60/16a085/ffffff?text=U049','普通用户',1,'2025-10-17 11:36:08','2026-08-22 11:36:08',NULL),('c2be77da-acc8-11f1-ba45-e88088b0e710','seed_050','$2a$10$QwJjmRETHwjI8DJHbSJFJO1HaCL0e4e13A250q/8gL2f3jSdmNEFm','喝可乐长不高','13895667840','seed50@steampy.test','https://via.placeholder.com/60x60/2c3e50/ffffff?text=U050','普通用户',1,'2026-01-11 11:36:08','2026-08-12 11:36:08',NULL),('ef8caf9b-917a-4356-a7c6-1d01f9388b7e','1234567','$2a$10$0t.RHSNnEfqblkSTtp.lM.MuRFoDjufVKOqRTCesCyKba/kaFJi6u','uuz','13899997777','','/uploads/avatars/5292cb340ad54bb6a335feb5f4681056.png','管理员',1,'2026-06-07 07:19:28','2026-10-22 11:04:00',NULL),('f0f9424a-1963-4cb3-9c89-fa5461b04b0b','456456','$2a$10$rcj7ZceHDCGUlX7q0.Xc/.nRdWVgKcgf9A6sYvUWIZ01Shu4qs9Ry','456456','13899999999',NULL,'https://via.placeholder.com/60x60/2c3e50/ffffff?text=头像','普通用户',1,'2026-06-07 07:13:58','2026-06-07 07:13:59',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `withdraw_records`
--

DROP TABLE IF EXISTS `withdraw_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `withdraw_records` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UUID 主键',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提现单号，UNIQUE',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FK → users.id',
  `pay_method` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '提现方式: bank, alipay, wechat',
  `account` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收款账号',
  `real_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `bank_card_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联 bank_cards.id',
  `bank_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行名称（冗余）',
  `amount` decimal(12,2) NOT NULL COMMENT '申请金额',
  `fee` decimal(12,2) DEFAULT NULL COMMENT '手续费',
  `net_amount` decimal(12,2) DEFAULT NULL COMMENT '实际到账金额（快照）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态: pending, approved, rejected, completed',
  `applied_at` datetime DEFAULT NULL COMMENT '申请时间',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审批时间',
  `review_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批备注/拒绝原因',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `withdraw_records`
--

LOCK TABLES `withdraw_records` WRITE;
/*!40000 ALTER TABLE `withdraw_records` DISABLE KEYS */;
INSERT INTO `withdraw_records` VALUES ('18de417b-5344-440b-810f-3c0445a924db','WD17884863496904487','20ce5e5c-751b-405d-98f0-060baf9dffdd','bank','111','111',NULL,NULL,2.00,0.02,1.98,'success','2026-09-04 09:45:50',NULL,NULL),('31b4b41f24cbe3a77afb697eb8c0f69d','WD179254332962306EA','41773925-6164-4e8c-9dc6-be142038cf3f','alipay','13898751487','啊啊',NULL,NULL,1.00,1.00,0.00,'success','2026-10-21 08:42:10','2026-10-21 08:42:58','已核对，打款中'),('48937bec4422bd4e42a5c1be838ae590','WD1792545101466987C','41773925-6164-4e8c-9dc6-be142038cf3f','alipay','15674587237','啊啊',NULL,NULL,10.00,1.00,9.00,'success','2026-10-21 09:11:41','2026-10-21 09:11:58',''),('5cd733e1-67ce-473c-829d-77ac831eeeac','WD17884863387513B18','20ce5e5c-751b-405d-98f0-060baf9dffdd','alipay','111111111','111',NULL,NULL,70.00,0.70,69.30,'success','2026-09-04 09:45:39',NULL,NULL),('6f39c7ff45108ec0ddb58c2c2790c391','WD1792544327251E1FC','41773925-6164-4e8c-9dc6-be142038cf3f','alipay','13800138000','测试用户',NULL,NULL,100.00,1.00,99.00,'failed','2026-10-21 08:58:47','2026-10-21 09:01:21',''),('885587162cfb401f05c49c0867de7ddb','WD1788745276419AF60','20ce5e5c-751b-405d-98f0-060baf9dffdd','bank','123456789123456789','一二三',NULL,NULL,2.00,0.02,1.98,'failed','2026-09-07 09:41:16','2026-09-07 09:42:03','信息不符'),('bb3d9f87cd3e73d97a9b8c10a4af0a33','WD1792541658634213D','41773925-6164-4e8c-9dc6-be142038cf3f','alipay','13888888888','测试',NULL,NULL,10.00,0.60,9.40,'failed','2026-10-21 08:14:19','2026-10-21 08:42:49','信息不符'),('e0cd8840-0ec6-4e5e-8f14-8afb020f0306','WD17887434107774984','20ce5e5c-751b-405d-98f0-060baf9dffdd','alipay','13888888888','一二三',NULL,NULL,20.00,0.20,19.80,'success','2026-09-07 09:10:11',NULL,NULL),('e57f4aa9167bbfdf3c6c0e075e7b31ee','WD1792542482310C890','41773925-6164-4e8c-9dc6-be142038cf3f','alipay','18041725959','啊啊',NULL,NULL,2.00,0.60,1.40,'failed','2026-10-21 08:28:02','2026-10-21 08:42:52','信息不符'),('f1f4ad546fb1d9c12c03c42cb931c2bc','WD17930700558044221','41773925-6164-4e8c-9dc6-be142038cf3f','alipay','13898745621','啊啊啊',NULL,NULL,50.00,1.00,49.00,'success','2026-10-27 11:00:56','2026-10-27 11:01:02','');
/*!40000 ALTER TABLE `withdraw_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'steampy'
--

--
-- Dumping routines for database 'steampy'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-29 10:45:08

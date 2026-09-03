# Steam PY匹歪 — Steam 游戏交易平台

基于 **Vue3 + Vite + Spring Boot 3 + MySQL** 构建的 Steam 游戏交易平台，支持官方游戏和玩家上架 CDKey 两种购买方式。

## ✨ 核心功能

- 🎮 **商城卡片网格** — 游戏卡片展示，自动合并官方 + 玩家上架，取最低价，折扣角标
- 📋 **出售卖家表格** — 每个游戏详情页列出所有可售卖家（官方 + 玩家），价格升序，卖家脱敏（`1***6`）
- 💸 **余额混合支付** — 买家可用余额支付（无手续费），不足部分走支付宝模拟支付；也可直接全额支付宝
- 🛒 **玩家上架 CDKey** — 卖家中心 3 个 Tab：上架 / 管理 / 订单
- 📦 **自动发货** — 购买玩家上架商品后后端自动从 listings 表取 CDKey 发给买家，listing 标记 sold
- 👛 **钱包 / 提现** — 卖家售出后余额自动增加，可 1% 手续费提现
- 🚫 **自购拦截** — 不能购买自己上架的 CDKey（前端禁用按钮 + 后端双重校验）
- 🔖 **来源区分** — `user_games` 用 `source=cdkey/store` 区分玩家购买 vs PY代购
- 📄 **分页加载** — 卖家超过 5 个自动折叠，"加载更多" 每次 +5

## 🛠 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| **前端** | Vue 3 + Vite + TypeScript | Vue 3.x / Vite 5.x |
| **后端** | Spring Boot + MyBatis-Plus + JWT | 3.2.5 |
| **数据库** | MySQL | 8.0 |
| **ORM** | MyBatis-Plus | 3.5.x |
| **构建** | Maven | 3.9+ |

## 📂 项目结构

```
-steampy/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/steampy/
│   │   ├── controller/               # REST 控制器
│   │   │   ├── OrderController.java      # 订单（混合支付、卖家自购拦截）
│   │   │   ├── ListingController.java    # CDKey 上架 / available-grouped / available-grouped-all
│   │   │   ├── WalletController.java     # 钱包充值 / 提现（1% 手续费）
│   │   │   ├── GameController.java       # 游戏
│   │   │   └── UserController.java       # 用户
│   │   ├── entity/                   # 实体（@TableName 对应数据库表）
│   │   ├── mapper/                   # MyBatis-Plus Mapper
│   │   └── config/                   # Jackson SNAKE_CASE / WebMvcConfig
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   ├── settings.xml                  # Maven 阿里云镜像
│   ├── init.sql                      # 数据库建表脚本
│   └── import.sql                    # 初始化数据脚本
│
├── src/                              # Vue 前端
│   ├── views/
│   │   ├── CDKeyMarket.vue          # 商城卡片网格（纯游戏，不拆分官方/玩家）
│   │   ├── GameDetail.vue           # 游戏详情 + 出售卖家表格（图1风格）
│   │   ├── SellerCenter.vue         # 卖家中心 3 Tab（上架/管理/订单）
│   │   ├── BalancePurchase.vue      # 钱包管理（充值 + 提现 + 1% 手续费）
│   │   ├── Login.vue                # 登录注册
│   │   ├── Home.vue                 # 首页
│   │   └── ...
│   ├── config/supabase-local.ts     # 前端 API client（walletAPI / orderAPI / listingAPI）
│   ├── components/Layout.vue
│   └── router/index.ts
│
├── public/picture/                   # 游戏图片
├── vite.config.ts                    # 代理 /api → localhost:8080
└── package.json
```

## 🗄 数据库表结构

```
users          — 用户（id VARCHAR36 PK, username UNIQUE, nickname, user_type）
games          — 游戏（id INT PK AUTO_INCREMENT, name, price, original_price, current_price）
listings       — 玩家上架的 CDKey（id VARCHAR36 PK, game_id INT, game_name, seller_id, price, cdkey, status available/sold）
orders         — 订单（id VARCHAR36 PK, buyer_id, seller_id, listing_id, cdkey, price, total_price, payment_method）
transactions   — 交易明细（id VARCHAR36 PK, user_id, type purchase/sale/withdraw/fee/recharge, amount NEG/pos）
user_wallets   — 钱包（user_id PK, balance, frozen_balance）
user_games     — 用户游戏库（id, user_id, game_id, cdkey, source=cdkey/store）
seller_quota   — 卖家额度
announcements  — 公告
```

## 🔌 关键 API

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/games` | 所有游戏 |
| `GET` | `/api/listings/available-grouped?game_id=&game_name=` | 某游戏可售 CDKey（按 卖家+价格 聚合） |
| `GET` | `/api/listings/available-grouped-all` | 跨游戏所有可售 CDKey（带 original_price）|
| `POST` | `/api/listings` | 玩家上架 CDKey |
| `POST` | `/api/orders?balance_amount=` | 创建订单（支持混合支付） |
| `GET` | `/api/orders/user/{userId}` | 用户订单 |
| `GET` | `/api/wallets/user/{userId}` | 查钱包余额 |
| `POST` | `/api/wallets/user/{userId}/recharge` | 充值 |
| `POST` | `/api/wallets/user/{userId}/withdraw` | 提现（1% 手续费） |
| `GET` | `/api/transactions/user/{userId}` | 交易明细 |

## 🚀 快速开始

```bash
# 1. 克隆
git clone https://github.com/123moyingzai/-steampy.git
cd -steampy

# 2. 初始化 MySQL
mysql -u root -proot -e "CREATE DATABASE steampy DEFAULT CHARACTER SET utf8mb4"
mysql -u root -proot steampy < backend/init.sql
mysql -u root -proot steampy < backend/import.sql

# 3. 启动后端
cd backend
mvn spring-boot:run -s settings.xml
# 看到 Started SteampyApplication → http://localhost:8080

# 4. 启动前端（另开一个终端）
cd ..
npm install
npm run dev
# → http://localhost:5173
```

> 详细搭建教程见 [`GUIDE.md`](./GUIDE.md)（组员操作指南）

## 🔑 测试账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| `123456` | `password` | 卖家（有 ¥75 余额，上架过多款 CDKey） |
| `aaa` | `aaa` | 买家（有 ¥42 余额） |
| 其他账号 | 自行注册 | 新注册默认余额 ¥0 |

## 📄 命名约定

- 前端：所有组件 class 前缀 `cjx-`
- Jackson 全局 SNAKE_CASE：Java `currentPrice` → JSON `current_price`
- 数据库 id：users / orders / listings 用 VARCHAR(36) UUID；games 用 INT 自增

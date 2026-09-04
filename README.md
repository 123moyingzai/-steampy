# Steam PY匹歪 — Steam 游戏交易平台

基于 **Vue3 + Vite + Spring Boot 3 + MySQL** 构建的 Steam 游戏交易平台，支持 CDK国区、礼物代购、余额购 三大购买入口。

## ✨ 核心功能

### 🛒 三大购买入口
- **CDK国区** — 官方正版 CDKey + 玩家上架，按价格升序聚合，卖家脱敏（`S***y`）
- **礼物代购** — 代购卖家表格（流畅度/发货类型/额度/折扣/成交数），完整下单链路生成 PY代购订单
- **余额购** — 平台热门游戏网格，用余额 + 支付宝混合支付

### 💸 支付与钱包
- **余额混合支付** — 买家余额无手续费，不足走支付宝模拟
- **钱包管理**（右上角入口 `/wallet`）— 3 Tab：申请提现 / 资金提现说明 / 提现记录
- **提现** — 1% 手续费，最低 ¥1 上限 ¥50，支持支付宝 / 银行卡
- **提现记录持久化** — 每次提现写 `withdraw_records` 表（订单号 WD+时间戳）

### 👤 卖家中心（SellerCenter）
- **上架 CDK** — 实时查重 / CDK 生成器 / 搜索游戏候选 Top10 / 一键定价（最低 - 0.01）
- **CDK 管理 Tab** — 按在售→待激活排序，批量勾选改价（同游戏自动合并），单个 ⚡定价，在售下架变待激活，待激活→重新上架（跳转上架 Tab 预填锁死 CDK）

### 🎮 游戏详情页（GameDetail）
- 全量后端 API 驱动（彻底砍掉本地 cdk_games.json 硬编码 fallback）
- 右侧购买弹窗 / 余额勾选 / 订单确认 / 模拟支付 / CDKey 自动发货
- 卖家表格 + 价格升序 + 自购拦截（前端禁用 + 后端双重校验）
- 版本自动归组（豪华版/标准版 同 baseName 归一起）

### � 图片架构
- **游戏封面** — Steam 官方全球 CDN：`https://cdn.akamai.steamstatic.com/steam/apps/{appid}/header.jpg`，零成本、永不丢失
- **本地图片** — `/public/picture/` 存放旧 12 个游戏图片 + 默认头像

### 📊 数据规模
- 50 款付费游戏（已删除 7 款免费游戏）
- 预售仅 2 款（生化危机安魂曲 + 豪华版）
- 分页每页 70 条，不足则不显示分页控件

## 🛠 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| **前端** | Vue 3 + Vite + TypeScript | Vue 3.x / Vite 5.x |
| **后端** | Spring Boot + MyBatis-Plus + JWT | 3.2.5 |
| **数据库** | MySQL | 8.0 |
| **ORM** | MyBatis-Plus | 3.5.x |
| **构建** | Maven（阿里云镜像 settings.xml） | 3.9+ |

## 📂 项目结构

```
-steampy/
├── backend/                              # Spring Boot 后端
│   ├── src/main/java/com/steampy/
│   │   ├── controller/
│   │   │   ├── OrderController.java          # 订单（混合支付 + 自购拦截 + PY代购跳过 user_games）
│   │   │   ├── ListingController.java        # CDKey 上架 / available-grouped(-all) / check-cdkey / py-sellers
│   │   │   ├── WalletController.java         # 钱包 / 充值 / 提现 / withdraw-records
│   │   │   ├── GameController.java           # 游戏
│   │   │   └── UserController.java           # 用户
│   │   ├── entity/
│   │   │   ├── WithdrawRecord.java           # 提现记录新实体
│   │   │   └── Listing.java                  # 已加 type / quota / autoDeliver
│   │   ├── mapper/
│   │   │   └── WithdrawRecordMapper.java
│   │   └── config/                           # Jackson SNAKE_CASE / WebMvcConfig
│   ├── src/main/resources/
│   │   ├── sql/seed_games.sql                # 45 款新游戏种子（Steam CDN 图）
│   │   ├── application.properties
│   │   └── init.sql                          # 数据库建表脚本
│   ├── pom.xml
│   └── settings.xml                          # Maven 阿里云镜像
│
├── src/                                   # Vue 前端
│   ├── views/
│   │   ├── CDKeyMarket.vue                   # 商城网格（含 🔥火热预售 单独一行）
│   │   ├── GameDetail.vue                    # 游戏详情 + 卖家表格 + 完整购买流程
│   │   ├── GiftPurchase.vue                  # 礼物代购完整交易页（PY代购订单）
│   │   ├── BalancePurchase.vue               # 余额购（GiftPurchase 同款网格，已删 Tab/sidebar）
│   │   ├── Wallet.vue                        # 钱包（提现 / 规则 / 记录 3 Tab）
│   │   ├── SellerCenter.vue                  # 卖家中心（上架 / CDK管理 / 订单）
│   │   ├── BuyerCenter.vue                   # 买家中心（CDKey / PY代购 Tab）
│   │   ├── Login.vue                         # 登录注册
│   │   ├── Home.vue                          # 首页
│   │   └── ...
│   ├── config/supabase-local.ts              # API client（walletAPI / orderAPI / listingAPI / fetchAllGames）
│   ├── components/Layout.vue                 # 右上角"钱包 / 明细"导航
│   └── router/index.ts                       # 路由含 /wallet /gift /balance /cdkey
│
├── public/picture/                        # 本地游戏图片（旧12款 + 默认头像）
├── README.md
├── GUIDE.md                               # 组员操作指南
└── vite.config.ts                         # 代理 /api → localhost:8080
```

## 🗄 数据库表结构

```
users          — 用户（id VARCHAR36 PK, username UNIQUE）
games          — 游戏（id INT PK AUTO_INCREMENT, name, price, original_price, image Steam CDN, developer, release_date, is_presale）
listings       — CDKey 上架（id VARCHAR36 PK, game_id, seller_id, price, cdkey, status available/sold/pending_activation, type cdkey/py, quota, auto_deliver）
orders         — 订单（order_type cdkey/py, cdkey 仅 cdkey 类型有，payment_method alipay/balance/hybrid）
transactions   — 交易明细（type purchase/sale/withdraw/fee/recharge, reference_type withdraw/order, amount NEG/pos）
user_wallets   — 钱包（user_id PK, balance, frozen_balance）
user_games     — 用户游戏库（source cdkey=玩家购 / store=PY代购 / official=官方）
withdraw_records — 提现记录（order_no WD+时间戳, pay_method alipay/bank, account, real_name, amount, fee, net_amount, status success）
```

## 🔌 关键 API

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/games` | 所有游戏（返回 Steam CDN 图 URL） |
| `GET` | `/api/listings/available-grouped?game_id=&game_name=` | 某游戏可售 CDKey（按 卖家+价格 聚合，seller_name 脱敏） |
| `GET` | `/api/listings/available-grouped-all` | 跨游戏聚合（带 original_price 用于 catalog 折扣计算） |
| `GET` | `/api/listings/py-sellers?game_id=` | 礼物代购卖家列表（含 quota / auto_deliver / py 类型） |
| `POST` | `/api/listings` | 玩家上架 CDKey |
| `PUT` | `/api/listings/{id}/price` | 单个改价 |
| `PUT` | `/api/listings/batch-price` | 批量改价 `{updates: {listingId: price}}` |
| `PUT` | `/api/listings/{id}/soft-delete` | 在售 → 待激活 |
| `PUT` | `/api/listings/{id}/relist` | 待激活 → 重新上架 |
| `PUT` | `/api/listings/{id}/self-activate` | 待激活 → 写入 user_games + DELETE listing |
| `GET` | `/api/listings/check-cdkey?cdkey=` | CDKey 格式校验 + 查重 |
| `POST` | `/api/orders?balance_amount=` | 创建订单（混合支付，支持 order_type=cdkey/py） |
| `GET` | `/api/wallets/user/{userId}` | 查钱包余额 |
| `POST` | `/api/wallets/user/{userId}/recharge` | 充值 |
| `POST` | `/api/wallets/user/{userId}/withdraw` | 提现（扩展 body: `{pay_method, account, real_name}`） |
| `GET` | `/api/wallets/user/{userId}/withdraw-records` | 提现记录列表 |
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

# 3. 执行种子数据（45 款 Steam CDN 新游戏）
mysql -u root -proot steampy < backend/src/main/resources/sql/seed_games.sql

# 4. 启动后端
cd backend
mvn spring-boot:run -s settings.xml
# 看到 Started SteampyApplication → http://localhost:8080

# 5. 启动前端（另开终端）
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
- 数据库 id：users / orders / listings / withdraw_records 用 VARCHAR(36) UUID；games 用 INT AUTO_INCREMENT
- 游戏图片：优先 Steam CDN `https://cdn.akamai.steamstatic.com/steam/apps/{appid}/header.jpg`

## 📦 部署注意

- 后端启动前确保 MySQL 里 `games.id` 有 AUTO_INCREMENT（本次已 ALTER）
- listings 表需要 `type / quota / auto_deliver` 三个新列
- withdraw_records 表必须存在（本次已 CREATE）
- 前端 Vite 代理 `/api` → `localhost:8080`，实际部署 Nginx 需同步配置

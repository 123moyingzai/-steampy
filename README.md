# Steam PY匹歪 — Steam 游戏交易平台

基于 **Vue 3 + Vite + Spring Boot 3 + MySQL** 构建的 Steam 游戏交易平台，提供 **CDK国区 / 礼物代购 / 余额购** 三大购买入口，并配套钱包资金、卖家上架、买家订单、管理后台与社区（评测 / 举报 / 公告 / 消息）。

> 部署上线请直接看 [DEPLOY.md](./DEPLOY.md)；团队分工见 [TEAM_ASSIGNMENT.md](./TEAM_ASSIGNMENT.md)。

---

## 一、核心功能

### 1. 购物与交易

| 模块 | 说明 |
|---|---|
| **CDK国区** | 官方 CDKey + 玩家上架聚合，价格升序，卖家名脱敏（`S***y`） |
| **礼物代购** | 代购卖家列表（流畅度 / 发货类型 / 额度 / 折扣 / 成交数），下单生成代购订单 |
| **余额购** | 平台游戏网格，余额 + 支付宝混合支付 |
| **游戏详情** | 版本自动归组（标准版 / 豪华版）、CDKey 列表、购买弹窗、自购拦截（前端 + 后端双重校验） |
| **订单** | 下单即时发货；订单状态机 7 态；`pending` 超时自动关闭并释放上架 |

### 2. 资金

| 模块 | 说明 |
|---|---|
| **钱包** | `user_wallets` 记录 `balance` / `frozen_balance`，余额变动写入前后快照 |
| **充值** | 余额充值 + 充值渠道配置（`payment_methods`） |
| **提现** | 手续费 clamp 公式：`fee = clamp(amount × fee_rate, min_fee, max_fee)`，默认 1% / ¥1 / ¥50；审核通过后扣冻结额 |
| **交易流水** | `transactions` 记录 purchase / sale / withdraw / fee / recharge，含正负号 |
| **退款** | 买家申请 → 管理员审核 → 订单置 `refunded` + 原路退回余额 + 扣回卖家 |

### 3. 卖家与买家中心

- **卖家中心**：批量粘贴上架、CDKey 查重、批量改价、下架 / 重新上架、库存与订单
- **买家中心**：订单列表、状态筛选、退款申请、我的游戏库

### 4. 管理后台（`/admin`，与用户端完全分离）

| 页面 | 能力 |
|---|---|
| 数据概览 | 用户 / 订单 / 游戏 / 总收入 / 今日收入 / 状态分布 / 热销 Top5 |
| 用户管理 | 搜索 + 角色筛选、封禁 / 解封 / 设管理员、单条与批量删除、CSV 导出 |
| 游戏管理 | CRUD、中文名（`name_cn`）维护、CSV 导出 |
| 订单管理 | 筛选、改状态（状态机校验）、批量改状态、CSV 导出 |
| 提现审核 | 通过 / 拒绝（拒绝退回余额 + 反向流水）、CSV 导出 |
| 退款审核 | 通过 / 拒绝（事务内订单 + 双端资金） |
| 评测审核 | 举报数 > 0 的评测优先，保留 / 拒绝 / 删除 |
| 公告管理 | 公告 CRUD |
| 支付渠道 | 费率 / 最小 / 最大手续费 / 启停 / 排序 |

### 5. 社区与账号

Steam 绑定与游戏库、评测与回复、点赞、举报、收藏、AI 助手、站内消息、公告、帮助中心。

---

## 二、技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 前端框架 | Vue 3 | 3.4.x |
| 构建 | Vite | 5.x |
| 语言 | TypeScript | 5.x / 6.x |
| 路由 | Vue Router | 4.3.x |
| 移动端 UI | Vant | 3.6.x |
| HTTP | Axios | 1.x |
| 后端框架 | Spring Boot | 3.2.5（Java 17） |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.x（`utf8mb4_unicode_ci`） |
| 密码 | Spring Security Crypto（BCrypt） | — |
| 校验 | Spring Validation | — |

**约定**

- 统一返回 `Result<T>`：`{ code, message, data }`，成功 `code = 200`
- Jackson 全局 `SNAKE_CASE`，前端在 `apiRequest` 出口统一转驼峰
- 主键：业务表 `varchar(36)` UUID（人可读种子如 `pm-bank` 除外）
- 时间：`LocalDateTime` + `@TableField(fill = ...)` 自动填充
- 订单状态：`OrderStatus` 常量类 + `String` 字段（不是 Java enum）

**鉴权模型（重要，不是 JWT）**

- 登录接口校验密码后**直接返回用户对象**，不签发 token；前端存入 `localStorage`，后续请求带 `user_id`
- 管理端接口由 `AdminAuthFilter` 校验请求头 `X-Admin-Id` + 该用户 `user_type = 管理员`，否则 403
- `BanCheckFilter` 对已封禁用户拦截所有写操作（GET 放行，余额可见）

---

## 三、项目结构

```
-steampy/
├── backend/                                   # Spring Boot 后端
│   ├── src/main/java/com/steampy/
│   │   ├── controller/                        # 19 个 Controller（见下表）
│   │   ├── entity/  mapper/  dto/
│   │   ├── config/                            # CorsConfig / WebConfig / AdminAuthFilter / BanCheckFilter
│   │   ├── constant/OrderStatus.java          # 订单状态机
│   │   ├── task/OrderTimeoutScheduler.java    # 超时自动关闭
│   │   └── service/SteamService.java          # Steam 数据抓取
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── init.sql                           # 建表脚本
│   │   └── sql/                               # 游戏种子数据（seed_games_v1~v4 / name_cn_mapping）
│   ├── src/test/java/.../SteamServiceTest.java
│   ├── pom.xml
│   └── settings-custom.xml                    # 可写本地仓库的 Maven 配置
│
├── src/                                       # Vue 前端
│   ├── views/                                 # 18 个用户端页面 + admin/ 下 10 个后台页面
│   ├── components/                            # Layout / GameCard / Pagination / Toast / ...
│   ├── config/supabase-local.ts               # HTTP client（apiRequest / *API / snakeToCamel）
│   ├── config/admin-api.ts                    # 管理端 API 封装（含 CSV 导出）
│   ├── directives/lazy.ts                     # v-lazy 图片懒加载
│   ├── router/index.ts                        # 路由 + /admin 权限守卫
│   └── main.ts
│
├── e2e/                                       # smoke.mjs（业务流程冒烟）/ regression.mjs（编码回归）
├── public/                                    # 静态资源与本地图片
├── steampy_full_backup.sql                    # 全库终态备份（结构 + 数据）
├── vite.config.ts                             # /api、/uploads 代理；manualChunks 分包
├── DEPLOY.md                                  # 部署文档
└── README.md
```

### Controller 一览（19）

| 域 | Controller |
|---|---|
| 交易 | `GameController` `OrderController` `ListingController` `WalletController` `TransactionController` `RefundController` `PaymentMethodController` `BankCardController` |
| 账号与社区 | `AuthController` `UserGameController` `FavoriteController` `ReviewController` `ReportController` `AnnouncementController` `NotificationController` `SteamController` `AIController` `UploadController` |
| 后台 | `AdminController`（仪表盘 / 用户 / 游戏 / 订单 / 上架 / 提现 / 退款 / 评测 / 公告 / 支付渠道 / 导出 / 批量） |

---

## 四、数据库（24 张表）

| 分组 | 表 |
|---|---|
| 用户 | `users` `user_wallets` `bank_cards` `user_notifications` |
| 商品 | `games` `listings` `seller_quota` |
| 交易 | `orders` `transactions` `withdraw_records` `refunds` `payment_methods` |
| 社区 | `reviews` `review_replies` `review_likes` `review_reply_likes` `reports` `favorites` `announcements` `announcement_reads` |
| Steam | `steam_accounts` `steam_libraries` `user_steam_bindings` `user_games` |

**设计要点**

- 金额一律 `decimal(10,2)` / `decimal(12,2)`，Java 侧 `BigDecimal`，避免浮点误差
- 订单、资产做**快照固化**（冗余存商品名称 / 图片），历史数据不受后续改名影响
- 订单 / 提现 / 退款使用状态机字段并建索引
- 资金表记录变动前后余额，支持对账
- 多对多关联（收藏、绑卡）使用复合唯一键防重

---

## 五、关键 API

### 交易

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/games` | 游戏列表（可按 `isPresale` 过滤） |
| `GET` | `/api/games/{id}` | 游戏详情 |
| `GET` | `/api/listings/available` | 可售 CDKey |
| `GET` | `/api/listings/available-grouped` | 按卖家 + 价格聚合（卖家脱敏） |
| `POST` | `/api/listings` | 上架 CDKey |
| `PUT` | `/api/listings/batch-price` | 批量改价 |
| `POST` | `/api/orders?balance_amount=` | 下单（混合支付，`@Transactional`） |
| `GET` | `/api/wallets/user/{userId}` | 钱包余额 |
| `POST` | `/api/wallets/user/{userId}/withdraw` | 提现（校验渠道状态 + 手续费 clamp） |
| `GET` | `/api/transactions/user/{userId}` | 交易流水 |

### 管理端（均需 `X-Admin-Id`）

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/admin/stats` | 仪表盘聚合 |
| `GET` | `/api/admin/users` | 用户分页（关键词 + 角色） |
| `PUT` | `/api/admin/users/{id}/ban` | 封禁 / 解封 / 设角色 |
| `PUT` / `DELETE` | `/api/admin/users/batch` | 批量改角色 / 批量删除（跳过管理员与操作者本人） |
| `PUT` | `/api/admin/orders/batch-status` | 批量改订单状态 |
| `PUT` | `/api/admin/withdrawals/{id}/review` | 提现审核 |
| `PUT` | `/api/admin/refunds/{id}/review` | 退款审核 |
| `GET` | `/api/admin/export/{users\|orders\|games\|withdrawals}` | CSV 导出（UTF-8 + BOM、中文表头、公式注入防护） |

> 全部 33 个管理端端点见 `AdminController` 类注释。

---

## 六、本地运行

### 前置

- JDK 17、Maven 3.9+、Node.js 18+、MySQL 8.x

### 1. 初始化数据库

```bash
mysql -u root -proot -e "CREATE DATABASE steampy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
mysql -u root -proot steampy < backend/init.sql
# 可选：导入全库终态（结构 + 数据）
mysql -u root -proot steampy < steampy_full_backup.sql
```

种子数据（按需）：

```bash
mysql -u root -proot steampy < backend/src/main/resources/sql/seed_games.sql
mysql -u root -proot steampy < backend/src/main/resources/sql/name_cn_mapping.sql
```

### 2. 启动后端

```bash
cd backend
mvn -s settings-custom.xml -DskipTests package
java -jar target/steampy-backend-1.0.0.jar
# → http://localhost:8080
```

> 若 Maven 全局 settings.xml 把本地仓库指向只读目录，**必须**用 `-s settings-custom.xml` 覆盖。

### 3. 启动前端

```bash
npm install
npm run dev
# → http://localhost:5173（/api、/uploads 由 Vite 代理到 8080）
```

### 4. 测试

```bash
npm run build          # 生产构建校验
npm run test:e2e       # puppeteer-core 冒烟（5 条主流程）
node e2e/regression.mjs  # 文件编码回归
cd backend && mvn -s settings-custom.xml test   # SteamServiceTest 15 例
```

---

## 七、测试账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| `1234567` | `1234567` | 管理员（可进 `/admin`） |
| `123456` | `123456` | 超级管理员（已绑定 Steam） |
| `aaa` | `1` | 买家（已绑定 Steam，有余额） |
| `seed_001` ~ `seed_050` | `1` | 种子用户（含 2 个已封禁：seed_004 / seed_005） |

完整账号清单见 [TEST_ACCOUNTS.md](./TEST_ACCOUNTS.md)。

---

## 八、命名与编码约定

- 前端组件 class 前缀 `cjx-`
- 后端返回 snake_case，前端 `apiRequest` 后统一 `snakeToCamel`
- 游戏封面优先 Steam CDN：`https://cdn.akamai.steamstatic.com/steam/apps/{appid}/header.jpg`
- 上传文件经 `UploadController` 落盘到 `uploads/`，通过 `/uploads/**` 静态映射访问（`app.upload-dir` 可配）
- 所有源文件统一 **UTF-8 无 BOM**，提交前跑 `node e2e/regression.mjs` 校验

---

## 九、相关文档

| 文档 | 内容 |
|---|---|
| [DEPLOY.md](./DEPLOY.md) | 生产部署：构建、Nginx、服务化、排错 |
| [TEAM_ASSIGNMENT.md](./TEAM_ASSIGNMENT.md) | 两人业务域划分与协作边界 |
| [718_GUIDE.md](./718_GUIDE.md) / [719_GUIDE.md](./719_GUIDE.md) | 各业务域专属指南 |
| [TEST_ACCOUNTS.md](./TEST_ACCOUNTS.md) | 测试账号与角色 |
| [PROJECT_STATUS.md](./PROJECT_STATUS.md) | 项目进度与遗留事项 |

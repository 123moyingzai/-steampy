# SteamPY 项目进度与分工文档

> 生成日期：2026-10-19
> 技术栈：Vue 3 + Vite + TypeScript + Element Plus（前端） / Spring Boot 3 + MyBatis-Plus + MySQL 8（后端）

---

## 一、项目简介

SteamPY 是一个面向 Steam 玩家的游戏交易与代充平台。用户可以在平台上买卖 Steam CDKey、购买代购游戏、充值 Steam 余额、管理 Steam 游戏库，同时平台提供 AI 智能助手解答问题。管理员通过后台管理用户、游戏、订单、审核提现和退款等。

**核心业务流**：
```
用户注册/登录 → 绑定 Steam 账号 → 浏览游戏/CDKey → 下单 → 支付
→ 余额/订单管理 → 申请提现 → 管理员审核 → 到账
```

---

## 二、已实现功能（✅ 完成）

### 2.1 用户端（18 个页面）

| 页面 | 路由 | 功能 |
|---|---|---|
| ✅ Home | `/` | 首页：游戏推荐轮播 + 最新公告 |
| ✅ Login | `/login` | 登录/注册：账号密码 + 手机号 + 验证码 |
| ✅ GameDetail | `/game/:id` | 游戏详情：介绍、价格、CDKey 列表、评论、购买 |
| ✅ CDKeyMarket | `/cdkey` | CDKey 市场：搜索、筛选、购买 |
| ✅ GiftPurchase | `/gift` | 代购游戏：选择好友 → 下单 → 支付 |
| ✅ BuyerCenter | `/buyer` | 买家中心：订单列表（全部/进行中/已完成） |
| ✅ SellerCenter | `/seller` | 卖家中心：上架 CDKey、库存管理、批量操作（上下架/删除）、分页 |
| ✅ Wallet | `/wallet` | 我的钱包：余额查看、申请提现、提现说明 |
| ✅ TransactionHistory | `/transactions` | 交易记录：收入/支出流水 |
| ✅ MyGames | `/mygames` | 我的游戏库：已购买/已激活的游戏 |
| ✅ BalancePurchase | `/balance` | 余额充值：选择金额 → 支付 |
| ✅ BalancePurchaseDetail | `/balance/:id` | 充值详情页 |
| ✅ Recharge | `/recharge` | 充值渠道 |
| ✅ ReviewPublish | `/review/:gameId` | 发布评测 |
| ✅ AccountSettings | `/settings` | 账号设置：基本资料、头像、Steam 绑定/解绑、游戏库 |
| ✅ MessageCenter | `/messages` | 消息中心：系统通知、已读/未读、分页 |
| ✅ HelpCenter | `/help` | 帮助中心：FAQ + AI 智能助手 + 游戏卡片 |
| ✅ TestCORS | — | 测试页（开发用） |

### 2.2 管理员端（10 个页面）

| 页面 | 路由 | 功能 |
|---|---|---|
| ✅ AdminLayout | `/admin/*` | 后台布局：侧边栏导航 + 顶部栏 + 面包屑 |
| ✅ AdminDashboard | `/admin/dashboard` | 数据概览：用户数、订单数、交易额、图表 |
| ✅ AdminUsers | `/admin/users` | 用户管理：列表、搜索、封禁/解封 |
| ✅ AdminGames | `/admin/games` | 游戏管理：CRUD、分类、排序 |
| ✅ AdminOrders | `/admin/orders` | 订单管理：列表、状态筛选、详情 |
| ✅ AdminRefunds | `/admin/refunds` | 退款审核：列表、通过/拒绝 |
| ✅ AdminWithdrawals | `/admin/withdrawals` | 提现审核：列表、通过/拒绝 |
| ✅ AdminReviews | `/admin/reviews` | 评测审核：被举报评测、处理 |
| ✅ AdminAnnouncements | `/admin/announcements` | 公告管理：CRUD、置顶 |
| ✅ AdminPaymentMethods | `/admin/payment-methods` | 支付渠道：费率/手续费/启停配置（行内编辑） |

### 2.3 后端（19 个 Controller + 20 个 Entity）

| Controller | 核心接口 |
|---|---|
| ✅ AuthController | 注册/登录/用户信息/修改密码/头像上传 |
| ✅ GameController | 游戏列表/详情/搜索 |
| ✅ OrderController | 下单/混合支付/订单状态机/超时关闭 |
| ✅ ListingController | CDKey 上架/下架/激活/批量操作 |
| ✅ SteamController | Steam 绑定/解绑/账号信息/游戏库 |
| ✅ WalletController | 余额查询/提现/银行卡管理 |
| ✅ TransactionController | 交易流水 |
| ✅ ReviewController | 评测/子评论 CRUD |
| ✅ RefundController | 退款申请 |
| ✅ ReportController | 举报 |
| ✅ NotificationController | 站内消息 |
| ✅ FavoriteController | 收藏 |
| ✅ BankCardController | 绑卡/解绑 |
| ✅ PaymentMethodController | 支付渠道配置（用户端可见） |
| ✅ AnnouncementController | 公告列表 |
| ✅ UserGameController | 用户游戏库 |
| ✅ UploadController | 头像/图片上传 |
| ✅ AIController | AI 助手（RAG + LLM 对话） |
| ✅ AdminController | 后台全量管理接口（仪表盘/用户/游戏/订单/提现/退款/评测/公告/支付渠道） |

### 2.4 基础设施

| 模块 | 说明 |
|---|---|
| ✅ SteamService | Steam ID 加密、游戏库获取 |
| ✅ OrderTimeoutScheduler | 订单超时自动关闭（定时任务） |
| ✅ BanCheckFilter | 用户封禁过滤器（可登录但禁止业务操作） |
| ✅ CorsConfig | 跨域配置 |
| ✅ PasswordMigrationRunner | 旧密码懒加载升级为 BCrypt |
| ✅ WebConfig | JWT 拦截器、静态资源映射 |
| ✅ Result\<T\> | 统一 API 返回包装 |
| ✅ OrderStatus | 订单状态常量枚举 |
| ✅ @author 全量注释 | 95 个 Java/Vue 文件 +2730 行中文注释 |
| ✅ Vue SFC 修复 | BOM 清理 + template 前注释清理 |
| ✅ vite manualChunks | 前端分包 + 路由懒加载 |
| ✅ v-lazy | 全局图片懒加载 directive |

### 2.5 数据库（20 张表）

users / games / orders / listings / transactions / wallets / withdraw_records / payment_methods / reviews / review_replies / favorites / bank_cards / steam_accounts / steam_libraries / user_games / user_steam_bindings / announcements / seller_quota / refunds / user_notifications

---

## 三、部分完成 / 需要完善的功能（🔧 进行中）

| 功能 | 完成度 | 缺什么 |
|---|---|---|
| 管理员端支付渠道：新增/删除 | 40% | 后端 POST/DELETE 接口 + 前端弹窗 |
| 管理员端支付渠道：type 可编辑 | 60% | 后端允许 type 更新 + 前端从 badge 改 select |
| 充值渠道手续费落地 | 30% | PaymentMethod 有 recharge 类型但 RechargeController 还没接入 |
| AI 智能助手 RAG 知识库扩充 | 50% | 目前只接了游戏数据，还可以加订单 FAQ、Steam 指南 |
| 用户端移动端适配 | 60% | 汉堡菜单骨架已有，但部分表格没完全卡片化 |
| 支付渠道统一 API 封装 | 0% | 前端 AdminPaymentMethods.vue 还在直连 axios |

---

## 四、待开发功能（📋 TODO）

| 功能 | 优先级 | 说明 |
|---|---|---|
| 管理员端：支付渠道新增/删除 | 高 | 已有行内编辑，差新增/删除入口 |
| 管理员端：批量启停支付渠道 | 中 | 和卖家中心批量操作同一模式 |
| 充值流程接手续费 | 中 | 复用提现的 clamp 公式 |
| AI 助手增强：上下文记忆 | 中 | 多轮对话时记住之前提的游戏名 |
| AI 助手增强：游戏卡片点击跳转 | 低 | 卡片 → GameDetail.vue |
| 卖家中心：订单自动发货模拟 | 低 | CDKey 自动发放的模拟逻辑 |
| 用户端：Steam 一键绑定（真 API） | 低 | 目前是模拟，真实 Steam OAuth 接入 |
| 前端：登录页记住密码 | 低 | localStorage 存用户名 |
| 前端：购物车（余额购买） | 低 | 先加购物车再结算 |
| 后端：操作日志（管理员操作记录） | 低 | 审计追踪 |
| 后端：防刷/限流 | 低 | IP 频率限制 |
| 单元测试 / 集成测试 | 低 | JUnit + Vitest |

---

## 五、项目分工（已确定）

> 详细分工见 → **[TEAM_ASSIGNMENT.md](./TEAM_ASSIGNMENT.md)**
> 分工模式：**业务模块制**（方案 B），两人各负责一个完整业务域，接口自然解耦。

### 719（Steam & 社区域）

| 指标 | 数量 |
|---|---|
| 后端 Controller | 10（Auth / Steam / UserGame / Review / Report / Announcement / Favorite / Notification / Upload / AI） |
| 后端基础设施 | SteamService + BanCheckFilter |
| 前端页面 | 14（用户端 10 + 管理端 4：AdminReviews / AdminAnnouncements / AdminUsers / AdminDashboard） |
| 数据库表 | 11（users / reviews / review_replies / announcements / favorites / user_notifications / steam_accounts / steam_libraries / user_steam_bindings / user_games / reports） |
| TODO 重点 | AI 助手上下文记忆、封禁联动验证、举报详情补全 |

### 718（交易 & 后台域）

| 指标 | 数量 |
|---|---|
| 后端 Controller | 9（Game / Order / Listing / Wallet / Transaction / Refund / PaymentMethod / BankCard / Admin） |
| 后端基础设施 | CorsConfig + WebConfig + PasswordMigrationRunner + OrderTimeoutScheduler + OrderStatus |
| 前端页面 | 14（用户端 8 + 管理端 6：AdminGames / AdminOrders / AdminWithdrawals / AdminRefunds / AdminPaymentMethods / AdminLayout） |
| 数据库表 | 9（games / orders / listings / user_wallets / withdraw_records / transactions / refunds / payment_methods / bank_cards） |
| TODO 重点 | 支付渠道新增/删除/充值手续费、仪表盘图表补全 |

### 公共协作

- **共同页面** Home.vue / GameDetail.vue / CDKeyMarket.vue：719 管评论+公告，718 管游戏+CDKey+订单；改前看 git blame
- **name_cn 汉化**：718 在 AdminGames.vue 维护，两边都用 `name_cn \|\| name` 显示
- **封禁**：719 在 AuthController 标记，BanCheckFilter 自动拦截，718 无需额外改代码

---

## 六、项目数据规模

| 维度 | 数量 |
|---|---|
| 后端 Controller | 19 |
| 后端 Entity | 20 |
| 后端 Mapper | 20 |
| 后端 Config | 4 |
| 后端 Service | 1 |
| 后端 Task（定时任务） | 1 |
| 后端 Constant（枚举） | 1 |
| **后端 Java 文件总计** | **67** |
| 前端 Vue 页面 | 28 |
| 前端 Vue 组件 | 10 |
| 前端 Router | 1 |
| **前端 Vue 文件总计** | **38** |
| 数据库表 | 20 |
| 注释行 | +2730 |

---

## 七、快速开始

```bash
# 后端
cd backend
mvn spring-boot:run          # http://localhost:8080

# 前端
npm run dev                  # http://localhost:5173

# 管理员登录
# URL: http://localhost:5173/admin/login
# 账号/密码 见 TEST_ACCOUNTS.md
```

---

## 八、版本历史

| 日期 | 版本 | 主要改动 |
|---|---|---|
| 2026-09 | v0.1 | 项目初始化、用户注册登录、基础游戏浏览 |
| 2026-10 初 | v0.2 | CDKey 市场、卖家中心、提现审核、Steam 绑定 |
| 2026-10 中 | v0.3 | 买家中心、订单超时自动关闭、移动端适配、消息中心分页 |
| 2026-10 12 | v0.4 | **全项目注释覆盖**（95 文件 +2730 行）、BOM 修复 |
| 2026-10 15 | v0.5 | **封禁功能**（可登录但禁业务）、**图片懒加载**、vite 分包 |


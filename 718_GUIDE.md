# SteamPY · 718 专属项目指南

> **你的域**：交易 & 后台域
> **业务定位**：订单交易 + 钱包资金 + 卖家中心 + 管理后台
> **分支**：`feature/trade-admin`
> **另一半**：719（Steam & 社区域），见 [719_GUIDE.md](./719_GUIDE.md)

---

## 一、你的领地一览

| 指标 | 数量 |
|---|---|
| 后端 Controller | 9 |
| 后端基础设施 | 5（CorsConfig / WebConfig / PasswordMigrationRunner / OrderTimeoutScheduler / OrderStatus） |
| 后端 Entity / Mapper | 各 9 |
| 前端页面 | 14（用户端 8 + 管理员端 6） |
| 数据库表 | 9 张 |
| TODO 预估工时 | ~9h |

---

## 二、全项目公共技术栈

### 后端（项目通用，你和 719 共享）

| 技术 | 版本 | 用途 |
|---|---|---|
| **Spring Boot** | 3.x | Web 框架主容器 |
| **MyBatis-Plus** | 3.5.5 | ORM，@TableName 自动 CRUD |
| **MySQL** | 8.0 | utf8mb4_unicode_ci |
| **Spring Validation** | — | @NotBlank / @Size / @Email |
| **Spring Security Crypto** | — | BCrypt 密码哈希 |
| **Lombok** | — | @Data 消除样板 |
| **Jackson** | — | JSON 序列化 |
| **自定义 Token 认证** | — | 拦截器校验 header token（非 JWT） |

### 前端（项目通用）

| 技术 | 版本 | 用途 |
|---|---|---|
| **Vue 3** | 3.4.21 | 响应式 UI |
| **Vue Router** | 4.3.0 | 路由 + 登录守卫 |
| **Vite** | 5.1.6 | 构建 |
| **TypeScript** | 3.3.3 | 类型安全 |
| **Vant 3** | 3.6.16 | 移动端 UI 组件（Button / Form / Popup / Tab） |
| **Axios** | 1.17.0 | HTTP，封装在 admin-api.ts / supabase-local.ts |
| **@supabase/supabase-js** | 2.39.7 | Supabase Storage 图片上传 |
| **自定义组件** | — | Layout / GameCard / Pagination / Toast / EmptyState / SearchBar / PriceTag / TabNav / LoadingSpinner（10 个） |
| **自定义指令** | v-lazy | 全局图片懒加载（IntersectionObserver） |

### 公共约定

- **API 返回**：后端 Result\<T\>（code + message + data），前端 Axios 拦截器解包
- **主键**：ASSIGN_UUID → varchar(36)
- **时间字段**：LocalDateTime + FieldFill.INSERT / INSERT_UPDATE
- **枚举**：OrderStatus 常量类 + String 字段（不是 Java enum）

---

## 三、你独有的技术栈与实现方式

| 技术 | 在哪用 | 实现方式 |
|---|---|---|
| **订单状态机（手写）** | OrderController | OrderStatus 常量类（PENDING / PAID / DELIVERED / COMPLETED / CANCELLED / REFUNDED），每次流转前手动校验前置状态 |
| **混合支付** | OrderController.pay() | 先扣 user_wallets.balance → 不够走第三方（模拟）→ 同时冻结 listings 表 CDKey |
| **手续费 clamp 公式** | WalletController（提现） | `fee = amount × feeRate` → `fee = clamp(fee, minFee, maxFee)`，BigDecimal.ROUND_HALF_UP；默认 feeRate=0.01 / minFee=¥1 / maxFee=¥50 |
| **订单超时自动关闭** | OrderTimeoutScheduler | @Scheduled cron 每分钟扫 PENDING > 30min → 批量 CANCELLED + 回滚 listings 库存 |
| **资金冻结/解冻** | user_wallets.frozen_balance | 下单 balance 减 + frozen_balance 加 → 完成时 frozen 转 balance |
| **CDKey 乐观锁** | ListingController.activate | `WHERE id=? AND status=ON_SALE` + affected rows=1 校验，防止超卖 |
| **AdminController 大一统** | 后台全部操作 | 一个 Controller 盖用户/游戏/订单/提现/退款/评测/公告/支付渠道 |
| **前端行内编辑** | AdminPaymentMethods.vue | Vant Field + 原生 input，每行独立保存 |

### 你的核心功能实现链路

| 功能 | 链路 |
|---|---|
| **CDKey 购买** | GameDetail.vue 选 CDKey → OrderController.create → 扣 listings.stock → 建 PENDING 订单 → 混合支付 → listings.status=SOLD → 返回 CDKey |
| **代购游戏** | GiftPurchase.vue 填 Steam 好友 → OrderController.create(gift=true) → 不走 CDKey → DELIVERED 后手动完成 |
| **提现** | Wallet.vue 填金额 + 选渠道 → WalletController.withdraw → ① 余额够（clamp 手续费）② 生成 withdraw_no（WD + 时间戳 + 随机）③ 冻结余额 → AdminWithdrawals.vue 审核 → approved 后扣冻结 |
| **退款** | BuyerCenter.vue 申请 → RefundController.create → AdminRefunds.vue 审核 → approved 后 orders.status=REFUNDED + 原路退回 balance |
| **卖家上架** | SellerCenter.vue 批量粘贴 → ListingController.batchCreate → INSERT listings 关联 game_id |
| **支付渠道** | AdminPaymentMethods.vue 行内编辑 → AdminController.updatePaymentMethod → 更新 payment_methods 表 |
| **游戏管理** | AdminGames.vue CRUD → games 表（id / name / **name_cn** / price / original_price / cover / description / category / stock） |
| **管理员仪表盘** | AdminDashboard.vue → AdminController.dashboardStats → 聚合 COUNT/SUM + 近 7 天订单趋势 |

---

## 四、你的业务全貌

```
游戏浏览 → 加购/购买 → 创建订单 → 支付（余额/代购）→ 自动发货
   ↓                                                        ↓
买家中心                                             卖家中心（上架/下架/库存）
   ↓                                                        ↓
钱包（充值/提现）←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←← 交易明细
   ↓
提现 → 管理员审核 → 到账
退款 → 管理员审核
```

---

## 五、你负责的后端文件（9 个 Controller + 5 基础设施）

### Controller

| 文件 | 职责 |
|---|---|
| **GameController.java** | 游戏列表/详情/搜索（用户 + 管理员共用） |
| **OrderController.java** | 下单/混合支付/状态机/超时自动关闭 |
| **ListingController.java** | CDKey 上架/下架/激活/批量操作/卖家订单 |
| **WalletController.java** | 余额查询/提现/银行卡管理 |
| **TransactionController.java** | 交易流水（收入/支出/手续费） |
| **RefundController.java** | 退款申请 |
| **PaymentMethodController.java** | 支付渠道配置（用户端读取） |
| **BankCardController.java** | 绑卡/解绑 |
| **AdminController.java** | 后台全量管理接口（除评测/公告/封禁外的全部） |

### 你维护的基础设施

| 文件 | 职责 |
|---|---|
| **CorsConfig.java** | 跨域配置 |
| **WebConfig.java** | JWT 拦截器、静态资源映射 |
| **PasswordMigrationRunner.java** | 旧密码懒加载升级 |
| **OrderTimeoutScheduler.java** | 订单超时自动关闭 |
| **OrderStatus.java** | 订单状态常量枚举 |

### 你的 Entity / Mapper（各 9 个）

Game · Order · Listing · Wallet · WithdrawRecord · Transaction · Refund · PaymentMethod · BankCard

---

## 六、你负责的前端页面（14 个）

### 用户端（8 个）

| 页面 | 路由 | 你负责 |
|---|---|---|
| ✅ **Home.vue** | `/` | 游戏推荐轮播（⚠️ 公告轮播是 719 负责的） |
| ✅ **GameDetail.vue** | `/game/:id` | 游戏详情 + CDKey 列表 + 购买弹窗 |
| ✅ **CDKeyMarket.vue** | `/cdkey` | CDKey 市场搜索筛选 |
| ✅ **GiftPurchase.vue** | `/gift` | 代购游戏下单 |
| ✅ **BuyerCenter.vue** | `/buyer` | 买家订单列表 + 状态筛选 |
| ✅ **SellerCenter.vue** | `/seller` | 上架/下架/批量操作/分页 |
| ✅ **Wallet.vue** | `/wallet` | 提现表单 + 提现说明 |
| ✅ **TransactionHistory.vue** | `/transactions` | 交易流水 |
| ✅ **BalancePurchase.vue** | `/balance` | 余额充值 |
| ✅ **BalancePurchaseDetail.vue** | `/balance/:id` | 充值详情 |
| ✅ **Recharge.vue** | `/recharge` | 充值渠道 |

### 管理员端（6 个）

| 页面 | 路由 | 你负责 |
|---|---|---|
| ✅ **AdminGames.vue** | `/admin/games` | 游戏 CRUD |
| ✅ **AdminOrders.vue** | `/admin/orders` | 订单列表 + 详情 |
| ✅ **AdminWithdrawals.vue** | `/admin/withdrawals` | 提现审核 |
| ✅ **AdminRefunds.vue** | `/admin/refunds` | 退款审核 |
| ✅ **AdminPaymentMethods.vue** | `/admin/payment-methods` | 支付渠道配置 |
| ✅ **AdminLayout.vue** | `/admin/*` | 后台布局骨架（侧边栏菜单） |

---

## 七、你负责的数据库表（9 张）

```
games | orders | listings | user_wallets | withdraw_records
transactions | refunds | payment_methods | bank_cards
```

---

## 八、你要做的 TODO（按优先级）

| 优先级 | 任务 | 难度 | 预估 |
|---|---|---|---|
| 🔴 高 | AdminPaymentMethods：**新增渠道**按钮 + 弹窗 + 后端 POST | 低 | 1h |
| 🔴 高 | AdminPaymentMethods：**删除渠道** + method_code 唯一校验 + 被占用不能删 | 中 | 1.5h |
| 🔴 高 | AdminPaymentMethods：充值渠道手续费接入 RechargeController | 中 | 1.5h |
| 🟡 中 | AdminPaymentMethods：type 从只读 badge 改成 select（withdraw/recharge 可切换） | 低 | 30min |
| 🟡 中 | 充值流程接手续费（复用提现的 clamp 公式） | 中 | 1h |
| 🟡 中 | PaymentMethod 前端从直连 axios 改统一 admin-api 封装 | 低 | 30min |
| 🟢 低 | 支付渠道空态引导（暂无渠道，点击新增） | 低 | 15min |
| 🟢 低 | AdminDashboard 仪表盘**订单/交易额图表**数据补全 | 低 | 30min |
| 🟢 低 | 卖家中心批量改价按钮样式优化 | 低 | 15min |

---

## 九、你和 719 的协作边界

| 场景 | 谁管 | 你要注意的 |
|---|---|---|
| **Home.vue** | 你管**游戏轮播**，719 管**公告轮播** | 改公告区前先问 719 |
| **GameDetail.vue** | 你管**CDKey 列表 + 购买**，719 管**评论区** | 改评论区别碰 |
| **name_cn 汉化字段** | **你维护**（AdminGames.vue） | 两边页面都用 `name_cn \|\| name` 显示 |
| **封禁联动** | 719 在 AuthController 标记 | BanCheckFilter 自动拦你的订单/钱包接口，你不用改代码，**被拦了不是你代码问题** |
| **订单超时关闭** | 你维护 OrderTimeoutScheduler | 719 改退款/通知时会留意，你别随意改 cron 表达式 |
| **支付渠道** | 你维护 | 充值/提现都走 PaymentMethodController，改费率公式前想清楚两边都受影响 |
| **Supabase Storage key** | 公共 | .env 里 SUPABASE_URL / SUPABASE_ANON_KEY 别 commit 到 git |

### 你写的接口 vs 719 调用

| 你写的接口 | 719 调的场景 |
|---|---|
| `GET /api/games` | Home.vue 游戏列表 / GameDetail 详情 |
| `GET /api/admin/payment-methods` | 719 **不调**，是你 AdminPaymentMethods.vue 用的 |

### 719 写的接口 vs 你调用

| 719 写的接口 | 你调的场景 |
|---|---|
| `POST /api/auth/login` | 双方登录页都用 |
| `GET /api/notifications` | 你不调 |
| `POST /api/reviews` | 你不调 |

---

## 十、分支与提交流程

```bash
# 1. 切到自己的分支
git checkout -b feature/trade-admin

# 2. 每次 commit 前先同步 main
git fetch origin
git merge origin/main

# 3. 开发、commit、push
git add .
git commit -m "feat: xxx"
git push -u origin feature/trade-admin

# 4. PR 到 main 前，确认没有和 719 改到同一个文件（git diff main --name-only）
```

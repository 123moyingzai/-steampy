# SteamPY API 文档

> 所有接口基础地址：开发态 `http://localhost:8080`（经 Vite proxy `/api` 访问），生产态由 Nginx 反代。
> 数据统一包装 `Result<T> = { code, message, data }`；成功 `code = 200`。
> 所有时间用 `LocalDateTime`（ISO-8601 `yyyy-MM-dd'T'HH:mm:ss`）；后端输出 snake_case，前端入口统一转 camelCase。

---

## 一、通用约定

### 请求头

| 头 | 作用 | 携带方 |
|---|---|---|
| `Content-Type: application/json` | 写接口必须 | 前端 |
| `user_id` | 普通用户身份（非强制，但涉及资源所有权的接口需要） | 前端（登录后） |
| `X-Admin-Id` | 管理员身份（管理员接口强制） | 前端（AdminLayout.vue onMounted 全局注入） |

### 鉴权

- `/api/admin/**` 全部需要 `X-Admin-Id` 头且该用户 `user_type = 管理员`，否则 403
- `/api/**` 写操作（POST/PUT/DELETE）会被 `BanCheckFilter` 检查封禁状态，已封禁用户一律 403
- `GET` 请求对已封禁用户放行（余额可见、订单可查、通知可读）

### 错误码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 参数错误 / 业务校验失败 |
| 401 | 未登录（部分接口） |
| 403 | 无权限（管理员鉴权 / 封禁拦截） |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 二、认证与账号 `/api/auth`

| 方法 | 路径 | 说明 | 请求体 | 返回 data |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | 注册（自动建空钱包） | `{ username, password, phone? }` | `User` |
| `POST` | `/api/auth/login` | 用户名/手机号 + 密码登录 | `{ username?, phone?, password }` | `{ user: User }` |
| `POST` | `/api/auth/phone-login` | 手机号验证码登录（只校验手机号存在） | `{ phone }` | `{ user: User }` |
| `GET` | `/api/auth/user/{id}` | 查用户（敏感字段脱敏） | — | `User`（不含 password_hash） |
| `PUT` | `/api/auth/user/{id}` | 更新用户资料 | `{ nickname?, avatarUrl?, email?, ... }` | `User` |
| `POST` | `/api/auth/verify-password` | 二次验密 | `{ userId, password }` | — |
| `POST` | `/api/auth/user/{id}/change-password` | 改密码 | `{ oldPassword, newPassword }` | — |

---

## 三、游戏 `/api/games`

| 方法 | 路径 | 说明 | 参数 | 返回 data |
|---|---|---|---|---|
| `GET` | `/api/games` | 游戏列表 | `isPresale` 可选（true / false 过滤） | `List<Map>` |
| `GET` | `/api/games/{id}` | 游戏详情 | — | `Game` |
| `GET` | `/api/games/presale` | 预售列表 | — | `List<Game>` |
| `GET` | `/api/games/normal` | 普通列表 | — | `List<Game>` |
| `GET` | `/api/games/search` | 关键词搜索 | `keyword` | `List<Game>` |
| `POST` | `/api/games` | 创建游戏 | `Game` body | `Game` |
| `PUT` | `/api/games/{id}` | 更新游戏 | `Game` body | `Game` |
| `DELETE` | `/api/games/{id}` | 删除游戏 | — | — |

---

## 四、上架 CDKey `/api/listings`

| 方法 | 路径 | 说明 | 参数 | 返回 data |
|---|---|---|---|---|
| `POST` | `/api/listings` | 上架 | `Listing` body | `Listing` |
| `POST` | `/api/listings/batch` | 批量上架 | `{ listings: [] }` | — |
| `GET` | `/api/listings/available` | 所有可售 CDKey | `gameId? gameName?` 可选 | `List<Listing>` |
| `GET` | `/api/listings/available-grouped` | 按卖家 + 价格聚合 | `gameId` 或 `gameName` | `List<Map>` |
| `GET` | `/api/listings/available-grouped-all` | 跨游戏聚合 | — | `List<Map>` |
| `GET` | `/api/listings/py-sellers` | 代购卖家列表 | `gameId` | `List<Listing>` |
| `GET` | `/api/listings/check-cdkey` | CDKey 格式校验 + 查重 | `cdkey` | `{ valid, exists }` |
| `GET` | `/api/listings/seller/{sellerId}` | 卖家上架 | — | `List<Listing>` |
| `GET` | `/api/listings/{id}` | 上架详情 | — | `Listing` |
| `DELETE` | `/api/listings/{id}` | 删除上架 | — | — |
| `PUT` | `/api/listings/{id}/sold` | 标记售出 | — | `Listing` |
| `PUT` | `/api/listings/{id}/price` | 单个改价 | `{ price }` | `Listing` |
| `PUT` | `/api/listings/batch-price` | 批量改价 | `{ updates: { listingId: price } }` | — |
| `PUT` | `/api/listings/{id}/soft-delete` | 在售 → 待激活 | — | `Listing` |
| `PUT` | `/api/listings/{id}/relist` | 待激活 → 重新上架 | — | `Listing` |
| `PUT` | `/api/listings/batch-soft-delete` | 批量下架 | `{ ids: [] }` | — |
| `PUT` | `/api/listings/batch-relist` | 批量重上架 | `{ ids: [] }` | — |
| `DELETE` | `/api/listings/batch` | 批量删除 | `{ ids: [] }` | — |
| `PUT` | `/api/listings/{id}/self-activate` | 自激活（写入 user_games + 删除 listing） | — | — |

---

## 五、订单 `/api/orders`

| 方法 | 路径 | 说明 | 参数 | 返回 data |
|---|---|---|---|---|
| `POST` | `/api/orders` | 创建订单（混合支付，`@Transactional` 事务） | `ListingId` body + `?balance_amount=` | `Map`（含订单 + CDKey） |
| `GET` | `/api/orders/user/{userId}` | 买家订单 | `page size status?` | `Map`（分页） |
| `GET` | `/api/orders/seller/{sellerId}` | 卖家订单 | `page size status?` | `Map` |
| `GET` | `/api/orders/{id}` | 订单详情 | — | `Order` |
| `PUT` | `/api/orders/{id}/cancel` | 取消 | — | `Order` |

---

## 六、钱包 `/api/wallets`

| 方法 | 路径 | 说明 | 请求体 | 返回 data |
|---|---|---|---|---|
| `GET` | `/api/wallets/user/{userId}` | 钱包余额 | — | `Wallet`（含 `balance / frozen_balance`） |
| `POST` | `/api/wallets/user/{userId}/recharge` | 充值 | `{ amount, method? }` | `Wallet` |
| `POST` | `/api/wallets/user/{userId}/withdraw` | 提现 | `{ payMethod, account, realName, amount }` | `Map` |
| `GET` | `/api/wallets/user/{userId}/withdraw-records` | 提现记录 | `page size?` | `Map` |

**手续费 clamp**：`fee = clamp(amount × feeRate, minFee, maxFee)`，默认 1% / ¥1 / ¥50。
**渠道校验**：`PaymentMethodMapper` 匹配 `method_code + type in ("withdraw","both") + is_active=1`。

---

## 七、交易流水 `/api/transactions`

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/transactions/user/{userId}` | 某用户全部流水（`type`：purchase / sale / withdraw / fee / recharge；金额带正负号） |

---

## 八、退款 `/api/refunds`

| 方法 | 路径 | 说明 | 请求体 |
|---|---|---|---|
| `POST` | `/api/refunds` | 发起退款申请 | `{ orderId, reason }` |
| `GET` | `/api/refunds/user/{userId}` | 买家退款列表 | `page size?` |

---

## 九、支付渠道 `/api/payment-methods`

| 方法 | 路径 | 说明 | 参数 |
|---|---|---|---|
| `GET` | `/api/payment-methods/active` | 查询指定用途下已启用的渠道 | `type` 默认 `withdraw`，可选 `recharge`；**both 类型同时满足两种** |

---

## 十、收藏 `/api/favorites`

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` `/api/favorites` | 收藏 | body `{ userId, gameId }` |
| `DELETE` `/api/favorites/{gameId}` | 取消收藏 | query `userId` |
| `GET` `/api/favorites/check` | 是否已收藏 | query `userId, gameId` |
| `GET` `/api/favorites/user/{userId}` | 用户收藏列表 | — |

---

## 十一、Steam 绑定 `/api/steam`

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/steam/prices` | Steam 实时价格查询 | `appIds` 逗号分隔 |
| `POST` `/api/steam/bind/{userId}` | 绑定 Steam 账号 | body `{ steamUserId, steamPassword? }` |
| `DELETE` `/api/steam/unbind/{userId}` | 解绑 | — |
| `GET` `/api/steam/info/{userId}` | 已绑定账号信息 | — |
| `GET` `/api/steam/library/{userId}` | Steam 游戏库 | — |
| `GET` `/api/steam/check-ownership` | 查 Steam 是否拥有 | `userId, gameId` |

---

## 十二、AI 助手 `/api/ai`

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` `/api/ai/chat` | 规则引擎 AI 对话 | `{ userId, message, context? }` |

---

## 十三、内容举报 `/api/content`

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` `/api/content/report` | 举报（支持 review / reply / game 三种 targetType） | `{ userId, targetType, targetId, reason }` |

---

## 十四、通知 `/api/notifications`

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/notifications/unread-count` | 未读数（角标） | query `userId` |
| `POST` `/api/notifications/{id}/read` | 标记单条已读 | query `userId` |
| `POST` `/api/notifications/read-all` | 全部已读 | body `{ userId }` |

（完整列表见前端 MessageCenter.vue，支持分页 + isRead 筛选）

---

## 十五、用户游戏库 `/api/user-games`

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/user-games/user/{userId}` | 用户游戏库 | — |
| `GET` `/api/user-games/{id}` | 单条详情 | — |
| `PUT` `/api/user-games/{id}/activate` | 激活 | — |
| `DELETE` `/api/user-games/{id}` | 删除 | — |

---

## 十六、银行卡 `/api/bank-cards`

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/bank-cards/user/{userId}` | 用户已绑卡（`uk_user_card` 唯一约束） | — |
| `POST` `/api/bank-cards` | 绑卡 | body `{ userId, cardNo, cardName, bankName, ... }` |
| `PUT` `/api/bank-cards/{id}/disable` | 停用 | — |

---

## 十七、文件上传 `/api/upload`

| 方法 | 路径 | 说明 | 返回 |
|---|---|---|---|
| `POST` `/api/upload/avatar` | 头像上传 | MultipartFormFile | `{ url }`（`/uploads/avatars/xxx.png`） |
| `POST` `/api/upload/review-image` | 评测图片上传 | MultipartFormFile | `{ url }`（`/uploads/reviews/xxx.png`） |

静态访问经 `WebConfig` 映射：本地 `uploads/` → HTTP `/uploads/**`。

---

## 十八、管理端 `/api/admin` （需 `X-Admin-Id`）

### 仪表盘

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/admin/stats` | 聚合用户数/订单数/游戏数/可用上架/待审核评测/待处理提现/总收入/今日订单收入/订单状态分布/热销 Top5/最近 8 条订单 |

### 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/admin/users` | 分页（`page size keyword role`）；LEFT JOIN 钱包；脱敏 password_hash |
| `PUT` `/api/admin/users/{id}/ban` | 封禁 / 解封 / 设管理员 |
| `PUT` `/api/admin/users/batch` | 批量封禁 / 解禁 / 设角色；复用单条通知逻辑，逐条写站内通知（通知发送人=操作管理员） |
| `DELETE` `/api/admin/users/batch` | 批量删除；**自动跳过管理员账号与操作者本人** |
| `DELETE` `/api/admin/users/{id}` | 删除单个 |

### 游戏 / 上架

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/admin/games` / `POST` / `PUT` / `DELETE` | 游戏 CRUD |
| `GET` `/api/admin/listings` | 上架列表（`status gameId type` 可自由过滤） |
| `PUT` `/api/admin/listings/{id}/status` | 上架状态变更 |
| `DELETE` `/api/admin/listings/{id}` | 删除上架 |

### 订单 / 提现 / 退款

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/admin/orders` | 订单列表 |
| `PUT` `/api/admin/orders/{id}/status` | 改订单状态（`OrderStatus.canTransition` 校验） |
| `PUT` `/api/admin/orders/batch-status` | 批量改订单状态 |
| `GET` `/api/admin/withdrawals` | 提现列表 |
| `PUT` `/api/admin/withdrawals/{id}/review` | 提现审核（通过 → `status=approved`；拒绝 → 退回余额 + 写 reversal 流水） |
| `GET` `/api/admin/refunds` | 退款列表 |
| `PUT` `/api/admin/refunds/{id}/review` | 退款审核（**事务内**：订单置 refunded + 原路退回买家 + 扣回卖家） |

### 评测 / 公告 / 支付渠道

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/admin/reviews` | 评测列表（只显示 `report_count > 0` 的） |
| `PUT` `/api/admin/reviews/{id}/review` | 评测审核（保留 / 拒绝 + 清零举报） |
| `DELETE` `/api/admin/reviews/{id}` | 删除评测 |
| `GET` `/api/admin/announcements` / `POST` / `PUT` / `DELETE` | 公告 CRUD |
| `GET` `/api/admin/payment-methods` | 支付渠道列表 |
| `PUT` `/api/admin/payment-methods/{id}` | 更新（费率 / 最小值 / 最大值 / 启停 / 排序） |

### 数据导出

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` `/api/admin/export/users` | 用户 CSV（复用 users 列表的筛选条件） |
| `GET` `/api/admin/export/orders` | 订单 CSV |
| `GET` `/api/admin/export/games` | 游戏 CSV |
| `GET` `/api/admin/export/withdrawals` | 提现 CSV |

**统一特性**：UTF-8 + BOM、中文表头、公式注入防护（`= + - @` 首字符加 `'`）、Excel 直接打开不乱码。

---

## 十九、分页约定

所有列表统一返回：

```json
{
  "list":  [...],
  "total": 123,
  "page":  1,
  "size":  10
}
```

默认 `size = 10` / 上限 100；用户列表有防刷保护（size > 100 自动收敛到 100）。

---

## 二十、事务要点

| Controller | 事务方法 | 作用 |
|---|---|---|
| `OrderController` | `createOrder` | 下单主事务：查 listing → 扣余额 → 改 listing → 插订单 → 插流水 → 插 user_games → 加卖家钱 → 写卖家流水 → 写 steam_libraries（**任何一步失败全部回滚**） |
| `WalletController` | `withdraw` | 提现：`update balance` + `insert withdraw_records` + `insert transactions`（2 条：提现 + fee） |
| `AdminController` | `reviewRefund` | 退款审核：订单 `refunded` + 买家退款 `+` + 卖家扣 `-` + 两条流水 |
| `OrderTimeoutScheduler` | `closeTimeoutPendingOrders` | `@Scheduled` + `@Transactional` 批量关单 + 释放 listings |

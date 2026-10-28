# SteamPY · 719 专属项目指南

> **你的域**：Steam & 社区域
> **业务定位**：Steam 生态 + 游戏管理 + 社区互动 + AI
> **分支**：`feature/steam-community`
> **另一半**：718（交易 & 后台域），见 [718_GUIDE.md](./718_GUIDE.md)
>
> **任务交换约定**：管理员端游戏新增/编辑（`AdminGames.vue` + AdminController 游戏段 + `/api/ai/game-info`）归 **你（719）**；作为交换，**登录注册**（`Login.vue`、`AdminLogin.vue`、AuthController 登录注册相关接口）归 **718**，你不再涉及。

---

## 一、你的领地一览

| 指标 | 数量 |
|---|---|
| 后端 Controller | 9（+ AdminController 中的游戏管理段，见第九节） |
| 后端基础设施 | 2（SteamService + BanCheckFilter） |
| 后端 Entity / Mapper | 各 11 |
| 前端页面 | 14（用户端 9 + 管理员端 5，不含登录注册页） |
| 数据库表 | 11 张 |
| TODO 预估工时 | ~8h |

---

## 二、全项目公共技术栈

### 后端（项目通用，你和 718 共享）

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
| **AI / deepseek-chat** | AIController | 纯 HTTP POST 调 deepseek.com/v1/chat/completions，自行拼 system prompt + RAG 片段 + 最近 6 轮历史，解析 choices[0].message.content |
| **RAG 检索增强** | AIController.buildKnowledgeContext | 直接 MySQL games 表关键词模糊匹配 → 取 title/name_cn/description → 拼知识片段注入 prompt（**不是向量数据库**） |
| **Steam ID AES 加密** | SteamController + SteamService | AES/CBC/PKCS5Padding，64 位 Steam ID 加密存 varchar |
| **封禁过滤器** | config.BanCheckFilter | OncePerRequestFilter，users.banned=1 时放行 GET 浏览但拦截 POST/PUT/DELETE |
| **Supabase Storage** | UploadController + 前端 | 前端 supabase-js upload() → publicUrl → 存 DB |
| **BCrypt 密码迁移** | PasswordMigrationRunner | ApplicationRunner 启动扫旧明文（非 $2a$ 开头）→ BCrypt.hashpw() 回写 |

### 你的核心功能实现链路

| 功能 | 链路 |
|---|---|
| **注册登录** | AuthController.login → BCrypt.checkpw → 查 users → 生成 token → 返回（含 steamBound 衍生字段） |
| **Steam 绑定** | SteamController.bind → AES 加密 steamId → 存 steam_accounts → user_steam_bindings 关联 |
| **AI 助手** | HelpCenter.vue → AIController.chat → ① 关键词匹配 games 表（RAG）② 拼 prompt ③ HTTP 调 deepseek ④ 反向匹配游戏名 → 生成 GameCard 列表 |
| **封禁** | AdminUsers.vue 点封禁 → AuthController.update banned=1 → BanCheckFilter 下次拦截返回 403 |
| **评测 + 子评论** | ReviewController + ReplyController → ReviewPublish.vue → 按 gameId 分组 |
| **站内消息** | NotificationController → user_notifications → MessageCenter.vue 分页 + 已读/未读 |
| **公告** | AnnouncementController（用户端只读）+ AdminAnnouncements.vue（CRUD + 置顶） |

---

## 四、你的业务全貌

```
登录（由 718 提供）→ Steam 绑定/解绑 → 游戏库 → 收藏
                                    ↓
                            发布评价/子评论 → 举报
                                    ↓
                            公告/通知 → AI 助手问答
```

---

## 五、你负责的后端文件（9 个 Controller + 2 基础设施）

### Controller

| 文件 | 职责 |
|---|---|
| **SteamController.java** | Steam 账号绑定/解绑/信息查询/游戏库 |
| **UserGameController.java** | 用户游戏库列表/详情 |
| **ReviewController.java** | 评测 CRUD + 子评论 CRUD |
| **ReportController.java** | 举报评测/评论/用户 |
| **AnnouncementController.java** | 公告列表（用户端只读） |
| **FavoriteController.java** | 收藏游戏 |
| **NotificationController.java** | 站内消息 |
| **UploadController.java** | 头像/图片上传 |
| **AIController.java** | AI 助手（RAG + LLM + 游戏卡片匹配）+ `/api/ai/game-info` 游戏信息抓取（管理员新增游戏用） |

### 你维护的基础设施

| 文件 | 职责 |
|---|---|
| **SteamService.java** | Steam ID 加密/解密、游戏库获取 |
| **BanCheckFilter.java** | 封禁过滤器 |

### 你的 Entity / Mapper（各 11 个）

User · UserSteamBinding · SteamAccount · SteamLibrary · UserGame · Review · Reply · Report · Announcement · Favorite · UserNotification

---

## 六、你负责的前端页面（14 个）

### 用户端（9 个）

> 登录/注册页（Login.vue）与管理员登录页（AdminLogin.vue）已移交 718，你不再涉及登录注册页面与接口。

| 页面 | 路由 | 你负责 |
|---|---|---|
| ✅ **AccountSettings.vue** | `/settings` | 基本资料 + 头像 + **Steam 绑定/解绑 Tab** |
| ✅ **MyGames.vue** | `/mygames` | Steam 游戏库 + 平台购买游戏 |
| ✅ **ReviewPublish.vue** | `/review/:gameId` | 发布评测 + 评分 + 子评论 |
| ✅ **MessageCenter.vue** | `/messages` | 系统通知 + 已读/未读筛选 |
| ✅ **HelpCenter.vue** | `/help` | FAQ + **AI 助手对话 + 游戏卡片** |
| ✅ **Home.vue** | `/` | 首页**公告轮播**（⚠️ 游戏轮播是 718 负责的，别碰） |
| ✅ **Layout.vue** | 公共组件 | 顶部栏（头像/通知/封禁提示） |
| ✅ **GameCard.vue** | 公共组件 | 游戏卡片（AI 弹窗里用） |
| ✅ **EmptyState.vue** | 公共组件 | 空态引导 |

### 管理员端（5 个）

> `AdminGames.vue` 是本次交换所得：管理员端游戏新增/编辑/删除、中英文名、AI 补全、封面校验均由你维护。

| 页面 | 路由 | 你负责 |
|---|---|---|
| ✅ **AdminGames.vue** | `/admin/games` | 游戏新增/编辑/删除（中英文名、AI 补全、封面校验、逻辑删除与 CDK 联动） |
| ✅ **AdminReviews.vue** | `/admin/reviews` | 评测审核（被举报列表/处理） |
| ✅ **AdminAnnouncements.vue** | `/admin/announcements` | 公告 CRUD + 置顶 |
| ✅ **AdminUsers.vue** | `/admin/users` | 用户列表 + **封禁/解封** |
| ✅ **AdminDashboard.vue** | `/admin/dashboard` | **用户/封禁统计**（⚠️ 订单/交易额统计是 718 负责的） |

---

## 七、你负责的数据库表（11 张）

```
users | user_steam_bindings | steam_accounts | steam_libraries | user_games
reviews | review_replies | reports | announcements | favorites | user_notifications
```

---

## 八、你要做的 TODO（按优先级）

| 优先级 | 任务 | 难度 | 预估 |
|---|---|---|---|
| ✅ 完成 | 管理员端游戏新增/编辑（AdminGames.vue）：中英文名二选一、AI 补全、封面校验、逻辑删除与 CDK 联动 | 中 | 2h |
| 🔴 高 | AI 助手上下文记忆（多轮对话记住之前提的游戏） | 中 | 2h |
| 🔴 高 | AdminUsers 封禁按钮 → 联动 BanCheckFilter 生效验证 | 低 | 30min |
| 🟡 中 | ReportController 补全举报详情查询接口 | 低 | 30min |
| 🟡 中 | 帮助页 AI 游戏卡片点击跳转 GameDetail | 低 | 1h |
| 🟡 中 | 用户端头像上传流程优化（失败兜底） | 低 | 30min |
| 🟢 低 | 公告置顶排序前端视觉区分（置顶标红/置顶角标） | 低 | 20min |
| 🟢 低 | 登录页记住密码（localStorage） | 低 | 15min |

---

## 九、你和 718 的协作边界

| 场景 | 谁管 | 你要注意的 |
|---|---|---|
| **Home.vue** | 你管**公告轮播**，718 管**游戏轮播** | 改游戏列表前先问 718 |
| **GameDetail.vue 评论区** | 你管**评论**，718 管**CDKey 列表 + 购买** | 改购买按钮别碰 |
| **name_cn 汉化字段** | **你维护**（AdminGames.vue） | 718 只在页面用 `name_cn \|\| name` 显示，不再改这里 |
| **游戏管理接口** | **你维护**（AdminGames.vue + AdminController 中的游戏段 + AIController 的 game-info） | 718 改 AdminController 其他段时不得动游戏段，避免冲突 |
| **登录注册** | **718 维护**（Login.vue / AdminLogin.vue / AuthController 登录注册），与游戏新增交换 | 你不再涉及登录注册页面与接口；改登录态存储 key 前先和 718 对齐 |
| **封禁联动** | 你维护 AdminUsers.vue + BanCheckFilter；718 维护 AuthController 里的封禁标记接口 | 共用 `users.banned` 字段，改封禁判断前先和 718 对齐 |
| **订单超时关闭** | 718 维护 | 你改退款/通知涉及订单状态时留意别和 OrderTimeoutScheduler 打架 |
| **支付渠道** | 718 维护 | 充值/提现手续费都走 PaymentMethodController，你不用管 |
| **Supabase Storage key** | 公共 | .env 里 SUPABASE_URL / SUPABASE_ANON_KEY 别 commit 到 git |

### 你写的接口 vs 718 调用

| 你写的接口 | 谁调 |
|---|---|
| `GET /api/notifications` | 你（MessageCenter.vue） |
| `POST /api/reviews` | 你（ReviewPublish.vue） |
| `POST /api/admin/games` | 你（AdminGames.vue） |
| `GET /api/ai/game-info` | 你（AdminGames.vue 自动补全） |

### 718 写的接口 vs 你调用

| 718 写的接口 | 你调的场景 |
|---|---|
| `GET /api/games` | Home.vue 游戏列表 / GameDetail 详情 |
| `POST /api/auth/login` | 你不调（登录页已归 718） |
| `GET /api/admin/payment-methods` | 你不调，不用管 |

---

## 十、分支与提交流程

```bash
# 1. 切到自己的分支
git checkout -b feature/steam-community

# 2. 每次 commit 前先同步 main
git fetch origin
git merge origin/main

# 3. 开发、commit、push
git add .
git commit -m "feat: xxx"
git push -u origin feature/steam-community

# 4. PR 到 main 前，确认没有和 718 改到同一个文件（git diff main --name-only）
```

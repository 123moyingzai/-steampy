# SteamPY 项目分工（业务模块制）

> 生成日期：2026-10-19
> 详细指南已拆成两份专属文档，点下面链接进入：

| 成员 | 业务域 | 专属指南 | 分支 |
|---|---|---|---|
| **719** | Steam & 社区 | → [719_GUIDE.md](./719_GUIDE.md) | `feature/steam-community` |
| **718** | 交易 & 后台 | → [718_GUIDE.md](./718_GUIDE.md) | `feature/trade-admin` |

---

## 一句话分工

- **719**：认证 / Steam 绑定 / AI 助手 / 评测 / 举报 / 公告 / 消息 / 封禁
- **718**：游戏 / 订单 / CDKey / 钱包 / 提现 / 退款 / 支付渠道 / 管理员后台

---

## 快速协作速查

| 场景 | 谁管 |
|---|---|
| Home.vue 公告轮播 | 719 |
| Home.vue 游戏轮播 | 718 |
| name_cn 汉化字段 | 718 维护（AdminGames） |
| 封禁用户 | 719 标记，BanCheckFilter 自动拦 |
| 订单超时关闭 | 718（OrderTimeoutScheduler） |
| 支付渠道 + 手续费 | 718 |

---

## 全项目公共技术栈

**后端**：Spring Boot 3 / MyBatis-Plus 3.5.5 / MySQL 8 / Spring Validation / Spring Security Crypto / Lombok / 自定义 Token 认证

**前端**：Vue 3.4.21 / Vue Router 4 / Vite 5 / TypeScript 3 / **Vant 3**（移动端 UI） / Axios / @supabase/supabase-js / 10 个自定义组件 / v-lazy 懒加载

**约定**：Result\<T\> 统一返回 / ASSIGN_UUID 主键 / LocalDateTime 自动填充 / OrderStatus String 常量

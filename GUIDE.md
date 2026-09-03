# 🔧 Steam PY匹歪 — 组员环境搭建指南

> 按照这份清单一步步走，20 分钟内把项目跑起来。**每个人用自己本地的 MySQL**，数据互相独立。

---

## 0. 📋 环境要求（一次性安装）

| 工具 | 最低版本 | 下载地址 | 安装后验证 |
|---|---|---|---|
| **JDK** | 17 | https://adoptium.net/zh-CN/temurin/releases/?version=17 | `java -version` → 输出 17 |
| **Maven** | 3.9+ | https://maven.apache.org/download.cgi | `mvn -v` |
| **Node.js** | 18+ | https://nodejs.org/ LTS 版 | `node -v` |
| **MySQL** | 8.0 | https://dev.mysql.com/downloads/mysql/ | `mysql --version` |
| **Git** | 最新 | https://git-scm.com/ | `git --version` |

### ⚠️ Windows 用户额外配置 PATH

JDK 和 Maven 装完后要加到系统环境变量 PATH 里：
```
C:\Program Files\Java\jdk-17\bin
E:\apache-maven-3.9.8\bin   ← 改成你的 Maven 实际路径
```

改完后**关掉所有终端重开**，再跑 `java -version` 验证。

---

## 1. 📥 拉取项目

```bash
# 打开 PowerShell / CMD，找个你想放项目的目录
cd D:\work

# 克隆仓库
git clone https://github.com/123moyingzai/-steampy.git
cd -steampy
```

如果 GitHub 提示登录，用你自己的 GitHub 账号授权。

---

## 2. 🗄 初始化 MySQL 数据库

### 2.1 启动 MySQL 服务

```bash
# Windows 服务方式：
net start mysql80
# 或 net start mysql（看你安装时的服务名）
```

### 2.2 创建数据库 + 导入脚本

```bash
# 打开新终端，跑以下三条（假设用户名 root，密码 root）
mysql -u root -proot -e "CREATE DATABASE steampy DEFAULT CHARACTER SET utf8mb4"
mysql -u root -proot --default-character-set=utf8mb4 steampy < -steampy/backend/init.sql
mysql -u root -proot --default-character-set=utf8mb4 steampy < -steampy/backend/import.sql
```

> 如果 MySQL 密码不是 `root`，把上面的 `-proot` 改成你的实际密码（`-p` 后面直接跟密码，没空格）。

### 2.3 修改数据库连接（如果你的账号不是 root/root）

编辑文件 `-steampy\backend\src\main\resources\application.properties`，改这三行：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/steampy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

---

## 3. ▶️ 启动后端（Spring Boot）

```bash
cd -steampy/backend

# 编译（首次或改了 Java 代码后）
mvn clean compile -s settings.xml

# 启动
mvn spring-boot:run -s settings.xml
```

看到下面这行就说明成功了：
```
Started SteampyApplication in X seconds
```
后端跑在 **http://localhost:8080**

### ✅ 验证后端是否就绪

浏览器打开：
```
http://localhost:8080/api/games
```
如果返回 JSON 数组（游戏数据）→ 后端 OK ✅

如果返回空数组，也没关系，说明后端正常，只是数据库里没数据。

---

## 4. ▶️ 启动前端（Vue）

**另开一个新的终端窗口**（后端那个别关！）：

```bash
cd -steampy

# 安装依赖（第一次必跑）
npm install

# 如果国内网慢，先换镜像：
npm config set registry https://registry.npmmirror.com
# 再 install

# 启动开发服务器
npm run dev
```

看到：
```
Local: http://localhost:5173/
```
浏览器打开 → 前端 OK ✅

> Vite 已经配置好代理，前端所有 `/api/*` 请求自动转发到 `localhost:8080`，**不用管跨域**。

---

## 5. 🔑 测试账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| `123456` | `password` | 卖家账户（上架过多款 CDKey，余额 ¥75）|
| `aaa` | `aaa` | 买家账户（余额 ¥42）|
| 随便填 | 随便填 | 新注册账户（默认余额 ¥0）|

---

## 6. 🔄 日常开发流程

### 启动项目（每天都要）

```bash
# 终端 1 — 后端
cd -steampy/backend
mvn spring-boot:run -s settings.xml

# 终端 2 — 前端
cd -steampy
npm run dev
```

### 改了后端 Java 代码

```bash
# Ctrl+C 停掉后端 → 重新编译 → 重跑
cd backend
mvn clean compile -s settings.xml
mvn spring-boot:run -s settings.xml
```

### 改了前端代码

**Vite 会自动热更新**，改完保存浏览器就刷新了，不用重启。

### 拉最新代码

```bash
cd -steampy
git pull origin main
```

### 提交推送自己的改动

```bash
# 先看看有哪些改了
git status

# 添加 + 提交 + 推送
git add -A
git commit -m "描述你改了什么"
git push origin main
```

---

## 7. ⚠️ 常见问题速查

### Q: Maven 下载慢 / 超时
A: **一定要带 `-s settings.xml`**。项目自带阿里云镜像。如果还是慢：
```bash
mvn spring-boot:run -s settings.xml  # 这个 settings.xml 在 backend/ 目录
```

### Q: npm install 卡住
A: 换国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
```

### Q: 启动报 `Communications link failure`
A: MySQL 没启动！跑 `net start mysql80`

### Q: 启动报 `Access denied for user`
A: `application.properties` 里的 MySQL 用户名/密码不对，去改。

### Q: 前端白屏 / 接口 404
A: **后端没起来**！确认 `localhost:8080` 在跑。浏览器 F12 → Network 看具体请求。

### Q: 启动报 `Table 'steampy.xxx' doesn't exist`
A: `init.sql` 没导入成功。重新跑：
```bash
mysql -u root -proot --default-character-set=utf8mb4 steampy < backend/init.sql
```

### Q: 端口被占（5173 或 8080）
A:
```bash
# 查哪个进程占了 8080
netstat -ano | findstr :8080
# 杀掉它（把 PID 换成查到的数字）
taskkill /PID 进程号 /F
```

### Q: 页面上卖家上架的商品没显示
A: 多半是数据库 listings 表 `game_id` 和 games 表对不上。用 Navicat 或命令行查一下：
```sql
SELECT l.id, l.game_id, l.game_name, g.id FROM listings l LEFT JOIN games g ON l.game_name = g.name;
-- 如果有 game_id 对不上的，更新它：
UPDATE listings SET game_id = 正确的ID WHERE id = 'list-xxx';
```

---

## 8. 🏗 端口速查

| 服务 | 地址 | 备注 |
|---|---|---|
| MySQL | `localhost:3306` | 数据库 `steampy` |
| Spring Boot | `localhost:8080` | 后端 API |
| Vite Dev | `localhost:5173` | 前端页面（如果 5173 被占会自动换 5174）|

---

## 9. 📘 技术要点（需要记住的）

### Jackson 全局 SNAKE_CASE
Java 驼峰 → JSON 下划线。**前端发请求必须用 snake_case**：
```ts
// 前端发送：
{ buyer_id: "xxx", game_name: "三更", total_price: 25.00 }
// 后端 Java：
@RequestBody Order order  // order.getBuyerId(), order.getGameName() 自动映射
```

### 混合支付 URL 格式
创建订单时带可选参数：
```
POST /api/orders?balance_amount=42.00
Body: { buyer_id, listing_id, ... }
```
后端会：1）扣买家钱包 ¥42 2）从 listing 取 cdkey 3）标记 listing=sold 4）给卖家加 ¥58 5）写交易记录（1 条 purchase + 1 条 sale）

### 卖家自购双重拦截
- **前端**：自己上架的行按钮变灰"不可购买"，cheapestRow 跳过自己
- **后端**：`ListingController` 里 `listing.getSellerId().equals(order.getBuyerId())` → 返回错误 "不能购买您自己上架的 CDKey"

### user_games.source 区分来源
```
source = 'store'  — PY代购（Store 页面购入）
source = 'cdkey'  — CDKey 玩家购买（本文档所有购买）
```

---

## 10. 🧪 验证功能是否正常

启动后，按这个 checklist 走一遍：

- [ ] 打开 http://localhost:5173 → 看到商城卡片网格
- [ ] 登录 `123456` / `password` → 进卖家中心能看到自己上架的 CDKey
- [ ] 切换到 `aaa` / `aaa` → 打开东方奇缘记详情页 → 出售卖家表格应该看到 **两行**（官方 + 1***6）
- [ ] 点击 1***6 那行的"购买"→ 右侧价格应该变成 ¥58
- [ ] 勾选余额支付（¥42）→ 确认弹窗显示"余额支付 ¥42 | 支付宝 ¥16"
- [ ] 点立即购买 → 模拟支付成功 → 看到 CDKey → 卖家表格那行 stock-1
- [ ] 交易明细里应该只有 **1 条 purchase**（标题带"余额支付 ¥42.00"）+ **1 条 sale**（卖家那边）

全部 OK → 环境搭建完毕，开始干活！

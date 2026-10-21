# SteamPY 部署文档

本文档描述如何把 SteamPY（Vue 3 前端 + Spring Boot 3 后端 + MySQL 8）部署到生产环境。
本地开发只需看 [README.md](./README.md)。

---

## 一、部署形态

推荐 **Nginx 反向代理 + 后端 Jar 常驻** 的单机方案：

```
浏览器
  │  :80
  ▼
Nginx ──── /            → dist/ 静态文件（Vue SPA，history 路由需 fallback）
  └────── /api/         → 127.0.0.1:8080  (Spring Boot)
  └────── /uploads/     → 127.0.0.1:8080  (后端静态映射)
                              │
                              ▼
                        MySQL 8  :3306
```

优势：前后端同源，浏览器不触发 CORS；`/uploads` 走后端统一鉴权与路径。

---

## 二、环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 后端编译与运行 |
| Maven | 3.9+ | 打包 |
| Node.js | 18+ | 前端构建（仅构建机需要） |
| MySQL | 8.0+ | `utf8mb4` / `utf8mb4_unicode_ci` |
| Nginx | 1.20+ | 静态托管 + 反代 |

服务器最小建议：2 核 4G，磁盘 20G（上传目录会增长）。

---

## 三、数据库准备

```sql
CREATE DATABASE steampy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'steampy'@'localhost' IDENTIFIED BY '<强密码>';
GRANT ALL PRIVILEGES ON steampy.* TO 'steampy'@'localhost';
FLUSH PRIVILEGES;
```

导入结构（二选一）：

```bash
# 只建结构
mysql -u steampy -p steampy < backend/init.sql

# 或导入全库终态（结构 + 数据，适合演示/答辩环境）
mysql -u steampy -p steampy < steampy_full_backup.sql
```

可选种子数据：

```bash
mysql -u steampy -p steampy < backend/src/main/resources/sql/seed_games.sql
mysql -u steampy -p steampy < backend/src/main/resources/sql/name_cn_mapping.sql
```

> 注意：`games.id` 是 `varchar(36)` UUID，不是自增整数；`withdraw_records` 需要 `bank_card_id` / `bank_name` 列，`bank_cards` 需要 `uk_user_card` 唯一键——请以 `steampy_full_backup.sql` 为准，不要用历史版本的建表语句。

---

## 四、后端打包与配置

### 1. 打包

```bash
cd backend
mvn -s settings-custom.xml -DskipTests clean package
# 产物：target/steampy-backend-1.0.0.jar
```

> 必须带 `-s settings-custom.xml`：项目环境的 Maven 全局 `conf/settings.xml` 把本地仓库指向了只读目录 `E:\maven-repository`，会用可写仓库覆盖。
> 生产机若无该约束，可省略 `-s`。

### 2. 生产配置

`application.properties` 目前为本地开发默认值，生产建议用**外置配置 + 环境变量**覆盖，不要改仓库文件：

```properties
# 生产覆盖示例（放在 jar 同目录 config/application.properties，或 --spring.config.additional-location）
server.port=8080
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/steampy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=steampy
spring.datasource.password=${DB_PASSWORD}

# 关闭 SQL stdout 日志，避免性能与日志膨胀
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl

# 上传目录改为绝对路径（默认相对 working dir 的 uploads）
app.upload-dir=/data/steampy/uploads

# 订单超时（分钟）
steampy.order.timeout-minutes=15
```

启动：

```bash
DB_PASSWORD='<强密码>' java -jar target/steampy-backend-1.0.0.jar
```

### 3. 服务化

**Linux（systemd）** `/etc/systemd/system/steampy.service`：

```ini
[Unit]
Description=SteamPY Backend
After=network.target mysql.service

[Service]
User=steampy
WorkingDirectory=/opt/steampy
Environment=DB_PASSWORD=<强密码>
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/steampy/steampy-backend-1.0.0.jar
Restart=always
RestartSec=5
StandardOutput=append:/var/log/steampy/app.log
StandardError=append:/var/log/steampy/app.err

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload && systemctl enable --now steampy
```

**Windows（NSSM）**：

```powershell
nssm install SteamPY "C:\Program Files\Java\jdk-17\bin\java.exe" "-jar D:\steampy\steampy-backend-1.0.0.jar"
nssm set SteamPY AppDirectory D:\steampy
nssm set SteamPY AppEnvironmentExtra DB_PASSWORD=<强密码>
nssm start SteamPY
```

---

## 五、前端构建

```bash
npm ci
npm run build
# 产物：dist/
```

构建产物部署到 Nginx 静态目录，例如 `/var/www/steampy`。

**生产环境不再需要 Vite 代理**（`vite.config.ts` 里的 `/api` 代理只在 dev 生效），改由 Nginx 转发。

---

## 六、Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    root /var/www/steampy;
    index index.html;

    # SPA history 路由 fallback（/admin/orders、/game/xxx 刷新不 404）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源长缓存（Vite 产物带 hash）
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 后端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
        client_max_body_size 20m;   # 评测图片 / 头像上传
    }

    # 上传文件（由后端静态映射提供）
    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        expires 30d;
    }
}
```

HTTPS 用 certbot 或云厂商证书即可，注意 `/api` 也走 HTTPS，避免混合内容被拦截。

---

## 七、上线前检查清单

| # | 检查项 | 说明 |
|---|---|---|
| 1 | 数据库账号非 root | 用独立账号 + 强密码 |
| 2 | `application.properties` 未提交真实密码 | 用外置配置 / 环境变量 |
| 3 | `CorsConfig` 收紧来源 | 当前 `addAllowedOriginPattern("*")` 为开发放行，生产同源部署可不依赖它，但建议改为具体域名 |
| 4 | 上传目录可写且有容量监控 | `app.upload-dir` 指向独立数据盘 |
| 5 | 后端日志轮转 | `logback` 或系统级 logrotate，避免无限增长 |
| 6 | MySQL 定时备份 | 见下方备份脚本 |
| 7 | 管理端鉴权生效 | 不带 `X-Admin-Id` 访问 `/api/admin/**` 应返回 **403** |
| 8 | 导出接口鉴权 | 同上，`/api/admin/export/**` 也应 403 |
| 9 | 封禁拦截生效 | 封禁账号 GET 放行、写操作 403 |
| 10 | 时区一致 | JDBC `serverTimezone=Asia/Shanghai`，服务器时区一致 |

### 快速自检命令

```bash
# 后端存活
curl -s http://127.0.0.1:8080/api/games | head -c 120

# 管理端未鉴权必须 403
curl -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8080/api/admin/stats

# 前端首页
curl -o /dev/null -w "%{http_code}\n" http://127.0.0.1/
```

---

## 八、备份与回滚

### 备份

```bash
mysqldump -u steampy -p --single-transaction --routines --triggers \
  steampy > /backup/steampy_$(date +%Y%m%d_%H%M).sql
```

建议 cron 每日一次 + 保留 14 天；上传目录 `uploads/` 同步纳入备份（图片无法从数据库恢复）。

### 回滚

```bash
# 1. 停服务
systemctl stop steampy
# 2. 还原数据库（覆盖前先做一次当前备份）
mysql -u steampy -p steampy < /backup/steampy_YYYYMMDD_HHMM.sql
# 3. 切回上一个 jar / dist
cp /opt/steampy/releases/steampy-backend-<上一版本>.jar /opt/steampy/steampy-backend-1.0.0.jar
systemctl start steampy
```

---

## 九、常见问题

| 现象 | 原因 | 处理 |
|---|---|---|
| `mvn package` 报仓库只读 | 全局 settings.xml 指向只读本地仓库 | 加 `-s settings-custom.xml` |
| 后端启动即退出，日志 `Access denied` | 数据库账号/密码错误 | 检查外置配置与环境变量 |
| 页面空白，控制台 `/api` 404 | Nginx 未配 `/api/` 反代，或 `proxy_pass` 少了结尾斜杠规则 | 按第六节配置 |
| 刷新 `/admin/xxx` 404 | 缺少 SPA fallback | 加 `try_files $uri $uri/ /index.html` |
| 中文乱码（Excel 打开 CSV） | 导出已带 UTF-8 BOM，若仍乱码说明被中间层转码 | 检查 Nginx `charset` 与代理缓冲 |
| 图片 404 | `app.upload-dir` 路径不对或未迁移 `uploads/` | 指到实际目录并确认读权限 |
| 管理页一直 403 | 前端未带 `X-Admin-Id`，或该账号不是管理员 | 重新以管理员登录后台 |
| 提现渠道为空 | `payment_methods` 无 `is_active=1` 的对应 type 渠道 | 后台「支付渠道」启用，或核对 type（withdraw / both） |
| 端口冲突 | 8080 被占用 | `server.port` 改端口，同步改 Nginx `proxy_pass` |

---

## 十、生产环境不建议保留的内容

以下为开发/演示辅助，生产可移除或限制访问：

- `TestCORS.vue` 路由 `/test-cors`
- `src/views/admin` 之外的测试页面
- 仓库内的 `steampy_full_backup.sql`（含生产数据，不应提交到公开仓库）
- `application.properties` 中的明文数据库口令

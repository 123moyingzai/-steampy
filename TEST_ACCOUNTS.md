# 测试账号清单

> **数据库状态**：所有密码已 BCrypt 加密，原始密码记录在此文件。
> **用途**：开发 / 测试 / 演示登录用。**生产环境前必须删除此文件**。
> **总量**：56 个用户（2 管理员 + 2 已封禁 + 52 普通用户）

---

## 一、管理员账号

| 用户名 | 原始密码 | 昵称 | 手机 | 邮箱 | Steam 绑定 | 钱包余额 | 备注 |
|---|---|---|---|---|---|---|---|
| 123456 | 123456 | 冰雪聪明大baka | 13877777777 | — | ✅ 已绑定 | ¥91.99 | 超级管理员，Steam 绑定 |
| 1234567 | 1234567 | 1234567 | 13899997777 | — | ❌ 未绑定 | ¥0.00 | 管理员，未绑 Steam |

---

## 二、Steam 已绑定账号

共 3 个用户已绑定 Steam 账号（可测试 Steam 游戏库功能）。

| 用户名 | 原始密码 | 昵称 | 手机 | 钱包余额 | 绑定的 Steam |
|---|---|---|---|---|---|
| 123456 | 123456 | 冰雪聪明大baka | 13877777777 | ¥91.99 | 123456_Steam |
| aaa | 1 | aaa自爆步兵 | 13855555555 | ¥59.00 | aaa_Master |
| 456 | 1 | 断头台高高手 | 13888888888 | ¥0.00 | 456_Steam |

Steam 模拟账号详情：

| Steam 昵称 | 内部 ID | Steam 等级 | 游戏数 | 游戏价值 | 总时长(小时) |
|---|---|---|---|---|---|
| 123456_Steam | 1 | — | — | — | 3 |
| aaa_Master | 3 | — | — | — | 32 |
| 456_Steam | 4 | — | — | — | 8 |
| 追风少年_Master | 5 | — | — | — | 14 |

---

## 三、普通用户（非种子）

| 用户名 | 原始密码 | 昵称 | 手机 | 钱包余额 |
|---|---|---|---|---|
| 123456789 | 1 | 测试用户 | — | ¥0.00 |
| 456456 | 1 | 456456 | 13899999999 | ¥0.00 |

---

## 四、种子用户 seed_001 ~ seed_050

**统一密码：`1`**。共 50 个，含 2 个已封禁。

| 用户名 | 昵称 | 手机 | 邮箱 | 状态 | 钱包余额 |
|---|---|---|---|---|---|
| seed_001 | 追风少年 | 13801385129 | seed1@steampy.test | ✅ 正常 | ¥1,488.28 |
| seed_002 | 深夜不睡的猫 | 13869262903 | seed2@steampy.test | ✅ 正常 | ¥489.98 |
| seed_003 | 赛博朋克 | 13832650715 | seed3@steampy.test | ✅ 正常 | ¥455.35 |
| seed_004 | 想养只猫 | 13808800435 | seed4@steampy.test | 🚫 已封禁 | ¥486.86 |
| seed_005 | 代码诗人 | 13845211359 | seed5@steampy.test | 🚫 已封禁 | ¥1,636.02 |
| seed_006 | 周末不加班 | 13895787111 | seed6@steampy.test | ✅ 正常 | ¥1,291.33 |
| seed_007 | 喝奶茶不加糖 | 13805954478 | seed7@steampy.test | ✅ 正常 | ¥97.32 |
| seed_008 | 月光族 | 13858334308 | seed8@steampy.test | ✅ 正常 | ¥238.92 |
| seed_009 | Steam上瘾 | 13848729571 | seed9@steampy.test | ✅ 正常 | ¥1,235.27 |
| seed_010 | 只玩单机 | 13812169789 | seed10@steampy.test | ✅ 正常 | ¥822.02 |
| seed_011 | FPS苦手 | 13805646002 | seed11@steampy.test | ✅ 正常 | ¥46.43 |
| seed_012 | RPG真爱 | 13857769817 | seed12@steampy.test | ✅ 正常 | ¥1,359.94 |
| seed_013 | 独立游戏控 | 13891349149 | seed13@steampy.test | ✅ 正常 | ¥349.69 |
| seed_014 | 云玩家 | 13887525262 | seed14@steampy.test | ✅ 正常 | ¥1,750.40 |
| seed_015 | 首发购入 | 13841810361 | seed15@steampy.test | ✅ 正常 | ¥1,120.73 |
| seed_016 | 等等党 | 13822657784 | seed16@steampy.test | ✅ 正常 | ¥91.00 |
| seed_017 | 2077粉丝 | 13806564129 | seed17@steampy.test | ✅ 正常 | ¥452.19 |
| seed_018 | 辐射老炮 | 13838460037 | seed18@steampy.test | ✅ 正常 | ¥1,256.51 |
| seed_019 | 老滚信徒 | 13882169467 | seed19@steampy.test | ✅ 正常 | ¥149.23 |
| seed_020 | 打工人 | 13810094088 | seed20@steampy.test | ✅ 正常 | ¥1,005.01 |
| seed_021 | 学生党 | 13855561872 | seed21@steampy.test | ✅ 正常 | ¥1,016.04 |
| seed_022 | 社畜 | 13837074995 | seed22@steampy.test | ✅ 正常 | ¥1,134.82 |
| seed_023 | 应届生 | 13873335050 | seed23@steampy.test | ✅ 正常 | ¥889.08 |
| seed_024 | 摸鱼达人 | 13856579835 | seed24@steampy.test | ✅ 正常 | ¥791.11 |
| seed_025 | 喝可乐长不高 | 13894405702 | seed25@steampy.test | ✅ 正常 | ¥261.22 |
| seed_026 | 追风少年 | 13851908825 | seed26@steampy.test | ✅ 正常 | ¥1,006.63 |
| seed_027 | 深夜不睡的猫 | 13891670884 | seed27@steampy.test | ✅ 正常 | ¥1,667.98 |
| seed_028 | 赛博朋克 | 13801919885 | seed28@steampy.test | ✅ 正常 | ¥1,154.67 |
| seed_029 | 想养只猫 | 13855536831 | seed29@steampy.test | ✅ 正常 | ¥1,599.78 |
| seed_030 | 代码诗人 | 13837153864 | seed30@steampy.test | ✅ 正常 | ¥462.27 |
| seed_031 | 周末不加班 | 13892091145 | seed31@steampy.test | ✅ 正常 | ¥1,333.19 |
| seed_032 | 喝奶茶不加糖 | 13825263806 | seed32@steampy.test | ✅ 正常 | ¥1,455.55 |
| seed_033 | 月光族 | 13889691588 | seed33@steampy.test | ✅ 正常 | ¥1,020.65 |
| seed_034 | Steam上瘾 | 13865526968 | seed34@steampy.test | ✅ 正常 | ¥1,942.62 |
| seed_035 | 只玩单机 | 13848993118 | seed35@steampy.test | ✅ 正常 | ¥1,815.41 |
| seed_036 | FPS苦手 | 13841191260 | seed36@steampy.test | ✅ 正常 | ¥398.02 |
| seed_037 | RPG真爱 | 13883772269 | seed37@steampy.test | ✅ 正常 | ¥381.58 |
| seed_038 | 独立游戏控 | 13890418491 | seed38@steampy.test | ✅ 正常 | ¥1,349.65 |
| seed_039 | 云玩家 | 13825783110 | seed39@steampy.test | ✅ 正常 | ¥712.96 |
| seed_040 | 首发购入 | 13804555292 | seed40@steampy.test | ✅ 正常 | ¥545.47 |
| seed_041 | 等等党 | 13812017298 | seed41@steampy.test | ✅ 正常 | ¥1,805.53 |
| seed_042 | 2077粉丝 | 13840986933 | seed42@steampy.test | ✅ 正常 | ¥215.15 |
| seed_043 | 辐射老炮 | 13842489421 | seed43@steampy.test | ✅ 正常 | ¥1,132.87 |
| seed_044 | 老滚信徒 | 13858414191 | seed44@steampy.test | ✅ 正常 | ¥373.82 |
| seed_045 | 打工人 | 13821692809 | seed45@steampy.test | ✅ 正常 | ¥378.36 |
| seed_046 | 学生党 | 13878640660 | seed46@steampy.test | ✅ 正常 | ¥562.86 |
| seed_047 | 社畜 | 13839598488 | seed47@steampy.test | ✅ 正常 | ¥85.80 |
| seed_048 | 应届生 | 13862988012 | seed48@steampy.test | ✅ 正常 | ¥157.28 |
| seed_049 | 摸鱼达人 | 13854650789 | seed49@steampy.test | ✅ 正常 | ¥1,573.32 |
| seed_050 | 喝可乐长不高 | 13895667840 | seed50@steampy.test | ✅ 正常 | ¥1,631.27 |

---

## 五、登录测试

```bash
# 超级管理员
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"123456","password":"123456"}'

# 普通种子用户
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"seed_001","password":"1"}'

# 测试管理员封禁功能（用 seed_004 或 seed_005）
# 测试 Steam 绑定功能（用 123456 / aaa / 456）
```

---

## 六、数据来源

本文件所有数据均从数据库实时查询生成：
```sql
SELECT username, nickname, user_type, email, phone,
       CASE WHEN steam_account_id IS NOT NULL THEN '已绑定' ELSE '未绑定' END AS steam_bound,
       COALESCE((SELECT balance FROM user_wallets w WHERE w.user_id = users.id), 0) AS balance
FROM users ORDER BY username;
```

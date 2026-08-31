# Steam PY - Vue3 游戏交易平台 719cjx

## 项目简介

基于 Vue3 + Vite + Supabase 构建的 Steam 游戏交易平台（Steam PY匹歪）。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架，使用组合式API
- **Vite** - 下一代前端构建工具
- **Vue Router 4** - 官方路由管理器
- **Supabase** - 开源 Firebase 替代方案，提供PostgreSQL数据库

## 项目结构

```
steampy-vue/
├── index.html              # 入口HTML文件
├── package.json            # 项目依赖配置
├── vite.config.js          # Vite配置
├── src/
│   ├── main.js             # 应用入口
│   ├── App.vue             # 根组件
│   ├── router/
│   │   └── index.js        # 路由配置
│   ├── config/
│   │   └── supabase.js     # Supabase客户端配置和API
│   ├── components/         # 公共组件
│   │   └── Layout.vue      # 通用布局组件
│   └── views/              # 页面组件
│       ├── Home.vue        # 首页（带左侧导航+顶部栏+轮播图）
│       ├── Login.vue       # 登录注册页（深色背景）
│       ├── BuyerCenter.vue # 买家中心
│       ├── SellerCenter.vue# 卖家中心
│       ├── AccountSettings.vue # 账号设置
│       ├── CDKeyMarket.vue # CDKey市场
│       └── GameDetail.vue  # 游戏详情
└── README.md
```

## 页面路由

| 路径 | 组件 | 说明 |
|------|------|------|
| `/` | Home | 首页 - 带左侧导航栏+轮播图 |
| `/login` | Login | 登录注册 - 深色背景 |
| `/buyer` | BuyerCenter | 买家中心 |
| `/seller` | SellerCenter | 卖家中心 |
| `/settings` | AccountSettings | 账号设置 |
| `/cdkey` | CDKeyMarket | CDKey市场 |
| `/game/:id` | GameDetail | 游戏详情 |

## 设计特点

### 与原始HTML保持一致

1. **首页设计**
   - 左侧固定导航栏（深色背景 #2c3e50）
   - 顶部信息栏（显示钱包余额）
   - 标签页导航
   - 横幅轮播区（左侧轮播图 + 右侧公告栏）
   - 游戏网格展示
   - 响应式设计

2. **登录页设计**
   - 深色背景 (#333333)
   - 简洁的表单设计
   - 标签页切换（账户密码登录/手机号登录）
   - 绿色主题色 (#2ecc71, #27ae60)

3. **其他页面**
   - 使用通用Layout组件
   - 左侧导航栏 + 顶部栏
   - 标签页导航
   - 统一的白色卡片内容区

### 命名规范

- ✅ 所有类名以 `cjx-` 开头
- ✅ 所有ID以 `cjx-` 开头
- ✅ 每个页面包含 `719cjx` 文字标识
- ✅ 所有CSS使用 `scoped`

### 组合式API示例

```vue
<script setup>
import { ref, computed, onMounted } from 'vue'

// 响应式数据
const count = ref(0)

// 计算属性
const double = computed(() => count.value * 2)

// 方法
const increment = () => count.value++

// 生命周期
onMounted(() => {
  // 组件挂载逻辑
})
</script>
```

## 后端API

Supabase配置在 `src/config/supabase.js`：

```javascript
const supabaseUrl = 'https://prvmjufbhsofvnjeswhq.supabase.co'
const supabaseKey = 'sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_'
```

提供的API模块：
- `authAPI` - 用户认证（登录、注册、更新）
- `gameAPI` - 游戏数据查询
- `announcementAPI` - 公告获取
- `orderAPI` - 订单管理
- `sellerAPI` - 卖家额度查询

## 安装和运行

```bash
# 进入项目目录
cd steampy-vue

# 安装依赖（优先使用cnpm）
cnpm install

# 如果cnpm失败，使用npm
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 数据库初始化

在 Supabase SQL编辑器中执行以下文件：
1. `../supabase-schema.sql` - 创建数据表结构
2. `../supabase-init-data.sql` - 插入初始数据

## 测试账号

| 用户名 | 密码 | 类型 |
|--------|------|------|
| admin | admin123 | 管理员 |
| 123moyingzai | password123 | 普通用户 |
| seller01 | seller123 | 卖家 |
| buyer01 | buyer123 | 买家 |

## 特性

- 719cjx 标识已添加到每个页面
- 所有CSS使用 scoped 样式
- 响应式设计
- 用户状态持久化（sessionStorage）
- 通用Layout组件复用
- 与原始HTML设计保持一致

## 注意事项

1. 确保已安装 Node.js 16+
2. 优先使用 cnpm 安装依赖
3. 数据库表需要先创建才能正常使用
4. 首页有自己的布局，其他页面使用Layout组件

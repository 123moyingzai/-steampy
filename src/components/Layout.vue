<template>
  <div class="cjx-layout-container">
    <!-- 顶部导航栏 -->
    <header class="cjx-top-nav">
      <!-- 左侧 Logo -->
      <div class="cjx-nav-logo" @click="$router.push('/')">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
        <span>Steam PY匹歪</span>
      </div>

      <!-- 中间导航菜单 -->
      <nav class="cjx-nav-menu">
        <div class="cjx-nav-group">
          <span class="cjx-nav-group-title">市场</span>
          <div class="cjx-nav-items">
            <a class="cjx-nav-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
              <span>首页</span>
            </a>
            <a class="cjx-nav-item" :class="{ active: $route.path === '/cdkey' }" @click="$router.push('/cdkey')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M3 3h18v2H3zm0 16h18v2H3zm0-8h18v2H3z"/></svg>
              <span>CDK国区</span>
            </a>
            <a class="cjx-nav-item" :class="{ active: $route.path === '/gift' }" @click="$router.push('/gift')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M18 4h2v16h-2zM4 4h14v2H4zm0 14h14v2H4zm0-7h14v2H4z"/></svg>
              <span>礼物代购</span>
            </a>
            <a class="cjx-nav-item" :class="{ active: $route.path === '/balance' }" @click="$router.push('/balance')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm0-4h-2V7h2v8z"/></svg>
              <span>余额购</span>
            </a>
          </div>
        </div>
        <div class="cjx-nav-divider"></div>
        <div class="cjx-nav-group">
          <span class="cjx-nav-group-title">用户中心</span>
          <div class="cjx-nav-items">
            <a class="cjx-nav-item" :class="{ active: $route.path === '/buyer' }" @click="$router.push('/buyer')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
              <span>买家中心</span>
            </a>
            <a class="cjx-nav-item" :class="{ active: $route.path === '/seller' }" @click="$router.push('/seller')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/></svg>
              <span>卖家中心</span>
            </a>
            <a class="cjx-nav-item" :class="{ active: $route.path === '/settings' }" @click="$router.push('/settings')">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/></svg>
              <span>账号设置</span>
            </a>
          </div>
        </div>
      </nav>

      <!-- 右侧钱包 + 用户 -->
      <div class="cjx-nav-right">
        <div class="cjx-wallet-info">
          <span class="cjx-wallet-label">钱包</span>
          <span class="cjx-wallet-balance">¥{{ walletBalance.toFixed(2) }}</span>
          <button class="cjx-top-btn cjx-withdraw">提现</button>
          <button class="cjx-top-btn cjx-details" @click="$router.push('/transactions')">明细</button>
        </div>
        <div class="cjx-user-actions">
          <svg class="cjx-action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
            <polyline points="22,6 12,13 2,6"></polyline>
          </svg>
          <svg class="cjx-action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
          <div class="cjx-avatar-menu-container">
            <div class="cjx-user-avatar" @click="toggleMenu">{{ avatarText }}</div>
            <div class="cjx-dropdown-menu" :class="{ active: showMenu }">
              <template v-if="isLoggedIn">
                <div class="cjx-username-display">{{ currentUser?.nickname || currentUser?.username || '游客' }}</div>
                <a class="cjx-dropdown-item" @click="$router.push('/settings')">账号设置</a>
                <a class="cjx-dropdown-item" @click="$router.push('/settings')">修改密码</a>
                <div class="cjx-dropdown-divider"></div>
                <a class="cjx-dropdown-item" @click="handleLogout">退出登录</a>
              </template>
              <template v-else>
                <div class="cjx-username-display">未登录</div>
                <a class="cjx-dropdown-item" @click="goToLogin">点击登录</a>
              </template>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="cjx-main-content">
      <div class="cjx-page-content">
        <slot></slot>
      </div>

      <!-- 底部页脚 -->
      <footer class="cjx-footer">
        <p>进入网页版新澡堂(测试) &nbsp;|&nbsp; 欢迎交流 官方QQ群 807662430</p>
        <p>用户协议 | 隐私政策</p>
      </footer>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../config/supabase-local.ts'

const router = useRouter()

// 响应式数据
const currentUser = ref(null)
const walletBalance = ref(0.06)
const showMenu = ref(false)

// 计算属性
const avatarText = computed(() => {
  const name = currentUser.value?.nickname || currentUser.value?.username || '游'
  return name.substring(0, 2)
})

const isLoggedIn = computed(() => {
  return currentUser.value && currentUser.value.id
})

// 方法
const toggleMenu = () => {
  showMenu.value = !showMenu.value
}

const goToLogin = () => {
  showMenu.value = false
  router.push('/login')
}

const handleLogout = () => {
  authAPI.logout()
  currentUser.value = null
  router.push('/login')
}

// 点击外部关闭菜单
const closeMenu = (e) => {
  if (!e.target.closest('.cjx-avatar-menu-container')) {
    showMenu.value = false
  }
}

// 加载数据
const loadData = () => {
  currentUser.value = authAPI.getCurrentUser()
  if (currentUser.value) {
    walletBalance.value = currentUser.value.wallet_balance || 0.06
  }
}

// 生命周期
onMounted(() => {
  loadData()
  document.addEventListener('click', closeMenu)

  // 监听sessionStorage变化，更新用户状态
  window.addEventListener('storage', (e) => {
    if (e.key === 'steampy_user') {
      currentUser.value = authAPI.getCurrentUser()
      if (currentUser.value) {
        walletBalance.value = currentUser.value.wallet_balance || 0.06
      }
    }
  })

  // 监听自定义登录/登出事件
  window.addEventListener('user-logged-in', () => {
    currentUser.value = authAPI.getCurrentUser()
    if (currentUser.value) {
      walletBalance.value = currentUser.value.wallet_balance || 0.06
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})
</script>

<style scoped>
.cjx-layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航栏 */
.cjx-top-nav {
  height: 64px;
  background-color: #2c3e50;
  color: #ecf0f1;
  display: flex;
  align-items: center;
  padding: 0 1.5rem;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  gap: 1.5rem;
}

/* Logo */
.cjx-nav-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.15rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
}

.cjx-nav-logo svg {
  width: 26px;
  height: 26px;
}

/* 中部导航菜单 */
.cjx-nav-menu {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 1rem;
  overflow: hidden;
}

.cjx-nav-group {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.cjx-nav-group-title {
  font-size: 0.75rem;
  color: #95a5a6;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.cjx-nav-items {
  display: flex;
  align-items: center;
  gap: 0.15rem;
}

.cjx-nav-divider {
  width: 1px;
  height: 24px;
  background-color: #34495e;
}

.cjx-nav-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #ecf0f1;
  text-decoration: none;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.cjx-nav-item svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.cjx-nav-item:hover {
  background-color: #34495e;
}

.cjx-nav-item.active {
  background-color: #34495e;
  color: #fff;
}

/* 右侧区域 */
.cjx-nav-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.cjx-wallet-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: #34495e;
  padding: 0.4rem 0.75rem;
  border-radius: 4px;
}

.cjx-wallet-label {
  font-size: 0.8rem;
  color: #bdc3c7;
}

.cjx-wallet-balance {
  font-weight: bold;
  color: #2ecc71;
  font-size: 0.9rem;
}

.cjx-top-btn {
  padding: 0.3rem 0.75rem;
  border-radius: 3px;
  border: none;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.cjx-withdraw {
  background-color: #3498db;
  color: white;
}

.cjx-withdraw:hover {
  background-color: #2980b9;
}

.cjx-details {
  background-color: #7f8c8d;
  color: white;
}

.cjx-details:hover {
  background-color: #5d6d7e;
}

.cjx-user-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.cjx-action-icon {
  width: 20px;
  height: 20px;
  cursor: pointer;
  color: #bdc3c7;
  transition: color 0.2s;
}

.cjx-action-icon:hover {
  color: #ecf0f1;
}

.cjx-avatar-menu-container {
  position: relative;
}

.cjx-user-avatar {
  width: 36px;
  height: 36px;
  background-color: #3498db;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  cursor: pointer;
  font-size: 12px;
}

.cjx-dropdown-menu {
  position: absolute;
  top: 45px;
  right: 0;
  width: 160px;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  display: none;
  z-index: 200;
}

.cjx-dropdown-menu.active {
  display: block;
}

.cjx-username-display {
  padding: 0 16px 8px;
  font-size: 0.8rem;
  color: #666;
  border-bottom: 1px solid #eee;
  margin-bottom: 4px;
}

.cjx-dropdown-item {
  display: block;
  padding: 10px 16px;
  font-size: 0.9rem;
  color: #333;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cjx-dropdown-item:hover {
  background-color: #f5f5f5;
}

.cjx-dropdown-divider {
  height: 1px;
  background-color: #eee;
  margin: 4px 0;
}

/* 主内容区 */
.cjx-main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.cjx-page-content {
  flex: 1;
  padding: 1.5rem 2rem;
}

/* 底部页脚 */
.cjx-footer {
  padding: 1.5rem 2rem;
  text-align: center;
  font-size: 0.8rem;
  color: #999;
  border-top: 1px solid #e0e0e0;
  background-color: #fafafa;
}

.cjx-footer p {
  margin: 0.25rem 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .cjx-top-nav {
    flex-wrap: wrap;
    height: auto;
    padding: 0.75rem 1rem;
    gap: 0.75rem;
  }

  .cjx-nav-menu {
    order: 3;
    flex-basis: 100%;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 0.25rem;
  }

  .cjx-nav-divider {
    display: none;
  }

  .cjx-page-content {
    padding: 1rem;
  }
}

@media (max-width: 768px) {
  .cjx-nav-group-title {
    display: none;
  }

  .cjx-wallet-label {
    display: none;
  }

  .cjx-nav-item span {
    font-size: 0.85rem;
  }
}
</style>

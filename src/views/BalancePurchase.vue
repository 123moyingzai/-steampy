<template>
  <div class="cjx-page-container">
    <!-- 左侧导航栏 -->
    <aside class="cjx-sidebar">
      <div class="cjx-sidebar-logo">
        <h1 @click="$router.push('/')">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
          <span>Steam PY匹歪</span>
        </h1>
      </div>
      <nav class="cjx-sidebar-menu">
        <div class="cjx-menu-title">市场</div>
        <div class="cjx-menu-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
            </svg>
            <span>首页页面 719cjx</span>
          </span>
        </div>
        <div class="cjx-menu-item" :class="{ active: $route.path === '/cdkey' }" @click="$router.push('/cdkey')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 3h18v2H3zm0 16h18v2H3zm0-8h18v2H3z"/>
            </svg>
            <span>cdk国区 719cjx</span>
          </span>
        </div>
        <div class="cjx-menu-item" :class="{ active: $route.path === '/gift' }" @click="$router.push('/gift')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M18 4h2v16h-2zM4 4h14v2H4zm0 14h14v2H4zm0-7h14v2H4z"/>
            </svg>
            <span>礼物代购</span>
          </span>
        </div>
        <div class="cjx-menu-item active" @click="$router.push('/balance')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm0-4h-2V7h2v8z"/>
            </svg>
            <span>余额购</span>
          </span>
        </div>
        
        <div class="cjx-menu-title">用户中心</div>
        <div class="cjx-menu-item" :class="{ active: $route.path === '/buyer' }" @click="$router.push('/buyer')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>
            </svg>
            <span>买家中心 719cjx</span>
          </span>
        </div>
        <div class="cjx-menu-item" :class="{ active: $route.path === '/seller' }" @click="$router.push('/seller')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/>
            </svg>
            <span>卖家中心 719cjx</span>
          </span>
        </div>
        <div class="cjx-menu-item" :class="{ active: $route.path === '/settings' }" @click="$router.push('/settings')">
          <span class="cjx-menu-link">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
            </svg>
            <span>账号设置 719cjx</span>
          </span>
        </div>
      </nav>
      <div class="cjx-sidebar-footer">
        <p>进入网页版新澡堂(测试)</p>
        <p>欢迎交流 官方QQ群<br>807662430</p>
        <p>用户协议 | 隐私政策 719cjx</p>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="cjx-main-content">
      <!-- 顶部信息栏 -->
      <div class="cjx-top-bar">
        <div class="cjx-wallet-info">
          <span>钱包金额: </span>
          <span class="cjx-wallet-balance">¥{{ walletBalance.toFixed(2) }}</span>
          <button class="cjx-top-btn cjx-withdraw">提现</button>
          <button class="cjx-top-btn cjx-details">明细</button>
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

      <!-- 页面标题 -->
      <div class="cjx-page-header">
        <h1>余额购-国区 719cjx</h1>
        <p>使用Steam钱包余额购买游戏，享受超低折扣</p>
      </div>

      <!-- 内容区域 -->
      <div class="cjx-content-wrapper">
        <!-- 左侧游戏列表 -->
        <div class="cjx-main-area">
          <!-- 筛选栏 -->
          <div class="cjx-filter-bar">
            <span class="cjx-filter-label">排序:</span>
            <select class="cjx-filter-select" v-model="sortBy">
              <option value="price-asc">价格从低到高</option>
              <option value="price-desc">价格从高到低</option>
              <option value="discount">折扣力度</option>
            </select>
          </div>

          <div class="cjx-game-grid">
            <div class="cjx-game-card" v-for="(game, index) in sortedGames" :key="index" @click="buyGame(game)">
              <div class="cjx-game-img-wrapper">
                <img :src="getImageUrl(game.image)" :alt="game.name" class="cjx-game-img">
                <span class="cjx-discount-badge" v-if="game.discount">{{ game.discount }}</span>
              </div>
              <div class="cjx-game-info">
                <h3 class="cjx-game-title">{{ game.name }}</h3>
                <div class="cjx-game-meta">
                  <span class="cjx-tag">余额购</span>
                  <span class="cjx-tag">国区</span>
                </div>
                <div class="cjx-game-price">
                  <span class="cjx-current-price">{{ game.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧说明栏 -->
        <div class="cjx-sidebar-area">
          <div class="cjx-balance-card">
            <h3>我的余额 719cjx</h3>
            <div class="cjx-balance-amount">¥{{ walletBalance.toFixed(2) }}</div>
            <button class="cjx-recharge-btn" @click="recharge">立即充值</button>
          </div>

          <div class="cjx-info-box">
            <h3>什么是余额购?</h3>
            <p>余额购是使用Steam账户钱包余额购买游戏的方式。通过SteamPY平台，您可以以更低的价格获得Steam余额，然后用于购买游戏。</p>
          </div>

          <div class="cjx-notice-box">
            <h3>购买须知</h3>
            <ul>
              <li>仅限国区Steam账户使用</li>
              <li>余额将在交易完成后直接充入您的Steam账户</li>
              <li>请确保您的Steam账户可以接收市场交易</li>
              <li>如有问题请联系客服</li>
            </ul>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { authAPI } from '../config/supabase-local.ts'

const router = useRouter()

// 响应式数据
const currentUser = ref(null)
const walletBalance = ref(0.06)
const showMenu = ref(false)
const games = ref([])
const sortBy = ref('price-asc')

// 计算属性
const avatarText = computed(() => {
  const name = currentUser.value?.nickname || currentUser.value?.username || '游'
  return name.substring(0, 2)
})

const isLoggedIn = computed(() => {
  return currentUser.value && currentUser.value.id
})

// 排序后的游戏列表
const sortedGames = computed(() => {
  let sorted = [...games.value]
  switch (sortBy.value) {
    case 'price-asc':
      sorted.sort((a, b) => parseFloat(a.price.replace(/[^0-9.]/g, '')) - parseFloat(b.price.replace(/[^0-9.]/g, '')))
      break
    case 'price-desc':
      sorted.sort((a, b) => parseFloat(b.price.replace(/[^0-9.]/g, '')) - parseFloat(a.price.replace(/[^0-9.]/g, '')))
      break
    case 'discount':
      sorted.sort((a, b) => {
        const discountA = parseInt(a.discount?.replace(/[^0-9-]/g, '') || 0)
        const discountB = parseInt(b.discount?.replace(/[^0-9-]/g, '') || 0)
        return discountA - discountB
      })
      break
  }
  return sorted
})

// 获取图片URL - 处理本地图片路径
const getImageUrl = (path) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  // 处理 ../picture/xxx.jpg 或 picture/xxx.jpg -> /picture/xxx.jpg
  if (path.includes('picture/')) {
    const fileName = path.split('picture/')[1]
    if (fileName) {
      return `/picture/${fileName}`
    }
  }
  return path.startsWith('/') ? path : `/${path}`
}

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

const buyGame = (game) => {
  // 跳转到余额购详情页
  const gameStr = encodeURIComponent(JSON.stringify(game))
  router.push(`/balance/detail/${gameStr}`)
}

const recharge = () => {
  alert('充值功能请在APP中使用 719cjx')
}

// 点击外部关闭菜单
const closeMenu = (e) => {
  if (!e.target.closest('.cjx-avatar-menu-container')) {
    showMenu.value = false
  }
}

// 加载数据
const loadData = async () => {
  currentUser.value = authAPI.getCurrentUser()
  if (currentUser.value) {
    walletBalance.value = currentUser.value.wallet_balance || 0.06
  }

  // 加载游戏数据
  try {
    const response = await axios.get('/cdk_games.json')
    if (response.data) {
      const data = response.data
      games.value = [...(data.preSaleItems || []), ...(data.gameItems || [])]
    } else {
      // 默认数据
      games.value = [
        { name: 'Fullbright Pres', price: '¥6', discount: '-77%', image: './picture/Fullbright Pres.jpg' },
        { name: '你的另一个老婆', price: '¥14.7', discount: '-18%', image: './picture/你的另一个老婆.jpg' },
        { name: '东方奇缘记', price: '¥2.5', discount: '-86%', image: './picture/东方奇缘记.jpg' },
        { name: '银河守卫战', price: '¥18.9', discount: '-54%', image: './picture/银河守卫战.jpg' },
        { name: '我与她们的大学画像', price: '¥16.55', discount: '-17%', image: './picture/我与她们的大学画像.jpg' },
        { name: '三更', price: '¥4.38', discount: '-27%', image: './picture/三更.jpg' }
      ]
    }
  } catch (error) {
    console.error('加载游戏数据失败:', error)
    games.value = [
      { name: 'Fullbright Pres', price: '¥6', discount: '-77%', image: './picture/Fullbright Pres.jpg' },
      { name: '你的另一个老婆', price: '¥14.7', discount: '-18%', image: './picture/你的另一个老婆.jpg' }
    ]
  }
}

// 生命周期
onMounted(() => {
  loadData()
  document.addEventListener('click', closeMenu)
  
  // 监听登录/登出事件，更新用户状态
  window.addEventListener('user-logged-in', () => {
    currentUser.value = authAPI.getCurrentUser()
  })
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})
</script>

<style scoped>
.cjx-page-container {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

/* 左侧导航栏 */
.cjx-sidebar {
  width: 220px;
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 1.5rem 0;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  overflow-y: auto;
  z-index: 100;
}

.cjx-sidebar-logo {
  padding: 0 1.5rem 1.5rem;
  border-bottom: 1px solid #34495e;
  margin-bottom: 1rem;
}

.cjx-sidebar-logo h1 {
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  cursor: pointer;
}

.cjx-sidebar-logo svg {
  width: 24px;
  height: 24px;
}

.cjx-sidebar-menu {
  padding: 0 0.5rem;
  list-style: none;
}

.cjx-menu-title {
  font-size: 0.9rem;
  color: #bdc3c7;
  padding: 1rem 1.5rem 0.5rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.cjx-menu-item {
  margin-bottom: 0.3rem;
  border-radius: 4px;
  transition: background-color 0.2s;
  cursor: pointer;
}

.cjx-menu-item:hover,
.cjx-menu-item.active {
  background-color: #34495e;
}

.cjx-menu-link {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.8rem 1.5rem;
  color: #ecf0f1;
  text-decoration: none;
  cursor: pointer;
}

.cjx-menu-item svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.cjx-sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid #34495e;
  margin-top: 1rem;
  font-size: 0.8rem;
  color: #bdc3c7;
}

.cjx-sidebar-footer p {
  margin-bottom: 0.5rem;
}

/* 主内容区 */
.cjx-main-content {
  flex: 1;
  margin-left: 220px;
  padding-bottom: 2rem;
  width: calc(100% - 220px);
  min-height: 100vh;
}

/* 顶部信息栏 */
.cjx-top-bar {
  height: 60px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
}

.cjx-wallet-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.cjx-wallet-balance {
  font-weight: bold;
  color: #27ae60;
}

.cjx-top-btn {
  padding: 0.4rem 1rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
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
  background-color: #95a5a6;
  color: white;
}

.cjx-details:hover {
  background-color: #7f8c8d;
}

.cjx-user-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.cjx-action-icon {
  width: 20px;
  height: 20px;
  cursor: pointer;
  color: #666;
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
  z-index: 100;
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

/* 页面标题 */
.cjx-page-header {
  padding: 2rem;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.cjx-page-header h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.cjx-page-header p {
  font-size: 1rem;
  opacity: 0.9;
}

/* 内容区域 */
.cjx-content-wrapper {
  padding: 2rem;
  display: flex;
  gap: 2rem;
}

.cjx-main-area {
  flex: 3;
}

.cjx-sidebar-area {
  flex: 1;
}

/* 筛选栏 */
.cjx-filter-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.cjx-filter-label {
  font-weight: 500;
  color: #666;
}

.cjx-filter-select {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
}

/* 游戏网格 */
.cjx-game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.cjx-game-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
  cursor: pointer;
}

.cjx-game-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.cjx-game-img-wrapper {
  position: relative;
  height: 120px;
}

.cjx-game-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cjx-discount-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #e74c3c;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.cjx-game-info {
  padding: 1rem;
}

.cjx-game-title {
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cjx-game-meta {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.cjx-tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background-color: #e8f5e9;
  color: #2e7d32;
  border-radius: 4px;
}

.cjx-game-price {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cjx-current-price {
  font-size: 1.2rem;
  font-weight: bold;
  color: #e74c3c;
}

/* 侧边栏卡片 */
.cjx-balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  text-align: center;
}

.cjx-balance-card h3 {
  font-size: 1rem;
  margin-bottom: 0.5rem;
  opacity: 0.9;
}

.cjx-balance-amount {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.cjx-recharge-btn {
  width: 100%;
  padding: 0.75rem;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s;
}

.cjx-recharge-btn:hover {
  transform: scale(1.02);
}

.cjx-info-box,
.cjx-notice-box {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.cjx-info-box h3,
.cjx-notice-box h3 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: #2c3e50;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #27ae60;
}

.cjx-info-box p {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.6;
}

.cjx-notice-box ul {
  padding-left: 1.5rem;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.8;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .cjx-sidebar {
    width: 60px;
    padding: 1rem 0;
  }
  
  .cjx-sidebar-logo h1 span,
  .cjx-menu-link span,
  .cjx-menu-title,
  .cjx-sidebar-footer {
    display: none;
  }
  
  .cjx-sidebar-logo {
    padding: 0 0.5rem 1rem;
    display: flex;
    justify-content: center;
  }
  
  .cjx-menu-link {
    justify-content: center;
    padding: 0.8rem;
  }
  
  .cjx-main-content {
    margin-left: 60px;
    width: calc(100% - 60px);
  }
  
  .cjx-content-wrapper {
    flex-direction: column;
  }
  
  .cjx-game-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

@media (max-width: 768px) {
  .cjx-page-header {
    padding: 1.5rem;
  }
  
  .cjx-page-header h1 {
    font-size: 1.5rem;
  }
  
  .cjx-filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

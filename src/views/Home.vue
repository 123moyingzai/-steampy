<template>
  <div class="cjx-home-container">
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
        <div class="cjx-menu-item" :class="{ active: $route.path === '/balance' }" @click="$router.push('/balance')">
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

      <!-- 标签页导航 -->
      <div class="cjx-tabs-nav">
        <ul class="cjx-tabs-list">
          <li class="cjx-tab-item active" @click="$router.push('/')">首页 719cjx</li>
          <li class="cjx-tab-item" @click="$router.push('/buyer')">买家中心</li>
          <li class="cjx-tab-item" @click="$router.push('/seller')">卖家中心</li>
          <li class="cjx-tab-item" @click="$router.push('/balance')">余额购-国区</li>
          <li class="cjx-tab-item" @click="$router.push('/cdkey')">CDKey市场</li>
        </ul>
      </div>

      <!-- 横幅轮播区 -->
      <div class="cjx-banner">
        <div class="cjx-banner-left">
          <div class="cjx-slider-container">
            <div class="cjx-slider-wrapper">
              <div 
                class="cjx-slide" 
                v-for="(slide, index) in slides" 
                :key="index"
                :class="{ active: currentSlide === index }"
              >
                <img :src="getImageUrl(slide.imageUrl)" :alt="slide.altText">
              </div>
            </div>
            <button class="cjx-slider-btn cjx-prev-btn" @click="prevSlide">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z"/>
              </svg>
            </button>
            <button class="cjx-slider-btn cjx-next-btn" @click="nextSlide">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
              </svg>
            </button>
            <div class="cjx-slider-dots">
              <span 
                v-for="(slide, index) in slides" 
                :key="index"
                class="cjx-dot"
                :class="{ active: currentSlide === index }"
                @click="setSlide(index)"
              ></span>
            </div>
          </div>
        </div>
        <div class="cjx-banner-right">
          <h3 class="cjx-announcement-title">公告栏 719cjx</h3>
          <ul class="cjx-announcement-list">
            <li class="cjx-announcement-item" v-for="(item, index) in announcements" :key="index">
              <strong>{{ item.date }}</strong> {{ item.title }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="cjx-content-wrapper">
        <!-- 主要内容 -->
        <div class="cjx-main-area">
          <div class="cjx-banner-img">
            <img :src="getImageUrl('picture/屏幕截图 2025-12-03 134029.png')" alt="买家请使用APP">
          </div>


        </div>

        <!-- 右侧边栏 -->
        <div class="cjx-sidebar-area">


        </div>
      </div>

      <!-- 页面底部区域 -->
      <div class="cjx-footer-section">
        <!-- 关于平台 -->
        <div class="cjx-footer-block">
          <h3 class="cjx-footer-title">
            <span class="cjx-title-bar"></span>
            关于平台
          </h3>
          <div class="cjx-footer-content">
            <p>本平台致力于给每位玩家提供一个安全快速便捷的交易平台，来共享出自己账号中的额度</p>
            <p>在平台上，你可以轻松查询自己的代购额度，并且挂出自己的账号余额，提供给有需要的玩家购买</p>
            <p>也可在平台市场中挑选自己心仪的卖家和折扣，支付金额、确认好友请求后手动收取礼物</p>
            <p>平台使用的账户信息直接对接steam接口，安全可靠。交易使用全自动流程，方便快捷，避免普通玩家之间交易产生的欺骗纠纷 719cjx</p>
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
import { fetchAllGames, announcementAPI, walletAPI } from '../config/supabase-local.ts'

const router = useRouter()

// 响应式数据
const currentUser = ref(null)
const walletBalance = ref(0.06)
const showMenu = ref(false)
const slides = ref([])
const currentSlide = ref(0)
const announcements = ref([])
const sideAnnouncements = ref([])
const games = ref([])

let slideInterval = null

// 计算属性
const avatarText = computed(() => {
  const name = currentUser.value?.nickname || currentUser.value?.username || '游'
  return name.substring(0, 2)
})

const isLoggedIn = computed(() => {
  return currentUser.value && currentUser.value.id
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

const setSlide = (index) => {
  currentSlide.value = index
}

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % slides.value.length
}

const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + slides.value.length) % slides.value.length
}

const goToGame = (game) => {
  if (game.link) {
    // 如果是外部链接或HTML文件，在新窗口打开
    if (game.link.includes('.html')) {
      window.location.href = game.link
    } else {
      router.push(game.link)
    }
  } else {
    // 默认跳转到CDKey市场
    router.push('/cdkey')
  }
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

// 加载轮播图数据
const loadSliderData = async () => {
  try {
    const response = await axios.get('/slider-data.json')
    if (response.data) {
      slides.value = response.data.slides || []
    } else {
      // 默认数据
      slides.value = [
        { imageUrl: './picture/gun3.png', altText: '开发者支持计划' },
        { imageUrl: './picture/gun2.png', altText: '热门游戏推荐' },
        { imageUrl: './picture/gun1.png', altText: '限时特惠' }
      ]
    }
  } catch (error) {
    console.error('加载轮播图数据失败:', error)
    // 默认数据
    slides.value = [
      { imageUrl: './picture/gun3.png', altText: '开发者支持计划' },
      { imageUrl: './picture/gun2.png', altText: '热门游戏推荐' },
      { imageUrl: './picture/gun1.png', altText: '限时特惠' }
    ]
  }
}

// 加载游戏数据 - 从 Supabase 数据库获取
const loadGamesData = async () => {
  try {
    // 优先从 Supabase 获取数据
    const allGames = await fetchAllGames()
    
    if (allGames && allGames.length > 0) {
      games.value = allGames
      console.log(`✓ 从 Supabase 加载了 ${allGames.length} 条游戏数据`)
    } else {
      // 如果 Supabase 没有数据，尝试从本地 JSON 文件加载
      const response = await axios.get('/cdk_games.json')
      if (response.data) {
        const data = response.data
        const allLocalGames = [...(data.preSaleItems || []), ...(data.gameItems || [])]
        games.value = allLocalGames
        console.log(`✓ 从本地 JSON 加载了 ${allLocalGames.length} 条游戏数据`)
      } else {
        // 使用默认数据
        throw new Error('无法加载数据')
      }
    }
  } catch (error) {
    console.warn('从数据库/JSON 加载失败，使用默认数据:', error)
    // 使用默认数据
    games.value = [
      { name: '艾尔登法环 标准版', price: '¥298.00', image: './picture/header.jpg' },
      { name: '赛博朋克2077 终极版', price: '¥198.00', image: './picture/header_schinese1.jpg' },
      { name: '黑神话：悟空 数字版', price: '¥268.00', image: './picture/header_schinese.jpg' }
    ]
  }
}

// 加载用户数据 - 从 Supabase 数据库获取
const loadUserData = async () => {
  currentUser.value = authAPI.getCurrentUser()
  
  if (currentUser.value) {
    try {
      const userId = currentUser.value.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
      const result = await walletAPI.getBalance(userId)
      
      if (result.data !== undefined) {
        walletBalance.value = parseFloat(result.data)
      } else {
        walletBalance.value = currentUser.value.wallet_balance || 0.06
      }
    } catch (error) {
      console.error('加载余额失败:', error)
      walletBalance.value = currentUser.value.wallet_balance || 0.06
    }
  }
}

// 加载公告数据 - 从 Supabase 数据库获取
const loadAnnouncements = async () => {
  try {
    const result = await announcementAPI.getAnnouncements(10)
    
    if (result.data && result.data.length > 0) {
      announcements.value = result.data.map(a => ({
        date: a.publish_date,
        title: a.title
      }))
      sideAnnouncements.value = result.data.slice(0, 5).map(a => ({
        title: a.title
      }))
      console.log(`✓ 从数据库加载了 ${result.data.length} 条公告`)
    }
  } catch (error) {
    console.error('从数据库加载公告失败，使用默认数据:', error)
    
    // 使用默认公告数据
    announcements.value = [
      { date: '2025-12-10', title: '系统维护通知，预计今晚20:00-22:00进行升级。' },
      { date: '2025-12-09', title: '新游戏《艾尔登法环》DLC现已上架，限时优惠。' },
      { date: '2025-12-08', title: '圣诞节特惠活动即将开启，敬请期待。' },
      { date: '2025-12-07', title: '用户安全提醒：请勿共享账号密码。' },
      { date: '2025-12-06', title: '新增支付方式：支持支付宝、微信支付。' }
    ]
    
    sideAnnouncements.value = [
      { title: '关于优化CDKey自动发货系统的通知' },
      { title: '2023年春节假期平台运营安排' },
      { title: '新用户注册福利活动开启' },
      { title: '平台手续费调整公告（2023.03.01生效）' },
      { title: 'APP版本更新说明（v2.5.0）' }
    ]
  }
}

// 加载所有数据
const loadData = async () => {
  await loadUserData()
  await loadSliderData()
  await loadGamesData()
  await loadAnnouncements()
  await loadUserSettings()
}

// 加载用户配置数据
const loadUserSettings = async () => {
  try {
    const response = await axios.get('/user-settings.json')
    if (response.data) {
      const settings = response.data
      console.log('✓ 从JSON加载了用户配置数据', settings)
      
      // 可以将配置存储到响应式变量中供页面使用
      userSettings.value = settings
    }
  } catch (error) {
    console.warn('加载用户配置失败:', error)
  }
}

// 响应式变量
const userSettings = ref({})

// 生命周期
onMounted(() => {
  loadData()
  slideInterval = setInterval(nextSlide, 5000)
  document.addEventListener('click', closeMenu)
  
  // 监听登录/登出事件，更新用户状态
  window.addEventListener('user-logged-in', () => {
    currentUser.value = authAPI.getCurrentUser()
  })
})

onUnmounted(() => {
  if (slideInterval) clearInterval(slideInterval)
  document.removeEventListener('click', closeMenu)
})
</script>

<style scoped>
.cjx-home-container {
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

/* 标签页导航 */
.cjx-tabs-nav {
  background-color: #f1f3f5;
  padding: 0 2rem;
  border-bottom: 1px solid #e9ecef;
}

.cjx-tabs-list {
  display: flex;
  gap: 0.5rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.cjx-tab-item {
  padding: 1rem 1.5rem;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.cjx-tab-item.active {
  border-bottom-color: #3498db;
  color: #3498db;
  font-weight: 500;
}

.cjx-tab-item:hover:not(.active) {
  background-color: #e9ecef;
}

/* 横幅轮播区 */
.cjx-banner {
  padding: 1.5rem 2rem;
  display: flex;
  gap: 2rem;
}

.cjx-banner-left {
  flex: 0 0 70%;
}

.cjx-banner-right {
  flex: 0 0 30%;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.cjx-announcement-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #eee;
}

.cjx-slider-container {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  height: 300px;
}

.cjx-slider-wrapper {
  height: 100%;
  position: relative;
}

.cjx-slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.5s ease-in-out;
}

.cjx-slide.active {
  opacity: 1;
}

.cjx-slide img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #2c3e50;
}

.cjx-slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.3s;
}

.cjx-slider-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.cjx-slider-btn svg {
  width: 24px;
  height: 24px;
}

.cjx-prev-btn {
  left: 15px;
}

.cjx-next-btn {
  right: 15px;
}

.cjx-slider-dots {
  position: absolute;
  bottom: 15px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 8px;
}

.cjx-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background 0.3s;
}

.cjx-dot.active {
  background: white;
}

.cjx-announcement-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.cjx-announcement-item {
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 0.95rem;
  color: #555;
}

.cjx-announcement-item:last-child {
  border-bottom: none;
}

.cjx-announcement-item strong {
  color: #3498db;
}

/* 内容区域 */
.cjx-content-wrapper {
  padding: 0 2rem;
  display: flex;
  gap: 2rem;
}

.cjx-main-area {
  flex: 3;
}

.cjx-sidebar-area {
  flex: 1;
}

.cjx-banner-img {
  margin-bottom: 2rem;
}

.cjx-banner-img img {
  width: 100%;
  border-radius: 8px;
}

.cjx-section-title {
  font-size: 1.3rem;
  margin: 2rem 0 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f1f3f5;
}

.cjx-game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.cjx-game-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
  cursor: pointer;
}

.cjx-game-card:hover {
  transform: translateY(-3px);
}

.cjx-game-card-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.cjx-game-card-info {
  padding: 1rem;
}

.cjx-game-card-title {
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cjx-game-card-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 1rem;
  margin: 0;
}

.cjx-game-card-discount {
  color: #27ae60;
  font-size: 0.85rem;
  margin: 0.25rem 0 0 0;
}

.cjx-announcement-box {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1.2rem;
  margin-bottom: 2rem;
}

.cjx-announcement-box h3 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cjx-announcement-box h3::before {
  content: '';
  width: 4px;
  height: 16px;
  background-color: #e74c3c;
  border-radius: 2px;
}

.cjx-announcement-box li {
  padding: 0.6rem 0;
  border-bottom: 1px dashed #e9ecef;
  font-size: 0.9rem;
}

.cjx-announcement-box li:last-child {
  border-bottom: none;
}

.cjx-app-download {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1.2rem;
}

.cjx-app-download h3 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.cjx-qr-codes {
  display: flex;
  gap: 1rem;
  justify-content: center;
  padding: 1rem 0;
}

.cjx-qr-item {
  text-align: center;
}

.cjx-qr-placeholder {
  width: 100px;
  height: 100px;
  background-color: #f1f3f5;
  margin: 0 auto 0.5rem;
}

.cjx-qr-item p {
  font-size: 0.8rem;
  margin: 0;
}

/* 页面底部区域 */
.cjx-footer-section {
  padding: 2rem;
  margin-top: 2rem;
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.cjx-footer-block {
  margin-bottom: 2rem;
}

.cjx-footer-block:last-child {
  margin-bottom: 0;
}

.cjx-footer-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: #333;
  font-weight: 600;
}

.cjx-title-bar {
  width: 4px;
  height: 18px;
  background-color: #27ae60;
  border-radius: 2px;
}

.cjx-footer-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.cjx-footer-list li {
  padding: 0.6rem 0;
  border-bottom: 1px dashed #e9ecef;
  font-size: 0.9rem;
  color: #555;
}

.cjx-footer-list li:last-child {
  border-bottom: none;
}

.cjx-footer-content {
  line-height: 1.8;
  color: #666;
  font-size: 0.9rem;
}

.cjx-footer-content p {
  margin: 0 0 0.5rem 0;
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
}

@media (max-width: 768px) {
  .cjx-top-bar {
    padding: 0 1rem;
  }
  
  .cjx-tabs-nav {
    padding: 0 1rem;
    overflow-x: auto;
  }
  
  .cjx-tabs-list {
    min-width: max-content;
  }
  
  .cjx-banner,
  .cjx-content-wrapper {
    padding: 1rem;
  }
  
  .cjx-banner {
    flex-direction: column;
  }
  
  .cjx-banner-left,
  .cjx-banner-right {
    flex: none;
    width: 100%;
  }
  
  .cjx-game-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>

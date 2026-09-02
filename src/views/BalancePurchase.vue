<template>
  <Layout>
      <!-- 页面标题 -->
      <div class="cjx-page-header">
        <h1>余额购-国区</h1>
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
            <h3>我的余额</h3>
            <div class="cjx-balance-amount">登录后查看</div>
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
    </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const games = ref([])
const sortBy = ref('price-asc')

const getPrice = (p: any) => {
  if (typeof p === 'number') return p
  return parseFloat(String(p).replace(/[^0-9.]/g, '')) || 0
}

// 排序后的游戏列表
const sortedGames = computed(() => {
  let sorted = [...games.value]
  switch (sortBy.value) {
    case 'price-asc':
      sorted.sort((a, b) => getPrice(a.price) - getPrice(b.price))
      break
    case 'price-desc':
      sorted.sort((a, b) => getPrice(b.price) - getPrice(a.price))
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
const buyGame = (game) => {
  // 跳转到余额购详情页
  const gameStr = encodeURIComponent(JSON.stringify(game))
  router.push(`/balance/detail/${gameStr}`)
}

const recharge = () => {
  alert('充值功能请在APP中使用')
}

// 加载数据
const loadData = async () => {
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
})
</script>

<style scoped>
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

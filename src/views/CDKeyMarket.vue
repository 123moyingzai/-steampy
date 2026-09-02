<template>
  <Layout>
    <div class="cjx-cdkey-page">
      <!-- 预售专区 -->
      <section class="cjx-section">
        <h2 class="cjx-section-title">
          <span class="cjx-tag-hot">HOT</span>
          预售专区
        </h2>
        <div class="cjx-game-grid">
          <div class="cjx-game-card cjx-presale" v-for="(game, index) in preSaleGames" :key="'pre-'+index" @click="goToGame(game)">
            <img :src="getImageUrl(game.image)" :alt="game.name" class="cjx-game-img">
            <div class="cjx-game-info">
              <h3 class="cjx-game-title">{{ game.name }}</h3>
              <p class="cjx-game-price">{{ game.price }}</p>
              <span v-if="game.discount" class="cjx-game-discount">{{ game.discount }}</span>
              <span class="cjx-presale-tag">预售</span>
            </div>
          </div>
        </div>
      </section>

      <!-- CDKey市场 -->
      <section class="cjx-section">
        <h2 class="cjx-section-title">CDKey市场</h2>
        
        <!-- 筛选栏 -->
        <div class="cjx-filter-bar">
          <SearchBar
            v-model="searchQuery"
            placeholder="搜索游戏名称"
            @search="filterGames"
          />
          <select class="cjx-select" v-model="sortBy" @change="sortGames">
            <option value="price-asc">价格从低到高</option>
            <option value="price-desc">价格从高到低</option>
            <option value="name">名称排序</option>
          </select>
        </div>

        <!-- 加载中 -->
        <LoadingSpinner v-if="loading" text="加载中..." />

        <!-- 空状态 -->
        <EmptyState
          v-else-if="displayGames.length === 0"
          title="暂无游戏"
          description="没有找到匹配的游戏"
        />

        <!-- 游戏列表 -->
        <div class="cjx-game-grid" v-else>
          <div class="cjx-game-card" v-for="(game, index) in displayGames" :key="'game-'+index" @click="goToGame(game)">
            <img :src="getImageUrl(game.image)" :alt="game.name" class="cjx-game-img">
            <div class="cjx-game-info">
              <h3 class="cjx-game-title">{{ game.name }}</h3>
              <p class="cjx-game-price">{{ game.price }}</p>
              <span v-if="game.discount" class="cjx-game-discount">{{ game.discount }}</span>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div class="cjx-load-more" v-if="displayGames.length < filteredGames.length">
          <button class="cjx-btn cjx-btn-secondary" @click="loadMore">加载更多</button>
        </div>
      </section>

      <!-- 分页 -->
      <Pagination
        v-if="totalPages > 1"
        v-model:current-page="currentPage"
        :total-pages="totalPages"
        @change="handlePageChange"
      />
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Layout from '../components/Layout.vue'
import GameCard from '../components/GameCard.vue'
import SearchBar from '../components/SearchBar.vue'
import Pagination from '../components/Pagination.vue'
import EmptyState from '../components/EmptyState.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import { fetchGamesFromSupabase } from '../config/supabase-local.ts'

const router = useRouter()

// 响应式数据
const preSaleGames = ref([])
const games = ref([])
const searchQuery = ref('')
const sortBy = ref('price-asc')
const currentPage = ref(1)
const pageSize = ref(8)
const loading = ref(false)

// 分页变化处理
const handlePageChange = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

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

// 计算属性
const filteredGames = computed(() => {
  let result = [...games.value]
  
  if (searchQuery.value) {
    result = result.filter(g => g.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
  }
  
  // 排序
  const getPrice = (p: any) => {
    if (typeof p === 'number') return p
    return parseFloat(String(p).replace(/[^0-9.]/g, '')) || 0
  }
  if (sortBy.value === 'price-asc') {
    result.sort((a, b) => getPrice(a.price) - getPrice(b.price))
  } else if (sortBy.value === 'price-desc') {
    result.sort((a, b) => getPrice(b.price) - getPrice(a.price))
  } else if (sortBy.value === 'name') {
    result.sort((a, b) => a.name.localeCompare(b.name))
  }
  
  return result
})

const totalPages = computed(() => Math.ceil(filteredGames.value.length / pageSize.value))

const displayGames = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredGames.value.slice(start, start + pageSize.value)
})

// 方法
const filterGames = () => {
  currentPage.value = 1
}

const sortGames = () => {
  currentPage.value = 1
}

const loadMore = () => {
  pageSize.value += 8
}

const goToGame = (game) => {
  // 生成游戏ID（使用游戏名称的URL编码）
  const gameId = encodeURIComponent(game.name)
  // 跳转到Vue详情页
  router.push(`/game/${gameId}`)
}

// 加载数据 - 从 Supabase 数据库获取
const loadData = async () => {
  loading.value = true
  try {
    // 优先从 Supabase 获取数据
    const data = await fetchGamesFromSupabase()
    
    if (data && (data.preSaleItems.length > 0 || data.gameItems.length > 0)) {
      preSaleGames.value = data.preSaleItems
      games.value = data.gameItems
      console.log(`✓ 从 Supabase 加载: ${data.preSaleItems.length} 预售, ${data.gameItems.length} 普通`)
    } else {
      // 如果 Supabase 没有数据，尝试本地 JSON
      const response = await axios.get('/cdk_games.json')
      if (response.data) {
        const localData = response.data
        preSaleGames.value = localData.preSaleItems || []
        games.value = localData.gameItems || []
        console.log('✓ 从本地 JSON 加载')
      } else {
        throw new Error('无法加载数据')
      }
    }
  } catch (error) {
    console.warn('加载失败，使用默认数据:', error)
    // 默认数据
    preSaleGames.value = [
      { name: '生化危机:安魂曲', price: '¥309', discount: '-11%', image: './picture/安魂曲.jpg', link: '三级项目安魂曲详情.html' },
      { name: '生化危机:安魂曲 豪华版', price: '¥354', discount: '-11%', image: './picture/安魂曲.jpg', link: '三级项目安魂曲豪华版详情.html' }
    ]
    games.value = [
      { name: 'Fullbright Pres', price: '¥6', discount: '-77%', image: './picture/Fullbright Pres.jpg' },
      { name: '你的另一个老婆', price: '¥14.7', discount: '-18%', image: './picture/你的另一个老婆.jpg' }
    ]
  }
  loading.value = false
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.cjx-cdkey-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.cjx-section {
  margin-bottom: 30px;
}

.cjx-section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.cjx-tag-hot {
  background: #e74c3c;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.cjx-filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.cjx-search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.cjx-select {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
}

.cjx-game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.cjx-game-card {
  background: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
  position: relative;
}

.cjx-game-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.cjx-game-card.cjx-presale {
  border: 2px solid #e74c3c;
}

.cjx-game-img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.cjx-game-info {
  padding: 15px;
}

.cjx-game-title {
  font-size: 14px;
  margin: 0 0 10px 0;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cjx-game-price {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.cjx-game-original {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
  margin: 5px 0 0 0;
}

.cjx-game-discount {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #e74c3c;
  color: #fff;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.cjx-presale-tag {
  display: inline-block;
  background: #e74c3c;
  color: #fff;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-top: 8px;
}

.cjx-load-more {
  text-align: center;
  margin-top: 30px;
}

.cjx-btn {
  padding: 12px 30px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-btn-secondary {
  background: #f0f0f0;
  color: #666;
}

.cjx-btn-secondary:hover {
  background: #e0e0e0;
}

.cjx-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.cjx-pagination button {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.cjx-pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.cjx-pagination span {
  color: #666;
}
</style>

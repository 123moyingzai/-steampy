<template>
  <Layout>

      <!-- 页面标题 -->
      <div class="cjx-page-header">
        <h1>礼物代购 719cjx</h1>
        <p>通过Steam好友赠送的方式购买游戏，安全可靠</p>
      </div>

      <!-- 内容区域 -->
      <div class="cjx-content-wrapper">
        <!-- 左侧游戏列表 -->
        <div class="cjx-main-area">
          <div class="cjx-game-list">
            <div class="cjx-game-item" v-for="(game, index) in games" :key="index">
              <img :src="getImageUrl(game.image)" :alt="game.name" class="cjx-game-img">
              <div class="cjx-game-info">
                <h3 class="cjx-game-title">{{ game.name }}</h3>
                <div class="cjx-game-tags">
                  <span class="cjx-tag">国区礼物</span>
                  <span class="cjx-tag">自动发货</span>
                </div>
              </div>
              <div class="cjx-game-price">
                <span class="cjx-current-price">{{ game.price }}</span>
                <span class="cjx-discount" v-if="game.discount">{{ game.discount }}</span>
              </div>
              <button class="cjx-buy-btn" @click="buyGame(game)">立即购买</button>
            </div>
          </div>
        </div>

        <!-- 右侧说明栏 -->
        <div class="cjx-sidebar-area">
          <div class="cjx-info-box">
            <h3>什么是礼物代购? 719cjx</h3>
            <p>礼物代购是通过Steam好友赠送功能购买游戏的方式。卖家将游戏以礼物形式发送到您的Steam账户，您收到后即可激活。</p>
            <h4>购买流程:</h4>
            <ol>
              <li>选择游戏并下单支付</li>
              <li>提供您的Steam好友代码</li>
              <li>卖家添加您为好友并发送礼物</li>
              <li>在Steam客户端接收礼物</li>
            </ol>
          </div>

          <div class="cjx-notice-box">
            <h3>注意事项</h3>
            <ul>
              <li>请确保您的Steam账户可以接收礼物</li>
              <li>国区礼物仅限国区Steam账户使用</li>
              <li>收到礼物后请及时确认收货</li>
            </ul>
          </div>
        </div>
      </div>
    </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const games = ref([])

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

const buyGame = (game) => {
  alert(`您选择了购买: ${game.name}\n请使用APP完成购买流程 719cjx`)
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
        { name: '艾尔登法环 标准版', price: '¥298.00', discount: '-20%', image: './picture/header.jpg' },
        { name: '赛博朋克2077 终极版', price: '¥198.00', discount: '-30%', image: './picture/header_schinese1.jpg' },
        { name: '黑神话：悟空 数字版', price: '¥268.00', discount: '-10%', image: './picture/header_schinese.jpg' },
        { name: '原神 3.8版本礼包', price: '¥98.00', discount: '-15%', image: './picture/xiaolaoshi.jpg' }
      ]
    }
  } catch (error) {
    console.error('加载游戏数据失败:', error)
    games.value = [
      { name: '艾尔登法环 标准版', price: '¥298.00', discount: '-20%', image: './picture/header.jpg' },
      { name: '赛博朋克2077 终极版', price: '¥198.00', discount: '-30%', image: './picture/header_schinese1.jpg' }
    ]
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 页面标题 */
.cjx-page-header {
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

/* 游戏列表 */
.cjx-game-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cjx-game-item {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.cjx-game-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.cjx-game-img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.cjx-game-info {
  flex: 1;
}

.cjx-game-title {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.cjx-game-tags {
  display: flex;
  gap: 0.5rem;
}

.cjx-tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background-color: #e3f2fd;
  color: #1976d2;
  border-radius: 4px;
}

.cjx-game-price {
  text-align: right;
  margin-right: 1rem;
}

.cjx-current-price {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  color: #e74c3c;
}

.cjx-discount {
  font-size: 0.85rem;
  color: #27ae60;
}

.cjx-buy-btn {
  padding: 0.75rem 1.5rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background-color 0.2s;
}

.cjx-buy-btn:hover {
  background-color: #2980b9;
}

/* 信息框 */
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
  border-bottom: 2px solid #3498db;
}

.cjx-info-box p {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.cjx-info-box h4 {
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  color: #34495e;
}

.cjx-info-box ol,
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
}

@media (max-width: 768px) {
  .cjx-page-header {
    padding: 1.5rem;
  }
  
  .cjx-page-header h1 {
    font-size: 1.5rem;
  }
  
  .cjx-game-item {
    flex-direction: column;
    text-align: center;
  }
  
  .cjx-game-img {
    width: 100%;
    height: 150px;
  }
  
  .cjx-game-price {
    text-align: center;
    margin-right: 0;
  }
}
</style>

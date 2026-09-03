﻿﻿﻿<template>
  <Layout>
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
          <h3 class="cjx-announcement-title">公告栏</h3>
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
            <p>平台使用的账户信息直接对接steam接口，安全可靠。交易使用全自动流程，方便快捷，避免普通玩家之间交易产生的欺骗纠纷</p>
          </div>
        </div>
      </div>
    </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { fetchAllGames, announcementAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// Home 页面特有的响应式数据
const slides = ref([])
const currentSlide = ref(0)
const announcements = ref([])
const sideAnnouncements = ref([])
const games = ref([])
const userSettings = ref({})

let slideInterval = null

// 获取图片URL - 处理本地图片路径
const getImageUrl = (path) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  if (path.includes('picture/')) {
    const fileName = path.split('picture/')[1]
    if (fileName) {
      return `/picture/${fileName}`
    }
  }
  return path.startsWith('/') ? path : `/${path}`
}

const setSlide = (index) => {
  currentSlide.value = index
}

const nextSlide = () => {
  if (slides.value.length) {
    currentSlide.value = (currentSlide.value + 1) % slides.value.length
  }
}

const prevSlide = () => {
  if (slides.value.length) {
    currentSlide.value = (currentSlide.value - 1 + slides.value.length) % slides.value.length
  }
}

const goToGame = (game) => {
  if (game.link) {
    if (game.link.includes('.html')) {
      window.location.href = game.link
    } else {
      router.push(game.link)
    }
  } else {
    router.push('/cdkey')
  }
}

// 加载轮播图数据
const loadSliderData = async () => {
  try {
    const response = await axios.get('/slider-data.json')
    if (response.data) {
      slides.value = response.data.slides || []
    } else {
      slides.value = [
        { imageUrl: './picture/gun3.png', altText: '开发者支持计划' },
        { imageUrl: './picture/gun2.png', altText: '热门游戏推荐' },
        { imageUrl: './picture/gun1.png', altText: '限时特惠' }
      ]
    }
  } catch (error) {
    console.error('加载轮播图数据失败:', error)
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
    const allGames = await fetchAllGames()
    if (allGames && allGames.length > 0) {
      games.value = allGames
      console.log(`✓ 从 Supabase 加载了 ${allGames.length} 条游戏数据`)
    } else {
      const response = await axios.get('/cdk_games.json')
      if (response.data) {
        const data = response.data
        const allLocalGames = [...(data.preSaleItems || []), ...(data.gameItems || [])]
        games.value = allLocalGames
        console.log(`✓ 从本地 JSON 加载了 ${allLocalGames.length} 条游戏数据`)
      } else {
        throw new Error('无法加载数据')
      }
    }
  } catch (error) {
    console.warn('从数据库/JSON 加载失败，使用默认数据:', error)
    games.value = [
      { name: '艾尔登法环 标准版', price: '¥298.00', image: './picture/header.jpg' },
      { name: '赛博朋克2077 终极版', price: '¥198.00', image: './picture/header_schinese1.jpg' },
      { name: '黑神话：悟空 数字版', price: '¥268.00', image: './picture/header_schinese.jpg' }
    ]
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

// 加载用户配置数据
const loadUserSettings = async () => {
  try {
    const response = await axios.get('/user-settings.json')
    if (response.data) {
      const settings = response.data
      console.log('✓ 从JSON加载了用户配置数据', settings)
      userSettings.value = settings
    }
  } catch (error) {
    console.warn('加载用户配置失败:', error)
  }
}

// 加载所有 Home 特有数据
const loadData = async () => {
  await loadSliderData()
  await loadGamesData()
  await loadAnnouncements()
  await loadUserSettings()
}

// 生命周期
onMounted(() => {
  loadData()
  slideInterval = setInterval(nextSlide, 5000)
})

onUnmounted(() => {
  if (slideInterval) clearInterval(slideInterval)
})
</script>

<style scoped>
/* Home 特有样式 */

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
  .cjx-content-wrapper {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
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

<template>
  <Layout>
    <div class="cjx-page-container">
      <!-- 标签页导航 -->
      <div class="cjx-tabs-nav">
        <ul class="cjx-tabs-list">
          <li class="cjx-tab-item" @click="$router.push('/')">首页</li>
          <li class="cjx-tab-item" @click="$router.push('/gift')">礼物代购</li>
          <li class="cjx-tab-item" @click="$router.push('/cdkey')">CDKey 719cjx</li>
          <li class="cjx-tab-item active" @click="$router.push('/balance')">余额购</li>
        </ul>
      </div>

      <!-- 返回按钮 -->
      <div class="cjx-back-bar">
        <button class="cjx-back-btn" @click="goBack">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
          </svg>
          返回余额购
        </button>
      </div>

      <!-- 游戏详情区域 -->
      <div class="cjx-detail-container">
        <!-- 左侧游戏信息 -->
        <div class="cjx-detail-main">
          <!-- 游戏信息卡片 -->
          <div class="cjx-game-detail-card">
            <div class="cjx-game-cover">
              <img :src="gameImageUrl" :alt="game.name" />
            </div>
            <div class="cjx-game-info">
              <h1 class="cjx-game-title">{{ game.name }}</h1>
              <p class="cjx-game-subtitle">{{ game.name }}</p>
              <div class="cjx-game-rating">
                <button class="cjx-detail-btn">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  游戏详情
                </button>
                <span class="cjx-rating-text">游戏评分: 9.4</span>
              </div>
              <div class="cjx-price-section">
                <span class="cjx-reference-price">参考价: ¥</span>
                <span class="cjx-current-price">{{ game.price }}</span>
                <span class="cjx-original-price">{{ getOriginalPrice(game.price, game.discount) }}</span>
                <span class="cjx-discount-badge" v-if="game.discount">{{ game.discount }}</span>
              </div>
            </div>
          </div>

          <!-- APP下载区域 -->
          <div class="cjx-app-section">
            <h2 class="cjx-section-title">PY手机APP下载链接</h2>
            <div class="cjx-app-notice">
              <p>近期由于steam网络问题导致网页下单成功率较低</p>
              <p>请尽量使用PY的手机APP下单获取更佳体验</p>
            </div>
            <div class="cjx-qrcode-container">
              <div class="cjx-qrcode-item">
                <div class="cjx-qrcode-label">安卓APP</div>
                <div class="cjx-qrcode-placeholder">
                  <svg viewBox="0 0 200 200" fill="none">
                    <rect width="200" height="200" fill="#f5f5f5" stroke="#ddd" stroke-width="2"/>
                    <path d="M30 30h140v140H30z" fill="#fff" stroke="#ddd" stroke-width="2"/>
                    <path d="M50 50h20v20H50z" fill="#000"/>
                    <path d="M130 50h20v20H130z" fill="#000"/>
                    <path d="M50 130h20v20H50z" fill="#000"/>
                    <path d="M110 110h80v80H110z" fill="#000"/>
                    <path d="M50 70h10v10H50z" fill="#000"/>
                    <path d="M70 50h10v10H70z" fill="#000"/>
                    <path d="M70 70h10v10H70z" fill="#000"/>
                    <path d="M130 70h20v20H130z" fill="#000"/>
                    <path d="M150 50h20v20H150z" fill="#000"/>
                    <path d="M150 70h20v20H150z" fill="#000"/>
                    <path d="M50 130h20v20H50z" fill="#000"/>
                    <path d="M50 150h20v20H50z" fill="#000"/>
                    <path d="M70 150h20v20H70z" fill="#000"/>
                  </svg>
                </div>
              </div>
              <div class="cjx-qrcode-item">
                <div class="cjx-qrcode-label">苹果APP</div>
                <div class="cjx-qrcode-placeholder">
                  <svg viewBox="0 0 200 200" fill="none">
                    <rect width="200" height="200" fill="#f5f5f5" stroke="#ddd" stroke-width="2"/>
                    <path d="M30 30h140v140H30z" fill="#fff" stroke="#ddd" stroke-width="2"/>
                    <path d="M50 50h20v20H50z" fill="#000"/>
                    <path d="M130 50h20v20H130z" fill="#000"/>
                    <path d="M50 130h20v20H50z" fill="#000"/>
                    <path d="M110 110h80v80H110z" fill="#000"/>
                    <path d="M50 70h10v10H50z" fill="#000"/>
                    <path d="M70 50h10v10H70z" fill="#000"/>
                    <path d="M70 70h10v10H70z" fill="#000"/>
                    <path d="M130 70h20v20H130z" fill="#000"/>
                    <path d="M150 50h20v20H150z" fill="#000"/>
                    <path d="M150 70h20v20H150z" fill="#000"/>
                    <path d="M50 130h20v20H50z" fill="#000"/>
                    <path d="M50 150h20v20H50z" fill="#000"/>
                    <path d="M70 150h20v20H70z" fill="#000"/>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- 购买说明 -->
          <div class="cjx-instructions-section">
            <h2 class="cjx-section-title">购买说明</h2>
            <div class="cjx-instructions-content">
              <p><strong>"余额购"需要您的steam账号开通Steam市场功能才可下单, 未满足余额购条件的订单会自动退款</strong></p>
              <p><strong>道具购买成功后余额会在2天后到账, 到账后请自行购买游戏</strong></p>

              <ol class="cjx-instructions-list">
                <li>整个订单流程中只需要您在手机Steam中确认上架,请注意查看提示, 我们会发送短信通知</li>
                <li>只要道具确认上架成功, 就请不要手动下架! 请不要手动下架! 请不要手动下架! 耐心等待机器人扫货即可</li>
                <li>如遇steam高峰期会出现《道具等待确认上架》但是没有确认提示的情况, 只有发生这种特殊情况时, 才需要您手动下架, 自行重新按游戏价格进行上架</li>
                <li>如您的账号条件不符, 比如手机令牌没有满足15天, 或者近期更换过手机设备, 出现暂挂现象, 或者因为加速节点问题导致登录失败, 订单会在24小时内自动退款</li>
                <li>如Steam价格变动很凶, 订单会变成对应付款的余额充值, 敬请谅解</li>
                <li>由于steam政策变更, 下单后余额预计1-2天左右时间到账, 请在游戏折扣期结束前2天购买游戏, 账户内已有余额的情况下会先使用账户余额购买游戏。</li>
                <li>其他任何问题, 加入官方QQ群 **** 私聊询问相关管理员</li>
              </ol>
            </div>
          </div>
        </div>

      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Layout from '../components/Layout.vue'
import { authAPI } from '../config/supabase-local.ts'

const router = useRouter()
const route = useRoute()

// 响应式数据
const game = ref({})
const walletBalance = ref(0.06)

// 计算游戏图片URL
const gameImageUrl = computed(() => {
  // 确保游戏数据存在
  if (!game.value || !game.value.name) {
    return '/picture/安魂曲.jpg'
  }
  
  // 直接使用游戏名生成图片路径
  const gameName = game.value.name
  
  // 处理游戏名中的特殊字符
  const cleanName = gameName
    .replace(/[:：]/g, '')
    .replace(/[\s\-\_]/g, '')
    .replace(/[\(\)\[\]\{\}]/g, '')
    .replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '')
  
  // 生成图片路径
  const imagePath = `/picture/${cleanName}.jpg`
  
  // 调试：输出图片路径
  console.log(`游戏名: ${gameName} → 图片路径: ${imagePath}`)
  
  return imagePath
})

// 解析路由参数中的游戏数据
const parseGameFromRoute = () => {
  try {
    const gameParam = route.params.game
    if (gameParam) {
      // 处理中文URI编码问题
      try {
        // 先解码一次
        let decoded = decodeURIComponent(gameParam)
        // 尝试解析JSON
        game.value = JSON.parse(decoded)
      } catch (e1) {
        try {
          // 尝试直接解析
          game.value = JSON.parse(gameParam)
        } catch (e2) {
          try {
            // 尝试解码两次（处理双重编码）
            let decoded = decodeURIComponent(decodeURIComponent(gameParam))
            game.value = JSON.parse(decoded)
          } catch (e3) {
            console.error('所有解析方式都失败:', e3)
            game.value = {}
          }
        }
      }
    }
  } catch (e) {
    console.error('解析游戏数据失败:', e)
    game.value = {}
  }
}

// 返回上一页
const goBack = () => {
  router.push('/balance')
}

// 充值
const recharge = () => {
  alert('充值功能开发中...')
}

// 计算原价
const getOriginalPrice = (currentPrice, discount) => {
  if (!discount || discount === '') return ''
  try {
    const priceNum = parseFloat(currentPrice.replace(/[^0-9.]/g, ''))
    const discountNum = parseInt(discount.replace(/[^0-9-]/g, ''))
    if (discountNum >= 0) return ''
    const originalPrice = priceNum / (1 + discountNum / 100)
    return `¥${originalPrice.toFixed(2)}`
  } catch (e) {
    return ''
  }
}

// 页面加载时解析游戏数据
onMounted(() => {
  parseGameFromRoute()

  // 获取用户信息
  const currentUser = authAPI.getCurrentUser()
  if (currentUser) {
    walletBalance.value = currentUser.wallet_balance || 0.06
  }
})
</script>

<style scoped>
.cjx-page-container {
  padding: 0;
}

/* 标签页导航 */
.cjx-tabs-nav {
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}

.cjx-tabs-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 30px;
}

.cjx-tab-item {
  padding: 15px 0;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.cjx-tab-item:hover {
  color: #e74c3c;
}

.cjx-tab-item.active {
  color: #e74c3c;
  border-bottom-color: #e74c3c;
}

/* 返回按钮 */
.cjx-back-bar {
  padding: 15px 20px;
  background: #f5f5f5;
}

.cjx-back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.cjx-back-btn:hover {
  background: #f0f0f0;
}

.cjx-back-btn svg {
  width: 18px;
  height: 18px;
}

/* 详情容器 */
.cjx-detail-container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.cjx-detail-main {
  width: 100%;
  max-width: 100%;
}

/* 游戏详情卡片 */
.cjx-game-detail-card {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 40px 60px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  margin-bottom: 30px;
  width: 100%;
  box-sizing: border-box;
}

.cjx-game-cover {
  flex-shrink: 0;
  width: 100%;
  max-width: 300px;
  aspect-ratio: 16 / 10;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0,0,0,0.15);
}

.cjx-game-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.3s ease;
}

.cjx-game-cover img:hover {
  transform: scale(1.02);
}

.cjx-game-info {
  flex: 1;
}

.cjx-game-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.cjx-game-subtitle {
  color: #666;
  font-size: 14px;
  margin: 0 0 20px 0;
}

.cjx-game-rating {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
}

.cjx-detail-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.cjx-detail-btn svg {
  width: 16px;
  height: 16px;
}

.cjx-rating-text {
  font-size: 14px;
  color: #666;
}

.cjx-price-section {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.cjx-reference-price {
  font-size: 14px;
  color: #666;
}

.cjx-current-price {
  font-size: 42px;
  font-weight: bold;
  color: #e74c3c;
}

.cjx-original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.cjx-discount-badge {
  background: #27ae60;
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: bold;
}

/* APP下载区域 */
.cjx-app-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.cjx-section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e74c3c;
  display: inline-block;
}

.cjx-app-notice {
  background: #fff3cd;
  border: 1px solid #ffeeba;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 25px;
}

.cjx-app-notice p {
  margin: 5px 0;
  color: #856404;
  font-size: 14px;
}

.cjx-qrcode-container {
  display: flex;
  justify-content: center;
  gap: 60px;
  flex-wrap: wrap;
}

.cjx-qrcode-item {
  text-align: center;
}

.cjx-qrcode-label {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.cjx-qrcode-placeholder {
  width: 200px;
  height: 200px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}

.cjx-qrcode-placeholder svg {
  width: 100%;
  height: 100%;
}

/* 购买说明 */
.cjx-instructions-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.cjx-instructions-content p {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 14px;
}

.cjx-instructions-list {
  margin: 20px 0;
  padding-left: 20px;
}

.cjx-instructions-list li {
  margin: 10px 0;
  color: #555;
  font-size: 14px;
  line-height: 1.6;
}

/* 右侧边栏样式 */
.cjx-balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 25px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 20px;
}

.cjx-balance-card h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: normal;
}

.cjx-balance-amount {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 15px;
}

.cjx-recharge-btn {
  width: 100%;
  padding: 12px;
  background: #fff;
  color: #667eea;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

.cjx-info-box,
.cjx-notice-box {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.cjx-info-box h3,
.cjx-notice-box h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.cjx-info-box p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.cjx-notice-box ul {
  margin: 0;
  padding-left: 20px;
}

.cjx-notice-box li {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 1024px) {
  .cjx-detail-container {
    flex-direction: column;
  }

}

@media (max-width: 768px) {
  .cjx-game-detail-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .cjx-game-cover {
    width: 100%;
    max-width: 280px;
    aspect-ratio: 16 / 10;
  }

  .cjx-game-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cjx-game-info {
    width: 100%;
  }

  .cjx-game-title {
    font-size: 22px;
  }

  .cjx-current-price {
    font-size: 32px;
  }

  .cjx-tabs-list {
    gap: 15px;
    overflow-x: auto;
  }

  .cjx-tab-item {
    white-space: nowrap;
  }

  .cjx-qrcode-container {
    gap: 30px;
  }
}
</style>
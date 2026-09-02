<template>
  <Layout>
    <div class="cjx-detail-page">
      <!-- 游戏详情内容 -->
      <div class="cjx-detail-container" v-if="game">
        <!-- 左侧游戏信息 -->
        <div class="cjx-detail-left">
          <div class="cjx-game-hero">
            <img :src="getImageUrl(game.image)" :alt="game.name" class="cjx-hero-img">
            <div class="cjx-game-badges">
              <span class="cjx-badge" v-if="game.is_presale">预售</span>
              <span class="cjx-badge cjx-badge-discount" v-if="game.discount">{{ game.discount }}</span>
            </div>
          </div>

          <div class="cjx-game-info-section">
            <h1 class="cjx-game-name">{{ game.name }}</h1>
            <p class="cjx-game-desc">{{ game.description || '暂无描述' }}</p>
            
            <!-- 版本切换 - 仅当存在多个版本时显示 -->
            <div class="cjx-version-switch" v-if="gameVersions.length > 1">
              <label>选择版本</label>
              <div class="cjx-version-options">
                <button 
                  v-for="version in gameVersions" 
                  :key="version.id"
                  class="cjx-version-btn"
                  :class="{ 'cjx-version-active': selectedVersion?.id === version.id }"
                  @click="switchVersion(version)"
                >
                  {{ version.name }}
                  <span class="cjx-version-price">¥{{ version.price.toFixed(2) }}</span>
                </button>
              </div>
            </div>
            
            <div class="cjx-game-meta">
              <div class="cjx-meta-item">
                <span>发行日期：</span>
                <strong>{{ game.release_date || '待定' }}</strong>
              </div>
              <div class="cjx-meta-item">
                <span>开发商：</span>
                <strong>{{ game.developer || '未知' }}</strong>
              </div>
              <div class="cjx-meta-item">
                <span>平台：</span>
                <strong>Steam</strong>
              </div>
            </div>
          </div>

          <!-- CDKey库存 -->
          <div class="cjx-cdkey-section">
            <h3>CDKey库存</h3>
            <table class="cjx-table">
              <thead>
                <tr>
                  <th>版本</th>
                  <th>价格</th>
                  <th>库存</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cdkey in cdkeyList" :key="cdkey.id">
                  <td>{{ cdkey.version || '标准版' }}</td>
                  <td class="cjx-price">¥{{ cdkey.price.toFixed(2) }}</td>
                  <td>{{ cdkey.quantity }}件</td>
                  <td>
                    <button class="cjx-btn cjx-btn-primary" @click="buyCDKey(cdkey)">购买</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 右侧购买区域 -->
        <div class="cjx-detail-right">
          <div class="cjx-buy-card">
            <div class="cjx-price-section">
              <p class="cjx-current-price">¥{{ game.current_price.toFixed(2) }}</p>
              <p class="cjx-original-price" v-if="game.original_price > game.current_price">
                ¥{{ game.original_price.toFixed(2) }}
              </p>
            </div>

            <div class="cjx-buy-options">
              <div class="cjx-option-group">
                <label>购买数量</label>
                <div class="cjx-quantity">
                  <button @click="quantity > 1 && quantity--">-</button>
                  <input type="number" v-model.number="quantity" min="1" max="99" />
                  <button @click="quantity++">+</button>
                </div>
              </div>

              <div class="cjx-option-group">
                <label>收货方式</label>
                <div class="cjx-radio-group">
                  <label class="cjx-radio active">
                    <input type="radio" value="cdkey" v-model="deliveryMethod" />
                    CDKey
                  </label>
                  <label class="cjx-radio">
                    <input type="radio" value="gift" v-model="deliveryMethod" />
                    礼物形式
                  </label>
                </div>
              </div>
            </div>

            <div class="cjx-total">
              <span>合计：</span>
              <strong>¥{{ (game.current_price * quantity).toFixed(2) }}</strong>
            </div>

            <button class="cjx-btn cjx-btn-buy" @click="buyNow">立即购买</button>
            <button class="cjx-btn cjx-btn-cart" @click="addToCart">加入购物车</button>
          </div>

          <!-- 卖家信息 -->
          <div class="cjx-seller-card">
            <h4>卖家信息</h4>
            <div class="cjx-seller-info">
              <div class="cjx-seller-avatar">PY</div>
              <div class="cjx-seller-detail">
                <p class="cjx-seller-name">Steam PY官方</p>
                <p class="cjx-seller-rating">⭐⭐⭐⭐⭐ 好评率 99%</p>
              </div>
            </div>
          </div>

          <!-- 服务保障 -->
          <div class="cjx-service-card">
            <h4>服务保障</h4>
            <ul class="cjx-service-list">
              <li>✓ 官方正版CDKey</li>
              <li>✓ 7天无理由退款</li>
              <li>✓ 24小时自动发货</li>
              <li>✓ 客服全程支持</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div class="cjx-loading" v-else>
        <p>加载中...</p>
      </div>
    </div>

    <!-- 创建订单弹窗 -->
    <div class="cjx-modal cjx-order-modal" v-if="showOrderModal" @click.self="closeOrderModal">
      <div class="cjx-modal-content">
        <div class="cjx-modal-header">
          <h3>创建订单</h3>
          <button class="cjx-modal-close" @click="closeOrderModal">×</button>
        </div>
        <div class="cjx-modal-body">
          <!-- 游戏信息 -->
          <div class="cjx-order-game">
            <img :src="getImageUrl(game?.image)" :alt="game?.name" class="cjx-order-game-img">
          </div>
          
          <div class="cjx-order-info">
            <div class="cjx-order-row">
              <span class="cjx-order-label">游戏名称：</span>
              <span class="cjx-order-value">{{ game?.name }}</span>
            </div>
            <div class="cjx-order-row">
              <span class="cjx-order-label">游戏金额：</span>
              <span class="cjx-order-value">¥ {{ game?.current_price?.toFixed(2) }}</span>
            </div>
            <div class="cjx-order-row">
              <span class="cjx-order-label">优惠券：</span>
              <span class="cjx-order-value cjx-coupon">暂无可用券 ></span>
            </div>
            <div class="cjx-order-row cjx-balance-row">
              <label class="cjx-checkbox-label">
                <input type="checkbox" v-model="useBalance" disabled />
                <span>PY余额抵现(可抵现: ¥0)</span>
              </label>
            </div>
            <div class="cjx-order-row">
              <span class="cjx-order-label">技术服务费:</span>
              <span class="cjx-order-value">0</span>
            </div>
            <div class="cjx-order-row">
              <span class="cjx-order-label">通道费:</span>
              <span class="cjx-order-value">0%</span>
            </div>
            <div class="cjx-order-row cjx-pay-amount">
              <span class="cjx-order-label">支付金额：</span>
              <span class="cjx-order-value cjx-amount">¥ {{ (game?.current_price * quantity)?.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 支付方式 -->
          <div class="cjx-payment-section">
            <span class="cjx-payment-label">请选择支付方式</span>
            <div class="cjx-payment-options">
              <div 
                class="cjx-payment-item"
                :class="{ active: selectedPayment === 'alipay' }"
                @click="selectedPayment = 'alipay'"
              >
                <span class="cjx-payment-icon">支</span>
                <span>支付宝</span>
              </div>
            </div>
          </div>
        </div>
        <div class="cjx-modal-footer cjx-order-footer">
          <button class="cjx-btn cjx-btn-prev" @click="closeOrderModal">上一步</button>
          <button class="cjx-btn cjx-btn-next" @click="goToConfirm">下一步</button>
        </div>
      </div>
    </div>

    <!-- 支付确认弹窗 -->
    <div class="cjx-modal cjx-confirm-modal" v-if="showConfirmModal" @click.self="closeConfirmModal">
      <div class="cjx-modal-content cjx-confirm-content">
        <div class="cjx-modal-header">
          <h3>支付确认</h3>
          <button class="cjx-modal-close" @click="closeConfirmModal">×</button>
        </div>
        <div class="cjx-modal-body cjx-confirm-body">
          <p class="cjx-confirm-title">请确认支付宝支付</p>
          <p class="cjx-confirm-fee">技术服务费: 0</p>
          <p class="cjx-confirm-amount">¥ {{ (game?.current_price * quantity)?.toFixed(2) }}</p>
          <div class="cjx-confirm-tips">
            <p>未满18岁请勿购买虚拟游戏产品</p>
            <p>* 请勿重复购买此游戏，重复购买概不负责</p>
            <p>购买后需登录steam进行兑换审核</p>
          </div>
        </div>
        <div class="cjx-modal-footer">
          <button class="cjx-btn cjx-btn-pay" @click="goToPay">支付</button>
        </div>
      </div>
    </div>

    <!-- 支付二维码弹窗 -->
    <div class="cjx-modal cjx-pay-modal" v-if="showPayModal" @click.self="closePayModal">
      <div class="cjx-modal-content cjx-pay-content">
        <div class="cjx-modal-header">
          <h3>支付订单</h3>
        </div>
        <div class="cjx-modal-body cjx-pay-body">
          <div class="cjx-qr-section">
            <div class="cjx-qr-code">
              <!-- 模拟支付宝二维码 -->
              <div class="cjx-qr-placeholder">
                <div class="cjx-qr-pattern"></div>
                <div class="cjx-qr-logo">支</div>
              </div>
            </div>
            <div class="cjx-qr-info">
              <p>使用手机支付宝扫码完成付款</p>
              <p class="cjx-qr-links">
                <a href="#">手机支付宝下载</a> | <a href="#">如何使用?</a>
              </p>
            </div>
          </div>
          <p class="cjx-pay-tips">* 扫码成功后请耐心等待页面跳转,检验预估1分钟左右</p>
          <!-- 模拟支付成功按钮 -->
          <button class="cjx-btn cjx-btn-simulate" @click="simulatePaySuccess">模拟支付成功</button>
        </div>
      </div>
    </div>

    <!-- 支付成功提示 -->
    <div class="cjx-toast" v-if="toastMessage">
      {{ toastMessage }}
    </div>

    <!-- CDKey 显示弹窗 -->
    <div class="cjx-modal cjx-cdkey-modal" v-if="showCdkeyModal" @click.self="closeCdkeyModal">
      <div class="cjx-modal-content cjx-cdkey-content">
        <div class="cjx-modal-header">
          <h3>支付成功</h3>
          <button class="cjx-modal-close" @click="closeCdkeyModal">×</button>
        </div>
        <div class="cjx-modal-body cjx-cdkey-body">
          <div class="cjx-cdkey-success-icon">✓</div>
          <p class="cjx-cdkey-message">恭喜您购买成功！</p>
          <p class="cjx-cdkey-submessage">以下是您的 CDKey，请妥善保存</p>
          <div class="cjx-cdkey-display">
            <span class="cjx-cdkey-code">{{ currentCdkey }}</span>
            <button class="cjx-btn-copy" @click="copyCdkey">复制</button>
          </div>
          <p class="cjx-cdkey-tip">您也可以在明细页面的订单详情中再次查看 CDKey</p>
        </div>
        <div class="cjx-modal-footer cjx-cdkey-footer">
          <button class="cjx-btn cjx-btn-view-order" @click="goToTransactions">查看订单</button>
          <button class="cjx-btn cjx-btn-close" @click="closeCdkeyModal">关闭</button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { authAPI, orderAPI, transactionAPI, walletAPI, userGameAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const game = ref(null)
const quantity = ref(1)
const deliveryMethod = ref('cdkey')
const walletBalance = ref(0)
const cdkeyList = ref([])

// 游戏版本列表（用于多版本切换）
const gameVersions = ref([])
const selectedVersion = ref(null)

// 购买流程弹窗控制
const showOrderModal = ref(false)
const showConfirmModal = ref(false)
const showPayModal = ref(false)
const selectedPayment = ref('alipay')
const useBalance = ref(false)
const toastMessage = ref('')

// CDKey 弹窗控制
const showCdkeyModal = ref(false)
const currentCdkey = ref('')

// 获取基础游戏名称（去除版本后缀）
const getBaseGameName = (name) => {
  if (!name) return ''
  // 移除"豪华版"、"标准版"等后缀
  return name.replace(/\s*豪华版$/, '').replace(/\s*标准版$/, '').trim()
}

// 获取游戏版本类型
const getVersionType = (name) => {
  if (!name) return '标准版'
  if (name.includes('豪华版')) return '豪华版'
  return '标准版'
}

// 根据游戏数据生成CDKey列表 - 仅展示当前版本的库存
const generateCdkeyList = (gameData) => {
  if (!gameData) return []
  
  const currentPrice = parseFloat(gameData.price?.replace(/[^0-9.]/g, '')) || 0
  const stock = gameData.stock || 99
  const versionType = getVersionType(gameData.name)
  
  // 只返回当前版本的库存
  return [
    { id: 1, version: versionType, price: currentPrice, quantity: stock }
  ]
}

// 切换版本
const switchVersion = (version) => {
  selectedVersion.value = version
  // 更新游戏数据
  game.value = {
    ...game.value,
    name: version.name,
    current_price: version.price,
    original_price: version.originalPrice,
    stock: version.stock
  }
  // 更新CDKey列表
  cdkeyList.value = generateCdkeyList({
    name: version.name,
    price: `¥${version.price}`,
    stock: version.stock
  })
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

// 解码游戏ID
const decodeGameId = (id) => {
  try {
    return decodeURIComponent(id)
  } catch (e) {
    return id
  }
}

// 方法
const buyCDKey = (cdkey) => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录')
    router.push('/login')
    return
  }
  // 打开创建订单弹窗
  showOrderModal.value = true
}

const buyNow = () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录')
    router.push('/login')
    return
  }
  // 打开创建订单弹窗
  showOrderModal.value = true
}

const addToCart = () => {
  alert(`已加入购物车：${game.value.name}`)
}

// 关闭弹窗
const closeOrderModal = () => {
  showOrderModal.value = false
}

const closeConfirmModal = () => {
  showConfirmModal.value = false
}

const closePayModal = () => {
  showPayModal.value = false
}

const closeCdkeyModal = () => {
  showCdkeyModal.value = false
}

// 复制 CDKey
const copyCdkey = () => {
  navigator.clipboard.writeText(currentCdkey.value).then(() => {
    showToast('CDKey 已复制到剪贴板')
  }).catch(() => {
    // 降级方案
    const input = document.createElement('input')
    input.value = currentCdkey.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    showToast('CDKey 已复制到剪贴板')
  })
}

// 跳转到明细页面
const goToTransactions = () => {
  showCdkeyModal.value = false
  router.push('/transactions')
}

// 前往支付确认
const goToConfirm = () => {
  if (!selectedPayment.value) {
    alert('请选择支付方式')
    return
  }
  showOrderModal.value = false
  showConfirmModal.value = true
}

// 前往支付页面
const goToPay = () => {
  showConfirmModal.value = false
  showPayModal.value = true
}

// 显示提示
const showToast = (message) => {
  toastMessage.value = message
  setTimeout(() => {
    toastMessage.value = ''
  }, 3000)
}

// 模拟支付成功
const simulatePaySuccess = async () => {
  try {
    const total = game.value.current_price * quantity.value
    const orderNo = 'ORD' + Date.now()
    const cdkey = generateCdkey()

    // 保存当前 CDKey 用于显示
    currentCdkey.value = cdkey

    const currentUser = authAPI.getCurrentUser()
    const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
    
    console.log('=== 提交订单数据 ===')
    console.log('userId:', userId)
    console.log('gameId:', game.value.id)
    console.log('gameName:', game.value.name)
    console.log('price:', game.value.current_price)
    console.log('total:', total)
    console.log('cdkey:', cdkey)
    
    // 创建订单到数据库
    let orderResult
    try {
      console.log('正在调用 orderAPI.createOrder...')
      orderResult = await orderAPI.createOrder({
        buyer_id: userId,
        game_id: game.value.id,
        game_name: game.value.name,
        game_image: game.value.image,
        price: game.value.current_price,
        quantity: quantity.value,
        total_price: total,
        delivery_method: deliveryMethod.value,
        version: getVersionType(game.value.name),
        cdkey: cdkey,
        status: 'completed',
        payment_method: selectedPayment.value
      })
      
      console.log('orderAPI.createOrder 返回:', orderResult)
      
      if (orderResult.error) {
        console.warn('订单创建使用本地存储:', orderResult.error)
        orderResult = { data: { id: 'local_' + Date.now() } }
      }
    } catch (err) {
      console.warn('订单创建失败，使用本地模式:', err)
      orderResult = { data: { id: 'local_' + Date.now() } }
    }
    
    // 后端 createOrder 已经自动创建 transactions 和 user_games 记录
    // 不需要前端重复调用
    
    // 扣除余额（如果使用余额支付）
    if (useBalance.value && total > 0) {
      try {
        await walletAPI.updateWallet(userId, { amount: total })
      } catch (err) {
        console.warn('余额更新失败:', err)
      }
    }
    
    console.log('✓ 订单处理完成')
    
    // 关闭支付弹窗
    showPayModal.value = false
    
    // 显示 CDKey 弹窗
    showCdkeyModal.value = true
    
  } catch (error) {
    console.error('支付处理出错:', error)
    alert('支付处理出现问题，请重试')
    
    // 即使出错也关闭支付弹窗并显示CDKey
    showPayModal.value = false
    showCdkeyModal.value = true
  }
}

// 生成模拟CDKey
const generateCdkey = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = ''
  for (let i = 0; i < 15; i++) {
    if (i > 0 && i % 5 === 0) result += '-'
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

// 加载数据
const loadData = async () => {
  // 加载真实钱包余额
  const currentUser = authAPI.getCurrentUser()
  if (currentUser?.id) {
    try {
      const r = await walletAPI.getBalance(currentUser.id)
      if (r.data) {
        const bal = r.data.balance ?? r.data
        walletBalance.value = parseFloat(bal) || 0
      }
    } catch {}
  }

  const gameId = route.params.id
  const decodedId = decodeGameId(gameId)
  if (gameId) {
    try {
      // 从 Supabase 获取游戏的正确数字 ID
      let dbGameId = null
      try {
        // 先尝试从数据库获取
        const dbGames = await request(`${SUPABASE_URL}/rest/v1/games?name=eq.${encodeURIComponent(decodedId)}&limit=1`)
        if (dbGames && dbGames.length > 0) {
          dbGameId = dbGames[0].id
          console.log('从数据库获取到游戏ID:', dbGameId)
        }
      } catch (e) {
        console.warn('获取游戏ID失败，使用备选方案')
      }
      
      // 从本地JSON加载游戏数据
      const response = await axios.get('/cdk_games.json')
      if (response.data) {
        const data = response.data
        // 合并预售商品和普通游戏
        const allGames = [...(data.preSaleItems || []), ...(data.gameItems || [])]
        
        // 查找对应的游戏 - 支持多种匹配方式
        const foundGame = allGames.find(g => {
          const name = g.name || ''
          if (name === decodedId || name === gameId) return true
          return name.includes(decodedId) || decodedId.includes(name) ||
                 name.includes(gameId) || gameId.includes(name)
        })
        
        if (foundGame) {
          const currentPrice = parseFloat(foundGame.price?.replace(/[^0-9.]/g, '')) || 0
          const originalPrice = parseFloat(foundGame.originalPrice?.replace(/[^0-9.]/g, '')) || currentPrice * 1.2
          const baseGameName = getBaseGameName(foundGame.name)
          
          // 查找同一游戏的所有版本
          const relatedVersions = allGames.filter(g => {
            const otherBaseName = getBaseGameName(g.name)
            return otherBaseName === baseGameName
          }).map(g => ({
            id: encodeURIComponent(g.name),
            name: g.name,
            price: parseFloat(g.price?.replace(/[^0-9.]/g, '')) || 0,
            originalPrice: parseFloat(g.originalPrice?.replace(/[^0-9.]/g, '')) || 0,
            stock: g.stock || 99,
            image: g.image,
            description: g.description,
            isPresale: g.isPresale,
            releaseDate: g.releaseDate,
            developer: g.developer,
            discount: g.discount
          }))
          
          // 设置游戏版本列表
          gameVersions.value = relatedVersions
          // 设置当前选中的版本
          selectedVersion.value = relatedVersions.find(v => v.name === foundGame.name) || relatedVersions[0]
          
          game.value = {
            id: dbGameId || gameId,
            name: foundGame.name,
            description: foundGame.description || '暂无描述',
            current_price: currentPrice,
            original_price: originalPrice,
            discount: foundGame.discount || '',
            image: foundGame.image || 'picture/安魂曲.jpg',
            is_presale: foundGame.isPresale || false,
            release_date: foundGame.releaseDate || '待定',
            developer: foundGame.developer || '未知',
            stock: foundGame.stock || 99
          }
          // 设置CDKey列表 - 使用真实数据，只显示当前版本
          cdkeyList.value = generateCdkeyList(foundGame)
          return
        }
      }
    } catch (error) {
      console.error('加载游戏数据失败:', error)
    }
    
    // 使用默认数据（根据ID生成）
    const isDeluxe = decodedId === 'anhunqu-deluxe' || gameId === 'anhunqu-deluxe'
    const defaultPrice = isDeluxe ? 354 : 309
    const defaultOriginalPrice = isDeluxe ? 398 : 348
    const defaultStock = isDeluxe ? 50 : 99
    
    game.value = {
      id: gameId,
      name: isDeluxe ? '生化危机:安魂曲 豪华版' : '生化危机:安魂曲',
      description: '一款史诗级的动作冒险游戏，带你进入一个充满神秘和危险的世界。',
      current_price: defaultPrice,
      original_price: defaultOriginalPrice,
      discount: '-11%',
      image: 'picture/安魂曲.jpg',
      is_presale: true,
      release_date: '2024-12-20',
      developer: 'Epic Games',
      stock: defaultStock
    }
    
    // 设置版本列表（默认数据）
    gameVersions.value = [
      { id: '生化危机:安魂曲', name: '生化危机:安魂曲', price: 309, originalPrice: 348, stock: 99 },
      { id: '生化危机:安魂曲%20豪华版', name: '生化危机:安魂曲 豪华版', price: 354, originalPrice: 398, stock: 50 }
    ]
    selectedVersion.value = gameVersions.value.find(v => v.name === game.value.name) || gameVersions.value[0]
    
    // 设置CDKey列表
    cdkeyList.value = generateCdkeyList({
      name: game.value.name,
      price: `¥${defaultPrice}`,
      stock: defaultStock
    })
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.cjx-detail-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.cjx-detail-container {
  display: flex;
  gap: 20px;
}

.cjx-detail-left {
  flex: 1;
}

.cjx-detail-right {
  width: 320px;
  flex-shrink: 0;
}

.cjx-game-hero {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.cjx-hero-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.cjx-game-badges {
  position: absolute;
  top: 15px;
  left: 15px;
  display: flex;
  gap: 10px;
}

.cjx-badge {
  background: #3498db;
  color: #fff;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.cjx-badge-discount {
  background: #e74c3c;
}

.cjx-game-info-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.cjx-game-name {
  font-size: 28px;
  margin: 0 0 15px 0;
  color: #333;
}

.cjx-game-desc {
  color: #666;
  line-height: 1.8;
  margin-bottom: 20px;
}

.cjx-game-meta {
  display: flex;
  gap: 30px;
}

/* 版本切换样式 */
.cjx-version-switch {
  margin: 20px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.cjx-version-switch label {
  display: block;
  margin-bottom: 12px;
  color: #333;
  font-weight: bold;
  font-size: 14px;
}

.cjx-version-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cjx-version-btn {
  padding: 12px 20px;
  border: 2px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
  min-width: 180px;
}

.cjx-version-btn:hover {
  border-color: #3498db;
  background: #f0f8ff;
}

.cjx-version-active {
  border-color: #3498db;
  background: #3498db;
  color: #fff;
}

.cjx-version-active .cjx-version-price {
  color: #fff;
}

.cjx-version-price {
  display: block;
  margin-top: 4px;
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.cjx-meta-item span {
  color: #999;
}

.cjx-meta-item strong {
  color: #333;
  margin-left: 5px;
}

.cjx-cdkey-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.cjx-cdkey-section h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.cjx-table {
  width: 100%;
  border-collapse: collapse;
}

.cjx-table th,
.cjx-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.cjx-table th {
  background: #f9f9f9;
  font-weight: bold;
  color: #666;
}

.cjx-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.cjx-buy-card {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.cjx-price-section {
  margin-bottom: 20px;
}

.cjx-current-price {
  font-size: 36px;
  color: #e74c3c;
  font-weight: bold;
  margin: 0;
}

.cjx-original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
  margin: 5px 0 0 0;
}

.cjx-buy-options {
  margin-bottom: 20px;
}

.cjx-option-group {
  margin-bottom: 20px;
}

.cjx-option-group label {
  display: block;
  margin-bottom: 10px;
  color: #333;
  font-size: 14px;
}

.cjx-quantity {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cjx-quantity button {
  width: 40px;
  height: 40px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-size: 18px;
}

.cjx-quantity input {
  width: 60px;
  height: 40px;
  text-align: center;
  border: 1px solid #ddd;
  font-size: 16px;
}

.cjx-radio-group {
  display: flex;
  gap: 10px;
}

.cjx-radio {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
}

.cjx-radio.active,
.cjx-radio:hover {
  border-color: #3498db;
  background: #f0f8ff;
}

.cjx-radio input {
  display: none;
}

.cjx-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-top: 1px solid #eee;
  margin-bottom: 20px;
}

.cjx-total strong {
  font-size: 24px;
  color: #e74c3c;
}

.cjx-btn {
  width: 100%;
  padding: 15px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 10px;
}

.cjx-btn-buy {
  background: #e74c3c;
  color: #fff;
}

.cjx-btn-buy:hover {
  background: #c0392b;
}

.cjx-btn-cart {
  background: #f0f0f0;
  color: #666;
}

.cjx-btn-cart:hover {
  background: #e0e0e0;
}

.cjx-seller-card,
.cjx-service-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.cjx-seller-card h4,
.cjx-service-card h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.cjx-seller-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.cjx-seller-avatar {
  width: 50px;
  height: 50px;
  background: #3498db;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.cjx-seller-name {
  margin: 0 0 5px 0;
  color: #333;
}

.cjx-seller-rating {
  margin: 0;
  color: #f39c12;
  font-size: 14px;
}

.cjx-service-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.cjx-service-list li {
  padding: 8px 0;
  color: #666;
}

.cjx-loading {
  text-align: center;
  padding: 100px;
  color: #999;
}

/* 弹窗样式 */
.cjx-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cjx-modal-content {
  background: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.cjx-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
}

.cjx-modal-header h3 {
  margin: 0;
  font-size: 18px;
  text-align: center;
  flex: 1;
}

.cjx-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.cjx-modal-body {
  padding: 20px;
}

.cjx-modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

/* 创建订单弹窗样式 */
.cjx-order-game {
  text-align: center;
  margin-bottom: 20px;
}

.cjx-order-game-img {
  max-width: 200px;
  max-height: 120px;
  border-radius: 4px;
}

.cjx-order-info {
  margin-bottom: 20px;
}

.cjx-order-row {
  display: flex;
  justify-content: flex-start;
  padding: 8px 0;
  font-size: 14px;
}

.cjx-order-label {
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.cjx-order-value {
  color: #333;
}

.cjx-coupon {
  color: #999;
  cursor: pointer;
}

.cjx-balance-row {
  padding: 12px 0;
}

.cjx-checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  cursor: not-allowed;
}

.cjx-checkbox-label input {
  cursor: not-allowed;
}

.cjx-pay-amount {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e0e0e0;
}

.cjx-pay-amount .cjx-order-label,
.cjx-pay-amount .cjx-order-value {
  font-size: 16px;
  font-weight: bold;
}

.cjx-pay-amount .cjx-amount {
  color: #e74c3c;
  font-size: 20px;
}

.cjx-payment-section {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-top: 1px solid #e0e0e0;
}

.cjx-payment-label {
  color: #666;
  font-size: 14px;
}

.cjx-payment-options {
  flex: 1;
}

.cjx-payment-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-payment-item.active,
.cjx-payment-item:hover {
  border-color: #52c41a;
  background: #f6ffed;
}

.cjx-payment-icon {
  width: 24px;
  height: 24px;
  background: #1677ff;
  color: #fff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.cjx-order-footer {
  display: flex;
  gap: 10px;
}

.cjx-btn-prev,
.cjx-btn-next,
.cjx-btn-pay {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.cjx-btn-prev {
  background: #4a4a4a;
  color: #fff;
}

.cjx-btn-prev:hover {
  background: #333;
}

.cjx-btn-next,
.cjx-btn-pay {
  background: #4a4a4a;
  color: #fff;
}

.cjx-btn-next:hover,
.cjx-btn-pay:hover {
  background: #333;
}

/* 支付确认弹窗 */
.cjx-confirm-content {
  max-width: 400px;
}

.cjx-confirm-body {
  text-align: center;
  padding: 30px 20px;
}

.cjx-confirm-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.cjx-confirm-fee {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
}

.cjx-confirm-amount {
  font-size: 36px;
  color: #e74c3c;
  font-weight: bold;
  margin: 20px 0;
}

.cjx-confirm-tips {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.cjx-confirm-tips p {
  font-size: 12px;
  color: #e74c3c;
  margin: 5px 0;
}

/* 支付二维码弹窗 */
.cjx-pay-content {
  max-width: 450px;
}

.cjx-pay-body {
  padding: 30px;
  text-align: center;
}

.cjx-qr-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  margin-bottom: 20px;
}

.cjx-qr-code {
  width: 180px;
  height: 180px;
  border: 1px solid #e0e0e0;
  padding: 10px;
  border-radius: 4px;
}

.cjx-qr-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cjx-qr-pattern {
  width: 100%;
  height: 100%;
  background-image: 
    repeating-linear-gradient(0deg, #333 0px, #333 4px, transparent 4px, transparent 8px),
    repeating-linear-gradient(90deg, #333 0px, #333 4px, transparent 4px, transparent 8px);
  background-size: 8px 8px;
  opacity: 0.8;
}

.cjx-qr-logo {
  position: absolute;
  width: 40px;
  height: 40px;
  background: #1677ff;
  color: #fff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.cjx-qr-info {
  text-align: left;
}

.cjx-qr-info p {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
}

.cjx-qr-links {
  font-size: 12px !important;
}

.cjx-qr-links a {
  color: #1677ff;
  text-decoration: none;
}

.cjx-pay-tips {
  font-size: 12px;
  color: #e74c3c;
  margin: 20px 0;
}

.cjx-btn-simulate {
  margin-top: 20px;
  padding: 12px 30px;
  background: #52c41a;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.cjx-btn-simulate:hover {
  background: #389e0d;
}

/* Toast提示 */
.cjx-toast {
  position: fixed;
  bottom: 50px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: #fff;
  padding: 12px 24px;
  border-radius: 6px;
  z-index: 2000;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

/* CDKey 弹窗样式 */
.cjx-cdkey-content {
  max-width: 450px;
}

.cjx-cdkey-body {
  padding: 30px;
  text-align: center;
}

.cjx-cdkey-success-icon {
  width: 60px;
  height: 60px;
  background: #52c41a;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin: 0 auto 20px;
}

.cjx-cdkey-message {
  font-size: 20px;
  color: #333;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.cjx-cdkey-submessage {
  font-size: 14px;
  color: #666;
  margin: 0 0 25px 0;
}

.cjx-cdkey-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #f5f5f5;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.cjx-cdkey-code {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  letter-spacing: 1px;
}

.cjx-btn-copy {
  padding: 8px 16px;
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.cjx-btn-copy:hover {
  background: #2980b9;
}

.cjx-cdkey-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.cjx-cdkey-footer {
  display: flex;
  gap: 10px;
}

.cjx-btn-view-order,
.cjx-btn-close {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.cjx-btn-view-order {
  background: #e74c3c;
  color: #fff;
}

.cjx-btn-view-order:hover {
  background: #c0392b;
}

.cjx-btn-close {
  background: #95a5a6;
  color: #fff;
}

.cjx-btn-close:hover {
  background: #7f8c8d;
}
</style>

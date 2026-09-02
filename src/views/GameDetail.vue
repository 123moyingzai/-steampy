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

          <!-- 出售卖家（图1风格） -->
          <div class="cjx-cdkey-section">
            <h3>出售卖家</h3>
            <table class="cjx-seller-table" v-if="visibleCdkeyList.length > 0">
              <thead>
                <tr>
                  <th style="width:40px">#</th>
                  <th>头像</th>
                  <th>Steam账户名</th>
                  <th>库存</th>
                  <th>CDKey单价</th>
                  <th>折扣</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(cdkey, idx) in visibleCdkeyList" 
                  :key="cdkey._rowKey || cdkey.id" 
                  :class="{ 'cjx-row-min': cdkey === cheapestRow }"
                >
                  <td class="cjx-rank">{{ idx + 1 }}</td>
                  <td>
                    <img 
                      v-if="cdkey.avatar" :src="cdkey.avatar" 
                      class="cjx-avatar"
                      @error="(e: any) => e.target.src = '/picture/default-avatar.png'"
                    />
                    <div v-else class="cjx-avatar cjx-avatar-default">🎮</div>
                  </td>
                  <td>
                    <span v-if="cdkey.source === 'official'" class="cjx-official-name">S***y（官方）</span>
                    <span v-else class="cjx-seller-name">{{ cdkey.seller_name || '未知用户' }}</span>
                    <span v-if="cdkey === cheapestRow" class="cjx-min-tag">最低价</span>
                  </td>
                  <td>{{ cdkey.stock ?? cdkey.quantity ?? 0 }}</td>
                  <td class="cjx-price-cell">¥{{ (cdkey.price || 0).toFixed(2) }}</td>
                  <td :class="'cjx-discount ' + (cdkey.discount_pct && cdkey.discount_pct > 0 ? 'cjx-discount-green' : '')">
                    <template v-if="cdkey.discount_pct && cdkey.discount_pct > 0">-{{ cdkey.discount_pct }}%</template>
                    <template v-else class="cjx-discount-none">—</template>
                  </td>
                  <td>
                    <button class="cjx-btn-buy" @click="buyCDKey(cdkey)">购买</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="cjx-empty-row">暂无可售 CDKey</div>

            <!-- 加载更多（分页折叠）-->
            <div class="cjx-load-more" v-if="cdkeyList.length > pageSize">
              <button class="cjx-btn-loadmore" @click="loadMore" v-if="displayCount < cdkeyList.length">
                加载更多（还剩 {{ cdkeyList.length - displayCount }} 条）
              </button>
              <span v-else class="cjx-all-loaded">— 已显示全部 {{ cdkeyList.length }} 条 —</span>
            </div>
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

          <!-- 服务保障（卖家信息已移至左侧出售卖家表格） -->
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
import { ref, computed, onMounted } from 'vue'
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

// 卖家 listing 模式
const selectedListingId = ref(route.query.listing_id || null)
const isSellerListing = ref(route.query.source === 'seller' || !!route.query.listing_id)
const activeListingIds = ref<string[]>([]) // 当前选中行可用的 listing_ids（聚合库存）
const activeRowSource = ref<'official' | 'seller' | null>(null)

// 分页（卖家列表折叠 + 加载更多）
const pageSize = 5
const displayCount = ref(pageSize)
const loadMore = () => { displayCount.value += pageSize }

// 可见行（前 displayCount 条）
const visibleCdkeyList = computed(() => cdkeyList.value.slice(0, displayCount.value))

// 最低价行（排序后的第一条）
const cheapestRow = computed(() => cdkeyList.value.length > 0 ? cdkeyList.value[0] : null)

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
  // 记录选中行信息
  activeRowSource.value = cdkey.source || (cdkey.listing_ids ? 'seller' : 'official')
  if (cdkey.listing_ids && cdkey.listing_ids.length > 0) {
    activeListingIds.value = [...cdkey.listing_ids]
  } else if (cdkey.id) {
    activeListingIds.value = [cdkey.id]
  } else {
    activeListingIds.value = []
  }
  selectedListingId.value = activeListingIds.value.length > 0 ? activeListingIds.value[0] : null

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
  // 自动选最低价行（排序后第一条）
  const row = cheapestRow.value
  if (row) {
    buyCDKey(row)
  } else {
    alert('暂无可售 CDKey')
  }
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

    const currentUser = authAPI.getCurrentUser()
    const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
    
    // 创建订单
    let orderResult
    try {
      const orderBody: any = {
        buyer_id: userId,
        game_id: game.value.id,
        game_name: game.value.name,
        game_image: game.value.image,
        price: game.value.current_price,
        quantity: quantity.value,
        total_price: total,
        delivery_method: deliveryMethod.value,
        version: getVersionType(game.value.name),
        status: 'completed',
        payment_method: selectedPayment.value
      }

      // 卖家 listing 模式：后端自动发货 + 自动填 cdkey + seller_id
      if (activeListingIds.value.length > 0 && activeRowSource.value !== 'official') {
        // 从当前聚合行中随机选一个可用 listing
        const idx = Math.floor(Math.random() * activeListingIds.value.length)
        const picked = activeListingIds.value.splice(idx, 1)[0]
        orderBody.listing_id = picked
        // 不传 cdkey，让后端从 listing 表自动取
      } else {
        // 官方模式：前端生成假 cdkey（展示用）
        orderBody.cdkey = generateCdkey()
      }

      console.log('正在调用 orderAPI.createOrder...', orderBody)
      orderResult = await orderAPI.createOrder(orderBody)
      
      console.log('orderAPI.createOrder 返回:', orderResult)
      
      if (orderResult.error) {
        alert('下单失败: ' + orderResult.error)
        return
      }
    } catch (err: any) {
      alert('下单失败: ' + (err?.message || '未知错误'))
      return
    }
    
    // 从后端订单取 cdkey（卖家 listing 模式下后端自动填入；官方模式也是后端返回的）
    const returnedCdkey = orderResult?.data?.cdkey || orderResult?.cdkey || ''
    currentCdkey.value = returnedCdkey || '（订单已创建，请在订单记录中查看）'
    
    console.log('✓ 订单创建成功，CDKey:', currentCdkey.value)

    // ===== 前端扣库存（更新 cdkeyList 对应行）=====
    // 遍历 cdkeyList，找到刚才选中的那一行，stock--，stock=0 时移除
    const targetKey = activeRowSource.value === 'seller'
      ? activeListingIds.value  // 已被 splice 减少
      : null
    for (let i = 0; i < cdkeyList.value.length; i++) {
      const row = cdkeyList.value[i]
      if (row.source === 'seller' && row.listing_ids) {
        // 如果 listing_ids 数量变了（刚才 splice 过），说明这行被选中了
        // 更直接：看当前 activeListingIds 是否是这行 listing_ids 的子集（因为 splice 从 activeListingIds 删了一个）
        const origLen = row.listing_ids.length
        const remainingInRow = row.listing_ids.filter((id: string) => activeListingIds.value.includes(id)).length
        // 简单处理：直接让这行 stock-- 并重算
        const newStock = Math.max(0, origLen - 1)
        row.stock = newStock
        if (newStock === 0) {
          cdkeyList.value.splice(i, 1)
          i--
        } else {
          // 同步 listing_ids 也移除已售的那个
          const soldId = row.listing_ids.find((id: string) => !activeListingIds.value.includes(id))
          if (soldId) {
            const idx = row.listing_ids.indexOf(soldId)
            if (idx >= 0) row.listing_ids.splice(idx, 1)
          }
        }
        break
      } else if (row.source === 'official') {
        // 官方行库存也 -1（模拟）
        row.stock = Math.max(0, (row.stock || 0) - 1)
        if ((row.stock || 0) === 0) {
          cdkeyList.value.splice(i, 1)
          i--
        }
        break
      }
    }

    // 如果官方行被移除或不再是最低价，更新 game.current_price 为当前最低价
    if (cheapestRow.value) {
      game.value.current_price = cheapestRow.value.price
    }
    
    // 关闭支付弹窗 + 显示 CDKey
    showPayModal.value = false
    showCdkeyModal.value = true
    
  } catch (error) {
    console.error('支付处理出错:', error)
    alert('支付处理出现问题，请重试')
    showPayModal.value = false
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
          // ===== 获取数据库游戏ID（多来源兜底）=====
          let dbGameId: any = route.query.game_id || null
          if (!dbGameId) {
            try {
              const dbGames = await request(`${SUPABASE_URL}/rest/v1/games?name=eq.${encodeURIComponent(decodedId)}&limit=1`)
              if (dbGames && dbGames.length > 0) {
                dbGameId = dbGames[0].id
                console.log('从 Supabase 获取 game_id:', dbGameId)
              }
            } catch (e) {
              console.warn('Supabase 拿不到 game_id，用 game_name 试试')
            }
          }
          if (dbGameId) console.log('✓ 最终 game_id =', dbGameId)

          // ===== 加载官方游戏数据 =====
      
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

          // ===== 构建 出售卖家 列表：官方 + 卖家 listings =====
          // 0. 统一算折扣
          const calcDiscount = (price: number, original: number) => {
            if (!original || original <= price) return 0
            return Math.round((1 - price / original) * 100)
          }

          // 1. 官方游戏一行
          const officialRow = {
            _rowKey: 'official',
            source: 'official',
            id: 'official',
            seller_name: 'S***y',
            avatar: '',
            price: currentPrice,
            stock: foundGame.stock || 99,
            listing_ids: [],
            version: getVersionType(foundGame.name),
            discount_pct: calcDiscount(currentPrice, originalPrice)
          }

          // 2. 卖家 listings（后端按 卖家+价格 聚合）
          let sellerRows: any[] = []
          try {
            const params: any = {}
            if (dbGameId) params.game_id = dbGameId
            params.game_name = decodedId // 永远带上 game_name 兜底
            console.log('查询卖家 listings params:', params)
            const gr = await axios.get(`/api/listings/available-grouped`, { params })
            console.log('卖家 listings 返回:', gr.data)
            if (gr.data?.code === 200 && Array.isArray(gr.data.data)) {
              sellerRows = gr.data.data.map((g: any, i: number) => ({
                _rowKey: `seller-${g.seller_id}-${g.price}-${i}`,
                source: 'seller',
                seller_id: g.seller_id,
                seller_name: g.seller_name,
                avatar: '',
                price: Number(g.price),
                stock: g.stock,
                listing_ids: g.listing_ids || [],
                version: g.version,
                discount_pct: calcDiscount(Number(g.price), originalPrice)
              }))
            }
          } catch (e) {
            console.warn('加载卖家 listings 失败', e)
          }

          // 3. 合并：按价格升序，同价卖家优先
          const all = [officialRow, ...sellerRows]
          all.sort((a, b) => {
            const pa = a.price, pb = b.price
            if (pa !== pb) return pa - pb
            const ra = a.source === 'seller' ? 0 : 1
            const rb = b.source === 'seller' ? 0 : 1
            return ra - rb
          })
          cdkeyList.value = all

          // 如果 URL 带了 listing_id，定位到那一行的 listing_ids
          if (selectedListingId.value) {
            const target = sellerRows.find(r => r.listing_ids.includes(selectedListingId.value))
            if (target) activeListingIds.value = [...target.listing_ids]
          }
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
  margin: 0 0 18px 0;
  color: #333;
  font-size: 16px;
}

/* 图1风格 卖家表格 */
.cjx-seller-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
}
.cjx-seller-table th,
.cjx-seller-table td {
  padding: 13px 14px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}
.cjx-seller-table thead th {
  background: #fafafa;
  font-weight: 600;
  color: #555;
  font-size: 12px;
}
.cjx-seller-table tbody tr:hover {
  background: #fafafa;
}
.cjx-seller-table tbody tr:last-child td {
  border-bottom: none;
}

/* 最低价行高亮 */
.cjx-row-min td {
  background: #fff8e1 !important;
}

.cjx-rank {
  color: #999;
  font-size: 13px;
  text-align: center;
}

/* 头像 */
.cjx-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background: #f0f0f0;
  display: inline-block;
  vertical-align: middle;
  border: 1px solid #eee;
}
.cjx-avatar-default {
  text-align: center;
  line-height: 32px;
  font-size: 16px;
}

/* 用户名 */
.cjx-official-name {
  color: #1565c0;
  font-weight: 500;
}
.cjx-seller-name {
  color: #333;
}

.cjx-min-tag {
  background: #ff9800;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
  margin-left: 6px;
  vertical-align: middle;
}

/* 价格 */
.cjx-price-cell {
  color: #e74c3c;
  font-weight: 600;
  font-size: 14px;
}

/* 折扣 */
.cjx-discount-green {
  color: #e74c3c;
  font-weight: 600;
}
.cjx-discount-none {
  color: #ccc;
}

/* 购买按钮 */
.cjx-btn-buy {
  padding: 5px 16px;
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
}
.cjx-btn-buy:hover {
  background: #c0392b;
}

/* 分页/加载更多 */
.cjx-load-more {
  text-align: center;
  margin-top: 14px;
  padding: 10px;
}
.cjx-btn-loadmore {
  padding: 8px 22px;
  background: #fff;
  border: 1px solid #27ae60;
  color: #27ae60;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
}
.cjx-btn-loadmore:hover {
  background: #e8f8f0;
}
.cjx-all-loaded {
  font-size: 12px;
  color: #aaa;
}
.cjx-empty-row {
  text-align: center;
  padding: 30px;
  color: #999;
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

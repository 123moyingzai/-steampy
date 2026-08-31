<template>
  <Layout>
    <div class="cjx-games-page">
      <h2 class="cjx-page-title">我的游戏库 719cjx</h2>

      <!-- 统计概览 -->
      <div class="cjx-stats-cards">
        <div class="cjx-stat-card">
          <span class="cjx-stat-label">已购游戏 719cjx</span>
          <span class="cjx-stat-value">{{ stats.totalGames }}</span>
        </div>
        <div class="cjx-stat-card">
          <span class="cjx-stat-label">待激活 719cjx</span>
          <span class="cjx-stat-value">{{ stats.pendingActivation }}</span>
        </div>
        <div class="cjx-stat-card">
          <span class="cjx-stat-label">已入库 719cjx</span>
          <span class="cjx-stat-value">{{ stats.activated }}</span>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="cjx-filter-bar">
        <input 
          type="text" 
          v-model="searchQuery"
          placeholder="搜索游戏名称 719cjx"
          class="cjx-search-input"
        />
        <select v-model="statusFilter" class="cjx-select">
          <option value="">全部状态</option>
          <option value="pending">待激活</option>
          <option value="activated">已入库</option>
        </select>
        <button class="cjx-btn cjx-btn-primary" @click="refreshGames">刷新</button>
      </div>

      <!-- 游戏列表 -->
      <div class="cjx-games-grid" v-if="filteredGames.length > 0">
        <div 
          v-for="game in filteredGames" 
          :key="game.id"
          class="cjx-game-card"
          :class="{ 'cjx-activated': game.status === 'activated' }"
        >
          <div class="cjx-game-cover">
            <img :src="game.image" :alt="game.name" />
            <span class="cjx-status-badge" :class="`status-${game.status}`">
              {{ game.status === 'activated' ? '已入库' : '待激活' }}
            </span>
          </div>
          
          <div class="cjx-game-info">
            <h3 class="cjx-game-name">{{ game.name }} 719cjx</h3>
            <p class="cjx-game-meta">
              <span>购买时间：{{ formatDate(game.purchase_date) }}</span>
              <span>版本：{{ game.version }}</span>
            </p>
            
            <!-- CDKey展示区域 -->
            <div class="cjx-cdkey-section">
              <div class="cjx-cdkey-header">
                <span class="cjx-cdkey-label">CDKey</span>
                <button 
                  class="cjx-btn-text"
                  @click="toggleCdkeyVisibility(game)"
                >
                  {{ game.showCdkey ? '隐藏' : '显示' }}
                </button>
              </div>
              <div class="cjx-cdkey-box">
                <code class="cjx-cdkey-value">
                  {{ game.showCdkey ? game.cdkey : maskCdkey(game.cdkey) }}
                </code>
                <button 
                  class="cjx-btn-icon"
                  @click="copyCdkey(game.cdkey)"
                  title="复制CDKey"
                >
                  📋
                </button>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="cjx-game-actions">
              <button 
                v-if="game.status === 'pending'"
                class="cjx-btn cjx-btn-activate"
                @click="activateGame(game)"
              >
                立即激活
              </button>
              <button 
                v-else
                class="cjx-btn cjx-btn-secondary"
                @click="viewInLibrary(game)"
              >
                查看详情
              </button>
              <button 
                class="cjx-btn cjx-btn-text"
                @click="showActivationGuide"
              >
                激活教程
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="cjx-empty" v-else>
        <div class="cjx-empty-icon">🎮</div>
        <p>暂无游戏 719cjx</p>
        <button class="cjx-btn cjx-btn-primary" @click="$router.push('/cdkey')">
          去购买游戏
        </button>
      </div>
    </div>

    <!-- 激活确认弹窗 -->
    <div class="cjx-modal" v-if="activatingGame" @click.self="activatingGame = null">
      <div class="cjx-modal-content">
        <div class="cjx-modal-header">
          <h3>确认激活 719cjx</h3>
          <button class="cjx-modal-close" @click="activatingGame = null">×</button>
        </div>
        <div class="cjx-modal-body">
          <p class="cjx-confirm-text">
            确认已将 <strong>{{ activatingGame.name }}</strong> 激活到您的Steam账号？
          </p>
          <div class="cjx-cdkey-display">
            <span>CDKey：</span>
            <code>{{ activatingGame.cdkey }}</code>
          </div>
          <div class="cjx-activation-steps">
            <h4>激活步骤 719cjx：</h4>
            <ol>
              <li>打开Steam客户端</li>
              <li>点击左下角"+添加游戏"</li>
              <li>选择"在Steam上激活产品"</li>
              <li>输入上方CDKey完成激活</li>
            </ol>
          </div>
        </div>
        <div class="cjx-modal-footer">
          <button class="cjx-btn cjx-btn-secondary" @click="activatingGame = null">取消</button>
          <button class="cjx-btn cjx-btn-primary" @click="confirmActivation">确认已激活</button>
        </div>
      </div>
    </div>

    <!-- 激活教程弹窗 -->
    <div class="cjx-modal" v-if="showGuide" @click.self="showGuide = false">
      <div class="cjx-modal-content cjx-modal-large">
        <div class="cjx-modal-header">
          <h3>Steam激活教程 719cjx</h3>
          <button class="cjx-modal-close" @click="showGuide = false">×</button>
        </div>
        <div class="cjx-modal-body">
          <div class="cjx-guide-steps">
            <div class="cjx-guide-step">
              <div class="cjx-step-number">1</div>
              <div class="cjx-step-content">
                <h4>打开Steam客户端</h4>
                <p>启动Steam并登录您的账号 719cjx</p>
              </div>
            </div>
            <div class="cjx-guide-step">
              <div class="cjx-step-number">2</div>
              <div class="cjx-step-content">
                <h4>添加游戏</h4>
                <p>点击左下角的"+ 添加游戏"按钮 719cjx</p>
              </div>
            </div>
            <div class="cjx-guide-step">
              <div class="cjx-step-number">3</div>
              <div class="cjx-step-content">
                <h4>选择激活产品</h4>
                <p>在菜单中选择"在Steam上激活产品" 719cjx</p>
              </div>
            </div>
            <div class="cjx-guide-step">
              <div class="cjx-step-number">4</div>
              <div class="cjx-step-content">
                <h4>输入CDKey</h4>
                <p>输入您获得的CDKey，点击确认完成激活 719cjx</p>
              </div>
            </div>
          </div>
          <div class="cjx-guide-tips">
            <h4>注意事项 719cjx：</h4>
            <ul>
              <li>请确保您登录的是正确的Steam账号</li>
              <li>CDKey一经使用即绑定到该账号，无法转移</li>
              <li>如遇问题请联系客服 719cjx</li>
            </ul>
          </div>
        </div>
        <div class="cjx-modal-footer">
          <button class="cjx-btn cjx-btn-primary" @click="showGuide = false">我知道了</button>
        </div>
      </div>
    </div>

    <!-- CDKey复制成功提示 -->
    <div class="cjx-toast" v-if="toastMessage">
      {{ toastMessage }}
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, userGameAPI, orderAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const games = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const activatingGame = ref(null)
const showGuide = ref(false)
const toastMessage = ref('')

// 统计
const stats = computed(() => {
  return games.value.reduce((acc, game) => {
    acc.totalGames++
    if (game.status === 'pending') acc.pendingActivation++
    if (game.status === 'activated') acc.activated++
    return acc
  }, { totalGames: 0, pendingActivation: 0, activated: 0 })
})

// 筛选后的游戏
const filteredGames = computed(() => {
  let result = games.value
  
  if (searchQuery.value) {
    result = result.filter(g => g.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
  }
  
  if (statusFilter.value) {
    result = result.filter(g => g.status === statusFilter.value)
  }
  
  // 按购买时间排序（最新的在前）
  return result.sort((a, b) => new Date(b.purchase_date) - new Date(a.purchase_date))
})

// 方法
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const maskCdkey = (cdkey) => {
  if (!cdkey) return '****-****-****'
  return cdkey.substring(0, 5) + '****-****-****' + cdkey.substring(cdkey.length - 4)
}

const toggleCdkeyVisibility = (game) => {
  game.showCdkey = !game.showCdkey
}

const copyCdkey = async (cdkey) => {
  try {
    await navigator.clipboard.writeText(cdkey)
    showToast('CDKey已复制到剪贴板 719cjx')
  } catch (err) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = cdkey
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    showToast('CDKey已复制到剪贴板 719cjx')
  }
}

const showToast = (message) => {
  toastMessage.value = message
  setTimeout(() => {
    toastMessage.value = ''
  }, 3000)
}

const activateGame = (game) => {
  activatingGame.value = game
}

const confirmActivation = async () => {
  if (activatingGame.value) {
    try {
      // 更新数据库中的激活状态
      await userGameAPI.activateGame(activatingGame.value.id)
      
      activatingGame.value.status = 'activated'
      activatingGame.value.activation_date = new Date().toISOString()
      
      showToast('游戏已标记为已激活 719cjx')
    } catch (error) {
      console.error('激活失败:', error)
      
      // 降级到 localStorage
      activatingGame.value.status = 'activated'
      activatingGame.value.activation_date = new Date().toISOString()
      localStorage.setItem('myGames', JSON.stringify(games.value))
      
      showToast('游戏已标记为已激活（本地） 719cjx')
    }
    
    activatingGame.value = null
  }
}

const viewInLibrary = (game) => {
  alert(`游戏 ${game.name} 已在您的Steam库中 719cjx`)
}

const showActivationGuide = () => {
  showGuide.value = true
}

const refreshGames = () => {
  loadData()
  showToast('游戏列表已刷新 719cjx')
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

// 加载数据 - 从 Supabase 数据库获取
const loadData = async () => {
  const currentUser = authAPI.getCurrentUser()
  const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  
  try {
    // 从数据库加载用户的游戏库
    const result = await userGameAPI.getUserGames(userId)
    
    if (result.data) {
      games.value = result.data.map(g => ({
        id: g.id,
        order_id: g.order_id,
        name: g.game_name,
        image: g.game_image || '/picture/安魂曲.jpg',
        cdkey: g.cdkey,
        showCdkey: false,
        status: g.status || 'pending',
        purchase_date: g.purchase_date,
        activation_date: g.activation_date,
        version: g.version || '标准版'
      }))
      
      console.log(`✓ 从数据库加载了 ${games.value.length} 个游戏`)
    }
  } catch (error) {
    console.error('从数据库加载游戏库失败，尝试本地存储:', error)
    
    // 降级到 localStorage
    const savedGames = localStorage.getItem('myGames')
    if (savedGames) {
      games.value = JSON.parse(savedGames)
    }
    
    const savedOrders = localStorage.getItem('orders')
    if (savedOrders) {
      const orders = JSON.parse(savedOrders)
      orders.forEach(order => {
        if (order.status === 'completed' && !games.value.find(g => g.order_id === order.id)) {
          games.value.push({
            id: `G${Date.now()}${Math.random().toString(36).substr(2, 9)}`,
            order_id: order.id,
            name: order.game_name || '未知游戏',
            image: order.game_image || '/picture/安魂曲.jpg',
            cdkey: generateCdkey(),
            showCdkey: false,
            status: 'pending',
            purchase_date: order.created_at,
            activation_date: null,
            version: order.version || '标准版'
          })
        }
      })
      localStorage.setItem('myGames', JSON.stringify(games.value))
    }
  }
}

// 生命周期
onMounted(() => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录 719cjx')
    router.push('/login')
    return
  }
  loadData()
})
</script>

<style scoped>
.cjx-games-page {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
}

.cjx-page-title {
  margin: 0 0 25px 0;
  font-size: 24px;
  color: #333;
}

/* 统计卡片 */
.cjx-stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.cjx-stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 25px;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.cjx-stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.cjx-stat-value {
  font-size: 32px;
  font-weight: bold;
}

/* 筛选栏 */
.cjx-filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
}

.cjx-search-input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.cjx-select {
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  min-width: 150px;
}

.cjx-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.cjx-btn-primary {
  background: #3498db;
  color: #fff;
}

.cjx-btn-primary:hover {
  background: #2980b9;
}

.cjx-btn-secondary {
  background: #f0f0f0;
  color: #666;
}

.cjx-btn-secondary:hover {
  background: #e0e0e0;
}

.cjx-btn-text {
  background: none;
  color: #3498db;
  padding: 8px 12px;
}

.cjx-btn-text:hover {
  background: #f0f8ff;
}

.cjx-btn-icon {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 5px;
}

/* 游戏网格 */
.cjx-games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.cjx-game-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.cjx-game-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.cjx-game-card.cjx-activated {
  border-color: #27ae60;
}

.cjx-game-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.cjx-game-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cjx-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.cjx-status-badge.status-pending {
  background: #fff3cd;
  color: #856404;
}

.cjx-status-badge.status-activated {
  background: #d4edda;
  color: #155724;
}

.cjx-game-info {
  padding: 20px;
}

.cjx-game-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cjx-game-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 12px;
  color: #666;
  margin-bottom: 15px;
}

/* CDKey区域 */
.cjx-cdkey-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.cjx-cdkey-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.cjx-cdkey-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.cjx-cdkey-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cjx-cdkey-value {
  flex: 1;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  background: #fff;
  padding: 10px 15px;
  border-radius: 6px;
  border: 1px solid #ddd;
  letter-spacing: 1px;
}

/* 操作按钮 */
.cjx-game-actions {
  display: flex;
  gap: 10px;
}

.cjx-btn-activate {
  flex: 1;
  background: #e74c3c;
  color: #fff;
}

.cjx-btn-activate:hover {
  background: #c0392b;
}

/* 空状态 */
.cjx-empty {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.cjx-empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.cjx-empty p {
  margin-bottom: 20px;
}

/* 弹窗 */
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
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.cjx-modal-large {
  max-width: 600px;
}

.cjx-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.cjx-modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.cjx-modal-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  line-height: 1;
}

.cjx-modal-close:hover {
  color: #333;
}

.cjx-modal-body {
  padding: 20px;
}

.cjx-modal-footer {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 激活弹窗内容 */
.cjx-confirm-text {
  margin-bottom: 20px;
  line-height: 1.6;
}

.cjx-cdkey-display {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.cjx-cdkey-display code {
  font-family: 'Courier New', monospace;
  font-size: 16px;
  color: #e74c3c;
  margin-left: 10px;
}

.cjx-activation-steps {
  background: #f0f8ff;
  padding: 15px;
  border-radius: 8px;
}

.cjx-activation-steps h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.cjx-activation-steps ol {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.8;
}

/* 教程弹窗 */
.cjx-guide-steps {
  margin-bottom: 25px;
}

.cjx-guide-step {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.cjx-step-number {
  width: 36px;
  height: 36px;
  background: #3498db;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.cjx-step-content h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.cjx-step-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.cjx-guide-tips {
  background: #fff3cd;
  padding: 15px;
  border-radius: 8px;
}

.cjx-guide-tips h4 {
  margin: 0 0 10px 0;
  color: #856404;
}

.cjx-guide-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #856404;
  font-size: 14px;
  line-height: 1.8;
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

@media (max-width: 768px) {
  .cjx-stats-cards {
    grid-template-columns: 1fr;
  }
  
  .cjx-games-grid {
    grid-template-columns: 1fr;
  }
  
  .cjx-filter-bar {
    flex-direction: column;
  }
}
</style>

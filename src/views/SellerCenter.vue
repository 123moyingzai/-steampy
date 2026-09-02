<template>
  <Layout>
    <div class="cjx-seller-page">
      <h2 class="cjx-page-title">卖家中心</h2>

      <!-- Tab 切换 -->
      <div class="cjx-tabs-nav">
        <ul class="cjx-tabs-list">
          <li :class="['cjx-tab-item', { active: activeTab === 'listings' }]" @click="activeTab = 'listings'">
            📦 CDKey管理（{{ sellerListings.length }}）
          </li>
          <li :class="['cjx-tab-item', { active: activeTab === 'new' }]" @click="activeTab = 'new'">
            ➕ 上架CDKey
          </li>
          <li :class="['cjx-tab-item', { active: activeTab === 'orders' }]" @click="activeTab = 'orders'">
            📋 卖家订单（{{ sellerOrders.length }}）
          </li>
        </ul>
      </div>

      <!-- 统计卡片 -->
      <div class="cjx-stats">
        <div class="cjx-stat-card">
          <h4>在售CDKey</h4>
          <p class="cjx-stat-value">{{ stats.available }}</p>
        </div>
        <div class="cjx-stat-card">
          <h4>已售出</h4>
          <p class="cjx-stat-value">{{ stats.sold }}</p>
        </div>
        <div class="cjx-stat-card">
          <h4>总销售额</h4>
          <p class="cjx-stat-value">¥{{ stats.totalSold.toFixed(2) }}</p>
        </div>
      </div>

      <!-- ======= Tab 1: CDKey 管理 ======= -->
      <div class="cjx-section" v-if="activeTab === 'listings'">
        <div class="cjx-filter-bar">
          <input type="text" v-model="listingSearch" placeholder="搜索游戏名" class="cjx-search-input" />
          <select v-model="listingStatusFilter" class="cjx-select">
            <option value="">全部状态</option>
            <option value="available">在售</option>
            <option value="sold">已售出</option>
          </select>
          <button class="cjx-btn cjx-btn-primary" @click="loadAll">刷新</button>
        </div>

        <table class="cjx-table" v-if="filteredListings.length > 0">
          <thead>
            <tr>
              <th>游戏</th>
              <th>版本</th>
              <th>CDKey</th>
              <th>价格</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in filteredListings" :key="l.id">
              <td>{{ l.game_name }}</td>
              <td>{{ l.version }}</td>
              <td>
                <code class="cjx-cdkey-mini">
                  {{ showCdkey(l) ? l.cdkey : maskCdkey(l.cdkey) }}
                </code>
                <button class="cjx-btn-icon" @click="toggleCdkey(l)">👁</button>
              </td>
              <td class="price">¥{{ l.price?.toFixed(2) }}</td>
              <td>
                <span :class="['cjx-badge', l.status === 'available' ? 'badge-green' : 'badge-gray']">
                  {{ l.status === 'available' ? '在售' : '已售' }}
                </span>
              </td>
              <td>{{ formatDate(l.created_at) }}</td>
              <td>
                <button 
                  v-if="l.status === 'available'" 
                  class="cjx-btn cjx-btn-small cjx-btn-danger" 
                  @click="deleteListing(l)"
                >
                  下架
                </button>
                <span v-else class="cjx-text-muted">-</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="cjx-empty" v-else>
          <p>暂无 CDKey 上架记录</p>
          <button class="cjx-btn cjx-btn-primary" @click="activeTab = 'new'">去上架</button>
        </div>
      </div>

      <!-- ======= Tab 2: 上架 CDKey ======= -->
      <div class="cjx-section" v-if="activeTab === 'new'">
        <div class="cjx-form-card">
          <h3 class="cjx-form-title">上架新 CDKey</h3>
          <div class="cjx-form-grid">
            <div class="cjx-form-item">
              <label>选择游戏 *</label>
              <select v-model="newForm.game_id" class="cjx-select" @change="onGameSelect">
                <option value="">-- 请选择游戏 --</option>
                <option v-for="g in games" :key="g.id" :value="g.id">{{ g.name }}</option>
              </select>
            </div>
            <div class="cjx-form-item">
              <label>版本</label>
              <select v-model="newForm.version" class="cjx-select">
                <option>标准版</option>
                <option>豪华版</option>
                <option>终极版</option>
                <option>国区</option>
              </select>
            </div>
            <div class="cjx-form-item">
              <label>售价 (¥) *</label>
              <input type="number" step="0.01" v-model.number="newForm.price" class="cjx-input" placeholder="299.00" />
            </div>
            <div class="cjx-form-item">
              <label>原价 (¥)</label>
              <input type="number" step="0.01" v-model.number="newForm.original_price" class="cjx-input" placeholder="选填" />
            </div>
            <div class="cjx-form-item">
              <label>区域</label>
              <select v-model="newForm.region" class="cjx-select">
                <option>国区</option>
                <option>港区</option>
                <option>美区</option>
                <option>全球</option>
              </select>
            </div>
          </div>
          <div class="cjx-form-item">
            <label>CDKey 列表 *（每行一个）</label>
            <textarea 
              v-model="newForm.cdkeys" 
              class="cjx-textarea" 
              rows="6" 
              placeholder="ABCD-1234-EFGH-5678&#10;IJKL-9012-MNOP-3456"
            ></textarea>
          </div>
          <div class="cjx-form-actions">
            <span class="cjx-hint">将批量上架 {{ newForm.cdkeys.split('\n').filter(x => x.trim()).length }} 个 CDKey</span>
            <button class="cjx-btn cjx-btn-primary" @click="submitListings" :disabled="submitting">
              {{ submitting ? '提交中...' : '确认上架' }}
            </button>
          </div>
        </div>
      </div>

      <!-- ======= Tab 3: 卖家订单 ======= -->
      <div class="cjx-section" v-if="activeTab === 'orders'">
        <table class="cjx-table" v-if="sellerOrders.length > 0">
          <thead>
            <tr>
              <th>订单号</th>
              <th>买家</th>
              <th>游戏</th>
              <th>成交价</th>
              <th>状态</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in sellerOrders" :key="o.id">
              <td class="mono">{{ o.order_no }}</td>
              <td>{{ o.buyer_id?.substring(0, 8) || '未知' }}...</td>
              <td>{{ o.game_name }}</td>
              <td class="price">¥{{ o.total_price?.toFixed(2) }}</td>
              <td>
                <span :class="['cjx-badge', o.status === 'completed' ? 'badge-green' : o.status === 'cancelled' ? 'badge-red' : 'badge-gray']">
                  {{ getStatusText(o.status) }}
                </span>
              </td>
              <td>{{ formatDate(o.created_at) }}</td>
            </tr>
          </tbody>
        </table>
        <div class="cjx-empty" v-else>
          <p>暂无卖家订单</p>
        </div>
      </div>

      <!-- Toast -->
      <div class="cjx-toast" v-if="toastMessage">{{ toastMessage }}</div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { authAPI, listingAPI, orderAPI, gameAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const activeTab = ref('listings')
const sellerListings = ref<any[]>([])
const sellerOrders = ref<any[]>([])
const games = ref<any[]>([])
const listingSearch = ref('')
const listingStatusFilter = ref('')
const submitting = ref(false)
const toastMessage = ref('')

const newForm = ref({
  game_id: '' as any,
  game_name: '',
  game_image: '',
  version: '标准版',
  price: null as number | null,
  original_price: 0,
  region: '国区',
  cdkeys: ''
})

const showCdkeyMap = ref<Record<string, boolean>>({})
const toggleCdkey = (l: any) => { showCdkeyMap.value[l.id] = !showCdkeyMap.value[l.id] }
const showCdkey = (l: any) => showCdkeyMap.value[l.id]
const maskCdkey = (k: string) => k ? k.substring(0, 4) + '****' + k.substring(k.length - 4) : ''

const filteredListings = computed(() => {
  let r = sellerListings.value
  if (listingSearch.value) r = r.filter(x => x.game_name?.includes(listingSearch.value))
  if (listingStatusFilter.value) r = r.filter(x => x.status === listingStatusFilter.value)
  return r.sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
})

const stats = computed(() => {
  const available = sellerListings.value.filter(l => l.status === 'available').length
  const sold = sellerListings.value.filter(l => l.status === 'sold').length
  const totalSold = sellerListings.value
    .filter(l => l.status === 'sold')
    .reduce((sum, l) => sum + Number(l.price || 0), 0)
  return { available, sold, totalSold }
})

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : '-'
const getStatusText = (s: string) => ({ completed: '已完成', cancelled: '已取消', pending: '待处理' }[s] || s)

const onGameSelect = () => {
  const g = games.value.find(x => String(x.id) === String(newForm.value.game_id))
  if (g) {
    newForm.value.game_name = g.name
    newForm.value.game_image = g.image_url || g.image || ''
    newForm.value.original_price = Number(g.original_price || 0)
    if (!newForm.value.price) newForm.value.price = Number(g.price || 0)
  }
}

const showToast = (msg: string) => {
  toastMessage.value = msg
  setTimeout(() => toastMessage.value = '', 3000)
}

const loadAll = async () => {
  const user = authAPI.getCurrentUser()
  if (!user?.id) return

  // 加载卖家的 CDKey
  const lr = await listingAPI.getBySeller(user.id)
  if (lr.data) sellerListings.value = lr.data

  // 加载卖家的订单
  const or = await orderAPI.getSellerOrders(user.id)
  if (or.data) sellerOrders.value = or.data

  // 加载游戏列表
  const gr = await gameAPI.getGames()
  if (gr.data) games.value = gr.data
}

const submitListings = async () => {
  const user = authAPI.getCurrentUser()
  if (!user) { showToast('请先登录'); return }
  if (!newForm.value.game_id) { showToast('请选择游戏'); return }
  if (!newForm.value.price || newForm.value.price <= 0) { showToast('请输入有效价格'); return }

  const cdkeyList = newForm.value.cdkeys.split('\n').map(x => x.trim()).filter(x => x)
  if (cdkeyList.length === 0) { showToast('请至少输入一个 CDKey'); return }

  submitting.value = true
  try {
    const items = cdkeyList.map(cdkey => ({
      seller_id: user.id,
      game_id: Number(newForm.value.game_id),
      game_name: newForm.value.game_name,
      game_image: newForm.value.game_image,
      version: newForm.value.version,
      cdkey,
      price: newForm.value.price,
      original_price: newForm.value.original_price,
      region: newForm.value.region
    }))

    const result = await listingAPI.createBatch(items)
    if (result.error) {
      showToast('上架失败: ' + result.error)
    } else {
      showToast(`✅ 成功上架 ${cdkeyList.length} 个 CDKey`)
      // 清空表单
      newForm.value = { game_id: '', game_name: '', game_image: '', version: '标准版', price: null, original_price: 0, region: '国区', cdkeys: '' }
      activeTab.value = 'listings'
      loadAll()
    }
  } catch (e: any) {
    showToast('上架失败: ' + e.message)
  } finally {
    submitting.value = false
  }
}

const deleteListing = async (l: any) => {
  if (!confirm(`确定下架 CDKey: ${l.cdkey.substring(0, 8)}... ?`)) return
  const r = await listingAPI.deleteListing(l.id)
  if (r.error) {
    showToast('下架失败: ' + r.error)
  } else {
    showToast('已下架')
    loadAll()
  }
}

onMounted(loadAll)
</script>

<style scoped>
.cjx-seller-page { background: #fff; border-radius: 8px; padding: 25px; }
.cjx-page-title { margin: 0 0 20px; font-size: 24px; color: #333; }

/* Tabs */
.cjx-tabs-nav { border-bottom: 2px solid #f0f0f0; margin-bottom: 25px; }
.cjx-tabs-list { list-style: none; display: flex; gap: 10px; margin: 0; padding: 0; }
.cjx-tab-item {
  padding: 12px 20px; cursor: pointer; border-radius: 6px 6px 0 0;
  color: #666; font-weight: 500; transition: all 0.2s;
}
.cjx-tab-item:hover { background: #f8f9fa; }
.cjx-tab-item.active { background: #3498db; color: #fff; }

/* Stats */
.cjx-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 25px; }
.cjx-stat-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 10px; padding: 20px; color: #fff; }
.cjx-stat-card h4 { margin: 0 0 8px; font-size: 13px; opacity: 0.9; font-weight: normal; }
.cjx-stat-value { margin: 0; font-size: 28px; font-weight: bold; }

/* Filter */
.cjx-filter-bar { display: flex; gap: 10px; margin-bottom: 20px; }
.cjx-search-input { flex: 1; padding: 10px 14px; border: 1px solid #ddd; border-radius: 6px; }
.cjx-select { padding: 10px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; min-width: 120px; }
.cjx-input, .cjx-textarea { padding: 10px 14px; border: 1px solid #ddd; border-radius: 6px; width: 100%; font-family: inherit; }

/* Table */
.cjx-table { width: 100%; border-collapse: collapse; }
.cjx-table th, .cjx-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.cjx-table th { background: #f8f9fa; font-weight: 600; color: #555; }
.price { color: #e74c3c; font-weight: 600; }
.mono { font-family: 'Courier New', monospace; font-size: 12px; }

/* Badges */
.cjx-badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-green { background: #d4edda; color: #155724; }
.badge-gray { background: #e2e3e5; color: #383d41; }
.badge-red { background: #f8d7da; color: #721c24; }

/* Buttons */
.cjx-btn { padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; transition: all 0.2s; }
.cjx-btn-primary { background: #3498db; color: #fff; }
.cjx-btn-primary:hover { background: #2980b9; }
.cjx-btn-primary:disabled { background: #95a5a6; cursor: not-allowed; }
.cjx-btn-small { padding: 6px 12px; font-size: 12px; }
.cjx-btn-danger { background: #e74c3c; color: #fff; }
.cjx-btn-danger:hover { background: #c0392b; }
.cjx-btn-icon { background: none; border: none; cursor: pointer; font-size: 14px; margin-left: 4px; }

/* Form */
.cjx-form-card { background: #fafbfc; border-radius: 10px; padding: 25px; }
.cjx-form-title { margin: 0 0 20px; color: #333; }
.cjx-form-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 15px; }
.cjx-form-item { margin-bottom: 15px; }
.cjx-form-item label { display: block; margin-bottom: 6px; font-size: 13px; color: #555; font-weight: 500; }
.cjx-form-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.cjx-hint { color: #666; font-size: 13px; }

/* Misc */
.cjx-cdkey-mini { font-family: 'Courier New', monospace; font-size: 12px; color: #555; background: #f0f0f0; padding: 3px 6px; border-radius: 4px; }
.cjx-text-muted { color: #999; }
.cjx-empty { text-align: center; padding: 60px 20px; color: #999; }
.cjx-toast { position: fixed; bottom: 50px; left: 50%; transform: translateX(-50%); background: #333; color: #fff; padding: 12px 24px; border-radius: 6px; z-index: 2000; }

@media (max-width: 768px) {
  .cjx-stats { grid-template-columns: 1fr; }
  .cjx-form-grid { grid-template-columns: 1fr; }
}
</style>

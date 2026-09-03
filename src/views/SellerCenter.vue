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
            <option value="pending_activation">待激活</option>
          </select>
          <button class="cjx-btn cjx-btn-primary" @click="loadAll">刷新</button>
        </div>

        <!-- 批量操作条（仅在有选中时显示）-->
        <div class="cjx-batch-bar" v-if="selectedIds.size > 0">
          <span class="cjx-batch-info">已选 {{ selectedIds.size }} 个</span>
          <button class="cjx-btn cjx-btn-quick" @click="openBatchPrice">⚡ 批量改价</button>
          <button class="cjx-btn cjx-btn-small" @click="toggleSelectAll">全选/全不选</button>
          <button class="cjx-btn cjx-btn-small cjx-btn-danger-outline" @click="clearSelection">取消</button>
        </div>

        <table class="cjx-table" v-if="filteredListings.length > 0">
          <thead>
            <tr>
              <th style="width:32px"><input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" :disabled="!hasSelectableRows" /></th>
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
              <td>
                <input type="checkbox" v-model="selectedIdsArr" :value="l.id" :disabled="l.status === 'sold'" />
              </td>
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
                <span :class="['cjx-badge', listingStatusBadgeClass(l.status)]">
                  {{ listingStatusText(l.status) }}
                </span>
                <span v-if="l.status === 'pending_activation'" class="cjx-pending-order-hint">
                  原订单 #{{ l.order_id?.substring(0, 8) || '-' }}
                </span>
              </td>
              <td>{{ formatDate(l.created_at) }}</td>
              <td>
                <!-- 在售 → 下架(变成待激活) + 定价 -->
                <template v-if="l.status === 'available'">
                  <button class="cjx-btn cjx-btn-small cjx-btn-quick" @click="openQuickPrice(l)">⚡定价</button>
                  <button class="cjx-btn cjx-btn-small cjx-btn-danger" @click="softDeleteListing(l)">下架</button>
                </template>
                <!-- 已售 → 不可操作 -->
                <template v-else-if="l.status === 'sold'">
                  <span class="cjx-text-muted">-</span>
                </template>
                <!-- 待激活 → 重新上架 或 自己激活 -->
                <template v-else-if="l.status === 'pending_activation'">
                  <button class="cjx-btn cjx-btn-small cjx-btn-primary" @click="jumpToRelist(l)">重新上架</button>
                  <button class="cjx-btn cjx-btn-small cjx-btn-generate" @click="selfActivate(l)">自己激活</button>
                </template>
                <span v-else class="cjx-text-muted">-</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="cjx-empty" v-else>
          <p>暂无 CDKey 上架记录</p>
          <button class="cjx-btn cjx-btn-primary" @click="activeTab = 'new'">去上架</button>
        </div>

        <!-- 批量改价弹窗 -->
        <div class="cjx-modal-mask" v-if="showBatchPriceModal" @click.self="closeBatchPrice">
          <div class="cjx-modal">
            <div class="cjx-modal-header">
              <h3>⚡ 批量改价（{{ batchPriceGroups.length }} 款游戏）</h3>
              <button class="cjx-btn-icon" @click="closeBatchPrice">✕</button>
            </div>
            <div class="cjx-modal-body">
              <div class="cjx-batch-group" v-for="g in batchPriceGroups" :key="g.game_id">
                <div class="cjx-batch-group-header">
                  <div class="cjx-batch-group-title">
                    <img :src="getImageUrl(g.image)" class="cjx-game-thumb" />
                    <span class="cjx-game-selected-name">{{ g.game_name }}</span>
                    <span class="cjx-batch-count">× {{ g.count }} 个CDKey</span>
                  </div>
                  <div class="cjx-batch-group-meta">
                    官方原价 ¥{{ g.original_price?.toFixed(2) || '0.00' }}
                    · 当前卖家最低 ¥{{ g.current_min?.toFixed(2) || '—' }}
                  </div>
                </div>
                <div class="cjx-batch-input-row">
                  <input type="number" step="0.01" v-model.number="g.price" class="cjx-input" placeholder="输入新价格" />
                  <button class="cjx-btn cjx-btn-quick" @click="g.price = batchOneClickPrice(g)">⚡一键定价 ¥{{ batchOneClickPrice(g).toFixed(2) }}</button>
                </div>
              </div>
            </div>
            <div class="cjx-modal-footer">
              <span class="cjx-hint">将更新 {{ selectedIds.size }} 个 CDKey 的价格</span>
              <button class="cjx-btn cjx-btn-primary" @click="applyBatchPrice">确认修改</button>
            </div>
          </div>
        </div>

        <!-- 单个改价弹窗 -->
        <div class="cjx-modal-mask" v-if="showQuickPriceModal" @click.self="showQuickPriceModal = false">
          <div class="cjx-modal" style="max-width:360px">
            <div class="cjx-modal-header">
              <h3>⚡ 改价 — {{ quickPriceTarget?.game_name }}</h3>
              <button class="cjx-btn-icon" @click="showQuickPriceModal = false">✕</button>
            </div>
            <div class="cjx-modal-body">
              <div class="cjx-batch-input-row">
                <input type="number" step="0.01" v-model.number="quickPriceValue" class="cjx-input" />
                <button class="cjx-btn cjx-btn-quick" @click="quickPriceValue = quickPriceOneClick">⚡¥{{ quickPriceOneClick.toFixed(2) }}</button>
              </div>
            </div>
            <div class="cjx-modal-footer">
              <button class="cjx-btn cjx-btn-primary" @click="applySinglePrice">确认</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ======= Tab 2: 上架 CDKey ======= -->
      <div class="cjx-section" v-if="activeTab === 'new'">
        <div class="cjx-form-card">
          <h3 class="cjx-form-title">上架新 CDKey</h3>

          <!-- 重新上架模式提示条 -->
          <div class="cjx-relist-banner" v-if="relistTarget">
            <span class="cjx-relist-icon">📋</span>
            <span>当前为 <b>重新上架模式</b>，CDKey 已锁定不可修改，您可以调整价格后点击确认。</span>
            <button class="cjx-btn-icon" @click="cancelRelist" title="取消">✕</button>
          </div>

          <div class="cjx-form-item cjx-form-game-search">
            <label>搜索游戏 *</label>
            <div class="cjx-autocomplete">
              <input
                type="text"
                v-model="gameSearchQuery"
                class="cjx-input"
                placeholder="输入游戏名称，例如：东方奇缘记"
                @focus="showGameDropdown = true"
                @blur="setTimeout(() => { showGameDropdown = false }, 200)"
                @input="selectedGame = null"
                :disabled="!!relistTarget"
              />
              <!-- 搜索结果下拉 -->
              <div class="cjx-autocomplete-dropdown" v-if="showGameDropdown && gameSearchQuery.trim()">
                <div
                  v-for="g in searchResultGames"
                  :key="g.id"
                  class="cjx-autocomplete-item"
                  :class="{ active: selectedGame && selectedGame.id === g.id }"
                  @mousedown="selectGame(g)"
                >
                  <span class="cjx-suggest-name">{{ g.name }}</span>
                  <span class="cjx-suggest-price">官方价 ¥{{ (g.current_price || g.price || 0).toFixed(2) }}</span>
                </div>
                <div v-if="searchResultGames.length === 0" class="cjx-autocomplete-empty">没找到相关游戏</div>
              </div>
            </div>
          </div>

          <!-- 已选游戏信息 + 定价提示（只有选中才显示）-->
          <div class="cjx-game-selected" v-if="selectedGame">
            <div class="cjx-game-selected-info">
              <img :src="getImageUrl(selectedGame.image_url || selectedGame.image)" class="cjx-game-thumb" />
              <div class="cjx-game-selected-detail">
                <div class="cjx-game-selected-name">{{ selectedGame.name }}</div>
                <div class="cjx-game-meta">
                  <span>官方原价 <b class="cjx-original-price">¥{{ Number(selectedGame.original_price || 0).toFixed(2) }}</b></span>
                  <span class="cjx-min-price-row" v-if="currentMinPrice != null">
                    当前最低在售 <b class="cjx-min-price">¥{{ currentMinPrice.toFixed(2) }}</b>
                  </span>
                  <span v-else class="cjx-no-min">暂无卖家在售</span>
                </div>
              </div>
            </div>
            <button
              class="cjx-btn cjx-btn-quick"
              @click="quickPrice"
              :disabled="currentMinPrice == null || currentMinPrice <= 0"
            >
              ⚡ 一键定价 ¥{{ listingQuickPriceValue.toFixed(2) }}
            </button>
          </div>

          <div class="cjx-form-grid">
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
              <input
                type="number" step="0.01"
                v-model.number="newForm.price"
                class="cjx-input"
                placeholder="299.00"
                :disabled="!selectedGame"
              />
            </div>
            <div class="cjx-form-item">
              <label>原价 (¥) 【只读，官方原价】</label>
              <input
                type="number" step="0.01"
                v-model.number="newForm.original_price"
                class="cjx-input"
                disabled
                placeholder="自动填充"
              />
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

          <!-- CDKey 生成器 + 输入 -->
          <div class="cjx-form-item">
            <label>CDKey 列表 *（每行一个，推荐格式 ABCD-1234-EFGH-5678）</label>
            <div class="cjx-cdkey-tools">
              <button class="cjx-btn cjx-btn-generate" @click="generateCdkey" :disabled="!selectedGame || !!relistTarget">
                🎲 生成一个 CDKey
              </button>
              <span v-if="generatedCdkey" class="cjx-generated">
                生成结果: <code>{{ generatedCdkey }}</code>
                <span class="cjx-dup-check" v-if="dupCheckResult === null">查重中...</span>
                <span v-else-if="dupCheckResult === true" class="cjx-dup-ok">✓ 未重复</span>
                <span v-else class="cjx-dup-bad">✗ 已存在，请重新生成</span>
                <button class="cjx-btn cjx-btn-copy" @click="appendGeneratedCdkey" :disabled="dupCheckResult === false">插入到下方 ↓</button>
              </span>
            </div>
            <textarea
              v-model="newForm.cdkeys"
              class="cjx-textarea"
              rows="6"
              placeholder="ABCD-1234-EFGH-5678&#10;IJKL-9012-MNOP-3456"
              @input="validateCdkeys"
              :disabled="!!relistTarget"
            ></textarea>
            <!-- CDKey 校验提示 -->
            <div class="cjx-cdkey-validation" v-if="cdkeyValidation.issues.length > 0">
              <div v-for="(issue, i) in cdkeyValidation.issues" :key="i" :class="'cjx-issue-' + issue.type">
                {{ issue.msg }}
              </div>
            </div>
            <div class="cjx-cdkey-stats" v-if="cdkeyValidation.total > 0">
              共 {{ cdkeyValidation.total }} 个，合法 {{ cdkeyValidation.valid }} 个，重复 {{ cdkeyValidation.dup }} 个
            </div>
          </div>

          <div class="cjx-form-actions">
            <span class="cjx-hint">将批量上架 {{ cdkeyValidation.valid }} 个有效 CDKey</span>
            <button
              class="cjx-btn cjx-btn-primary"
              @click="submitListings"
              :disabled="submitting || !selectedGame || cdkeyValidation.valid === 0"
            >
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
  // 排序：在售新 → 在售旧 → 已售新 → 已售旧 → 待激活新 → 待激活旧
  const statusRank = (s: string) => {
    if (s === 'available') return 0
    if (s === 'sold') return 1
    if (s === 'pending_activation') return 2
    return 3
  }
  return r.sort((a, b) => {
    const ra = statusRank(a.status)
    const rb = statusRank(b.status)
    if (ra !== rb) return ra - rb
    return new Date(b.created_at).getTime() - new Date(a.created_at).getTime()
  })
})

const stats = computed(() => {
  const available = sellerListings.value.filter(l => l.status === 'available').length
  const sold = sellerListings.value.filter(l => l.status === 'sold').length
  const pending = sellerListings.value.filter(l => l.status === 'pending_activation').length
  const totalSold = sellerListings.value
    .filter(l => l.status === 'sold')
    .reduce((sum, l) => sum + Number(l.price || 0), 0)
  return { available, sold, pending, totalSold }
})

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : '-'
const getStatusText = (s: string) => ({ completed: '已完成', cancelled: '已取消', pending: '待处理' }[s] || s)

// ======== 状态文字/样式 ========
const listingStatusText = (s: string) => ({
  available: '在售',
  sold: '已售',
  pending_activation: '待激活'
}[s] || s)
const listingStatusBadgeClass = (s: string) => ({
  available: 'badge-green',
  sold: 'badge-gray',
  pending_activation: 'badge-pending'
}[s] || 'badge-gray')

// ======== 选中 CDK（批量改价用）=======
const selectedIds = ref<Set<string>>(new Set())
/** v-model 绑定时，数组方便操作；用 computed 中转 */
const selectedIdsArr = computed({
  get: () => Array.from(selectedIds.value),
  set: (v: string[]) => { selectedIds.value = new Set(v) }
})
const isAllSelected = computed(() => {
  const rows = filteredListings.value.filter(l => l.status !== 'sold')
  return rows.length > 0 && rows.every(l => selectedIds.value.has(l.id))
})
const hasSelectableRows = computed(() => filteredListings.value.some(l => l.status !== 'sold'))
const toggleSelectAll = () => {
  const rows = filteredListings.value.filter(l => l.status !== 'sold')
  if (isAllSelected.value) {
    rows.forEach(l => selectedIds.value.delete(l.id))
  } else {
    rows.forEach(l => selectedIds.value.add(l.id))
  }
}
const clearSelection = () => { selectedIds.value = new Set() }

// ======== 批量改价弹窗 ========
const showBatchPriceModal = ref(false)
const batchPriceGroups = ref<any[]>([])

const openBatchPrice = async () => {
  if (selectedIds.value.size === 0) { showToast('请先选择 CDKey'); return }
  const ids = Array.from(selectedIds.value)
  const selListings = sellerListings.value.filter(l => ids.includes(l.id))

  // 按 game_id 分组
  const byGame: Record<string, any> = {}
  for (const l of selListings) {
    const gid = String(l.game_id)
    if (!byGame[gid]) {
      // 查该游戏的 current_price + original_price
      const g = games.value.find(x => String(x.id) === gid)
      const op = Number(g?.original_price || l.original_price || 0)
      // 查当前在售最低价（排除自己选的那些）
      const minExcluding = sellerListings.value
        .filter(x => x.status === 'available' && String(x.game_id) === gid && !ids.includes(x.id))
      const groupedMin = selectedListings.value
        .filter((x: any) => String(x.game_id) === gid)
        .reduce((m: number, x: any) => Math.min(m, Number(x.price || Infinity)), Infinity)
      const currentMin = Math.min(
        Number(g?.price || Infinity),
        ...minExcluding.map(x => Number(x.price || Infinity)),
        groupedMin
      )
      byGame[gid] = {
        game_id: l.game_id,
        game_name: l.game_name,
        image: l.game_image,
        original_price: op,
        current_min: currentMin === Infinity || currentMin <= 0 ? null : currentMin,
        price: Number(l.price || 0), // 预填第一个的价格
        count: 0
      }
    }
    byGame[gid].count++
  }
  batchPriceGroups.value = Object.values(byGame)
  showBatchPriceModal.value = true
}

const batchOneClickPrice = (g: any) => {
  // 一键定价 = max(0.01, 当前最低价 - 0.01)
  if (g.current_min == null) return 0
  return Math.max(0.01, +(g.current_min - 0.01).toFixed(2))
}

const closeBatchPrice = () => { showBatchPriceModal.value = false }

const applyBatchPrice = async () => {
  const ids = Array.from(selectedIds.value)
  // 构建 id → 新价格 map
  const idPriceMap: Record<string, number> = {}
  const gidPriceMap: Record<string, number> = {}
  for (const g of batchPriceGroups.value) {
    if (g.price > 0) gidPriceMap[String(g.game_id)] = Number(g.price)
  }
  for (const l of sellerListings.value) {
    if (ids.includes(l.id) && gidPriceMap[String(l.game_id)]) {
      idPriceMap[l.id] = gidPriceMap[String(l.game_id)]
    }
  }
  if (Object.keys(idPriceMap).length === 0) { showToast('请输入有效价格'); return }
  try {
    const r = await listingAPI.batchUpdatePrice(idPriceMap)
    if (r.error) { showToast('修改失败: ' + r.error) }
    else {
      showToast(`✅ 已修改 ${Object.keys(idPriceMap).length} 个 CDKey 的价格`)
      closeBatchPrice()
      clearSelection()
      loadAll()
    }
  } catch (e: any) { showToast('修改失败: ' + e.message) }
}

// ======== 单个改价弹窗 ========
const showQuickPriceModal = ref(false)
const quickPriceTarget = ref<any>(null)
const quickPriceValue = ref<number>(0)
const quickPriceOneClick = ref<number>(0)

const openQuickPrice = async (l: any) => {
  quickPriceTarget.value = l
  quickPriceValue.value = Number(l.price || 0)
  // 查当前最低价
  try {
    const g = games.value.find(x => String(x.id) === String(l.game_id))
    const r = await listingAPI.getGrouped({ game_id: l.game_id })
    const rows = r.data || []
    const min = rows.reduce((m: number, x: any) => Math.min(m, Number(x.price || Infinity)), Infinity)
    const official = Number(g?.price || Infinity)
    const cur = Math.min(official, min)
    quickPriceOneClick.value = cur === Infinity || cur <= 0 ? 0 : Math.max(0.01, +(cur - 0.01).toFixed(2))
  } catch {
    quickPriceOneClick.value = 0
  }
  showQuickPriceModal.value = true
}

const applySinglePrice = async () => {
  if (!quickPriceTarget.value || quickPriceValue.value <= 0) { showToast('请输入有效价格'); return }
  try {
    const r = await listingAPI.updatePrice(quickPriceTarget.value.id, quickPriceValue.value)
    if (r.error) showToast('修改失败: ' + r.error)
    else {
      showToast('✅ 已修改价格')
      showQuickPriceModal.value = false
      loadAll()
    }
  } catch (e: any) { showToast('修改失败: ' + e.message) }
}

// ======== 下架（available → pending_activation）/ 重新上架 / 自己激活 ========
/** available 下架 → 变成 pending_activation（不丢 CDK / 不丢 price / 可重新上架）*/
const softDeleteListing = async (l: any) => {
  if (!confirm(`确定下架 CDKey (${l.cdkey?.substring(0, 8)}...)？下架后将进入"待激活"状态，随时可重新上架。`)) return
  try {
    const r = await listingAPI.softDelete(l.id)
    if (r.error) showToast('下架失败: ' + r.error)
    else { showToast('✅ 已下架（进入待激活状态）'); clearSelection(); loadAll() }
  } catch (e: any) { showToast('下架失败: ' + e.message) }
}

/** 待激活 → 重新上架（跳转上架 Tab + 预填表单 + 锁 CDKey + 只能操作一个）*/
const relistTarget = ref<any>(null) // 非 null 时表示"重新上架模式"
const jumpToRelist = async (l: any) => {
  if (relistTarget.value) {
    showToast('当前只能同时操作一个重新上架，请先完成上一个');
    return
  }
  relistTarget.value = l
  // 预填表单（复用 selectGame 的逻辑）
  const g = games.value.find(x => String(x.id) === String(l.game_id))
  if (g) {
    await selectGame(g)
  } else {
    // games 里没找到（比如数据问题），手动填
    selectedGame.value = { id: l.game_id, name: l.game_name, image_url: l.game_image, original_price: l.original_price, price: l.price }
    newForm.value.game_id = l.game_id
    newForm.value.game_name = l.game_name
    newForm.value.game_image = l.game_image
    newForm.value.original_price = Number(l.original_price || 0)
  }
  newForm.value.version = l.version || '标准版'
  newForm.value.price = Number(l.price || 0)
  newForm.value.region = l.region || '国区'
  // CDK 自动填入（锁死不可改）
  newForm.value.cdkeys = l.cdkey
  validateCdkeys()
  activeTab.value = 'new'
  showToast('📋 已自动预填，请确认价格后点击"确认上架"提交')
}

/** 取消重新上架模式 — 回到正常上架 */
const cancelRelist = () => {
  if (!confirm('取消重新上架？已填的表单会清空。')) return
  relistTarget.value = null
  newForm.value = { game_id: '', game_name: '', game_image: '', version: '标准版', price: null, original_price: 0, region: '国区', cdkeys: '' }
  selectedGame.value = null
  selectedListings.value = []
  gameSearchQuery.value = ''
  generatedCdkey.value = ''
  dupCheckResult.value = null
  cdkeyValidation.value = { total: 0, valid: 0, dup: 0, issues: [] }
}

/** 正常的待激活 → 重新上架接口调用（模板不再直接调用，但保留用于 submitListings 里）*/
const relistPending = async (l: any) => {
  try {
    const r = await listingAPI.relistPending(l.id)
    if (r.error) showToast('上架失败: ' + r.error)
    else { showToast('✅ 已重新上架'); loadAll() }
  } catch (e: any) { showToast('上架失败: ' + e.message) }
}

/** 待激活 → 自己激活（加入 user_games）*/
const selfActivate = async (l: any) => {
  const user = authAPI.getCurrentUser()
  if (!user) { showToast('请先登录'); return }
  if (!confirm(`确认将该 CDKey (${l.cdkey?.substring(0, 8)}...) 激活到你自己的账号？`)) return
  try {
    const r = await listingAPI.selfActivate(l.id)
    if (r.error) showToast('激活失败: ' + r.error)
    else { showToast('✅ 已激活，CDKey 已加入你的游戏库'); loadAll() }
  } catch (e: any) { showToast('激活失败: ' + e.message) }
}


// ======== 游戏搜索 autocomplete ========
const gameSearchQuery = ref('')
const showGameDropdown = ref(false)
const selectedGame = ref<any>(null)
const selectedListings = ref<any[]>([]) // 选中游戏后，加载 available-grouped 拿最低价

/** 搜索结果（模糊匹配 + 重合度排序，最多 10 条）*/
const searchResultGames = computed(() => {
  const q = gameSearchQuery.value.trim().toLowerCase()
  if (!q) return []
  const scored = games.value
    .filter(g => g.name?.toLowerCase().includes(q))
    .map(g => {
      const name = g.name.toLowerCase()
      const idx = name.indexOf(q)
      // 分数：位置越靠前 + 被包含 → 分数越高；取负数升序
      let score = idx * 10 // 位置分
      if (name === q) score -= 1000 // 完全匹配优先级最高
      else if (name.startsWith(q)) score -= 500 // 开头匹配
      else if (name.includes(q)) score -= 100
      return { g, score, len: name.length }
    })
    .sort((a, b) => a.score - b.score || a.len - b.len)
  return scored.slice(0, 10).map(x => x.g)
})

/** 选中一个游戏 —— 填充表单 + 查最低价 */
const selectGame = async (g: any) => {
  selectedGame.value = g
  gameSearchQuery.value = g.name
  newForm.value.game_id = g.id
  newForm.value.game_name = g.name
  newForm.value.game_image = g.image_url || g.image || ''
  newForm.value.original_price = Number(g.original_price || 0)
  // 查当前最低价（available-grouped 取该游戏所有 seller 的最低）
  try {
    const r = await listingAPI.getGrouped({ game_id: g.id, game_name: g.name })
    selectedListings.value = r.data || []
  } catch {
    selectedListings.value = []
  }
  showGameDropdown.value = false
}

/** 当前最低在售价 */
const currentMinPrice = computed<number | null>(() => {
  if (!selectedGame.value) return null
  const rows = selectedListings.value.filter((r: any) => r.status === 'available' || !r.status)
  // 同时考虑 games 表的 current_price（官方价）
  const official = Number(selectedGame.value.current_price || selectedGame.value.price || 0)
  const listingMin = rows.length > 0 ? Math.min(...rows.map((r: any) => Number(r.price || Infinity))) : Infinity
  const min = Math.min(official || Infinity, listingMin)
  return min === Infinity || min <= 0 ? null : min
})

/** 一键定价值（最低 - 0.01）*/
const listingQuickPriceValue = computed(() => {
  if (currentMinPrice.value == null) return 0
  return Math.max(0.01, +(currentMinPrice.value - 0.01).toFixed(2))
})
const quickPrice = () => {
  if (currentMinPrice.value == null) return
  newForm.value.price = listingQuickPriceValue.value
  showToast(`⚡ 已一键定价 ¥${listingQuickPriceValue.value.toFixed(2)}`)
}

const getImageUrl = (src?: string) => {
  if (!src) return ''
  if (src.startsWith('http')) return src
  return '/picture/' + src
}

// ======== CDKey 生成 + 查重 ========
const generatedCdkey = ref('')
const dupCheckResult = ref<boolean | null>(null)

const generateCdkey = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  const rand4 = () => Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
  const k = `${rand4()}-${rand4()}-${rand4()}-${rand4()}`
  generatedCdkey.value = k
  dupCheckResult.value = null
  // 异步查重
  listingAPI.checkCdkey(k).then((r: any) => {
    dupCheckResult.value = !r.exists
  }).catch(() => {
    dupCheckResult.value = null // 查不到也允许，让后端报错去
  })
}

const appendGeneratedCdkey = () => {
  if (!generatedCdkey.value) return
  const cur = newForm.value.cdkeys.trim()
  newForm.value.cdkeys = cur ? cur + '\n' + generatedCdkey.value : generatedCdkey.value
  generatedCdkey.value = ''
  dupCheckResult.value = null
  validateCdkeys()
}

// ======== CDKey 格式校验 ========
/** 推荐格式: XXXX-XXXX-XXXX-XXXX（4 段 × 4 字符，大写字母/数字），也接受不加横杠的 */
const CDK_PATTERN = /^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$/
const cdkeyValidation = ref<{ total: number; valid: number; dup: number; issues: { type: string; msg: string }[] }>(
  { total: 0, valid: 0, dup: 0, issues: [] }
)

const validateCdkeys = () => {
  const lines = newForm.value.cdkeys.split('\n').map(x => x.trim()).filter(x => x)
  const seen = new Set<string>()
  const issues: { type: string; msg: string }[] = []
  let valid = 0
  let dup = 0
  lines.forEach((k, i) => {
    const ln = i + 1
    const normalized = k.toUpperCase()
    // 规范化：允许用户没写横杠的自动补上？不，就校验推荐格式
    if (!CDK_PATTERN.test(normalized)) {
      issues.push({ type: 'warn', msg: `第 ${ln} 行 "${k}" 格式异常，推荐 XXXX-XXXX-XXXX-XXXX（4×4 字母/数字）` })
      return
    }
    if (seen.has(normalized)) {
      dup++
      issues.push({ type: 'dup', msg: `第 ${ln} 行与上方重复: ${normalized}` })
      return
    }
    seen.add(normalized)
    valid++
  })
  cdkeyValidation.value = { total: lines.length, valid, dup, issues }
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
  if (!selectedGame.value || !newForm.value.game_id) { showToast('请先搜索并选择游戏'); return }
  if (!newForm.value.price || newForm.value.price <= 0) { showToast('请输入有效售价'); return }

  const lines = newForm.value.cdkeys.split('\n').map(x => x.trim().toUpperCase()).filter(x => CDK_PATTERN.test(x))
  if (lines.length === 0) { showToast('请至少输入一个格式正确的 CDKey'); return }

  submitting.value = true
  try {
    // ====== 重新上架模式（relistTarget 非 null）======
    if (relistTarget.value) {
      // 1. 先 relistPending（pending_activation → available，清 order_id）
      const r = await listingAPI.relistPending(relistTarget.value.id)
      if (r.error) { showToast('重新上架失败: ' + r.error); return }
      // 2. 再改价格（此时已 available，updatePrice 可用）
      if (Number(newForm.value.price) !== Number(relistTarget.value.price)) {
        const up = await listingAPI.updatePrice(relistTarget.value.id, Number(newForm.value.price))
        if (up.error) { showToast('修改价格失败: ' + up.error); return }
      }
      showToast(`✅ CDKey ${relistTarget.value.cdkey?.substring(0, 8)}... 已重新上架`)
    } else {
      // ====== 普通批量上架 ======
      const items = lines.map(cdkey => ({
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
      if (result.error) { showToast('上架失败: ' + result.error); return }
      showToast(`✅ 成功上架 ${lines.length} 个 CDKey`)
    }
    // 统一清场
    newForm.value = { game_id: '', game_name: '', game_image: '', version: '标准版', price: null, original_price: 0, region: '国区', cdkeys: '' }
    selectedGame.value = null
    selectedListings.value = []
    gameSearchQuery.value = ''
    generatedCdkey.value = ''
    dupCheckResult.value = null
    cdkeyValidation.value = { total: 0, valid: 0, dup: 0, issues: [] }
    relistTarget.value = null // 清重新上架模式
    activeTab.value = 'listings'
    loadAll()
  } catch (e: any) {
    showToast('上架失败: ' + e.message)
  } finally {
    submitting.value = false
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
/* 表格行里的操作按钮：统一宽度 + 文字居中，"⚡定价"和"下架"等长 */
.cjx-table .cjx-btn-small { width: 68px; text-align: center; padding: 6px 0; margin-right: 6px; }
/* small + 彩色按钮时，让 small 的尺寸优先生效（避免 cjx-btn-quick/generate 的大 padding 覆盖）*/
.cjx-btn-small.cjx-btn-quick,
.cjx-btn-small.cjx-btn-generate { padding: 6px 12px; font-size: 12px; font-weight: 500; }
.cjx-table .cjx-btn-small.cjx-btn-quick,
.cjx-table .cjx-btn-small.cjx-btn-generate { padding: 6px 0; font-weight: 500; }
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

/* ======== autocomplete ======== */
.cjx-autocomplete { position: relative; }
.cjx-autocomplete-dropdown {
  position: absolute; top: 100%; left: 0; right: 0;
  background: #fff; border: 1px solid #ddd; border-top: none;
  border-radius: 0 0 6px 6px; max-height: 320px; overflow-y: auto;
  z-index: 1000; box-shadow: 0 4px 12px rgba(0,0,0,.1);
}
.cjx-autocomplete-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #f0f0f0;
  transition: background .15s;
}
.cjx-autocomplete-item:hover, .cjx-autocomplete-item.active { background: #f0f7ff; }
.cjx-suggest-name { font-weight: 500; color: #333; }
.cjx-suggest-price { color: #999; font-size: 12px; }
.cjx-autocomplete-empty { padding: 16px; text-align: center; color: #999; font-size: 13px; }

/* ======== 已选游戏卡片 ======== */
.cjx-game-selected {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f9ff 100%);
  border: 1px solid #cde4f7; border-radius: 8px; padding: 14px 16px;
  margin-bottom: 15px; gap: 16px;
}
.cjx-game-selected-info { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 0; }
.cjx-game-thumb { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; background: #eee; }
.cjx-game-selected-detail { min-width: 0; }
.cjx-game-selected-name { font-weight: 600; color: #2c3e50; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cjx-game-meta { margin-top: 4px; font-size: 12px; color: #666; display: flex; gap: 16px; flex-wrap: wrap; }
.cjx-game-meta b { font-weight: 600; margin-left: 2px; }
.cjx-original-price { color: #999; text-decoration: line-through; }
.cjx-min-price { color: #e67e22; }
.cjx-no-min { color: #27ae60; }

/* 一键定价按钮 */
.cjx-btn-quick {
  background: linear-gradient(135deg, #f39c12, #e67e22);
  color: #fff; padding: 10px 16px; font-weight: 600;
  white-space: nowrap;
}
.cjx-btn-quick:hover:not(:disabled) { background: linear-gradient(135deg, #e67e22, #d35400); }
.cjx-btn-quick:disabled { background: #bdc3c7; cursor: not-allowed; }

/* ======== CDKey 工具 ======== */
.cjx-cdkey-tools { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin-bottom: 10px; }
.cjx-btn-generate {
  background: linear-gradient(135deg, #9b59b6, #8e44ad); color: #fff;
  padding: 8px 14px; font-weight: 500;
}
.cjx-btn-generate:hover:not(:disabled) { opacity: .9; }
.cjx-btn-generate:disabled { background: #bdc3c7; cursor: not-allowed; }
.cjx-generated { display: inline-flex; align-items: center; gap: 8px; font-size: 13px; color: #555; }
.cjx-generated code {
  background: #2c3e50; color: #f1c40f; padding: 3px 8px; border-radius: 4px;
  font-family: 'Courier New', monospace; font-size: 12px;
}
.cjx-dup-check { color: #f39c12; font-size: 12px; }
.cjx-dup-ok { color: #27ae60; font-size: 12px; }
.cjx-dup-bad { color: #e74c3c; font-size: 12px; font-weight: 600; }
.cjx-btn-copy {
  background: #16a085; color: #fff; font-size: 12px; padding: 5px 10px;
}
.cjx-btn-copy:disabled { background: #bdc3c7; cursor: not-allowed; }

/* ======== CDKey 校验 ======== */
.cjx-cdkey-validation { margin-top: 8px; display: flex; flex-direction: column; gap: 4px; }
.cjx-cdkey-validation > div { font-size: 12px; padding: 4px 8px; border-radius: 4px; }
.cjx-issue-warn { background: #fef9e7; color: #7d6608; }
.cjx-issue-dup { background: #fdedec; color: #922b21; }
.cjx-cdkey-stats { margin-top: 8px; font-size: 12px; color: #7f8c8d; }

/* disabled 输入框颜色更明显 */
.cjx-input:disabled, .cjx-select:disabled { background: #f5f5f5; color: #999; cursor: not-allowed; }

/* ======== 批量操作条 ======== */
.cjx-batch-bar {
  display: flex; align-items: center; gap: 10px;
  background: linear-gradient(135deg, #fef9e7, #fef5d7);
  border: 1px solid #f39c12; border-radius: 8px; padding: 10px 14px;
  margin-bottom: 15px;
}

/* ======== 重新上架模式提示条 ======== */
.cjx-relist-banner {
  display: flex; align-items: center; gap: 10px;
  background: linear-gradient(135deg, #eaf7ed, #dff2e5);
  border: 1px solid #27ae60; border-radius: 8px; padding: 10px 14px;
  margin-bottom: 18px; color: #1e7e43; font-size: 13px;
}
.cjx-relist-banner b { color: #155724; }
.cjx-relist-icon { font-size: 16px; }
.cjx-batch-info { font-weight: 600; color: #b7791f; }
.cjx-btn-danger-outline { background: transparent; color: #e74c3c; border: 1px solid #e74c3c; }
.cjx-btn-danger-outline:hover { background: #e74c3c; color: #fff; }

/* checkbox 样式 */
input[type="checkbox"] { width: 16px; height: 16px; cursor: pointer; vertical-align: middle; }
input[type="checkbox"]:disabled { cursor: not-allowed; opacity: .4; }

/* pending badge + hint */
.badge-pending { background: #fff3cd; color: #856404; border: 1px solid #ffeeba; }
.cjx-pending-order-hint { margin-left: 6px; font-size: 11px; color: #999; font-family: 'Courier New', monospace; }

/* ======== Modal ======== */
.cjx-modal-mask {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.5); z-index: 2000;
  display: flex; align-items: center; justify-content: center;
}
.cjx-modal {
  background: #fff; border-radius: 12px;
  width: 600px; max-width: 95vw; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 50px rgba(0,0,0,.2);
}
.cjx-modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 20px; border-bottom: 1px solid #eee;
}
.cjx-modal-header h3 { margin: 0; font-size: 16px; color: #2c3e50; }
.cjx-modal-body { padding: 18px 20px; }
.cjx-modal-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 20px; border-top: 1px solid #eee;
}

/* ======== Batch price groups ======== */
.cjx-batch-group {
  background: #fafbfc; border: 1px solid #eef0f2;
  border-radius: 8px; padding: 14px; margin-bottom: 12px;
}
.cjx-batch-group-header { margin-bottom: 10px; }
.cjx-batch-group-title { display: flex; align-items: center; gap: 10px; font-weight: 600; color: #2c3e50; }
.cjx-batch-count { color: #7f8c8d; font-size: 12px; font-weight: 400; }
.cjx-batch-group-meta { font-size: 12px; color: #999; margin-top: 4px; padding-left: 58px; }
.cjx-batch-input-row { display: flex; gap: 10px; align-items: center; }
.cjx-batch-input-row .cjx-input { flex: 1; }
</style>

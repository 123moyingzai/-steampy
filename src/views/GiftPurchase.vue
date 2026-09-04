﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <Layout>
    <div class="cjx-gift-page">

      <!-- ========== 详情态 ========== -->
      <template v-if="viewMode === 'detail' && selectedGame">
        <div class="cjx-detail-top">
          <button class="cjx-back-btn" @click="viewMode = 'list'">← 返回</button>
        </div>

        <div class="cjx-detail-header">
          <img :src="getImageUrl(selectedGame.image)" class="cjx-detail-cover" />
          <div class="cjx-detail-info">
            <h1 class="cjx-detail-name">{{ selectedGame.name }}</h1>
            <p class="cjx-detail-subname">{{ selectedGame.name }}</p>
            <div class="cjx-detail-meta">
              <span class="cjx-detail-rating">🎮 游戏评分: {{ selectedGame.rating || '9.5' }}</span>
            </div>
          </div>
          <div class="cjx-detail-price-box">
            <div class="cjx-detail-ref">参考价:</div>
            <div class="cjx-detail-ref-price">¥{{ (selectedGame.price || 0).toFixed(2) }}</div>
            <div class="cjx-detail-origin-row">
              <span class="cjx-detail-origin" v-if="selectedGame.original_price">¥{{ selectedGame.original_price }}</span>
              <span class="cjx-detail-discount" v-if="selectedGame.discount">{{ selectedGame.discount }}</span>
            </div>
          </div>
        </div>

        <hr class="cjx-detail-divider" />

        <div class="cjx-detail-sellers">
          <h2 class="cjx-detail-sellers-title">为您寻找合适卖家</h2>
          <table class="cjx-sellers-table">
            <thead>
              <tr>
                <th>流畅度</th>
                <th>头像</th>
                <th>Steam Id64</th>
                <th>发货类型</th>
                <th>Steam 账户名</th>
                <th>账户区服</th>
                <th class="sortable" @click="sortSellers('price')">出售金额 ↕</th>
                <th class="sortable" @click="sortSellers('minBuy')">最低金额 ↕</th>
                <th class="sortable" @click="sortSellers('discount')">折扣 ↕</th>
                <th class="sortable" @click="sortSellers('deals')">成交 ↕</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(s, i) in sortedSellers" :key="i">
                <td class="cjx-fluid-cell">
                  <span class="cjx-fluid-label">{{ s.fluency }}</span>
                  <span class="cjx-fluid-dot" :class="s.fluencyClass"></span>
                </td>
                <td><div class="cjx-avatar" :style="{ background: s.avatarBg }">{{ s.avatarText }}</div></td>
                <td class="cjx-mono">{{ s.steamId64 }}</td>
                <td>
                  <span :class="['cjx-deliver-type', s.deliverType === '自动发货' ? 'auto' : 'manual']">
                    {{ s.deliverType }}
                  </span>
                </td>
                <td class="cjx-account-name">{{ s.accountName }}</td>
                <td>{{ s.region }}</td>
                <td class="cjx-price-cell">¥{{ s.salePrice.toFixed(2) }}</td>
                <td class="cjx-price-cell">¥{{ s.quota.toFixed(2) }}</td>
                <td class="cjx-discount-cell">{{ s.discountPct }}%</td>
                <td class="cjx-deals-cell">{{ s.deals }}</td>
                <td><button class="cjx-buy-seller-btn" @click="showBuyDialog(s)">购买</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <!-- ========== 列表态 ========== -->
      <template v-else>
        <div class="cjx-list-header">
          <h2 class="cjx-list-title">平台热门游戏</h2>
          <div class="cjx-list-actions">
            <input
              v-model="searchQuery"
              placeholder="搜索游戏名称"
              class="cjx-list-search"
              @input="page = 1"
            />
            <button class="cjx-help-btn">购买教程演示</button>
          </div>
        </div>

        <div class="cjx-hot-grid">
          <div
            class="cjx-hot-card"
            v-for="item in displayGames"
            :key="item.uid"
            @click="goDetail(item)"
          >
            <div class="cjx-hot-img-wrap">
              <img :src="getImageUrl(item.image)" class="cjx-hot-img" />
              <span v-if="item.discount" class="cjx-hot-discount">{{ item.discount }}</span>
              <span v-if="item.is_presale" class="cjx-hot-presale">预售</span>
            </div>
            <div class="cjx-hot-info">
              <div class="cjx-hot-name" :title="item.name">{{ item.name }}</div>
              <div class="cjx-hot-price-row">
                <span class="cjx-hot-original" v-if="item.original_price">¥{{ item.original_price }}</span>
                <span class="cjx-hot-current">¥{{ item.price.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="cjx-pagination" v-if="totalPages > 1">
          <button class="cjx-page-btn" :disabled="page === 1" @click="page--">‹</button>
          <button
            v-for="p in visiblePages"
            :key="p"
            class="cjx-page-btn"
            :class="{ active: p === page }"
            @click="page = p"
          >{{ p }}</button>
          <span class="cjx-page-ellipsis" v-if="page < totalPages - 2">...</span>
          <button class="cjx-page-btn" :disabled="page === totalPages" @click="page++">›</button>
          <span class="cjx-page-total">共 {{ totalGames }} 款</span>
        </div>
      </template>

      <!-- ========== 代购购买弹窗 ========== -->
      <div v-if="buyDialogVisible" class="cjx-dialog-mask" @click.self="buyDialogVisible = false">
        <div class="cjx-dialog">
          <div class="cjx-dialog-header">
            <h3>确认代购 · {{ selectedGame?.name }}</h3>
            <button class="cjx-dialog-close" @click="buyDialogVisible = false">✕</button>
          </div>
          <div class="cjx-dialog-body" v-if="selectedBuyer">
            <div class="cjx-dialog-row"><span>出售方</span><b>{{ selectedBuyer.accountName }}</b></div>
            <div class="cjx-dialog-row"><span>发货类型</span><b>{{ selectedBuyer.deliverType }}</b></div>
            <div class="cjx-dialog-row"><span>代购金额</span><b class="cjx-red">¥{{ selectedBuyer.salePrice.toFixed(2) }}</b></div>
            <div class="cjx-dialog-row"><span>可用额度</span><b>¥{{ selectedBuyer.quota.toFixed(2) }}</b></div>

            <hr class="cjx-dialog-hr" />
            <div class="cjx-dialog-row">
              <label class="cjx-check-row">
                <input type="checkbox" v-model="balanceCheck" :disabled="buyerBalance <= 0" />
                使用余额抵扣 <small>(¥{{ buyerBalance.toFixed(2) }})</small>
              </label>
            </div>
            <div v-if="balanceCheck" class="cjx-split-pay">
              <div class="cjx-split-line">余额支付 <b>¥{{ balanceAmount.toFixed(2) }}</b></div>
              <div class="cjx-split-line">支付宝支付 <b>¥{{ alipayAmount.toFixed(2) }}</b></div>
            </div>
          </div>
          <div class="cjx-dialog-footer">
            <button class="cjx-btn cjx-btn-cancel" @click="buyDialogVisible = false">取消</button>
            <button class="cjx-btn cjx-btn-confirm" @click="confirmBuy">✅ 确认代购</button>
          </div>
        </div>
      </div>

    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '../components/Layout.vue'
import { fetchAllGames, listingAPI, orderAPI, authAPI, walletAPI } from '../config/supabase-local.ts'

const router = useRouter()

// ====== 状态 ======
const viewMode = ref<'list' | 'detail'>('list')
const selectedGame = ref<any>(null)
const games = ref<any[]>([])
const listings = ref<any[]>([])
const page = ref(1)
const pageSize = 70
const sortKey = ref<string>('price')
const searchQuery = ref('')

// ====== 工具 ======
const getImageUrl = (path: string) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  if (path.includes('picture/')) {
    const fn = path.split('picture/')[1]
    if (fn) return `/picture/${fn}`
  }
  return path.startsWith('/') ? path : `/${path}`
}

// ====== Catalog（复用 CDKeyMarket 的逻辑）======
const allGames = computed(() => {
  const map = new Map<string, any>()
  for (const g of games.value) {
    const p = Number(g.current_price ?? g.price ?? 0)
    const op = Number(g.original_price ?? 0)
    const discount = op > 0 && p > 0 ? '-' + Math.round((1 - p / op) * 100) + '%' : ''
    map.set(String(g.id), {
      uid: 'g-' + g.id,
      game_id: g.id,
      name: g.name,
      image: g.image_url || g.image,
      price: p,
      original_price: op || null,
      discount,
      is_presale: !!g.is_presale,
      rating: g.rating || null
    })
  }
  // listings 聚合
  const sellerGroups: Record<string, { minPrice: number }> = {}
  for (const l of listings.value) {
    const gid = String(l.game_id)
    const p = Number(l.price)
    if (!sellerGroups[gid] || p < sellerGroups[gid].minPrice) {
      sellerGroups[gid] = { minPrice: p }
    }
  }
  for (const [gid, info] of Object.entries(sellerGroups)) {
    const official = map.get(gid)
    if (official && info.minPrice < official.price) {
      official.price = info.minPrice
      let op = official.original_price ? Number(official.original_price) : 0
      if (op === 0) {
        const lOp = listings.value.find(x => String(x.game_id) === gid)?.original_price
        op = Number(lOp || 0)
      }
      if (op > 0) {
        official.original_price = op
        official.discount = '-' + Math.round((1 - info.minPrice / op) * 100) + '%'
      }
    }
  }
  return Array.from(map.values())
})

// ====== 列表分页 ======
const filteredAllGames = computed(() => {
  if (!searchQuery.value) return allGames.value
  const q = searchQuery.value.toLowerCase()
  return allGames.value.filter(g => g.name.toLowerCase().includes(q))
})
const totalGames = computed(() => filteredAllGames.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalGames.value / pageSize)))
const displayGames = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredAllGames.value.slice(start, start + pageSize)
})
const visiblePages = computed(() => {
  const pages: number[] = []
  const tp = totalPages.value
  const cur = page.value
  const max = 5
  let startP = Math.max(1, cur - 2)
  let endP = Math.min(tp, startP + max - 1)
  if (endP - startP < max - 1) startP = Math.max(1, endP - max + 1)
  for (let i = startP; i <= endP; i++) pages.push(i)
  return pages
})

// ====== 详情页卖家表格（mock，后端后续补）======
const mockSellers = (game: any) => {
  const seed = String(game.game_id).split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  const basePrice = Number(game.price || 100)
  const names = ['叽***啦', '女***发', '西***里', 's***n', 'C***e', 'l***n', 'v***7', 'L***n', '嘟***帝', 'R***y']
  const regions = ['中国', '中国', '中国', '中国（大陆）', '中国（大陆）', '全球']
  const types = ['手动发货', '手动发货', '手动发货', '自动发货', '自动发货', '自动发货']
  const fluencies = [
    { label: '顺畅', cls: 'dot-green' },
    { label: '顺畅', cls: 'dot-green' },
    { label: '一般', cls: 'dot-yellow' },
    { label: '顺畅', cls: 'dot-green' }
  ]
  const result: any[] = []
  for (let i = 0; i < 10; i++) {
    const priceOffset = Math.round((Math.sin(seed + i * 1.7) * 0.2 + 0.05) * basePrice * 100) / 100
    const salePrice = Math.max(0.01, +(basePrice - priceOffset).toFixed(2))
    const op = Number(game.original_price || basePrice)
    const disc = op > 0 ? Math.round((1 - salePrice / op) * 100) : 0
    const flu = fluencies[(seed + i) % fluencies.length]
    result.push({
      fluency: flu.label,
      fluencyClass: flu.cls,
      avatarText: names[i][0] || '?',
      avatarBg: `hsl(${(seed + i * 37) % 360}, 60%, 55%)`,
      steamId64: (76561198000000000 + seed * 100 + i * 7007).toString(),
      deliverType: types[(seed + i) % types.length],
      accountName: names[i],
      region: regions[(seed + i) % regions.length],
      salePrice,
      minBuy: i < 3 ? 0 : +(salePrice * 0.5).toFixed(2),
      discountPct: Math.max(0, disc),
      deals: ['100+', '100+', '100+', '50+', '100+', '50+', '100+', '2', '50+', '100+'][i]
    })
  }
  return result
}

const detailSellers = ref<any[]>([])
const sortSellers = (key: string) => {
  sortKey.value = key
  const map: Record<string, (a: any, b: any) => number> = {
    price: (a, b) => a.salePrice - b.salePrice,
    minBuy: (a, b) => a.minBuy - b.minBuy,
    discount: (a, b) => b.discountPct - a.discountPct,
    deals: (a, b) => (parseInt(b.deals) || 0) - (parseInt(a.deals) || 0)
  }
  if (map[key]) detailSellers.value = [...detailSellers.value].sort(map[key])
}

const sortedSellers = computed(() => detailSellers.value)

// ====== 导航 ======
const goDetail = async (item: any) => {
  selectedGame.value = item
  viewMode.value = 'detail'
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // 加载真实代购卖家列表
  const r = await listingAPI.getPySellers({ gameId: item.game_id })
  const raw: any[] = r.data || []
  // 把后端字段映射到前端用的（保持模板兼容）
  detailSellers.value = raw.map((s: any) => ({
    listingId: s.listing_id,
    sellerId: s.seller_id,
    accountName: s.seller_name,
    avatar: s.seller_avatar,
    region: s.region,
    salePrice: Number(s.price),
    quota: Number(s.quota || 0),
    deliverType: s.deliver_type,
    fluency: s.fluency,
    fluencyClass: s.fluency_class,
    discountPct: s.discount_pct,
    deals: s.deals
  }))
  sortKey.value = 'price'
  detailSellers.value.sort((a, b) => a.salePrice - b.salePrice)
}

// 余额状态
const buyerBalance = ref(0)
const balanceCheck = ref(false)
const balanceAmount = computed(() => {
  if (!balanceCheck.value) return 0
  return Math.min(buyerBalance.value, selectedBuyer.value?.salePrice || 0)
})
const alipayAmount = computed(() => {
  return (selectedBuyer.value?.salePrice || 0) - balanceAmount.value
})
const selectedBuyer = ref<any>(null) // 当前弹窗选中的卖家

const showBuyDialog = (s: any) => {
  const user = authAPI.getCurrentUser()
  if (!user) { showToast('请先登录'); router.push('/login'); return }
  // 不能买自己的
  if (s.sellerId === user.id) { showToast('不能购买您自己上架的商品'); return }
  selectedBuyer.value = s
  balanceCheck.value = false
  buyerBalance.value = 0
  // 查买家余额
  walletAPI.getWallet(user.id).then((r: any) => {
    buyerBalance.value = Number(r.data?.balance || 0)
  })
  buyDialogVisible.value = true
}

const buyDialogVisible = ref(false)

const confirmBuy = async () => {
  const user = authAPI.getCurrentUser()
  if (!user || !selectedBuyer.value) return
  const seller = selectedBuyer.value
  try {
    const body: any = {
      buyer_id: user.id,
      listing_id: seller.listingId,
      game_id: selectedGame.value.game_id,
      game_name: selectedGame.value.name,
      game_image: selectedGame.value.image,
      price: seller.salePrice,
      total_amount: seller.salePrice,
      order_type: 'py',
      delivery_method: 'gift',
      version: '标准版'
    }
    const url = balanceAmount.value > 0
      ? `/orders?balance_amount=${balanceAmount.value.toFixed(2)}`
      : undefined
    const r = await orderAPI.createOrder(body, url)
    if (r.error || (r.data && r.data.id === undefined && !(r.data instanceof Object))) {
      showToast('下单失败: ' + (r.error || '未知错误'))
      return
    }
    buyDialogVisible.value = false
    showToast('✅ 代购下单成功！请到【PY代购】查看进度')
    router.push('/buyer?tab=py')
  } catch (e: any) {
    showToast('下单失败: ' + (e.message || '请重试'))
  }
}

// ====== 数据加载 ======
const loadData = async () => {
  try {
    const [gamesArr, lr] = await Promise.all([
      fetchAllGames().catch(() => []),
      listingAPI.getAvailable().catch(() => ({ data: [] }))
    ])
    if (Array.isArray(gamesArr)) games.value = gamesArr
    if (lr.data) listings.value = lr.data
  } catch (e) {
    console.error('加载失败', e)
  }
}

const showToast = (msg: string) => {
  const el = document.createElement('div')
  el.textContent = msg
  Object.assign(el.style, {
    position: 'fixed', top: '20px', left: '50%', transform: 'translateX(-50%)',
    background: '#2c3e50', color: '#fff', padding: '10px 20px', borderRadius: '6px',
    zIndex: '9999', fontSize: '14px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
  })
  document.body.appendChild(el)
  setTimeout(() => el.remove(), 2500)
}

onMounted(loadData)
</script>

<style scoped>
.cjx-gift-page { background: #f7f8fa; min-height: 100%; padding-bottom: 30px; }

/* =================== 列表态 =================== */
.cjx-list-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 28px 4px 20px;
}
.cjx-list-title { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0; }
.cjx-list-actions { display: flex; gap: 10px; align-items: center; }
.cjx-list-search {
  padding: 7px 14px; border: 1px solid #ddd; border-radius: 4px;
  font-size: 13px; width: 200px; transition: border-color .15s;
}
.cjx-list-search:focus { outline: none; border-color: #3498db; }
.cjx-help-btn {
  padding: 8px 20px; background: #fff; border: 1px solid #ddd; border-radius: 4px;
  cursor: pointer; font-size: 13px; color: #555; transition: all .2s;
}
.cjx-help-btn:hover { border-color: #3498db; color: #3498db; }

/* 热门网格 — 和 CDKeyMarket 同款尺寸 */
.cjx-hot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}
.cjx-hot-card {
  background: #fff; border-radius: 6px; overflow: hidden; cursor: pointer;
  border: 1px solid #eee; transition: transform .2s, box-shadow .2s;
}
.cjx-hot-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.12); }
.cjx-hot-img-wrap { position: relative; }
.cjx-hot-img { width: 100%; height: 120px; object-fit: cover; display: block; background: #f5f5f5; }
.cjx-hot-discount {
  position: absolute; top: 8px; right: 8px;
  background: #27ae60; color: #fff; font-size: 12px; font-weight: 600;
  padding: 3px 8px; border-radius: 3px;
}
.cjx-hot-presale {
  position: absolute; top: 8px; left: 8px;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff; font-size: 11px; font-weight: 600;
  padding: 3px 8px; border-radius: 3px;
}
.cjx-hot-info { padding: 10px 12px 12px; }
.cjx-hot-name {
  font-size: 13px; font-weight: 500; color: #333; margin-bottom: 6px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.cjx-hot-price-row { display: flex; align-items: baseline; gap: 8px; }
.cjx-hot-original {
  font-size: 11px; color: #bbb; text-decoration: line-through;
}
.cjx-hot-current {
  font-size: 16px; font-weight: 700; color: #fff;
  background: linear-gradient(135deg, #3498db, #2980b9);
  padding: 3px 10px; border-radius: 4px;
}

/* 分页 */
.cjx-pagination {
  display: flex; justify-content: center; align-items: center; gap: 4px;
  margin-top: 30px;
}
.cjx-page-btn {
  min-width: 32px; height: 32px; padding: 0 8px;
  border: 1px solid #e0e0e0; background: #fff; border-radius: 4px;
  cursor: pointer; font-size: 13px; color: #555; transition: all .15s;
}
.cjx-page-btn:hover:not(:disabled) { border-color: #3498db; color: #3498db; }
.cjx-page-btn.active { background: #3498db; color: #fff; border-color: #3498db; }
.cjx-page-btn:disabled { color: #ccc; cursor: not-allowed; }
.cjx-page-ellipsis { color: #bbb; padding: 0 6px; }
.cjx-page-total { margin-left: 12px; font-size: 13px; color: #888; }

/* =================== 详情态 =================== */
.cjx-detail-top { padding: 12px 0 0; }
.cjx-back-btn {
  background: none; border: none; color: #666; font-size: 14px;
  cursor: pointer; padding: 6px 4px; transition: color .15s;
}
.cjx-back-btn:hover { color: #3498db; }

.cjx-detail-header {
  display: flex; align-items: flex-start; gap: 24px;
  padding: 16px 0 24px;
}
.cjx-detail-cover {
  width: 260px; height: 160px; object-fit: cover; border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.cjx-detail-info { flex: 1; padding-top: 4px; }
.cjx-detail-name { font-size: 26px; font-weight: 700; color: #1a1a1a; margin: 0 0 4px; }
.cjx-detail-subname { font-size: 14px; color: #888; margin: 0 0 14px; }
.cjx-detail-meta { display: flex; gap: 16px; align-items: center; }
.cjx-detail-rating { font-size: 13px; color: #555; }

.cjx-detail-price-box { text-align: right; padding-top: 8px; min-width: 180px; }
.cjx-detail-ref { font-size: 13px; color: #888; margin-bottom: 4px; }
.cjx-detail-ref-price {
  font-size: 48px; font-weight: 800; color: #e74c3c; line-height: 1;
  letter-spacing: -1px;
}
.cjx-detail-origin-row { margin-top: 8px; display: flex; gap: 8px; justify-content: flex-end; align-items: baseline; }
.cjx-detail-origin { font-size: 15px; color: #bbb; text-decoration: line-through; }
.cjx-detail-discount {
  font-size: 13px; background: #27ae60; color: #fff;
  padding: 2px 8px; border-radius: 3px; font-weight: 600;
}

.cjx-detail-divider { border: none; border-top: 1px solid #eee; margin: 8px 0 28px; }

/* 卖家表格 */
.cjx-detail-sellers { background: #fff; border-radius: 6px; padding: 20px 24px; }
.cjx-detail-sellers-title {
  text-align: center; font-size: 18px; font-weight: 600; color: #333;
  margin: 0 0 20px;
}
.cjx-sellers-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.cjx-sellers-table th,
.cjx-sellers-table td {
  padding: 12px 10px; text-align: left; border-bottom: 1px solid #f0f0f0;
}
.cjx-sellers-table thead th {
  background: #fafbfc; color: #666; font-weight: 500; font-size: 12px;
  border-bottom: 1px solid #e8e8e8;
}
.cjx-sellers-table tbody tr:hover { background: #fafbfc; }
.sortable { cursor: pointer; user-select: none; }
.sortable:hover { color: #3498db; }

.cjx-fluid-cell { display: flex; align-items: center; gap: 6px; }
.cjx-fluid-label { font-size: 12px; color: #555; }
.cjx-fluid-dot { width: 8px; height: 8px; border-radius: 50%; }
.cjx-fluid-dot.dot-green { background: #27ae60; }
.cjx-fluid-dot.dot-yellow { background: #f39c12; }
.cjx-fluid-dot.dot-red { background: #e74c3c; }

.cjx-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 13px; font-weight: 600;
}
.cjx-mono { font-family: 'Courier New', monospace; font-size: 12px; color: #888; }
.cjx-deliver-type {
  font-size: 12px; padding: 2px 8px; border-radius: 3px;
}
.cjx-deliver-type.auto { background: #eaf7ed; color: #27ae60; }
.cjx-deliver-type.manual { background: #fff3cd; color: #e67e22; }
.cjx-account-name { color: #555; }
.cjx-price-cell { color: #333; font-weight: 500; }
.cjx-discount-cell { color: #e74c3c; font-weight: 600; }
.cjx-deals-cell { color: #888; }

.cjx-buy-seller-btn {
  padding: 5px 14px; background: #3498db; color: #fff;
  border: none; border-radius: 4px; cursor: pointer; font-size: 12px;
  transition: background .15s;
}
.cjx-buy-seller-btn:hover { background: #2980b9; }

/* 弹窗 */
.cjx-dialog-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center; z-index: 999;
}
.cjx-dialog {
  background: #fff; border-radius: 8px; width: 420px; max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2); overflow: hidden;
}
.cjx-dialog-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid #eee;
}
.cjx-dialog-header h3 { margin: 0; font-size: 16px; color: #333; }
.cjx-dialog-close {
  border: none; background: none; font-size: 18px; cursor: pointer;
  color: #999; padding: 0; line-height: 1;
}
.cjx-dialog-body { padding: 20px; }
.cjx-dialog-row {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 10px; font-size: 13px; color: #555;
}
.cjx-dialog-row b { color: #222; font-weight: 600; }
.cjx-red { color: #e74c3c !important; font-size: 16px; }
.cjx-dialog-hr { border: none; border-top: 1px dashed #ddd; margin: 14px 0; }
.cjx-check-row { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.cjx-split-pay { margin-top: 8px; padding: 10px; background: #f8f9fa; border-radius: 4px; font-size: 12px; }
.cjx-split-line { margin: 3px 0; color: #555; }
.cjx-split-line b { color: #3498db; margin-left: 6px; }
.cjx-dialog-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 12px 20px; border-top: 1px solid #eee;
}
.cjx-btn { padding: 7px 18px; border-radius: 4px; font-size: 13px; cursor: pointer; border: 1px solid transparent; }
.cjx-btn-cancel { background: #f5f5f5; color: #555; border-color: #ddd; }
.cjx-btn-confirm { background: #3498db; color: #fff; }
.cjx-btn-confirm:hover { background: #2980b9; }
</style>

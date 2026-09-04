﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <Layout>
    <div class="cjx-cdkey-page">
      <!-- ========== 火热预售（单独一行）========== -->
      <section class="cjx-presale-section" v-if="presaleGames.length > 0">
        <h2 class="cjx-section-title cjx-presale-title">🔥 火热预售</h2>
        <div class="cjx-presale-row">
          <div 
            class="cjx-game-card cjx-presale-card" 
            v-for="item in presaleGames" 
            :key="item.uid" 
            @click="goToGame(item)"
          >
            <div class="cjx-card-img-wrapper">
              <img :src="getImageUrl(item.image)" class="cjx-card-img" />
              <span v-if="item.discount" class="cjx-card-discount">{{ item.discount }}</span>
              <span class="cjx-presale-badge">预售</span>
            </div>
            <div class="cjx-card-info">
              <div class="cjx-card-name">{{ item.name }}</div>
              <div class="cjx-card-price-row">
                <span class="cjx-card-original" v-if="item.original_price">¥{{ item.original_price }}</span>
                <span class="cjx-card-price cjx-presale-price">¥{{ item.price.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- CDKey市场 -->
      <section class="cjx-section">
        <h2 class="cjx-section-title">
          出售CDKey
          <span class="cjx-count-badge">共 {{ displayGames.length }} 款游戏</span>
        </h2>

        <!-- 排序栏 -->
        <div class="cjx-filter-bar">
          <span class="cjx-filter-label">排序</span>
          <button 
            v-for="opt in sortOptions" :key="opt.value"
            class="cjx-sort-btn"
            :class="{ 'cjx-sort-active': sortBy === opt.value }"
            @click="sortBy = opt.value"
          >{{ opt.label }}</button>
          <input v-model="searchQuery" placeholder="游戏全称搜索" class="cjx-search-input" />
          <span class="cjx-magnifier">🔍</span>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="cjx-loading">加载中...</div>
        <div v-else-if="displayGames.length === 0" class="cjx-empty">暂无商品</div>

        <!-- 游戏卡片网格（图2） -->
        <div class="cjx-game-grid" v-else>
          <div 
            class="cjx-game-card" 
            v-for="item in displayGames" 
            :key="item.uid" 
            @click="goToGame(item)"
          >
            <div class="cjx-card-img-wrapper">
              <img :src="getImageUrl(item.image)" class="cjx-card-img" />
              <span v-if="item.discount" class="cjx-card-discount">{{ item.discount }}</span>
            </div>
            <div class="cjx-card-info">
              <div class="cjx-card-name">{{ item.name }}</div>
              <div class="cjx-card-price-row">
                <span class="cjx-card-original" v-if="item.original_price">¥{{ item.original_price }}</span>
                <span class="cjx-card-price">¥{{ item.price.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Layout from '../components/Layout.vue'
import { fetchAllGames, listingAPI } from '../config/supabase-local.ts'

const router = useRouter()

const games = ref<any[]>([])       // 官方游戏
const listings = ref<any[]>([])    // 卖家上架
const searchQuery = ref('')
const sortBy = ref('newest')       // newest / week-sales / sales / discount / price
const loading = ref(false)

const sortOptions = [
  { value: 'newest', label: '最新' },
  { value: 'week-sales', label: '周销量' },
  { value: 'sales', label: '销量' },
  { value: 'discount', label: '折扣' },
  { value: 'price', label: '价格' }
]

const getImageUrl = (path: string) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  if (path.includes('picture/')) {
    const fn = path.split('picture/')[1]
    if (fn) return `/picture/${fn}`
  }
  return path.startsWith('/') ? path : `/${path}`
}

// 合并：每个游戏一行，价格取最低价（玩家 < 官方）
const allGames = computed(() => {
  const map = new Map<string, any>()

  // 官方游戏先放进去
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
      lowest_source: 'official',
      is_presale: !!g.is_presale
    })
  }

  // 玩家 listings 聚合到游戏上，取最低价
  const sellerGroups: Record<string, { minPrice: number; count: number }> = {}
  for (const l of listings.value) {
    const gid = String(l.game_id)
    const p = Number(l.price)
    if (!sellerGroups[gid] || p < sellerGroups[gid].minPrice) {
      sellerGroups[gid] = { minPrice: p, count: (sellerGroups[gid]?.count || 0) + 1 }
    } else if (p === sellerGroups[gid].minPrice) {
      sellerGroups[gid].count++
    }
  }

  // 如果某个游戏有玩家卖更便宜 → 更新
  for (const [gid, info] of Object.entries(sellerGroups)) {
    // 先找这个 game_id 对应的官方游戏
    const official = map.get(gid)
    if (official && info.minPrice < official.price) {
      official.price = info.minPrice
      official.lowest_source = 'seller'
      // 继承预售标记（从官方）
      if (!official.is_presale) official.is_presale = false
      // 有官方 original_price 就用官方的，没有就从 listings 找
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
    // 如果没有官方游戏，就从 listings 里创建一行
    if (!official) {
      const l = listings.value.find(x => String(x.game_id) === gid)
      if (l) {
        const op = Number(l.original_price || 0)
        const d = op > 0 ? '-' + Math.round((1 - info.minPrice / op) * 100) + '%' : ''
        map.set(gid, {
          uid: 's-' + gid,
          game_id: l.game_id,
          name: l.game_name,
          image: l.game_image,
          price: info.minPrice,
          original_price: op || null,
          discount: d,
          lowest_source: 'seller'
        })
      }
    }
  }

  const result = Array.from(map.values())
  // 诊断 log — 只看 game_id=8 三更
  const sg = result.find((x: any) => String(x.game_id) === '8')
  if (sg) console.log('[catalog] 三更:', JSON.stringify(sg))
  return result
})

// 预售游戏（单独一行，不带搜索过滤）
const presaleGames = computed(() => {
  return allGames.value.filter(g => g.is_presale)
})

// 普通游戏（排除预售）
const normalGames = computed(() => {
  return allGames.value.filter(g => !g.is_presale)
})

const displayGames = computed(() => {
  let result = [...normalGames.value]

  // 搜索
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(g => g.name.toLowerCase().includes(q))
  }

  // 排序
  switch (sortBy.value) {
    case 'price': result.sort((a, b) => a.price - b.price); break
    case 'discount': result.sort((a, b) => {
      const da = parseInt(a.discount?.replace(/[^0-9]/g, '') || 0)
      const db = parseInt(b.discount?.replace(/[^0-9]/g, '') || 0)
      return da - db // 折扣大的在前（绝对值大）
    }); break
    case 'newest':
    case 'week-sales':
    case 'sales':
    default: break // 保持原顺序
  }
  return result
})

const goToGame = (item: any) => {
  router.push({
    path: `/game/${encodeURIComponent(item.name)}`,
    query: {
      game_id: item.game_id,
      lowest_source: item.lowest_source
    }
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const [gamesRes, listingsRes] = await Promise.all([
      fetchAllGames().catch(() => []),
      listingAPI.getAvailable().catch(() => ({ data: [] }))
    ])
    games.value = gamesRes || []
    listings.value = listingsRes?.data || []
    console.log(`✓ 加载: ${games.value.length} 官方, ${listings.value.length} 玩家CDKey, 合并 ${allGames.value.length} 款游戏`)
  } catch (e) {
    console.warn('加载失败', e)
  }
  loading.value = false
}

onMounted(loadData)
</script>

<style scoped>
.cjx-cdkey-page { background: #fff; border-radius: 8px; padding: 20px; }

/* ======== 火热预售区（单独一行横条）======== */
.cjx-presale-section { margin-bottom: 30px; }
.cjx-presale-title { color: #e74c3c; border-left: 4px solid #e74c3c; padding-left: 12px; }
.cjx-presale-row {
  display: flex; flex-wrap: nowrap; gap: 16px;
  overflow-x: auto; padding-bottom: 6px;
}
.cjx-presale-row::-webkit-scrollbar { height: 6px; }
.cjx-presale-row::-webkit-scrollbar-thumb { background: #ddd; border-radius: 3px; }
.cjx-presale-card { flex: 0 0 220px; max-width: 220px; }
.cjx-presale-badge {
  position: absolute; top: 8px; right: 8px; z-index: 3;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff; font-size: 11px; font-weight: 600;
  padding: 3px 8px; border-radius: 4px; letter-spacing: 1px;
}
.cjx-presale-price { color: #e74c3c !important; font-weight: 700; }

.cjx-section { margin-bottom: 30px; }
.cjx-section-title { font-size: 18px; font-weight: bold; margin-bottom: 18px; color: #333; display: flex; align-items: center; gap: 10px; }
.cjx-count-badge { font-size: 12px; color: #888; font-weight: normal; background: #f0f0f0; padding: 2px 8px; border-radius: 10px; }

/* 排序栏 */
.cjx-filter-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.cjx-filter-label { font-size: 14px; color: #333; margin-right: 6px; }
.cjx-sort-btn {
  padding: 6px 14px; border: 1px solid #ddd; background: #fff; border-radius: 4px;
  cursor: pointer; font-size: 13px; color: #666; transition: all 0.2s;
}
.cjx-sort-btn:hover { border-color: #e74c3c; color: #e74c3c; }
.cjx-sort-active { background: #333; border-color: #333; color: #fff !important; }

.cjx-search-input {
  margin-left: auto; padding: 8px 14px; border: 1px solid #ddd; border-radius: 4px;
  width: 220px; font-size: 13px;
}
.cjx-search-input:focus { outline: none; border-color: #333; }
.cjx-magnifier { color: #999; font-size: 14px; margin-left: -28px; pointer-events: none; }

/* 卡片网格 — 图2风格 */
.cjx-game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}
.cjx-game-card {
  background: #fff; border-radius: 6px; overflow: hidden; cursor: pointer;
  border: 1px solid #eee; transition: all 0.2s;
}
.cjx-game-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.12); border-color: #ddd; }

.cjx-card-img-wrapper { position: relative; }
.cjx-card-img { width: 100%; height: 120px; object-fit: cover; display: block; background: #f5f5f5; }
.cjx-card-discount {
  position: absolute; top: 6px; left: 6px;
  background: #e74c3c; color: #fff; padding: 2px 6px; border-radius: 3px;
  font-size: 11px; font-weight: 600;
}

.cjx-card-info { padding: 10px 12px; }
.cjx-card-name {
  font-size: 13px; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  margin-bottom: 6px; font-weight: 500;
}
.cjx-card-price-row { display: flex; align-items: baseline; gap: 6px; }
.cjx-card-original { font-size: 11px; color: #aaa; text-decoration: line-through; }
.cjx-card-price { font-size: 17px; color: #e74c3c; font-weight: 700; }

.cjx-loading, .cjx-empty { text-align: center; padding: 60px; color: #999; }
</style>

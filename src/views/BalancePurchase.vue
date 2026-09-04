﻿<template>
  <Layout>
    <div class="cjx-list-header">
      <h2 class="cjx-list-title">余额购 · 平台热门游戏</h2>
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
      <div class="cjx-hot-card" v-for="game in displayGames" :key="game.game_id" @click="buyGame(game)">
        <div class="cjx-hot-img-wrap">
          <img :src="getImageUrl(game.image)" class="cjx-hot-img" />
          <span v-if="game.discount" class="cjx-hot-discount">{{ game.discount }}</span>
        </div>
        <div class="cjx-hot-info">
          <div class="cjx-hot-name" :title="game.name">{{ game.name }}</div>
          <div class="cjx-hot-price-row">
            <span class="cjx-hot-original" v-if="game.originalPrice">¥{{ game.originalPrice.toFixed(2) }}</span>
            <span class="cjx-hot-current">¥{{ getRawPrice(game.price).toFixed(2) }}</span>
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
        :disabled="p === '...'"
        @click="typeof p === 'number' && (page = p)"
      >{{ p }}</button>
      <button class="cjx-page-btn" :disabled="page === totalPages" @click="page++">›</button>
      <span class="cjx-page-total">共 {{ filteredSortedGames.length }} 款</span>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '../components/Layout.vue'
import { fetchAllGames, listingAPI } from '../config/supabase-local.ts'

const router = useRouter()

// ======= 余额购 =======
const games = ref<any[]>([])
const searchQuery = ref('')
const page = ref(1)
const pageSize = 70

const getRawPrice = (p: any) => {
  if (typeof p === 'number') return p
  return parseFloat(String(p).replace(/[^0-9.]/g, '')) || 0
}

// 搜索 + 排序
const filteredSortedGames = computed(() => {
  let result = [...games.value]
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(g => g.name.toLowerCase().includes(q))
  }
  result.sort((a, b) => getRawPrice(a.price) - getRawPrice(b.price))
  return result
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredSortedGames.value.length / pageSize)))
const displayGames = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredSortedGames.value.slice(start, start + pageSize)
})
const visiblePages = computed(() => {
  const tp = totalPages.value
  const cur = page.value
  const pages: (number | string)[] = []
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) pages.push(i)
  } else {
    pages.push(1)
    if (cur > 4) pages.push('...')
    const startP = Math.max(2, cur - 1)
    const endP = Math.min(tp - 1, cur + 1)
    for (let i = startP; i <= endP; i++) pages.push(i)
    if (cur < tp - 3) pages.push('...')
    pages.push(tp)
  }
  return pages
})

const getImageUrl = (path: string) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  if (path.includes('picture/')) {
    const fn = path.split('picture/')[1]
    if (fn) return `/picture/${fn}`
  }
  return path.startsWith('/') ? path : `/${path}`
}

const buyGame = (game: any) => {
  router.push(`/balance/detail/${game.game_id}`)
}

// ======= 加载 =======
onMounted(async () => {
  try {
    const [gs, ls] = await Promise.all([fetchAllGames(), listingAPI.getAvailable()])
    const listArr: any[] = ls.data || []
    const catalog: any[] = []
    for (const g of gs) {
      const py = listArr.filter((l: any) => Number(l.game_id) === Number(g.game_id))
      let price = Number(g.price)
      let originalPrice = Number(g.original_price || g.price)
      if (py.length) {
        const min = Math.min(...py.map((p: any) => Number(p.price)))
        if (min < price) price = min
      }
      const discount = originalPrice > 0
        ? `-${Math.round((1 - price / originalPrice) * 100)}%`
        : ''
      catalog.push({
        game_id: g.game_id,
        name: g.name,
        image: g.image,
        price,
        originalPrice,
        discount
      })
    }
    games.value = catalog
  } catch {
    games.value = []
  }
})
</script>

<style scoped>
/* 列表 header — GiftPurchase 同款 */
.cjx-list-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 0 20px; border-bottom: 1px solid #eee; margin-bottom: 20px;
}
.cjx-list-title { font-size: 18px; color: #333; margin: 0; font-weight: 500; }
.cjx-list-actions { display: flex; gap: 10px; align-items: center; }
.cjx-list-search {
  padding: 7px 14px; border: 1px solid #ddd; border-radius: 4px;
  font-size: 13px; width: 200px;
}
.cjx-list-search:focus { outline: none; border-color: #3498db; }
.cjx-help-btn {
  padding: 7px 14px; border: 1px solid #ddd; border-radius: 4px;
  background: #fff; font-size: 13px; cursor: pointer; color: #555; transition: all .2s;
}
.cjx-help-btn:hover { border-color: #3498db; color: #3498db; }

/* 热门网格 — GiftPurchase/CDKeyMarket 同款 */
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
.cjx-hot-info { padding: 10px 12px 12px; }
.cjx-hot-name {
  font-size: 13px; font-weight: 500; color: #333; margin-bottom: 6px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.cjx-hot-price-row { display: flex; align-items: baseline; gap: 8px; }
.cjx-hot-original { font-size: 11px; color: #bbb; text-decoration: line-through; }
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
.cjx-page-total { margin-left: 12px; font-size: 13px; color: #888; }
</style>

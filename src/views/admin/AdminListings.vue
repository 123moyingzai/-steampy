<template>
  <div class="admin-listings">
    <div class="toolbar">
      <div class="filter-bar">
        <select v-model="filterStatus" @change="loadListings">
          <option value="">全部状态</option>
          <option value="available">在售</option>
          <option value="pending_activation">待激活</option>
          <option value="sold">已售出</option>
        </select>
        <select v-model="filterType" @change="loadListings">
          <option value="">全部类型</option>
          <option value="cdkey">CDKey</option>
          <option value="py">PY代购</option>
        </select>
        <input type="text" v-model="searchKeyword" placeholder="搜索游戏名..." @input="loadListings" />
      </div>
      <button class="btn btn-outline" @click="loadListings">刷新</button>
    </div>

    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>游戏</th>
            <th>卖家ID</th>
            <th>类型</th>
            <th>价格</th>
            <th>库存/额度</th>
            <th>状态</th>
            <th>上架时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in filteredList" :key="l.id">
            <td class="td-game">
              <img :src="getImg(l.game_image)" class="td-thumb" />
              <span>{{ l.game_name }}</span>
            </td>
            <td class="td-monospace">{{ shortId(l.seller_id) }}</td>
            <td>
              <span :class="['badge', l.type === 'py' ? 'badge-py' : 'badge-cdkey']">
                {{ l.type === 'py' ? 'PY代购' : 'CDKey' }}
              </span>
            </td>
            <td class="td-price">¥{{ Number(l.price).toFixed(2) }}</td>
            <td>{{ l.type === 'py' ? (l.quota ? '¥' + Number(l.quota).toFixed(2) : '-') : (l.cdkey ? '有' : '-') }}</td>
            <td>
              <span :class="['badge', statusBadge(l.status)]">{{ statusLabel(l.status) }}</span>
            </td>
            <td>{{ formatTime(l.created_at) }}</td>
            <td class="td-actions">
              <button v-if="l.status !== 'available'" class="btn btn-small btn-primary" @click="toggleStatus(l, 'available')">恢复上架</button>
              <button v-else class="btn btn-small btn-warn" @click="toggleStatus(l, 'pending_activation')">下架</button>
              <button class="btn btn-small btn-danger" @click="hardDelete(l)">删除</button>
            </td>
          </tr>
          <tr v-if="filteredList.length === 0">
            <td colspan="8" class="td-empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const all = ref<any[]>([])
const filterStatus = ref('')
const filterType = ref('')
const searchKeyword = ref('')

const filteredList = computed(() => {
  return all.value.filter(l => {
    if (filterStatus.value && l.status !== filterStatus.value) return false
    if (filterType.value && l.type !== filterType.value) return false
    if (searchKeyword.value && !(l.game_name || '').toLowerCase().includes(searchKeyword.value.toLowerCase())) return false
    return true
  })
})

async function loadListings() {
  try {
    const params = new URLSearchParams()
    if (filterStatus.value) params.set('status', filterStatus.value)
    if (filterType.value) params.set('type', filterType.value)
    const r = await axios.get('/api/admin/listings?' + params.toString())
    all.value = r.data?.data || []
  } catch (e) { console.error('加载上架失败', e) }
}

async function toggleStatus(l: any, newStatus: string) {
  try {
    await axios.put(`/api/admin/listings/${l.id}/status`, { status: newStatus })
    alert('状态已更新')
    loadListings()
  } catch (e: any) { alert('操作失败: ' + (e?.response?.data?.message || e.message)) }
}

async function hardDelete(l: any) {
  if (!confirm(`确定删除该上架记录？\n游戏：${l.game_name}\n此操作不可恢复`)) return
  try {
    await axios.delete(`/api/admin/listings/${l.id}`)
    alert('已删除')
    loadListings()
  } catch (e: any) { alert('删除失败: ' + (e?.response?.data?.message || e.message)) }
}

function getImg(src: string) {
  if (!src) return '/picture/header.jpg'
  if (src.startsWith('http')) return src
  return src
}
function statusLabel(s: string) {
  return ({ available: '在售', pending_activation: '待激活', sold: '已售出' } as any)[s] || s
}
function statusBadge(s: string) {
  return ({ available: 'badge-success', pending_activation: 'badge-warning', sold: 'badge-default' } as any)[s] || 'badge-default'
}
function shortId(id: string) { return id ? (id.slice(0, 8) + '...') : '-' }
function formatTime(t: string) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadListings)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; padding: 16px; background: #fff; border-radius: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.filter-bar { display: flex; gap: 10px; flex: 1; flex-wrap: wrap; }
.filter-bar select, .filter-bar input {
  padding: 7px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; min-width: 140px;
}
.filter-bar input { flex: 1; min-width: 200px; }
.btn { padding: 8px 14px; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; transition: all 0.15s; }
.btn-primary { background: #4a6cf7; color: #fff; }
.btn-primary:hover { background: #3a5ce5; }
.btn-outline { background: transparent; border: 1px solid #4a6cf7; color: #4a6cf7; }
.btn-outline:hover { background: #f0f4ff; }
.btn-danger { background: #e74c3c; color: #fff; }
.btn-danger:hover { background: #c0392b; }
.btn-warn { background: #e67e22; color: #fff; }
.btn-warn:hover { background: #d35400; }
.btn-small { padding: 5px 10px; font-size: 12px; margin-right: 4px; }

.card { background: #fff; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table thead { background: #f5f7fa; }
.data-table th { text-align: left; padding: 12px; font-weight: 600; color: #555; border-bottom: 1px solid #eee; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; color: #333; }
.data-table tr:hover td { background: #fafbfc; }

.td-game { display: flex; align-items: center; gap: 8px; }
.td-thumb { width: 36px; height: 50px; object-fit: cover; border-radius: 4px; background: #f0f0f0; }
.td-price { font-weight: 600; color: #e74c3c; }
.td-monospace { font-family: monospace; color: #888; font-size: 12px; }
.td-actions { white-space: nowrap; }
.td-empty { text-align: center; padding: 40px; color: #aaa; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-success { background: #e8f8ef; color: #27ae60; }
.badge-warning { background: #fdecea; color: #e67e22; }
.badge-default { background: #f0f0f0; color: #888; }
.badge-cdkey { background: #e3f2fd; color: #2980b9; }
.badge-py { background: #fff3e0; color: #e67e22; }
</style>

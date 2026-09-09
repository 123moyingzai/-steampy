<template>
  <div class="admin-reviews">
    <div class="toolbar">
      <div class="filter-bar">
        <span class="stat-pill">待审核 <strong>{{ pendingCount }}</strong> 条</span>
        <span class="stat-pill stat-report">累计被举报 <strong>{{ totalReportCount }}</strong> 次</span>
      </div>
      <button class="btn btn-outline" @click="loadReviews">刷新</button>
    </div>

    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>游戏</th>
            <th>用户</th>
            <th>评价</th>
            <th>内容</th>
            <th>🔥 举报</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in filteredList" :key="r.id">
            <td class="td-game">
              <span class="td-strong">{{ r.game_name || ('#' + r.game_id) }}</span>
            </td>
            <td>
              <div class="td-avatar">{{ (r.user_name || '匿').slice(0, 1) }}</div>
              <span>{{ r.user_name || '匿名' }}</span>
            </td>
            <td>
              <span :class="['badge', r.recommend === 1 ? 'badge-pos' : 'badge-neg']">
                {{ r.recommend === 1 ? '👍 推荐' : '👎 不推荐' }}
              </span>
            </td>
            <td class="td-content">
              <div class="content-text">{{ r.content }}</div>
              <div class="content-imgs" v-if="r.images">
                <img v-for="(img, i) in (r.images?.includes('|||') ? r.images.split('|||').filter(Boolean) : (r.images ? [r.images] : []))" :key="i" :src="img" class="content-img" />
              </div>
            </td>
            <td>
              <span class="report-count">🔥 {{ r.report_count || 0 }}</span>
            </td>
            <td>
              <span :class="['badge', statusBadge(r.status)]">{{ statusLabel(r.status) }}</span>
            </td>
            <td>{{ formatTime(r.created_at) }}</td>
            <td class="td-actions">
              <button class="btn btn-small btn-primary" @click="review(r, 1)">放行</button>
              <button class="btn btn-small btn-warn" @click="review(r, 2)">下架</button>
              <button class="btn btn-small btn-danger" @click="del(r)">彻底删除</button>
            </td>
          </tr>
          <tr v-if="filteredList.length === 0">
            <td colspan="8" class="td-empty">暂无被举报的评测</td>
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

const filteredList = computed(() => all.value)
const pendingCount = computed(() => all.value.filter(r => r.status !== 1).length)
const totalReportCount = computed(() => all.value.reduce((s, r) => s + (r.report_count || 0), 0))

async function loadReviews() {
  try {
    const r = await axios.get('/api/admin/reviews')
    all.value = r.data?.data || []
  } catch (e) { console.error('加载评测列表失败', e) }
}

async function review(r: any, newStatus: number) {
  const action = newStatus === 1 ? '放行（保留内容，用户可继续看到，举报计数清零）' : '下架（用户端不再显示）'
  if (!confirm(`确定${action}？`)) return
  try {
    await axios.put(`/api/admin/reviews/${r.id}/review`, { status: newStatus })
    alert(newStatus === 1 ? '已放行该评论' : '已下架该评论')
    loadReviews()
  } catch (e: any) { alert('操作失败: ' + (e?.response?.data?.message || e.message)) }
}

async function del(r: any) {
  if (!confirm('确定删除该评测？此操作不可恢复')) return
  try {
    await axios.delete(`/api/admin/reviews/${r.id}`)
    loadReviews()
  } catch (e: any) { alert('删除失败') }
}

function statusLabel(s: number) {
  return ({ 0: '待审核', 1: '已通过', 2: '已拒绝' } as any)[s] || ('状态' + s)
}
function statusBadge(s: number) {
  return ({ 0: 'badge-warning', 1: 'badge-success', 2: 'badge-danger' } as any)[s] || 'badge-default'
}
function formatTime(t: string) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadReviews)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; padding: 16px; background: #fff; border-radius: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.filter-bar { display: flex; gap: 10px; flex: 1; align-items: center; flex-wrap: wrap; }
.filter-bar select { padding: 7px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; min-width: 140px; }
.stat-pill { padding: 6px 12px; background: #fff8e1; color: #e67e22; border-radius: 16px; font-size: 13px; }
.stat-pill strong { margin-left: 2px; }

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
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; color: #333; vertical-align: top; }
.data-table tr:hover td { background: #fafbfc; }

.td-game { min-width: 140px; }
.td-avatar { display: inline-flex; width: 28px; height: 28px; border-radius: 50%; background: linear-gradient(135deg, #4a6cf7, #3a5ce5); color: #fff; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; margin-right: 6px; }
.td-content { max-width: 340px; }
.content-text { font-size: 13px; color: #444; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.content-imgs { display: flex; gap: 6px; margin-top: 6px; }
.content-img { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; cursor: zoom-in; }
.td-strong { font-weight: 600; }
.td-actions { white-space: nowrap; }
.td-empty { text-align: center; padding: 40px; color: #aaa; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-success { background: #e8f8ef; color: #27ae60; }
.badge-warning { background: #fff8e1; color: #e67e22; }
.badge-danger { background: #fdecea; color: #c0392b; }
.badge-default { background: #f0f0f0; color: #888; }
.badge-pos { background: #e8f8ef; color: #27ae60; }
.badge-neg { background: #fdecea; color: #c0392b; }
.report-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: #ffeaea;
  color: #c0392b;
}
.stat-report { background: #fef3e7; color: #e67e22; margin-left: 8px; }
</style>

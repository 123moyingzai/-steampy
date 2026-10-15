<template>
  <div class="admin-refunds">
    <div class="toolbar">
      <div class="filter-bar">
        <select v-model="filterStatus" @change="loadRefunds">
          <option value="">全部状态</option>
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已拒绝</option>
        </select>
        <input
          v-model="keyword"
          type="text"
          class="keyword-input"
          placeholder="搜索退款单号 / 订单号 / 游戏名称"
          @keyup.enter="loadRefunds"
        />
        <button class="btn btn-outline" @click="loadRefunds">搜索</button>
        <span class="stat-pill">待审核 <strong>{{ pendingCount }}</strong> 笔</span>
        <span class="stat-pill">退款总额 <strong class="amount">¥{{ totalAmount.toFixed(2) }}</strong></span>
      </div>
      <button class="btn btn-outline" @click="loadRefunds">刷新</button>
    </div>

    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>退款单号</th>
            <th>订单号</th>
            <th>买家</th>
            <th>游戏</th>
            <th>退款金额</th>
            <th>退款原因</th>
            <th>状态</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in list" :key="r.id">
            <td class="td-monospace">{{ r.refund_no }}</td>
            <td class="td-monospace">{{ r.order_no }}</td>
            <td>
              <div>{{ r.buyer_nickname || r.buyer_username || '未知用户' }}</div>
              <div class="td-sub">{{ shortId(r.buyer_id) }}</div>
            </td>
            <td>{{ r.game_name || '-' }}</td>
            <td class="td-price">¥{{ Number(r.amount).toFixed(2) }}</td>
            <td>
              <div class="reason-text">{{ r.reason || '-' }}</div>
              <div v-if="r.review_remark" class="td-sub remark">审核备注：{{ r.review_remark }}</div>
            </td>
            <td>
              <span :class="['badge', statusBadge(r.status)]">{{ statusLabel(r.status) }}</span>
            </td>
            <td>{{ formatTime(r.applied_at) }}</td>
            <td class="td-actions">
              <template v-if="r.status === 'pending'">
                <button class="btn btn-small btn-primary" @click="review(r, 'approve')">通过退款</button>
                <button class="btn btn-small btn-danger" @click="review(r, 'reject')">拒绝</button>
              </template>
              <span v-else class="td-sub">{{ formatTime(r.reviewed_at) }}</span>
            </td>
          </tr>
          <tr v-if="list.length === 0">
            <td colspan="9" class="td-empty">暂无退款记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const list = ref<any[]>([])
const filterStatus = ref('')
const keyword = ref('')

const pendingCount = computed(() => list.value.filter(r => r.status === 'pending').length)
const totalAmount = computed(() => list.value.reduce((s, r) => s + Number(r.amount || 0), 0))

async function loadRefunds() {
  try {
    const params = new URLSearchParams()
    if (filterStatus.value) params.set('status', filterStatus.value)
    if (keyword.value.trim()) params.set('keyword', keyword.value.trim())
    const qs = params.toString()
    const r = await axios.get('/api/admin/refunds' + (qs ? `?${qs}` : ''))
    list.value = r.data?.data || []
  } catch (e) { console.error('加载退款列表失败', e) }
}

async function review(r: any, action: 'approve' | 'reject') {
  const remark = action === 'approve'
    ? (prompt('审核备注（可选）:', '已核对，退款至买家余额') || '')
    : (prompt('拒绝原因:', '不符合退款条件') || '')
  try {
    await axios.put(`/api/admin/refunds/${r.id}/review`, { action, remark })
    alert(action === 'approve' ? '已通过，退款已退回买家余额' : '已拒绝退款申请')
    loadRefunds()
  } catch (e: any) { alert('操作失败: ' + (e?.response?.data?.message || e.message)) }
}

function statusLabel(s: string) {
  return ({ pending: '待审核', approved: '已通过', rejected: '已拒绝' } as any)[s] || s
}
function statusBadge(s: string) {
  return ({ pending: 'badge-warning', approved: 'badge-success', rejected: 'badge-danger' } as any)[s] || 'badge-default'
}
function shortId(id: string) { return id ? (id.slice(0, 8) + '...') : '-' }
function formatTime(t: string) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadRefunds)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; padding: 16px; background: #fff; border-radius: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.filter-bar { display: flex; gap: 10px; flex: 1; align-items: center; flex-wrap: wrap; }
.filter-bar select, .keyword-input { padding: 7px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.filter-bar select { min-width: 130px; }
.keyword-input { flex: 1; min-width: 220px; }
.stat-pill { padding: 6px 12px; background: #f0f4ff; color: #4a6cf7; border-radius: 16px; font-size: 13px; }
.stat-pill strong { color: #2a4ce5; margin-left: 2px; }
.stat-pill .amount { color: #e74c3c; }

.btn { padding: 8px 14px; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; transition: all 0.15s; }
.btn-primary { background: #4a6cf7; color: #fff; }
.btn-primary:hover { background: #3a5ce5; }
.btn-outline { background: transparent; border: 1px solid #4a6cf7; color: #4a6cf7; }
.btn-outline:hover { background: #f0f4ff; }
.btn-danger { background: #e74c3c; color: #fff; }
.btn-danger:hover { background: #c0392b; }
.btn-small { padding: 5px 10px; font-size: 12px; margin-right: 4px; }

.card { background: #fff; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table thead { background: #f5f7fa; }
.data-table th { text-align: left; padding: 12px; font-weight: 600; color: #555; border-bottom: 1px solid #eee; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; color: #333; vertical-align: top; }
.data-table tr:hover td { background: #fafbfc; }

.td-price { font-weight: 600; color: #e74c3c; }
.td-sub { font-size: 12px; color: #999; margin-top: 2px; }
.td-sub.remark { color: #c0392b; }
.td-monospace { font-family: monospace; color: #888; font-size: 12px; }
.td-actions { white-space: nowrap; }
.td-empty { text-align: center; padding: 40px; color: #aaa; }
.reason-text { max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-success { background: #e8f8ef; color: #27ae60; }
.badge-warning { background: #fff8e1; color: #e67e22; }
.badge-danger { background: #fdecea; color: #c0392b; }
.badge-default { background: #f0f0f0; color: #888; }
</style>

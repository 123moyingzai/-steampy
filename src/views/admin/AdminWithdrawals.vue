<template>
  <div class="admin-withdrawals">
    <div class="toolbar">
      <div class="filter-bar">
        <select v-model="filterStatus" @change="loadWithdrawals">
          <option value="">全部状态</option>
          <option value="pending">待审核</option>
          <option value="success">已通过</option>
          <option value="failed">已拒绝</option>
        </select>
        <span class="stat-pill">待审核 <strong>{{ pendingCount }}</strong> 笔</span>
        <span class="stat-pill">总金额 <strong class="amount">¥{{ totalAmount.toFixed(2) }}</strong></span>
      </div>
      <button class="btn btn-outline" @click="loadWithdrawals">刷新</button>
    </div>

    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>提现单号</th>
            <th>用户ID</th>
            <th>方式</th>
            <th>收款账户</th>
            <th>提现金额</th>
            <th>手续费</th>
            <th>实到</th>
            <th>状态</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in filteredList" :key="w.id">
            <td class="td-monospace">{{ w.order_no }}</td>
            <td class="td-monospace">{{ shortId(w.user_id) }}</td>
            <td>{{ payLabel(w.pay_method) }}</td>
            <td>
              <div>{{ w.real_name }}</div>
              <div class="td-sub">{{ w.account }}</div>
            </td>
            <td class="td-price">¥{{ Number(w.amount).toFixed(2) }}</td>
            <td class="td-sub">-¥{{ Number(w.fee || 0).toFixed(2) }}</td>
            <td class="td-strong">¥{{ Number(w.net_amount || w.amount).toFixed(2) }}</td>
            <td>
              <span :class="['badge', statusBadge(w.status)]">{{ statusLabel(w.status) }}</span>
              <div v-if="w.review_remark" class="td-sub remark">{{ w.review_remark }}</div>
            </td>
            <td>{{ formatTime(w.applied_at) }}</td>
            <td class="td-actions">
              <button v-if="w.status === 'pending'" class="btn btn-small btn-primary" @click="review(w, 'approve')">通过</button>
              <button v-if="w.status === 'pending'" class="btn btn-small btn-danger" @click="review(w, 'reject')">拒绝</button>
              <span v-else class="td-sub">{{ formatTime(w.reviewed_at) }}</span>
            </td>
          </tr>
          <tr v-if="filteredList.length === 0">
            <td colspan="10" class="td-empty">暂无提现记录</td>
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

const filteredList = computed(() => {
  return all.value.filter(w => !filterStatus.value || w.status === filterStatus.value)
})
const pendingCount = computed(() => all.value.filter(w => w.status === 'pending').length)
const totalAmount = computed(() => all.value.reduce((s, w) => s + Number(w.net_amount || w.amount || 0), 0))

async function loadWithdrawals() {
  try {
    const params = filterStatus.value ? `?status=${filterStatus.value}` : ''
    const r = await axios.get('/api/admin/withdrawals' + params)
    all.value = r.data?.data || []
  } catch (e) { console.error('加载提现列表失败', e) }
}

async function review(w: any, action: 'approve' | 'reject') {
  const remark = action === 'approve'
    ? (prompt('审核备注（可选）:', '已核对，打款中') || '')
    : (prompt('拒绝原因:', '信息不符') || '')
  try {
    await axios.put(`/api/admin/withdrawals/${w.id}/review`, { action, remark })
    alert(action === 'approve' ? '已通过，打款中' : '已拒绝，余额已退回用户')
    loadWithdrawals()
  } catch (e: any) { alert('操作失败: ' + (e?.response?.data?.message || e.message)) }
}

function payLabel(m: string) {
  return ({ alipay: '支付宝', bank: '银行卡' } as any)[m] || m || '-'
}
function statusLabel(s: string) {
  return ({ pending: '待审核', success: '已通过', failed: '已拒绝' } as any)[s] || s
}
function statusBadge(s: string) {
  return ({ pending: 'badge-warning', success: 'badge-success', failed: 'badge-danger' } as any)[s] || 'badge-default'
}
function shortId(id: string) { return id ? (id.slice(0, 8) + '...') : '-' }
function formatTime(t: string) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadWithdrawals)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; padding: 16px; background: #fff; border-radius: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.filter-bar { display: flex; gap: 10px; flex: 1; align-items: center; flex-wrap: wrap; }
.filter-bar select { padding: 7px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; min-width: 140px; }
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
.td-strong { font-weight: 600; color: #27ae60; }
.td-sub { font-size: 12px; color: #999; margin-top: 2px; }
.td-sub.remark { color: #c0392b; }
.td-monospace { font-family: monospace; color: #888; font-size: 12px; }
.td-actions { white-space: nowrap; }
.td-empty { text-align: center; padding: 40px; color: #aaa; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-success { background: #e8f8ef; color: #27ae60; }
.badge-warning { background: #fff8e1; color: #e67e22; }
.badge-danger { background: #fdecea; color: #c0392b; }
.badge-default { background: #f0f0f0; color: #888; }
</style>

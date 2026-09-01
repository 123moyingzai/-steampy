<template>
  <div class="admin-orders">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-box">
        <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索订单号、游戏名、买家ID..."
          @input="filterOrders"
        >
      </div>
      <div class="filter-group">
        <select v-model="statusFilter" @change="filterOrders">
          <option value="">全部状态</option>
          <option value="completed">已完成</option>
          <option value="pending">处理中</option>
          <option value="cancelled">已取消</option>
          <option value="failed">失败</option>
        </select>
      </div>
    </div>

    <!-- 汇总 -->
    <div class="summary-bar">
      <div class="summary-item">
        <span class="label">总订单</span>
        <span class="value">{{ filteredOrders.length }}</span>
      </div>
      <div class="summary-item">
        <span class="label">总金额</span>
        <span class="value price">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">已完成</span>
        <span class="value success">{{ completedCount }}</span>
      </div>
      <div class="summary-item">
        <span class="label">处理中</span>
        <span class="value warning">{{ pendingCount }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>买家ID</th>
            <th>游戏</th>
            <th>类型</th>
            <th>金额</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
            <td class="mono">{{ order.order_no || ('ORD' + order.id) }}</td>
            <td class="mono">{{ String(order.buyer_id).slice(0, 8) }}</td>
            <td>
              <div class="game-cell">
                <img v-if="order.game_image" :src="order.game_image" class="game-img">
                <span>{{ order.game_name }}</span>
              </div>
            </td>
            <td>
              <span class="type-tag">{{ order.order_type || 'cdkey' }}</span>
            </td>
            <td class="price">¥{{ order.total_price || order.price }}</td>
            <td>
              <span class="badge" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </span>
            </td>
            <td class="time">{{ formatTime(order.created_at) }}</td>
            <td class="actions">
              <select
                v-model="order._status"
                class="status-select"
                @change="handleStatusChange(order)"
              >
                <option value="">改状态</option>
                <option value="completed">已完成</option>
                <option value="pending">处理中</option>
                <option value="cancelled">已取消</option>
              </select>
              <button class="btn-link danger" @click="handleDelete(order)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredOrders.length === 0" class="empty-state">
        <p>暂无订单数据</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminOrderAPI } from '../../config/admin-api'

const orders = ref<any[]>([])
const filteredOrders = ref<any[]>([])
const searchKeyword = ref('')
const statusFilter = ref('')

const totalAmount = computed(() => {
  return filteredOrders.value.reduce((sum, o) => sum + parseFloat(o.total_price || o.price || 0), 0)
})

const completedCount = computed(() =>
  filteredOrders.value.filter(o => o.status === 'completed').length
)

const pendingCount = computed(() =>
  filteredOrders.value.filter(o => o.status === 'pending').length
)

const loadOrders = async () => {
  const data = await adminOrderAPI.getOrders()
  // 给每条订单加一个 _status 用于下拉框绑定
  orders.value = data.map(o => ({ ...o, _status: '' }))
  filterOrders()
}

const filterOrders = () => {
  let result = [...orders.value]
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    result = result.filter(o =>
      (o.order_no || '').toLowerCase().includes(kw) ||
      (o.game_name || '').toLowerCase().includes(kw) ||
      String(o.buyer_id || '').toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value) {
    result = result.filter(o => o.status === statusFilter.value)
  }
  filteredOrders.value = result
}

const handleStatusChange = async (order: any) => {
  if (!order._status) return
  const result = await adminOrderAPI.updateOrderStatus(order.id, order._status)
  if (result.error) {
    alert('更新失败: ' + result.error)
  } else {
    order.status = order._status
    order._status = ''
  }
}

const handleDelete = async (order: any) => {
  if (!confirm(`确定删除订单 "${order.order_no || order.id}" 吗？`)) return
  const result = await adminOrderAPI.deleteOrder(order.id)
  if (result.error) {
    alert('删除失败: ' + result.error)
  } else {
    alert('删除成功')
    await loadOrders()
  }
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    completed: 'success',
    pending: 'warning',
    cancelled: 'danger',
    failed: 'danger'
  }
  return map[status] || 'default'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    completed: '已完成',
    pending: '处理中',
    cancelled: '已取消',
    failed: '失败'
  }
  return map[status] || status || '未知'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    const d = new Date(timeStr)
    return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch {
    return timeStr
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-orders {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  gap: 12px;
}

.search-box {
  flex: 1;
}

.search-box input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.search-box input:focus {
  outline: none;
  border-color: #f59e0b;
}

.filter-group select {
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
}

.filter-group select:focus {
  outline: none;
  border-color: #f59e0b;
}

.summary-bar {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 16px;
  border-right: 1px solid #f3f4f6;
}

.summary-item:last-child {
  border-right: none;
}

.summary-item .label {
  font-size: 13px;
  color: #6b7280;
}

.summary-item .value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.summary-item .value.price {
  color: #dc2626;
}

.summary-item .value.success {
  color: #10b981;
}

.summary-item .value.warning {
  color: #f59e0b;
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  text-align: left;
  padding: 12px;
  color: #6b7280;
  font-weight: 500;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-size: 12px;
  white-space: nowrap;
}

.data-table td {
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
  vertical-align: middle;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table tr:hover td {
  background: #fafafa;
}

.mono {
  font-family: 'Menlo', monospace;
  font-size: 12px;
  color: #6b7280;
}

.game-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.game-img {
  width: 28px;
  height: 20px;
  border-radius: 3px;
  object-fit: cover;
}

.type-tag {
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
}

.price {
  font-weight: 600;
  color: #dc2626;
}

.time {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}

.actions {
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-select {
  padding: 5px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 5px;
  font-size: 12px;
  background: #fff;
  cursor: pointer;
}

.status-select:focus {
  outline: none;
  border-color: #3b82f6;
}

.btn-link {
  background: none;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link.danger {
  color: #dc2626;
}

.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge.success {
  background: #d1fae5;
  color: #065f46;
}

.badge.warning {
  background: #fef3c7;
  color: #92400e;
}

.badge.danger {
  background: #fee2e2;
  color: #991b1b;
}

.badge.default {
  background: #f3f4f6;
  color: #374151;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
}
</style>

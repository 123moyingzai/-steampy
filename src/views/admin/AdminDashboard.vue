<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statsCards" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bg }">
          <span v-html="stat.icon"></span>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 两栏区域 -->
    <div class="dashboard-grid">
      <!-- 最近订单 -->
      <div class="card">
        <div class="card-header">
          <h3>最近订单</h3>
          <router-link to="/admin/orders" class="view-all">查看全部 →</router-link>
        </div>
        <div class="card-body">
          <table class="data-table" v-if="stats.recentOrders.length > 0">
            <thead>
              <tr>
                <th>订单号</th>
                <th>游戏名</th>
                <th>金额</th>
                <th>状态</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in stats.recentOrders" :key="order.id">
                <td class="mono">{{ order.order_no || ('ORD' + order.id) }}</td>
                <td>{{ order.game_name }}</td>
                <td class="price">¥{{ order.total_price || order.price }}</td>
                <td>
                  <span class="badge" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
                </td>
                <td class="time">{{ formatTime(order.created_at) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-state">
            <p>暂无订单数据</p>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="card">
        <div class="card-header">
          <h3>快捷操作</h3>
        </div>
        <div class="card-body quick-actions">
          <router-link to="/admin/users" class="quick-action">
            <div class="qa-icon" style="background:#3498db"><span>👥</span></div>
            <span>管理用户</span>
          </router-link>
          <router-link to="/admin/games" class="quick-action">
            <div class="qa-icon" style="background:#27ae60"><span>🎮</span></div>
            <span>管理游戏</span>
          </router-link>
          <router-link to="/admin/orders" class="quick-action">
            <div class="qa-icon" style="background:#f39c12"><span>📋</span></div>
            <span>处理订单</span>
          </router-link>
          <router-link to="/admin/announcements" class="quick-action">
            <div class="qa-icon" style="background:#7f8c8d"><span>📢</span></div>
            <span>发布公告</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminStatsAPI } from '../../config/admin-api'

const stats = ref({
  totalUsers: 0,
  totalGames: 0,
  totalOrders: 0,
  totalAnnouncements: 0,
  totalRevenue: 0,
  recentOrders: [] as any[]
})

const statsCards = [
  {
    label: '总用户数',
    get value() { return stats.value.totalUsers },
    icon: '<path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"/>',
    bg: '#3498db'
  },
  {
    label: '游戏数量',
    get value() { return stats.value.totalGames },
    icon: '<path d="M21 6H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-10 7H8v3H6v-3H3v-2h3V10h2v3h3v2zm4.5 2c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm4-3c-.83 0-1.5-.67-1.5-1.5S18.67 9 19.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/>',
    bg: '#27ae60'
  },
  {
    label: '总订单数',
    get value() { return stats.value.totalOrders },
    icon: '<path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>',
    bg: '#f39c12'
  },
  {
    label: '总收入 (¥)',
    get value() { return stats.value.totalRevenue.toFixed(2) },
    icon: '<path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>',
    bg: '#e74c3c'
  }
]

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

const loadStats = async () => {
  const result = await adminStatsAPI.getDashboardStats()
  stats.value = result
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon span {
  display: flex;
  width: 28px;
  height: 28px;
}

.stat-icon svg {
  width: 28px;
  height: 28px;
  fill: #fff;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

/* 两栏 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.view-all {
  color: #3498db;
  text-decoration: none;
  font-size: 13px;
}

.view-all:hover {
  text-decoration: underline;
}

.card-body {
  padding: 16px 20px;
}

/* 数据表格 */
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  text-align: left;
  padding: 10px 12px;
  color: #666;
  font-weight: 500;
  border-bottom: 1px solid #eee;
  font-size: 12px;
}

.data-table td {
  padding: 12px;
  border-bottom: 1px solid #f5f5f5;
  color: #333;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table .mono {
  font-family: 'Menlo', monospace;
  font-size: 12px;
  color: #666;
}

.data-table .price {
  font-weight: 600;
  color: #c0392b;
}

.data-table .time {
  font-size: 12px;
  color: #999;
}

.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge.success {
  background: #d5f5e3;
  color: #1e8449;
}

.badge.warning {
  background: #fdebd0;
  color: #d68910;
}

.badge.danger {
  background: #fee2e2;
  color: #c0392b;
}

.badge.default {
  background: #eee;
  color: #333;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 12px;
  background: #f5f5f5;
  border-radius: 10px;
  text-decoration: none;
  color: #333;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.quick-action:hover {
  background: #eee;
  transform: translateY(-2px);
}

.qa-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

/* 响应式 */
@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>

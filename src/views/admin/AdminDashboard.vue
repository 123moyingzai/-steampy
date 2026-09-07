﻿﻿﻿<template>
  <div class="admin-dashboard">
    <!-- 第一行：7 张主卡片 + 2 张今日卡片 -->
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
      <div class="stat-card stat-today">
        <div class="stat-today-header">
          <span class="stat-today-tag">今日</span>
          <span class="stat-today-date">{{ todayStr }}</span>
        </div>
        <div class="stat-today-body">
          <div class="stat-sub">
            <div class="stat-sub-value">{{ stats.todayOrderCount || 0 }}</div>
            <div class="stat-sub-label">新订单</div>
          </div>
          <div class="stat-sub-divider"></div>
          <div class="stat-sub">
            <div class="stat-sub-value price">¥{{ Number(stats.todayRevenue || 0).toFixed(2) }}</div>
            <div class="stat-sub-label">今日收入</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 第二行：最近订单 + 订单状态分布 + 热销 Top 5 -->
    <div class="dashboard-grid">
      <!-- 最近订单 -->
      <div class="card card-wide">
        <div class="card-header">
          <h3>最近订单（{{ (stats.recentOrders || []).length }}）</h3>
          <router-link to="/admin/orders" class="view-all">查看全部 →</router-link>
        </div>
        <div class="card-body">
          <table class="data-table" v-if="stats.recentOrders?.length > 0">
            <thead>
              <tr>
                <th>订单号</th>
                <th>游戏</th>
                <th>金额</th>
                <th>类型</th>
                <th>状态</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in stats.recentOrders" :key="order.id">
                <td class="mono">{{ order.order_no || ('ORD' + order.id.slice(0, 8)) }}</td>
                <td>{{ order.game_name || '-' }}</td>
                <td class="price">¥{{ Number(order.total_price || order.price || 0).toFixed(2) }}</td>
                <td>
                  <span class="type-tag">{{ order.order_type === 'py' ? 'PY代购' : 'CDKey' }}</span>
                </td>
                <td>
                  <span class="badge" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
                </td>
                <td class="time">{{ formatTime(order.created_at) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-state"><p>暂无订单数据</p></div>
        </div>
      </div>

      <!-- 订单状态分布 -->
      <div class="card">
        <div class="card-header"><h3>订单状态分布</h3></div>
        <div class="card-body status-breakdown">
          <div v-for="(cnt, key) in statusBreakdown" :key="key" class="status-row">
            <span class="status-label">{{ getStatusText(key) }}</span>
            <div class="status-bar-bg">
              <div class="status-bar-fill" :class="getStatusClass(key)"
                   :style="{ width: getBarWidth(cnt) + '%' }"></div>
            </div>
            <span class="status-count">{{ cnt }}</span>
          </div>
          <div v-if="!hasAnyOrders" class="empty-state"><p>暂无数据</p></div>
        </div>
      </div>
    </div>

    <!-- 第三行：热销榜 + 快捷操作 -->
    <div class="dashboard-grid">
      <!-- 热销 Top 5 -->
      <div class="card">
        <div class="card-header"><h3>热销 Top 5 游戏</h3></div>
        <div class="card-body top-games">
          <div v-for="(g, idx) in stats.topGames" :key="g.gameName" class="top-game-row">
            <span class="rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
            <span class="top-game-name">{{ g.gameName }}</span>
            <span class="top-game-count">{{ g.orderCount }} 单</span>
          </div>
          <div v-if="!stats.topGames?.length" class="empty-state"><p>暂无销售数据</p></div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="card">
        <div class="card-header"><h3>快捷操作</h3></div>
        <div class="card-body quick-actions">
          <router-link to="/admin/users" class="quick-action">
            <div class="qa-icon" style="background:#3498db"><span>👥</span></div>
            <span>用户管理</span>
          </router-link>
          <router-link to="/admin/games" class="quick-action">
            <div class="qa-icon" style="background:#27ae60"><span>🎮</span></div>
            <span>游戏管理</span>
          </router-link>
          <router-link to="/admin/orders" class="quick-action">
            <div class="qa-icon" style="background:#f39c12"><span>📋</span></div>
            <span>订单管理</span>
          </router-link>
          <router-link to="/admin/withdrawals" class="quick-action">
            <div class="qa-icon" style="background:#e67e22"><span>💰</span></div>
            <span>提现审核</span>
            <span v-if="stats.pendingWithdrawals > 0" class="qa-badge">{{ stats.pendingWithdrawals }}</span>
          </router-link>
          <router-link to="/admin/reviews" class="quick-action">
            <div class="qa-icon" style="background:#9b59b6"><span>💬</span></div>
            <span>评测审核</span>
            <span v-if="stats.pendingReviews > 0" class="qa-badge">{{ stats.pendingReviews }}</span>
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const stats = ref<any>({
  userCount: 0, gameCount: 0, orderCount: 0,
  availableListings: 0, pendingReviews: 0, pendingWithdrawals: 0,
  totalRevenue: 0, todayOrderCount: 0, todayRevenue: 0,
  recentOrders: [], topGames: [], orderStatusBreakdown: {}
})

const statsCards = computed(() => [
  { label: '总用户数',      get value() { return stats.value.userCount },           bg: '#3498db', icon: '<path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"/>' },
  { label: '游戏数量',      get value() { return stats.value.gameCount },           bg: '#27ae60', icon: '<path d="M21 6H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-10 7H8v3H6v-3H3v-2h3V10h2v3h3v2zm4.5 2c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm4-3c-.83 0-1.5-.67-1.5-1.5S18.67 9 19.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/>' },
  { label: '在售上架',      get value() { return stats.value.availableListings },   bg: '#1abc9c', icon: '<path d="M3 3h18v4H3V3zm0 5h18v13H3V8zm3 3v2h12v-2H6zm0 4v2h8v-2H6z"/>' },
  { label: '总收入 (¥)',    get value() { return Number(stats.value.totalRevenue || 0).toFixed(2) }, bg: '#e74c3c', icon: '<path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>' },
  { label: '总订单数',      get value() { return stats.value.orderCount },          bg: '#f39c12', icon: '<path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>' },
  { label: '待审核提现',    get value() { return stats.value.pendingWithdrawals },  bg: '#e67e22', icon: '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>' },
  { label: '待审核评测',    get value() { return stats.value.pendingReviews },      bg: '#9b59b6', icon: '<path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z"/>' }
])

const todayStr = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const statusBreakdown = computed(() => stats.value.orderStatusBreakdown || {})
const hasAnyOrders = computed(() => Object.values(statusBreakdown.value).some((v: any) => v > 0))
const statusBreakdownTotal = computed(() => Object.values(statusBreakdown.value).reduce((s: number, v: any) => s + v, 0))
function getBarWidth(cnt: number) {
  const t = statusBreakdownTotal.value
  if (!t) return 0
  return Math.max(4, Math.round((cnt / t) * 100))
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = { completed: 'success', pending: 'warning', cancelled: 'danger', failed: 'danger' }
  return map[status] || 'default'
}
const getStatusText = (status: string) => {
  const map: Record<string, string> = { completed: '已完成', pending: '处理中', cancelled: '已取消', failed: '失败' }
  return map[status] || status || '未知'
}
const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    const d = new Date(timeStr)
    return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch { return timeStr }
}

const loadStats = async () => {
  try {
    const r = await axios.get('/api/admin/stats')
    Object.assign(stats.value, r.data?.data || {})
  } catch (e) { console.error('加载统计失败', e) }
}

onMounted(loadStats)
</script>

<style scoped>
.admin-dashboard { display: flex; flex-direction: column; gap: 20px; }

/* === 统计卡片网格 === */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06); transition: transform 0.2s, box-shadow 0.2s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-icon span { display: flex; width: 24px; height: 24px; }
.stat-icon svg { width: 24px; height: 24px; fill: #fff; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: #333; line-height: 1.2; }
.stat-label { font-size: 12px; color: #666; margin-top: 4px; }

/* 今日卡片 */
.stat-today { flex-direction: column; gap: 12px; align-items: stretch; background: linear-gradient(135deg, #f0f4ff 0%, #fff 60%); border: 1px solid #e0e8ff; }
.stat-today-header { display: flex; justify-content: space-between; align-items: center; }
.stat-today-tag { background: #4a6cf7; color: #fff; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.stat-today-date { font-size: 12px; color: #888; }
.stat-today-body { display: flex; align-items: center; gap: 12px; }
.stat-sub { flex: 1; text-align: center; }
.stat-sub-value { font-size: 22px; font-weight: 700; color: #333; }
.stat-sub-value.price { color: #e74c3c; }
.stat-sub-label { font-size: 12px; color: #888; margin-top: 2px; }
.stat-sub-divider { width: 1px; height: 36px; background: #ddd; }

/* === 两栏网格 === */
.dashboard-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
.card { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); overflow: hidden; }
.card-wide { grid-column: span 1; }
.card-header { padding: 14px 18px; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between; }
.card-header h3 { margin: 0; font-size: 15px; font-weight: 600; color: #333; }
.view-all { color: #4a6cf7; text-decoration: none; font-size: 13px; }
.view-all:hover { text-decoration: underline; }
.card-body { padding: 14px 18px; }

/* === 最近订单表格 === */
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table th { text-align: left; padding: 10px 12px; color: #666; font-weight: 500; background: #fafbfc; border-bottom: 1px solid #eee; font-size: 12px; }
.data-table td { padding: 10px 12px; border-bottom: 1px solid #f5f5f5; color: #333; }
.data-table tr:last-child td { border-bottom: none; }
.data-table .mono { font-family: Menlo, monospace; font-size: 12px; color: #666; }
.data-table .price { font-weight: 600; color: #c0392b; }
.data-table .time { font-size: 12px; color: #999; white-space: nowrap; }
.data-table .type-tag { background: #eee; padding: 2px 8px; border-radius: 4px; font-size: 11px; color: #666; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge.success { background: #d5f5e3; color: #1e8449; }
.badge.warning { background: #fdebd0; color: #d68910; }
.badge.danger { background: #fee2e2; color: #c0392b; }
.badge.default { background: #eee; color: #555; }

.empty-state { text-align: center; padding: 30px; color: #aaa; font-size: 14px; }

/* === 订单状态分布 === */
.status-breakdown { display: flex; flex-direction: column; gap: 12px; }
.status-row { display: grid; grid-template-columns: 60px 1fr 36px; gap: 10px; align-items: center; }
.status-label { font-size: 13px; color: #555; }
.status-bar-bg { background: #f0f0f0; height: 10px; border-radius: 5px; overflow: hidden; }
.status-bar-fill { height: 100%; border-radius: 5px; transition: width 0.3s; min-width: 4px; }
.status-bar-fill.success { background: #27ae60; }
.status-bar-fill.warning { background: #f39c12; }
.status-bar-fill.danger { background: #e74c3c; }
.status-bar-fill.default { background: #999; }
.status-count { font-size: 13px; font-weight: 600; color: #333; text-align: right; }

/* === 热销 Top 5 === */
.top-games { display: flex; flex-direction: column; gap: 8px; }
.top-game-row { display: grid; grid-template-columns: 28px 1fr 48px; gap: 10px; align-items: center; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.top-game-row:last-child { border-bottom: none; }
.rank { width: 22px; height: 22px; border-radius: 6px; background: #eee; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; color: #888; }
.rank-1 { background: linear-gradient(135deg, #ffd700, #ffb300); color: #fff; }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); color: #fff; }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b36b20); color: #fff; }
.rank-4, .rank-5 { background: #f0f0f0; color: #888; }
.top-game-name { font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.top-game-count { font-size: 12px; color: #888; text-align: right; font-weight: 600; }

/* === 快捷操作 === */
.quick-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.quick-action { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 8px; background: #f7f8fa; border-radius: 10px; text-decoration: none; color: #333; font-size: 12px; font-weight: 500; transition: all 0.2s; position: relative; }
.quick-action:hover { background: #eef1f8; transform: translateY(-2px); }
.qa-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.qa-badge { position: absolute; top: 6px; right: 6px; background: #e74c3c; color: #fff; border-radius: 10px; font-size: 11px; padding: 1px 7px; font-weight: 700; }

/* 响应式 */
@media (max-width: 1100px) {
  .dashboard-grid { grid-template-columns: 1fr; }
  .quick-actions { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 600px) {
  .quick-actions { grid-template-columns: repeat(2, 1fr); }
}
</style>

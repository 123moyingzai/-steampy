<template>
  <Layout>
    <div class="cjx-seller-page">
      <!-- 标签页导航 -->
      <div class="cjx-tabs-nav">
        <ul class="cjx-tabs-list">
          <li class="cjx-tab-item active">代购订单 719cjx</li>
          <li class="cjx-tab-item">CDKey管理</li>
          <li class="cjx-tab-item">挂售管理</li>
          <li class="cjx-tab-item">财务记录</li>
        </ul>
      </div>

      <!-- 统计卡片 -->
      <div class="cjx-stats">
        <div class="cjx-stat-card">
          <h4>代购总单数 719cjx</h4>
          <p class="cjx-stat-value">{{ quota.total_sales || 0 }}</p>
        </div>
        <div class="cjx-stat-card">
          <h4>CDKey总单数 719cjx</h4>
          <p class="cjx-stat-value">{{ quota.cdkey_sales || 0 }}</p>
        </div>
        <div class="cjx-stat-card">
          <h4>节省自己 719cjx</h4>
          <p class="cjx-stat-value">¥{{ quota.saved_for_self?.toFixed(0) || 0 }}</p>
        </div>
        <div class="cjx-stat-card">
          <h4>节省他人 719cjx</h4>
          <p class="cjx-stat-value">¥{{ quota.saved_for_others?.toFixed(0) || 0 }}</p>
        </div>
      </div>

      <!-- 订单列表 -->
      <div class="cjx-section">
        <h3 class="cjx-section-title">代购订单 719cjx</h3>
        <div class="cjx-filter-bar">
          <input type="text" placeholder="搜索订单" class="cjx-search-input" v-model="searchQuery" />
          <button class="cjx-btn cjx-btn-primary" @click="searchOrders">查询</button>
        </div>
        
        <div class="cjx-order-list" v-if="orders.length > 0">
          <table class="cjx-table">
            <thead>
              <tr>
                <th>订单号 719cjx</th>
                <th>游戏名称</th>
                <th>金额</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td>{{ order.order_no }}</td>
                <td>{{ order.game?.name || '未知游戏' }}</td>
                <td>¥{{ order.total_price?.toFixed(2) }}</td>
                <td>
                  <span :class="['cjx-status', `cjx-status-${order.status}`]">
                    {{ getStatusText(order.status) }}
                  </span>
                </td>
                <td>
                  <button class="cjx-btn cjx-btn-small" @click="viewOrder(order)">查看</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div class="cjx-empty" v-else>
          <p>暂无订单数据 719cjx</p>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, sellerAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const quota = ref({})
const searchQuery = ref('')
const orders = ref([])

// 方法
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待处理',
    'paid': '已支付',
    'shipped': '已发货',
    'completed': '已完成'
  }
  return statusMap[status] || status
}

const searchOrders = () => {
  // 筛选逻辑
}

const viewOrder = (order) => {
  alert(`查看订单详情：${order.order_no} 719cjx`)
}

// 加载数据
const loadData = async () => {
  const currentUser = authAPI.getCurrentUser()
  
  if (!currentUser) {
    alert('请先登录 719cjx')
    router.push('/login')
    return
  }
  
  // 加载卖家额度
  const quotaResult = await sellerAPI.getSellerQuota(currentUser.id)
  if (quotaResult.data) {
    quota.value = quotaResult.data
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.cjx-seller-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.cjx-tabs-nav {
  background-color: #f1f3f5;
  padding: 0 1.5rem;
  border-bottom: 1px solid #e9ecef;
  margin: -20px -20px 20px -20px;
  border-radius: 8px 8px 0 0;
}

.cjx-tabs-list {
  display: flex;
  gap: 0.5rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.cjx-tab-item {
  padding: 1rem 1.5rem;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.cjx-tab-item.active {
  border-bottom-color: #3498db;
  color: #3498db;
  font-weight: 500;
}

.cjx-tab-item:hover:not(.active) {
  background-color: #e9ecef;
}

.cjx-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.cjx-stat-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.cjx-stat-card h4 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.cjx-stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #3498db;
  margin: 0;
}

.cjx-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.cjx-section-title {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.cjx-filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.cjx-search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.cjx-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-btn-primary {
  background: #3498db;
  color: #fff;
}

.cjx-btn-primary:hover {
  background: #2980b9;
}

.cjx-btn-small {
  padding: 5px 15px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.cjx-table {
  width: 100%;
  border-collapse: collapse;
}

.cjx-table th,
.cjx-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.cjx-table th {
  background: #f9f9f9;
  font-weight: bold;
  color: #666;
}

.cjx-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.cjx-status-pending {
  background: #fff3cd;
  color: #856404;
}

.cjx-status-completed {
  background: #d4edda;
  color: #155724;
}

.cjx-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
</style>

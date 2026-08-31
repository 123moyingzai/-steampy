<template>
  <Layout>
    <div class="cjx-buyer-page">
      <!-- 标签页导航 -->
      <div class="cjx-tabs-nav">
        <ul class="cjx-tabs-list">
          <li 
            class="cjx-tab-item" 
            :class="{ active: activeTab === 'py' }"
            @click="activeTab = 'py'"
          >PY代购 719cjx</li>
          <li 
            class="cjx-tab-item" 
            :class="{ active: activeTab === 'cdkey' }"
            @click="activeTab = 'cdkey'"
          >CDKey 719cjx</li>
          <li 
            class="cjx-tab-item" 
            :class="{ active: activeTab === 'request' }"
            @click="activeTab = 'request'"
          >求购 719cjx</li>
          <li 
            class="cjx-tab-item" 
            :class="{ active: activeTab === 'collect' }"
            @click="activeTab = 'collect'"
          >代领 719cjx</li>
        </ul>
      </div>

      <!-- PY代购内容 -->
      <div v-show="activeTab === 'py'" class="cjx-tab-content">
        <div class="cjx-filter-bar">
          <input type="text" placeholder="搜索游戏名称" class="cjx-search-input" v-model="searchQuery" />
          <select class="cjx-select" v-model="statusFilter">
            <option value="">全部状态 719cjx</option>
            <option value="pending">待支付</option>
            <option value="paid">已支付</option>
            <option value="shipped">已发货</option>
            <option value="completed">已完成</option>
          </select>
          <button class="cjx-btn cjx-btn-primary" @click="searchOrders">查询</button>
        </div>

        <div class="cjx-order-list" v-if="filteredOrders.length > 0">
          <table class="cjx-table">
            <thead>
              <tr>
                <th>订单号 719cjx</th>
                <th>下单时间</th>
                <th>游戏名称</th>
                <th>单价</th>
                <th>数量</th>
                <th>总价</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in filteredOrders" :key="order.id">
                <td>{{ order.order_no }}</td>
                <td>{{ formatDate(order.created_at) }}</td>
                <td>{{ order.game_name || '未知游戏' }}</td>
                <td>¥{{ (order.price || 0).toFixed(2) }}</td>
                <td>{{ order.quantity || 1 }}</td>
                <td>¥{{ (order.total_price || 0).toFixed(2) }}</td>
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
          <button class="cjx-btn cjx-btn-primary" @click="$router.push('/cdkey')">去购买</button>
        </div>
      </div>

      <!-- CDKey订单标签页 -->
      <div v-show="activeTab === 'cdkey'" class="cjx-tab-content">
        <div class="cjx-filter-bar">
          <input type="text" placeholder="搜索订单号或游戏名称" class="cjx-search-input" v-model="cdkeySearchQuery" />
          <select class="cjx-select" v-model="cdkeyStatusFilter">
            <option value="">全部状态 719cjx</option>
            <option value="completed">交易完成</option>
            <option value="pending">待支付</option>
            <option value="cancelled">已取消</option>
          </select>
        </div>

        <div class="cjx-order-list" v-if="filteredCdkeyOrders.length > 0">
          <table class="cjx-table cjx-order-table">
            <thead>
              <tr>
                <th>订单号 719cjx</th>
                <th>购买日期</th>
                <th>游戏名称</th>
                <th>订单金额</th>
                <th>卖家ID</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in filteredCdkeyOrders" :key="order.id">
                <td>{{ order.order_no }}</td>
                <td>{{ formatDateTime(order.created_at) }}</td>
                <td>{{ order.game_name || '未知游戏' }}</td>
                <td class="cjx-price">¥{{ order.total_price?.toFixed(2) }}</td>
                <td>Steam PY官方</td>
                <td>
                  <span :class="['cjx-status', `cjx-status-${order.status}`]">
                    {{ getOrderStatusText(order.status) }}
                  </span>
                </td>
                <td>
                  <button class="cjx-btn cjx-btn-small" @click="viewOrderDetail(order)">查看</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div class="cjx-empty" v-else>
          <p>暂无CDKey订单 719cjx</p>
          <button class="cjx-btn cjx-btn-primary" @click="$router.push('/cdkey')">去购买</button>
        </div>
      </div>

      <!-- 求购标签页 -->
      <div v-show="activeTab === 'request'" class="cjx-tab-content">
        <div class="cjx-filter-bar">
          <button class="cjx-btn cjx-btn-primary" @click="showRequestForm = true">发布求购 719cjx</button>
        </div>
        <div class="cjx-empty">
          <p>暂无求购信息 719cjx</p>
        </div>
      </div>

      <!-- 代领标签页 -->
      <div v-show="activeTab === 'collect'" class="cjx-tab-content">
        <div class="cjx-empty">
          <p>暂无代领订单 719cjx</p>
        </div>
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <div class="cjx-modal" v-if="selectedOrder" @click.self="closeOrderDetail">
      <div class="cjx-modal-content">
        <div class="cjx-modal-header">
          <h3>订单详情 719cjx</h3>
          <button class="cjx-modal-close" @click="closeOrderDetail">×</button>
        </div>
        <div class="cjx-modal-body">
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">订单号</span>
            <span class="cjx-detail-value">{{ selectedOrder.order_no }}</span>
          </div>
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">购买时间</span>
            <span class="cjx-detail-value">{{ formatDateTime(selectedOrder.created_at) }}</span>
          </div>
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">游戏名称</span>
            <span class="cjx-detail-value">{{ selectedOrder.game_name }}</span>
          </div>
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">单价</span>
            <span class="cjx-detail-value">¥{{ selectedOrder.unit_price?.toFixed(2) }}</span>
          </div>
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">数量</span>
            <span class="cjx-detail-value">{{ selectedOrder.quantity }}</span>
          </div>
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">总价</span>
            <span class="cjx-detail-value cjx-price">¥{{ selectedOrder.total_price?.toFixed(2) }}</span>
          </div>
          <div class="cjx-detail-row">
            <span class="cjx-detail-label">订单状态</span>
            <span class="cjx-detail-value">{{ getOrderStatusText(selectedOrder.status) }}</span>
          </div>
          <div class="cjx-detail-row cjx-cdkey-row" v-if="selectedOrder.cdkey">
            <span class="cjx-detail-label">游戏 CDKey</span>
            <div class="cjx-cdkey-box">
              <span class="cjx-cdkey-value">{{ selectedOrder.cdkey }}</span>
              <button class="cjx-btn-copy" @click="copyCdkeyFromOrder">复制 719cjx</button>
            </div>
          </div>
        </div>
        <div class="cjx-modal-footer">
          <button class="cjx-btn cjx-btn-primary" @click="closeOrderDetail">确定</button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, orderAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const activeTab = ref('py')
const searchQuery = ref('')
const statusFilter = ref('')
const orders = ref([])
const cdkeySearchQuery = ref('')
const cdkeyStatusFilter = ref('')
const cdkeyOrders = ref([])
const showRequestForm = ref(false)
const selectedOrder = ref(null)

// 计算属性
// PY代购订单 - 显示order_type为'py'或没有cdkey的订单
const filteredOrders = computed(() => {
  let result = orders.value
  
  // 只显示已完成的订单，且是PY代购类型
  result = result.filter(o => o.status === 'completed' && (o.order_type === 'py' || !o.order_type || !o.cdkey))
  
  if (searchQuery.value) {
    result = result.filter(o => o.game_name?.includes(searchQuery.value))
  }
  
  if (statusFilter.value) {
    result = result.filter(o => o.status === statusFilter.value)
  }
  
  return result
})

// CDKey订单 - 显示order_type为'cdkey'的订单
const filteredCdkeyOrders = computed(() => {
  let result = cdkeyOrders.value
  
  // 只显示已完成的订单，且是CDKey类型
  result = result.filter(o => o.status === 'completed' && o.order_type === 'cdkey')
  
  if (cdkeySearchQuery.value) {
    const query = cdkeySearchQuery.value.toLowerCase()
    result = result.filter(o => 
      o.order_no?.toLowerCase().includes(query) || 
      o.game_name?.toLowerCase().includes(query)
    )
  }
  
  if (cdkeyStatusFilter.value) {
    result = result.filter(o => o.status === cdkeyStatusFilter.value)
  }
  
  // 排序（最新的在前）
  result.sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
  
  return result
})

// 方法
const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const getOrderStatusText = (status) => {
  const statusMap = {
    'completed': '交易完成',
    'pending': '待支付',
    'cancelled': '已取消',
    'shipped': '已发货',
    'paid': '已支付'
  }
  return statusMap[status] || status
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
}

const copyCdkeyFromOrder = () => {
  if (!selectedOrder.value?.cdkey) return
  navigator.clipboard.writeText(selectedOrder.value.cdkey).then(() => {
    alert('CDKey已复制到剪贴板 719cjx')
  }).catch(() => {
    const textarea = document.createElement('textarea')
    textarea.value = selectedOrder.value.cdkey
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    alert('CDKey已复制到剪贴板 719cjx')
  })
}

const closeOrderDetail = () => {
  selectedOrder.value = null
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待支付',
    'paid': '已支付',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消',
    'refunded': '已退款'
  }
  return statusMap[status] || status
}

const searchOrders = () => {
  // 筛选逻辑在computed中处理
}

const viewOrder = (order) => {
  alert(`查看订单详情：${order.order_no} 719cjx`)
}

// 加载数据 - 从 Supabase 数据库获取
const loadData = async () => {
  const currentUser = authAPI.getCurrentUser()
  const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'

  if (!currentUser) {
    alert('请先登录 719cjx')
    router.push('/login')
    return
  }

  try {
    // 从数据库加载订单
    const result = await orderAPI.getOrders(userId)
    
    if (result.data) {
      orders.value = result.data
      cdkeyOrders.value = result.data
      console.log(`✓ 从数据库加载了 ${result.data.length} 条订单`)
    }
  } catch (error) {
    console.error('从数据库加载订单失败，尝试本地存储:', error)
    
    // 降级到 localStorage
    const savedOrders = localStorage.getItem('orders')
    if (savedOrders) {
      orders.value = JSON.parse(savedOrders)
      cdkeyOrders.value = JSON.parse(savedOrders)
    }
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.cjx-buyer-page {
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

.cjx-select {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
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

.cjx-btn-small:hover {
  background: #e0e0e0;
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

.cjx-status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.cjx-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.cjx-empty p {
  margin-bottom: 20px;
}

/* CDKey相关样式 */
.cjx-cdkey-code {
  font-family: 'Courier New', monospace;
  background: #f8f9fa;
  padding: 5px 10px;
  border-radius: 4px;
  margin-right: 10px;
  font-size: 13px;
}

.cjx-status-activated {
  background: #d4edda;
  color: #155724;
}

.cjx-status-pending {
  background: #fff3cd;
  color: #856404;
}

/* 订单表格样式 */
.cjx-order-table th {
  background: #000;
  color: #fff;
}

.cjx-price {
  color: #e74c3c;
  font-weight: bold;
}

/* 弹窗样式 */
.cjx-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cjx-modal-content {
  background: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.cjx-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
}

.cjx-modal-header h3 {
  margin: 0;
  font-size: 16px;
}

.cjx-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.cjx-modal-close:hover {
  color: #333;
}

.cjx-modal-body {
  padding: 20px;
}

.cjx-detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.cjx-detail-row:last-child {
  border-bottom: none;
}

.cjx-cdkey-row {
  flex-direction: column;
  gap: 10px;
}

.cjx-detail-label {
  color: #666;
}

.cjx-detail-value {
  color: #333;
  font-weight: 500;
}

.cjx-cdkey-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8f9fa;
  padding: 12px 15px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.cjx-cdkey-value {
  font-family: 'Courier New', monospace;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  letter-spacing: 1px;
  flex: 1;
}

.cjx-btn-copy {
  padding: 6px 12px;
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.cjx-btn-copy:hover {
  background: #2980b9;
}

.cjx-modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  text-align: center;
}
</style>

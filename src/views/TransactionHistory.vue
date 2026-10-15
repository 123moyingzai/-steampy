<template>
  <Layout>
    <div class="cjx-transaction-page">
      <h2 class="cjx-page-title">余额明细</h2>

      <!-- 钱包余额显示 -->
      <div class="cjx-balance-header">
        <span>钱包余额：</span>
        <span class="cjx-balance-amount">¥{{ currentBalance.toFixed(2) }}</span>
        <span class="cjx-pending-balance">待处理余额：0</span>
      </div>

      <!-- 筛选栏 -->
      <div class="cjx-filter-bar">
        <div class="cjx-filter-group">
          <label>种类：</label>
          <select v-model="filterType" class="cjx-select">
            <option value="">全部类型</option>
            <option value="recharge">充值</option>
            <option value="purchase">购买</option>
            <option value="refund">退款</option>
          </select>
        </div>
        <div class="cjx-filter-group">
          <label>日期：</label>
          <input type="date" v-model="startDate" class="cjx-date-input" placeholder="开始时间" />
          <span class="cjx-date-separator">-</span>
          <input type="date" v-model="endDate" class="cjx-date-input" placeholder="结束时间" />
        </div>
        <button class="cjx-btn cjx-btn-confirm" @click="searchRecords">确定</button>
        <button class="cjx-btn cjx-btn-reset" @click="resetFilters">重置</button>
      </div>

      <!-- 交易列表 -->
      <div class="cjx-transaction-list">
        <div class="cjx-list-header">
          <span class="cjx-col-order">订单号</span>
          <span class="cjx-col-date">日期</span>
          <span class="cjx-col-type">订单类型</span>
          <span class="cjx-col-amount">金额</span>
        </div>

        <div class="cjx-list-body" v-if="filteredTransactions.length > 0">
          <div 
            v-for="item in filteredTransactions" 
            :key="item.id"
            class="cjx-transaction-item"
          >
            <span class="cjx-col-order">{{ item.order_no || item.id }}</span>
            <span class="cjx-col-date">{{ formatDateTime(item.created_at) }}</span>
            <span class="cjx-col-type">{{ getTypeText(item.type) }}</span>
            <span class="cjx-col-amount" :class="item.amount > 0 ? 'cjx-amount-positive' : 'cjx-amount-negative'">
              {{ item.amount > 0 ? '' : '-' }}¥{{ Math.abs(item.amount).toFixed(2) }}
            </span>
          </div>
        </div>

        <div class="cjx-empty" v-else>
          <p>暂无明细记录</p>
        </div>
      </div>

      <!-- 分页 -->
      <div class="cjx-pagination" v-if="totalPages > 1">
        <button 
          class="cjx-page-btn" 
          :disabled="currentPage === 1"
          @click="currentPage--"
        >&lt;</button>
        <span class="cjx-page-num" :class="{ active: currentPage === 1 }">1</span>
        <span v-if="totalPages > 1" class="cjx-page-num" :class="{ active: currentPage === 2 }">2</span>
        <button 
          class="cjx-page-btn" 
          :disabled="currentPage === totalPages"
          @click="currentPage++"
        >&gt;</button>
        <span class="cjx-page-size">10条/页</span>
        <span class="cjx-page-jump">跳至</span>
        <input type="number" v-model.number="jumpPage" class="cjx-jump-input" min="1" :max="totalPages" />
        <span class="cjx-page-total">页</span>
      </div>
    </div>

  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, transactionAPI, walletAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const transactions = ref([])
const currentBalance = ref(0)
const filterType = ref('')
const startDate = ref('')
const endDate = ref('')
const currentPage = ref(1)
const pageSize = 10
const jumpPage = ref(1)

// 筛选后的交易记录
const filteredTransactions = computed(() => {
  let result = [...transactions.value]
  
  // 按类型筛选
  if (filterType.value) {
    result = result.filter(item => item.type === filterType.value)
  }
  
  // 按日期范围筛选
  if (startDate.value) {
    const start = new Date(startDate.value)
    result = result.filter(item => new Date(item.created_at) >= start)
  }
  if (endDate.value) {
    const end = new Date(endDate.value)
    end.setHours(23, 59, 59, 999)
    result = result.filter(item => new Date(item.created_at) <= end)
  }
  
  // 排序（最新的在前）
  result.sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
  
  // 分页
  const start = (currentPage.value - 1) * pageSize
  return result.slice(start, start + pageSize)
})

// 总页数
const totalPages = computed(() => {
  let result = [...transactions.value]
  if (filterType.value) {
    result = result.filter(item => item.type === filterType.value)
  }
  if (startDate.value) {
    const start = new Date(startDate.value)
    result = result.filter(item => new Date(item.created_at) >= start)
  }
  if (endDate.value) {
    const end = new Date(endDate.value)
    end.setHours(23, 59, 59, 999)
    result = result.filter(item => new Date(item.created_at) <= end)
  }
  return Math.ceil(result.length / pageSize) || 1
})

// 方法
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

const getTypeText = (type) => {
  const typeMap: Record<string, string> = {
    'recharge': '充值',
    'purchase': '购买',
    'refund': '退款',
    'bonus': '赠送',
    '消费': '购买',
    '充值': '充值'
  }
  return typeMap[type] || type || '其他'
}

const searchRecords = () => {
  currentPage.value = 1
  jumpPage.value = 1
}

const resetFilters = () => {
  filterType.value = ''
  startDate.value = ''
  endDate.value = ''
  currentPage.value = 1
  jumpPage.value = 1
}

// 加载数据 - 从 Supabase 数据库获取
const loadData = async () => {
  const currentUser = authAPI.getCurrentUser()
  const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  
  try {
    // 从数据库加载余额
    const balanceResult = await walletAPI.getBalance(userId)
    if (balanceResult.data) {
      // data 可能是对象 { balance: 123.45 } 或直接数字
      const bal = balanceResult.data.balance ?? balanceResult.data
      currentBalance.value = parseFloat(bal) || 0
    }
    
    // 从数据库加载交易记录
    const transactionResult = await transactionAPI.getTransactions(userId)
    if (transactionResult.data) {
      transactions.value = transactionResult.data
    }
    
    console.log(`✓ 从数据库加载了 ${transactions.value.length} 条交易记录`)
  } catch (error) {
    console.error('从数据库加载数据失败，尝试本地存储:', error)
    
    // 降级到 localStorage
    const savedBalance = localStorage.getItem('userBalance')
    if (savedBalance) {
      currentBalance.value = parseFloat(savedBalance)
    }
    
    const savedTransactions = localStorage.getItem('transactions')
    if (savedTransactions) {
      transactions.value = JSON.parse(savedTransactions)
    }
  }
}

// 生命周期
onMounted(() => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录')
    router.push('/login')
    return
  }
  loadData()
})
</script>

<style scoped>
.cjx-transaction-page {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
}

.cjx-page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
  text-align: center;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 15px;
}

/* 钱包余额头部 */
.cjx-balance-header {
  text-align: center;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.cjx-balance-amount {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 5px;
}

.cjx-pending-balance {
  margin-left: 15px;
  color: #999;
}

/* 筛选栏 */
.cjx-filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.cjx-filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cjx-filter-group label {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.cjx-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  min-width: 100px;
  font-size: 13px;
}

.cjx-date-input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  width: 120px;
}

.cjx-date-separator {
  color: #999;
}

.cjx-btn {
  padding: 6px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
}

.cjx-btn-confirm {
  background: #4a4a4a;
  color: #fff;
}

.cjx-btn-confirm:hover {
  background: #333;
}

.cjx-btn-reset {
  background: #999;
  color: #fff;
}

.cjx-btn-reset:hover {
  background: #777;
}

/* 交易列表 - 黑色表头 */
.cjx-transaction-list {
  border: 1px solid #e0e0e0;
  overflow: hidden;
}

.cjx-list-header {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1fr;
  gap: 10px;
  padding: 12px 20px;
  background: #000;
  color: #fff;
  font-weight: normal;
  font-size: 14px;
}

.cjx-transaction-item {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1fr;
  gap: 10px;
  padding: 12px 20px;
  border-bottom: 1px solid #e0e0e0;
  align-items: center;
  font-size: 13px;
}

.cjx-transaction-item:last-child {
  border-bottom: none;
}

.cjx-transaction-item:hover {
  background: #f8f9fa;
}

.cjx-col-order {
  color: #666;
}

.cjx-col-date {
  color: #666;
}

.cjx-col-type {
  color: #666;
}

.cjx-col-amount {
  text-align: right;
}

.cjx-amount-positive {
  color: #3498db;
}

.cjx-amount-negative {
  color: #e74c3c;
}

.cjx-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

/* 分页 */
.cjx-pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  font-size: 13px;
}

.cjx-page-btn {
  padding: 4px 10px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.cjx-page-btn:hover:not(:disabled) {
  background: #f0f0f0;
}

.cjx-page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.cjx-page-num {
  padding: 4px 10px;
  border: 1px solid #ddd;
  cursor: pointer;
  color: #666;
}

.cjx-page-num.active {
  background: #4a4a4a;
  color: #fff;
  border-color: #4a4a4a;
}

.cjx-page-size {
  margin-left: 10px;
  color: #666;
}

.cjx-page-jump {
  color: #666;
}

.cjx-jump-input {
  width: 40px;
  padding: 4px;
  border: 1px solid #ddd;
  text-align: center;
}

.cjx-page-total {
  color: #666;
}

@media (max-width: 768px) {
  .cjx-filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .cjx-list-header,
  .cjx-transaction-item {
    grid-template-columns: 1fr;
    gap: 5px;
  }
  
  .cjx-list-header {
    display: none;
  }
}
</style>

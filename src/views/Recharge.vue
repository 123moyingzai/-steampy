<template>
  <Layout>
    <div class="cjx-recharge-page">
      <h2 class="cjx-page-title">余额充值 719cjx</h2>
      
      <!-- 当前余额展示 -->
      <div class="cjx-balance-card">
        <div class="cjx-balance-info">
          <span class="cjx-balance-label">当前余额 719cjx</span>
          <span class="cjx-balance-amount">¥{{ userBalance.toFixed(2) }}</span>
        </div>
        <div class="cjx-balance-actions">
          <button class="cjx-btn cjx-btn-outline" @click="viewHistory">查看明细</button>
        </div>
      </div>

      <!-- 充值金额选择 -->
      <div class="cjx-recharge-section">
        <h3>选择充值金额 719cjx</h3>
        <div class="cjx-amount-options">
          <div 
            v-for="amount in presetAmounts" 
            :key="amount"
            class="cjx-amount-item"
            :class="{ active: selectedAmount === amount }"
            @click="selectAmount(amount)"
          >
            <span class="cjx-amount-value">¥{{ amount }}</span>
            <span class="cjx-amount-bonus" v-if="getBonus(amount) > 0">送¥{{ getBonus(amount) }}</span>
          </div>
          <div 
            class="cjx-amount-item cjx-amount-custom"
            :class="{ active: isCustomAmount }"
            @click="isCustomAmount = true; selectedAmount = null"
          >
            <span v-if="!isCustomAmount">自定义</span>
            <input 
              v-else
              v-model.number="customAmount"
              type="number"
              placeholder="输入金额"
              min="1"
              max="50000"
              @click.stop
            />
          </div>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="cjx-payment-section">
        <h3>选择支付方式 719cjx</h3>
        <div class="cjx-payment-options">
          <label 
            v-for="method in paymentMethods" 
            :key="method.id"
            class="cjx-payment-item"
            :class="{ active: selectedPayment === method.id }"
          >
            <input 
              type="radio" 
              :value="method.id" 
              v-model="selectedPayment"
            />
            <span class="cjx-payment-icon">{{ method.icon }}</span>
            <span class="cjx-payment-name">{{ method.name }}</span>
          </label>
        </div>
      </div>

      <!-- 充值信息汇总 -->
      <div class="cjx-recharge-summary" v-if="rechargeAmount > 0">
        <div class="cjx-summary-row">
          <span>充值金额：</span>
          <strong>¥{{ rechargeAmount.toFixed(2) }}</strong>
        </div>
        <div class="cjx-summary-row" v-if="bonusAmount > 0">
          <span>赠送金额：</span>
          <strong class="cjx-bonus-text">+¥{{ bonusAmount.toFixed(2) }}</strong>
        </div>
        <div class="cjx-summary-row cjx-total-row">
          <span>实际到账：</span>
          <strong class="cjx-total-amount">¥{{ totalAmount.toFixed(2) }}</strong>
        </div>
      </div>

      <!-- 充值按钮 -->
      <div class="cjx-recharge-actions">
        <button 
          class="cjx-btn cjx-btn-recharge" 
          :disabled="rechargeAmount <= 0 || !selectedPayment || isProcessing"
          @click="handleRecharge"
        >
          <span v-if="isProcessing">处理中...</span>
          <span v-else>立即充值 ¥{{ rechargeAmount > 0 ? rechargeAmount.toFixed(2) : '0.00' }}</span>
        </button>
        <p class="cjx-recharge-tip">
          充值即表示同意《用户充值协议》719cjx
        </p>
      </div>

      <!-- 充值记录 -->
      <div class="cjx-recharge-history">
        <h3>最近充值记录 719cjx</h3>
        <div class="cjx-history-list" v-if="rechargeRecords.length > 0">
          <div 
            v-for="record in rechargeRecords" 
            :key="record.id"
            class="cjx-history-item"
          >
            <div class="cjx-history-info">
              <span class="cjx-history-type">{{ record.type }}</span>
              <span class="cjx-history-time">{{ formatDate(record.created_at) }}</span>
            </div>
            <div class="cjx-history-amount">
              <span class="cjx-amount-positive">+¥{{ record.amount.toFixed(2) }}</span>
              <span class="cjx-history-status" :class="`status-${record.status}`">
                {{ getStatusText(record.status) }}
              </span>
            </div>
          </div>
        </div>
        <div class="cjx-empty" v-else>
          <p>暂无充值记录 719cjx</p>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, walletAPI, transactionAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const userBalance = ref(0)
const selectedAmount = ref(null)
const customAmount = ref('')
const isCustomAmount = ref(false)
const selectedPayment = ref('alipay')
const isProcessing = ref(false)
const rechargeRecords = ref([])

// 预设充值金额
const presetAmounts = [50, 100, 200, 500, 1000, 2000]

// 支付方式
const paymentMethods = [
  { id: 'alipay', name: '支付宝', icon: '💳' },
  { id: 'wechat', name: '微信支付', icon: '💬' },
  { id: 'bank', name: '银行卡', icon: '🏦' }
]

// 计算属性
const rechargeAmount = computed(() => {
  if (isCustomAmount.value) {
    return parseFloat(customAmount.value) || 0
  }
  return selectedAmount.value || 0
})

const bonusAmount = computed(() => {
  const amount = rechargeAmount.value
  if (amount >= 2000) return amount * 0.1
  if (amount >= 1000) return amount * 0.05
  if (amount >= 500) return 20
  if (amount >= 200) return 10
  return 0
})

const totalAmount = computed(() => {
  return rechargeAmount.value + bonusAmount.value
})

// 方法
const selectAmount = (amount) => {
  selectedAmount.value = amount
  isCustomAmount.value = false
  customAmount.value = ''
}

const getBonus = (amount) => {
  if (amount >= 2000) return Math.floor(amount * 0.1)
  if (amount >= 1000) return Math.floor(amount * 0.05)
  if (amount >= 500) return 20
  if (amount >= 200) return 10
  return 0
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '处理中',
    'completed': '已完成',
    'failed': '失败'
  }
  return statusMap[status] || status
}

const viewHistory = () => {
  router.push('/transactions')
}

const handleRecharge = async () => {
  if (rechargeAmount.value <= 0) {
    alert('请选择或输入充值金额 719cjx')
    return
  }
  if (!selectedPayment.value) {
    alert('请选择支付方式 719cjx')
    return
  }

  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录 719cjx')
    router.push('/login')
    return
  }

  isProcessing.value = true

  // 模拟充值处理
  setTimeout(async () => {
    const currentUser = authAPI.getCurrentUser()
    const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
    const paymentName = paymentMethods.find(m => m.id === selectedPayment.value)?.name || '充值'
    
    try {
      // 充值到数据库
      const result = await walletAPI.recharge(userId, totalAmount.value, paymentName)
      
      if (result.error) throw new Error(result.error)
      
      userBalance.value = parseFloat(result.data)
      
      const newRecord = {
        id: Date.now(),
        type: paymentName,
        amount: rechargeAmount.value,
        status: 'completed',
        created_at: new Date().toISOString()
      }
      rechargeRecords.value.unshift(newRecord)
      
      console.log(`✓ 充值成功: ¥${totalAmount.value.toFixed(2)}`)
      alert(`充值成功！到账金额 ¥${totalAmount.value.toFixed(2)} 719cjx`)
    } catch (error) {
      console.error('充值失败:', error)
      alert(`充值失败: ${error.message}`)
      
      // 降级到 localStorage
      userBalance.value += totalAmount.value
      localStorage.setItem('userBalance', userBalance.value)
    }
    
    // 重置表单
    selectedAmount.value = null
    customAmount.value = ''
    isCustomAmount.value = false
    isProcessing.value = false
  }, 1500)
}

// 加载数据 - 从 Supabase 数据库获取
const loadData = async () => {
  const currentUser = authAPI.getCurrentUser()
  const userId = currentUser?.id || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  
  try {
    // 从数据库加载余额
    const balanceResult = await walletAPI.getBalance(userId)
    if (balanceResult.data !== undefined) {
      userBalance.value = parseFloat(balanceResult.data)
    }
    
    // 从数据库加载交易记录（筛选充值类型）
    const transactionResult = await transactionAPI.getTransactions(userId, { type: 'recharge' })
    if (transactionResult.data) {
      rechargeRecords.value = transactionResult.data.map(t => ({
        id: t.id,
        type: t.title?.replace('充值 ', '') || '充值',
        amount: Math.abs(t.amount),
        status: t.status,
        created_at: t.created_at
      }))
    }
    
    console.log('✓ 从数据库加载充值数据')
  } catch (error) {
    console.error('从数据库加载失败，尝试本地存储:', error)
    
    // 降级到 localStorage
    const savedBalance = localStorage.getItem('userBalance')
    if (savedBalance) {
      userBalance.value = parseFloat(savedBalance)
    }
    
    const savedRecords = localStorage.getItem('rechargeRecords')
    if (savedRecords) {
      rechargeRecords.value = JSON.parse(savedRecords)
    }
  }
}

// 生命周期
onMounted(() => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录 719cjx')
    router.push('/login')
    return
  }
  loadData()
})
</script>

<style scoped>
.cjx-recharge-page {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
}

.cjx-page-title {
  margin: 0 0 25px 0;
  font-size: 24px;
  color: #333;
}

/* 余额卡片 */
.cjx-balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.cjx-balance-label {
  display: block;
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.cjx-balance-amount {
  display: block;
  font-size: 36px;
  font-weight: bold;
}

.cjx-btn-outline {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.5);
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-btn-outline:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 充值金额选择 */
.cjx-recharge-section {
  margin-bottom: 30px;
}

.cjx-recharge-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
}

.cjx-amount-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.cjx-amount-item {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.cjx-amount-item:hover {
  border-color: #3498db;
}

.cjx-amount-item.active {
  border-color: #3498db;
  background: #f0f8ff;
}

.cjx-amount-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.cjx-amount-bonus {
  display: block;
  font-size: 12px;
  color: #e74c3c;
  margin-top: 5px;
}

.cjx-amount-custom {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cjx-amount-custom input {
  width: 100%;
  border: none;
  background: transparent;
  text-align: center;
  font-size: 18px;
  outline: none;
}

/* 支付方式 */
.cjx-payment-section {
  margin-bottom: 30px;
}

.cjx-payment-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
}

.cjx-payment-options {
  display: flex;
  gap: 15px;
}

.cjx-payment-item {
  flex: 1;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-payment-item:hover {
  border-color: #3498db;
}

.cjx-payment-item.active {
  border-color: #3498db;
  background: #f0f8ff;
}

.cjx-payment-item input {
  display: none;
}

.cjx-payment-icon {
  font-size: 24px;
}

.cjx-payment-name {
  font-size: 14px;
  color: #333;
}

/* 充值汇总 */
.cjx-recharge-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 25px;
}

.cjx-summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.cjx-summary-row:last-child {
  margin-bottom: 0;
}

.cjx-bonus-text {
  color: #e74c3c;
}

.cjx-total-row {
  border-top: 1px solid #e0e0e0;
  padding-top: 15px;
  margin-top: 15px;
  font-size: 16px;
}

.cjx-total-amount {
  color: #e74c3c;
  font-size: 24px;
}

/* 充值按钮 */
.cjx-recharge-actions {
  text-align: center;
  margin-bottom: 40px;
}

.cjx-btn-recharge {
  width: 100%;
  max-width: 400px;
  padding: 18px;
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-btn-recharge:hover:not(:disabled) {
  background: #c0392b;
}

.cjx-btn-recharge:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.cjx-recharge-tip {
  margin-top: 15px;
  font-size: 12px;
  color: #999;
}

/* 充值记录 */
.cjx-recharge-history {
  border-top: 1px solid #eee;
  padding-top: 25px;
}

.cjx-recharge-history h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #333;
}

.cjx-history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cjx-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.cjx-history-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.cjx-history-type {
  font-weight: 500;
  color: #333;
}

.cjx-history-time {
  font-size: 12px;
  color: #999;
}

.cjx-history-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.cjx-amount-positive {
  color: #27ae60;
  font-weight: bold;
  font-size: 16px;
}

.cjx-history-status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
}

.cjx-history-status.completed {
  background: #d4edda;
  color: #155724;
}

.cjx-history-status.pending {
  background: #fff3cd;
  color: #856404;
}

.cjx-history-status.failed {
  background: #f8d7da;
  color: #721c24;
}

.cjx-empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 768px) {
  .cjx-amount-options {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .cjx-payment-options {
    flex-direction: column;
  }
}
</style>

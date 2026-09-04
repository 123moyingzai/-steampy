﻿﻿﻿﻿﻿<template>
  <Layout>
    <!-- 页面标题 -->
    <div class="cjx-page-header">
      <h1>余额中心</h1>
      <p>充值、提现、余额购，一站搞定</p>
    </div>

    <!-- Tab 切换 -->
    <div class="cjx-tabs">
      <button
        class="cjx-tab"
        :class="{ 'cjx-tab-active': activeTab === 'shop' }"
        @click="activeTab = 'shop'"
      >🛒 余额购</button>
      <button
        class="cjx-tab"
        :class="{ 'cjx-tab-active': activeTab === 'wallet' }"
        @click="activeTab = 'wallet'; loadWallet()"
      >💰 钱包管理</button>
    </div>

    <!-- 内容区域 -->
    <div class="cjx-content-wrapper">
      <!-- ============ Tab1: 余额购 ============ -->
      <template v-if="activeTab === 'shop'">
        <div class="cjx-main-area">
          <div class="cjx-filter-bar">
            <span class="cjx-filter-label">排序:</span>
            <select class="cjx-filter-select" v-model="sortBy">
              <option value="price-asc">价格从低到高</option>
              <option value="price-desc">价格从高到低</option>
              <option value="discount">折扣力度</option>
            </select>
          </div>

          <div class="cjx-game-grid">
            <div class="cjx-game-card" v-for="(game, index) in sortedGames" :key="index" @click="buyGame(game)">
              <div class="cjx-game-img-wrapper">
                <img :src="getImageUrl(game.image)" :alt="game.name" class="cjx-game-img">
                <span class="cjx-discount-badge" v-if="game.discount">{{ game.discount }}</span>
              </div>
              <div class="cjx-game-info">
                <h3 class="cjx-game-title">{{ game.name }}</h3>
                <div class="cjx-game-meta">
                  <span class="cjx-tag">余额购</span>
                  <span class="cjx-tag">国区</span>
                </div>
                <div class="cjx-game-price">
                  <span class="cjx-current-price">{{ game.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧 -->
        <div class="cjx-sidebar-area">
          <div class="cjx-balance-card">
            <h3>我的余额</h3>
            <div class="cjx-balance-amount">¥{{ walletBalance.toFixed(2) }}</div>
            <div class="cjx-balance-btns">
              <button class="cjx-btn cjx-btn-recharge" @click="activeTab = 'wallet'; loadWallet()">充值/提现 →</button>
            </div>
          </div>

          <div class="cjx-info-box">
            <h3>什么是余额购?</h3>
            <p>余额购是使用Steam账户钱包余额购买游戏的方式。通过SteamPY平台，您可以以更低的价格获得Steam余额，然后用于购买游戏。</p>
          </div>

          <div class="cjx-notice-box">
            <h3>购买须知</h3>
            <ul>
              <li>仅限国区Steam账户使用</li>
              <li>余额将在交易完成后直接充入您的Steam账户</li>
              <li>请确保您的Steam账户可以接收市场交易</li>
              <li>如有问题请联系客服</li>
            </ul>
          </div>
        </div>
      </template>

      <!-- ============ Tab2: 钱包管理 ============ -->
      <template v-if="activeTab === 'wallet'">
        <div class="cjx-main-area">
          <!-- 余额大卡 -->
          <div class="cjx-wallet-card">
            <div class="cjx-wallet-label">账户余额</div>
            <div class="cjx-wallet-balance">¥{{ walletBalance.toFixed(2) }}</div>
            <div class="cjx-wallet-sub">充值金额实时到账；提现到账扣 1% 手续费</div>
          </div>

          <!-- 操作按钮 -->
          <div class="cjx-wallet-actions">
            <button class="cjx-btn cjx-btn-big cjx-btn-green" @click="openRechargeModal">
              💳 账户充值
            </button>
            <button
              class="cjx-btn cjx-btn-big cjx-btn-orange"
              :disabled="walletBalance <= 0"
              @click="openWithdrawModal"
            >
              🏦 余额提现
            </button>
          </div>

          <!-- 快捷金额（充值） -->
          <div class="cjx-quick-amounts">
            <div class="cjx-quick-label">快捷充值金额</div>
            <div class="cjx-quick-grid">
              <button
                v-for="amt in [10, 30, 50, 100, 200, 500]"
                :key="amt"
                class="cjx-quick-btn"
                @click="openRechargeWithAmount(amt)"
              >
                ¥{{ amt }}
              </button>
            </div>
          </div>

          <!-- 交易记录 -->
          <div class="cjx-tx-section">
            <h3 class="cjx-tx-title">最近交易</h3>
            <div v-if="transactions.length === 0" class="cjx-empty-tip">暂无交易记录</div>
            <div v-else class="cjx-tx-list">
              <div class="cjx-tx-item" v-for="tx in transactions" :key="tx.id">
                <div class="cjx-tx-left">
                  <span class="cjx-tx-icon" :class="getTxTypeClass(tx.type)">{{ getTxIcon(tx.type) }}</span>
                  <div>
                    <div class="cjx-tx-title">{{ tx.title || typeLabel(tx.type) }}</div>
                    <div class="cjx-tx-time">{{ formatTime(tx.created_at) }}</div>
                  </div>
                </div>
                <div class="cjx-tx-amount" :class="Number(tx.amount) >= 0 ? 'cjx-income' : 'cjx-expense'">
                  {{ Number(tx.amount) >= 0 ? '+' : '' }}{{ Number(tx.amount).toFixed(2) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧说明 -->
        <div class="cjx-sidebar-area">
          <div class="cjx-info-box">
            <h3>💳 账户充值</h3>
            <p>充值金额实时到账钱包余额，可用于购买游戏。支持微信、支付宝等多种支付方式。</p>
          </div>
          <div class="cjx-info-box cjx-info-orange">
            <h3>🏦 余额提现</h3>
            <p>提现将扣除 <strong class="cjx-highlight">1% 手续费</strong>，实际到账 = 提现金额 × 0.99。提现完成后余额立即减少。</p>
          </div>
        </div>
      </template>
    </div>

    <!-- ========== 充值弹窗 ========== -->
    <div class="cjx-modal" v-if="showRechargeModal" @click.self="showRechargeModal = false">
      <div class="cjx-modal-dialog">
        <div class="cjx-modal-header">
          <h3>💳 账户充值</h3>
          <span class="cjx-modal-close" @click="showRechargeModal = false">×</span>
        </div>
        <div class="cjx-modal-body">
          <div class="cjx-form-row">
            <label>充值金额（元）</label>
            <input type="number" v-model.number="rechargeAmount" min="0.01" step="0.01" placeholder="请输入充值金额">
          </div>
          <div class="cjx-pay-row">
            <label>支付方式</label>
            <div class="cjx-pay-options">
              <label class="cjx-pay-option">
                <input type="radio" v-model="rechargePayMethod" value="wechat">
                <span>🟢 微信支付</span>
              </label>
              <label class="cjx-pay-option">
                <input type="radio" v-model="rechargePayMethod" value="alipay">
                <span>🔵 支付宝</span>
              </label>
            </div>
          </div>
        </div>
        <div class="cjx-modal-footer">
          <button class="cjx-btn cjx-btn-default" @click="showRechargeModal = false">取消</button>
          <button class="cjx-btn cjx-btn-green" @click="confirmRecharge">
            立即充值 ¥{{ (rechargeAmount || 0).toFixed(2) }}
          </button>
        </div>
      </div>
    </div>

    <!-- ========== 提现弹窗 ========== -->
    <div class="cjx-modal" v-if="showWithdrawModal" @click.self="showWithdrawModal = false">
      <div class="cjx-modal-dialog">
        <div class="cjx-modal-header">
          <h3>🏦 余额提现</h3>
          <span class="cjx-modal-close" @click="showWithdrawModal = false">×</span>
        </div>
        <div class="cjx-modal-body">
          <div class="cjx-wallet-big-balance">当前余额：<strong>¥{{ walletBalance.toFixed(2) }}</strong></div>
          <div class="cjx-form-row">
            <label>提现金额（元）</label>
            <input type="number" v-model.number="withdrawAmount" min="0.01" step="0.01" :max="walletBalance" placeholder="请输入提现金额">
          </div>
          <div class="cjx-fee-preview" v-if="withdrawAmount > 0">
            <div class="cjx-fee-row"><span>提现金额</span><span>¥{{ withdrawAmount.toFixed(2) }}</span></div>
            <div class="cjx-fee-row"><span>手续费（1%）</span><span class="cjx-fee-minus">-¥{{ (withdrawAmount * 0.01).toFixed(2) }}</span></div>
            <div class="cjx-fee-total"><span>实际到账</span><span>¥{{ (withdrawAmount * 0.99).toFixed(2) }}</span></div>
          </div>
          <div class="cjx-pay-row">
            <label>收款方式</label>
            <div class="cjx-pay-options">
              <label class="cjx-pay-option">
                <input type="radio" v-model="withdrawPayMethod" value="alipay">
                <span>🔵 支付宝</span>
              </label>
              <label class="cjx-pay-option">
                <input type="radio" v-model="withdrawPayMethod" value="wechat">
                <span>🟢 微信</span>
              </label>
              <label class="cjx-pay-option">
                <input type="radio" v-model="withdrawPayMethod" value="bank">
                <span>🏦 银行卡</span>
              </label>
            </div>
          </div>
        </div>
        <div class="cjx-modal-footer">
          <button class="cjx-btn cjx-btn-default" @click="showWithdrawModal = false">取消</button>
          <button
            class="cjx-btn cjx-btn-orange"
            :disabled="!withdrawAmount || withdrawAmount > walletBalance"
            @click="confirmWithdraw"
          >
            确认提现 ¥{{ (withdrawAmount || 0).toFixed(2) }}
          </button>
        </div>
      </div>
    </div>

    <!-- ========== 支付中遮罩 ========== -->
    <div class="cjx-paying-mask" v-if="showPayingMask">
      <div class="cjx-paying-box">
        <div class="cjx-spinner"></div>
        <div class="cjx-paying-text">{{ payingText }}</div>
      </div>
    </div>

    <!-- Toast -->
    <div class="cjx-toast" v-if="toastMsg">{{ toastMsg }}</div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Layout from '../components/Layout.vue'
import { authAPI, walletAPI, transactionAPI } from '../config/supabase-local.ts'

const router = useRouter()

// Tab
const activeTab = ref<'shop' | 'wallet'>('shop')

// ======= 余额购（Tab1） =======
const games = ref<any[]>([])
const sortBy = ref('price-asc')

const getRawPrice = (p: any) => {
  if (typeof p === 'number') return p
  return parseFloat(String(p).replace(/[^0-9.]/g, '')) || 0
}
const sortedGames = computed(() => {
  let sorted = [...games.value]
  switch (sortBy.value) {
    case 'price-asc':
      sorted.sort((a, b) => getRawPrice(a.price) - getRawPrice(b.price)); break
    case 'price-desc':
      sorted.sort((a, b) => getRawPrice(b.price) - getRawPrice(a.price)); break
    case 'discount':
      sorted.sort((a, b) => {
        const da = parseInt(a.discount?.replace(/[^0-9-]/g, '') || 0)
        const db = parseInt(b.discount?.replace(/[^0-9-]/g, '') || 0)
        return da - db
      }); break
  }
  return sorted
})

const getImageUrl = (path: string) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  if (path.includes('picture/')) {
    const fn = path.split('picture/')[1]
    if (fn) return `/picture/${fn}`
  }
  return path.startsWith('/') ? path : `/${path}`
}

const buyGame = (game: any) => {
  router.push(`/cdmarket`) // 跳商城挑选
}

// ======= 钱包（Tab2） =======
const walletBalance = ref(0)
const transactions = ref<any[]>([])

async function loadWallet() {
  const u = authAPI.getCurrentUser()
  if (!u?.id) return
  const w = await walletAPI.getWallet(u.id)
  walletBalance.value = Number(w.data?.balance ?? 0)
  try {
    const tx = await transactionAPI.getUserTransactions(u.id)
    transactions.value = (tx.data || []).slice(0, 10)
  } catch {}
}

// ======= 充值弹窗 =======
const showRechargeModal = ref(false)
const rechargeAmount = ref(50)
const rechargePayMethod = ref('wechat')

function openRechargeModal() {
  rechargeAmount.value = 50
  rechargePayMethod.value = 'wechat'
  showRechargeModal.value = true
}
function openRechargeWithAmount(amt: number) {
  rechargeAmount.value = amt
  openRechargeModal()
}

// 模拟支付遮罩
const showPayingMask = ref(false)
const payingText = ref('')

function confirmRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    alert('请输入有效金额')
    return
  }
  const u = authAPI.getCurrentUser()
  if (!u) { alert('请先登录'); return }

  // 模拟支付中
  payingText.value = `正在通过${rechargePayMethod.value === 'wechat' ? '微信' : '支付宝'}支付 ¥${rechargeAmount.value.toFixed(2)}...`
  showPayingMask.value = true

  setTimeout(async () => {
    const res = await walletAPI.recharge(u.id, rechargeAmount.value)
    showPayingMask.value = false
    if (res.error) {
      alert('充值失败: ' + res.error)
      return
    }
    showRechargeModal.value = false
    showToast(`✓ 充值成功！到账 ¥${rechargeAmount.value.toFixed(2)}`)
    await loadWallet()
  }, 1200)
}

// ======= 提现弹窗 =======
const showWithdrawModal = ref(false)
const withdrawAmount = ref(0)
const withdrawPayMethod = ref('alipay')

function openWithdrawModal() {
  withdrawAmount.value = 0
  withdrawPayMethod.value = 'alipay'
  showWithdrawModal.value = true
}

function confirmWithdraw() {
  if (!withdrawAmount.value || withdrawAmount.value <= 0) { alert('请输入有效金额'); return }
  if (withdrawAmount.value > walletBalance.value) { alert('余额不足'); return }
  const u = authAPI.getCurrentUser()
  if (!u) { alert('请先登录'); return }

  const fee = (withdrawAmount.value * 0.01).toFixed(2)
  const net = (withdrawAmount.value * 0.99).toFixed(2)
  const payName = withdrawPayMethod.value === 'alipay' ? '支付宝'
    : withdrawPayMethod.value === 'wechat' ? '微信' : '银行卡'

  if (!confirm(`确认提现 ¥${withdrawAmount.value.toFixed(2)} 到${payName}？\n手续费 ¥${fee}，实际到账 ¥${net}`)) return

  payingText.value = `正在为您提现到${payName}...`
  showPayingMask.value = true

  setTimeout(async () => {
    const res = await walletAPI.withdraw(u.id, withdrawAmount.value)
    showPayingMask.value = false
    if (res.error) {
      alert('提现失败: ' + res.error)
      return
    }
    showWithdrawModal.value = false
    showToast(`✓ 提现成功！实际到账 ¥${net}`)
    await loadWallet()
  }, 1500)
}

// ======= 工具 =======
function showToast(msg: string) {
  toastMsg.value = msg
  setTimeout(() => toastMsg.value = '', 2500)
}
const toastMsg = ref('')

function typeLabel(type: string) {
  const m: Record<string, string> = {
    recharge: '账户充值', purchase: '购买支出', sale: '售出收入',
    withdraw: '余额提现', fee: '手续费', refund: '退款'
  }
  return m[type] || type
}
function getTxTypeClass(type: string) {
  const m: Record<string, string> = {
    recharge: 'cjx-tx-in', sale: 'cjx-tx-in', refund: 'cjx-tx-in'
  }
  return m[type] || 'cjx-tx-out'
}
function getTxIcon(type: string) {
  const m: Record<string, string> = {
    recharge: '💳', purchase: '🎮', sale: '💰', withdraw: '🏦', fee: '📉', refund: '↩️'
  }
  return m[type] || '💰'
}
function formatTime(t: any) {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 16)
}

// ======= 加载 =======
onMounted(async () => {
  try {
    const response = await axios.get('/cdk_games.json')
    if (response.data) {
      const data = response.data
      games.value = [...(data.preSaleItems || []), ...(data.gameItems || [])]
    }
  } catch {
    games.value = [
      { name: 'Fullbright Pres', price: '¥6', discount: '-77%', image: './picture/Fullbright Pres.jpg' },
      { name: '东方奇缘记', price: '¥2.5', discount: '-86%', image: './picture/东方奇缘记.jpg' },
    ]
  }
  await loadWallet()
})
</script>

<style scoped>
/* Header */
.cjx-page-header { padding: 2rem; background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; }
.cjx-page-header h1 { font-size: 2rem; margin-bottom: 0.5rem; }
.cjx-page-header p { font-size: 1rem; opacity: 0.9; }

/* Tabs */
.cjx-tabs { display: flex; gap: 0; border-bottom: 2px solid #eee; background: #fff; padding: 0 2rem; }
.cjx-tab { padding: 1rem 2rem; background: none; border: none; font-size: 1rem; cursor: pointer; color: #888; border-bottom: 3px solid transparent; transition: all 0.2s; }
.cjx-tab:hover { color: #11998e; }
.cjx-tab-active { color: #11998e; border-bottom-color: #11998e; font-weight: 600; }

/* Content */
.cjx-content-wrapper { padding: 2rem; display: flex; gap: 2rem; }
.cjx-main-area { flex: 3; }
.cjx-sidebar-area { flex: 1; }

/* Shop */
.cjx-filter-bar { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; padding: 1rem; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.cjx-filter-label { font-weight: 500; color: #666; }
.cjx-filter-select { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 4px; font-size: 0.9rem; }
.cjx-game-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1.5rem; }
.cjx-game-card { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); transition: transform 0.2s; cursor: pointer; }
.cjx-game-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.cjx-game-img-wrapper { position: relative; height: 120px; }
.cjx-game-img { width: 100%; height: 100%; object-fit: cover; }
.cjx-discount-badge { position: absolute; top: 8px; right: 8px; background: #e74c3c; color: white; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.8rem; font-weight: bold; }
.cjx-game-info { padding: 1rem; }
.cjx-game-title { font-size: 0.95rem; margin-bottom: 0.5rem; color: #2c3e50; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cjx-game-meta { display: flex; gap: 0.5rem; margin-bottom: 0.5rem; }
.cjx-tag { font-size: 0.75rem; padding: 0.25rem 0.5rem; background-color: #e8f5e9; color: #2e7d32; border-radius: 4px; }
.cjx-game-price { display: flex; justify-content: space-between; align-items: center; }
.cjx-current-price { font-size: 1.1rem; color: #e74c3c; font-weight: bold; }

/* Balance card */
.cjx-balance-card { background: white; border-radius: 8px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 1rem; }
.cjx-balance-card h3 { margin: 0 0 0.5rem; color: #666; font-size: 0.9rem; }
.cjx-balance-amount { font-size: 2rem; font-weight: bold; color: #11998e; margin-bottom: 1rem; }
.cjx-balance-btns { display: flex; gap: 0.5rem; }
.cjx-btn-recharge { flex: 1; padding: 0.6rem; background: #11998e; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.9rem; }
.cjx-btn-recharge:hover { background: #0f8577; }

/* Info boxes */
.cjx-info-box { background: white; border-radius: 8px; padding: 1.2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); margin-bottom: 1rem; }
.cjx-info-box h3 { margin: 0 0 0.5rem; font-size: 1rem; color: #333; }
.cjx-info-box p { margin: 0; font-size: 0.85rem; color: #666; line-height: 1.6; }
.cjx-info-orange h3 { color: #e67e22; }
.cjx-highlight { color: #e67e22; }

.cjx-notice-box { background: white; border-radius: 8px; padding: 1.2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.cjx-notice-box h3 { margin: 0 0 0.5rem; font-size: 1rem; color: #333; }
.cjx-notice-box ul { margin: 0; padding-left: 1.2rem; color: #666; font-size: 0.85rem; line-height: 1.8; }

/* Wallet Tab */
.cjx-wallet-card { background: linear-gradient(135deg, #11998e, #38ef7d); color: white; border-radius: 12px; padding: 2rem; box-shadow: 0 4px 20px rgba(17,153,142,0.3); margin-bottom: 1.5rem; }
.cjx-wallet-label { font-size: 0.9rem; opacity: 0.85; margin-bottom: 0.5rem; }
.cjx-wallet-balance { font-size: 3rem; font-weight: bold; }
.cjx-wallet-sub { font-size: 0.8rem; opacity: 0.8; margin-top: 0.3rem; }

.cjx-wallet-actions { display: flex; gap: 1rem; margin-bottom: 1.5rem; }
.cjx-btn-big { flex: 1; padding: 1rem; border: none; border-radius: 8px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.cjx-btn-big:disabled { opacity: 0.5; cursor: not-allowed; }
.cjx-btn-green { background: #11998e; color: white; }
.cjx-btn-green:hover:not(:disabled) { background: #0f8577; }
.cjx-btn-orange { background: #e67e22; color: white; }
.cjx-btn-orange:hover:not(:disabled) { background: #ca6f1e; }

.cjx-quick-amounts { background: white; border-radius: 8px; padding: 1.2rem; margin-bottom: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.cjx-quick-label { font-size: 0.9rem; color: #666; margin-bottom: 0.8rem; }
.cjx-quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.8rem; }
.cjx-quick-btn { padding: 0.8rem; border: 1px solid #11998e; background: white; color: #11998e; border-radius: 6px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.cjx-quick-btn:hover { background: #11998e; color: white; }

/* Transaction list */
.cjx-tx-section { background: white; border-radius: 8px; padding: 1.2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.cjx-tx-title { margin: 0 0 1rem; font-size: 1.1rem; color: #333; }
.cjx-empty-tip { color: #999; text-align: center; padding: 2rem; }
.cjx-tx-list { display: flex; flex-direction: column; }
.cjx-tx-item { display: flex; justify-content: space-between; align-items: center; padding: 0.8rem 0; border-bottom: 1px solid #f0f0f0; }
.cjx-tx-item:last-child { border-bottom: none; }
.cjx-tx-left { display: flex; align-items: center; gap: 0.8rem; }
.cjx-tx-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.cjx-tx-in { background: #e8f8f0; }
.cjx-tx-out { background: #fdecea; }
.cjx-tx-title { font-size: 0.95rem; color: #333; font-weight: 500; }
.cjx-tx-time { font-size: 0.75rem; color: #999; margin-top: 2px; }
.cjx-tx-amount { font-weight: bold; font-size: 1rem; }
.cjx-income { color: #27ae60; }
.cjx-expense { color: #e74c3c; }

/* Modal */
.cjx-modal { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.cjx-modal-dialog { background: white; border-radius: 12px; width: 440px; max-width: 90vw; max-height: 90vh; overflow: hidden; display: flex; flex-direction: column; }
.cjx-modal-header { display: flex; justify-content: space-between; align-items: center; padding: 1.2rem 1.5rem; border-bottom: 1px solid #eee; }
.cjx-modal-header h3 { margin: 0; font-size: 1.1rem; }
.cjx-modal-close { font-size: 1.8rem; cursor: pointer; color: #999; line-height: 1; }
.cjx-modal-close:hover { color: #333; }
.cjx-modal-body { padding: 1.5rem; overflow-y: auto; }
.cjx-modal-footer { display: flex; justify-content: flex-end; gap: 0.8rem; padding: 1rem 1.5rem; border-top: 1px solid #eee; background: #fafafa; }

.cjx-form-row { margin-bottom: 1rem; }
.cjx-form-row label { display: block; margin-bottom: 0.5rem; color: #666; font-size: 0.9rem; }
.cjx-form-row input { width: 100%; padding: 0.7rem 0.9rem; border: 1px solid #ddd; border-radius: 6px; font-size: 1rem; box-sizing: border-box; }
.cjx-form-row input:focus { border-color: #11998e; outline: none; }

.cjx-wallet-big-balance { background: #f8f9fa; padding: 0.8rem 1rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.95rem; color: #555; }
.cjx-wallet-big-balance strong { color: #11998e; font-size: 1.1rem; }

.cjx-fee-preview { background: #fef9e7; border-radius: 6px; padding: 1rem; margin-bottom: 1rem; border: 1px solid #f9e79f; }
.cjx-fee-row { display: flex; justify-content: space-between; padding: 0.2rem 0; font-size: 0.9rem; color: #666; }
.cjx-fee-minus { color: #e67e22; }
.cjx-fee-total { display: flex; justify-content: space-between; padding-top: 0.6rem; margin-top: 0.4rem; border-top: 1px dashed #f0d78c; font-weight: bold; color: #e67e22; }

.cjx-pay-row { margin-bottom: 0.5rem; }
.cjx-pay-row > label { display: block; margin-bottom: 0.5rem; color: #666; font-size: 0.9rem; }
.cjx-pay-options { display: flex; gap: 0.8rem; flex-wrap: wrap; }
.cjx-pay-option { display: flex; align-items: center; gap: 0.4rem; padding: 0.6rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 0.9rem; }
.cjx-pay-option input { margin: 0; }
.cjx-pay-option:has(input:checked) { border-color: #11998e; background: #e8f8f4; color: #11998e; }

.cjx-btn { padding: 0.6rem 1.2rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.9rem; font-weight: 500; }
.cjx-btn-default { background: #ecf0f1; color: #7f8c8d; }
.cjx-btn-default:hover { background: #dfe6e9; }
.cjx-btn-green { background: #11998e; color: white; }
.cjx-btn-green:hover { background: #0f8577; }
.cjx-btn-orange { background: #e67e22; color: white; }
.cjx-btn-orange:hover { background: #ca6f1e; }

/* Paying mask */
.cjx-paying-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 2000; }
.cjx-paying-box { background: white; border-radius: 12px; padding: 2rem 3rem; text-align: center; }
.cjx-spinner { width: 50px; height: 50px; border: 4px solid #eee; border-top-color: #11998e; border-radius: 50%; animation: cjx-spin 0.9s linear infinite; margin: 0 auto 1rem; }
@keyframes cjx-spin { to { transform: rotate(360deg); } }
.cjx-paying-text { color: #333; font-size: 0.95rem; }

/* Toast */
.cjx-toast { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); background: rgba(0,0,0,0.85); color: white; padding: 0.8rem 1.5rem; border-radius: 6px; z-index: 3000; animation: cjx-fade 0.3s; }
@keyframes cjx-fade { from { opacity: 0; transform: translate(-50%, -10px); } to { opacity: 1; transform: translate(-50%, 0); } }
</style>

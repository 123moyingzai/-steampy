<template>
  <Layout>
    <!-- 返回 -->
    <div class="cjx-wallet-back" @click="$router.back()">← 返回</div>

    <!-- 余额头 -->
    <div class="cjx-wallet-balance-head">
      <span>钱包余额:<strong>¥{{ balance.toFixed(2) }}</strong></span>
      <span class="cjx-sep">|</span>
      <span>待处理余额:<strong class="cjx-pending">¥{{ frozenBalance.toFixed(2) }}</strong></span>
    </div>

    <!-- Tab -->
    <div class="cjx-wallet-tabs">
      <button
        class="cjx-wallet-tab"
        :class="{ active: tab === 'withdraw' }"
        @click="tab = 'withdraw'"
      >💸 申请提现</button>
      <button
        class="cjx-wallet-tab"
        :class="{ active: tab === 'rules' }"
        @click="tab = 'rules'"
      >📋 资金提现说明</button>
      <button
        class="cjx-wallet-tab"
        :class="{ active: tab === 'records' }"
        @click="tab = 'records'; loadRecords()"
      >📄 提现记录</button>
    </div>

    <!-- ============ Tab1: 提现表单 ============ -->
    <div v-if="tab === 'withdraw'" class="cjx-wallet-panel">
      <div class="cjx-withdraw-form">
        <div class="cjx-form-row">
          <label>提现金额</label>
          <div class="cjx-input-with-btn">
            <input
              type="number"
              v-model.number="withdrawAmount"
              min="0.01"
              step="0.01"
              :max="balance"
              placeholder="请输入提现金额"
              class="cjx-wallet-input"
            />
            <button class="cjx-btn-all" @click="withdrawAmount = balance">全部提取</button>
          </div>
        </div>

        <!-- 支付方式 Tab -->
        <div class="cjx-pay-tabs">
          <button
            class="cjx-pay-tab"
            :class="{ active: payMethod === 'alipay' }"
            @click="payMethod = 'alipay'"
          >支付宝</button>
          <button
            class="cjx-pay-tab"
            :class="{ active: payMethod === 'bank' }"
            @click="payMethod = 'bank'"
          >银行卡</button>
        </div>

        <!-- 支付宝表单 -->
        <template v-if="payMethod === 'alipay'">
          <div class="cjx-form-row">
            <label>支付宝账号</label>
            <input
              v-model="account"
              class="cjx-wallet-input"
              placeholder="请输入支付宝账号（手机号或邮箱）"
            />
          </div>
          <div class="cjx-form-row">
            <label>支付宝真实姓名</label>
            <input
              v-model="realName"
              class="cjx-wallet-input"
              placeholder="请输入与支付宝账号一致的真实姓名"
            />
          </div>
        </template>

        <!-- 银行卡表单 -->
        <template v-if="payMethod === 'bank'">
          <div class="cjx-form-row">
            <label>开户银行</label>
            <input
              v-model="bankName"
              class="cjx-wallet-input"
              placeholder="请输入开户银行（如中国工商银行）"
            />
          </div>
          <div class="cjx-form-row">
            <label>银行卡号</label>
            <input
              v-model="account"
              class="cjx-wallet-input"
              placeholder="请输入银行卡号"
            />
          </div>
          <div class="cjx-form-row">
            <label>持卡人姓名</label>
            <input
              v-model="realName"
              class="cjx-wallet-input"
              placeholder="请输入与银行卡一致的持卡人姓名"
            />
          </div>
        </template>

        <!-- 预估 -->
        <div v-if="withdrawAmount > 0" class="cjx-fee-preview">
          <div class="cjx-fee-line"><span>提现金额</span><span>¥{{ withdrawAmount.toFixed(2) }}</span></div>
          <div class="cjx-fee-line"><span>手续费（1%，最低¥1）</span><span>-¥{{ fee.toFixed(2) }}</span></div>
          <div class="cjx-fee-line cjx-fee-total"><span>预计到账</span><span>¥{{ netAmount.toFixed(2) }}</span></div>
        </div>

        <button
          class="cjx-btn-apply"
          :disabled="!canSubmit"
          @click="submitWithdraw"
        >申请提现</button>
      </div>
    </div>

    <!-- ============ Tab2: 资金提现说明 ============ -->
    <div v-if="tab === 'rules'" class="cjx-wallet-panel cjx-rules-panel">
      <h2 class="cjx-rules-title">资金提现</h2>
      <div class="cjx-rules-body">
        <p>订单完成后，资金会自动转入卖家钱包</p>
        <p>1.平台代购基础手续费为3%。国区代购挂单折扣设定95%以上(不含)会额外增加手续费5%，折扣设定110%及以上会额外增加手续费10%。手续费由卖家承担，会在出售成功时自动扣除。实际收益请自行按实际steam支出根据汇率计算</p>
        <p>平台CDKey手续费为3%，由卖家承担，会在出售成功时自动扣除</p>
        <p>外区代购汇率按当天steam汇率进行计算</p>

        <p class="cjx-rule-num">1.提现需先至【个人-实名认证】进行实名认证，未满十八岁不可提现。根据收益总额将在提现时提醒进行不同级别认证，完成认证后即可发起提现或使用余额。可到安卓app或苹果商店搜索匠互下app，app内个人中心进行认证</p>
        <p class="cjx-rule-num">2.所有实名信息必须一致，实名信息强制提现，提现账号必须为实名人下账号，实名无法随意更改，请勿填写无关信息进行认证!</p>
        <p class="cjx-rule-num">3.每天24:00前只能提现一笔，单日单笔上限1万元，如需更高提现额度请发起工单或加QQ群联系管理</p>
        <p class="cjx-rule-num">4.提现金额满1000元时可使用银行提现，不足1000元的请用支付宝提现</p>
        <p class="cjx-rule-num">5.提现时请务必正确填写银行卡号与其对应的真实姓名，银行开户行请填写具体银行名称而非支行，仅支持储蓄卡提现</p>
        <p class="cjx-rule-num">6.支付宝提现请先确认自己的支付宝账号（手机号或邮箱号），姓名部分请填写对应真实姓名</p>
        <p class="cjx-rule-num">7.支付宝提现48小时到账。银行提现48小时-72小时，发放后会延迟到账，个别地区银行在非工作日需要时间更长，请耐心等待</p>
        <p class="cjx-rule-num">8.提现仅支持整数提取，小数点不支持提现，小数点可用于订单支付</p>
        <p class="cjx-rule-num">9.支付宝银行每笔提现均收取1%手续费，最低1元（手续费不足1元的按照1元收取），上限50元</p>
        <p class="cjx-rule-num">10.提现手续费从PY余额中扣除，请确保余额足额，不足则无法发起提现<br>例: PY余额10元，提现金额只可填写9元，到账9元剩余1元作为提现手续费扣除</p>
        <p class="cjx-rule-num">11.从2023年7月1日起，出售的CDKey订单中，部分游戏收款金额会有30天左右的延迟到账时间，期间可以待处理余额的方式展示在钱包内，游戏名单详情可查看PC官网公告</p>
        <p class="cjx-rule-num">12.提现一旦发起无法撤销，如遇提现超过72小时未到账，提交工单或加入QQ群829720316私聊询问相关管理员</p>
      </div>
    </div>

    <!-- ============ Tab3: 提现记录 ============ -->
    <div v-if="tab === 'records'" class="cjx-wallet-panel cjx-records-panel">
      <div class="cjx-records-header">
        <h3>提现记录</h3>
      </div>
      <div v-if="records.length === 0" class="cjx-empty">暂无提现记录</div>
      <table v-else class="cjx-records-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>申请日期</th>
            <th>提现金额</th>
            <th>提现手续费</th>
            <th>到账方式</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.id">
            <td>{{ r.order_no }}</td>
            <td>{{ formatDate(r.applied_at) }}</td>
            <td>¥{{ Number(r.amount).toFixed(2) }}</td>
            <td>¥{{ Number(r.fee).toFixed(2) }}</td>
            <td>{{ r.pay_method === 'alipay' ? '支付宝' : '银行卡' }}</td>
            <td>
              <span class="cjx-status" :class="`cjx-status-${r.status}`">
                {{ r.status === 'success' ? '提现成功' : r.status === 'pending' ? '处理中' : '提现失败' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Toast -->
    <div class="cjx-toast" v-if="toastMsg">{{ toastMsg }}</div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '../components/Layout.vue'
import { authAPI, walletAPI } from '../config/supabase-local.ts'

const router = useRouter()

const tab = ref<'withdraw' | 'rules' | 'records'>('withdraw')
const balance = ref(0)
const frozenBalance = ref(0)

// 表单
const withdrawAmount = ref(0)
const payMethod = ref<'alipay' | 'bank'>('alipay')
const account = ref('')
const realName = ref('')
const bankName = ref('')

// 记录
const records = ref<any[]>([])

// Toast
const toastMsg = ref('')
const showToast = (m: string) => { toastMsg.value = m; setTimeout(() => toastMsg.value = '', 2500) }

// 计算
const fee = computed(() => {
  if (withdrawAmount.value <= 0) return 0
  const raw = withdrawAmount.value * 0.01
  return Math.max(1, Math.min(50, Math.round(raw * 100) / 100))
})
const netAmount = computed(() => Math.max(0, withdrawAmount.value - fee.value))

const canSubmit = computed(() => {
  return withdrawAmount.value > 0
    && withdrawAmount.value <= balance.value
    && account.value.trim()
    && realName.value.trim()
    && (payMethod.value !== 'bank' || bankName.value.trim())
})

const formatDate = (t: any) => {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 19)
}

async function loadBalance() {
  const u = authAPI.getCurrentUser()
  if (!u?.id) return
  const res = await walletAPI.getWallet(u.id)
  if (res.data) {
    balance.value = Number(res.data.balance ?? 0)
    frozenBalance.value = Number(res.data.frozen_balance ?? 0)
  }
}

async function loadRecords() {
  const u = authAPI.getCurrentUser()
  if (!u?.id) return
  const res = await walletAPI.getWithdrawRecords(u.id)
  records.value = res.data || []
}

async function submitWithdraw() {
  if (!canSubmit.value) { showToast('请填写完整信息'); return }
  const u = authAPI.getCurrentUser()
  if (!u?.id) { showToast('请先登录'); router.push('/login'); return }

  if (!confirm(`确认提现 ¥${withdrawAmount.value.toFixed(2)}？\n手续费 ¥${fee.value.toFixed(2)}，实际到账 ¥${netAmount.value.toFixed(2)}`)) return

  const res = await walletAPI.withdraw(u.id, withdrawAmount.value, {
    pay_method: payMethod.value,
    account: account.value,
    real_name: realName.value
  })
  if (res.error) {
    showToast('提现失败: ' + res.error)
    return
  }
  showToast(`✓ 提现成功！订单号 ${res.data.order_no || ''}，到账 ¥${res.data.net_amount}`)
  withdrawAmount.value = 0
  account.value = ''
  realName.value = ''
  bankName.value = ''
  await loadBalance()
}

onMounted(loadBalance)
</script>

<style scoped>
.cjx-wallet-back {
  padding: 12px 0 8px; font-size: 14px; color: #666;
  cursor: pointer; user-select: none;
}
.cjx-wallet-back:hover { color: #3498db; }

/* 余额头 */
.cjx-wallet-balance-head {
  text-align: center; padding: 24px 0; font-size: 18px; color: #333;
  border-bottom: 1px solid #eee; margin-bottom: 20px;
}
.cjx-wallet-balance-head strong { font-size: 22px; color: #e74c3c; margin: 0 4px; }
.cjx-wallet-balance-head .cjx-pending { color: #f39c12; }
.cjx-sep { margin: 0 20px; color: #ddd; }

/* Tab */
.cjx-wallet-tabs {
  display: flex; border-bottom: 2px solid #eee; margin-bottom: 24px;
}
.cjx-wallet-tab {
  padding: 12px 24px; background: none; border: none; cursor: pointer;
  font-size: 15px; color: #777; border-bottom: 3px solid transparent;
  transition: all .2s;
}
.cjx-wallet-tab:hover { color: #3498db; }
.cjx-wallet-tab.active {
  color: #3498db; border-bottom-color: #3498db; font-weight: 600;
}

/* 面板 */
.cjx-wallet-panel {
  max-width: 720px; margin: 0 auto;
}
.cjx-records-panel { max-width: 100% !important; padding: 0 20px; }

/* 表单行 */
.cjx-form-row {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 20px;
}
.cjx-form-row label {
  width: 140px; font-size: 14px; color: #555; text-align: right; flex-shrink: 0;
}
.cjx-wallet-input {
  flex: 1; padding: 10px 14px; border: 1px solid #ddd; border-radius: 4px;
  font-size: 14px; outline: none; transition: border-color .2s;
}
.cjx-wallet-input:focus { border-color: #3498db; }
.cjx-input-with-btn {
  flex: 1; display: flex; gap: 8px;
}
.cjx-input-with-btn .cjx-wallet-input { flex: 1; }
.cjx-btn-all {
  padding: 10px 18px; border: 1px solid #3498db; background: #fff; color: #3498db;
  border-radius: 4px; cursor: pointer; font-size: 14px; white-space: nowrap;
}
.cjx-btn-all:hover { background: #3498db; color: #fff; }

/* 支付方式 Tab */
.cjx-pay-tabs {
  display: flex; border-bottom: 1px solid #ddd; margin-bottom: 24px; gap: 0;
}
.cjx-pay-tab {
  padding: 10px 32px; background: none; border: none; cursor: pointer;
  font-size: 15px; color: #999; border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}
.cjx-pay-tab:hover { color: #333; }
.cjx-pay-tab.active {
  color: #333; border-bottom-color: #333; font-weight: 500;
}

/* 费用预览 */
.cjx-fee-preview {
  background: #f8f9fa; border-radius: 6px; padding: 14px 18px;
  margin-bottom: 24px; font-size: 14px;
}
.cjx-fee-line { display: flex; justify-content: space-between; padding: 4px 0; color: #666; }
.cjx-fee-total { border-top: 1px dashed #ddd; margin-top: 8px; padding-top: 10px; font-weight: 600; color: #e74c3c; }

/* 申请提现按钮 */
.cjx-btn-apply {
  width: 100%; padding: 14px; background: linear-gradient(135deg, #333, #555);
  color: #fff; border: none; border-radius: 6px; font-size: 16px;
  cursor: pointer; transition: all .2s;
}
.cjx-btn-apply:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.2); }
.cjx-btn-apply:disabled { opacity: 0.5; cursor: not-allowed; }

/* 规则页 */
.cjx-rules-panel { max-width: 700px; }
.cjx-rules-title { text-align: center; font-size: 20px; color: #333; margin-bottom: 24px; font-weight: 500; }
.cjx-rules-body p {
  font-size: 13px; color: #555; line-height: 1.8; margin: 10px 0;
}
.cjx-rule-num { padding-left: 1em; }

/* 记录表格 */
.cjx-records-header { margin-bottom: 16px; padding: 0 4px; }
.cjx-records-header h3 { font-size: 16px; color: #333; margin: 0; }
.cjx-records-table {
  width: 100%; border-collapse: collapse; font-size: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08); border-radius: 6px; overflow: hidden;
}
.cjx-records-table th, .cjx-records-table td {
  padding: 14px 18px; text-align: left;
}
.cjx-records-table th {
  background: #2c3e50; color: #fff; font-weight: 500;
  font-size: 13px; letter-spacing: 0.5px;
}
.cjx-records-table td {
  border-bottom: 1px solid #f0f0f0; color: #444;
}
.cjx-records-table tr:hover td { background: #fafbfc; }
.cjx-status { padding: 3px 10px; border-radius: 3px; font-size: 12px; }
.cjx-status-success { color: #27ae60; }
.cjx-status-pending { color: #f39c12; }
.cjx-status-failed { color: #e74c3c; }

.cjx-empty { text-align: center; padding: 60px 0; color: #aaa; font-size: 14px; }

/* Toast */
.cjx-toast {
  position: fixed; top: 20px; left: 50%; transform: translateX(-50%);
  background: rgba(0,0,0,0.85); color: #fff; padding: 10px 24px;
  border-radius: 6px; font-size: 14px; z-index: 9999;
}
</style>

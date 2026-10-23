<template>
  <div>
    <!-- 顶部说明 -->
    <div class="pay-head">
      <div>
        <h3 class="pay-title">支付渠道配置</h3>
        <p class="pay-desc">开关控制用户端可见/可用的提现渠道；费率、最低/最高手续费实时生效，用户端预览与后端结算一致。内置渠道（支付宝/微信/银行卡/余额）不可删除，自定义渠道无历史引用时可删除。</p>
      </div>
      <button class="btn btn-primary btn-small" @click="openCreate">+ 新增渠道</button>
    </div>

    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>渠道</th>
            <th>编码</th>
            <th>用途</th>
            <th>费率(%)</th>
            <th>最低费(¥)</th>
            <th>最高费(¥)</th>
            <th>排序</th>
            <th>开关</th>
            <th>备注</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in list" :key="m.id">
            <td>
              <input v-model="m.method_name" class="cell-input" style="width: 90px" />
            </td>
            <td class="td-monospace">{{ m.method_code }}</td>
            <td><span class="badge badge-default">{{ typeLabel(m.type) }}</span></td>
            <td>
              <input v-model.number="m.fee_rate" type="number" step="0.001" min="0" class="cell-input" style="width: 70px" />
            </td>
            <td>
              <input v-model.number="m.min_fee" type="number" step="0.01" min="0" class="cell-input" style="width: 70px" />
            </td>
            <td>
              <input v-model.number="m.max_fee" type="number" step="0.01" min="0" class="cell-input" style="width: 70px" />
            </td>
            <td>
              <input v-model.number="m.sort_order" type="number" class="cell-input" style="width: 60px" />
            </td>
            <td>
              <button
                class="btn btn-small"
                :class="m.is_active ? 'btn-active' : 'btn-inactive'"
                @click="toggleActive(m)"
              >{{ m.is_active ? '启用' : '停用' }}</button>
            </td>
            <td>
              <input v-model="m.remark" class="cell-input" style="width: 150px" />
            </td>
            <td class="td-actions">
              <button class="btn btn-primary btn-small" :disabled="m._saving" @click="save(m)">{{ m._saving ? '保存中' : '保存' }}</button>
              <button
                class="btn btn-danger btn-small"
                :disabled="isBuiltin(m) || m._deleting"
                :title="isBuiltin(m) ? '内置渠道不可删除，请改用停用' : '删除该渠道'"
                @click="removeMethod(m)"
              >{{ m._deleting ? '删除中' : '删除' }}</button>
            </td>
          </tr>
          <tr v-if="list.length === 0">
            <td colspan="10" class="td-empty">暂无支付渠道配置</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增渠道弹窗 -->
    <div v-if="showCreate" class="pay-modal-mask" @click.self="showCreate = false">
      <div class="pay-modal">
        <h4 class="pay-modal__title">新增支付渠道</h4>
        <div class="pay-modal__grid">
          <label>渠道编码<input v-model="form.method_code" class="cell-input" placeholder="如 unionpay" /></label>
          <label>渠道名称<input v-model="form.method_name" class="cell-input" placeholder="如 云闪付" /></label>
          <label>用途
            <select v-model="form.type" class="cell-input">
              <option value="both">充值 + 提现</option>
              <option value="recharge">仅充值</option>
              <option value="withdraw">仅提现</option>
            </select>
          </label>
          <label>费率(%)<input v-model.number="form.fee_rate" type="number" step="0.001" min="0" class="cell-input" /></label>
          <label>最低费(¥)<input v-model.number="form.min_fee" type="number" step="0.01" min="0" class="cell-input" /></label>
          <label>最高费(¥)<input v-model.number="form.max_fee" type="number" step="0.01" min="0" class="cell-input" /></label>
          <label>排序<input v-model.number="form.sort_order" type="number" class="cell-input" /></label>
          <label>备注<input v-model="form.remark" class="cell-input" /></label>
        </div>
        <div class="pay-modal__footer">
          <button class="btn btn-small" @click="showCreate = false">取消</button>
          <button class="btn btn-primary btn-small" :disabled="creating" @click="createMethod">{{ creating ? '创建中' : '创建' }}</button>
        </div>
      </div>
    </div>

    <div v-if="toast" class="pay-toast">{{ toast }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const list = ref<any[]>([])
const toast = ref('')
const showCreate = ref(false)
const creating = ref(false)
const form = ref<any>({
  method_code: '', method_name: '', type: 'both',
  fee_rate: 0, min_fee: 0, max_fee: 0, sort_order: 0, remark: ''
})

/** 内置渠道：被充值/提现主流程硬编码使用，只允许停用不允许删除 */
const BUILTIN_CODES = ['alipay', 'wechat', 'bank', 'balance']
function isBuiltin(m: any) {
  return BUILTIN_CODES.includes(String(m.method_code))
}

function showToast(text: string) {
  toast.value = text
  window.setTimeout(() => { toast.value = '' }, 1800)
}

function openCreate() {
  form.value = {
    method_code: '', method_name: '', type: 'both',
    fee_rate: 0, min_fee: 0, max_fee: 0, sort_order: (list.value.length + 1), remark: ''
  }
  showCreate.value = true
}

async function createMethod() {
  if (!form.value.method_code.trim() || !form.value.method_name.trim()) {
    alert('渠道编码和渠道名称不能为空')
    return
  }
  creating.value = true
  try {
    const r = await axios.post('/api/admin/payment-methods', form.value)
    if (r.data?.code !== 200) { alert(r.data?.message || '创建失败'); return }
    showCreate.value = false
    showToast('渠道已创建')
    await load()
  } catch (e: any) {
    alert('创建失败: ' + (e?.response?.data?.message || e.message))
  } finally {
    creating.value = false
  }
}

async function removeMethod(m: any) {
  if (isBuiltin(m)) { alert('内置渠道不可删除，请改用停用'); return }
  if (!window.confirm(`确定删除渠道「${m.method_name}」？删除后不可恢复。`)) return
  m._deleting = true
  try {
    const r = await axios.delete(`/api/admin/payment-methods/${m.id}`)
    if (r.data?.code !== 200) { alert(r.data?.message || '删除失败'); return }
    list.value = list.value.filter(x => x.id !== m.id)
    showToast('渠道已删除')
  } catch (e: any) {
    alert('删除失败: ' + (e?.response?.data?.message || e.message))
  } finally {
    m._deleting = false
  }
}

async function load() {
  try {
    const r = await axios.get('/api/admin/payment-methods')
    list.value = (r.data?.data || []).map((m: any) => ({ ...m, _saving: false }))
  } catch (e) {
    console.error('加载支付渠道失败', e)
    alert('加载支付渠道失败，请确认后端已重启')
  }
}

function typeLabel(t: string) {
  return ({ withdraw: '提现', recharge: '充值' } as any)[t] || t
}

async function toggleActive(m: any) {
  m.is_active = !m.is_active
  await save(m)
}

async function save(m: any) {
  m._saving = true
  try {
    const r = await axios.put(`/api/admin/payment-methods/${m.id}`, {
      method_name: m.method_name,
      fee_rate: m.fee_rate,
      min_fee: m.min_fee,
      max_fee: m.max_fee,
      sort_order: m.sort_order,
      is_active: m.is_active,
      remark: m.remark
    })
    const updated = r.data?.data
    if (updated) {
      const idx = list.value.findIndex(x => x.id === m.id)
      if (idx >= 0) list.value[idx] = { ...updated, _saving: false }
      showToast('已保存')
    }
  } catch (e: any) {
    alert('保存失败: ' + (e?.response?.data?.message || e.message))
  } finally {
    m._saving = false
  }
}

onMounted(load)
</script>

<style scoped>
.pay-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 16px; }
.pay-title { margin: 0 0 4px; font-size: 16px; color: #2c3e50; }
.pay-desc { margin: 0; font-size: 13px; color: #888; }

.card { background: #fff; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table thead { background: #f5f7fa; }
.data-table th { text-align: left; padding: 12px; font-weight: 600; color: #555; border-bottom: 1px solid #eee; }
.data-table td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; color: #333; vertical-align: middle; }
.data-table tr:hover td { background: #fafbfc; }

.cell-input {
  border: 1px solid #ddd; border-radius: 4px; padding: 5px 8px; font-size: 13px;
  width: 100%; box-sizing: border-box; background: #fff;
}
.cell-input:focus { outline: none; border-color: #4a6cf7; }
.td-monospace { font-family: monospace; color: #888; font-size: 12px; }
.td-actions { white-space: nowrap; }
.td-empty { text-align: center; padding: 40px; color: #aaa; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-default { background: #f0f0f0; color: #888; }

.btn { padding: 6px 12px; border-radius: 6px; border: none; cursor: pointer; font-size: 13px; transition: all 0.15s; }
.btn-small { font-size: 12px; padding: 4px 10px; }
.btn-primary { background: #4a6cf7; color: #fff; }
.btn-primary:hover:not(:disabled) { background: #3a5ce5; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-active { background: #e8f8ef; color: #27ae60; border: 1px solid #b7e4c7; }
.btn-active:hover { background: #d4f2df; }
.btn-inactive { background: #fdecea; color: #c0392b; border: 1px solid #f5b7b1; }
.btn-inactive:hover { background: #fadbd8; }
.btn-danger { background: #fff; color: #c0392b; border: 1px solid #f5b7b1; margin-left: 6px; }
.btn-danger:hover:not(:disabled) { background: #fdecea; }
.btn-danger:disabled { opacity: 0.45; cursor: not-allowed; }

.pay-modal-mask {
  position: fixed; inset: 0; background: rgba(0, 0, 0, 0.35);
  display: flex; align-items: center; justify-content: center; z-index: 2000;
}
.pay-modal { background: #fff; border-radius: 10px; width: 520px; max-width: 92vw; padding: 20px 22px; }
.pay-modal__title { margin: 0 0 14px; font-size: 15px; color: #2c3e50; }
.pay-modal__grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 16px; }
.pay-modal__grid label { display: flex; flex-direction: column; gap: 5px; font-size: 12px; color: #888; }
.pay-modal__footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 18px; }

.pay-toast {
  position: fixed; left: 50%; transform: translateX(-50%); bottom: 60px;
  background: rgba(44, 62, 80, 0.92); color: #fff; font-size: 13px;
  padding: 8px 18px; border-radius: 18px; z-index: 2100;
}
</style>

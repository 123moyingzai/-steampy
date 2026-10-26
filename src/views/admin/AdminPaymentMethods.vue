<template>
  <div>
    <!-- 顶部说明 -->
    <div class="pay-head">
      <div>
        <h3 class="pay-title">支付渠道配置</h3>
        <p class="pay-desc">开关控制用户端可见/可用的提现渠道；费率、最低/最高手续费实时生效，用户端预览与后端结算一致。</p>
      </div>
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
            </td>
          </tr>
          <tr v-if="list.length === 0">
            <td colspan="10" class="td-empty">暂无支付渠道配置</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const list = ref<any[]>([])

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
</style>

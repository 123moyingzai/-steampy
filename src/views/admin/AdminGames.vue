<template>
  <div class="admin-games">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-box">
        <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索游戏名称..."
          @input="filterGames"
        >
      </div>
      <button class="btn btn-outline" :disabled="exporting" @click="handleExport">
        {{ exporting ? '导出中...' : '导出 CSV' }}
      </button>
      <button class="btn btn-primary" @click="openCreateModal">
        <span>+</span> 新增游戏
      </button>
    </div>

    <!-- 游戏列表 -->
    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>封面</th>
            <th>游戏名称</th>
            <th>现价</th>
            <th>原价</th>
            <th>折扣</th>
            <th>库存</th>
            <th>类型</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="game in paginatedGames" :key="game.id">
            <td class="mono">{{ game.id }}</td>
            <td>
              <div class="game-thumb">
                <img :src="getImageUrl(game.image)" :alt="game.name_cn || game.name" @error="onImgError">
              </div>
            </td>
            <td class="game-name">{{ game.name_cn || game.name }}</td>
            <td class="price">¥{{ stripYuan(game.price) }}</td>
            <td class="original-price">¥{{ stripYuan(game.original_price) || '-' }}</td>
            <td>
              <span class="discount">-{{ game.discount || '0' }}%</span>
            </td>
            <td :title="'在售 CDKey 数量（由卖家上架决定）'">{{ stockMap[String(game.id)] || 0 }} 件</td>
            <td>
              <span class="badge" :class="game.is_presale ? 'warning' : 'success'">
                {{ game.is_presale ? '预售' : '正式' }}
              </span>
            </td>
            <td class="actions">
              <button class="btn-link" @click="openEditModal(game)">编辑</button>
              <button class="btn-link danger" @click="handleDelete(game)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredGames.length === 0" class="empty-state">
        <p>暂无游戏数据</p>
      </div>

      <!-- 分页 -->
      <div v-if="filteredGames.length > 0" class="pagination">
        <span class="page-info">共 {{ filteredGames.length }} 条</span>
        <select v-model.number="pageSize" @change="currentPage = 1" class="page-size">
          <option :value="20">20 条/页</option>
          <option :value="50">50 条/页</option>
          <option :value="100">100 条/页</option>
        </select>
        <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">上一页</button>
        <template v-for="p in displayPages" :key="p">
          <span v-if="p === '...'" class="page-ellipsis">...</span>
          <button v-else class="page-btn" :class="{ active: p === currentPage }" @click="currentPage = p">{{ p }}</button>
        </template>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-lg">
        <div class="modal-header">
          <h3>{{ editingGame ? '编辑游戏' : '新增游戏' }}</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>游戏名称 *</label>
              <input type="text" v-model="formData.name" placeholder="请输入游戏名称"
                     @keyup.enter="autoFillByName" @blur="checkExists">
              <p v-if="nameExists" class="field-hint error">
                游戏「{{ existingName }}」已存在，不能重复添加
                <button type="button" class="btn-link" @click="editExistingGame">去编辑该游戏</button>
              </p>
            </div>
            <div class="form-group">
              <label>开发商</label>
              <input type="text" v-model="formData.developer" placeholder="开发商名称">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>现价 (¥) *</label>
              <input type="number" v-model.number="formData.price" step="0.01" min="0">
            </div>
            <div class="form-group">
              <label>原价 (¥)</label>
              <input type="number" v-model.number="formData.original_price" step="0.01" min="0">
            </div>
            <div class="form-group">
              <label>折扣 (%)</label>
              <input type="number" v-model.number="formData.discount" min="0" max="100">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>在售数量</label>
              <input type="text" :value="(stockMap[String(formData.id)] || 0) + ' 件'" disabled
                     title="由卖家上架的 CDKey 数量自动汇总，不可手工修改">
              <p class="field-hint">在售库存由卖家上架决定，管理员不再手工填写</p>
            </div>
            <div class="form-group">
              <label>是否预售</label>
              <select v-model="formData.is_presale">
                <option :value="false">正式发售</option>
                <option :value="true">预售</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>封面图片URL</label>
            <div class="image-row">
              <input type="text" v-model="formData.image" placeholder="https://... 或 /picture/xxx.jpg"
                     @input="imageStatus = 'loading'" @blur="checkImage">
              <button type="button" class="btn btn-default btn-download" :disabled="downloading"
                      @click="downloadImage">
                {{ downloading ? '下载中...' : '下载到本地' }}
              </button>
            </div>
            <div v-if="previewUrl" class="image-preview">
              <img :src="previewUrl" alt="封面预览"
                   @load="imageStatus = 'ok'" @error="imageStatus = 'error'">
              <span class="field-hint" :class="{ error: imageStatus === 'error' }">
                {{ imageStatus === 'ok' ? '图片可正常访问' : imageStatus === 'error' ? '图片无法访问，保存时会自动尝试下载' : '正在检测图片...' }}
              </span>
            </div>
          </div>

          <div class="form-group">
            <label>游戏描述</label>
            <textarea v-model="formData.description" rows="3" placeholder="游戏简介..."></textarea>
          </div>

          <div class="form-group">
            <label>发售日期</label>
            <input type="date" v-model="formData.release_date">
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ai footer-ai" :disabled="aiFilling" @click="autoFillByName">
            {{ aiFilling ? 'AI 生成中...' : 'AI 自动填写' }}
          </button>
          <button class="btn btn-default" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSave"
                  :disabled="saving || nameExists"
                  :title="nameExists ? '该游戏已存在，不能重复添加' : ''">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import { adminExportAPI } from '../../config/admin-api'
import { getImageUrl, onImgError } from '@/composables/useGameImage'

const games = ref<any[]>([])
const filteredGames = ref<any[]>([])
/** 各游戏在售 CDKey 数量（由 listings 聚合，管理端只读展示） */
const stockMap = ref<Record<string, number>>({})
const searchKeyword = ref('')
const showModal = ref(false)
const editingGame = ref<any>(null)
const saving = ref(false)
const exporting = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

const totalPages = computed(() => Math.max(1, Math.ceil(filteredGames.value.length / pageSize.value)))
const paginatedGames = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredGames.value.slice(start, start + pageSize.value)
})
const displayPages = computed(() => {
  const total = totalPages.value, cur = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages: (number | string)[] = [1]
  if (cur > 3) pages.push('...')
  for (let i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) pages.push(i)
  if (cur < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

watch(filteredGames, () => { currentPage.value = 1 })

const formData = reactive({
  id: '' as string,
  name: '', developer: '', price: 0, original_price: 0, discount: 0,
  is_presale: false, image: '',
  description: '', release_date: ''
})

const resetForm = () => {
  Object.assign(formData, { id: '', name: '', developer: '', price: 0, original_price: 0, discount: 0,
    is_presale: false, image: '', description: '', release_date: '' })
  nameExists.value = false
  existingName.value = ''
  imageStatus.value = 'loading'
}

/** 游戏名重复检测结果与提示文案 */
const nameExists = ref(false)
const existingName = ref('')
const existingId = ref('')
/** 封面图片可用性：loading 检测中 / ok 可访问 / error 加载失败 */
const imageStatus = ref<'loading' | 'ok' | 'error'>('loading')
const downloading = ref(false)

/** 预览地址：相对路径补前导斜杠，网络地址原样使用 */
const previewUrl = computed(() => {
  const p = (formData.image || '').trim()
  if (!p) return ''
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  return p.startsWith('/') ? p : '/' + p
})

/** 名称失焦时查重（编辑时排除自身） */
const checkExists = async () => {
  const name = formData.name.trim()
  if (!name) { nameExists.value = false; return }
  try {
    const r = await axios.get('/api/admin/games/exists', { params: { name } })
    const d: any = r.data?.data || {}
    const selfId = editingGame.value ? String(editingGame.value.id) : null
    if (d.exists && (!selfId || String(d.id) !== selfId)) {
      nameExists.value = true
      existingName.value = d.name || name
      existingId.value = String(d.id || '')
    } else {
      nameExists.value = false
      existingId.value = ''
    }
  } catch {
    nameExists.value = false
  }
}

/** 图片地址变化后重新检测（img 的 load/error 回调负责置状态） */
const checkImage = () => {
  if ((formData.image || '').trim()) imageStatus.value = 'loading'
}

/** 把网络封面下载到服务器本地，保证后续长期可显示 */
const downloadImage = async () => {
  const url = (formData.image || '').trim()
  if (!url.startsWith('http')) { alert('请先填写 http(s) 开头的图片地址'); return }
  downloading.value = true
  try {
    const r = await axios.post('/api/upload/remote-image', { url }, { timeout: 40000 })
    const d: any = r.data?.data
    if (!d?.url) throw new Error(r.data?.message || '下载失败')
    formData.image = d.url
    alert('已下载到本地：' + d.url)
  } catch (e: any) {
    alert('下载失败: ' + (e?.response?.data?.message || e.message))
  } finally {
    downloading.value = false
  }
}

/** AI 自动填写：按游戏名补全开发商 / 封面 / 简介 / 发售日期 */
const aiFilling = ref(false)
const autoFillByName = async () => {
  const name = formData.name.trim()
  if (!name) { alert('请先填写游戏名称'); return }
  if (aiFilling.value) return
  // 已存在的游戏不再重复生成内容
  await checkExists()
  if (nameExists.value) {
    alert('游戏「' + existingName.value + '」已存在，无需再自动填写')
    return
  }
  aiFilling.value = true
  try {
    const r = await axios.post('/api/ai/game-info', { name }, { timeout: 45000 })
    const d: any = r.data?.data || {}
    // 只补空字段，不覆盖用户已经手填的内容
    let filled = 0
    const keys = ['developer', 'image', 'description', 'release_date',
      'price', 'original_price', 'discount', 'is_presale'] as const
    for (const key of keys) {
      const val: any = d[key]
      if (val === undefined || val === null || val === '') continue
      // 折扣 0 没有填写意义，其余数字（含免费游戏的 0 元）按实填写
      if (typeof val === 'number' && val === 0 && key !== 'price' && key !== 'original_price') continue
      if (!formData[key]) {
        ;(formData as any)[key] = val
        filled++
      }
    }
    if (!filled) {
      alert('没有可自动填写的新内容，表单已有值或未识别到该游戏')
    } else {
      alert(`已自动填写 ${filled} 项${d.steam_appid ? '（封面来自 Steam AppID ' + d.steam_appid + '）' : ''}`)
    }
  } catch (e: any) {
    alert('自动填写失败: ' + (e?.response?.data?.message || e.message))
  } finally {
    aiFilling.value = false
  }
}

const loadGames = async () => {
  try {
    const r = await axios.get('/api/admin/games')
    games.value = r.data?.data || []
    filteredGames.value = [...games.value]
    await loadStockStats()
  } catch (e) { console.error('加载游戏失败', e) }
}

/** 拉取各游戏在售数量（listings 聚合），用于「库存」列只读展示 */
const loadStockStats = async () => {
  try {
    const r = await axios.get('/api/admin/games/stock-stats')
    const map: Record<string, number> = {}
    for (const row of (r.data?.data || [])) {
      const key = String(row.game_id ?? row.gameId ?? '')
      if (key) map[key] = Number(row.cnt ?? 0)
    }
    stockMap.value = map
  } catch { stockMap.value = {} }
}

const filterGames = () => {
  const kw = searchKeyword.value.trim().toLowerCase()
  filteredGames.value = kw ? games.value.filter(g =>
    (g.name_cn || '').includes(kw) || (g.name || '').toLowerCase().includes(kw)
  ) : [...games.value]
}

const openCreateModal = () => { editingGame.value = null; resetForm(); showModal.value = true }
const openEditModal = (game: any) => {
  editingGame.value = game
  Object.assign(formData, {
    id: String(game.id || ''),
    name: game.name, developer: game.developer || '',
    price: Number(game.price || 0), original_price: Number(game.original_price || 0),
    discount: Number(game.discount || 0),
    is_presale: !!game.is_presale, image: game.image || '',
    description: game.description || '',
    release_date: game.release_date ? game.release_date.slice(0, 10) : ''
  })
  nameExists.value = false; existingId.value = ''
  showModal.value = true
}
/** 重名时直接跳到编辑该游戏，避免"想补录资料却被拦住" */
const editExistingGame = () => {
  const target = games.value.find(g => String(g.id) === existingId.value)
  if (target) openEditModal(target)
  else alert('列表里没找到该游戏，请刷新后重试')
}
const closeModal = () => { showModal.value = false; resetForm() }

const handleSave = async () => {
  if (!formData.name.trim()) { alert('请输入游戏名称'); return }
  if (formData.price < 0) { alert('价格不能为负'); return }
  if (nameExists.value) { alert('游戏「' + existingName.value + '」已存在，不能重复添加'); return }
  saving.value = true
  try {
    const payload: any = {
      name: formData.name, developer: formData.developer,
      price: formData.price, original_price: formData.original_price || null,
      discount: formData.discount || 0,
      is_presale: formData.is_presale, image: formData.image,
      description: formData.description, release_date: formData.release_date || null
    }
    // 网络封面先下载到服务器本地，避免 CDN 失效导致列表图片空白；
    // 下载失败不阻断保存，保留网络地址即可，否则管理员会因为图片问题卡在新增流程
    let imageLocalized = true
    let imageError = ''
    if (payload.image && String(payload.image).trim().startsWith('http')) {
      try {
        const r = await axios.post('/api/upload/remote-image',
          { url: String(payload.image).trim() }, { timeout: 40000 })
        if (r.data?.data?.url) {
          payload.image = r.data.data.url
        } else {
          throw new Error(r.data?.message || '下载失败')
        }
      } catch (e: any) {
        imageLocalized = false
        imageError = e?.response?.data?.message || e.message
      }
    }
    const imageHint = imageLocalized ? '' : `（封面未下载到本地，已保留网络地址：${imageError}）`
    if (editingGame.value) {
      await axios.put(`/api/admin/games/${editingGame.value.id}`, payload)
      alert('更新成功' + imageHint)
    } else {
      await axios.post('/api/admin/games', payload)
      alert('创建成功' + imageHint)
    }
    closeModal(); await loadGames()
  } catch (e: any) { alert('保存失败: ' + (e?.response?.data?.message || e.message)) }
  finally { saving.value = false }
}

const handleDelete = async (game: any) => {
  if (!await confirm(`确定删除游戏 "${game.name_cn || game.name}" 吗？此操作不可恢复！`)) return
  try {
    await axios.delete(`/api/admin/games/${game.id}`)
    alert('删除成功'); await loadGames()
  } catch (e: any) { alert('删除失败: ' + (e?.response?.data?.message || e.message)) }
}

const stripYuan = (val: any): string => {
  if (val === null || val === undefined || val === '') return ''
  return String(val).replace(/[¥$￥]/g, '').trim()
}

/** 导出游戏列表 CSV（后端 /api/admin/export/games） */
const handleExport = async () => {
  exporting.value = true
  try {
    await adminExportAPI.downloadCsv('games')
  } catch (e: any) {
    alert('导出失败: ' + (e?.response?.data?.message || e.message))
  } finally { exporting.value = false }
}

onMounted(loadGames)
</script>

<style scoped>
.admin-games {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar {
  display: flex;
  gap: 12px;
}

.search-box {
  flex: 1;
}

.search-box input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.search-box input:focus {
  outline: none;
  border-color: #3498db;
}

.btn {
  padding: 10px 18px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #3498db;
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-ai {
  background: linear-gradient(135deg, #7b5cf0, #4f9df7);
  color: #fff;
  white-space: nowrap;
}

.btn-ai:hover:not(:disabled) {
  opacity: 0.9;
}

/* 弹窗左下角：自动填写按钮靠左，取消/保存保持右侧 */
.footer-ai {
  margin-right: auto;
}

/* 封面 URL 输入行 + 预览 */
.image-row {
  display: flex;
  gap: 8px;
}

.image-row input {
  flex: 1;
  min-width: 0;
}

.btn-download {
  white-space: nowrap;
}

.image-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.image-preview img {
  width: 140px;
  height: 52px;
  object-fit: cover;
  border-radius: 4px;
  background: #f2f4f8;
}

.field-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: #8a94a6;
}

.field-hint.error {
  color: #e5484d;
}

.btn-outline {
  background: #fff;
  color: #3498db;
  border: 1px solid #3498db;
}

.btn-outline:hover:not(:disabled) {
  background: #eaf4fc;
}

.btn-default {
  background: #eee;
  color: #333;
}

.btn-default:hover {
  background: #ddd;
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  text-align: left;
  padding: 12px 12px;
  color: #666;
  font-weight: 500;
  background: #f5f5f5;
  border-bottom: 1px solid #ddd;
  font-size: 12px;
  white-space: nowrap;
}

.data-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  color: #333;
  vertical-align: middle;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table tr:hover td {
  background: #f5f5f5;
}

.mono {
  font-family: 'Menlo', monospace;
  font-size: 12px;
  color: #999;
}

.game-thumb {
  width: 48px;
  height: 32px;
  border-radius: 4px;
  overflow: hidden;
  background: #eee;
}

.game-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.game-name {
  font-weight: 500;
  color: #333;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-weight: 600;
  color: #e74c3c;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 12px;
}

.discount {
  color: #f39c12;
  font-weight: 600;
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

.actions {
  white-space: nowrap;
}

.btn-link {
  background: none;
  border: none;
  color: #3498db;
  cursor: pointer;
  font-size: 13px;
  margin-right: 12px;
  padding: 0;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link.danger {
  color: #e74c3c;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 16px;
  border-top: 1px solid #eee;
  background: #fafafa;
}
.page-info { font-size: 12px; color: #666; }
.page-size {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  background: #fff;
}
.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  color: #333;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: #3498db; color: #3498db; }
.page-btn.active { background: #3498db; color: #fff; border-color: #3498db; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-ellipsis { font-size: 12px; color: #999; padding: 0 4px; }

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal {
  background: #fff;
  border-radius: 12px;
  width: 100%;
  max-width: 520px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-lg {
  max-width: 640px;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-row .form-group {
  flex: 1;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: #333;
  margin-bottom: 6px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group textarea {
  resize: vertical;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #10b981;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

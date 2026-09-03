﻿﻿﻿﻿﻿<template>
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
          <tr v-for="game in filteredGames" :key="game.id">
            <td class="mono">{{ game.id }}</td>
            <td>
              <div class="game-thumb">
                <img :src="game.image_url || game.image || '/picture/安魂曲.jpg'" :alt="game.name">
              </div>
            </td>
            <td class="game-name">{{ game.name }}</td>
            <td class="price">¥{{ stripYuan(game.price) }}</td>
            <td class="original-price">¥{{ stripYuan(game.original_price) || '-' }}</td>
            <td>
              <span class="discount">-{{ game.discount || '0' }}%</span>
            </td>
            <td>{{ game.stock ?? '∞' }}</td>
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
              <input type="text" v-model="formData.name" placeholder="请输入游戏名称">
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
              <label>库存</label>
              <input type="number" v-model.number="formData.stock" min="0" placeholder="留空为无限">
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
            <input type="text" v-model="formData.image_url" placeholder="https://... 或 /picture/xxx.jpg">
          </div>

          <div class="form-group">
            <label>跳转链接</label>
            <input type="text" v-model="formData.link" placeholder="点击游戏时跳转的路由或URL">
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
          <button class="btn btn-default" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { adminGameAPI } from '../../config/admin-api'

const games = ref<any[]>([])
const filteredGames = ref<any[]>([])
const searchKeyword = ref('')
const showModal = ref(false)
const editingGame = ref<any>(null)
const saving = ref(false)

const formData = reactive({
  name: '',
  developer: '',
  price: 0,
  original_price: 0,
  discount: 0,
  stock: null as number | null,
  is_presale: false,
  image_url: '',
  link: '',
  description: '',
  release_date: ''
})

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    developer: '',
    price: 0,
    original_price: 0,
    discount: 0,
    stock: null,
    is_presale: false,
    image_url: '',
    link: '',
    description: '',
    release_date: ''
  })
}

const loadGames = async () => {
  games.value = await adminGameAPI.getGames()
  filteredGames.value = [...games.value]
}

const filterGames = () => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) {
    filteredGames.value = [...games.value]
    return
  }
  filteredGames.value = games.value.filter(g =>
    (g.name || '').toLowerCase().includes(kw)
  )
}

const openCreateModal = () => {
  editingGame.value = null
  resetForm()
  showModal.value = true
}

const openEditModal = (game: any) => {
  editingGame.value = game
  Object.assign(formData, {
    name: game.name,
    developer: game.developer || '',
    price: game.price || 0,
    original_price: game.original_price || 0,
    discount: game.discount || 0,
    stock: game.stock ?? null,
    is_presale: !!game.is_presale,
    image_url: game.image_url || game.image || '',
    link: game.link || '',
    description: game.description || '',
    release_date: game.release_date ? game.release_date.slice(0, 10) : ''
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const handleSave = async () => {
  if (!formData.name.trim()) {
    alert('请输入游戏名称')
    return
  }
  if (!formData.price || formData.price < 0) {
    alert('请输入有效的价格')
    return
  }

  saving.value = true
  try {
    const payload: any = {
      name: formData.name,
      developer: formData.developer,
      price: formData.price,
      original_price: formData.original_price || null,
      discount: formData.discount || 0,
      stock: formData.stock,
      is_presale: formData.is_presale,
      image_url: formData.image_url,
      link: formData.link,
      description: formData.description,
      release_date: formData.release_date || null
    }

    if (editingGame.value) {
      const result = await adminGameAPI.updateGame(editingGame.value.id, payload)
      if (result.error) {
        alert('更新失败: ' + result.error)
      } else {
        alert('更新成功')
        closeModal()
        await loadGames()
      }
    } else {
      const result = await adminGameAPI.createGame(payload)
      if (result.error) {
        alert('创建失败: ' + result.error)
      } else {
        alert('创建成功')
        closeModal()
        await loadGames()
      }
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async (game: any) => {
  if (!confirm(`确定删除游戏 "${game.name}" 吗？此操作不可恢复！`)) return
  const result = await adminGameAPI.deleteGame(game.id)
  if (result.error) {
    alert('删除失败: ' + result.error)
  } else {
    alert('删除成功')
    await loadGames()
  }
}

const stripYuan = (val: any): string => {
  if (val === null || val === undefined || val === '') return ''
  return String(val).replace(/[¥$￥]/g, '').trim()
}

onMounted(() => {
  loadGames()
})
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

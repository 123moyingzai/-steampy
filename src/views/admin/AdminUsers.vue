﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="admin-users">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-box">
        <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索用户名、昵称、手机号..."
          @input="onSearchInput"
        >
      </div>
      <select v-model="roleFilter" @change="onSearchInput" class="role-select">
        <option value="">全部角色</option>
        <option value="普通用户">普通用户</option>
        <option value="管理员">管理员</option>
        <option value="已封禁">已封禁</option>
      </select>
      <button class="btn btn-primary" @click="openCreateModal">
        <span>+</span> 新增用户
      </button>
    </div>

    <!-- 用户列表 -->
    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>角色</th>
            <th>钱包余额</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td class="mono">{{ String(user.id).slice(0, 8) }}</td>
            <td class="username">{{ user.username }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span
                class="badge"
                :class="getUserBadgeClass(user)"
              >{{ getUserBadgeText(user) }}</span>
            </td>
            <td class="price">¥{{ user.wallet_balance || '0.00' }}</td>
            <td class="time">{{ formatTime(user.created_at) }}</td>
            <td class="actions">
              <button class="btn-link" @click="openEditModal(user)">编辑</button>
              <button
                class="btn-link"
                :class="isUserBanned(user) ? 'success' : 'danger'"
                @click="isUserBanned(user) ? handleUnban(user) : handleBan(user)"
              >{{ isUserBanned(user) ? '解禁' : '封禁' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <p>暂无用户数据</p>
      </div>
      <!-- 分页条 -->
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条 · 第 {{ page }}/{{ totalPages }} 页</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
          <button class="page-btn" v-for="p in visiblePages" :key="p" :class="{ active: p === page }" @click="goPage(p)">{{ p }}</button>
          <button class="page-btn" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
          <select v-model.number="size" @change="goPage(1)" class="size-select">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingUser ? '编辑用户' : '新增用户' }}</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="formData.username" :disabled="!!editingUser">
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input type="text" v-model="formData.nickname">
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input type="text" v-model="formData.phone">
          </div>
          <div v-if="!editingUser" class="form-group">
            <label>密码</label>
            <input type="password" v-model="formData.password" placeholder="至少6位">
          </div>
          <div v-if="editingUser" class="form-group">
            <label>重置密码 (留空不修改)</label>
            <input type="password" v-model="formData.password" placeholder="输入新密码">
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="formData.user_type">
              <option value="普通用户">普通用户</option>
              <option value="管理员">管理员</option>
            </select>
          </div>
          <div class="form-group">
            <label>钱包余额</label>
            <input type="number" v-model.number="formData.wallet_balance" step="0.01">
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
import { ref, reactive, onMounted, computed } from 'vue'
import axios from 'axios'

const filteredUsers = ref<any[]>([])
const searchKeyword = ref('')
const roleFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showModal = ref(false)
const editingUser = ref<any>(null)
const saving = ref(false)

// 搜索 debounce timer
let searchTimer: ReturnType<typeof setTimeout> | null = null

const formData = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  user_type: '普通用户',
  wallet_balance: 0
})

const resetForm = () => {
  formData.username = ''; formData.nickname = ''; formData.phone = ''
  formData.password = ''; formData.user_type = '普通用户'; formData.wallet_balance = 0
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const visiblePages = computed(() => {
  const tp = totalPages.value
  const cur = page.value
  // 显示当前页 ±1
  const list: number[] = []
  const start = Math.max(1, cur - 1)
  const end = Math.min(tp, start + 2)
  for (let i = start; i <= end; i++) list.push(i)
  return list
})

const loadUsers = async () => {
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      size: String(size.value)
    })
    if (searchKeyword.value.trim()) params.set('keyword', searchKeyword.value.trim())
    if (roleFilter.value) params.set('role', roleFilter.value)

    const r = await axios.get('/api/admin/users?' + params.toString())
    const payload = r.data?.data || {}
    total.value = Number(payload.total) || 0
    filteredUsers.value = payload.list || []
  } catch (e) { console.error('加载用户失败', e) }
}

// debounce 300ms — 搜索输入不立刻打后端
const onSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadUsers()
  }, 300)
}

const goPage = (p: number) => {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadUsers()
}

const openCreateModal = () => { editingUser.value = null; resetForm(); showModal.value = true }
const openEditModal = (user: any) => {
  editingUser.value = user
  Object.assign(formData, {
    username: user.username, nickname: user.nickname || '', phone: user.phone || '',
    password: '', user_type: user.user_type || '普通用户', wallet_balance: user.wallet_balance || 0
  })
  showModal.value = true
}
const closeModal = () => { showModal.value = false; resetForm() }

const handleSave = async () => {
  if (!formData.username.trim()) { alert('请输入用户名'); return }
  saving.value = true
  try {
    if (editingUser.value) {
      // 只有角色通过 ban 端点能改；其他字段暂时后端没 admin update user 端点
      const action = formData.user_type === '管理员' ? 'admin'
                   : formData.user_type === '已封禁' ? 'ban'
                   : 'normal'
      await axios.put(`/api/admin/users/${editingUser.value.id}/ban`, { action })
      alert('更新成功')
      closeModal(); await loadUsers()
    } else {
      if (!formData.password || formData.password.length < 6) { alert('密码至少6位'); return }
      await axios.post('/api/auth/register', {
        username: formData.username, password: formData.password,
        nickname: formData.nickname || formData.username, phone: formData.phone
      })
      alert('创建成功')
      closeModal(); await loadUsers()
    }
  } catch (e: any) { alert('保存失败: ' + (e?.response?.data?.message || e.message)) }
  finally { saving.value = false }
}

const isUserBanned = (user: any) => user.user_type === '已封禁'
const getUserBadgeText = (user: any) => {
  if (user.user_type === '已封禁') return '已封禁'
  return user.user_type || '普通用户'
}
const getUserBadgeClass = (user: any) => {
  if (user.user_type === '已封禁') return 'banned'
  if (user.user_type === '管理员') return 'admin'
  return 'user'
}

const handleBan = async (user: any) => {
  if (!confirm(`确定封禁用户 "${user.username}" 吗？`)) return
  try {
    await axios.put(`/api/admin/users/${user.id}/ban`, { action: 'ban' })
    alert('封禁成功'); await loadUsers()
  } catch (e: any) { alert('封禁失败: ' + (e?.response?.data?.message || e.message)) }
}
const handleUnban = async (user: any) => {
  if (!confirm(`确定解禁用户 "${user.username}" 吗？`)) return
  try {
    await axios.put(`/api/admin/users/${user.id}/ban`, { action: 'normal' })
    alert('解禁成功'); await loadUsers()
  } catch (e: any) { alert('解禁失败: ' + (e?.response?.data?.message || e.message)) }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    const d = new Date(timeStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch { return timeStr }
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-users {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
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
  transition: border-color 0.2s;
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
  padding: 12px 16px;
  color: #666;
  font-weight: 500;
  background: #f5f5f5;
  border-bottom: 1px solid #ddd;
  font-size: 12px;
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #eee;
  color: #333;
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

.username {
  font-weight: 500;
  color: #333;
}

.price {
  font-weight: 600;
  color: #e74c3c;
}

.time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
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

.btn-link.success {
  color: #27ae60;
}

.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge.admin {
  background: #fdebd0;
  color: #d68910;
}

.badge.user {
  background: #dbeafe;
  color: #1e40af;
}

.badge.banned {
  background: #fdecea;
  color: #c0392b;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.role-select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.pagination-info {
  font-size: 13px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.page-btn:hover:not(:disabled):not(.active) {
  border-color: #3498db;
  color: #3498db;
}

.page-btn.active {
  background: #3498db;
  border-color: #3498db;
  color: #fff;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.size-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
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
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
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
  max-height: 60vh;
  overflow-y: auto;
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
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3498db;
}

.form-group input:disabled {
  background: #f5f5f5;
  color: #999;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

<template>
  <div class="admin-users">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-box">
        <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索用户名、昵称、手机号..."
          @input="filterUsers"
        >
      </div>
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
                :class="user.user_type === '管理员' ? 'admin' : 'user'"
              >{{ user.user_type || '普通用户' }}</span>
            </td>
            <td class="price">¥{{ user.wallet_balance || '0.00' }}</td>
            <td class="time">{{ formatTime(user.created_at) }}</td>
            <td class="actions">
              <button class="btn-link" @click="openEditModal(user)">编辑</button>
              <button class="btn-link danger" @click="handleDelete(user)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <p>暂无用户数据</p>
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
import { ref, reactive, onMounted } from 'vue'
import { adminUserAPI } from '../../config/admin-api'

const users = ref<any[]>([])
const filteredUsers = ref<any[]>([])
const searchKeyword = ref('')
const showModal = ref(false)
const editingUser = ref<any>(null)
const saving = ref(false)

const formData = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  user_type: '普通用户',
  wallet_balance: 0
})

const resetForm = () => {
  formData.username = ''
  formData.nickname = ''
  formData.phone = ''
  formData.password = ''
  formData.user_type = '普通用户'
  formData.wallet_balance = 0
}

const loadUsers = async () => {
  users.value = await adminUserAPI.getUsers()
  filteredUsers.value = [...users.value]
}

const filterUsers = () => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) {
    filteredUsers.value = [...users.value]
    return
  }
  filteredUsers.value = users.value.filter(u =>
    (u.username || '').toLowerCase().includes(kw) ||
    (u.nickname || '').toLowerCase().includes(kw) ||
    (u.phone || '').includes(kw)
  )
}

const openCreateModal = () => {
  editingUser.value = null
  resetForm()
  showModal.value = true
}

const openEditModal = (user: any) => {
  editingUser.value = user
  Object.assign(formData, {
    username: user.username,
    nickname: user.nickname || '',
    phone: user.phone || '',
    password: '',
    user_type: user.user_type || '普通用户',
    wallet_balance: user.wallet_balance || 0
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const handleSave = async () => {
  if (!formData.username.trim()) {
    alert('请输入用户名')
    return
  }

  saving.value = true
  try {
    if (editingUser.value) {
      const updateData: any = {
        nickname: formData.nickname,
        phone: formData.phone,
        user_type: formData.user_type,
        wallet_balance: formData.wallet_balance
      }
      if (formData.password) {
        updateData.password_hash = formData.password
      }
      const result = await adminUserAPI.updateUser(editingUser.value.id, updateData)
      if (result.error) {
        alert('更新失败: ' + result.error)
      } else {
        alert('更新成功')
        closeModal()
        await loadUsers()
      }
    } else {
      if (!formData.password || formData.password.length < 6) {
        alert('密码至少6位')
        return
      }
      const result = await adminUserAPI.createUser({
        username: formData.username,
        password_hash: formData.password,
        nickname: formData.nickname || formData.username,
        phone: formData.phone,
        user_type: formData.user_type,
        wallet_balance: formData.wallet_balance
      })
      if (result.error) {
        alert('创建失败: ' + result.error)
      } else {
        alert('创建成功')
        closeModal()
        await loadUsers()
      }
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async (user: any) => {
  if (!confirm(`确定删除用户 "${user.username}" 吗？此操作不可恢复！`)) return
  const result = await adminUserAPI.deleteUser(user.id)
  if (result.error) {
    alert('删除失败: ' + result.error)
  } else {
    alert('删除成功')
    await loadUsers()
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    const d = new Date(timeStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch {
    return timeStr
  }
}

onMounted(() => {
  loadUsers()
})
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
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.search-box input:focus {
  outline: none;
  border-color: #3b82f6;
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
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-default {
  background: #f3f4f6;
  color: #374151;
}

.btn-default:hover {
  background: #e5e7eb;
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
  color: #6b7280;
  font-weight: 500;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-size: 12px;
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table tr:hover td {
  background: #fafafa;
}

.mono {
  font-family: 'Menlo', monospace;
  font-size: 12px;
  color: #9ca3af;
}

.username {
  font-weight: 500;
  color: #1f2937;
}

.price {
  font-weight: 600;
  color: #dc2626;
}

.time {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}

.actions {
  white-space: nowrap;
}

.btn-link {
  background: none;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  font-size: 13px;
  margin-right: 12px;
  padding: 0;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link.danger {
  color: #dc2626;
}

.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge.admin {
  background: #fef3c7;
  color: #92400e;
}

.badge.user {
  background: #dbeafe;
  color: #1e40af;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
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
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px 8px;
}

.close-btn:hover {
  color: #374151;
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
  color: #374151;
  margin-bottom: 6px;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3b82f6;
}

.form-group input:disabled {
  background: #f9fafb;
  color: #9ca3af;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #f3f4f6;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

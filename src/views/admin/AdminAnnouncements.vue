﻿<template>
  <div class="admin-announcements">
    <!-- 工具栏 -->
    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreateModal">
        <span>+</span> 发布公告
      </button>
    </div>

    <!-- 公告列表 -->
    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>发布日期</th>
            <th>置顶</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ann in announcements" :key="ann.id">
            <td class="mono">{{ ann.id }}</td>
            <td class="title-cell">
              <span v-if="ann.is_top" class="top-tag">置顶</span>
              {{ ann.title }}
            </td>
            <td class="time">{{ formatDate(ann.publish_date) }}</td>
            <td>
              <span class="badge" :class="ann.is_top ? 'warning' : 'default'">
                {{ ann.is_top ? '是' : '否' }}
              </span>
            </td>
            <td>
              <span class="badge" :class="ann.is_active ? 'success' : 'danger'">
                {{ ann.is_active ? '显示中' : '已隐藏' }}
              </span>
            </td>
            <td class="actions">
              <button class="btn-link" @click="toggleActive(ann)">
                {{ ann.is_active ? '隐藏' : '显示' }}
              </button>
              <button class="btn-link" @click="openEditModal(ann)">编辑</button>
              <button class="btn-link danger" @click="handleDelete(ann)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="announcements.length === 0" class="empty-state">
        <p>暂无公告，点击"发布公告"添加</p>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingAnn ? '编辑公告' : '发布公告' }}</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>公告标题 *</label>
            <input type="text" v-model="formData.title" placeholder="请输入公告标题">
          </div>
          <div class="form-group">
            <label>公告内容</label>
            <textarea v-model="formData.content" rows="5" placeholder="公告详细内容..."></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>发布日期</label>
              <input type="date" v-model="formData.publish_date">
            </div>
            <div class="form-group flex-end">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.is_top">
                <span>置顶显示</span>
              </label>
            </div>
            <div class="form-group flex-end">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.is_active">
                <span>立即显示</span>
              </label>
            </div>
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
import { adminAnnouncementAPI } from '../../config/admin-api'

const announcements = ref<any[]>([])
const showModal = ref(false)
const editingAnn = ref<any>(null)
const saving = ref(false)

const formData = reactive({
  title: '',
  content: '',
  publish_date: new Date().toISOString().slice(0, 10),
  is_top: false,
  is_active: true
})

const resetForm = () => {
  Object.assign(formData, {
    title: '',
    content: '',
    publish_date: new Date().toISOString().slice(0, 10),
    is_top: false,
    is_active: true
  })
}

const loadAnnouncements = async () => {
  announcements.value = await adminAnnouncementAPI.getAnnouncements()
}

const openCreateModal = () => {
  editingAnn.value = null
  resetForm()
  showModal.value = true
}

const openEditModal = (ann: any) => {
  editingAnn.value = ann
  Object.assign(formData, {
    title: ann.title,
    content: ann.content || '',
    publish_date: ann.publish_date ? ann.publish_date.slice(0, 10) : new Date().toISOString().slice(0, 10),
    is_top: !!ann.is_top,
    is_active: ann.is_active !== false
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const handleSave = async () => {
  if (!formData.title.trim()) {
    alert('请输入公告标题')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: formData.title,
      content: formData.content,
      publish_date: formData.publish_date || new Date().toISOString().slice(0, 10),
      is_top: formData.is_top,
      is_active: formData.is_active
    }

    if (editingAnn.value) {
      const result = await adminAnnouncementAPI.updateAnnouncement(editingAnn.value.id, payload)
      if (result.error) {
        alert('更新失败: ' + result.error)
      } else {
        alert('更新成功')
        closeModal()
        await loadAnnouncements()
      }
    } else {
      const result = await adminAnnouncementAPI.createAnnouncement(payload)
      if (result.error) {
        alert('发布失败: ' + result.error)
      } else {
        alert('发布成功')
        closeModal()
        await loadAnnouncements()
      }
    }
  } finally {
    saving.value = false
  }
}

const toggleActive = async (ann: any) => {
  const result = await adminAnnouncementAPI.updateAnnouncement(ann.id, {
    is_active: !ann.is_active
  })
  if (result.error) {
    alert('操作失败: ' + result.error)
  } else {
    ann.is_active = !ann.is_active
  }
}

const handleDelete = async (ann: any) => {
  if (!confirm(`确定删除公告 "${ann.title}" 吗？此操作不可恢复！`)) return
  const result = await adminAnnouncementAPI.deleteAnnouncement(ann.id)
  if (result.error) {
    alert('删除失败: ' + result.error)
  } else {
    alert('删除成功')
    await loadAnnouncements()
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  } catch {
    return dateStr
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.admin-announcements {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  gap: 12px;
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
  white-space: nowrap;
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

.title-cell {
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 300px;
}

.top-tag {
  background: #f39c12;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
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

.badge.danger {
  background-color: #fdecea;
  color: #c0392b;
}

.badge.default {
  background: #eee;
  color: #666;
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
  gap: 16px;
  align-items: flex-end;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
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
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
}

.flex-end {
  flex: 0 0 auto;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: normal !important;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  accent-color: #3498db;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

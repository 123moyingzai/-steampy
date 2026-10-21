<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
          </svg>
          <span>管理后台</span>
        </div>
      </div>

      <nav class="sidebar-menu">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          @click="navigate(item.path)"
        >
          <span class="menu-icon" v-html="item.icon"></span>
          <span class="menu-label">{{ item.label }}</span>
        </div>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <h2 class="page-title">{{ currentTitle }}</h2>
        </div>
        <div class="header-right">
          <div ref="triggerRef" class="admin-trigger" :class="{ active: showMenu }" @click.stop="showMenu = !showMenu">
            <div class="admin-avatar">
              <img v-if="adminAvatar" :src="adminAvatar" alt="头像" />
              <span v-else>{{ adminName.substring(0, 1) }}</span>
            </div>
            <div class="admin-detail">
              <div class="admin-name">{{ adminName }}</div>
              <div class="admin-role">管理员</div>
            </div>
            <svg class="admin-caret" :class="{ open: showMenu }" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 10l5 5 5-5z" />
            </svg>

            <transition name="dropdown">
              <div v-if="showMenu" class="admin-dropdown" @click.stop>
                <div class="dropdown-item" @click="openProfile('basic')">账号设置</div>
                <div class="dropdown-item" @click="openProfile('security')">修改密码</div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-item dropdown-item--danger" @click="handleLogout">退出登录</div>
              </div>
            </transition>
          </div>
        </div>
      </header>

      <!-- 账号设置弹窗 -->
      <div v-if="showProfile" class="profile-mask" @click.self="closeProfile">
        <div class="profile-dialog">
          <div class="profile-dialog__header">
            <h3>账号设置</h3>
            <button class="profile-close" @click="closeProfile">×</button>
          </div>

          <div class="profile-dialog__body">
            <aside class="profile-tabs">
              <div
                class="profile-tab"
                :class="{ active: activeTab === 'basic' }"
                @click="activeTab = 'basic'"
              >基本信息</div>
              <div
                class="profile-tab"
                :class="{ active: activeTab === 'security' }"
                @click="activeTab = 'security'"
              >安全设置</div>
            </aside>

            <section class="profile-panel">
              <h4 class="panel-title">{{ activeTab === 'basic' ? '基本信息' : '修改密码' }}</h4>

              <template v-if="activeTab === 'basic'">
                <div class="form-row">
                  <label>用户头像</label>
                  <div class="avatar-edit">
                    <div class="avatar-preview">
                      <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="头像" />
                      <span v-else>{{ (form.nickname || adminName).substring(0, 1) }}</span>
                    </div>
                    <button class="btn-ghost" :disabled="uploadingAvatar" @click="pickAvatar">
                      {{ uploadingAvatar ? '上传中...' : '更换头像' }}
                    </button>
                    <input ref="fileRef" type="file" accept="image/*" class="hidden-file" @change="handleAvatarFile" />
                  </div>
                </div>

                <div class="form-row">
                  <label>用户名</label>
                  <div class="form-control">
                    <input :value="admin?.username" disabled class="form-input" />
                    <span class="form-hint">用户名不可修改</span>
                  </div>
                </div>

                <div class="form-row">
                  <label>用户 ID</label>
                  <div class="form-static">{{ admin?.id }}</div>
                </div>

                <div class="form-row">
                  <label>昵称</label>
                  <div class="form-control">
                    <input v-model="form.nickname" class="form-input" placeholder="默认昵称" maxlength="30" />
                  </div>
                </div>

                <div class="form-row">
                  <label>手机号</label>
                  <div class="form-control">
                    <input v-model="form.phone" class="form-input" placeholder="请输入手机号" maxlength="20" />
                  </div>
                </div>

                <div class="form-row">
                  <label>邮箱</label>
                  <div class="form-control">
                    <input v-model="form.email" class="form-input" placeholder="请输入邮箱" maxlength="60" />
                  </div>
                </div>

                <div class="form-actions">
                  <button class="btn-primary" :disabled="saving" @click="saveBasic">
                    {{ saving ? '保存中...' : '保存修改' }}
                  </button>
                </div>
              </template>

              <template v-else>
                <div class="form-row">
                  <label>原密码</label>
                  <div class="form-control">
                    <input v-model="pwdForm.old" type="password" class="form-input" placeholder="请输入原密码" />
                  </div>
                </div>

                <div class="form-row">
                  <label>新密码</label>
                  <div class="form-control">
                    <input v-model="pwdForm.new" type="password" class="form-input" placeholder="不少于 4 位" />
                  </div>
                </div>

                <div class="form-row">
                  <label>确认密码</label>
                  <div class="form-control">
                    <input v-model="pwdForm.confirm" type="password" class="form-input" placeholder="请再次输入新密码" />
                  </div>
                </div>

                <div class="form-actions">
                  <button class="btn-primary" :disabled="savingPwd" @click="savePassword">
                    {{ savingPwd ? '保存中...' : '保存修改' }}
                  </button>
                </div>
              </template>
            </section>
          </div>
        </div>
      </div>

      <!-- 页面内容 -->
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { adminAuthAPI } from '../../config/admin-api'
import { authAPI } from '../../config/supabase-local'

const router = useRouter()
const route = useRoute()

const adminName = ref('管理员')
const admin = ref<any>(null)
const adminAvatar = computed(() => form.avatarUrl || '')

// 头像下拉菜单
const showMenu = ref(false)
const triggerRef = ref<HTMLElement | null>(null)

// 账号设置弹窗
const showProfile = ref(false)
const activeTab = ref<'basic' | 'security'>('basic')
const saving = ref(false)
const savingPwd = ref(false)
const uploadingAvatar = ref(false)
const fileRef = ref<HTMLInputElement | null>(null)

const form = reactive({ nickname: '', phone: '', email: '', avatarUrl: '' })
const pwdForm = reactive({ old: '', new: '', confirm: '' })

// 侧边栏菜单项
const menuItems = [
  {
    path: '/admin/dashboard',
    label: '数据概览',
    icon: '<path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>'
  },
  {
    path: '/admin/users',
    label: '用户管理',
    icon: '<path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>'
  },
  {
    path: '/admin/games',
    label: '游戏管理',
    icon: '<path d="M21 6H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-10 7H8v3H6v-3H3v-2h3V10h2v3h3v2zm4.5 2c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm4-3c-.83 0-1.5-.67-1.5-1.5S18.67 9 19.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/>'
  },
  {
    path: '/admin/orders',
    label: '订单管理',
    icon: '<path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>'
  },
  {
    path: '/admin/announcements',
    label: '公告管理',
    icon: '<path d="M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"/>'
  },
  {
    path: '/admin/withdrawals',
    label: '提现审核',
    icon: '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>'
  },
  {
    path: '/admin/refunds',
    label: '退款审核',
    icon: '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>'
  },
  {
    path: '/admin/payment-methods',
    label: '支付渠道',
    icon: '<path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 14H4V6h16v12zm-9-8h9v2h-9v-2zm0 4h9v2h-9v-2zM7 8l-2 2 2 2 1.41-1.41L8.83 10l1.58-1.59L7 8z"/>'
  },
  {
    path: '/admin/reviews',
    label: '评测审核',
    icon: '<path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z"/>'
  }
]

const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(path)
}

const navigate = (path: string) => {
  router.push(path)
}

const currentTitle = computed(() => {
  const matched = menuItems.find(item => isActive(item.path))
  return matched ? matched.label : '管理后台'
})

const closeMenu = (e: MouseEvent) => {
  if (triggerRef.value && !triggerRef.value.contains(e.target as Node)) {
    showMenu.value = false
  }
}

const loadAdmin = () => {
  const current = adminAuthAPI.getCurrentAdmin()
  admin.value = current
  if (current) {
    adminName.value = current.nickname || current.username || '管理员'
    form.nickname = current.nickname || ''
    form.phone = current.phone || ''
    form.email = current.email || ''
    form.avatarUrl = current.avatarUrl || ''
    // 全局注入管理后台鉴权请求头（后端 AdminAuthFilter 校验 /api/admin/**）
    axios.defaults.headers.common['X-Admin-Id'] = current.id
  }
}

const openProfile = (tab: 'basic' | 'security') => {
  showMenu.value = false
  activeTab.value = tab
  showProfile.value = true
}

const closeProfile = () => {
  showProfile.value = false
  pwdForm.old = ''
  pwdForm.new = ''
  pwdForm.confirm = ''
}

const pickAvatar = () => fileRef.value?.click()

const handleAvatarFile = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    alert('图片不能超过 5MB')
    input.value = ''
    return
  }
  uploadingAvatar.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch('/api/upload/avatar', { method: 'POST', body: fd })
    const json = await res.json()
    if (json.code !== 200 || !json.data?.url) {
      alert('上传失败：' + (json.message || '未知错误'))
      return
    }
    form.avatarUrl = json.data.url
  } catch (err: any) {
    alert('上传失败：' + (err?.message || '网络错误'))
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

const saveBasic = async () => {
  if (!admin.value?.id) return
  saving.value = true
  const result = await authAPI.updateUser(admin.value.id, {
    nickname: form.nickname,
    phone: form.phone,
    email: form.email,
    avatarUrl: form.avatarUrl
  } as any)
  saving.value = false

  if (result.error) {
    alert('保存失败：' + result.error)
    return
  }

  const merged = { ...admin.value, ...(result.data || {}) }
  admin.value = merged
  adminName.value = merged.nickname || merged.username || '管理员'
  form.avatarUrl = merged.avatarUrl || form.avatarUrl
  sessionStorage.setItem('steampy_user', JSON.stringify(merged))
  window.dispatchEvent(new Event('user-data-updated'))
  alert('保存成功！')
  showProfile.value = false
}

const savePassword = async () => {
  if (!pwdForm.old || !pwdForm.new || !pwdForm.confirm) {
    alert('请填写完整')
    return
  }
  if (pwdForm.new.length < 4) {
    alert('新密码不少于 4 位')
    return
  }
  if (pwdForm.new !== pwdForm.confirm) {
    alert('两次输入的新密码不一致')
    return
  }
  if (!admin.value?.id) return

  savingPwd.value = true
  const result = await authAPI.changePassword(admin.value.id, pwdForm.old, pwdForm.new)
  savingPwd.value = false

  if (result.error) {
    alert('修改失败：' + result.error)
    return
  }
  alert('密码修改成功，请牢记新密码')
  closeProfile()
}

const handleLogout = () => {
  if (confirm('确定要退出管理员登录吗？')) {
    showMenu.value = false
    adminAuthAPI.logout()
    delete axios.defaults.headers.common['X-Admin-Id']
    router.replace('/')
  }
}

onMounted(() => {
  loadAdmin()
  document.addEventListener('click', closeMenu)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenu)
})

watch(() => route.path, () => {
  const current = adminAuthAPI.getCurrentAdmin()
  if (current) {
    admin.value = current
    adminName.value = current.nickname || current.username || '管理员'
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

/* 侧边栏 - 与客户端 Layout 一致 */
.admin-sidebar {
  width: 220px;
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 1.5rem 0;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  overflow-y: auto;
  z-index: 100;
}

.sidebar-header {
  padding: 0 1.5rem 1.5rem;
  border-bottom: 1px solid #34495e;
  margin-bottom: 1rem;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
}

.sidebar-logo svg {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.sidebar-menu {
  padding: 0 0.5rem;
  flex: 1;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.8rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
  color: #ecf0f1;
  margin-bottom: 0.3rem;
  white-space: nowrap;
  font-size: 0.95rem;
}

.menu-item:hover {
  background-color: #34495e;
}

.menu-item.active {
  background-color: #34495e;
  color: #ecf0f1;
}

.menu-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-icon svg {
  width: 18px;
  height: 18px;
}

.menu-label {
  font-size: 0.95rem;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  margin-left: 220px;
  width: calc(100% - 220px);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 顶部栏 - 与客户端顶栏一致 */
.admin-header {
  height: 60px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #2c3e50;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.admin-trigger {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.35rem 0.6rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  user-select: none;
}

.admin-trigger:hover,
.admin-trigger.active {
  background-color: #eef2f6;
}

.admin-avatar {
  width: 36px;
  height: 36px;
  background-color: #3498db;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  overflow: hidden;
  flex-shrink: 0;
}

.admin-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #2c3e50;
}

.admin-role {
  font-size: 0.75rem;
  color: #7f8c8d;
}

.admin-caret {
  width: 18px;
  height: 18px;
  color: #7f8c8d;
  transition: transform 0.2s;
}

.admin-caret.open {
  transform: rotate(180deg);
}

/* 头像下拉菜单 */
.admin-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 132px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
  padding: 6px 0;
  z-index: 50;
}

.dropdown-item {
  padding: 0.55rem 1rem;
  font-size: 0.86rem;
  color: #303133;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.15s, color 0.15s;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
  color: #3498db;
}

.dropdown-item--danger:hover {
  background-color: #fef0f0;
  color: #e74c3c;
}

.dropdown-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 5px 0;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* 账号设置弹窗 */
.profile-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.42);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.profile-dialog {
  width: 720px;
  max-width: calc(100vw - 2rem);
  max-height: calc(100vh - 4rem);
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.profile-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #ebeef5;
}

.profile-dialog__header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
}

.profile-close {
  border: none;
  background: transparent;
  font-size: 1.4rem;
  line-height: 1;
  color: #909399;
  cursor: pointer;
  padding: 0 0.2rem;
}

.profile-close:hover {
  color: #303133;
}

.profile-dialog__body {
  display: flex;
  min-height: 360px;
  overflow: auto;
}

.profile-tabs {
  width: 130px;
  flex-shrink: 0;
  background: #f7f8fa;
  border-right: 1px solid #ebeef5;
  padding: 0.75rem 0;
}

.profile-tab {
  padding: 0.65rem 1.2rem;
  font-size: 0.86rem;
  color: #606266;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.15s;
}

.profile-tab:hover {
  color: #3498db;
}

.profile-tab.active {
  background: #e8f2fc;
  color: #3498db;
  border-left-color: #3498db;
  font-weight: 500;
}

.profile-panel {
  flex: 1;
  padding: 1.25rem 1.75rem 1.5rem;
  min-width: 0;
}

.panel-title {
  margin: 0 0 1.25rem;
  font-size: 0.98rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 1.05rem;
}

.form-row > label {
  width: 88px;
  flex-shrink: 0;
  text-align: right;
  padding-right: 1rem;
  line-height: 34px;
  font-size: 0.85rem;
  color: #606266;
}

.form-control {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  min-width: 0;
}

.form-input {
  width: 300px;
  max-width: 100%;
  height: 34px;
  padding: 0 0.65rem;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #3498db;
}

.form-input:disabled {
  background: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}

.form-static {
  line-height: 34px;
  font-size: 0.85rem;
  color: #303133;
  word-break: break-all;
}

.form-hint {
  font-size: 0.75rem;
  color: #a8abb2;
}

.avatar-edit {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.avatar-preview {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #3498db;
  font-size: 1.5rem;
  font-weight: 600;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hidden-file {
  display: none;
}

.btn-ghost {
  padding: 0.4rem 0.9rem;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 0.82rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-ghost:hover:not(:disabled) {
  border-color: #3498db;
  color: #3498db;
}

.btn-primary {
  padding: 0.45rem 1.3rem;
  background: #3498db;
  border: none;
  border-radius: 4px;
  color: #fff;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled,
.btn-ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  padding-left: 88px;
  margin-top: 1.5rem;
}

/* 内容区 */
.admin-content {
  flex: 1;
  padding: 1.5rem 2rem;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .admin-sidebar {
    width: 60px;
    padding: 1rem 0;
  }

  .sidebar-logo span,
  .menu-label,
  .sidebar-footer {
    display: none;
  }

  .sidebar-header {
    padding: 0 0.5rem 1rem;
    display: flex;
    justify-content: center;
  }

  .menu-item {
    justify-content: center;
    padding: 0.8rem;
  }

  .admin-main {
    margin-left: 60px;
    width: calc(100% - 60px);
  }
}

@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: -220px;
    top: 0;
    bottom: 0;
    z-index: 1000;
    width: 220px;
    padding: 1.5rem 0;
  }

  .admin-sidebar.mobile-open {
    left: 0;
  }

  .admin-content {
    padding: 1rem;
  }

  .admin-header {
    padding: 0 1rem;
  }
}
</style>

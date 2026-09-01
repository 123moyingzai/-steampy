<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
          </svg>
          <span v-show="!sidebarCollapsed">管理后台</span>
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
          <span v-show="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <svg viewBox="0 0 24 24" fill="currentColor" :style="{ transform: sidebarCollapsed ? 'rotate(180deg)' : 'none' }">
            <path d="M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z"/>
          </svg>
          <span v-show="!sidebarCollapsed">收起</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <h2 class="page-title">{{ currentTitle }}</h2>
        </div>
        <div class="header-right">
          <div class="admin-info">
            <div class="admin-avatar">{{ adminName.substring(0, 1) }}</div>
            <div class="admin-detail">
              <div class="admin-name">{{ adminName }}</div>
              <div class="admin-role">管理员</div>
            </div>
          </div>
          <button class="logout-btn" @click="handleLogout">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
            </svg>
            退出
          </button>
        </div>
      </header>

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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { adminAuthAPI } from '../../config/admin-api'

const router = useRouter()
const route = useRoute()

const sidebarCollapsed = ref(false)
const adminName = ref('管理员')

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

const handleLogout = () => {
  if (confirm('确定要退出管理员登录吗？')) {
    adminAuthAPI.logout()
    router.replace('/admin/login')
  }
}

onMounted(() => {
  const admin = adminAuthAPI.getCurrentAdmin()
  if (admin) {
    adminName.value = admin.nickname || admin.username || '管理员'
  }
})

watch(() => route.path, () => {
  const admin = adminAuthAPI.getCurrentAdmin()
  if (admin) {
    adminName.value = admin.nickname || admin.username || '管理员'
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

/* 侧边栏 */
.admin-sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #ecf0f1;
  transition: width 0.25s ease;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.admin-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar-logo svg {
  width: 24px;
  height: 24px;
  color: #e74c3c;
  flex-shrink: 0;
}

.sidebar-menu {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
  white-space: nowrap;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.menu-item.active {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff;
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.menu-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-icon svg {
  width: 20px;
  height: 20px;
}

.menu-label {
  font-size: 14px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.collapse-btn {
  width: 100%;
  padding: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: none;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
  font-size: 13px;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.collapse-btn svg {
  width: 16px;
  height: 16px;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 顶部栏 */
.admin-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-avatar {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.admin-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.admin-role {
  font-size: 12px;
  color: #9ca3af;
}

.logout-btn {
  padding: 8px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  color: #dc2626;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fecaca;
}

.logout-btn svg {
  width: 16px;
  height: 16px;
}

/* 内容区 */
.admin-content {
  flex: 1;
  padding: 24px;
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
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: -220px;
    top: 0;
    bottom: 0;
    z-index: 1000;
  }

  .admin-sidebar.mobile-open {
    left: 0;
  }

  .admin-content {
    padding: 16px;
  }

  .admin-header {
    padding: 0 16px;
  }
}
</style>

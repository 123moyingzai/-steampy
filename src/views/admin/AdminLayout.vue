﻿﻿﻿﻿﻿<template>
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

.admin-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
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

.logout-btn {
  padding: 0.4rem 1rem;
  background-color: #95a5a6;
  border: none;
  border-radius: 4px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.logout-btn:hover {
  background-color: #7f8c8d;
}

.logout-btn svg {
  width: 16px;
  height: 16px;
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

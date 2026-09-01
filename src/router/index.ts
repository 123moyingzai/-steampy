import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Home from '../views/Home.vue'
import { adminAuthAPI } from '../config/admin-api'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/test-cors',
    name: 'TestCORS',
    component: () => import('../views/TestCORS.vue')
  },
  {
    path: '/buyer',
    name: 'BuyerCenter',
    component: () => import('../views/BuyerCenter.vue')
  },
  {
    path: '/seller',
    name: 'SellerCenter',
    component: () => import('../views/SellerCenter.vue')
  },
  {
    path: '/settings',
    name: 'AccountSettings',
    component: () => import('../views/AccountSettings.vue')
  },
  {
    path: '/cdkey',
    name: 'CDKeyMarket',
    component: () => import('../views/CDKeyMarket.vue')
  },
  {
    path: '/game/:id',
    name: 'GameDetail',
    component: () => import('../views/GameDetail.vue')
  },
  {
    path: '/gift',
    name: 'GiftPurchase',
    component: () => import('../views/GiftPurchase.vue')
  },
  {
    path: '/balance',
    name: 'BalancePurchase',
    component: () => import('../views/BalancePurchase.vue')
  },
  {
    path: '/transactions',
    name: 'TransactionHistory',
    component: () => import('../views/TransactionHistory.vue')
  },
  {
    path: '/balance/detail/:game',
    name: 'BalancePurchaseDetail',
    component: () => import('../views/BalancePurchaseDetail.vue')
  },
  {
    path: '/help',
    name: 'HelpCenter',
    component: () => import('../views/HelpCenter.vue')
  },

  // ========== 管理员路由 ==========
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue')
      },
      {
        path: 'games',
        name: 'AdminGames',
        component: () => import('../views/admin/AdminGames.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/AdminOrders.vue')
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('../views/admin/AdminAnnouncements.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 管理员权限检查
router.beforeEach((to, from, next) => {
  // 访问 /admin/* 路由需要管理员权限
  if (to.path.startsWith('/admin')) {
    const admin = adminAuthAPI.getCurrentAdmin()
    const isAdmin = admin && admin.user_type === '管理员'
    if (!adminAuthAPI.isAdminLoggedIn() || !isAdmin) {
      // 未登录或非管理员，跳转登录页，登录后自动回跳（普通用户登录后会被 Login.vue 静默拦到首页）
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }

  next()
})

export default router

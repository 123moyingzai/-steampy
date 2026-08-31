import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Home from '../views/Home.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

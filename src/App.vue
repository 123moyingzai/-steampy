<template>
  <div id="cjx-app">
    <!-- 封禁状态全局横幅 -->
    <div v-if="isBanned" class="cjx-banned-banner">
      ⛔ 您的账号已被封禁，购买、出售、充值、评价等功能暂时无法使用，如有疑问请联系管理员
    </div>
    <router-view v-slot="{ Component, route }">
      <!-- 列表页缓存（CDK国区 / 礼物代购 / 余额购 / 首页）：进详情页再返回保留滚动位置 -->
      <keep-alive :include="keepAliveNames">
        <component :is="Component" />
      </keep-alive>
    </router-view>
    <div class="cjx-footer-text"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

// 列表页组件名（Vue3 script setup 默认取文件名）
const keepAliveNames = ['CDKeyMarket', 'GiftPurchase', 'BalancePurchase', 'Home']

// ===== 封禁状态横幅 =====
const isBanned = ref(false)

const refreshBanState = () => {
  try {
    const raw = sessionStorage.getItem('steampy_user')
    if (!raw) {
      isBanned.value = false
      return
    }
    const u = JSON.parse(raw)
    isBanned.value = u?.userType === '已封禁' || u?.user_type === '已封禁'
  } catch {
    isBanned.value = false
  }
}

onMounted(() => {
  refreshBanState()
  // 登录成功 / 解封后资料刷新时同步横幅
  window.addEventListener('user-logged-in', refreshBanState)
  window.addEventListener('user-updated', refreshBanState)
})

onUnmounted(() => {
  window.removeEventListener('user-logged-in', refreshBanState)
  window.removeEventListener('user-updated', refreshBanState)
})
</script>

<style scoped>
#cjx-app {
  min-height: 100vh;
  position: relative;
}

.cjx-banned-banner {
  background: linear-gradient(90deg, #e74c3c, #c0392b);
  color: #fff;
  text-align: center;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  position: sticky;
  top: 0;
  z-index: 2000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.cjx-footer-text {
  text-align: center;
  padding: 10px;
  color: #999;
  font-size: 12px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.9);
  z-index: 1000;
}
</style>

<template>
  <div id="cjx-app">
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
// 列表页组件名（Vue3 script setup 默认取文件名）
const keepAliveNames = ['CDKeyMarket', 'GiftPurchase', 'BalancePurchase', 'Home']
</script>

<style scoped>
#cjx-app {
  min-height: 100vh;
  position: relative;
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

<template>
  <div id="cjx-app">
    <router-view />
    <div class="cjx-footer-text"></div>
  </div>
</template>

<script setup lang="ts">
// 应用根组件 - 使用组合式API
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

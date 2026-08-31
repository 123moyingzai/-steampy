<template>
  <div class="loading-spinner" :class="size">
    <div class="spinner"></div>
    <span v-if="text" class="loading-text">{{ text }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps({
  size: {
    type: String,
    default: 'normal' // small, normal, large
  },
  text: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.spinner {
  border-radius: 50%;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #e74c3c;
  animation: spin 1s linear infinite;
}

/* 尺寸 */
.loading-spinner.small .spinner {
  width: 20px;
  height: 20px;
}

.loading-spinner.normal .spinner {
  width: 36px;
  height: 36px;
}

.loading-spinner.large .spinner {
  width: 50px;
  height: 50px;
}

.loading-text {
  font-size: 14px;
  color: #666;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
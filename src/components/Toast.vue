<template>
  <div class="toast" :class="[type, position]">
    <div class="toast-content">
      <svg v-if="type === 'success'" class="toast-icon" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
      </svg>
      <svg v-else-if="type === 'error'" class="toast-icon" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
      </svg>
      <svg v-else-if="type === 'warning'" class="toast-icon" viewBox="0 0 24 24" fill="currentColor">
        <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
      </svg>
      <svg v-else class="toast-icon" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
      </svg>
      <span class="toast-message">{{ message }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'info' // info, success, error, warning
  },
  position: {
    type: String,
    default: 'center' // top, center, bottom
  }
})
</script>

<style scoped>
.toast {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.toast.top {
  top: 20px;
}

.toast.center {
  top: 50%;
  transform: translate(-50%, -50%);
}

.toast.bottom {
  bottom: 20px;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.8);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  max-width: 80%;
}

.toast-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.toast.success .toast-icon {
  color: #4caf50;
}

.toast.error .toast-icon {
  color: #f44336;
}

.toast.warning .toast-icon {
  color: #ff9800;
}

.toast.info .toast-icon {
  color: #2196f3;
}

.toast-message {
  word-break: break-word;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>
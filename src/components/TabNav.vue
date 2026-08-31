<template>
  <div class="tab-nav">
    <div class="tab-list" :class="{ scrollable: scrollable }">
      <div
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: modelValue === tab.value, disabled: tab.disabled }"
        @click="handleClick(tab)"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="tab.badge" class="tab-badge">{{ tab.badge }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  modelValue: {
    type: [String, Number],
    required: true
  },
  tabs: {
    type: Array,
    required: true
  },
  scrollable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const handleClick = (tab) => {
  if (tab.disabled) return
  emit('update:modelValue', tab.value)
  emit('change', tab)
}
</script>

<style scoped>
.tab-nav {
  background: #fff;
  border-bottom: 1px solid #eee;
}

.tab-list {
  display: flex;
  gap: 0;
}

.tab-list.scrollable {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.tab-item {
  position: relative;
  padding: 12px 20px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  white-space: nowrap;
  transition: color 0.3s;
}

.tab-item:hover:not(.disabled) {
  color: #e74c3c;
}

.tab-item.active {
  color: #e74c3c;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #e74c3c;
}

.tab-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.tab-badge {
  margin-left: 6px;
  padding: 2px 6px;
  background: #e74c3c;
  color: #fff;
  border-radius: 10px;
  font-size: 12px;
}
</style>
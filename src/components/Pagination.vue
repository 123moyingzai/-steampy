<template>
  <div class="pagination">
    <button
      class="page-btn prev"
      :disabled="currentPage <= 1"
      @click="handlePrev"
    >
      上一页
    </button>
    <div class="page-numbers">
      <button
        v-for="page in visiblePages"
        :key="page"
        class="page-num"
        :class="{ active: page === currentPage }"
        @click="handlePageClick(page)"
      >
        {{ page }}
      </button>
    </div>
    <button
      class="page-btn next"
      :disabled="currentPage >= totalPages"
      @click="handleNext"
    >
      下一页
    </button>
    <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  visibleSize: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits(['update:currentPage', 'change'])

const visiblePages = computed(() => {
  const pages = []
  const half = Math.floor(props.visibleSize / 2)
  let start = Math.max(1, props.currentPage - half)
  let end = Math.min(props.totalPages, start + props.visibleSize - 1)

  if (end - start < props.visibleSize - 1) {
    start = Math.max(1, end - props.visibleSize + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const handlePrev = () => {
  if (props.currentPage > 1) {
    const newPage = props.currentPage - 1
    emit('update:currentPage', newPage)
    emit('change', newPage)
  }
}

const handleNext = () => {
  if (props.currentPage < props.totalPages) {
    const newPage = props.currentPage + 1
    emit('update:currentPage', newPage)
    emit('change', newPage)
  }
}

const handlePageClick = (page) => {
  emit('update:currentPage', page)
  emit('change', page)
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px 0;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
  border-color: #e74c3c;
  color: #e74c3c;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 6px;
}

.page-num {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-num:hover {
  border-color: #e74c3c;
  color: #e74c3c;
}

.page-num.active {
  background: #e74c3c;
  border-color: #e74c3c;
  color: #fff;
}

.page-info {
  font-size: 14px;
  color: #666;
  margin-left: 10px;
}
</style>
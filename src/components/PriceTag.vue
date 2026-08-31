<template>
  <div class="price-tag" :class="[type, size]">
    <span class="label" v-if="showLabel">{{ label }}</span>
    <span class="price">{{ formattedPrice }}</span>
    <span class="original" v-if="originalPrice">{{ originalPrice }}</span>
    <span class="unit" v-if="unit">{{ unit }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  price: {
    type: [Number, String],
    required: true
  },
  originalPrice: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'primary' // primary, success, warning
  },
  size: {
    type: String,
    default: 'normal' // small, normal, large
  },
  label: {
    type: String,
    default: '价格'
  },
  showLabel: {
    type: Boolean,
    default: false
  },
  unit: {
    type: String,
    default: '¥'
  }
})

const formattedPrice = computed(() => {
  if (typeof props.price === 'string') {
    return props.price.startsWith(props.unit) ? props.price : `${props.unit}${props.price}`
  }
  return `${props.unit}${props.price.toFixed(2)}`
})
</script>

<style scoped>
.price-tag {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
}

.label {
  font-size: 12px;
  color: #999;
}

.price {
  font-weight: bold;
}

.original {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.unit {
  font-size: 12px;
}

/* 类型样式 */
.price-tag.primary .price {
  color: #e74c3c;
}

.price-tag.success .price {
  color: #27ae60;
}

.price-tag.warning .price {
  color: #f39c12;
}

/* 尺寸样式 */
.price-tag.small .price {
  font-size: 14px;
}

.price-tag.normal .price {
  font-size: 18px;
}

.price-tag.large .price {
  font-size: 32px;
}
</style>
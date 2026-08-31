<template>
  <div class="game-card" @click="handleClick">
    <div class="card-image">
      <img :src="imageUrl" :alt="title" />
      <span v-if="discount" class="discount-tag">{{ discount }}</span>
      <span v-if="presale" class="presale-tag">预售</span>
    </div>
    <div class="card-content">
      <h3 class="card-title">{{ title }}</h3>
      <div class="card-meta">
        <slot name="tags"></slot>
      </div>
      <div class="card-price">
        <span class="current-price">{{ price }}</span>
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  image: {
    type: String,
    default: ''
  },
  price: {
    type: String,
    default: '¥0'
  },
  discount: {
    type: String,
    default: ''
  },
  presale: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const imageUrl = computed(() => {
  if (!props.image) return '/picture/安魂曲.jpg'
  if (props.image.startsWith('http')) return props.image
  if (props.image.includes('picture/')) {
    const fileName = props.image.split('picture/')[1]
    return `/picture/${fileName}`
  }
  return props.image.startsWith('/') ? props.image : `/${props.image}`
})

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.game-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.game-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.card-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.discount-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #e74c3c;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.presale-tag {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: #f39c12;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.card-content {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  margin-bottom: 8px;
  min-height: 20px;
}

.card-price {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}
</style>
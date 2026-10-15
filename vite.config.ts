﻿import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        // 手动分包 — 让浏览器对变化不频繁的 vendor 做长期缓存
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('vue') || id.includes('@vue')) {
              return 'vue-fam'
            }
            if (id.includes('vant')) {
              return 'vant-ui'
            }
            if (id.includes('supabase')) {
              return 'supabase'
            }
            if (id.includes('axios')) {
              return 'axios'
            }
            return 'vendor'
          }
        }
      }
    },
    // 小于 4KB 的资源内联进 JS，避免额外请求
    assetsInlineLimit: 4096,
    // CSS 独立分包（默认开启，显式写明）
    cssCodeSplit: true,
    // 生产构建用 esbuild 移除 console
    minify: 'esbuild',
    esbuild: {
      drop: ['console', 'debugger']
    },
    chunkSizeWarningLimit: 500
  }
})

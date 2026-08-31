import { defineConfig } from 'vite'
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
      '/supabase': {
        target: 'https://prvmjufbhsofvnjeswhq.supabase.co',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/supabase/, '')
      }
    }
  }
})

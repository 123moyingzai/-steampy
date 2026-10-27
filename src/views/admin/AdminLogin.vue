<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="admin-logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        <div class="admin-brand">
          <h1>SteamPY 管理后台</h1>
          <p>仅限管理员登录</p>
        </div>
      </div>

      <form class="admin-form" @submit.prevent="handleLogin">
        <div class="admin-form-group">
          <label>管理员账号</label>
          <input
            type="text"
            class="admin-form-control"
            placeholder="请输入管理员用户名"
            v-model="form.username"
            autocomplete="username"
          >
        </div>

        <div class="admin-form-group">
          <label>密码</label>
          <input
            :type="showPwd ? 'text' : 'password'"
            class="admin-form-control"
            placeholder="请输入密码"
            v-model="form.password"
            autocomplete="current-password"
          >
          <span class="admin-toggle" @click="showPwd = !showPwd">
            <svg v-if="showPwd" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
              <line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
          </span>
        </div>

        <div v-if="errorMsg" class="admin-error">{{ errorMsg }}</div>

        <button type="button" class="admin-btn" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '管理员登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authAPI } from '../../config/supabase-local'
import { adminAuthAPI } from '../../config/admin-api'

const router = useRouter()
const route = useRoute()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const showPwd = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  errorMsg.value = ''
  if (!form.username) { errorMsg.value = '请输入管理员账号'; return }
  if (!form.password) { errorMsg.value = '请输入密码'; return }

  loading.value = true
  try {
    const result = await authAPI.login(form.username, form.password)
    loading.value = false

    if (result.error) {
      errorMsg.value = result.error
      return
    }

    const user = result.data as any
    const isAdmin = user?.userType === '管理员'

    // 清掉 authAPI.login 偷偷存的 steampy_user —— 管理员不算普通用户
    sessionStorage.removeItem('steampy_user')
    sessionStorage.removeItem('steampy_admin')

    if (!isAdmin) {
      errorMsg.value = '该账号无管理员权限，请使用前台登录'
      return
    }

    adminAuthAPI.setAdminUser(user)
    adminAuthAPI.setAdmin()
    window.dispatchEvent(new Event('user-logged-in'))

    const redirect = route.query.redirect as string
    if (redirect && redirect.startsWith('/admin')) {
      router.replace(redirect)
    } else {
      router.replace('/admin/dashboard')
    }
  } catch (e: any) {
    loading.value = false
    errorMsg.value = '登录异常：' + (e?.message || '网络错误')
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  background-color: #333333;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.admin-login-card {
  width: 100%;
  max-width: 400px;
  background-color: #444444;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
}

.admin-logo svg {
  width: 30px;
  height: 30px;
  fill: #2ecc71;
  flex-shrink: 0;
}

.admin-brand h1 {
  color: #ffffff;
  font-size: 18px;
  margin: 0;
  font-weight: bold;
}

.admin-brand p {
  color: #bbbbbb;
  font-size: 12px;
  margin: 3px 0 0;
}

.admin-form-group {
  margin-bottom: 18px;
  position: relative;
}

.admin-form-group label {
  display: block;
  color: #bbbbbb;
  font-size: 13px;
  margin-bottom: 8px;
}

.admin-form-control {
  width: 100%;
  padding: 12px 15px;
  background-color: #555555;
  border: 1px solid #666;
  border-radius: 4px;
  color: #ffffff;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.admin-form-control::placeholder {
  color: #999;
}

.admin-form-control:focus {
  outline: none;
  border-color: #2ecc71;
}

.admin-toggle {
  position: absolute;
  right: 12px;
  bottom: 11px;
  color: #999;
  cursor: pointer;
  display: flex;
}

.admin-toggle svg {
  width: 18px;
  height: 18px;
}

.admin-toggle:hover svg {
  color: #2ecc71;
}

.admin-error {
  background-color: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.3);
  color: #e74c3c;
  padding: 10px 14px;
  border-radius: 4px;
  font-size: 13px;
  margin-bottom: 16px;
}

.admin-btn {
  width: 100%;
  padding: 12px;
  background-color: #27ae60;
  border: none;
  border-radius: 4px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
  margin-top: 8px;
  box-sizing: border-box;
}

.admin-btn:hover { opacity: 0.9; }.admin-btn:active { transform: translateY(1px); }
.admin-btn:disabled { opacity: 0.6; cursor: not-allowed; }

</style>

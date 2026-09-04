﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="cjx-login-page">
    <div class="cjx-logo">
      <svg viewBox="0 0 24 24">
        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
      </svg>
      <span>SteamPY匹歪</span>
    </div>

    <div class="cjx-auth-container">
      <!-- 登录表单 -->
      <div v-if="isLogin" id="cjx-login-panels" class="cjx-form-panel cjx-active">
        <div class="cjx-tabs">
          <div 
            class="cjx-tab" 
            :class="{ active: loginTab === 'password' }"
            @click="loginTab = 'password'"
          >账户密码登录</div>
          <div 
            class="cjx-tab" 
            :class="{ active: loginTab === 'phone' }"
            @click="loginTab = 'phone'"
          >手机号登录</div>
        </div>

        <!-- 账户密码登录面板 -->
        <div v-show="loginTab === 'password'" class="cjx-login-tab-panel">
          <div class="cjx-form-group">
            <input 
              type="text" 
              class="cjx-form-control" 
              placeholder="请输入用户名"
              v-model="loginForm.username"
            >
            <div class="cjx-error-message">{{ errors.loginUsername }}</div>
          </div>

          <div class="cjx-form-group cjx-password-toggle">
            <input 
              :type="showPassword ? 'text' : 'password'"
              class="cjx-form-control" 
              placeholder="请输入密码"
              v-model="loginForm.password"
            >
            <span class="cjx-toggle-icon" @click="showPassword = !showPassword">
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </span>
            <div class="cjx-error-message">{{ errors.loginPassword }}</div>
          </div>

          <div class="cjx-form-group cjx-captcha-group">
            <input 
              type="text" 
              class="cjx-form-control" 
              placeholder="请输入图片验证码"
              v-model="loginForm.captcha"
            >
            <div class="cjx-captcha-image" @click="generateCaptcha">{{ captchaCode }}</div>
          </div>
          <div class="cjx-error-message">{{ errors.loginCaptcha }}</div>

          <div class="cjx-options">
            <div class="cjx-checkbox-group">
              <input type="checkbox" class="cjx-form-checkbox" v-model="loginForm.autoLogin">
              <label>自动登录</label>
            </div>
            <a href="#" class="cjx-forgot-password">忘记密码</a>
          </div>

          <div class="cjx-checkbox-group" style="margin-bottom: 20px;">
            <input type="checkbox" class="cjx-form-checkbox" v-model="loginForm.agree">
            <label>我已阅读并同意用户协议,隐私政策</label>
          </div>
          <div class="cjx-error-message">{{ errors.loginAgree }}</div>

          <button class="cjx-btn cjx-btn-primary" @click="handleLogin" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <div class="cjx-link-text">
            <a @click="switchToRegister">注册账户</a>
          </div>
        </div>

        <!-- 手机号登录面板 -->
        <div v-show="loginTab === 'phone'" class="cjx-login-tab-panel">
          <div class="cjx-form-group cjx-phone-input-group">
            <select class="cjx-country-code">
              <option>+86(中国)</option>
            </select>
            <input 
              type="text" 
              class="cjx-form-control" 
              placeholder="请输入手机号"
              v-model="phoneForm.phone"
            >
          </div>
          <div class="cjx-error-message">{{ errors.phone }}</div>

          <div class="cjx-form-group cjx-verify-code-group">
            <input 
              type="text" 
              class="cjx-form-control" 
              placeholder="请输入短信验证码"
              v-model="phoneForm.code"
            >
            <button 
              class="cjx-get-code-btn" 
              @click="sendPhoneCode"
              :disabled="phoneCodeCountdown > 0"
            >
              {{ phoneCodeCountdown > 0 ? `重新发送(${phoneCodeCountdown})` : '获取验证码' }}
            </button>
          </div>
          <div class="cjx-error-message">{{ errors.phoneCode }}</div>

          <div class="cjx-options">
            <div class="cjx-checkbox-group">
              <input type="checkbox" class="cjx-form-checkbox" v-model="phoneForm.autoLogin">
              <label>自动登录</label>
            </div>
            <a href="#" class="cjx-forgot-password">忘记密码</a>
          </div>

          <div class="cjx-checkbox-group" style="margin-bottom: 20px;">
            <input type="checkbox" class="cjx-form-checkbox" v-model="phoneForm.agree">
            <label>我已阅读并同意用户协议,隐私政策</label>
          </div>
          <div class="cjx-error-message">{{ errors.phoneAgree }}</div>

          <button class="cjx-btn cjx-btn-primary" @click="handlePhoneLogin">
            登录
          </button>

          <div class="cjx-link-text">
            <a @click="switchToRegister">注册账户</a>
          </div>
        </div>
      </div>

      <!-- 注册表单 -->
      <div v-else id="cjx-register-panel" class="cjx-form-panel cjx-active">
        <h2>注册</h2>

        <div class="cjx-form-group">
          <input 
            type="text" 
            class="cjx-form-control" 
            placeholder="请输入用户名(字母或数字)"
            v-model="registerForm.username"
          >
          <div class="cjx-error-message">{{ errors.regUsername }}</div>
        </div>

        <div class="cjx-form-group cjx-password-toggle">
          <input 
            :type="showRegPassword ? 'text' : 'password'"
            class="cjx-form-control" 
            placeholder="请输入密码，长度为6-20个字符"
            v-model="registerForm.password"
          >
          <span class="cjx-toggle-icon" @click="showRegPassword = !showRegPassword">
            <svg v-if="showRegPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
              <line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
          </span>
          <div class="cjx-error-message">{{ errors.regPassword }}</div>
        </div>

        <div class="cjx-form-group">
          <input 
            type="password" 
            class="cjx-form-control" 
            placeholder="请再次输入确认密码"
            v-model="registerForm.confirmPassword"
          >
          <div class="cjx-error-message">{{ errors.regConfirm }}</div>
        </div>

        <div class="cjx-form-group cjx-phone-input-group">
          <select class="cjx-country-code">
            <option>+86(中国)</option>
          </select>
          <input 
            type="text" 
            class="cjx-form-control" 
            placeholder="请输入手机号"
            v-model="registerForm.phone"
          >
        </div>
        <div class="cjx-error-message">{{ errors.regPhone }}</div>

        <div class="cjx-form-group cjx-verify-code-group">
          <input 
            type="text" 
            class="cjx-form-control" 
            placeholder="请输入短信验证码"
            v-model="registerForm.code"
          >
          <button 
            class="cjx-get-code-btn"
            @click="sendRegCode"
            :disabled="regCodeCountdown > 0"
          >
            {{ regCodeCountdown > 0 ? `重新发送(${regCodeCountdown})` : '获取验证码' }}
          </button>
        </div>
        <div class="cjx-error-message">{{ errors.regCode }}</div>

        <div class="cjx-checkbox-group" style="margin-bottom: 20px;">
          <input type="checkbox" class="cjx-form-checkbox" v-model="registerForm.agree">
          <label>我已阅读并同意用户协议,隐私政策</label>
        </div>
        <div class="cjx-error-message">{{ errors.regAgree }}</div>

        <button class="cjx-btn cjx-btn-primary" @click="handleRegister" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <div class="cjx-link-text">
            <a @click="switchToLogin">使用已有账号登录</a>
          </div>
        </div>
      </div>

    <div class="cjx-footer">
      - Steam游戏交易平台 | 沪ICP备19042195号-1
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authAPI } from '../config/supabase-local.ts'

const router = useRouter()
const route = useRoute()

// 响应式数据
const isLogin = ref(true)
const loginTab = ref('password')
const loading = ref(false)
const showPassword = ref(false)
const showRegPassword = ref(false)
const captchaCode = ref('')
const phoneCodeCountdown = ref(0)
const regCodeCountdown = ref(0)

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  autoLogin: true,
  agree: false
})

const phoneForm = reactive({
  phone: '',
  code: '',
  autoLogin: true,
  agree: false
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  code: '',
  agree: false
})

const errors = reactive({
  loginUsername: '',
  loginPassword: '',
  loginCaptcha: '',
  loginAgree: '',
  phone: '',
  phoneCode: '',
  phoneAgree: '',
  regUsername: '',
  regPassword: '',
  regConfirm: '',
  regPhone: '',
  regCode: '',
  regAgree: ''
})

// 生成验证码
const generateCaptcha = () => {
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  let captcha = ''
  for (let i = 0; i < 3; i++) {
    captcha += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captcha += '-'
  captcha += Math.floor(Math.random() * 10)
  captchaCode.value = captcha
}

// 切换登录/注册
const switchToRegister = () => {
  isLogin.value = false
  clearErrors()
}

const switchToLogin = () => {
  isLogin.value = true
  clearErrors()
}

const clearErrors = () => {
  Object.keys(errors).forEach(key => errors[key] = '')
}

// 倒计时
const startCountdown = (type) => {
  let countdown = 60
  if (type === 'phone') {
    phoneCodeCountdown.value = countdown
    const timer = setInterval(() => {
      countdown--
      phoneCodeCountdown.value = countdown
      if (countdown <= 0) clearInterval(timer)
    }, 1000)
  } else {
    regCodeCountdown.value = countdown
    const timer = setInterval(() => {
      countdown--
      regCodeCountdown.value = countdown
      if (countdown <= 0) clearInterval(timer)
    }, 1000)
  }
}

// 发送验证码
const sendPhoneCode = () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    errors.phone = '请输入正确的手机号'
    return
  }
  errors.phone = ''
  startCountdown('phone')
}

const sendRegCode = () => {
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    errors.regPhone = '请输入正确的手机号'
    return
  }
  errors.regPhone = ''
  startCountdown('reg')
}

// 登录处理
const handleLogin = async () => {
  clearErrors()
  let isValid = true

  if (!loginForm.username) {
    errors.loginUsername = '请输入用户名'
    isValid = false
  }
  if (!loginForm.password) {
    errors.loginPassword = '请输入密码'
    isValid = false
  }
  if (loginForm.captcha.toUpperCase() !== captchaCode.value.toUpperCase()) {
    errors.loginCaptcha = '验证码输入错误'
    isValid = false
  }
  if (!loginForm.agree) {
    errors.loginAgree = '请同意用户协议和隐私政策'
    isValid = false
  }

  if (!isValid) return

  loading.value = true
  try {
    const result = await authAPI.login(loginForm.username, loginForm.password)
    loading.value = false
    console.log('登录结果:', result)

    if (result.error) {
      errors.loginPassword = result.error
      alert(result.error) // 使用alert确保错误信息显示
    } else {
      const user = result.data as any
      const isAdmin = user?.user_type === '管理员'
      // 如果是管理员，额外标记
      if (isAdmin) {
        sessionStorage.setItem('steampy_admin', 'true')
      }
      alert('登录成功！')
      window.dispatchEvent(new Event('user-logged-in'))

      // 优先使用 redirect 参数（从路由守卫跳转过来的）
      const redirect = route.query.redirect as string
      if (redirect && redirect.startsWith('/admin')) {
        if (isAdmin) {
          router.replace(redirect)
        } else {
          router.replace('/')
        }
      } else if (redirect && redirect.startsWith('/')) {
        router.replace(redirect)
      } else {
        // 默认跳转：管理员去后台，普通用户去首页
        router.replace(isAdmin ? '/admin/dashboard' : '/')
      }
    }
  } catch (error) {
    loading.value = false
    console.error('登录异常:', error)
    alert('登录失败，请重试')
  }
}

// 手机号登录
const handlePhoneLogin = () => {
  clearErrors()
  let isValid = true

  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    errors.phone = '请输入正确的手机号'
    isValid = false
  }
  if (!phoneForm.code || phoneForm.code.length < 6) {
    errors.phoneCode = '请输入6位短信验证码'
    isValid = false
  }
  if (!phoneForm.agree) {
    errors.phoneAgree = '请同意用户协议和隐私政策'
    isValid = false
  }

  if (!isValid) return

  alert('手机号登录成功！')
  router.push('/')
}

// 注册处理
const handleRegister = async () => {
  clearErrors()
  let isValid = true

  if (!/^[a-zA-Z0-9]+$/.test(registerForm.username)) {
    errors.regUsername = '用户名只能包含字母或数字'
    isValid = false
  }
  if (registerForm.password.length < 6 || registerForm.password.length > 20) {
    errors.regPassword = '密码长度必须为6-20个字符'
    isValid = false
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    errors.regConfirm = '两次输入的密码不一致'
    isValid = false
  }
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    errors.regPhone = '请输入正确的手机号'
    isValid = false
  }
  if (!registerForm.code || registerForm.code.length < 6) {
    errors.regCode = '请输入6位短信验证码'
    isValid = false
  }
  if (!registerForm.agree) {
    errors.regAgree = '请同意用户协议和隐私政策'
    isValid = false
  }

  if (!isValid) return

  loading.value = true
  const result = await authAPI.register({
    username: registerForm.username,
    password: registerForm.password,
    phone: registerForm.phone
  })
  loading.value = false

  if (result.error) {
    errors.regUsername = result.error
    alert(result.error)
  } else {
    alert('注册成功，请登录')
    switchToLogin()
  }
}

// 初始化
onMounted(() => {
  generateCaptcha()
})
</script>

<style scoped>
.cjx-login-page {
  min-height: 100vh;
  background-color: #333333;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.cjx-logo {
  color: #ffffff;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.cjx-logo svg {
  width: 30px;
  height: 30px;
  fill: #2ecc71;
}

.cjx-auth-container {
  width: 100%;
  max-width: 400px;
  background-color: #444444;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.cjx-tabs {
  display: flex;
  border-bottom: 1px solid #555;
  margin-bottom: 25px;
}

.cjx-tab {
  padding: 10px 0;
  margin-right: 20px;
  color: #bbbbbb;
  cursor: pointer;
  position: relative;
  font-size: 16px;
}

.cjx-tab.active {
  color: #ffffff;
}

.cjx-tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #2ecc71;
}

.cjx-form-group {
  margin-bottom: 20px;
}

.cjx-form-control {
  width: 100%;
  padding: 12px 15px;
  background-color: #555555;
  border: 1px solid #666;
  border-radius: 4px;
  color: #ffffff;
  font-size: 14px;
}

.cjx-form-control::placeholder {
  color: #999;
}

.cjx-form-control:focus {
  outline: none;
  border-color: #2ecc71;
}

.cjx-captcha-group {
  display: flex;
  gap: 10px;
}

.cjx-captcha-image {
  flex: 0 0 120px;
  height: 44px;
  background-color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 18px;
  letter-spacing: 3px;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

.cjx-phone-input-group {
  display: flex;
  gap: 10px;
}

.cjx-country-code {
  flex: 0 0 90px;
  padding: 12px 15px;
  background-color: #555555;
  border: 1px solid #666;
  border-radius: 4px;
  color: #ffffff;
  font-size: 14px;
}

.cjx-verify-code-group {
  display: flex;
  gap: 10px;
}

.cjx-get-code-btn {
  flex: 0 0 120px;
  padding: 12px 15px;
  background-color: #666;
  border: 1px solid #777;
  border-radius: 4px;
  color: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.cjx-get-code-btn:hover {
  background-color: #777;
}

.cjx-get-code-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
}

.cjx-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.cjx-checkbox-group {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #bbbbbb;
}

.cjx-form-checkbox {
  width: 16px;
  height: 16px;
  accent-color: #2ecc71;
}

.cjx-forgot-password {
  color: #2ecc71;
  text-decoration: none;
}

.cjx-forgot-password:hover {
  text-decoration: underline;
}

.cjx-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.cjx-btn:hover {
  opacity: 0.9;
}

.cjx-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.cjx-btn-primary {
  background-color: #27ae60;
  color: #ffffff;
}

.cjx-link-text {
  text-align: center;
  margin-top: 20px;
  color: #bbbbbb;
  font-size: 14px;
}

.cjx-link-text a {
  color: #2ecc71;
  text-decoration: none;
  cursor: pointer;
}

.cjx-link-text a:hover {
  text-decoration: underline;
}

.cjx-footer {
  margin-top: 40px;
  color: #777;
  font-size: 12px;
}

.cjx-password-toggle {
  position: relative;
  display: flex;
  flex-direction: column;
}

.cjx-toggle-icon {
  position: absolute;
  right: 15px;
  top: 10px;
  color: #999;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
}
.cjx-toggle-icon svg {
  width: 20px;
  height: 20px;
  transition: color 0.2s;
}
.cjx-toggle-icon:hover svg {
  color: #333;
}

.cjx-login-tab-panel {
  display: block;
}

#cjx-register-panel h2 {
  text-align: center;
  color: #ffffff;
  margin-bottom: 25px;
  font-size: 18px;
}

.cjx-error-message {
  color: #e74c3c;
  font-size: 12px;
  margin-top: 5px;
  min-height: 16px;
}
</style>

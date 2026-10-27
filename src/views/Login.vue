<template>
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
        <form v-show="loginTab === 'password'" class="cjx-login-tab-panel" @submit.prevent="handleLogin">
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

          <button type="button" class="cjx-btn cjx-btn-primary" @click="handleLogin" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <div class="cjx-link-text">
            <a @click="switchToRegister">注册账户</a>
          </div>
        </form>

        <!-- 手机号登录面板 -->
        <form v-show="loginTab === 'phone'" class="cjx-login-tab-panel" @submit.prevent="phoneSubMode === 'password' ? handlePhoneLogin() : handlePhoneCodeLogin()">
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

          <!-- 密码模式输入 -->
          <template v-if="phoneSubMode === 'password'">
            <div class="cjx-form-group cjx-password-toggle">
              <input 
                :type="showPhonePassword ? 'text' : 'password'"
                class="cjx-form-control" 
                placeholder="请输入密码"
                v-model="phoneForm.password"
              >
              <span class="cjx-toggle-icon" @click="showPhonePassword = !showPhonePassword">
                <svg v-if="showPhonePassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </span>
            </div>
            <div class="cjx-error-message">{{ errors.phoneCode }}</div>

            <div class="cjx-options">
              <div class="cjx-checkbox-group">
                <input type="checkbox" class="cjx-form-checkbox" v-model="phoneForm.autoLogin">
                <label>自动登录</label>
              </div>
              <a href="#" class="cjx-forgot-password">忘记密码</a>
            </div>
          </template>

          <!-- 验证码模式输入（默认主流程） -->
          <template v-else>
            <div class="cjx-form-group">
              <div class="cjx-code-generate-row">
                <input 
                  type="text" 
                  class="cjx-form-control" 
                  placeholder="请输入验证码"
                  v-model="phoneForm.code"
                  maxlength="6"
                >
                <button 
                  type="button"
                  class="cjx-get-code-btn"
                  @click="generatePhoneCode"
                  :disabled="phoneCodeCountdown > 0"
                >
                  {{ phoneCodeCountdown > 0 ? `${phoneCodeCountdown}s` : '获取验证码' }}
                </button>
              </div>
              <div v-if="generatedPhoneCode" class="cjx-code-hint">
                本次验证码：<strong>{{ generatedPhoneCode }}</strong>
              </div>
            </div>
            <div class="cjx-error-message">{{ errors.phoneCode }}</div>

            <div class="cjx-auto-register-hint">
              未注册绑定手机号验证成功后自动注册
            </div>
          </template>

          <div class="cjx-checkbox-group" style="margin-bottom: 20px;">
            <input type="checkbox" class="cjx-form-checkbox" v-model="phoneForm.agree">
            <label>我已阅读并同意用户协议,隐私政策</label>
          </div>
          <div class="cjx-error-message">{{ errors.phoneAgree }}</div>

          <button 
            type="button" 
            class="cjx-btn cjx-btn-primary" 
            @click="phoneSubMode === 'password' ? handlePhoneLogin() : handlePhoneCodeLogin()" 
            :disabled="phoneLoading"
          >
            {{ phoneLoading ? '登录中...' : (phoneSubMode === 'password' ? '密码登录' : '验证登录') }}
          </button>

          <!-- 底部切换入口 -->
          <div class="cjx-bottom-switch">
            <span class="cjx-switch-divider">其他渠道</span>
            <button 
              class="cjx-switch-btn" 
              @click="switchPhoneSubMode(phoneSubMode === 'password' ? 'code' : 'password')"
              type="button"
            >
              <!-- 切到密码模式：显示锁图标 -->
              <svg v-if="phoneSubMode === 'code'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <!-- 切到验证码模式：显示锁开启图标 -->
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 9.9-1"/>
              </svg>
              {{ phoneSubMode === 'code' ? '账号密码' : '验证码登录' }}
            </button>
          </div>

          <div class="cjx-link-text">
            <a @click="switchToRegister">注册账户</a>
          </div>
        </form>
      </div>

      <!-- 注册表单 -->
      <form v-else id="cjx-register-panel" class="cjx-form-panel cjx-active" @submit.prevent="handleRegister">
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
          <button type="button" 
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

        <button type="button" class="cjx-btn cjx-btn-primary" @click="handleRegister" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <div class="cjx-link-text">
          <a @click="switchToLogin">使用已有账号登录</a>
        </div>
      </form>
    </div>

    <div class="cjx-footer">
      - Steam游戏交易平台 | 沪ICP备19042195号-1
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authAPI, snakeToCamel } from '../config/supabase-local.ts'

const router = useRouter()
const route = useRoute()

// 响应式状态
/** true = 显示登录面板，false = 显示注册面板 */
const isLogin = ref(true)
/** 登录 Tab：'password' = 账户密码，'phone' = 手机号 */
const loginTab = ref('password')
/** 手机号面板子模式：'password' = 密码登录，'code' = 验证码登录（主流程） */
const phoneSubMode = ref<'password' | 'code'>('code')
/** 登录 / 注册按钮 loading 状态 */
const loading = ref(false)
/** 手机号密码登录按钮 loading 状态 */
const phoneLoading = ref(false)
/** 密码可见性切换 */
const showPassword = ref(false)
/** 手机号密码可见性切换 */
const showPhonePassword = ref(false)
/** 注册密码可见性切换 */
const showRegPassword = ref(false)
/** 图片验证码（登录用） */
const captchaCode = ref('')
/** 手机号验证码登录的随机码（前端生成） */
const generatedPhoneCode = ref('')
/** 手机号验证码倒计时（秒） */
const phoneCodeCountdown = ref(0)
/** 注册短信验证码倒计时（秒） */
const regCodeCountdown = ref(0)

/** 账户密码登录表单 */
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  autoLogin: true,
  agree: false
})

const phoneForm = reactive({
  phone: '',
  password: '',
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
/** 生成图片验证码（字母+数字，格式如 ABC-5） */
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
/**
 * 账户密码登录流程：表单校验 → 调 authAPI.login → 根据角色跳转首页或 admin/dashboard
 */
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
      alert(result.error)
    } else {
      const user = result.data as any
      // 管理员账号不允许从前台登录页进入
      if (user?.userType === '管理员') {
        errors.loginPassword = '管理员请使用独立登录入口'
        alert('管理员请使用独立登录入口')
        return
      }
      sessionStorage.removeItem('steampy_admin')
      const isBanned = (user as any)?.userType === '已封禁'
      alert(isBanned ? '登录成功，但您的账号已被封禁，购买、出售等功能暂时无法使用！' : '登录成功！')
      window.dispatchEvent(new Event('user-logged-in'))

      const redirect = route.query.redirect as string
      if (redirect && redirect.startsWith('/')) {
        router.replace(redirect)
      } else {
        router.replace('/')
      }
    }
  } catch (error) {
    loading.value = false
    console.error('登录异常:', error)
    alert('登录失败，请重试')
  }
}

// 手机号 + 密码登录（后端 LoginReq 已支持 phone 字段）
/**
 * 手机号 + 密码登录，调 /api/auth/login 接口
 */
const handlePhoneLogin = async () => {
  clearErrors()
  let isValid = true

  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    errors.phone = '请输入正确的手机号'
    isValid = false
  }
  if (!phoneForm.password) {
    errors.phoneCode = '请输入密码'
    isValid = false
  }
  if (!phoneForm.agree) {
    errors.phoneAgree = '请同意用户协议和隐私政策'
    isValid = false
  }

  if (!isValid) return

  phoneLoading.value = true
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone: phoneForm.phone, password: phoneForm.password })
    })
    const json = await res.json()
    if (json.code === 200) {
      const user = snakeToCamel(json.data.user)
      sessionStorage.setItem('steampy_user', JSON.stringify(user))
      sessionStorage.removeItem('steampy_admin')
      if ((user as any)?.userType === '管理员') {
        alert('管理员请使用独立登录入口')
        return
      }
      const isBanned = (user as any)?.userType === '已封禁'
      alert(isBanned ? '登录成功，但您的账号已被封禁，购买、出售等功能暂时无法使用！' : '登录成功！')
      window.dispatchEvent(new Event('user-logged-in'))
      router.replace('/')
    } else {
      alert(json.message || '登录失败')
    }
  } catch (e: any) {
    alert('登录异常：' + (e?.message || '网络错误'))
  } finally {
    phoneLoading.value = false
  }
}

// 手机号面板子切换
const switchPhoneSubMode = (mode: 'password' | 'code') => {
  phoneSubMode.value = mode
  clearErrors()
  generatedPhoneCode.value = ''
  phoneForm.code = ''
  phoneForm.password = ''
}

// 生成随机 6 位验证码 + 60s 倒计时
const generatePhoneCode = () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    errors.phone = '请输入正确的手机号'
    return
  }
  errors.phone = ''
  const code = Math.floor(100000 + Math.random() * 900000).toString()
  generatedPhoneCode.value = code
  phoneForm.code = ''
  // 60 秒倒计时
  phoneCodeCountdown.value = 60
  const timer = setInterval(() => {
    phoneCodeCountdown.value--
    if (phoneCodeCountdown.value <= 0) {
      clearInterval(timer)
      generatedPhoneCode.value = ''
    }
  }, 1000)
}

// 手机号 + 验证码登录
/**
 * 手机号 + 验证码登录（主流程）：校验前端生成的随机码 → 调 /api/auth/phone-login → 登录成功自动注册
 */
const handlePhoneCodeLogin = async () => {
  clearErrors()
  let isValid = true

  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    errors.phone = '请输入正确的手机号'
    isValid = false
  }
  if (!generatedPhoneCode.value) {
    errors.phoneCode = '请先获取验证码'
    isValid = false
  } else if (!phoneForm.code) {
    errors.phoneCode = '请输入验证码'
    isValid = false
  } else if (phoneForm.code !== generatedPhoneCode.value) {
    errors.phoneCode = '验证码不正确'
    isValid = false
  }
  if (!phoneForm.agree) {
    errors.phoneAgree = '请同意用户协议和隐私政策'
    isValid = false
  }

  if (!isValid) return

  phoneLoading.value = true
  try {
    const res = await fetch('/api/auth/phone-login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone: phoneForm.phone })
    })
    const json = await res.json()
    if (json.code === 200) {
      const user = snakeToCamel(json.data.user)
      sessionStorage.setItem('steampy_user', JSON.stringify(user))
      sessionStorage.removeItem('steampy_admin')
      if ((user as any)?.userType === '管理员') {
        errors.phoneCode = '管理员请使用独立登录入口'
        return
      }
      const isBanned = (user as any)?.userType === '已封禁'
      alert(isBanned ? '登录成功，但您的账号已被封禁，购买、出售等功能暂时无法使用！' : '登录成功！')
      window.dispatchEvent(new Event('user-logged-in'))
      router.replace('/')
    } else {
      errors.phoneCode = json.message || '登录失败'
    }
  } catch (e: any) {
    errors.phoneCode = '登录异常：' + (e?.message || '网络错误')
  } finally {
    phoneLoading.value = false
  }
}

// 注册处理
/**
 * 用户注册：表单校验（用户名 / 密码 6-20 位 / 密码一致 / 手机号正则 / 6 位验证码 / 协议勾选）
 * 通过 authAPI.register 提交，成功后自动切回登录面板
 */
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
  box-sizing: border-box;
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
  box-sizing: border-box;
}

.cjx-verify-code-group {
  display: flex;
  gap: 10px;
}

/* 底部切换入口（密码登录 / 验证码登录 二选一） */
.cjx-bottom-switch {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.cjx-switch-divider {
  display: flex;
  align-items: center;
  width: 100%;
  color: #777;
  font-size: 12px;
}
.cjx-switch-divider::before,
.cjx-switch-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #555;
}
.cjx-switch-divider::before { margin-right: 12px; }
.cjx-switch-divider::after { margin-left: 12px; }
.cjx-switch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  color: #bbbbbb;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: color 0.15s, background 0.15s;
}
.cjx-switch-btn:hover {
  color: #2ecc71;
  background: rgba(46, 204, 113, 0.08);
}

/* 验证码登录自动注册提示 */
.cjx-auto-register-hint {
  font-size: 12px;
  color: #888;
  margin-bottom: 8px;
  line-height: 1.4;
}

/* 验证码行 */
.cjx-code-generate-row {
  display: flex;
  gap: 10px;
}
.cjx-code-generate-row .cjx-form-control {
  flex: 1;
}
.cjx-code-hint {
  margin-top: 6px;
  font-size: 13px;
  color: #2ecc71;
  background: rgba(46, 204, 113, 0.1);
  padding: 6px 10px;
  border-radius: 4px;
}
.cjx-code-hint strong {
  font-size: 16px;
  letter-spacing: 3px;
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
  box-sizing: border-box;
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
  box-sizing: border-box;
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

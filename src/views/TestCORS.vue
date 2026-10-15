<template>
  <div class="test-cors-page">
    <h1>CORS 测试页面</h1>
    
    <div class="test-section">
      <h2>测试: 使用代理连接 Supabase</h2>
      <button @click="testProxyConnection" :disabled="loading">测试连接</button>
      <div class="result">{{ proxyResult }}</div>
    </div>
    
    <div class="test-section">
      <h2>测试登录功能</h2>
      <div class="form-group">
        <input type="text" v-model="loginForm.username" placeholder="用户名">
      </div>
      <div class="form-group">
        <input type="password" v-model="loginForm.password" placeholder="密码">
      </div>
      <button @click="testLogin" :disabled="loading">测试登录</button>
      <div class="result">{{ loginResult }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import axios from 'axios'
import { authAPI } from '../config/supabase-local.ts'

const loading = ref(false)
const proxyResult = ref('')
const loginResult = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

// 测试代理连接
const testProxyConnection = async () => {
  loading.value = true
  proxyResult.value = '正在测试...'
  
  try {
    // 使用 axios 测试
    const response = await axios.get('/supabase/rest/v1/users?limit=1', {
      headers: {
        'apikey': 'sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_',
        'Authorization': 'Bearer sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_'
      }
    })
    
    if (response.status >= 200 && response.status < 300) {
      const data = response.data
      proxyResult.value = `成功！获取到 ${data.length} 条数据`
    } else {
      proxyResult.value = `错误: HTTP ${response.status}`
    }
  } catch (e) {
    proxyResult.value = `异常: ${e.message}`
  } finally {
    loading.value = false
  }
}

// 测试登录
const testLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    loginResult.value = '请输入用户名和密码'
    return
  }
  
  loading.value = true
  loginResult.value = '正在测试登录...'
  
  try {
    const result = await authAPI.login(loginForm.username, loginForm.password)
    
    if (result.error) {
      loginResult.value = `登录失败: ${result.error}`
    } else {
      loginResult.value = `登录成功！欢迎 ${result.data.username}`
    }
  } catch (e) {
    loginResult.value = `异常: ${e.message}`
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.test-cors-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
}

button {
  padding: 10px 20px;
  background-color: #2ecc71;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.result {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.form-group {
  margin-bottom: 10px;
}

input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
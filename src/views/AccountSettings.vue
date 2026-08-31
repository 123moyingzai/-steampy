<template>
  <Layout>
    <div class="cjx-settings-page">
      <!-- 侧边标签 -->
      <div class="cjx-settings-sidebar">
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'basic' }"
          @click="activeTab = 'basic'"
        >基本信息 719cjx</div>
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'security' }"
          @click="activeTab = 'security'"
        >账号安全 719cjx</div>
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'steam' }"
          @click="activeTab = 'steam'"
        >Steam绑定 719cjx</div>
      </div>

      <!-- 内容区域 -->
      <div class="cjx-settings-content">
        <!-- 基本信息 -->
        <div v-show="activeTab === 'basic'" class="cjx-settings-panel">
          <h2>基本信息 719cjx</h2>
          
          <div class="cjx-avatar-section">
            <div class="cjx-avatar-large">{{ avatarText }}</div>
            <button class="cjx-btn cjx-btn-secondary">更换头像</button>
          </div>

          <div class="cjx-form">
            <div class="cjx-form-row">
              <label>用户名</label>
              <input type="text" :value="userInfo.username" disabled class="cjx-input" />
              <span class="cjx-hint">用户名不可修改</span>
            </div>

            <div class="cjx-form-row">
              <label>昵称 719cjx</label>
              <input type="text" v-model="userInfo.nickname" class="cjx-input" placeholder="设置昵称" />
            </div>

            <div class="cjx-form-row">
              <label>性别 719cjx</label>
              <div class="cjx-radio-group">
                <label><input type="radio" v-model="userInfo.gender" value="male" /> 男</label>
                <label><input type="radio" v-model="userInfo.gender" value="female" /> 女</label>
              </div>
            </div>

            <div class="cjx-form-row">
              <label>国家/地区 719cjx</label>
              <select v-model="userInfo.country" class="cjx-select">
                <option value="中国">中国</option>
                <option value="美国">美国</option>
                <option value="日本">日本</option>
                <option value="其他">其他</option>
              </select>
            </div>

            <div class="cjx-form-actions">
              <button class="cjx-btn cjx-btn-primary" @click="saveBasic" :disabled="saving">
                {{ saving ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 账号安全 -->
        <div v-show="activeTab === 'security'" class="cjx-settings-panel">
          <h2>账号安全 719cjx</h2>
          
          <div class="cjx-security-list">
            <div class="cjx-security-item">
              <div class="cjx-security-info">
                <h4>登录密码 719cjx</h4>
                <p>密码强度：{{ userInfo.passwordStrength }}</p>
              </div>
              <button class="cjx-btn cjx-btn-secondary" @click="showPasswordModal = true">修改</button>
            </div>

            <div class="cjx-security-item">
              <div class="cjx-security-info">
                <h4>手机绑定 719cjx</h4>
                <p>{{ userInfo.phone || '未绑定' }}</p>
              </div>
              <button class="cjx-btn cjx-btn-secondary">更换</button>
            </div>
          </div>
        </div>

        <!-- Steam绑定 -->
        <div v-show="activeTab === 'steam'" class="cjx-settings-panel">
          <h2>Steam绑定 719cjx</h2>
          
          <div class="cjx-steam-bind" v-if="!userInfo.steam_id">
            <div class="cjx-steam-icon">🎮</div>
            <p>绑定Steam账号后可以使用代购服务</p>
            <button class="cjx-btn cjx-btn-primary">立即绑定</button>
          </div>
          
          <div class="cjx-steam-info" v-else>
            <p><strong>Steam ID：</strong>{{ userInfo.steam_id }}</p>
            <p><strong>Steam链接：</strong>{{ userInfo.steam_url }}</p>
            <button class="cjx-btn cjx-btn-secondary">解除绑定</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div class="cjx-modal" v-if="showPasswordModal" @click.self="showPasswordModal = false">
      <div class="cjx-modal-content">
        <h3>修改密码 719cjx</h3>
        <div class="cjx-form">
          <div class="cjx-form-row">
            <label>原密码</label>
            <input type="password" v-model="passwordForm.old" class="cjx-input" />
          </div>
          <div class="cjx-form-row">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.new" class="cjx-input" />
          </div>
          <div class="cjx-form-row">
            <label>确认新密码</label>
            <input type="password" v-model="passwordForm.confirm" class="cjx-input" />
          </div>
          <div class="cjx-modal-actions">
            <button class="cjx-btn cjx-btn-secondary" @click="showPasswordModal = false">取消</button>
            <button class="cjx-btn cjx-btn-primary" @click="savePassword">确认</button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const activeTab = ref('basic')
const saving = ref(false)
const showPasswordModal = ref(false)

const userInfo = ref({
  username: '',
  nickname: '',
  gender: 'male',
  country: '中国',
  phone: '',
  passwordStrength: '中',
  steam_id: '',
  steam_url: ''
})

const passwordForm = ref({
  old: '',
  new: '',
  confirm: ''
})

// 计算属性
const avatarText = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || '用'
  return name.substring(0, 2)
})

// 方法
const saveBasic = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  
  saving.value = true
  const result = await authAPI.updateUser(currentUser.id, {
    nickname: userInfo.value.nickname,
    gender: userInfo.value.gender,
    country: userInfo.value.country
  })
  saving.value = false
  
  if (result.error) {
    alert('保存失败：' + result.error)
  } else {
    alert('保存成功！')
  }
}

const savePassword = () => {
  if (passwordForm.value.new !== passwordForm.value.confirm) {
    alert('两次输入的密码不一致')
    return
  }
  alert('密码修改成功！719cjx')
  showPasswordModal.value = false
}

// 加载数据
const loadData = () => {
  const currentUser = authAPI.getCurrentUser()
  
  if (!currentUser) {
    alert('请先登录 719cjx')
    router.push('/login')
    return
  }
  
  userInfo.value = {
    username: currentUser.username || '',
    nickname: currentUser.nickname || '',
    gender: currentUser.gender || 'male',
    country: currentUser.country || '中国',
    phone: currentUser.phone || '',
    passwordStrength: currentUser.password_strength || '中',
    steam_id: currentUser.steam_id || '',
    steam_url: currentUser.steam_url || ''
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.cjx-settings-page {
  display: flex;
  gap: 20px;
}

.cjx-settings-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 10px 0;
}

.cjx-settings-tab {
  padding: 15px 20px;
  cursor: pointer;
  color: #666;
  border-left: 3px solid transparent;
  transition: all 0.3s;
}

.cjx-settings-tab:hover,
.cjx-settings-tab.active {
  background: #f5f5f5;
  border-left-color: #3498db;
  color: #3498db;
}

.cjx-settings-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
}

.cjx-settings-panel h2 {
  margin: 0 0 30px 0;
  color: #333;
  font-size: 20px;
}

.cjx-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.cjx-avatar-large {
  width: 100px;
  height: 100px;
  background: #3498db;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
}

.cjx-form-row {
  margin-bottom: 20px;
}

.cjx-form-row label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.cjx-input,
.cjx-textarea,
.cjx-select {
  width: 100%;
  max-width: 400px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.cjx-input:disabled {
  background: #f5f5f5;
}

.cjx-textarea {
  resize: vertical;
}

.cjx-hint {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.cjx-radio-group {
  display: flex;
  gap: 20px;
}

.cjx-radio-group label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.cjx-form-actions {
  margin-top: 30px;
}

.cjx-btn {
  padding: 10px 30px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.cjx-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.cjx-btn-primary {
  background: #3498db;
  color: #fff;
}

.cjx-btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.cjx-btn-secondary {
  background: #f0f0f0;
  color: #666;
}

.cjx-btn-secondary:hover {
  background: #e0e0e0;
}

.cjx-security-list {
  border-top: 1px solid #eee;
}

.cjx-security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.cjx-security-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.cjx-security-info p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.cjx-steam-bind {
  text-align: center;
  padding: 60px 20px;
}

.cjx-steam-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.cjx-steam-bind p {
  color: #666;
  margin-bottom: 20px;
}

.cjx-steam-info {
  padding: 20px 0;
}

.cjx-steam-info p {
  margin: 10px 0;
  color: #666;
}

.cjx-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cjx-modal-content {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
}

.cjx-modal-content h3 {
  margin: 0 0 20px 0;
}

.cjx-modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>

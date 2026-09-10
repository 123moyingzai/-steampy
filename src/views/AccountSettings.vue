<template>
  <div class="cjx-settings">
    <!-- 顶部 tab（与 MessageCenter 风格一致） -->
    <div class="cjx-tabs">
      <div class="cjx-tab" :class="{ active: tab === 'profile' }" @click="tab = 'profile'">👤 基本信息</div>
      <div class="cjx-tab" :class="{ active: tab === 'security' }" @click="tab = 'security'">🔐 修改密码</div>
      <div class="cjx-tab" :class="{ active: tab === 'steam' }" @click="tab = 'steam'">🎮 Steam 绑定</div>
    </div>

    <!-- 基本信息 -->
    <div v-if="tab === 'profile'" class="cjx-card">
      <h3 class="cjx-card-title">基本信息</h3>
      <p class="cjx-card-hint">用户名是账号的唯一标识，不可修改</p>

      <div class="cjx-form-row">
        <label>用户名</label>
        <input v-model="form.username" disabled class="cjx-input cjx-input-disabled" />
      </div>

      <div class="cjx-form-row">
        <label>昵称</label>
        <input v-model="form.nickname" class="cjx-input" placeholder="显示给其他玩家的名字" />
      </div>

      <div class="cjx-form-row">
        <label>手机号</label>
        <input v-model="form.phone" class="cjx-input" placeholder="选填" maxlength="20" />
      </div>

      <div class="cjx-form-row">
        <label>邮箱</label>
        <input v-model="form.email" class="cjx-input" placeholder="选填" />
      </div>

      <div class="cjx-form-row">
        <label>头像 URL</label>
        <input v-model="form.avatarUrl" class="cjx-input" placeholder="图片链接" />
      </div>

      <div v-if="form.avatarUrl" class="cjx-avatar-preview">
        <img :src="form.avatarUrl" alt="头像预览" />
      </div>

      <div class="cjx-form-actions">
        <button class="cjx-btn-primary" :disabled="saving" @click="saveProfile">
          {{ saving ? '保存中...' : '保存修改' }}
        </button>
        <span v-if="profileMsg" class="cjx-form-msg" :class="profileMsgType">{{ profileMsg }}</span>
      </div>
    </div>

    <!-- 修改密码 -->
    <div v-if="tab === 'security'" class="cjx-card">
      <h3 class="cjx-card-title">修改密码</h3>
      <p class="cjx-card-hint">修改后下次登录生效</p>

      <div class="cjx-form-row">
        <label>当前密码</label>
        <input v-model="pwdForm.oldPassword" type="password" class="cjx-input" placeholder="请输入当前密码" />
      </div>
      <div class="cjx-form-row">
        <label>新密码</label>
        <input v-model="pwdForm.newPassword" type="password" class="cjx-input" placeholder="不少于 4 位" />
      </div>
      <div class="cjx-form-row">
        <label>确认新密码</label>
        <input v-model="pwdForm.confirmPassword" type="password" class="cjx-input" placeholder="再输一遍新密码" />
      </div>

      <div class="cjx-form-actions">
        <button class="cjx-btn-primary" :disabled="changing" @click="changePassword">
          {{ changing ? '提交中...' : '确认修改' }}
        </button>
        <span v-if="pwdMsg" class="cjx-form-msg" :class="pwdMsgType">{{ pwdMsg }}</span>
      </div>
    </div>

    <!-- Steam 绑定 -->
    <div v-if="tab === 'steam'" class="cjx-card">
      <h3 class="cjx-card-title">🎮 Steam 绑定</h3>
      <p class="cjx-card-hint">展示您的 Steam 社区公开信息</p>

      <div v-if="!form.steamId" class="cjx-empty">
        暂未绑定 Steam 账号<br />
        <small>（可通过 Steam 社区绑定，或联系管理员手动绑定）</small>
      </div>

      <div v-else class="cjx-steam-info">
        <div class="cjx-steam-avatar">
          <img v-if="form.steamAvatarUrl" :src="form.steamAvatarUrl" alt="Steam头像" />
          <div v-else>{{ (form.steamName || 'S').substring(0, 1) }}</div>
        </div>
        <div class="cjx-steam-meta">
          <div class="cjx-steam-name">{{ form.steamName || '未知' }}</div>
          <div class="cjx-steam-row"><span>Steam ID</span><span>{{ form.steamId }}</span></div>
          <div v-if="form.steamLevel" class="cjx-steam-row"><span>等级</span><span>{{ form.steamLevel }}</span></div>
          <div v-if="form.steamGameCount" class="cjx-steam-row"><span>游戏数</span><span>{{ form.steamGameCount }}</span></div>
          <div v-if="form.steamPlaytime" class="cjx-steam-row"><span>游戏时长</span><span>{{ form.steamPlaytime }} 小时</span></div>
          <div v-if="form.steamRegion" class="cjx-steam-row"><span>地区</span><span>{{ form.steamRegion }}</span></div>
          <div v-if="form.steamAccountValue" class="cjx-steam-row"><span>账户价值</span><span>¥{{ form.steamAccountValue }}</span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../config/supabase-local.ts'

const router = useRouter()
const tab = ref<'profile' | 'security' | 'steam'>('profile')

const form = ref<any>({})
const saving = ref(false)
const profileMsg = ref('')
const profileMsgType = ref<'ok' | 'err'>('ok' as const)

const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changing = ref(false)
const pwdMsg = ref('')
const pwdMsgType = ref<'ok' | 'err'>('ok' as const)

const syncUser = (src: any) => {
  form.value = {
    username: src.username || '',
    nickname: src.nickname || '',
    phone: src.phone || '',
    email: src.email || '',
    avatarUrl: src.avatarUrl || '',
    steamId: src.steamId || '',
    steamName: src.steamName || '',
    steamAvatarUrl: src.steamAvatarUrl || '',
    steamLevel: src.steamLevel ?? null,
    steamGameCount: src.steamGameCount ?? null,
    steamPlaytime: src.steamPlaytime ?? null,
    steamRegion: src.steamRegion || '',
    steamAccountValue: src.steamAccountValue ?? null
  }
}

onMounted(async () => {
  const u = authAPI.getCurrentUser()
  if (!u) { router.push('/login'); return }
  const fresh = await authAPI.getUser(u.id)
  syncUser(fresh || u)
})

const saveProfile = async () => {
  const u = authAPI.getCurrentUser()
  if (!u) return
  saving.value = true; profileMsg.value = ''
  try {
    const r = await authAPI.updateUser(u.id, {
      nickname: form.value.nickname?.trim(),
      phone: form.value.phone?.trim(),
      email: form.value.email?.trim(),
      avatarUrl: form.value.avatarUrl?.trim()
    })
    if (r.error) { profileMsg.value = r.error; profileMsgType.value = 'err' }
    else {
      const merged = { ...u, ...r.data }
      sessionStorage.setItem('steampy_user', JSON.stringify(merged))
      profileMsg.value = '保存成功 ✅'; profileMsgType.value = 'ok'
    }
  } catch (e: any) {
    profileMsg.value = e.message || '保存失败'; profileMsgType.value = 'err'
  } finally {
    saving.value = false
    setTimeout(() => { profileMsg.value = '' }, 3000)
  }
}

const changePassword = async () => {
  const u = authAPI.getCurrentUser()
  if (!u) return
  const { oldPassword, newPassword, confirmPassword } = pwdForm.value
  pwdMsg.value = ''
  if (!oldPassword || !newPassword || !confirmPassword) {
    pwdMsg.value = '请填写完整'; pwdMsgType.value = 'err'; return
  }
  if (newPassword.length < 4) {
    pwdMsg.value = '新密码不少于 4 位'; pwdMsgType.value = 'err'; return
  }
  if (newPassword !== confirmPassword) {
    pwdMsg.value = '两次新密码不一致'; pwdMsgType.value = 'err'; return
  }
  if (oldPassword === newPassword) {
    pwdMsg.value = '新密码不能和原密码相同'; pwdMsgType.value = 'err'; return
  }
  changing.value = true
  try {
    const r = await authAPI.changePassword(u.id, oldPassword, newPassword)
    if (r.error) { pwdMsg.value = r.error; pwdMsgType.value = 'err' }
    else {
      pwdMsg.value = '密码修改成功 ✅'; pwdMsgType.value = 'ok'
      pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    }
  } catch (e: any) {
    pwdMsg.value = e.message || '修改失败'; pwdMsgType.value = 'err'
  } finally {
    changing.value = false
    setTimeout(() => { pwdMsg.value = '' }, 3000)
  }
}
</script>

<style scoped>
.cjx-settings {
  max-width: 720px;
  margin: 0 auto;
}

/* 顶部 tab（与 MessageCenter 同款） */
.cjx-tabs {
  display: flex;
  align-items: center;
  gap: 1rem;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 1rem;
}
.cjx-tab {
  padding: 0.75rem 1.25rem;
  cursor: pointer;
  font-weight: 600;
  color: #666;
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s;
}
.cjx-tab:hover { color: #2c3e50; }
.cjx-tab.active {
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  margin-bottom: -2px;
}

/* 卡片容器 */
.cjx-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px 28px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.cjx-card-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
}
.cjx-card-hint {
  margin: 0 0 20px;
  font-size: 12px;
  color: #999;
}

/* 空状态 */
.cjx-empty {
  text-align: center;
  color: #999;
  padding: 60px 20px;
  font-size: 14px;
  line-height: 1.8;
}

/* 表单 */
.cjx-form-row {
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.cjx-form-row label {
  width: 96px;
  flex-shrink: 0;
  font-size: 13px;
  color: #666;
  text-align: right;
}
.cjx-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.15s;
  font-family: inherit;
  box-sizing: border-box;
}
.cjx-input:focus { outline: none; border-color: #3498db; }
.cjx-input-disabled { background: #f5f5f5; color: #999; cursor: not-allowed; }

.cjx-avatar-preview {
  margin-left: 112px;
  margin-bottom: 14px;
}
.cjx-avatar-preview img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eee;
}

.cjx-form-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  padding-left: 112px;
}
.cjx-btn-primary {
  padding: 7px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  background: #3498db;
  color: #fff;
  font-weight: 600;
  transition: opacity 0.15s;
}
.cjx-btn-primary:hover:not(:disabled) { background: #2e89c7; }
.cjx-btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.cjx-form-msg { font-size: 13px; }
.cjx-form-msg.ok { color: #27ae60; }
.cjx-form-msg.err { color: #e74c3c; }

/* Steam 绑定展示 */
.cjx-steam-info {
  display: flex;
  gap: 20px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #1b2838, #2a475e);
  border-radius: 8px;
  color: #fff;
}
.cjx-steam-avatar img, .cjx-steam-avatar > div {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #3498db;
  object-fit: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  flex-shrink: 0;
}
.cjx-steam-meta { flex: 1; min-width: 0; }
.cjx-steam-name { font-size: 17px; font-weight: 700; margin-bottom: 8px; }
.cjx-steam-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #c7d5e0;
  margin-bottom: 3px;
}
.cjx-steam-row span:first-child { width: 72px; color: #8f98a0; flex-shrink: 0; }

@media (max-width: 700px) {
  .cjx-form-row { flex-direction: column; align-items: flex-start; gap: 6px; }
  .cjx-form-row label { text-align: left; width: auto; }
  .cjx-form-actions, .cjx-avatar-preview { padding-left: 0; margin-left: 0; }
}
</style>

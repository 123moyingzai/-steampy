<template>
  <Layout>
    <div class="cjx-settings-page">
      <!-- 侧边标签 -->
      <div class="cjx-settings-sidebar">
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'basic' }"
          @click="activeTab = 'basic'"
        >基本信息</div>
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'security' }"
          @click="activeTab = 'security'"
        >账号安全</div>
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'library' }"
          @click="switchToLibrary"
        >游戏库</div>
        <div 
          class="cjx-settings-tab" 
          :class="{ active: activeTab === 'steam' }"
          @click="switchToSteam"
        >
          Steam绑定
          <span v-if="steamBound" class="cjx-bind-dot" title="已绑定"></span>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="cjx-settings-content">
        <!-- 基本信息 -->
        <div v-show="activeTab === 'basic'" class="cjx-settings-panel">
          <h2>基本信息</h2>
          
          <div class="cjx-avatar-section">
            <div class="cjx-avatar-large" :style="userInfo.avatarUrl ? `background-image:url(${userInfo.avatarUrl});background-size:cover;background-position:center;` : ''"><span v-if="!userInfo.avatarUrl">{{ avatarText }}</span></div>
            <div>
              <button class="cjx-btn cjx-btn-secondary" @click="$refs.avatarInput.click()">
                {{ uploadingAvatar ? '上传中...' : '更换头像' }}
              </button>
              <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarFile" />
            </div>
          </div>

          <div class="cjx-form">
            <div class="cjx-form-row">
              <label>用户名</label>
              <input type="text" :value="userInfo.username" disabled class="cjx-input" />
              <span class="cjx-hint">用户名不可修改</span>
            </div>

            <div class="cjx-form-row">
              <label>昵称</label>
              <input type="text" v-model="userInfo.nickname" class="cjx-input" placeholder="设置昵称" />
            </div>

            <div class="cjx-form-row">
              <label>性别</label>
              <div class="cjx-radio-group">
                <label><input type="radio" v-model="userInfo.gender" value="male" /> 男</label>
                <label><input type="radio" v-model="userInfo.gender" value="female" /> 女</label>
              </div>
            </div>

            <div class="cjx-form-row">
              <label>国家/地区</label>
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
          <h2>账号安全</h2>
          
          <div class="cjx-security-list">
            <div class="cjx-security-item">
              <div class="cjx-security-info">
                <h4>登录密码</h4>
                <p>密码强度：{{ userInfo.passwordStrength }}</p>
              </div>
              <button class="cjx-btn cjx-btn-secondary" @click="showPasswordModal = true">修改</button>
            </div>

            <div class="cjx-security-item">
              <div class="cjx-security-info">
                <h4>手机绑定</h4>
                <p>{{ userInfo.phone || '未绑定' }}</p>
              </div>
              <button class="cjx-btn cjx-btn-secondary">更换</button>
            </div>
          </div>
        </div>

        <!-- ========== 游戏库 ========== -->
        <div v-show="activeTab === 'library'" class="cjx-settings-panel">
          <h2>🎮 游戏库</h2>

          <!-- Tab 切换：展柜 / 收藏 -->
          <div class="cjx-lib-tabs">
            <span :class="{ active: libTab === 'showcase' }" @click="libTab = 'showcase'; loadShowcase()">游戏展柜 ({{ showcase.length }})</span>
            <span :class="{ active: libTab === 'fav' }" @click="libTab = 'fav'; loadFavorites()">游戏收藏 ({{ favorites.length }})</span>
          </div>

          <!-- 展柜 -->
          <div v-if="libTab === 'showcase'" class="cjx-lib-section">
            <p v-if="showcase.length === 0 && !libLoading" class="cjx-lib-empty">还没有购买记录，快去首页逛逛吧～</p>
            <div v-else class="cjx-lib-grid">
              <div v-for="o in showcase" :key="o.id" class="cjx-lib-card" @click="$router.push('/game/' + o.gameId)">
                <div class="cjx-lib-img-wrap">
                  <img :src="getImageUrl(o.gameImage)" @error="(e:any)=>{e.target.style.display='none'}" />
                </div>
                <div class="cjx-lib-info">
                  <div class="cjx-lib-name" :title="o.gameName">{{ o.gameName }}</div>
                  <div class="cjx-lib-date">购买于 {{ formatDate(o.createdAt) }}</div>
                  <div class="cjx-lib-price-row">
                    <span class="cjx-lib-final">¥{{ Number(o.price ?? o.totalPrice ?? 0).toFixed(2) }}</span>
                    <span v-if="o.originalPrice && Number(o.originalPrice) > Number(o.price ?? 0)" class="cjx-lib-original">¥{{ Number(o.originalPrice).toFixed(2) }}</span>
                    <span v-if="o.discount" class="cjx-lib-discount">{{ o.discount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 收藏 -->
          <div v-if="libTab === 'fav'" class="cjx-lib-section">
            <p v-if="favorites.length === 0 && !libLoading" class="cjx-lib-empty">还没有收藏，在游戏详情页点 ☆ 收藏吧～</p>
            <div v-else class="cjx-lib-grid">
              <div v-for="f in favorites" :key="f.favoriteId || f.id" class="cjx-lib-card" @click="$router.push('/game/' + f.gameId)">
                <div class="cjx-lib-img-wrap">
                  <img :src="getImageUrl(f.imageUrl || f.image)" @error="(e:any)=>{e.target.style.display='none'}" />
                </div>
                <div class="cjx-lib-info">
                  <div class="cjx-lib-name" :title="f.name">{{ f.name }}</div>
                  <div class="cjx-lib-price-row">
                    <span class="cjx-lib-final">¥{{ Number(f.price ?? 0).toFixed(2) }}</span>
                    <span v-if="f.originalPrice && Number(f.originalPrice) > Number(f.price ?? 0)" class="cjx-lib-original">¥{{ Number(f.originalPrice).toFixed(2) }}</span>
                    <span v-if="f.discount" class="cjx-lib-discount">{{ f.discount }}</span>
                  </div>
                  <button class="cjx-btn cjx-btn-secondary cjx-btn-small cjx-lib-unfav" @click.stop="unfavorite(f)">取消收藏</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== Steam绑定 ========== -->
        <div v-show="activeTab === 'steam'" class="cjx-settings-panel cjx-steam-panel">
          <h2>Steam 绑定</h2>

          <!-- 未绑定状态 -->
          <div v-if="!steamBound" class="cjx-steam-unbound">
            <div class="cjx-steam-icon-big">🎮</div>
            <h3>尚未绑定 Steam 账号</h3>
            <p class="cjx-steam-tip">绑定 Steam 账号后可以正常购买游戏，系统会自动检测您的游戏库存避免重复购买</p>
            <button class="cjx-btn cjx-btn-primary cjx-btn-bind" :disabled="binding" @click="handleBind">
              {{ binding ? '绑定中...' : '立即绑定 Steam' }}
            </button>
          </div>

          <!-- 已绑定状态 -->
          <div v-else>
            <!-- 顶部账号信息栏 -->
            <div class="cjx-steam-profile">
              <!-- 第一行：头像 + 名称 + SteamID + 地区 -->
              <div class="cjx-steam-row1">
                <div class="cjx-steam-avatar-wrap">
                  <img
                    v-if="steamInfo.steam_avatar_url"
                    :src="steamInfo.steam_avatar_url"
                    class="cjx-steam-avatar"
                    @error="(e: any) => { (e.target as HTMLImageElement).style.display = 'none' }"
                  />
                  <div v-else class="cjx-steam-avatar-fallback">🎮</div>
                </div>
                <div class="cjx-steam-info">
                  <div class="cjx-steam-name-row">
                    <span class="cjx-steam-name">{{ steamInfo.steam_name || '—' }}</span>
                    <span class="cjx-steam-badge">已绑定</span>
                  </div>
                  <div class="cjx-steam-meta-row">
                    <span class="cjx-steam-meta">
                      <i class="cjx-meta-icon">🆔</i>
                      Steam ID: {{ steamInfo.steam_id || '—' }}
                    </span>
                    <span class="cjx-steam-meta">
                      <i class="cjx-meta-icon">🌍</i>
                      {{ steamInfo.steam_region || '—' }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 第二行：等级 + 游戏数量 + 账号价值 + 游戏时长 -->
              <div class="cjx-steam-row2">
                <div class="cjx-stat-item">
                  <div class="cjx-stat-value cjx-stat-level">Lv.{{ steamInfo.steam_level ?? 0 }}</div>
                  <div class="cjx-stat-label">Steam 等级</div>
                </div>
                <div class="cjx-stat-item">
                  <div class="cjx-stat-value">{{ steamInfo.steam_game_count ?? 0 }}</div>
                  <div class="cjx-stat-label">游戏数量</div>
                </div>
                <div class="cjx-stat-item">
                  <div class="cjx-stat-value">¥{{ (steamInfo.steam_account_value ?? 0).toFixed(2) }}</div>
                  <div class="cjx-stat-label">账号价值</div>
                </div>
                <div class="cjx-stat-item">
                  <div class="cjx-stat-value">{{ steamInfo.steam_playtime ?? 0 }}h</div>
                  <div class="cjx-stat-label">游戏时长</div>
                </div>
              </div>
            </div>

            <!-- 库存游戏列表 -->
            <div class="cjx-library-section">
              <div class="cjx-library-head">
                <h3>🎯 游戏库存 ({{ library.length }})</h3>
                <button class="cjx-btn cjx-btn-secondary cjx-btn-small" @click="handleRefreshLibrary" :disabled="refreshing">
                  {{ refreshing ? '刷新中...' : '刷新库存' }}
                </button>
              </div>

              <div v-if="library.length > 0" class="cjx-library-grid">
                <div v-for="item in library" :key="item.game_id + '-' + item.game_name" class="cjx-library-card">
                  <div class="cjx-library-img-wrap">
                    <img
                      v-if="item.game_image"
                      :src="getImageUrl(item.game_image)"
                      class="cjx-library-img"
                      @error="(e: any) => { (e.target as HTMLImageElement).src = '/picture/安魂曲.jpg' }"
                    />
                    <div v-else class="cjx-library-img-fallback">🎮</div>
                    <div class="cjx-library-playtime">⏱ {{ item.playtime ?? 0 }}h</div>
                  </div>
                  <div class="cjx-library-name" :title="item.game_name">{{ item.game_name }}</div>
                </div>
              </div>
              <div v-else class="cjx-library-empty">
                <p>库存为空</p>
              </div>
            </div>

            <!-- 底部操作 -->
            <div class="cjx-steam-actions">
              <button class="cjx-btn cjx-btn-danger" @click="handleUnbind" :disabled="unbinding">
                {{ unbinding ? '解绑中...' : '解除绑定' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div class="cjx-modal" v-if="showPasswordModal" @click.self="showPasswordModal = false">
      <div class="cjx-modal-content">
        <h3>修改密码</h3>
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

    <!-- 绑定 Steam 弹窗 -->
    <div class="cjx-modal" v-if="showBindModal" @click.self="showBindModal = false">
      <div class="cjx-modal-content cjx-bind-modal">
        <h3>🎮 模拟绑定 Steam</h3>
        <p class="cjx-bind-desc">这是一个模拟绑定功能，后端将为您生成一份模拟的 Steam 账号信息和游戏库存数据。</p>
        <div class="cjx-modal-actions">
          <button class="cjx-btn cjx-btn-secondary" @click="showBindModal = false">取消</button>
          <button class="cjx-btn cjx-btn-primary" :disabled="binding" @click="confirmBind">
            {{ binding ? '绑定中...' : '确认绑定' }}
          </button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, steamAPI, orderAPI, favoriteAPI, snakeToCamel } from '../config/supabase-local.ts'
import Layout from '../components/Layout.vue'

const router = useRouter()

// 响应式数据
const activeTab = ref('basic')
const saving = ref(false)
const uploading = ref(false)
const uploadingAvatar = ref(false)
const showPasswordModal = ref(false)
const showBindModal = ref(false)
const binding = ref(false)
const unbinding = ref(false)
const refreshing = ref(false)

// 游戏库相关
const libTab = ref<'showcase' | 'fav'>('showcase')
const showcase = ref<any[]>([])
const favorites = ref<any[]>([])
const libLoading = ref(false)

const userInfo = ref({
  username: '',
  nickname: '',
  avatarUrl: '',
  gender: 'male',
  country: '中国',
  phone: '',
  email: '',
  passwordStrength: '中',
  steam_id: '',
  steam_url: ''
})

const passwordForm = ref({
  old: '',
  new: '',
  confirm: ''
})

// Steam 相关
const steamBound = ref(false)
const steamInfo = ref<any>({})
const library = ref<any[]>([])

const avatarText = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || '用'
  return name.substring(0, 2)
})

const getImageUrl = (path: string) => {
  if (!path) return '/picture/安魂曲.jpg'
  if (path.startsWith('http')) return path
  if (path.includes('picture/')) {
    const fileName = path.split('picture/')[1]
    if (fileName) return `/picture/${fileName}`
  }
  return path.startsWith('/') ? path : `/${path}`
}

const saveBasic = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  
  saving.value = true
  const result = await authAPI.updateUser(currentUser.id, {
    nickname: userInfo.value.nickname,
    avatarUrl: userInfo.value.avatarUrl,
    phone: userInfo.value.phone,
    email: userInfo.value.email
  })
  saving.value = false
  
  if (result.error) {
    alert('保存失败：' + result.error)
  } else {
    // 更新 sessionStorage
    const merged = { ...currentUser, ...result.data }
    sessionStorage.setItem('steampy_user', JSON.stringify(merged))
    // 通知 Layout（同 tab，storage 事件不触发）
    window.dispatchEvent(new Event('user-data-updated'))
    alert('保存成功！')
  }
}

const handleAvatarFile = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { alert('图片不能超过 5MB'); return }
  uploadingAvatar.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch('/api/upload/avatar', { method: 'POST', body: fd })
    const json = await res.json()
    if (json.code !== 200 || !json.data?.url) {
      alert('上传失败：' + (json.message || '未知错误'))
      return
    }
    userInfo.value.avatarUrl = json.data.url
    alert('头像上传成功！记得点「保存修改」生效哦 ✅')
  } catch (err: any) {
    alert('上传失败：' + (err?.message || '网络错误'))
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

const savePassword = async () => {
  if (passwordForm.value.new !== passwordForm.value.confirm) {
    alert('两次输入的密码不一致')
    return
  }
  if (!passwordForm.value.old || !passwordForm.value.new || !passwordForm.value.confirm) {
    alert('请填写完整')
    return
  }
  if (passwordForm.value.new.length < 4) {
    alert('新密码不少于 4 位')
    return
  }
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  const res = await authAPI.changePassword(currentUser.id, passwordForm.value.old, passwordForm.value.new)
  if (res.error) {
    alert('修改失败：' + res.error)
    return
  }
  alert('密码修改成功！')
  passwordForm.value = { old: '', new: '', confirm: '' }
  showPasswordModal.value = false
}

// ========== 游戏库 ==========

const switchToLibrary = () => {
  activeTab.value = 'library'
  if (libTab.value === 'showcase') loadShowcase()
  else loadFavorites()
}

const loadShowcase = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  libLoading.value = true
  try {
    const res = await orderAPI.getUserOrders(String(currentUser.id))
    // 订单数据 snakeToCamel 处理，并 JOIN games 拿原价/折扣
    const raw = (res.data || []).map(snakeToCamel) as any[]
    // 为每个订单补 originalPrice / discount（查 games 表）
    const gameIds = [...new Set(raw.map(o => o.gameId).filter(Boolean))]
    let gamesMap: Record<number, any> = {}
    if (gameIds.length > 0) {
      try {
        const gRes = await fetch('/api/games')
        const gJson = await gRes.json()
        if (gJson.code === 200) {
          const glist = (gJson.data || []).map(snakeToCamel)
          for (const g of glist) gamesMap[g.id] = g
        }
      } catch {}
    }
    showcase.value = raw.map(o => {
      const g = gamesMap[o.gameId] || {}
      return {
        ...o,
        originalPrice: g.originalPrice ?? o.originalPrice,
        discount: g.discount ?? o.discount
      }
    })
  } catch (e) {
    showcase.value = []
  } finally {
    libLoading.value = false
  }
}

const loadFavorites = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  libLoading.value = true
  try {
    const res = await favoriteAPI.listByUser(String(currentUser.id))
    favorites.value = res.data || []
  } catch (e) {
    favorites.value = []
  } finally {
    libLoading.value = false
  }
}

const unfavorite = async (f: any) => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  if (!confirm(`取消收藏「${f.name}」？`)) return
  await favoriteAPI.remove(String(currentUser.id), Number(f.gameId))
  await loadFavorites()
}

const formatDate = (dt: any) => {
  if (!dt) return ''
  const d = new Date(dt)
  if (isNaN(d.getTime())) return String(dt).substring(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

// ========== Steam 相关方法 ==========

const switchToSteam = () => {
  activeTab.value = 'steam'
  loadSteamData()
}

const loadSteamData = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return

  // 先从 sessionStorage 取状态快速响应
  steamBound.value = !!currentUser.steam_bound || !!currentUser.steamId || currentUser.steam_bound === true

  if (steamBound.value) {
    // 从后端拉最新数据
    try {
      const info = await steamAPI.getInfo(currentUser.id)
      if (info.data) steamInfo.value = info.data
      const lib = await steamAPI.getLibrary(currentUser.id)
      if (lib.data) library.value = lib.data
    } catch (e) {
      console.warn('加载 Steam 数据失败', e)
    }
  } else {
    steamInfo.value = {}
    library.value = []
  }
}

const handleBind = () => {
  showBindModal.value = true
}

const confirmBind = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) {
    alert('请先登录')
    router.push('/login')
    return
  }
  binding.value = true
  try {
    const res = await steamAPI.bind(currentUser.id)
    if (res.error) {
      alert('绑定失败：' + res.error)
    } else {
      showBindModal.value = false
      alert('Steam 绑定成功！')
      await loadSteamData()
    }
  } catch (e: any) {
    alert('绑定失败：' + (e?.message || '未知错误'))
  } finally {
    binding.value = false
  }
}

const handleUnbind = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  if (!confirm('确定要解绑 Steam 吗？解绑后将无法购买游戏。')) return

  unbinding.value = true
  try {
    const res = await steamAPI.unbind(currentUser.id)
    if (res.error) {
      alert('解绑失败：' + res.error)
    } else {
      steamBound.value = false
      steamInfo.value = {}
      library.value = []
      alert('已解绑 Steam')
    }
  } catch (e: any) {
    alert('解绑失败：' + (e?.message || '未知错误'))
  } finally {
    unbinding.value = false
  }
}

const handleRefreshLibrary = async () => {
  const currentUser = authAPI.getCurrentUser()
  if (!currentUser) return
  refreshing.value = true
  try {
    const res = await steamAPI.getLibrary(currentUser.id)
    if (res.data) {
      library.value = res.data
      alert('库存已刷新')
    }
  } catch (e) {
    alert('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// ========== 初始加载 ==========

const loadData = () => {
  const currentUser = authAPI.getCurrentUser()
  
  if (!currentUser) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  userInfo.value = {
    username: currentUser.username || '',
    nickname: currentUser.nickname || '',
    avatarUrl: currentUser.avatarUrl || currentUser.avatar_url || '',
    gender: currentUser.gender || 'male',
    country: currentUser.country || '中国',
    phone: currentUser.phone || '',
    email: currentUser.email || '',
    passwordStrength: currentUser.password_strength || '中',
    steam_id: currentUser.steam_id || currentUser.steamId || '',
    steam_url: currentUser.steam_url || ''
  }

  // 同步 Steam 绑定状态
  steamBound.value = !!currentUser.steam_bound || !!currentUser.steamId
  if (steamBound.value) {
    steamInfo.value = {
      steam_id: currentUser.steam_id || currentUser.steamId,
      steam_name: currentUser.steam_name || currentUser.steamName,
      steam_avatar_url: currentUser.steam_avatar_url || currentUser.steamAvatarUrl,
      steam_region: currentUser.steam_region || currentUser.steamRegion,
      steam_level: currentUser.steam_level ?? currentUser.steamLevel,
      steam_game_count: currentUser.steam_game_count ?? currentUser.steamGameCount,
      steam_account_value: currentUser.steam_account_value ?? currentUser.steamAccountValue,
      steam_playtime: currentUser.steam_playtime ?? currentUser.steamPlaytime
    }
  }
}

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
  position: relative;
}

.cjx-settings-tab:hover,
.cjx-settings-tab.active {
  background: #f5f5f5;
  border-left-color: #3498db;
  color: #3498db;
}

.cjx-bind-dot {
  position: absolute;
  top: 12px;
  right: 14px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
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

.cjx-btn-small {
  padding: 6px 14px;
  font-size: 12px;
}

.cjx-btn-danger {
  background: #e74c3c;
  color: #fff;
}

.cjx-btn-danger:hover:not(:disabled) {
  background: #c0392b;
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

/* ========== 游戏库 ========== */
.cjx-lib-tabs {
  display: flex;
  gap: 30px;
  border-bottom: 2px solid #eee;
  margin-bottom: 24px;
}
.cjx-lib-tabs span {
  padding: 10px 4px;
  cursor: pointer;
  color: #888;
  font-size: 15px;
  font-weight: 500;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
}
.cjx-lib-tabs span:hover { color: #3498db; }
.cjx-lib-tabs span.active {
  color: #3498db;
  border-bottom-color: #3498db;
}

.cjx-lib-section { padding-top: 4px; }

.cjx-lib-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.cjx-lib-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}
.cjx-lib-card:hover {
  border-color: #3498db;
  box-shadow: 0 4px 14px rgba(52,152,219,0.18);
  transform: translateY(-2px);
}

.cjx-lib-img-wrap {
  position: relative;
  width: 100%;
  padding-top: 56%;
  background: #1b2838;
}
.cjx-lib-img-wrap img {
  position: absolute;
  inset: 0;
  width: 100%; height: 100%;
  object-fit: cover;
}

.cjx-lib-info { padding: 12px 14px 14px; }
.cjx-lib-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
.cjx-lib-date {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.cjx-lib-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
}
.cjx-lib-final {
  font-size: 16px;
  font-weight: 700;
  color: #e74c3c;
}
.cjx-lib-original {
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
}
.cjx-lib-discount {
  font-size: 11px;
  color: #fff;
  background: #e74c3c;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 500;
}
.cjx-lib-unfav { margin-top: 8px; width: 100%; }

.cjx-lib-empty {
  text-align: center;
  padding: 50px 20px;
  color: #aaa;
  font-size: 14px;
}

/* ========== Steam 未绑定 ========== */
.cjx-steam-unbound {
  text-align: center;
  padding: 80px 20px;
}

.cjx-steam-icon-big {
  font-size: 80px;
  margin-bottom: 16px;
}

.cjx-steam-unbound h3 {
  color: #333;
  margin: 0 0 12px 0;
}

.cjx-steam-tip {
  color: #888;
  max-width: 460px;
  margin: 0 auto 28px;
  font-size: 14px;
  line-height: 1.7;
}

.cjx-btn-bind {
  padding: 12px 40px;
  font-size: 15px;
  background: linear-gradient(135deg, #1b2838, #2a475e);
  box-shadow: 0 4px 14px rgba(42, 71, 94, 0.3);
}

.cjx-btn-bind:hover:not(:disabled) {
  background: linear-gradient(135deg, #2a475e, #1b2838);
}

/* ========== Steam 顶部账号栏 ========== */
.cjx-steam-profile {
  background: linear-gradient(135deg, #1b2838 0%, #2a475e 100%);
  border-radius: 12px;
  padding: 24px 28px;
  color: #e5e5e5;
  margin-bottom: 28px;
  box-shadow: 0 4px 16px rgba(27, 40, 56, 0.15);
}

.cjx-steam-row1 {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.cjx-steam-avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #66c0f4;
  flex-shrink: 0;
  background: #3a4a5c;
}

.cjx-steam-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cjx-steam-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.cjx-steam-info {
  flex: 1;
  min-width: 0;
}

.cjx-steam-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.cjx-steam-name {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.cjx-steam-badge {
  background: #52c41a;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.cjx-steam-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  color: #b8b6b4;
}

.cjx-steam-meta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.cjx-meta-icon {
  font-style: normal;
}

/* 第二行统计 */
.cjx-steam-row2 {
  display: flex;
  gap: 0;
  padding-top: 18px;
}

.cjx-stat-item {
  flex: 1;
  text-align: center;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.cjx-stat-item:last-child {
  border-right: none;
}

.cjx-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}

.cjx-stat-level {
  color: #66c0f4;
}

.cjx-stat-label {
  font-size: 12px;
  color: #8f98a0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* ========== 库存 ========== */
.cjx-library-section {
  margin-bottom: 28px;
}

.cjx-library-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.cjx-library-head h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.cjx-library-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}

.cjx-library-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
  cursor: default;
}

.cjx-library-card:hover {
  border-color: #66c0f4;
  box-shadow: 0 4px 12px rgba(102, 192, 244, 0.2);
  transform: translateY(-2px);
}

.cjx-library-img-wrap {
  position: relative;
  width: 100%;
  padding-top: 56%;
  background: #1b2838;
}

.cjx-library-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cjx-library-img-fallback {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
}

.cjx-library-playtime {
  position: absolute;
  bottom: 6px;
  left: 6px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.cjx-library-name {
  padding: 10px 12px;
  font-size: 13px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cjx-library-empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* ========== 底部操作 ========== */
.cjx-steam-actions {
  border-top: 1px solid #eee;
  padding-top: 20px;
  text-align: right;
}

/* ========== 弹窗 ========== */
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

.cjx-bind-modal h3 {
  text-align: center;
}

.cjx-bind-desc {
  text-align: center;
  color: #666;
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 24px;
}

.cjx-modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>

<template>
  <Layout>
    <div class="rp-page">
      <!-- 返回按钮 -->
      <div class="rp-back" @click="handleBack">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
        <span>返回</span>
      </div>

      <!-- 游戏名 -->
      <div class="rp-game" v-if="gameName">
        <span class="rp-game-label">评测游戏</span>
        <span class="rp-game-name">{{ gameName }}</span>
      </div>

      <div class="rp-card">
        <!-- 推荐 / 不推荐 -->
        <div class="rp-row">
          <div class="rp-label">你的评价</div>
          <div class="rp-recommend">
            <label
              class="rp-rec-btn rp-rec-pos"
              :class="{ active: form.recommend === 1 }"
              @click="form.recommend = 1"
            >
              <span class="rp-rec-emoji">👍</span>
              <span>推荐</span>
            </label>
            <label
              class="rp-rec-btn rp-rec-neg"
              :class="{ active: form.recommend === 0 }"
              @click="form.recommend = 0"
            >
              <span class="rp-rec-emoji">👎</span>
              <span>不推荐</span>
            </label>
          </div>
        </div>

        <!-- 图片上传 -->
        <div class="rp-row">
          <div class="rp-label">图片（可选，最多 4 张）</div>
          <div class="rp-images">
            <div class="rp-img-item" v-for="(url, i) in form.imagesList" :key="i">
              <img :src="url" />
              <button class="rp-img-del" @click="removeImage(i)">×</button>
            </div>
            <label class="rp-img-add" v-if="form.imagesList.length < 4">
              <input type="file" accept="image/*" hidden @change="onUpload" />
              <div class="rp-img-add-inner">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                <span>添加图片</span>
              </div>
            </label>
          </div>
          <div class="rp-upload-tip" v-if="uploadCooldownLeft > 0">图片上传需稍后 {{ uploadCooldownLeft }} 秒（5分钟内限 2 次）</div>
        </div>

        <!-- 内容 -->
        <div class="rp-row">
          <div class="rp-label">
            评测内容
            <span class="rp-count" :class="{ warn: form.content.length < 5 }">{{ form.content.length }} 字</span>
          </div>
          <textarea
            v-model="form.content"
            class="rp-textarea"
            maxlength="500"
            placeholder="说说这款游戏值得 / 不值得购买的理由…（不少于 5 字）"
          ></textarea>
        </div>

        <!-- 提交 -->
        <button class="rp-submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : existingId ? '修改评测' : '发布评测' }}
        </button>
      </div>

      <!-- 规则提示 -->
      <div class="rp-rules">
        <p>① 每一位用户，每个游戏，仅可评论一次，发布成功后支持编辑修改。</p>
        <p>② 图片上传，每五分钟内仅可上传两次，勿频繁操作。</p>
        <p>③ 评论内容提交后会进行审核，列表页稍后刷新查看。</p>
        <p>④ 请勿带有辱骂、广告、诱导等其他违法词汇，严重违规者账号作封禁处罚。</p>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authAPI, reviewAPI, fetchAllGames } from '../config/supabase-local'

const route = useRoute()
const router = useRouter()

const gameId = Number(route.params.gameId || 0)
const gameName = ref('')
const existingId = ref('')
const submitting = ref(false)

const form = reactive({
  recommend: -1, // -1 = 未选
  content: '',
  imagesList: [] as string[]
})

// 上传冷却（每 5 分钟 2 次，前端轻量限频）
const lastUploadTimes = ref<number[]>([])
const uploadCooldownLeft = computed(() => {
  const now = Date.now()
  // 清理 5 分钟外的
  lastUploadTimes.value = lastUploadTimes.value.filter(t => now - t < 5 * 60 * 1000)
  if (lastUploadTimes.value.length >= 2) {
    const earliest = lastUploadTimes.value[0]
    return Math.max(0, Math.ceil((earliest + 5 * 60 * 1000 - now) / 1000))
  }
  return 0
})

// 把 base64 图暂存 localStorage（后端无文件服务，简化方案）
function onUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (uploadCooldownLeft.value > 0) {
    alert('图片上传冷却中，请稍后再试')
    input.value = ''
    return
  }
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    alert('仅支持 jpg / png / webp')
    input.value = ''
    return
  }
  if (file.size > 3 * 1024 * 1024) {
    alert('图片不超过 3MB')
    input.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    form.imagesList.push(reader.result as string)
    lastUploadTimes.value.push(Date.now())
  }
  reader.readAsDataURL(file)
  input.value = ''
}

function removeImage(i: number) {
  form.imagesList.splice(i, 1)
}

async function submit() {
  if (form.recommend !== 0 && form.recommend !== 1) {
    alert('请先选择「推荐」或「不推荐」')
    return
  }
  if (form.content.trim().length < 5) {
    alert('评测内容不少于 5 个字')
    return
  }

  const currentUser = authAPI.getCurrentUser()
  if (!currentUser?.id) {
    alert('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true
  try {
    const res = await reviewAPI.save({
      id: existingId.value || undefined,
      gameId,
      userId: currentUser.id,
      userName: currentUser.username || currentUser.nickname || '匿名用户',
      recommend: form.recommend,
      content: form.content.trim(),
      images: form.imagesList.join(',')
    })
    if (res?.code === 200 || !res?.code) {
      alert(existingId.value ? '修改成功' : '发布成功，评论稍后展示')
      router.back()
    } else {
      alert(res?.msg || res?.message || '发布失败')
    }
  } catch (e: any) {
    alert('发布失败：' + (e?.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

function handleBack() {
  if (window.history.length > 1) router.back()
  else router.push(`/game/${gameId}?game_id=${gameId}`)
}

onMounted(async () => {
  // 1. 取游戏名
  try {
    const all = await fetchAllGames()
    const g = all.find(x => Number(x.id) === gameId)
    gameName.value = g?.name || `游戏 #${gameId}`
  } catch {
    gameName.value = `游戏 #${gameId}`
  }

  // 2. 回显已有评论（编辑模式）
  const u = authAPI.getCurrentUser()
  if (u?.id) {
    const r = await reviewAPI.myReview(gameId, u.id)
    if (r) {
      existingId.value = r.id
      form.recommend = r.recommend
      form.content = r.content || ''
      form.imagesList = (r.images || '').split(',').filter(Boolean)
    }
  }
})
</script>

<style scoped>
.rp-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  max-width: 720px;
  margin: 0 auto;
}
.rp-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  cursor: pointer;
  color: #4a6cf7;
  font-size: 14px;
  border-radius: 6px;
  background: #f0f4ff;
  margin-bottom: 12px;
  user-select: none;
  transition: all 0.15s;
}
.rp-back:hover { background: #e0e8ff; }
.rp-back:active { transform: translateX(-2px); }

.rp-game {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 14px;
  border-bottom: 1px solid #eee;
  margin-bottom: 18px;
}
.rp-game-label {
  font-size: 12px;
  color: #999;
  letter-spacing: 0.5px;
}
.rp-game-name {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}

.rp-card {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.rp-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rp-label {
  font-size: 14px;
  color: #444;
  font-weight: 600;
}
.rp-count {
  margin-left: 8px;
  font-size: 12px;
  color: #888;
  font-weight: 400;
}
.rp-count.warn { color: #e74c3c; }

.rp-recommend {
  display: flex;
  gap: 12px;
}
.rp-rec-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  color: #555;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
  background: #fafafa;
}
.rp-rec-btn:hover { border-color: #bbb; }
.rp-rec-emoji { font-size: 22px; }
.rp-rec-btn.active { border-width: 2px; color: #fff; font-weight: 600; }
.rp-rec-pos.active { border-color: #2ecc71; background: linear-gradient(135deg, #2ecc71, #27ae60); }
.rp-rec-neg.active { border-color: #e74c3c; background: linear-gradient(135deg, #e74c3c, #c0392b); }

.rp-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.rp-img-item {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}
.rp-img-item img { width: 100%; height: 100%; object-fit: cover; }
.rp-img-del {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border: none;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
}
.rp-img-add {
  width: 90px;
  height: 90px;
  border: 2px dashed #ccc;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}
.rp-img-add-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #888;
  font-size: 12px;
}
.rp-img-add:hover { border-color: #4a6cf7; }
.rp-img-add:hover .rp-img-add-inner { color: #4a6cf7; }

.rp-upload-tip {
  font-size: 12px;
  color: #e67e22;
}

.rp-textarea {
  width: 100%;
  min-height: 160px;
  padding: 12px 14px;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.15s;
}
.rp-textarea:focus { border-color: #4a6cf7; }

.rp-submit {
  margin-top: 4px;
  padding: 14px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #4a6cf7, #3a5ce5);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.rp-submit:hover:not(:disabled) { opacity: 0.92; transform: translateY(-1px); }
.rp-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.rp-rules {
  margin-top: 28px;
  padding: 14px 16px;
  background: #fffbf0;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  font-size: 12.5px;
  color: #8a7a2c;
  line-height: 1.8;
}
.rp-rules p { margin: 0; }
</style>

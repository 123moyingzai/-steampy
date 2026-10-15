<template>
  <div class="cjx-msg-center">
    <!-- 返回按钮 -->
    <div class="cjx-back-bar">
      <button class="cjx-back-btn" @click="router.back()">← 返回</button>
    </div>

    <!-- 顶部标签 -->
    <div class="cjx-tabs">
      <div class="cjx-tab" :class="{ active: tab === 'msg' }" @click="switchTab('msg')">
        💬 交互消息
        <span v-if="msgUnread > 0" class="cjx-tab-badge">{{ msgUnread }}</span>
      </div>
      <div class="cjx-tab" :class="{ active: tab === 'ann' }" @click="switchTab('ann')">
        🔔 官方公告
        <span v-if="annUnread > 0" class="cjx-tab-badge">{{ annUnread }}</span>
      </div>
      <button v-if="(tab === 'msg' ? msgUnread : annUnread) > 0" class="cjx-mark-all" @click="markAllRead">全部已读</button>
    </div>

    <!-- 交互消息筛选栏 -->
    <div v-if="tab === 'msg'" class="cjx-filter-bar">
      <div class="cjx-filter-tabs">
        <button :class="['cjx-filter-tab', { active: msgFilter === 'all' }]" @click="setFilter('all')">全部</button>
        <button :class="['cjx-filter-tab', { active: msgFilter === 'unread' }]" @click="setFilter('unread')">未读</button>
        <button :class="['cjx-filter-tab', { active: msgFilter === 'read' }]" @click="setFilter('read')">已读</button>
      </div>
      <span class="cjx-filter-count">共 {{ msgTotal }} 条</span>
    </div>

    <!-- 交互消息列表 -->
    <div v-if="tab === 'msg'" class="cjx-msg-list">
      <div v-if="msgList.length === 0" class="cjx-empty">暂无消息，去评论区互动吧～</div>
      <div v-for="n in msgList" :key="n.id" class="cjx-msg-item" :class="{ unread: !n.isRead }" @click="openNotification(n)">
        <div class="cjx-msg-avatar" :style="n.actorAvatarUrl ? `background-image:url(${n.actorAvatarUrl});background-size:cover;background-position:center;` : ''">
          <span v-if="!n.actorAvatarUrl">{{ (n.actorName || '用').substring(0, 1) }}</span>
        </div>
        <div class="cjx-msg-body">
          <div class="cjx-msg-title">
            <span class="cjx-msg-actor">{{ n.actorName || '某人' }}</span>
            <span class="cjx-msg-action">{{ typeText(n.type) }}</span>
          </div>
          <!-- 引用块：like 通知始终显示被点赞的原内容；reply 只在有 targetContent 时显示被回复的原评论 -->
          <div v-if="(n.type !== 'reply' && (n.targetContent || n.contentSnippet)) || (n.type === 'reply' && n.targetContent)" class="cjx-msg-quote">
            {{ n.targetContent || n.contentSnippet }}
          </div>
          <!-- reply 正文：无论新旧通知，只要有 contentSnippet 就一行省略显示 -->
          <div v-if="n.type === 'reply' && n.contentSnippet" class="cjx-msg-reply-body">
            {{ n.contentSnippet }}
          </div>
          <div class="cjx-msg-meta">
            <span>{{ formatTime(n.createdAt) }}</span>
            <span class="cjx-msg-hint" v-if="n.gameId">点击查看 →</span>
          </div>
        </div>
        <div v-if="!n.isRead" class="cjx-unread-dot"></div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="tab === 'msg' && msgTotal > msgSize" class="cjx-pagination">
      <button :disabled="msgPage <= 1" class="cjx-page-btn" @click="changePage(msgPage - 1)">上一页</button>
      <span class="cjx-page-info">{{ msgPage }} / {{ msgTotalPages }}</span>
      <button :disabled="msgPage >= msgTotalPages" class="cjx-page-btn" @click="changePage(msgPage + 1)">下一页</button>
    </div>

    <!-- 公告列表 -->
    <div v-else class="cjx-msg-list">
      <div v-if="annList.length === 0" class="cjx-empty">暂无公告</div>
      <div v-for="a in annList" :key="a.id" class="cjx-msg-item ann-item">
        <div class="cjx-msg-body">
          <div class="cjx-msg-title">
            <span class="cjx-msg-actor">📢 {{ a.title }}</span>
            <span v-if="a.is_top" class="cjx-popover-top">置顶</span>
          </div>
          <div class="cjx-msg-content">{{ a.content }}</div>
          <div class="cjx-msg-meta">{{ a.publish_date || a.publishDate }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, notificationAPI, announcementAPI } from '../config/supabase-local.ts'

const router = useRouter()

/** 当前选中 Tab：'msg' = 交互消息，'ann' = 官方公告 */
const tab = ref<'msg' | 'ann'>('msg')
/** 交互通知列表 */
const msgList = ref<any[]>([])
/** 官方公告列表 */
const annList = ref<any[]>([])
/** 交互消息未读数 */
const msgUnread = ref(0)
/** 公告未读数 */
const annUnread = ref(0)
/** 当前登录用户 */
const currentUser = ref<any>(null)

// 分页 + 筛选
/** 当前消息页码 */
const msgPage = ref(1)
/** 每页消息数量 */
const msgSize = ref(10)
/** 消息总条数 */
const msgTotal = ref(0)
/** 消息筛选：全部 / 未读 / 已读 */
const msgFilter = ref<'all' | 'unread' | 'read'>('all')
/** 消息总页数 */
const msgTotalPages = computed(() => Math.max(1, Math.ceil(msgTotal.value / msgSize.value)))

/**
 * 将通知类型码转为中文描述
 * @param t 通知类型：reply / like_review / like_reply
 * @returns 中文描述字符串
 */
const typeText = (t: string) => {
  if (t === 'reply') return '回复了你'
  if (t === 'like_review') return '赞了你的评论'
  if (t === 'like_reply') return '赞了你的回复'
  if (t === 'system') return '系统通知'
  return ''
}

/**
 * 时间戳相对时间格式化（刚刚 / N 分钟前 / N 小时前 / N 天前 / 日期）
 * @param t ISO 时间字符串
 * @returns 人类可读的相对时间
 */
const formatTime = (t: string) => {
  if (!t) return ''
  try {
    const d = new Date(t), now = new Date()
    const diff = (now.getTime() - d.getTime()) / 1000
    if (diff < 60) return '刚刚'
    if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
    if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
    if (diff < 7 * 86400) return Math.floor(diff / 86400) + ' 天前'
    return d.toLocaleDateString('zh-CN')
  } catch { return t }
}

/**
 * 加载当前页的交互通知列表
 * 根据 msgFilter 决定是否只拉未读/已读
 */
const loadNotifications = async () => {
  if (!currentUser.value?.id) return
  const isRead = msgFilter.value === 'all' ? undefined : (msgFilter.value === 'read')
  const res = await notificationAPI.list(currentUser.value.id, msgPage.value, msgSize.value, isRead)
  msgList.value = res.list
  msgTotal.value = res.total
}

const loadData = async () => {
  currentUser.value = authAPI.getCurrentUser()
  if (!currentUser.value?.id) {
    alert('请先登录')
    router.push('/login')
    return
  }
  const [mCount, r] = await Promise.all([
    notificationAPI.unreadCount(currentUser.value.id),
    announcementAPI.getAnnouncements(50)
  ])
  msgUnread.value = mCount
  annList.value = r.data || []
  annUnread.value = await announcementAPI.unreadCount(currentUser.value.id)
  await loadNotifications()
}

const switchTab = async (t: 'msg' | 'ann') => {
  tab.value = t
}

const setFilter = async (f: 'all' | 'unread' | 'read') => {
  msgFilter.value = f
  msgPage.value = 1
  await loadNotifications()
}

const changePage = async (p: number) => {
  if (p < 1 || p > msgTotalPages.value) return
  msgPage.value = p
  await loadNotifications()
}

const markAllRead = async () => {
  if (!currentUser.value?.id) return
  if (tab.value === 'msg') {
    await notificationAPI.markAllRead(currentUser.value.id)
    msgList.value.forEach(n => n.isRead = true)
    msgUnread.value = 0
    if (msgFilter.value === 'unread') {
      msgPage.value = 1
      await loadNotifications()
    }
  } else {
    await announcementAPI.markAllRead(currentUser.value.id)
    annUnread.value = 0
  }
}

const openNotification = async (n: any) => {
  if (!n.isRead) {
    await notificationAPI.markRead(n.id)
    n.isRead = true
    msgUnread.value = Math.max(0, msgUnread.value - 1)
  }
  if (n.gameId) router.push({ path: '/game/' + n.gameId, query: { highlight: n.targetId, hlType: n.targetType } })
}

onMounted(loadData)
</script>

<style scoped>
.cjx-msg-center {
  max-width: 720px;
  margin: 0 auto;
}

.cjx-back-bar {
  margin-bottom: 12px;
}

.cjx-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #3498db;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.15s;
}

.cjx-back-btn:hover {
  background: #eaf4fc;
  text-decoration: underline;
}

.cjx-tabs {
  display: flex;
  align-items: center;
  gap: 1rem;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 1rem;
  padding-bottom: 0;
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

.cjx-tab-badge {
  background: #e74c3c;
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.cjx-mark-all {
  margin-left: auto;
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #3498db;
  color: #3498db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.cjx-mark-all:hover { background: #ebf5fb; }

/* 筛选栏 */
.cjx-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 0 4px;
}
.cjx-filter-tabs {
  display: flex;
  gap: 8px;
}
.cjx-filter-tab {
  padding: 5px 14px;
  font-size: 13px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  transition: all 0.15s;
}
.cjx-filter-tab:hover {
  border-color: #3498db;
  color: #3498db;
}
.cjx-filter-tab.active {
  background: #3498db;
  color: #fff;
  border-color: #3498db;
}
.cjx-filter-count {
  font-size: 13px;
  color: #999;
}

/* 分页 */
.cjx-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
  padding: 12px 0;
}
.cjx-page-btn {
  padding: 6px 16px;
  font-size: 13px;
  border: 1px solid #3498db;
  background: #fff;
  color: #3498db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
}
.cjx-page-btn:hover:not(:disabled) {
  background: #3498db;
  color: #fff;
}
.cjx-page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.cjx-page-info {
  font-size: 13px;
  color: #666;
}

.cjx-empty {
  text-align: center;
  color: #999;
  padding: 60px 20px;
  font-size: 14px;
}

.cjx-msg-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: box-shadow 0.15s;
  border: 1px solid transparent;
  position: relative;
}
.cjx-msg-item:hover {
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
  border-color: #ddd;
}
.cjx-msg-item.unread {
  background: #f0f7ff;
  border-color: #c8e0f5;
}

.cjx-msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3498db;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.cjx-msg-body { flex: 1; min-width: 0; }

.cjx-msg-title {
  font-size: 14px;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.cjx-msg-actor { color: #3498db; font-weight: 600; }
.cjx-msg-action { color: #555; }

.cjx-msg-quote {
  font-size: 13px;
  color: #888;
  background: #f4f5f6;
  border-left: 3px solid #ccc;
  padding: 6px 10px;
  border-radius: 0 4px 4px 0;
  margin-bottom: 4px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cjx-msg-reply-body {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.cjx-msg-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cjx-msg-meta {
  font-size: 12px;
  color: #aaa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cjx-msg-hint { color: #3498db; }

.cjx-unread-dot {
  width: 8px;
  height: 8px;
  background: #e74c3c;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 14px;
}

.ann-item .cjx-msg-avatar { background: #e67e22; }

.cjx-popover-top {
  background: #e74c3c;
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
}
</style>

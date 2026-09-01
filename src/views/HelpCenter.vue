<template>
  <Layout>
    <div class="cjx-help-page">
      <!-- 页面标题 -->
      <div class="cjx-page-header">
        <h1>帮助中心</h1>
        <p>常见问题解答与使用指南</p>
      </div>

      <!-- 搜索框 -->
      <div class="cjx-search-bar">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索帮助内容..."
          class="cjx-search-input"
        />
      </div>

      <!-- 智能助手 -->
      <div class="cjx-assistant-card">
        <div class="cjx-assistant-header">
          <div class="cjx-assistant-avatar">🤖</div>
          <div class="cjx-assistant-info">
            <h3>智能助手</h3>
            <span class="cjx-assistant-status">
              <span class="cjx-status-dot"></span>
              在线 · 自动搜索知识库答疑
            </span>
          </div>
          <button class="cjx-clear-btn" @click="resetChat" title="清空对话">
            <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
              <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
            </svg>
          </button>
        </div>

        <!-- 对话区域 -->
        <div class="cjx-chat-messages" ref="chatBoxRef">
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            :class="['cjx-chat-message', msg.role]"
          >
            <div class="cjx-msg-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="cjx-msg-bubble" v-html="formatMessage(msg.content)"></div>
          </div>

          <!-- 打字指示器 -->
          <div v-if="isTyping" class="cjx-chat-message assistant">
            <div class="cjx-msg-avatar">🤖</div>
            <div class="cjx-msg-bubble">
              <div class="cjx-typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 推荐问题 -->
        <div class="cjx-quick-prompts" v-if="messages.length <= 1">
          <span
            v-for="prompt in quickPrompts"
            :key="prompt"
            class="cjx-prompt-chip"
            @click="sendMessage(prompt)"
          >{{ prompt }}</span>
        </div>

        <!-- 输入区域 -->
        <div class="cjx-chat-input">
          <input
            v-model="userInput"
            type="text"
            placeholder="输入您的问题，回车发送..."
            @keyup.enter="sendMessage()"
            class="cjx-chat-textarea"
            :disabled="isTyping"
          />
          <button
            class="cjx-send-btn"
            @click="sendMessage()"
            :disabled="isTyping || !userInput.trim()"
          >
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 帮助分类 -->
      <div class="cjx-help-content">
        <div
          class="cjx-faq-section"
          v-for="section in filteredSections"
          :key="section.title"
        >
          <h2 class="cjx-section-title">
            <span>{{ section.icon }}</span>
            {{ section.title }}
          </h2>
          <div class="cjx-faq-list">
            <div
              class="cjx-faq-item"
              v-for="(faq, idx) in section.items"
              :key="idx"
              @click="toggleFaq(`${section.title}-${idx}`)"
            >
              <div class="cjx-faq-question">
                <span>{{ faq.question }}</span>
                <svg
                  :class="{ expanded: openFaq === `${section.title}-${idx}` }"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                </svg>
              </div>
              <div
                class="cjx-faq-answer"
                v-show="openFaq === `${section.title}-${idx}`"
              >
                {{ faq.answer }}
              </div>
            </div>
          </div>
        </div>

        <!-- 联系我们 -->
        <div class="cjx-contact-section">
          <h2 class="cjx-section-title">
            <span>📞</span>
            联系我们
          </h2>
          <div class="cjx-contact-grid">
            <div class="cjx-contact-card">
              <div class="cjx-contact-icon">💬</div>
              <h3>官方QQ群</h3>
              <p>807662430</p>
              <span class="cjx-contact-desc">欢迎交流与反馈</span>
            </div>
            <div class="cjx-contact-card">
              <div class="cjx-contact-icon">⏰</div>
              <h3>服务时间</h3>
              <p>每日 9:00 - 22:00</p>
              <span class="cjx-contact-desc">节假日正常服务</span>
            </div>
            <div class="cjx-contact-card">
              <div class="cjx-contact-icon">📝</div>
              <h3>问题反馈</h3>
              <p>在群内@管理员</p>
              <span class="cjx-contact-desc">我们会尽快处理</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import Layout from '../components/Layout.vue'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

interface FAQItem {
  question: string
  answer: string
  keywords?: string[]
}

interface FAQSection {
  title: string
  icon: string
  items: FAQItem[]
}

const searchQuery = ref('')
const openFaq = ref('')
const userInput = ref('')
const isTyping = ref(false)
const chatBoxRef = ref<HTMLElement | null>(null)

const messages = ref<Message[]>([
  {
    role: 'assistant',
    content:
      '你好！我是 SteamPY 智能助手 🤖\n我可以帮你解答平台使用、交易流程、账号安全等问题。试试问我：'
  }
])

const quickPrompts = [
  '怎么注册账号？',
  '礼物代购怎么操作？',
  'CDKey 怎么激活？',
  '充值没到账怎么办？',
  '平台安全吗？'
]

// ========== 知识库 ==========
const faqSections = ref<FAQSection[]>([
  {
    title: '账号相关',
    icon: '👤',
    items: [
      {
        question: '如何注册账号？',
        answer:
          '点击登录页面的「注册」按钮，填写用户名、密码、邮箱等信息即可完成注册。注册成功后需前往邮箱验证账号。',
        keywords: ['注册', '账号', '新建', '创建', 'signup', 'register']
      },
      {
        question: '忘记密码怎么办？',
        answer:
          '在登录页面点击「忘记密码」，通过注册时填写的邮箱接收重置密码链接，设置新密码即可。',
        keywords: ['忘记密码', '重置', '修改密码', '找回密码', 'password']
      },
      {
        question: '账号被封禁了怎么办？',
        answer:
          '如账号被封禁，请联系官方QQ群管理员了解原因，提供相关说明后我们会审核处理。',
        keywords: ['封禁', '封号', 'ban', '已封禁', '不能登录']
      },
      {
        question: '如何登录平台？',
        answer:
          '在登录页面输入用户名和密码，点击登录按钮即可。如果账号被封禁将无法登录。登录后可以使用平台的全部功能。',
        keywords: ['登录', '登陆', 'login', 'sign in', '进入']
      },
      {
        question: '如何退出登录？',
        answer:
          '点击右上角的用户头像，在下拉菜单中选择「退出登录」即可安全退出。',
        keywords: ['退出', '登出', 'logout', 'sign out']
      }
    ]
  },
  {
    title: '礼物代购',
    icon: '🎁',
    items: [
      {
        question: '什么是礼物代购？',
        answer:
          '礼物代购是通过Steam好友赠送功能购买游戏的方式。卖家将游戏以礼物形式发送到您的Steam账户，您收到后即可激活。',
        keywords: ['礼物', '代购', 'gift', '好友赠送']
      },
      {
        question: '购买流程是怎样的？',
        answer:
          '选择游戏并下单支付 → 提供Steam好友代码 → 卖家添加您为好友并发送礼物 → 在Steam客户端接收礼物。',
        keywords: ['流程', '怎么买', '步骤', 'how to buy', '下单', '购买']
      },
      {
        question: '国区礼物可以在其他地区使用吗？',
        answer: '国区礼物仅限国区Steam账户使用。如果您的账号属于其他地区，请谨慎购买。',
        keywords: ['国区', '区域', '地区', '港区', '美区', '跨区']
      }
    ]
  },
  {
    title: '余额购',
    icon: '💰',
    items: [
      {
        question: '什么是余额购？',
        answer:
          '余额购是使用Steam账户钱包余额购买游戏的方式。平台会以更低的价格为您充值Steam余额，然后用于购买游戏。',
        keywords: ['余额', '余额购', 'balance', '钱包充值', 'steam余额']
      },
      {
        question: '充值需要多长时间？',
        answer: '通常充值会在几分钟内完成，高峰时段可能略有延迟。如超过30分钟未到账，请联系客服。',
        keywords: ['充值', '到账', '时间', '多久', '慢', '没到账']
      },
      {
        question: '充值失败怎么办？',
        answer:
          '充值失败的订单会自动退款到您的平台账户余额，您可以重新下单或联系客服协助处理。',
        keywords: ['充值失败', '失败', '退款', 'error', '无法充值']
      }
    ]
  },
  {
    title: 'CDKey相关',
    icon: '🔑',
    items: [
      {
        question: 'CDKey如何使用？',
        answer:
          '登录Steam客户端 → 点击左上角「游戏」菜单 → 选择「在Steam上激活产品」→ 输入CDKey即可激活游戏。',
        keywords: ['cdkey', 'cdk', '激活', '使用', 'key', '序列号']
      },
      {
        question: 'CDKey激活失败怎么办？',
        answer:
          '请检查CDKey是否输入正确（注意区分大小写和符号）。如果确认无误但仍然失败，可能是CDKey已被使用或区域不匹配，请联系客服。',
        keywords: ['激活失败', 'cdk失败', 'cdkey不能用', '已使用', '无效']
      },
      {
        question: '在哪里查看我的CDKey？',
        answer:
          '在「买家中心」可以查看您购买过的所有订单，包含CDKey的订单会显示对应的CDKey码，支持一键复制。',
        keywords: ['查看cdk', '查看cdkey', '我的cdk', '复制cdk', '订单']
      }
    ]
  },
  {
    title: '安全与交易',
    icon: '🔒',
    items: [
      {
        question: '平台交易安全吗？',
        answer:
          '平台使用全自动交易流程，账户信息直接对接Steam官方接口，安全可靠。每笔交易都有记录可查，避免交易纠纷。',
        keywords: ['安全', '靠谱', '可靠', '放心', '骗局', '骗子', 'safe']
      },
      {
        question: '如何保护账号安全？',
        answer:
          '建议启用Steam双重验证（2FA），不要向任何人透露您的账号密码，不要在公共设备上记住登录状态。',
        keywords: ['保护', '防盗', '安全设置', '2fa', '双重验证', '密码']
      },
      {
        question: '遇到诈骗怎么办？',
        answer:
          '如发现可疑行为或遭遇诈骗，请立即联系官方QQ群管理员，并保留相关聊天记录和交易凭证，我们会协助处理。',
        keywords: ['诈骗', '被骗', '举报', '投诉', 'fraud', 'scam']
      }
    ]
  },
  {
    title: '其他问题',
    icon: '❓',
    items: [
      {
        question: '如何联系客服？',
        answer: '可以加入官方QQ群：807662430，群内有管理员值班。服务时间：每日 9:00 - 22:00。',
        keywords: ['客服', '联系', 'qq群', '怎么找', '人工', 'support']
      },
      {
        question: '平台的服务时间？',
        answer: '平台服务时间为每日 9:00 - 22:00，节假日正常服务。',
        keywords: ['时间', '营业', '几点', '上班', '服务时间', '开放']
      },
      {
        question: '平台支持哪些支付方式？',
        answer: '平台支持常用的在线支付方式，具体以页面显示为准。充值和购买操作均在平台内安全完成。',
        keywords: ['支付', '付款', '微信', '支付宝', '银行卡', 'pay']
      }
    ]
  }
])

// 展开全部条目为便于搜索的列表
const allKnowledge = computed(() => {
  const list: { section: string; question: string; answer: string; keywords: string[] }[] = []
  faqSections.value.forEach(sec => {
    sec.items.forEach(item => {
      const kw = [
        ...(item.keywords || []),
        sec.title,
        item.question,
        item.question.replace(/[？?。.!！,\s]/g, '')
      ]
      list.push({
        section: sec.title,
        question: item.question,
        answer: item.answer,
        keywords: kw.map(k => k.toLowerCase())
      })
    })
  })
  return list
})

// ========== 搜索匹配引擎 ==========
interface MatchResult {
  item: (typeof allKnowledge.value)[number]
  score: number
  matchedKeyword: string
}

function searchKnowledge(query: string): MatchResult[] {
  const q = query.toLowerCase().trim()
  if (!q) return []

  const results: MatchResult[] = []
  const queryChars = q.replace(/\s/g, '')

  for (const item of allKnowledge.value) {
    let score = 0
    let matchedKeyword = ''

    // 精确匹配关键词
    for (const kw of item.keywords) {
      if (kw === q) {
        score += 100
        matchedKeyword = kw
      } else if (kw.includes(q) || q.includes(kw)) {
        score += 30
        if (!matchedKeyword) matchedKeyword = kw
      }
    }

    // 字符包含匹配（处理错别字和模糊查询）
    if (score === 0 && queryChars.length >= 2) {
      let charMatches = 0
      for (const ch of queryChars) {
        if (item.question.includes(ch) || item.answer.includes(ch)) {
          charMatches++
        }
      }
      const ratio = charMatches / queryChars.length
      if (ratio >= 0.6) {
        score += Math.round(ratio * 25)
      }
    }

    if (score > 0) {
      results.push({ item, score, matchedKeyword })
    }
  }

  results.sort((a, b) => b.score - a.score)
  return results
}

// ========== 意图识别 + 回复生成 ==========
function generateReply(query: string): string {
  const results = searchKnowledge(query)

  if (results.length === 0) {
    return `抱歉，我暂时没找到与 "${query}" 相关的内容 😅\n\n建议：\n1. 换个关键词试试，比如"注册"、"充值"、"CDKey"\n2. 查看下方的常见问题分类\n3. 加入官方QQ群 807662430 联系人工客服`
  }

  const top = results.slice(0, 3)
  let reply = `根据搜索结果，为您找到 ${results.length} 条相关内容：\n\n`

  top.forEach((r, i) => {
    reply += `**${i + 1}. ${r.item.question}**\n${r.item.answer}\n\n`
  })

  if (results.length > 3) {
    reply += `还有 ${results.length - 3} 条相关结果，您可以调整关键词继续搜索 🔍`
  }

  return reply.trim()
}

// ========== 消息处理 ==========
function formatMessage(text: string): string {
  // 支持简单 markdown：**粗体** 和 \n 换行
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
}

async function sendMessage(text?: string) {
  const content = (text ?? userInput.value).trim()
  if (!content || isTyping.value) return

  messages.value.push({ role: 'user', content })
  userInput.value = ''
  isTyping.value = true
  scrollToBottom()

  // 模拟思考延迟
  await new Promise(r => setTimeout(r, 600 + Math.random() * 500))

  const reply = generateReply(content)
  messages.value.push({ role: 'assistant', content: reply })
  isTyping.value = false
  scrollToBottom()
}

function resetChat() {
  messages.value = [
    {
      role: 'assistant',
      content:
        '对话已清空！有什么我可以帮你的吗？试试问我关于平台使用、交易流程、账号安全等问题 🤖'
    }
  ]
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBoxRef.value) {
      chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
    }
  })
}

// ========== FAQ 展示 ==========
const toggleFaq = (key: string) => {
  openFaq.value = openFaq.value === key ? '' : key
}

const filteredSections = computed(() => {
  if (!searchQuery.value.trim()) {
    return faqSections.value
  }
  const query = searchQuery.value.toLowerCase()
  return faqSections.value
    .map(section => ({
      ...section,
      items: section.items.filter(
        faq =>
          faq.question.toLowerCase().includes(query) ||
          faq.answer.toLowerCase().includes(query)
      )
    }))
    .filter(section => section.items.length > 0)
})
</script>

<style scoped>
.cjx-help-page {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

/* 页面标题 */
.cjx-page-header {
  text-align: center;
  padding: 2rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
  margin-bottom: 1.5rem;
}

.cjx-page-header h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.cjx-page-header p {
  font-size: 1rem;
  opacity: 0.9;
}

/* 搜索框 */
.cjx-search-bar {
  margin-bottom: 1.5rem;
}

.cjx-search-input {
  width: 100%;
  padding: 0.85rem 1.2rem;
  font-size: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.cjx-search-input:focus {
  outline: none;
  border-color: #3498db;
}

/* ========== 智能助手卡片 ========== */
.cjx-assistant-card {
  background: linear-gradient(180deg, #f8f9ff 0%, #ffffff 100%);
  border: 1px solid #e0e4f0;
  border-radius: 12px;
  padding: 0;
  margin-bottom: 2rem;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.08);
}

.cjx-assistant-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.cjx-assistant-avatar {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.cjx-assistant-info {
  flex: 1;
}

.cjx-assistant-info h3 {
  font-size: 1rem;
  margin: 0;
  font-weight: 600;
}

.cjx-assistant-status {
  font-size: 0.75rem;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}

.cjx-status-dot {
  width: 8px;
  height: 8px;
  background: #2ecc71;
  border-radius: 50%;
  display: inline-block;
}

.cjx-clear-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.cjx-clear-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* 对话区域 */
.cjx-chat-messages {
  padding: 1rem 1.25rem;
  height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  background: #fafbff;
}

.cjx-chat-message {
  display: flex;
  gap: 0.5rem;
  max-width: 85%;
}

.cjx-chat-message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.cjx-msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  background: #f0f0f0;
}

.cjx-chat-message.user .cjx-msg-avatar {
  background: #3498db;
}

.cjx-chat-message.assistant .cjx-msg-avatar {
  background: #667eea;
}

.cjx-msg-bubble {
  padding: 0.7rem 1rem;
  border-radius: 12px;
  font-size: 0.9rem;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.cjx-chat-message.user .cjx-msg-bubble {
  background: #3498db;
  color: white;
  border-bottom-right-radius: 4px;
}

.cjx-chat-message.assistant .cjx-msg-bubble {
  background: white;
  color: #333;
  border: 1px solid #e8ecf4;
  border-bottom-left-radius: 4px;
}

/* 打字指示器 */
.cjx-typing {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.cjx-typing span {
  width: 8px;
  height: 8px;
  background: #bbb;
  border-radius: 50%;
  animation: cjx-bounce 1.2s infinite ease-in-out;
}

.cjx-typing span:nth-child(2) {
  animation-delay: 0.15s;
}
.cjx-typing span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes cjx-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* 推荐问题 */
.cjx-quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: #fafbff;
  border-top: 1px solid #eef0f8;
}

.cjx-prompt-chip {
  padding: 0.4rem 0.85rem;
  background: white;
  border: 1px solid #d8dff0;
  border-radius: 20px;
  font-size: 0.8rem;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}

.cjx-prompt-chip:hover {
  background: #667eea;
  border-color: #667eea;
  color: white;
  transform: translateY(-1px);
}

/* 输入区域 */
.cjx-chat-input {
  display: flex;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-top: 1px solid #eef0f8;
  background: white;
}

.cjx-chat-textarea {
  flex: 1;
  padding: 0.65rem 1rem;
  border: 1px solid #dde1ed;
  border-radius: 20px;
  font-size: 0.9rem;
  outline: none;
  transition: border-color 0.2s;
}

.cjx-chat-textarea:focus {
  border-color: #667eea;
}

.cjx-chat-textarea:disabled {
  background: #f5f5f5;
}

.cjx-send-btn {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, opacity 0.2s;
  flex-shrink: 0;
}

.cjx-send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.cjx-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ========== 原有 FAQ 区域 ========== */
.cjx-help-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.cjx-section-title {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f1f3f5;
}

.cjx-section-title span {
  font-size: 1.3rem;
}

.cjx-faq-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.cjx-faq-item {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
  cursor: pointer;
}

.cjx-faq-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.cjx-faq-question {
  padding: 1rem 1.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
  font-weight: 500;
  color: #2c3e50;
}

.cjx-faq-question svg {
  width: 20px;
  height: 20px;
  color: #95a5a6;
  transition: transform 0.2s;
  flex-shrink: 0;
}

.cjx-faq-question svg.expanded {
  transform: rotate(180deg);
}

.cjx-faq-answer {
  padding: 1rem 1.25rem;
  color: #555;
  line-height: 1.7;
  background: white;
  border-top: 1px solid #e9ecef;
}

/* 联系我们 */
.cjx-contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.cjx-contact-card {
  background: #fafafa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.cjx-contact-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cjx-contact-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.cjx-contact-card h3 {
  font-size: 1rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.cjx-contact-card p {
  font-size: 1.1rem;
  font-weight: bold;
  color: #3498db;
  margin-bottom: 0.25rem;
}

.cjx-contact-desc {
  font-size: 0.8rem;
  color: #999;
}

@media (max-width: 768px) {
  .cjx-help-page {
    padding: 16px;
  }

  .cjx-page-header {
    padding: 1.5rem 1rem;
  }

  .cjx-page-header h1 {
    font-size: 1.5rem;
  }

  .cjx-chat-messages {
    height: 250px;
  }

  .cjx-chat-message {
    max-width: 92%;
  }
}
</style>

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// ============ 全局 axios 拦截器：自动注入管理员身份 ============
// 所有 .vue 文件都 import axios from 'axios'（默认实例）
// 所以在这里加拦截器，每次请求 /api/admin/** 前自动从 sessionStorage 读 X-Admin-Id
// 解决 AdminLayout.onMounted 与子组件 AdminOrders.onMounted 的竞态
axios.interceptors.request.use((config) => {
  if (config.url && config.url.startsWith('/api/admin')) {
    // 管理员登录状态独立存储在 steampy_admin_user，优先取它；
    // 兼容旧流程（管理员身份存在 steampy_user 里）时再回退
    const raw = sessionStorage.getItem('steampy_admin_user') || sessionStorage.getItem('steampy_user')
    if (raw) {
      try {
        const user = JSON.parse(raw)
        if (user?.id) {
          config.headers = config.headers || {}
          config.headers['X-Admin-Id'] = user.id
        }
      } catch { /* 忽略坏 JSON */ }
    }
  }
  return config
})

// 引入Vant组件和样式
import {
  Button,
  Field,
  CellGroup,
  Tabbar,
  TabbarItem,
  NavBar,
  Icon,
  Loading,
  Empty,
  Tag,
  Badge,
  Divider,
  Toast,
  Dialog,
  ActionSheet,
  Popup,
  SwipeCell,
  Checkbox,
  CheckboxGroup,
  Radio,
  RadioGroup,
  Switch,
  Slider,
  Stepper,
  Progress,
  Circle,
  NoticeBar,
  Image as VanImage
} from 'vant'
import 'vant/lib/index.css'

// 全局替换浏览器原生 alert → Vant Dialog（好看 + 不阻塞主线程）
import { patchBrowser } from './utils/ui'
patchBrowser()

const app = createApp(App)

// 注册Vant组件
app.use(Button)
app.use(Field)
app.use(CellGroup)
app.use(Tabbar)
app.use(TabbarItem)
app.use(NavBar)
app.use(Icon)
app.use(Loading)
app.use(Empty)
app.use(Tag)
app.use(Badge)
app.use(Divider)
app.use(Toast)
app.use(Dialog)
app.use(ActionSheet)
app.use(Popup)
app.use(SwipeCell)
app.use(Checkbox)
app.use(CheckboxGroup)
app.use(Radio)
app.use(RadioGroup)
app.use(Switch)
app.use(Slider)
app.use(Stepper)
app.use(Progress)
app.use(Circle)
app.use(NoticeBar)
app.use(VanImage)

// ===== 全局图片懒加载指令 v-lazy =====
// 用法：<img v-lazy="图片URL" /> 或 <img :src="xxx" v-lazy />
// 浏览器原生 IntersectionObserver + data-src 回退
const lazyImages = new WeakSet<HTMLImageElement>()

const io = new IntersectionObserver((entries) => {
  for (const entry of entries) {
    if (entry.isIntersecting) {
      const img = entry.target as HTMLImageElement
      // 从 data-src 取真实 URL，避免直接覆盖 src 导致已加载的图被重置
      const real = img.dataset.src || img.dataset.lazySrc
      if (real) img.src = real
      io.unobserve(img)
    }
  }
}, { rootMargin: '200px' /* 提前 200px 预加载 */, threshold: 0.01 })

app.directive('lazy', {
  mounted(el: HTMLImageElement, binding) {
    if (!(el instanceof HTMLImageElement)) return
    // 如果传了值，先用占位图或留空
    if (binding.value) {
      el.dataset.src = binding.value
      // 给一个占位避免布局塌陷
      if (!el.src) el.src = 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=='
    }
    lazyImages.add(el)
    io.observe(el)
  },
  beforeUnmount(el: HTMLImageElement) {
    io.unobserve(el)
    lazyImages.delete(el)
  }
})

// ===== 全局自动标记：扫描 body 内所有 <img loading> 补充 =====
// 原生 loading="lazy" 浏览器自己就会处理，不需要 directive
// 这里通过 Vue MutationObserver 兜底，给新增节点的 <img> 补 loading="lazy"
// （不阻塞首屏，只处理动态渲染出来的列表图片）
if ('loading' in HTMLImageElement.prototype) {
  const markLazy = (root: ParentNode = document) => {
    const imgs = (root as ParentNode).querySelectorAll?.('img:not([loading])')
    imgs?.forEach((img) => {
      // 首页首屏（viewport 内）的图不要延迟，其他全部 lazy
      const r = img.getBoundingClientRect()
      if (r.top > window.innerHeight) {
        img.setAttribute('loading', 'lazy')
      }
    })
  }
  // DOMContentLoaded 之后扫描一次 + 观察后续 DOM 变化
  const onReady = () => markLazy(document)
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', onReady)
  } else {
    onReady()
  }
  const mo = new MutationObserver((mutations) => {
    mutations.forEach((m) => m.addedNodes.forEach((n) => {
      if (n.nodeType === Node.ELEMENT_NODE) markLazy(n as Element)
    }))
  })
  document.addEventListener('DOMContentLoaded', () => {
    mo.observe(document.body, { childList: true, subtree: true })
  })
}

app.use(router)
app.mount('#app')

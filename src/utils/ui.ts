/**
 * 全局提示框工具 — 替换浏览器原生丑陋的 alert/confirm
 *
 * Vant 3.x API（项目用 3.6.16）：
 *   Toast('msg') / Toast({ message, type, duration })
 *   Toast.clear()
 *   Dialog.alert({ title, message })       → Promise<void>
 *   Dialog.confirm({ title, message })     → Promise<boolean>
 */
import { Toast, Dialog } from 'vant'

const BRAND = '#4a6cf7'

/** Toast 轻提示（自动消失） */
export function toast(msg: string, type: 'success' | 'fail' | 'info' | 'loading' = 'info', duration = 2000) {
  Toast({ message: msg, type, duration })
}
export const toastSuccess = (msg: string) => toast(msg, 'success')
export const toastFail    = (msg: string) => toast(msg, 'fail')

/** 弹框（替代 alert）— 单按钮，fire-and-forget */
export function alert(message: string, title?: string) {
  Dialog.alert({
    title: title || '提示',
    message,
    confirmButtonText: '确定',
    confirmButtonColor: BRAND,
  }).catch(() => {})
}

/** 确认框（替代 confirm）— 双按钮，await 后拿到 boolean */
export function confirm(message: string, title?: string): Promise<boolean> {
  return Dialog.confirm({
    title: title || '提示',
    message,
    confirmButtonText: '确定',
    confirmButtonColor: BRAND,
    cancelButtonText: '取消',
  })
    .then(() => true)
    .catch(() => false)
}

/** 全屏 loading */
export const loading = {
  show(msg = '加载中...') { Toast({ message: msg, type: 'loading', duration: 0, forbidClick: true }) },
  hide()                 { Toast.clear() },
}

/**
 * Monkey patch window.alert + window.confirm
 * alert: fire-and-forget — 90+ 处现有调用自动生效
 * confirm: 返回 Promise<boolean> — 调用方需 `await confirm(...)`
 */
export function patchBrowser() {
  window.alert = ((msg: any, title?: string) => {
    const text = typeof msg === 'string' ? msg : String(msg ?? '')
    if (!text) return
    alert(text, title)
  }) as typeof window.alert

  ;(window as any).confirm = ((msg: any, title?: string): Promise<boolean> => {
    const text = typeof msg === 'string' ? msg : String(msg ?? '')
    return confirm(text, title)
  })
}

export default { alert, confirm, toast, toastSuccess, toastFail, loading, patchBrowser }

/**
 * 全局提示框工具 — 替换浏览器原生丑陋的 alert
 *
 * 用法：
 *   alert('操作成功')                                    // 弹框（已 patch window，直接用）
 *   toast('保存成功', 'success')                         // 轻提示（自动消失）
 *   const ok = await confirm('确定删除？')               // 确认框（Promise<boolean>）
 */
import { showToast, showDialog, showConfirmDialog, closeToast } from 'vant'

/** Toast 轻提示（自动消失） */
export function toast(msg: string, type: 'success' | 'fail' | 'info' | 'loading' = 'info', duration = 2000) {
  showToast({ message: msg, type, duration })
}
export const toastSuccess = (msg: string) => toast(msg, 'success')
export const toastFail    = (msg: string) => toast(msg, 'fail')

/** 弹框（替代 alert）— 单按钮，异步但不关心返回值 */
export function alert(message: string, title?: string) {
  showDialog({
    title: title || '提示',
    message,
    confirmButtonText: '确定',
    confirmButtonColor: '#4a6cf7',
  }).catch(() => {})
}

/** 确认框（替代 confirm）— 双按钮，await 后拿到 boolean */
export function confirm(message: string, title?: string): Promise<boolean> {
  return showConfirmDialog({
    title: title || '提示',
    message,
    confirmButtonText: '确定',
    confirmButtonColor: '#4a6cf7',
    cancelButtonText: '取消',
  })
    .then(() => true)
    .catch(() => false)
}

/** 全屏 loading */
export const loading = {
  show(msg = '加载中...') { showToast({ message: msg, type: 'loading', duration: 0, forbidClick: true }) },
  hide()                 { closeToast() },
}

/**
 * Monkey patch window.alert + window.confirm
 * alert: fire-and-forget（不关心返回值）— 90+ 处现有调用自动生效
 * confirm: 返回 Promise<boolean>，调用方需 await（项目里已手动改好）
 */
export function patchBrowser() {
  // alert — fire-and-forget，完全兼容原生
  window.alert = (msg: any, title?: string) => {
    const text = typeof msg === 'string' ? msg : String(msg ?? '')
    if (!text) return
    alert(text, title)
  } as typeof window.alert

  // confirm — 返回 Promise<boolean>，调用方需 `await confirm(...)`
  ;(window as any).confirm = (msg: any, title?: string): Promise<boolean> => {
    const text = typeof msg === 'string' ? msg : String(msg ?? '')
    return confirm(text, title)
  }
}

export default { alert, confirm, toast, toastSuccess, toastFail, loading, patchBrowser }

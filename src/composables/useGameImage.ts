/**
 * 统一的图片路径解析工具
 * 替代 GameCard.vue / CDKeyMarket.vue / BalancePurchaseDetail.vue 中 3 处重复的 getImageUrl 逻辑
 *
 * 数据库 image 字段可能的格式：
 *   1. http(s)://...               — Steam CDN 在线图，直接用
 *   2. ../picture/games/xxx.jpg     — 本地图（迁移后新格式）
 *   3. ../picture/xxx.jpg           — 本地图（旧格式，兼容）
 *   4. 空 / null                    — 兜底图
 */

/** 兜底图：当 image 字段为空或加载失败时显示 */
export const FALLBACK_IMAGE = '/picture/games/安魂曲.jpg'

/** 本地图应该放的子目录 */
export const LOCAL_GAMES_SUBDIR = '/picture/games/'

/**
 * 解析 image 字段为浏览器可直接访问的 URL
 * @param image DB 中的 image 字段值
 * @returns 可用于 <img :src> 的 URL
 */
export function getImageUrl(image: string | null | undefined): string {
  if (!image) return FALLBACK_IMAGE

  // 1. Steam CDN 在线图
  if (image.startsWith('http://') || image.startsWith('https://')) {
    // 偶尔 Steam header URL 缺 .jpg 后缀，补一下
    if (image.includes('/header') && !/\.(jpg|jpeg|png|gif|webp|avif)$/i.test(image)) {
      return image + '.jpg'
    }
    return image
  }

  // 2. 本地图：去掉 ../ 前缀，保留 picture/ 后的完整路径
  // ../picture/games/xxx.jpg  →  /picture/games/xxx.jpg
  // ../picture/xxx.jpg        →  /picture/xxx.jpg  (兼容旧格式)
  let normalized = image
    .replace(/^\.\./, '')     // 去掉开头 ../
    .replace(/^\/+/, '/')     // 确保单斜杠开头

  return normalized
}

/**
 * 图片加载失败兜底回调，绑定 <img> 的 @error 事件
 * 用法：<img :src="getImageUrl(x.image)" @error="onImgError" />
 *
 * 或绑定 ref：
 *   const imgRef = ref()
 *   imgRef.value.onerror = () => onImgError(imgRef.value)
 */
export function onImgError(e: Event | HTMLImageElement): void {
  const el = e instanceof HTMLImageElement ? e : (e.target as HTMLImageElement)
  if (!el) return
  // 防止兜底图自己也挂了 → 死循环
  if (el.src && !el.src.includes('picture/games/安魂曲')) {
    el.src = FALLBACK_IMAGE
    el.onerror = null
  }
}

/**
 * 数据库 image 字段写入时的反向转换（可选）
 * 把浏览器路径转回 DB 存储格式
 */
export function toDbImage(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // /picture/xxx.jpg → ../picture/xxx.jpg
  return url.replace(/^\//, '../')
}

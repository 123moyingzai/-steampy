// SteamPY 后端 API - 本地 Spring Boot
import axios from 'axios'

const API_BASE = '/api'

// 通用请求
async function apiRequest<T = any>(
  path: string,
  method: string = 'GET',
  body?: any
): Promise<T> {
  const url = `${API_BASE}${path}`
  try {
    const res = await axios({
      method,
      url,
      data: body,
      headers: { 'Content-Type': 'application/json' }
    })
    // Spring Boot 返回 { code, message, data }
    const payload = res.data
    if (payload && payload.code === 200) {
      return payload.data as T
    }
    throw new Error(payload?.message || `HTTP ${res.status}`)
  } catch (err: any) {
    if (err.response?.data?.message) throw new Error(err.response.data.message)
    if (err.message) throw err
    throw new Error('请求失败')
  }
}

interface UserData {
  username: string
  password?: string
  phone?: string
  nickname?: string
  user_type?: string
}

interface ApiResponse<T = any> {
  data?: T
  error?: string
}

// ========== 认证 ==========
export const authAPI = {
  async register(userData: UserData): Promise<ApiResponse<User>> {
    try {
      const data = await apiRequest<User>('/auth/register', 'POST', userData)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async login(username: string, password: string): Promise<ApiResponse<User>> {
    try {
      const res = await apiRequest<{ user: User }>('/auth/login', 'POST', { username, password })
      const user = res.user
      // 存入 sessionStorage
      sessionStorage.setItem('steampy_user', JSON.stringify(user))
      return { data: user }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  getCurrentUser(): User | null {
    const u = sessionStorage.getItem('steampy_user')
    return u ? JSON.parse(u) : null
  },

  logout(): void {
    sessionStorage.removeItem('steampy_user')
  },

  async updateUser(userId: string, updateData: Partial<User>): Promise<ApiResponse<User>> {
    try {
      const data = await apiRequest<User>(`/auth/user/${userId}`, 'PUT', updateData)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  }
}

// ========== 游戏 ==========
export const gameAPI = {
  async getGames(options: Record<string, any> = {}): Promise<ApiResponse<Game[]>> {
    try {
      const data = await apiRequest<Game[]>('/games')
      return { data }
    } catch (e: any) {
      // 兜底从本地 JSON 加载
      const res = await axios.get('/cdk_games.json')
      const d = res.data
      const arr = Array.isArray(d) ? d : [...(d.preSaleItems || []), ...(d.gameItems || [])]
      return { data: arr as Game[] }
    }
  },

  async getGameById(gameId: number | string): Promise<ApiResponse<Game | null>> {
    try {
      const data = await apiRequest<Game>(`/games/${gameId}`)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  }
}

// ========== 公告 ==========
interface Announcement {
  id: number
  title: string
  content: string
  publish_date: string
  is_top: boolean
  is_active: boolean
}

export const announcementAPI = {
  async getAnnouncements(limit: number = 10): Promise<ApiResponse<Announcement[]>> {
    try {
      const data = await apiRequest<any[]>('/announcements')
      // 把 snake_case 转成原有前端期望的字段名
      const mapped: Announcement[] = data.map((a: any) => ({
        id: a.id,
        title: a.title,
        content: a.content,
        publish_date: a.publishDate,
        is_top: a.isTop,
        is_active: a.isActive
      }))
      return { data: mapped }
    } catch (e: any) {
      return {
        data: [{
          id: 1,
          title: '欢迎使用 SteamPY 平台',
          content: '这是一个安全可靠的 Steam 游戏交易平台',
          publish_date: new Date().toISOString(),
          is_top: true,
          is_active: true
        }]
      }
    }
  }
}

// ========== 订单 ==========
interface OrderData {
  buyer_id: string | number
  game_id?: string | number
  game_name: string
  game_image?: string
  price: string | number
  quantity?: number
  total_amount?: string | number
  total_price?: string | number
  order_type?: string
  cdkey?: string
}

export const orderAPI = {
  async createOrder(orderData: OrderData, urlOverride?: string): Promise<ApiResponse<Order>> {
    try {
      // Jackson SNAKE_CASE 模式：直接发 snake_case 字段名
      const body: any = {
        buyer_id: String(orderData.buyer_id),
        game_id: orderData.game_id ? Number(orderData.game_id) : null,
        game_name: orderData.game_name,
        game_image: orderData.game_image || '',
        price: Number(orderData.price),
        quantity: orderData.quantity || 1,
        total_price: Number(orderData.total_amount || orderData.total_price || orderData.price),
        delivery_method: (orderData as any).delivery_method || 'cdkey',
        version: (orderData as any).version || '标准版',
        cdkey: orderData.cdkey || '',
        listing_id: (orderData as any).listing_id || null,
        seller_id: (orderData as any).seller_id || null,
        status: (orderData as any).status || 'completed',
        order_type: orderData.order_type || 'cdkey',
        payment_method: (orderData as any).payment_method || null
      }
      const data = await apiRequest<any>(urlOverride || '/orders', 'POST', body)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async getUserOrders(userId: string | number): Promise<ApiResponse<Order[]>> {
    try {
      const data = await apiRequest<any[]>(`/orders/user/${userId}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  },

  async getOrders(userId: string | number): Promise<ApiResponse<Order[]>> {
    return this.getUserOrders(userId)
  },

  async getSellerOrders(sellerId: string | number): Promise<ApiResponse<Order[]>> {
    try {
      const data = await apiRequest<any>(`/orders/seller/${sellerId}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  }
}

// ========== 钱包 ==========
export const walletAPI = {
  async getWallet(userId: string | number): Promise<ApiResponse<Wallet>> {
    try {
      const data = await apiRequest<any>(`/wallets/user/${userId}`)
      return { data }
    } catch (e: any) {
      return { data: { balance: 0, frozen_balance: 0 } as Wallet }
    }
  },

  async recharge(userId: string | number, amount: number): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/wallets/user/${userId}/recharge`, 'POST', { amount })
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async withdraw(
    userId: string | number,
    amount: number,
    extra: { pay_method?: string; account?: string; real_name?: string } = {}
  ): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/wallets/user/${userId}/withdraw`, 'POST', {
        amount,
        pay_method: extra.pay_method || 'alipay',
        account: extra.account || '',
        real_name: extra.real_name || ''
      })
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },
  async getWithdrawRecords(userId: string | number): Promise<ApiResponse<any[]>> {
    try {
      const data = await apiRequest<any[]>(`/wallets/user/${userId}/withdraw-records`)
      return { data: data || [] }
    } catch (e: any) {
      return { data: [] }
    }
  },

  // 兼容旧调用
  async updateWallet(userId: string | number, updateData: Partial<Wallet>): Promise<ApiResponse<Wallet>> {
    try {
      const data = await apiRequest<any>(`/wallets/user/${userId}/recharge`, 'POST', updateData)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async getBalance(userId: string | number): Promise<ApiResponse<Wallet>> {
    return this.getWallet(userId)
  }
}

// ========== 交易记录 ==========
interface TransactionData {
  user_id: string | number
  type: string
  title: string
  amount: string | number
  balance_before?: string | number
  balance_after?: string | number
  reference_type?: string
  reference_id?: string | number
}

export const transactionAPI = {
  async createTransaction(tx: TransactionData): Promise<ApiResponse<Transaction>> {
    try {
      const body: any = {
        user_id: String(tx.user_id),
        type: tx.type,
        title: tx.title,
        subtitle: (tx as any).subtitle || '',
        amount: Number(tx.amount),
        balance_before: Number(tx.balance_before || 0),
        balance_after: Number(tx.balance_after || 0),
        status: (tx as any).status || 'completed',
        reference_type: tx.reference_type,
        reference_id: tx.reference_id ? String(tx.reference_id) : null,
        order_id: (tx as any).order_id ? String((tx as any).order_id) : null
      }
      const data = await apiRequest<any>('/transactions', 'POST', body)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async getTransactions(userId: string | number): Promise<ApiResponse<Transaction[]>> {
    try {
      const data = await apiRequest<any[]>(`/transactions/user/${userId}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  }
}

// ========== 用户游戏库 ==========
interface UserGameData {
  user_id: string | number
  order_id?: string | number
  game_id?: string | number
  game_name: string
  game_image?: string
  cdkey?: string
  version?: string
  status?: string
}

export const userGameAPI = {
  async getUserGames(userId: string | number): Promise<any[]> {
    try {
      const data = await apiRequest<any[]>(`/user-games/user/${userId}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  },

  async addUserGame(g: UserGameData): Promise<ApiResponse<any>> {
    try {
      const body: any = {
        user_id: String(g.user_id),
        order_id: g.order_id ? String(g.order_id) : null,
        game_id: g.game_id ? Number(g.game_id) : null,
        game_name: g.game_name,
        game_image: g.game_image || '',
        cdkey: g.cdkey || '',
        version: g.version || '标准版',
        status: g.status || 'pending'
      }
      const data = await apiRequest<any>('/user-games', 'POST', body)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async activateGame(gameId: string | number): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/user-games/${gameId}/activate`, 'PUT')
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async deleteGame(gameId: string | number): Promise<ApiResponse<any>> {
    try {
      await apiRequest<any>(`/user-games/${gameId}`, 'DELETE')
      return { data: null }
    } catch (e: any) {
      return { error: e.message }
    }
  }
}

// ========== 卖家额度 ==========
export const sellerAPI = {
  async getSellerQuota(sellerId: string): Promise<ApiResponse<any>> {
    try {
      return { data: null }
    } catch {
      return { data: null }
    }
  }
}

// ========== 上架/CDKey（listings）==========
export const listingAPI = {
  async createListing(listing: any): Promise<ApiResponse<any>> {
    try {
      const body: any = {
        seller_id: String(listing.seller_id),
        game_id: Number(listing.game_id),
        game_name: listing.game_name,
        game_image: listing.game_image || '',
        version: listing.version || '标准版',
        cdkey: listing.cdkey,
        price: Number(listing.price),
        original_price: Number(listing.original_price || 0),
        region: listing.region || '国区'
      }
      const data = await apiRequest<any>('/listings', 'POST', body)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async createBatch(listings: any[]): Promise<ApiResponse<any>> {
    try {
      const body = listings.map(l => ({
        seller_id: String(l.seller_id),
        game_id: Number(l.game_id),
        game_name: l.game_name,
        game_image: l.game_image || '',
        version: l.version || '标准版',
        cdkey: l.cdkey,
        price: Number(l.price),
        original_price: Number(l.original_price || 0),
        region: l.region || '国区'
      }))
      const data = await apiRequest<any>('/listings/batch', 'POST', body)
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  async getAvailable(gameId?: number): Promise<ApiResponse<any[]>> {
    try {
      const url = gameId ? `/listings/available?game_id=${gameId}` : '/listings/available'
      const data = await apiRequest<any[]>(url)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  },

  /** 某游戏按卖家+价格 聚合的可售列表（GameDetail 用的同一接口） */
  async getGrouped(params: { game_id?: number; game_name?: string }): Promise<ApiResponse<any[]>> {
    try {
      const qs = new URLSearchParams()
      if (params.game_id) qs.set('game_id', String(params.game_id))
      if (params.game_name) qs.set('game_name', params.game_name)
      const data = await apiRequest<any[]>(`/listings/available-grouped?${qs.toString()}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  },

  /** 查重：给定 CDKey，查 listings 表里是否已存在 */
  async checkCdkey(cdkey: string): Promise<ApiResponse<{ exists: boolean }>> {
    try {
      const data = await apiRequest<{ exists: boolean }>(`/listings/check-cdkey?cdkey=${encodeURIComponent(cdkey.toUpperCase())}`)
      return { data }
    } catch (e: any) {
      return { data: { exists: false } }
    }
  },

  async getPySellers(params: { gameId?: number; region?: string } = {}): Promise<ApiResponse<any[]>> {
    try {
      const qs = new URLSearchParams()
      if (params.gameId) qs.set('game_id', String(params.gameId))
      if (params.region) qs.set('region', params.region)
      const data = await apiRequest<any[]>(`/listings/py-sellers${qs.toString() ? '?' + qs.toString() : ''}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  },

  async getBySeller(sellerId: string | number): Promise<ApiResponse<any[]>> {
    try {
      const data = await apiRequest<any[]>(`/listings/seller/${sellerId}`)
      return { data }
    } catch (e: any) {
      return { data: [] }
    }
  },

  async deleteListing(id: string): Promise<ApiResponse<any>> {
    try {
      await apiRequest<any>(`/listings/${id}`, 'DELETE')
      return { data: null }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  /** 改单个 CDK 价格（available） */
  async updatePrice(id: string, price: number): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/listings/${id}/price`, 'PUT', { price })
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  /** 批量改价：{ listingId: newPrice } */
  async batchUpdatePrice(idPriceMap: Record<string, number>): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>('/listings/batch-price', 'PUT', { updates: idPriceMap })
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  /** 已售 → 待激活（拿回 CDK） */
  async softDelete(id: string): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/listings/${id}/soft-delete`, 'PUT')
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  /** 待激活 → 重新上架 */
  async relistPending(id: string): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/listings/${id}/relist`, 'PUT')
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  },

  /** 待激活 → 自己激活（加入 user_games）*/
  async selfActivate(id: string): Promise<ApiResponse<any>> {
    try {
      const data = await apiRequest<any>(`/listings/${id}/self-activate`, 'PUT')
      return { data }
    } catch (e: any) {
      return { error: e.message }
    }
  }
}

// ========== 首页游戏数据 ==========
export const fetchAllGames = async (): Promise<Game[]> => {
  try {
    const data = await apiRequest<Game[]>('/games')
    return data.map(mapGameToFrontend)
  } catch {
    const res = await axios.get('/cdk_games.json')
    const d = res.data
    return Array.isArray(d) ? d : [...(d.preSaleItems || []), ...(d.gameItems || [])]
  }
}

export const fetchGamesFromSupabase = async (): Promise<{ preSaleItems: Game[], gameItems: Game[] }> => {
  try {
    const data = await apiRequest<any[]>('/games')
    const mapped = data.map(mapGameToFrontend)
    return {
      preSaleItems: mapped.filter((g: any) => g.isPresale || g.is_presale),
      gameItems: mapped.filter((g: any) => !g.isPresale && !g.is_presale)
    }
  } catch {
    const res = await axios.get('/cdk_games.json')
    const d = res.data
    let games: Game[] = Array.isArray(d) ? d : [...(d.preSaleItems || []), ...(d.gameItems || [])]
    return {
      preSaleItems: games.filter((g: any) => g.is_presale || g.isPresale),
      gameItems: games.filter((g: any) => !g.is_presale && !g.isPresale)
    }
  }
}

// 把 Spring Boot 返回的 snake_case 字段映射到前端期望的格式
function mapGameToFrontend(g: any): any {
  return {
    id: g.id,
    name: g.name,
    price: g.price,
    original_price: g.original_price ?? g.originalPrice,
    originalPrice: g.original_price ?? g.originalPrice,
    discount: g.discount,
    image: g.image_url || g.imageUrl || g.image,
    image_url: g.image_url || g.imageUrl || g.image,
    link: g.link,
    description: g.description,
    release_date: g.release_date ?? g.releaseDate,
    releaseDate: g.release_date ?? g.releaseDate,
    developer: g.developer,
    is_presale: g.is_presale ?? g.isPresale,
    isPresale: g.is_presale ?? g.isPresale,
    stock: g.stock
  }
}

// 默认导出（兼容旧代码 import supabaseRequest）
export default apiRequest

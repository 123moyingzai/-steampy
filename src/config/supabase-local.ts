// Supabase 配置 - TypeScript 版本
import axios, { type AxiosRequestConfig } from 'axios'

export const SUPABASE_URL = 'https://jffospwfgajablevtnqu.supabase.co'
const SUPABASE_KEY = 'sb_publishable_pDsZKZSNQSlKbDYRsz2t_Q_FVznIhu0'

const headers: Record<string, string> = {
  'apikey': SUPABASE_KEY,
  'Authorization': `Bearer ${SUPABASE_KEY}`,
  'Content-Type': 'application/json',
  'Prefer': 'return=representation'
}

// 请求选项接口
interface RequestOptions {
  method?: string
  body?: string
  headers?: Record<string, string>
  params?: Record<string, string | number | boolean>
}

// 发送请求的函数 - 使用 axios
async function supabaseRequest(endpoint: string, options: RequestOptions = {}): Promise<any> {
  const url = `${SUPABASE_URL}/rest/v1/${endpoint}`
  
  const config: AxiosRequestConfig = {
    method: (options.method || 'GET').toLowerCase(),
    url: url,
    headers: { ...headers, ...options.headers },
    data: options.body ? JSON.parse(options.body) : undefined,
    params: options.params
  }
  
  try {
    const response = await axios(config)
    
    if (response.status >= 200 && response.status < 300) {
      return response.data
    } else {
      throw new Error(response.data?.message || `HTTP ${response.status}`)
    }
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data?.message || `HTTP ${error.response.status}`)
    }
    throw error
  }
}

// 用户数据接口
interface UserData {
  username: string
  password?: string
  phone?: string
  nickname?: string
  user_type?: string
}

// API 响应接口
interface ApiResponse<T = any> {
  data?: T
  error?: string
}

// 用户认证相关
export const authAPI = {
  // 用户注册
  async register(userData: UserData): Promise<ApiResponse<User>> {
    try {
      // 先检查用户名是否已存在
      const existing = await supabaseRequest(`users?select=id&username=eq.${encodeURIComponent(userData.username)}`)
      if (existing && existing.length > 0) {
        return { error: '用户名已被注册' }
      }
      
      // 创建新用户
      const newUser = {
        username: userData.username,
        password_hash: userData.password,
        phone: userData.phone,
        nickname: userData.username,
        user_type: '普通用户',
        created_at: new Date().toISOString()
      }
      
      const result = await supabaseRequest('users', {
        method: 'POST',
        body: JSON.stringify(newUser)
      })
      
      return { data: result[0] }
    } catch (error: any) {
      console.error('注册失败:', error)
      return { error: error.message }
    }
  },

  // 用户登录
  async login(username: string, password: string): Promise<ApiResponse<User>> {
    try {
      const users = await supabaseRequest(`users?select=*&username=eq.${encodeURIComponent(username)}`)
      
      if (!users || users.length === 0) {
        return { error: '用户名或密码错误' }
      }
      
      const user: User = users[0]
      if (user.password_hash !== password) {
        return { error: '用户名或密码错误' }
      }
      
      // 保存到sessionStorage
      const { password_hash, ...userWithoutPassword } = user
      sessionStorage.setItem('steampy_user', JSON.stringify(userWithoutPassword))
      return { data: userWithoutPassword as User }
    } catch (error: any) {
      console.error('登录失败:', error)
      return { error: error.message }
    }
  },

  // 获取当前用户
  getCurrentUser(): User | null {
    const user = sessionStorage.getItem('steampy_user')
    return user ? JSON.parse(user) : null
  },

  // 退出登录
  logout(): void {
    sessionStorage.removeItem('steampy_user')
  },

  // 更新用户信息
  async updateUser(userId: string, updateData: Partial<User>): Promise<ApiResponse<User>> {
    try {
      const result = await supabaseRequest(`users?id=eq.${userId}`, {
        method: 'PATCH',
        body: JSON.stringify(updateData)
      })
      return { data: result[0] }
    } catch (error: any) {
      console.error('更新用户失败:', error)
      return { error: error.message }
    }
  }
}

// 游戏相关 - 从 Supabase 获取（使用 axios）
export const gameAPI = {
  async getGames(options: Record<string, any> = {}): Promise<ApiResponse<Game[]>> {
    try {
      const games: Game[] = await supabaseRequest('games?select=*&order=name')
      return { data: games }
    } catch (error: any) {
      console.error('获取游戏失败:', error)
      // 备用：从本地 JSON 加载（使用 axios）
      const response = await axios.get('/cdk_games.json')
      const games: Game[] = response.data
      return { data: games }
    }
  },

  async getGameById(gameId: number | string): Promise<ApiResponse<Game | null>> {
    try {
      const games: Game[] = await supabaseRequest(`games?select=*&id=eq.${gameId}&limit=1`)
      return { data: games[0] || null }
    } catch (error: any) {
      console.error('获取游戏失败:', error)
      return { error: error.message }
    }
  }
}

// 公告接口
interface Announcement {
  id: number
  title: string
  content: string
  publish_date: string
  is_top: boolean
  is_active: boolean
}

// 公告相关 - 从 Supabase 获取
export const announcementAPI = {
  async getAnnouncements(limit: number = 10): Promise<ApiResponse<Announcement[]>> {
    try {
      const announcements: Announcement[] = await supabaseRequest(`announcements?select=*&is_active=eq.true&order=publish_date.desc&limit=${limit}`)
      return { data: announcements }
    } catch (error: any) {
      console.error('获取公告失败:', error)
      // 返回默认公告
      return {
        data: [
          {
            id: 1,
            title: '欢迎使用 SteamPY 平台',
            content: '这是一个安全可靠的 Steam 游戏交易平台',
            publish_date: new Date().toISOString(),
            is_top: true,
            is_active: true
          }
        ]
      }
    }
  }
}

// 订单接口
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

// 订单相关 - 保存到 Supabase
export const orderAPI = {
  async createOrder(orderData: OrderData): Promise<ApiResponse<Order>> {
    try {
      const orderNo = 'ORD' + Date.now().toString(36).toUpperCase()
      const newOrder = {
        order_no: orderNo,
        buyer_id: String(orderData.buyer_id),
        game_id: parseInt(String(orderData.game_id)) || null,
        game_name: orderData.game_name,
        game_image: orderData.game_image || '',
        price: parseFloat(String(orderData.price)),
        quantity: orderData.quantity || 1,
        total_price: parseFloat(String(orderData.total_amount || orderData.total_price || orderData.price)),
        status: 'completed',
        order_type: orderData.order_type || 'cdkey',
        cdkey: orderData.cdkey || '',
        created_at: new Date().toISOString()
      }
      
      const result = await supabaseRequest('orders', {
        method: 'POST',
        body: JSON.stringify(newOrder)
      })
      
      return { data: result[0] }
    } catch (error: any) {
      console.error('创建订单失败:', error)
      return { error: error.message }
    }
  },

  async getUserOrders(userId: string | number): Promise<ApiResponse<Order[]>> {
    try {
      const orders: Order[] = await supabaseRequest(`orders?select=*&buyer_id=eq.${String(userId)}&order=created_at.desc`)
      return { data: orders }
    } catch (error: any) {
      console.error('获取订单失败:', error)
      return { data: [] }
    }
  },

  async getOrders(userId: string | number): Promise<ApiResponse<Order[]>> {
    return this.getUserOrders(userId)
  }
}

// 钱包相关 - 从 Supabase 获取
export const walletAPI = {
  async getWallet(userId: string | number): Promise<ApiResponse<Wallet>> {
    try {
      const wallets: Wallet[] = await supabaseRequest(`user_wallets?select=*&user_id=eq.${String(userId)}&limit=1`)
      if (wallets && wallets.length > 0) {
        return { data: wallets[0] }
      }
      // 如果没有钱包，创建一个
      const newWallet = {
        user_id: String(userId),
        balance: 0,
        frozen_balance: 0,
        created_at: new Date().toISOString()
      }
      const result = await supabaseRequest('user_wallets', {
        method: 'POST',
        body: JSON.stringify(newWallet)
      })
      return { data: result[0] }
    } catch (error: any) {
      console.error('获取钱包失败:', error)
      return { data: { balance: 0, frozen_balance: 0 } as Wallet }
    }
  },

  async updateWallet(userId: string | number, updateData: Partial<Wallet>): Promise<ApiResponse<Wallet>> {
    try {
      const result = await supabaseRequest(`user_wallets?user_id=eq.${String(userId)}`, {
        method: 'PATCH',
        body: JSON.stringify(updateData)
      })
      return { data: result[0] }
    } catch (error: any) {
      console.error('更新钱包失败:', error)
      return { error: error.message }
    }
  },

  async getBalance(userId: string | number): Promise<ApiResponse<Wallet>> {
    return this.getWallet(userId)
  }
}

// 交易记录接口
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

// 交易记录相关 - 保存到 Supabase
export const transactionAPI = {
  async createTransaction(transactionData: TransactionData): Promise<ApiResponse<Transaction>> {
    try {
      const transactionNo = 'TXN' + Date.now().toString(36).toUpperCase()
      const newTransaction = {
        transaction_no: transactionNo,
        user_id: String(transactionData.user_id),
        type: transactionData.type,
        title: transactionData.title,
        amount: parseFloat(String(transactionData.amount)),
        balance_before: parseFloat(String(transactionData.balance_before || 0)),
        balance_after: parseFloat(String(transactionData.balance_after || 0)),
        status: 'completed',
        reference_type: transactionData.reference_type,
        reference_id: transactionData.reference_id,
        created_at: new Date().toISOString()
      }
      
      const result = await supabaseRequest('transactions', {
        method: 'POST',
        body: JSON.stringify(newTransaction)
      })
      
      return { data: result[0] }
    } catch (error: any) {
      console.error('创建交易记录失败:', error)
      return { error: error.message }
    }
  },

  async getTransactions(userId: string | number): Promise<ApiResponse<Transaction[]>> {
    try {
      const transactions: Transaction[] = await supabaseRequest(`transactions?select=*&user_id=eq.${String(userId)}&order=created_at.desc`)
      return { data: transactions }
    } catch (error: any) {
      console.error('获取交易记录失败:', error)
      return { data: [] }
    }
  }
}

// 用户游戏库接口
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

// 用户游戏库相关 - 保存到 Supabase
export const userGameAPI = {
  async getUserGames(userId: string | number): Promise<any[]> {
    try {
      const games = await supabaseRequest(`user_games?select=*&user_id=eq.${String(userId)}&order=purchase_date.desc`)
      return { data: games }
    } catch (error: any) {
      console.error('获取游戏库失败:', error)
      return { data: [] }
    }
  },

  async addUserGame(gameData: UserGameData): Promise<ApiResponse<any>> {
    try {
      const newGame = {
        user_id: String(gameData.user_id),
        order_id: gameData.order_id || null,
        game_id: parseInt(String(gameData.game_id)) || null,
        game_name: gameData.game_name,
        game_image: gameData.game_image || '',
        cdkey: gameData.cdkey || '',
        version: gameData.version || '标准版',
        status: 'pending',
        purchase_date: new Date().toISOString()
      }
      
      const result = await supabaseRequest('user_games', {
        method: 'POST',
        body: JSON.stringify(newGame)
      })
      
      return { data: result[0] }
    } catch (error: any) {
      console.error('添加游戏失败:', error)
      return { error: error.message }
    }
  },

  async activateGame(gameId: string | number): Promise<ApiResponse<any>> {
    try {
      const result = await supabaseRequest(`user_games?id=eq.${gameId}`, {
        method: 'PATCH',
        body: JSON.stringify({
          status: 'activated',
          activation_date: new Date().toISOString()
        })
      })
      return { data: result[0] }
    } catch (error: any) {
      console.error('激活游戏失败:', error)
      return { error: error.message }
    }
  }
}

// 卖家额度相关
export const sellerAPI = {
  async getSellerQuota(sellerId: string): Promise<ApiResponse<any>> {
    try {
      const quota = await supabaseRequest(`seller_quota?select=*&seller_id=eq.${sellerId}&limit=1`)
      return { data: quota[0] || null }
    } catch (error: any) {
      console.error('获取额度失败:', error)
      return { data: null }
    }
  }
}

// 游戏数据获取（首页）- 使用 axios
export const fetchAllGames = async (): Promise<Game[]> => {
  try {
    const games: Game[] = await supabaseRequest('games?select=*&order=name')
    return games
  } catch (error: any) {
    console.error('获取游戏失败:', error)
    // 备用：从本地 JSON 加载（使用 axios）
    const response = await axios.get('/cdk_games.json')
    const data = response.data
    if (Array.isArray(data)) return data
    return [...(data.preSaleItems || []), ...(data.gameItems || [])]
  }
}

// 获取游戏列表（用于首页轮播）- 使用 axios
export const fetchGamesFromSupabase = async (): Promise<{ preSaleItems: Game[], gameItems: Game[] }> => {
  try {
    const games: Game[] = await supabaseRequest('games?select=*&order=name')
    
    return {
      preSaleItems: games.filter((g: any) => g.is_presale).map((g: any) => ({
        name: g.name,
        price: g.price,
        originalPrice: g.original_price,
        discount: g.discount,
        image: g.image_url || g.image,
        link: g.link,
        description: g.description,
        releaseDate: g.release_date,
        developer: g.developer,
        isPresale: true,
        stock: g.stock,
        id: g.id
      })),
      gameItems: games.filter((g: any) => !g.is_presale).map((g: any) => ({
        name: g.name,
        price: g.price,
        originalPrice: g.original_price,
        discount: g.discount,
        image: g.image_url || g.image,
        link: g.link,
        description: g.description,
        releaseDate: g.release_date,
        developer: g.developer,
        isPresale: false,
        stock: g.stock,
        id: g.id
      }))
    }
  } catch (error: any) {
    console.error('获取游戏失败:', error)
    // 备用：从本地 JSON 加载（使用 axios）
    const response = await axios.get('/cdk_games.json')
    const data = response.data
    let games: Game[] = []
    if (Array.isArray(data)) {
      games = data
    } else {
      games = [...(data.preSaleItems || []), ...(data.gameItems || [])]
    }
    
    return {
      preSaleItems: games.filter((g: any) => g.is_presale),
      gameItems: games.filter((g: any) => !g.is_presale)
    }
  }
}

export default supabaseRequest

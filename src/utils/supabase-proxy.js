// Supabase 配置 - 使用代理
export const SUPABASE_URL = '/supabase'
const SUPABASE_ANON_KEY = 'sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_'

const headers = {
  'apikey': SUPABASE_ANON_KEY,
  'Authorization': `Bearer ${SUPABASE_ANON_KEY}`,
  'Content-Type': 'application/json',
  'Prefer': 'return=representation'
}

export async function request(url, options = {}) {
  // 如果是完整 URL，替换为代理路径
  const proxyUrl = url.replace('https://prvmjufbhsofvnjeswhq.supabase.co', SUPABASE_URL)
  
  const response = await fetch(proxyUrl, {
    ...options,
    mode: 'cors',
    credentials: 'omit',
    headers: { ...headers, ...options.headers }
  })
  
  if (!response.ok) {
    const error = await response.json().catch(() => ({}))
    throw new Error(error.message || `HTTP ${response.status}`)
  }
  
  return response.json()
}

// 获取所有游戏
export async function fetchAllGames() {
  try {
    const games = await request(`${SUPABASE_URL}/rest/v1/games?select=*&order=name`)
    return games
  } catch (error) {
    console.error('获取所有游戏失败:', error)
    // 从本地 JSON 文件加载备用数据
    const response = await fetch('/cdk_games.json')
    const localGames = await response.json()
    return localGames
  }
}

// 获取游戏列表（用于首页轮播）
export async function fetchGamesFromSupabase() {
  try {
    // 获取所有游戏，不过滤 is_active（表里没有这个字段）
    const games = await request(`${SUPABASE_URL}/rest/v1/games?select=*&order=name`)
    
    return {
      preSaleItems: games.filter(g => g.is_presale).map(g => ({
        name: g.name,
        price: g.price,
        originalPrice: g.original_price,
        discount: g.discount,
        image: g.image,
        link: g.link,
        description: g.description,
        releaseDate: g.release_date,
        developer: g.developer,
        isPresale: true,
        stock: g.stock,
        id: g.id
      })),
      gameItems: games.filter(g => !g.is_presale).map(g => ({
        name: g.name,
        price: g.price,
        originalPrice: g.original_price,
        discount: g.discount,
        image: g.image,
        link: g.link,
        description: g.description,
        releaseDate: g.release_date,
        developer: g.developer,
        isPresale: false,
        stock: g.stock,
        id: g.id
      }))
    }
  } catch (error) {
    console.error('从 Supabase 获取游戏数据失败:', error)
    // 从本地 JSON 文件加载备用数据
    const response = await fetch('/cdk_games.json')
    const localGames = await response.json()
    
    return {
      preSaleItems: localGames.filter(g => g.is_presale).map(g => ({
        name: g.name,
        price: g.price,
        originalPrice: g.original_price,
        discount: g.discount,
        image: g.image,
        link: g.link,
        description: g.description,
        releaseDate: g.release_date,
        developer: g.developer,
        isPresale: true,
        stock: g.stock,
        id: g.id
      })),
      gameItems: localGames.filter(g => !g.is_presale).map(g => ({
        name: g.name,
        price: g.price,
        originalPrice: g.original_price,
        discount: g.discount,
        image: g.image,
        link: g.link,
        description: g.description,
        releaseDate: g.release_date,
        developer: g.developer,
        isPresale: false,
        stock: g.stock,
        id: g.id
      }))
    }
  }
}

// 公告相关
export const announcementAPI = {
  // 获取公告列表
  async getAnnouncements(limit = 10) {
    try {
      const announcements = await request(
        `${SUPABASE_URL}/rest/v1/announcements?is_active=eq.true&order=publish_date.desc,is_top.desc&limit=${limit}`
      )
      return { data: announcements }
    } catch (error) {
      console.error('获取公告失败:', error)
      // 返回默认公告数据
      return {
        data: [
          {
            id: 1,
            title: '欢迎使用 SteamPY 平台',
            content: '这是一个安全可靠的 Steam 游戏交易平台',
            publish_date: new Date().toISOString(),
            is_top: true,
            is_active: true
          },
          {
            id: 2,
            title: '新用户注册送10元优惠券',
            content: '新用户注册即可获得10元优惠券，满100元可用',
            publish_date: new Date().toISOString(),
            is_top: false,
            is_active: true
          }
        ]
      }
    }
  }
}

// 钱包相关
export const walletAPI = {
  // 获取用户钱包
  async getWallet(userId) {
    try {
      const wallet = await request(
        `${SUPABASE_URL}/rest/v1/user_wallets?select=*&user_id=eq.${userId}&limit=1`
      )
      return { data: wallet[0] || null }
    } catch (error) {
      console.error('获取钱包失败:', error)
      // 返回默认钱包数据
      return { data: { balance: 0.06, frozen_balance: 0 } }
    }
  },

  // 更新钱包余额
  async updateWallet(userId, updateData) {
    try {
      const wallet = await request(
        `${SUPABASE_URL}/rest/v1/user_wallets?user_id=eq.${userId}`,
        {
          method: 'PATCH',
          body: JSON.stringify(updateData)
        }
      )
      return { data: wallet[0] }
    } catch (error) {
      console.error('更新钱包失败:', error)
      return { error: error.message }
    }
  }
}

// 订单相关
export const orderAPI = {
  // 创建订单
  async createOrder(orderData) {
    try {
      const order = await request(
        `${SUPABASE_URL}/rest/v1/orders`,
        {
          method: 'POST',
          body: JSON.stringify(orderData)
        }
      )
      return { data: order[0] }
    } catch (error) {
      console.error('创建订单失败:', error)
      return { error: error.message }
    }
  },

  // 获取用户订单
  async getUserOrders(userId) {
    try {
      const orders = await request(
        `${SUPABASE_URL}/rest/v1/orders?select=*&buyer_id=eq.${userId}&order=created_at.desc`
      )
      return { data: orders }
    } catch (error) {
      console.error('获取用户订单失败:', error)
      return { data: [] }
    }
  }
}

// 用户相关
export const userAPI = {
  // 获取用户信息
  async getUserInfo(userId) {
    try {
      const user = await request(
        `${SUPABASE_URL}/rest/v1/users?select=*&id=eq.${userId}&limit=1`
      )
      return { data: user[0] || null }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return { error: error.message }
    }
  },

  // 更新用户信息
  async updateUserInfo(userId, updateData) {
    try {
      const user = await request(
        `${SUPABASE_URL}/rest/v1/users?id=eq.${userId}`,
        {
          method: 'PATCH',
          body: JSON.stringify(updateData)
        }
      )
      return { data: user[0] }
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return { error: error.message }
    }
  }
}

// 游戏详情相关
export const gameAPI = {
  // 根据名称获取游戏
  async getGameByName(name) {
    try {
      const games = await request(
        `${SUPABASE_URL}/rest/v1/games?select=*&name=eq.${encodeURIComponent(name)}&limit=1`
      )
      return { data: games[0] || null }
    } catch (error) {
      console.error('获取游戏失败:', error)
      return { error: error.message }
    }
  },

  // 根据ID获取游戏
  async getGameById(id) {
    try {
      const games = await request(
        `${SUPABASE_URL}/rest/v1/games?select=*&id=eq.${id}&limit=1`
      )
      return { data: games[0] || null }
    } catch (error) {
      console.error('获取游戏失败:', error)
      return { error: error.message }
    }
  }
}

// 交易记录相关
export const transactionAPI = {
  async createTransaction(transactionData) {
    try {
      const transactionNo = 'T' + Date.now().toString(36).toUpperCase()
      
      const transaction = await request(
        `${SUPABASE_URL}/rest/v1/transactions`,
        {
          method: 'POST',
          body: JSON.stringify({
            transaction_no: transactionNo,
            user_id: transactionData.user_id,
            type: transactionData.type,
            title: transactionData.title,
            subtitle: transactionData.subtitle,
            amount: parseFloat(transactionData.amount),
            balance_before: parseFloat(transactionData.balance_before || 0),
            balance_after: parseFloat(transactionData.balance_after || 0),
            status: transactionData.status || 'completed',
            reference_type: transactionData.reference_type,
            reference_id: transactionData.reference_id,
            order_id: transactionData.order_id
          })
        }
      )
      
      return { data: transaction[0] }
    } catch (error) {
      console.error('创建交易记录失败:', error)
      return { error: error.message }
    }
  },

  async getTransactions(userId, filters = {}) {
    try {
      let url = `${SUPABASE_URL}/rest/v1/transactions?user_id=eq.${userId}&order=created_at.desc`
      
      if (filters.type) url += `&type=eq.${filters.type}`
      if (filters.startDate) url += `&created_at=gte.${filters.startDate}`
      if (filters.endDate) url += `&created_at=lte.${filters.endDate}`
      
      const transactions = await request(url)
      return { data: transactions }
    } catch (error) {
      console.error('获取交易记录失败:', error)
      return { error: error.message, data: [] }
    }
  }
}

// 用户游戏库相关
export const userGameAPI = {
  async getUserGames(userId) {
    try {
      const games = await request(
        `${SUPABASE_URL}/rest/v1/user_games?user_id=eq.${userId}&order=purchase_date.desc`
      )
      return { data: games }
    } catch (error) {
      console.error('获取我的游戏失败:', error)
      return { error: error.message, data: [] }
    }
  },

  async addUserGame(gameData) {
    try {
      const game = await request(
        `${SUPABASE_URL}/rest/v1/user_games`,
        {
          method: 'POST',
          body: JSON.stringify({
            user_id: gameData.user_id,
            order_id: gameData.order_id,
            game_id: gameData.game_id,
            game_name: gameData.game_name,
            game_image: gameData.game_image,
            cdkey: gameData.cdkey,
            version: gameData.version || '标准版',
            status: gameData.status || 'pending'
          })
        }
      )
      return { data: game[0] }
    } catch (error) {
      console.error('添加游戏到库失败:', error)
      return { error: error.message }
    }
  },

  async activateGame(gameId) {
    try {
      const result = await request(
        `${SUPABASE_URL}/rest/v1/user_games?id=eq.${gameId}`,
        {
          method: 'PATCH',
          body: JSON.stringify({
            status: 'activated',
            activation_date: new Date().toISOString()
          })
        }
      )
      return { data: result[0] }
    } catch (error) {
      console.error('激活游戏失败:', error)
      return { error: error.message }
    }
  }
}

// 卖家额度相关
export const sellerAPI = {
  async getSellerQuota(sellerId) {
    try {
      const quota = await request(
        `${SUPABASE_URL}/rest/v1/seller_quota?seller_id=eq.${sellerId}&limit=1`
      )
      return { data: quota[0] || null }
    } catch (error) {
      console.error('获取卖家额度失败:', error)
      return { data: { quota_total: 0, quota_used: 0, quota_remaining: 0 } }
    }
  }
}
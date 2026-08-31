// Supabase 配置
export const SUPABASE_URL = 'https://prvmjufbhsofvnjeswhq.supabase.co'
const SUPABASE_ANON_KEY = 'sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_'

const headers = {
  'apikey': SUPABASE_ANON_KEY,
  'Authorization': `Bearer ${SUPABASE_ANON_KEY}`,
  'Content-Type': 'application/json',
  'Prefer': 'return=representation',
  'Access-Control-Allow-Origin': '*'
}

export async function request(url, options = {}) {
  const response = await fetch(url, {
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
    return { preSaleItems: [], gameItems: [] }
  }
}

export async function fetchAllGames() {
  try {
    const games = await request(`${SUPABASE_URL}/rest/v1/games?select=*&order=name`)
    
    return games.map(g => ({
      id: g.id,
      name: g.name,
      price: g.price,
      originalPrice: g.original_price,
      discount: g.discount,
      image: g.image,
      link: g.link,
      description: g.description,
      releaseDate: g.release_date,
      developer: g.developer,
      isPresale: g.is_presale,
      stock: g.stock
    }))
  } catch (error) {
    console.error('获取所有游戏失败:', error)
    return []
  }
}

export const orderAPI = {
  async createOrder(orderData) {
    try {
      const orderNo = 'ORD' + Date.now().toString(36).toUpperCase()
      
      const order = await request(`${SUPABASE_URL}/rest/v1/orders`, {
        method: 'POST',
        body: JSON.stringify({
          order_no: orderNo,
          buyer_id: orderData.buyer_id,
          game_id: orderData.game_id,
          game_name: orderData.game_name,
          game_image: orderData.game_image,
          price: parseFloat(orderData.price),
          quantity: orderData.quantity || 1,
          total_price: parseFloat(orderData.total_price),
          delivery_method: orderData.delivery_method || 'cdkey',
          version: orderData.version || '标准版',
          cdkey: orderData.cdkey,
          status: orderData.status || 'completed',
          payment_method: orderData.payment_method
        })
      })
      
      return { data: order[0] }
    } catch (error) {
      console.error('创建订单失败:', error)
      return { error: error.message }
    }
  },

  async getOrders(userId) {
    try {
      const orders = await request(
        `${SUPABASE_URL}/rest/v1/orders?buyer_id=eq.${userId}&order=created_at.desc`
      )
      return { data: orders }
    } catch (error) {
      console.error('获取订单失败:', error)
      return { error: error.message, data: [] }
    }
  },

  async updateOrderStatus(orderId, status) {
    try {
      const result = await request(`${SUPABASE_URL}/rest/v1/orders?id=eq.${orderId}`, {
        method: 'PATCH',
        body: JSON.stringify({ status, paid_at: new Date().toISOString() }),
        headers: { 'Prefer': 'return=representation' }
      })
      return { data: result[0] }
    } catch (error) {
      console.error('更新订单状态失败:', error)
      return { error: error.message }
    }
  }
}

export const transactionAPI = {
  async createTransaction(transactionData) {
    try {
      const transactionNo = 'T' + Date.now().toString(36).toUpperCase()
      
      const transaction = await request(`${SUPABASE_URL}/rest/v1/transactions`, {
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
      })
      
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

export const walletAPI = {
  async getBalance(userId) {
    try {
      const wallets = await request(`${SUPABASE_URL}/rest/v1/user_wallets?user_id=eq.${userId}`)
      
      if (wallets && wallets.length > 0) {
        return { data: wallets[0].balance }
      }
      
      const newWallet = await request(`${SUPABASE_URL}/rest/v1/user_wallets`, {
        method: 'POST',
        body: JSON.stringify({
          user_id: userId,
          balance: 0.06
        })
      })
      
      return { data: newWallet[0].balance }
    } catch (error) {
      console.error('获取余额失败:', error)
      return { error: error.message, data: 0.06 }
    }
  },

  async updateBalance(userId, amount, operation = 'add') {
    try {
      const wallets = await request(`${SUPABASE_URL}/rest/v1/user_wallets?user_id=eq.${userId}`)
      let currentBalance = 0
      
      if (wallets && wallets.length > 0) {
        currentBalance = parseFloat(wallets[0].balance) || 0
        
        if (operation === 'add') {
          currentBalance += parseFloat(amount)
        } else if (operation === 'subtract') {
          currentBalance -= parseFloat(amount)
        }
        
        await request(`${SUPABASE_URL}/rest/v1/user_wallets?id=eq.${wallets[0].id}`, {
          method: 'PATCH',
          body: JSON.stringify({ balance: currentBalance }),
          headers: { 'Prefer': 'return=representation' }
        })
        
        return { data: currentBalance }
      }
      
      return { error: '未找到用户钱包' }
    } catch (error) {
      console.error('更新余额失败:', error)
      return { error: error.message }
    }
  },

  async recharge(userId, amount, paymentMethod) {
    try {
      const result = await this.updateBalance(userId, amount, 'add')
      
      if (result.error) return result
      
      await transactionAPI.createTransaction({
        user_id: userId,
        type: 'recharge',
        title: `充值 ¥${amount}`,
        subtitle: `支付方式: ${paymentMethod}`,
        amount: parseFloat(amount),
        balance_before: parseFloat(result.data) - parseFloat(amount),
        balance_after: parseFloat(result.data),
        status: 'completed'
      })
      
      await request(`${SUPABASE_URL}/rest/v1/recharge_records`, {
        method: 'POST',
        body: JSON.stringify({
          user_id: userId,
          amount: parseFloat(amount),
          payment_method: paymentMethod,
          status: 'completed'
        })
      })
      
      return { data: result.data }
    } catch (error) {
      console.error('充值失败:', error)
      return { error: error.message }
    }
  }
}

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
      const game = await request(`${SUPABASE_URL}/rest/v1/user_games`, {
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
      })
      return { data: game[0] }
    } catch (error) {
      console.error('添加游戏到库失败:', error)
      return { error: error.message }
    }
  },

  async activateGame(gameId) {
    try {
      const result = await request(`${SUPABASE_URL}/rest/v1/user_games?id=eq.${gameId}`, {
        method: 'PATCH',
        body: JSON.stringify({
          status: 'activated',
          activation_date: new Date().toISOString()
        }),
        headers: { 'Prefer': 'return=representation' }
      })
      return { data: result[0] }
    } catch (error) {
      console.error('激活游戏失败:', error)
      return { error: error.message }
    }
  }
}

export const announcementAPI = {
  async getAnnouncements(limit = 10) {
    try {
      const announcements = await request(
        `${SUPABASE_URL}/rest/v1/announcements?is_active=eq.true&order=publish_date.desc,is_top.desc&limit=${limit}`
      )
      return { data: announcements }
    } catch (error) {
      console.error('获取公告失败:', error)
      return { error: error.message, data: [] }
    }
  }
}

// SUPABASE_URL 已在第2行导出

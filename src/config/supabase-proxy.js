import { createClient } from '@supabase/supabase-js'

// 使用代理连接
const supabaseUrl = '/supabase'
const supabaseKey = 'sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_'

export const supabase = createClient(supabaseUrl, supabaseKey, {
  global: {
    headers: {
      'apikey': supabaseKey,
      'Authorization': `Bearer ${supabaseKey}`
    }
  },
  auth: {
    autoRefreshToken: false,
    persistSession: false
  }
})

// 用户认证相关
export const authAPI = {
  // 用户注册
  async register(userData) {
    try {
      // 检查用户名是否已存在
      const { data: existingUser } = await supabase
        .from('users')
        .select('username')
        .eq('username', userData.username)
        .single()
      
      if (existingUser) {
        return { error: '用户名已被注册' }
      }
      
      // 创建新用户
      const { data, error } = await supabase
        .from('users')
        .insert([{
          username: userData.username,
          password_hash: userData.password, // 实际项目中应该加密
          phone: userData.phone,
          nickname: userData.username,
          wallet_balance: 0,
          user_type: '普通用户'
        }])
        .select()
        .single()
      
      if (error) throw error
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  },

  // 用户登录
  async login(username, password) {
    try {
      // 先按用户名查询，再本地验证密码（避免 406 错误）
      const { data, error } = await supabase
        .from('users')
        .select('*')
        .eq('username', username)
        .single()
      
      if (error || !data) {
        return { error: '用户名或密码错误' }
      }
      
      // 本地验证密码
      if (data.password_hash !== password) {
        return { error: '用户名或密码错误' }
      }
      
      // 保存到sessionStorage
      sessionStorage.setItem('steampy_user', JSON.stringify(data))
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  },

  // 获取当前用户
  getCurrentUser() {
    const user = sessionStorage.getItem('steampy_user')
    return user ? JSON.parse(user) : null
  },

  // 退出登录
  logout() {
    sessionStorage.removeItem('steampy_user')
  },

  // 更新用户信息
  async updateUser(userId, updateData) {
    try {
      const { data, error } = await supabase
        .from('users')
        .update(updateData)
        .eq('id', userId)
        .select()
        .single()
      
      if (error) throw error
      
      // 更新sessionStorage
      sessionStorage.setItem('steampy_user', JSON.stringify(data))
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  }
}

// 游戏相关
export const gameAPI = {
  // 获取游戏列表
  async getGames(options = {}) {
    try {
      let query = supabase
        .from('games')
        .select('*')
      
      if (options.isPresale !== undefined) {
        query = query.eq('is_presale', options.isPresale)
      }
      
      if (options.limit) {
        query = query.limit(options.limit)
      }
      
      const { data, error } = await query.order('created_at', { ascending: false })
      
      if (error) throw error
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  },

  // 获取单个游戏
  async getGameById(gameId) {
    try {
      const { data, error } = await supabase
        .from('games')
        .select('*')
        .eq('id', gameId)
        .single()
      
      if (error) throw error
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  }
}

// 公告相关
export const announcementAPI = {
  // 获取公告列表
  async getAnnouncements(limit = 10) {
    try {
      const { data, error } = await supabase
        .from('announcements')
        .select('*')
        .eq('is_active', true)
        .order('publish_date', { ascending: false })
        .limit(limit)
      
      if (error) throw error
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  }
}

// 订单相关
export const orderAPI = {
  // 获取买家订单
  async getBuyerOrders(buyerId) {
    try {
      const { data, error } = await supabase
        .from('orders')
        .select(`
          *,
          game:game_id (name, image)
        `)
        .eq('buyer_id', buyerId)
        .order('created_at', { ascending: false })
      
      if (error) throw error
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  }
}

// 卖家额度相关
export const sellerAPI = {
  // 获取卖家额度
  async getSellerQuota(sellerId) {
    try {
      const { data, error } = await supabase
        .from('seller_quota')
        .select('*')
        .eq('seller_id', sellerId)
        .single()
      
      if (error) throw error
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  }
}
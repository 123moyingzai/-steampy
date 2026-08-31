// 使用原生 fetch API 实现 Supabase 操作
const SUPABASE_URL = 'https://prvmjufbhsofvnjeswhq.supabase.co'
const SUPABASE_KEY = 'sb_publishable_6IyTNcEoZNCFsnP_SmDnvQ_mtrBZ1t_'

const headers = {
  'apikey': SUPABASE_KEY,
  'Authorization': `Bearer ${SUPABASE_KEY}`,
  'Content-Type': 'application/json',
  'Prefer': 'return=representation'
}

// 用户认证相关
export const authAPI = {
  // 用户注册
  async register(userData) {
    try {
      // 检查用户名是否已存在
      const checkResponse = await fetch(
        `${SUPABASE_URL}/rest/v1/users?select=username&username=eq.${encodeURIComponent(userData.username)}`,
        {
          method: 'GET',
          headers: headers
        }
      )
      
      if (checkResponse.status === 200) {
        const existingUser = await checkResponse.json()
        if (existingUser.length > 0) {
          return { error: '用户名已被注册' }
        }
      }
      
      // 创建新用户
      const createResponse = await fetch(
        `${SUPABASE_URL}/rest/v1/users`,
        {
          method: 'POST',
          headers: headers,
          body: JSON.stringify({
            username: userData.username,
            password_hash: userData.password,
            phone: userData.phone,
            nickname: userData.username,
            wallet_balance: 0,
            user_type: '普通用户'
          })
        }
      )
      
      if (!createResponse.ok) {
        const errorData = await createResponse.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP 错误: ${createResponse.status}`)
      }
      
      const data = await createResponse.json()
      return { data: data[0] }
    } catch (error) {
      return { error: error.message }
    }
  },

  // 用户登录
  async login(username, password) {
    try {
      // 查询用户
      const response = await fetch(
        `${SUPABASE_URL}/rest/v1/users?select=*&username=eq.${encodeURIComponent(username)}`,
        {
          method: 'GET',
          headers: headers
        }
      )
      
      if (!response.ok) {
        if (response.status === 404) {
          return { error: '用户名或密码错误' }
        }
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP 错误: ${response.status}`)
      }
      
      const users = await response.json()
      if (users.length === 0) {
        return { error: '用户名或密码错误' }
      }
      
      const user = users[0]
      
      // 验证密码
      if (user.password_hash !== password) {
        return { error: '用户名或密码错误' }
      }
      
      // 保存到sessionStorage
      sessionStorage.setItem('steampy_user', JSON.stringify(user))
      return { data: user }
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
      const response = await fetch(
        `${SUPABASE_URL}/rest/v1/users?id=eq.${encodeURIComponent(userId)}`,
        {
          method: 'PATCH',
          headers: headers,
          body: JSON.stringify(updateData)
        }
      )
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP 错误: ${response.status}`)
      }
      
      const data = await response.json()
      const updatedUser = data[0]
      
      // 更新sessionStorage
      sessionStorage.setItem('steampy_user', JSON.stringify(updatedUser))
      return { data: updatedUser }
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
      let url = `${SUPABASE_URL}/rest/v1/games?select=*`
      
      if (options.isPresale !== undefined) {
        url += `&is_presale=eq.${options.isPresale}`
      }
      
      if (options.limit) {
        url += `&limit=${options.limit}`
      }
      
      url += `&order=created_at.desc`
      
      const response = await fetch(url, {
        method: 'GET',
        headers: headers
      })
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP 错误: ${response.status}`)
      }
      
      const data = await response.json()
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  },

  // 获取单个游戏
  async getGameById(gameId) {
    try {
      const response = await fetch(
        `${SUPABASE_URL}/rest/v1/games?select=*&id=eq.${encodeURIComponent(gameId)}`,
        {
          method: 'GET',
          headers: headers
        }
      )
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP 错误: ${response.status}`)
      }
      
      const data = await response.json()
      return { data: data[0] || null }
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
      const response = await fetch(
        `${SUPABASE_URL}/rest/v1/announcements?select=*&is_active=eq.true&order=publish_date.desc&limit=${limit}`,
        {
          method: 'GET',
          headers: headers
        }
      )
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `HTTP 错误: ${response.status}`)
      }
      
      const data = await response.json()
      return { data }
    } catch (error) {
      return { error: error.message }
    }
  }
}
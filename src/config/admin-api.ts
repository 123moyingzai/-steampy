// 管理员专用 API 模块
// 基于 supabase-local.ts 的 supabaseRequest 封装
import supabaseRequest, { authAPI } from './supabase-local'

// ============ 管理员鉴权（复用用户登录，仅做角色判断）============
export const adminAuthAPI = {
  /**
   * 判断当前登录用户是否为管理员
   */
  isAdminLoggedIn(): boolean {
    const isAdmin = sessionStorage.getItem('steampy_admin') === 'true'
    return isAdmin
  },

  /**
   * 设置管理员标记（由 Login.vue 在登录成功后调用）
   */
  setAdmin() {
    sessionStorage.setItem('steampy_admin', 'true')
  },

  /**
   * 清除管理员标记
   */
  clearAdmin() {
    sessionStorage.removeItem('steampy_admin')
  },

  /**
   * 管理员退出（同时清除普通用户和管理员标记）
   */
  logout() {
    sessionStorage.removeItem('steampy_admin')
    sessionStorage.removeItem('steampy_user')
  },

  /**
   * 获取当前管理员信息
   */
  getCurrentAdmin() {
    const user = sessionStorage.getItem('steampy_user')
    return user ? JSON.parse(user) : null
  }
}

// ============ 仪表盘统计 ============
export const adminStatsAPI = {
  /**
   * 获取所有统计数据
   */
  async getDashboardStats(): Promise<{
    totalUsers: number
    totalGames: number
    totalOrders: number
    totalAnnouncements: number
    totalRevenue: number
    recentOrders: any[]
  }> {
    try {
      const users = await supabaseRequest('users?select=id')
      const games = await supabaseRequest('games?select=id')
      const orders = await supabaseRequest('orders?select=*')
      const announcements = await supabaseRequest('announcements?select=id')

      // 计算总收入
      const totalRevenue = orders.reduce((sum: number, o: any) => {
        return sum + parseFloat(o.total_price || o.price || 0)
      }, 0)

      // 最近10条订单
      const recentOrders = orders
        .sort((a: any, b: any) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
        .slice(0, 10)

      return {
        totalUsers: users.length,
        totalGames: games.length,
        totalOrders: orders.length,
        totalAnnouncements: announcements.length,
        totalRevenue: Math.round(totalRevenue * 100) / 100,
        recentOrders
      }
    } catch (error: any) {
      console.error('获取统计数据失败:', error)
      return {
        totalUsers: 0,
        totalGames: 0,
        totalOrders: 0,
        totalAnnouncements: 0,
        totalRevenue: 0,
        recentOrders: []
      }
    }
  }
}

// ============ 用户管理 ============
export const adminUserAPI = {
  /** 获取所有用户 */
  async getUsers(): Promise<any[]> {
    try {
      return await supabaseRequest('users?select=*&order=created_at.desc')
    } catch (error: any) {
      console.error('获取用户列表失败:', error)
      return []
    }
  },

  /** 根据ID获取用户 */
  async getUserById(id: string): Promise<any> {
    try {
      const users = await supabaseRequest(`users?select=*&id=eq.${id}&limit=1`)
      return users[0] || null
    } catch (error: any) {
      console.error('获取用户详情失败:', error)
      return null
    }
  },

  /** 创建用户 */
  async createUser(userData: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest('users', {
        method: 'POST',
        body: JSON.stringify({
          ...userData,
          created_at: new Date().toISOString()
        })
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 更新用户 */
  async updateUser(id: string, updateData: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest(`users?id=eq.${id}`, {
        method: 'PATCH',
        body: JSON.stringify(updateData)
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 删除用户 */
  async deleteUser(id: string): Promise<{ error?: string }> {
    try {
      await supabaseRequest(`users?id=eq.${id}`, { method: 'DELETE' })
      return {}
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 修改用户角色 */
  async updateUserType(id: string, userType: string): Promise<{ data?: any; error?: string }> {
    return this.updateUser(id, { user_type: userType })
  }
}

// ============ 游戏管理 ============
export const adminGameAPI = {
  /** 获取所有游戏 */
  async getGames(): Promise<any[]> {
    try {
      return await supabaseRequest('games?select=*&order=name')
    } catch (error: any) {
      console.error('获取游戏列表失败:', error)
      return []
    }
  },

  /** 创建游戏 */
  async createGame(gameData: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest('games', {
        method: 'POST',
        body: JSON.stringify({
          ...gameData,
          created_at: new Date().toISOString()
        })
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 更新游戏 */
  async updateGame(id: string, updateData: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest(`games?id=eq.${id}`, {
        method: 'PATCH',
        body: JSON.stringify(updateData)
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 删除游戏 */
  async deleteGame(id: string): Promise<{ error?: string }> {
    try {
      await supabaseRequest(`games?id=eq.${id}`, { method: 'DELETE' })
      return {}
    } catch (error: any) {
      return { error: error.message }
    }
  }
}

// ============ 订单管理 ============
export const adminOrderAPI = {
  /** 获取所有订单 */
  async getOrders(): Promise<any[]> {
    try {
      return await supabaseRequest('orders?select=*&order=created_at.desc')
    } catch (error: any) {
      console.error('获取订单列表失败:', error)
      return []
    }
  },

  /** 更新订单状态 */
  async updateOrderStatus(id: string, status: string): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest(`orders?id=eq.${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ status })
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 删除订单 */
  async deleteOrder(id: string): Promise<{ error?: string }> {
    try {
      await supabaseRequest(`orders?id=eq.${id}`, { method: 'DELETE' })
      return {}
    } catch (error: any) {
      return { error: error.message }
    }
  }
}

// ============ 公告管理 ============
export const adminAnnouncementAPI = {
  /** 获取所有公告 */
  async getAnnouncements(): Promise<any[]> {
    try {
      return await supabaseRequest('announcements?select=*&order=publish_date.desc')
    } catch (error: any) {
      console.error('获取公告列表失败:', error)
      return []
    }
  },

  /** 创建公告 */
  async createAnnouncement(data: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest('announcements', {
        method: 'POST',
        body: JSON.stringify({
          title: data.title,
          content: data.content || '',
          publish_date: data.publish_date || new Date().toISOString(),
          is_top: data.is_top || false,
          is_active: data.is_active !== undefined ? data.is_active : true
        })
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 更新公告 */
  async updateAnnouncement(id: string, data: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest(`announcements?id=eq.${id}`, {
        method: 'PATCH',
        body: JSON.stringify(data)
      })
      return { data: result[0] }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 删除公告 */
  async deleteAnnouncement(id: string): Promise<{ error?: string }> {
    try {
      await supabaseRequest(`announcements?id=eq.${id}`, { method: 'DELETE' })
      return {}
    } catch (error: any) {
      return { error: error.message }
    }
  }
}

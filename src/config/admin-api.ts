// 管理员专用 API 模块
// 基于 supabase-local.ts 的 supabaseRequest 封装
import axios from 'axios'
import supabaseRequest, { authAPI } from './supabase-local'

// ============ 管理员鉴权（独立存储，不与普通用户 steampy_user 混用）============
export const adminAuthAPI = {
  isAdminLoggedIn(): boolean {
    return sessionStorage.getItem('steampy_admin') === 'true'
  },

  setAdmin() {
    sessionStorage.setItem('steampy_admin', 'true')
  },

  clearAdmin() {
    sessionStorage.removeItem('steampy_admin')
    sessionStorage.removeItem('steampy_admin_user')
  },

  logout() {
    sessionStorage.removeItem('steampy_admin')
    sessionStorage.removeItem('steampy_admin_user')
    // 管理员退出时如果 steampy_user 也是同一账号，也清掉（因为 admin 和 user 不能同时"半登录"）
    // 但如果是先登 user 再登 admin（不合法流程），steampy_user 已被覆盖成 admin 数据
    // 所以直接清 steampy_user 也安全 —— 管理员不应该同时是普通用户
    sessionStorage.removeItem('steampy_user')
  },

  getCurrentAdmin() {
    const u = sessionStorage.getItem('steampy_admin_user')
    return u ? JSON.parse(u) : null
  },

  setAdminUser(user: any) {
    sessionStorage.setItem('steampy_admin_user', JSON.stringify(user))
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
  /** 获取所有公告（含已停用，供管理端列表展示） */
  async getAnnouncements(): Promise<any[]> {
    try {
      return await supabaseRequest('/announcements/all')
    } catch (error: any) {
      console.error('获取公告列表失败:', error)
      return []
    }
  },

  /** 创建公告 */
  async createAnnouncement(data: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest('/announcements', 'POST', {
        title: data.title,
        content: data.content || '',
        publish_date: data.publish_date || new Date().toISOString(),
        is_top: data.is_top || false,
        is_active: data.is_active !== undefined ? data.is_active : true
      })
      return { data: result }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 更新公告 */
  async updateAnnouncement(id: string, data: any): Promise<{ data?: any; error?: string }> {
    try {
      const result = await supabaseRequest(`/announcements/${id}`, 'PUT', data)
      return { data: result }
    } catch (error: any) {
      return { error: error.message }
    }
  },

  /** 删除公告 */
  async deleteAnnouncement(id: string): Promise<{ error?: string }> {
    try {
      await supabaseRequest(`/announcements/${id}`, 'DELETE')
      return {}
    } catch (error: any) {
      return { error: error.message }
    }
  }
}

// ============ 数据导出（CSV）============
export const adminExportAPI = {
  /**
   * 下载管理后台 CSV 导出文件。
   *
   * 走后端 /api/admin/export/{type}（响应 text/csv + BOM），必须携带 X-Admin-Id 鉴权头。
   *
   * @param type   users / orders / games / withdrawals
   * @param params 可选筛选参数（如 keyword / role / status），与列表页筛选保持一致
   */
  async downloadCsv(
    type: 'users' | 'orders' | 'games' | 'withdrawals',
    params: Record<string, string> = {}
  ): Promise<void> {
    const entries = Object.entries(params).filter(([, v]) => v !== '' && v !== undefined && v !== null)
    const qs = new URLSearchParams(entries as [string, string][]).toString()
    const url = `/api/admin/export/${type}${qs ? '?' + qs : ''}`
    const admin = adminAuthAPI.getCurrentAdmin()

    const res = await axios.get(url, {
      responseType: 'blob',
      headers: admin?.id ? { 'X-Admin-Id': admin.id } : {}
    })

    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `steampy-${type}-${today}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
  }
}

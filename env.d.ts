/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface User {
  id: string
  username: string
  password_hash?: string
  nickname?: string
  phone?: string
  wallet_balance?: number
  user_type?: string
  created_at?: string
}

interface Game {
  id?: number | string
  name: string
  price: string
  originalPrice?: string
  discount?: string
  image: string
  description?: string
  link?: string
  region?: string
  type?: string
  image_url?: string
}

interface Order {
  id: string
  user_id: string
  game_name: string
  amount: number
  status: string
  seller_id?: string
  order_type?: string
  created_at?: string
  cdkey?: string
}

interface Transaction {
  id: string
  user_id: string
  type: string
  amount: number
  balance_after: number
  description: string
  created_at: string
}

interface Wallet {
  id: number
  user_id: string
  balance: number
}

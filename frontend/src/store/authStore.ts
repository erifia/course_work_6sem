import { create } from 'zustand'
import client from '../api/client'
import type { AuthResponse, RoleName, TokenPair, UserView } from '../types'

type AuthState = {
  accessToken: string | null
  refreshToken: string | null
  user: UserView | null
  role: RoleName | null

  hydrated: boolean
  login: (data: AuthResponse) => void
  setTokens: (tokens: TokenPair) => void
  logout: () => void
  refresh: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set, get) => {
  const raw = typeof window !== 'undefined' ? localStorage.getItem('auth') : null
  const parsed = raw ? (JSON.parse(raw) as Partial<AuthResponse> & { tokens?: TokenPair }) : null

  const accessToken = parsed?.tokens?.accessToken ?? null
  const refreshToken = parsed?.tokens?.refreshToken ?? null
  const user = parsed?.user ?? null
  const role = (user?.role as RoleName) ?? null

  return {
    accessToken,
    refreshToken,
    user,
    role,
    hydrated: true,

    login: (data) => {
      localStorage.setItem(
        'auth',
        JSON.stringify({
          tokens: data.tokens,
          user: data.user,
        }),
      )
      set({
        accessToken: data.tokens.accessToken,
        refreshToken: data.tokens.refreshToken,
        user: data.user,
        role: data.user.role,
      })
    },

    setTokens: (tokens) => {
      const existingRaw = localStorage.getItem('auth')
      const existing = existingRaw ? JSON.parse(existingRaw) : {}
      localStorage.setItem(
        'auth',
        JSON.stringify({
          ...existing,
          tokens,
        }),
      )
      set({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken })
    },

    logout: () => {
      localStorage.removeItem('auth')
      set({ accessToken: null, refreshToken: null, user: null, role: null })
    },

    refresh: async () => {
      const refreshToken = get().refreshToken
      if (!refreshToken) throw new Error('No refresh token')

      const resp = await client.post<AuthResponse>('/api/auth/refresh', {
        refreshToken,
      })

      get().login(resp.data)
    },
  }
})


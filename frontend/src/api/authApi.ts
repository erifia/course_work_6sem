import client from './client'
import type { AuthResponse } from '../types'

export function login(username: string, password: string) {
  return client.post<AuthResponse>('/api/auth/login', { username, password })
}

export function register(username: string, password: string, email: string, role?: string) {
  const payload: any = { username, password, email }
  if (role) payload.role = role
  return client.post<AuthResponse>('/api/auth/register', payload)
}


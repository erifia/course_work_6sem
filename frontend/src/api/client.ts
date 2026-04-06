import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const client = axios.create({
  baseURL: '',
})

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers = config.headers ?? {}
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

export async function refreshTokensIfNeeded() {
  const { refreshToken, refresh } = useAuthStore.getState()
  if (!refreshToken) return false
  try {
    await refresh()
    return true
  } catch {
    return false
  }
}

client.interceptors.response.use(
  (r) => r,
  async (err) => {
    const original = err?.config
    const status = err?.response?.status
    if (status === 401 && original && !original.__isRetry) {
      original.__isRetry = true
      const refreshed = await refreshTokensIfNeeded()
      if (refreshed) {
        original.headers['Authorization'] = `Bearer ${useAuthStore.getState().accessToken}`
        return client.request(original)
      }
    }
    return Promise.reject(err)
  },
)

export default client


import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { register as registerApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'
import type { RoleName } from '../types'

export default function RegisterPage() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<RoleName>('CLIENT')
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const resp = await registerApi(username, password, email, role)
      login(resp.data)
      navigate('/estates')
    } catch (err: any) {
      setError(err?.response?.data?.error ?? 'Ошибка регистрации')
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold">Регистрация</h1>
      <p className="text-sm text-black/60 mt-1">
        Роль администратора создаётся вручную через БД — выбрать её здесь нельзя.
      </p>

      <form onSubmit={onSubmit} className="space-y-3 mt-5">
        <label className="block">
          <span className="text-sm text-black/70">Логин</span>
          <input
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="например: user1"
          />
        </label>
        <label className="block">
          <span className="text-sm text-black/70">Email</span>
          <input
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="user1@example.com"
          />
        </label>
        <label className="block">
          <span className="text-sm text-black/70">Пароль</span>
          <input
            type="password"
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
          />
        </label>

        <label className="block">
          <span className="text-sm text-black/70">Роль</span>
          <select
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={role}
            onChange={(e) => setRole(e.target.value as RoleName)}
          >
            <option value="CLIENT">Покупатель (CLIENT)</option>
            <option value="APPRAISER">Оценщик (APPRAISER)</option>
          </select>
        </label>

        {error ? <div className="text-sm text-red-600">{error}</div> : null}
        <button
          type="submit"
          className="w-full rounded-xl bg-brand-900 text-white py-2 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
          disabled={!username || !email || !password}
        >
          Зарегистрироваться
        </button>
      </form>
    </div>
  )
}


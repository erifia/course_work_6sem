import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login as loginApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

export default function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const resp = await loginApi(username, password)
      login(resp.data)
      navigate('/estates')
    } catch (err: any) {
      setError(err?.response?.data?.error ?? 'Ошибка входа')
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold">Вход</h1>
      <p className="text-sm text-black/60 mt-1">Введите логин и пароль.</p>

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
          <span className="text-sm text-black/70">Пароль</span>
          <input
            type="password"
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
          />
        </label>
        {error ? <div className="text-sm text-red-600">{error}</div> : null}
        <button
          type="submit"
          className="w-full rounded-xl bg-brand-900 text-white py-2 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
          disabled={!username || !password}
        >
          Войти
        </button>
      </form>

      <p className="mt-4 text-sm text-black/60">
        Нет аккаунта?{' '}
        <a className="text-brand-900 hover:underline" href="/register">
          Регистрация
        </a>
      </p>
    </div>
  )
}


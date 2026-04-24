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
  const [activeHint, setActiveHint] = useState<'username' | 'email' | 'password' | null>(null)

  const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
  const loginOk = /^[A-Za-z0-9_.-]{3,30}$/.test(username)
  const passwordOk = /^(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/.test(password)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    if (!loginOk) {
      setError('Логин должен быть 3-30 символов и содержать только латиницу, цифры, _, ., -')
      return
    }
    if (!emailOk) {
      setError('Введите корректный email')
      return
    }
    if (!passwordOk) {
      setError('Пароль: минимум 8 символов, строчная буква, цифра и спецсимвол')
      return
    }
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

      <form onSubmit={onSubmit} className="space-y-3 mt-5">
        <label className="block relative">
          <div className="flex items-center gap-2">
            <span className="text-sm text-black/70">Логин</span>
            <span
              className="inline-flex h-5 w-5 items-center justify-center rounded-full border border-black/15 bg-white text-[11px] text-black/60 select-none cursor-help"
              onMouseEnter={() => setActiveHint('username')}
              onMouseLeave={() => setActiveHint(null)}
              aria-hidden
            >
              i
            </span>
          </div>
          <div
            className={`absolute left-0 -top-12 z-10 w-[320px] rounded-xl border border-black/10 bg-white px-3 py-2 text-[12px] text-black/70 shadow-lg transition ${
              activeHint === 'username' ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-1 pointer-events-none'
            }`}
          >
            Логин: только латиница/цифры, 3–30 символов, разрешены символы <b>_ . -</b>. Кириллица запрещена.
            <div className="absolute left-5 -bottom-2 h-3 w-3 rotate-45 border-r border-b border-black/10 bg-white" />
          </div>
          <input
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onFocus={() => setActiveHint('username')}
            onBlur={() => setActiveHint(null)}
            placeholder="например: user1"
            title="Логин: только латиница/цифры, 3-30 символов, без кириллицы."
          />
          {username && !loginOk ? <div className="text-xs text-red-600 mt-1">Некорректный логин</div> : null}
        </label>
        <label className="block relative">
          <div className="flex items-center gap-2">
            <span className="text-sm text-black/70">Email</span>
            <span
              className="inline-flex h-5 w-5 items-center justify-center rounded-full border border-black/15 bg-white text-[11px] text-black/60 select-none cursor-help"
              onMouseEnter={() => setActiveHint('email')}
              onMouseLeave={() => setActiveHint(null)}
              aria-hidden
            >
              i
            </span>
          </div>
          <div
            className={`absolute left-0 -top-12 z-10 w-[320px] rounded-xl border border-black/10 bg-white px-3 py-2 text-[12px] text-black/70 shadow-lg transition ${
              activeHint === 'email' ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-1 pointer-events-none'
            }`}
          >
            Введите действующий email в формате <b>name@example.com</b>.
            <div className="absolute left-5 -bottom-2 h-3 w-3 rotate-45 border-r border-b border-black/10 bg-white" />
          </div>
          <input
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onFocus={() => setActiveHint('email')}
            onBlur={() => setActiveHint(null)}
            placeholder="user1@example.com"
            title="Введите действующий email в формате name@example.com"
          />
          {email && !emailOk ? <div className="text-xs text-red-600 mt-1">Некорректный email</div> : null}
        </label>
        <label className="block relative">
          <div className="flex items-center gap-2">
            <span className="text-sm text-black/70">Пароль</span>
            <span
              className="inline-flex h-5 w-5 items-center justify-center rounded-full border border-black/15 bg-white text-[11px] text-black/60 select-none cursor-help"
              onMouseEnter={() => setActiveHint('password')}
              onMouseLeave={() => setActiveHint(null)}
              aria-hidden
            >
              i
            </span>
          </div>
          <div
            className={`absolute left-0 -top-14 z-10 w-[320px] rounded-xl border border-black/10 bg-white px-3 py-2 text-[12px] text-black/70 shadow-lg transition ${
              activeHint === 'password' ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-1 pointer-events-none'
            }`}
          >
            Минимум <b>8</b> символов, хотя бы <b>1 строчная буква</b>, <b>1 цифра</b> и <b>1 спецсимвол</b>.
            <div className="absolute left-5 -bottom-2 h-3 w-3 rotate-45 border-r border-b border-black/10 bg-white" />
          </div>
          <input
            type="password"
            className="mt-1 w-full rounded-xl border border-black/15 px-3 py-2 outline-none focus:border-brand-900 bg-white"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onFocus={() => setActiveHint('password')}
            onBlur={() => setActiveHint(null)}
            placeholder="••••••••"
            title="Минимум 8 символов, хотя бы 1 строчная буква, 1 цифра и 1 спецсимвол."
          />
          {password && !passwordOk ? (
            <div className="text-xs text-red-600 mt-1">Пароль не соответствует требованиям безопасности</div>
          ) : null}
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
          disabled={!username || !email || !password || !emailOk || !loginOk || !passwordOk}
        >
          Зарегистрироваться
        </button>
      </form>
    </div>
  )
}


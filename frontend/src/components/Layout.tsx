import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

export default function Layout({ children }: { children: React.ReactNode }) {
  const logout = useAuthStore((s) => s.logout)
  const role = useAuthStore((s) => s.role)
  const accessToken = useAuthStore((s) => s.accessToken)
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-brand-800 text-brand-900">
      <header className="bg-brand-800/70 backdrop-blur border-b border-black/5">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="font-semibold tracking-wide">
            <span className="text-brand-900">Оценка недвижимости</span>
          </div>

          <nav className="flex items-center gap-4 text-sm">
            <NavLink
              to="/"
              className={({ isActive }) =>
                isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
              }
            >
              Главная
            </NavLink>

            {accessToken ? (
              <>
                <NavLink
                  to="/estates"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Объекты
                </NavLink>
                <NavLink
                  to="/recommendations"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Подбор
                </NavLink>
                <NavLink
                  to="/search"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Поиск
                </NavLink>
                <NavLink
                  to="/evaluations"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Оценки
                </NavLink>
                {role === 'ADMIN' ? (
                  <NavLink
                    to="/admin"
                    className={({ isActive }) =>
                      isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                    }
                  >
                    Админ
                  </NavLink>
                ) : null}
                <NavLink
                  to="/profile"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Кабинет
                </NavLink>
                {role ? <span className="text-brand-600/80">({role})</span> : null}
                <button
                  className="text-brand-600 hover:text-brand-900"
                  onClick={() => {
                    logout()
                    navigate('/login')
                  }}
                >
                  Выход
                </button>
              </>
            ) : (
              <>
                <NavLink
                  to="/login"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Вход
                </NavLink>
                <NavLink
                  to="/register"
                  className={({ isActive }) =>
                    isActive ? 'text-brand-900 font-semibold' : 'text-brand-600 hover:text-brand-900'
                  }
                >
                  Регистрация
                </NavLink>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6">{children}</main>
    </div>
  )
}


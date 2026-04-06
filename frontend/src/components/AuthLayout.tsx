import React from 'react'
import { NavLink } from 'react-router-dom'

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-brand-800 text-brand-900">
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between">
          <NavLink to="/" className="font-semibold text-brand-900">
            Оценка недвижимости
          </NavLink>
          <div className="text-sm text-brand-600">Вход в систему</div>
        </div>

        <div className="mt-10 grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
          <div className="hidden lg:block">
            <div className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur p-8">
              <div className="text-sm text-black/60">Курсовой проект</div>
              <h1 className="text-3xl font-semibold mt-2 leading-tight">
                Оценка и подбор объектов недвижимости
              </h1>
              <p className="text-black/60 mt-4">
                Поиск, фильтры, оценки, отзывы, рекомендации - в одном приложении.
              </p>
              <div className="mt-6 grid grid-cols-2 gap-3 text-sm">
                <div className="rounded-2xl border border-black/10 bg-white p-4">
                  <div className="font-semibold">Покупатель</div>
                  <div className="text-black/60 mt-1">Подбор и отзывы</div>
                </div>
                <div className="rounded-2xl border border-black/10 bg-white p-4">
                  <div className="font-semibold">Оценщик</div>
                  <div className="text-black/60 mt-1">Оценки и отчёты</div>
                </div>
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur p-6 shadow-[0_30px_80px_rgba(10,31,68,0.10)]">
            {children}
          </div>
        </div>
      </div>
    </div>
  )
}


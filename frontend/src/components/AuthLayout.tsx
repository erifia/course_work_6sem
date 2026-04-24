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
          <div className="text-sm text-brand-600">Secure access</div>
        </div>

        <div className="mt-10 grid grid-cols-1 lg:grid-cols-2 gap-6 items-center">
          <div className="hidden lg:block">
            <div className="rounded-3xl border border-black/10 bg-white/75 backdrop-blur p-8 shadow-[0_25px_70px_rgba(10,31,68,0.10)]">
              <h1 className="text-3xl font-semibold leading-tight">Платформа оценки недвижимости</h1>
              <p className="text-black/60 mt-4">Оценка стоимости, подбор объектов и аналитика в едином интерфейсе.</p>
            </div>
          </div>

          <div className="rounded-3xl border border-black/10 bg-white/80 backdrop-blur p-6 shadow-[0_30px_80px_rgba(10,31,68,0.12)]">
            {children}
          </div>
        </div>
      </div>
    </div>
  )
}


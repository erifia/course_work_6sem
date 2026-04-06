import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import client from '../api/client'
import type { EstateResponse } from '../types'

type CurrencyWidget = {
  target: string
  rate: number
}
const FALLBACK_IMAGE = 'https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=1200&q=80'

export default function HomePage() {
  const role = useAuthStore((s) => s.role)
  const token = useAuthStore((s) => s.accessToken)

  const [featured, setFeatured] = useState<EstateResponse[]>([])
  const [currentIdx, setCurrentIdx] = useState(0)
  const [currency, setCurrency] = useState<CurrencyWidget | null>(null)
  const [currencyBoard, setCurrencyBoard] = useState<Array<{ code: string; rate: number }>>([])
  const [weather, setWeather] = useState<number | null>(null)
  const [map, setMap] = useState<{ lat: number; lon: number } | null>(null)
  const [mapQuery, setMapQuery] = useState('Минск, Беларусь')
  const [apiErrors, setApiErrors] = useState<string[]>([])

  useEffect(() => {
    ;(async () => {
      try {
        const estatesResp = await client.get('/api/real-estate', { params: { page: 0, size: 5 } })
        setFeatured(estatesResp.data.content ?? [])
      } catch {
        // игнорируем, просто не покажем карусель
      }

      try {
        const rateResp = await client.get('/api/external/currency', { params: { target: 'EUR' } })
        setCurrency(rateResp.data.data)
        const desired = ['BYN', 'RUB', 'CNY', 'PLN']
        const rates = await Promise.all(
          desired.map(async (code) => {
            const r = await client.get('/api/external/currency', { params: { target: code } })
            return { code, rate: Number(r.data?.data?.rate ?? 0) }
          }),
        )
        setCurrencyBoard(rates.filter((x) => Number.isFinite(x.rate) && x.rate > 0))
      } catch (e: any) {
        setApiErrors((p) => [...p, e?.response?.data?.error ?? 'Курс валют недоступен'])
      }
      try {
        const geo = await client.get('/api/external/geocode', { params: { address: 'Минск, Беларусь' } })
        const lat = Number(geo.data?.data?.lat)
        const lon = Number(geo.data?.data?.lon)
        if (Number.isFinite(lat) && Number.isFinite(lon)) {
          setMap({ lat, lon })
          const w = await client.get('/api/external/weather', { params: { lat, lon } })
          const t = Number(w.data?.data?.temperature2m)
          setWeather(Number.isFinite(t) ? t : null)
        }
      } catch (e: any) {
        setApiErrors((p) => [...p, e?.response?.data?.error ?? 'Геокодирование/погода недоступны'])
      }
    })()
  }, [])

  async function searchOnMap() {
    try {
      const geo = await client.get('/api/external/geocode', { params: { address: mapQuery } })
      const lat = Number(geo.data?.data?.lat)
      const lon = Number(geo.data?.data?.lon)
      if (Number.isFinite(lat) && Number.isFinite(lon)) {
        setMap({ lat, lon })
      }
    } catch (e: any) {
      setApiErrors((p) => [...p, e?.response?.data?.error ?? 'Поиск на карте недоступен'])
    }
  }

  useEffect(() => {
    if (!featured.length) return
    const id = setInterval(() => {
      setCurrentIdx((i) => (i + 1) % featured.length)
    }, 5000)
    return () => clearInterval(id)
  }, [featured])

  return (
    <div className="space-y-4">
      <div className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur p-6 shadow-[0_30px_80px_rgba(10,31,68,0.08)]">
        <div className="text-xs tracking-[0.35em] text-brand-600/80">ВОЗМОЖНОСТИ СИСТЕМЫ</div>
        <h1 className="text-4xl font-semibold mt-2 leading-tight">Что мы предлагаем</h1>
        <p className="text-brand-600 mt-3">
          Поиск и фильтрация объектов, расчёт стоимости, сравнение, прогноз, отзывы и рекомендации.
        </p>

        <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="rounded-3xl border border-black/10 bg-white p-5 transition hover:-translate-y-1 hover:shadow-lg">
            <div className="text-sm text-brand-600">Оценка</div>
            <div className="font-semibold mt-1">Расчёт стоимости</div>
            <div className="text-sm text-brand-600 mt-2">
              Автоматический расчёт стоимости на основе средних цен по районам, спроса и состояния.
            </div>
          </div>
          <div className="rounded-3xl border border-black/10 bg-white p-5 transition hover:-translate-y-1 hover:shadow-lg">
            <div className="text-sm text-brand-600">Подбор</div>
            <div className="font-semibold mt-1">Поиск квартир</div>
            <div className="text-sm text-brand-600 mt-2">
              Умный подбор по критериям с рекомендациями и списком подходящих объектов.
            </div>
          </div>
          <div className="rounded-3xl border border-black/10 bg-white p-5 transition hover:-translate-y-1 hover:shadow-lg">
            <div className="text-sm text-brand-600">Аналитика</div>
            <div className="font-semibold mt-1">Рыночные данные</div>
            <div className="text-sm text-brand-600 mt-2">
              Сводная статистика по объектам и оценкам, а также показатели по районам.
            </div>
          </div>
        </div>

        {!token ? (
          <div className="mt-6 flex flex-wrap gap-2">
            <NavLink
              to="/login"
              className="rounded-2xl bg-brand-900 text-white px-5 py-3 font-semibold hover:bg-brand-900/90"
            >
              Войти
            </NavLink>
            <NavLink
              to="/register"
              className="rounded-2xl border border-black/15 px-5 py-3 font-semibold hover:bg-black/5 bg-white/60"
            >
              Регистрация
            </NavLink>
          </div>
        ) : (
          <div className="mt-5 grid grid-cols-1 md:grid-cols-2 gap-3">
            <NavLink to="/estates" className="rounded-2xl border border-black/10 bg-white p-4 hover:bg-black/5">
              <div className="font-semibold">Объекты</div>
              <div className="text-sm text-black/60 mt-1">Список, оценка, отзывы</div>
            </NavLink>
            <NavLink to="/recommendations" className="rounded-2xl border border-black/10 bg-white p-4 hover:bg-black/5">
              <div className="font-semibold">Подбор</div>
              <div className="text-sm text-black/60 mt-1">Предпочтения и рекомендации</div>
            </NavLink>
            <NavLink to="/search" className="rounded-2xl border border-black/10 bg-white p-4 hover:bg-black/5">
              <div className="font-semibold">Поиск</div>
              <div className="text-sm text-black/60 mt-1">Фильтры и пагинация</div>
            </NavLink>
            <NavLink to="/profile" className="rounded-2xl border border-black/10 bg-white p-4 hover:bg-black/5">
              <div className="font-semibold">Личный кабинет</div>
              <div className="text-sm text-black/60 mt-1">Профиль, мои оценки, PDF</div>
            </NavLink>
            {role === 'ADMIN' ? (
              <NavLink to="/admin" className="rounded-2xl border border-black/10 bg-white p-4 hover:bg-black/5 md:col-span-2">
                <div className="font-semibold">Админ‑панель</div>
                <div className="text-sm text-black/60 mt-1">Пользователи, роли, статистика, отчёт</div>
              </NavLink>
            ) : null}
          </div>
        )}
      </div>

      {apiErrors.length ? (
        <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Ошибки внешних API: {apiErrors.join(' | ')}
        </div>
      ) : null}

      {/* Карусель объектов + курс валют */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 rounded-3xl border border-black/10 bg-brand-100 p-4 md:p-6">
          <div className="flex items-center justify-between mb-3">
            <div>
              <div className="text-xs text-brand-600 uppercase tracking-[0.2em]">Живые объекты</div>
              <div className="text-xl font-semibold mt-1">Квартиры из базы</div>
            </div>
            {featured.length > 1 ? (
              <div className="flex gap-2">
                <button
                  className="h-8 w-8 rounded-full border border-brand-500/40 text-sm"
                  onClick={() =>
                    setCurrentIdx((i) => (i - 1 + featured.length) % featured.length)
                  }
                >
                  ‹
                </button>
                <button
                  className="h-8 w-8 rounded-full border border-brand-500/40 text-sm"
                  onClick={() => setCurrentIdx((i) => (i + 1) % featured.length)}
                >
                  ›
                </button>
              </div>
            ) : null}
          </div>
          {featured.length ? (
            <div className="rounded-3xl bg-white shadow-md overflow-hidden transition-all duration-500 ease-out">
              <div className="aspect-[16/9] bg-brand-700/30">
                <img
                  src={featured[currentIdx].imagePath || FALLBACK_IMAGE}
                  alt={featured[currentIdx].address}
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="p-4">
                <div className="font-semibold text-lg">{featured[currentIdx].address}</div>
                <div className="text-sm text-brand-600 mt-1">
                  {featured[currentIdx].districtName ?? 'Район не указан'} /{' '}
                  {featured[currentIdx].rooms} комн / {featured[currentIdx].area} м²
                </div>
                <div className="mt-2 font-semibold text-brand-900">
                  {new Intl.NumberFormat('ru-RU').format(featured[currentIdx].price)} USD
                </div>
              </div>
            </div>
          ) : (
            <div className="text-sm text-brand-600">
              Добавьте несколько объектов недвижимости, чтобы увидеть их здесь.
            </div>
          )}
        </div>

        <div className="rounded-3xl border border-black/10 bg-white/80 p-4 flex flex-col justify-between">
          <div>
            <div className="text-xs text-brand-600 uppercase tracking-[0.2em]">Курс валют</div>
            <div className="text-xl font-semibold mt-1">USD → {currency?.target ?? 'EUR'}</div>
            <div className="mt-2 text-sm text-brand-600">
              Все расчёты выполняются в USD, при
              необходимости конвертация делается по текущему курсу.
            </div>
          </div>
          <div className="mt-4 text-3xl font-semibold">
            {currency ? currency.rate.toFixed(3) : '—'}{' '}
            <span className="text-base text-brand-600">за 1 USD</span>
          </div>
          {currencyBoard.length ? (
            <div className="mt-3 grid grid-cols-2 gap-2 text-sm">
              {currencyBoard.map((c) => (
                <div key={c.code} className="rounded border border-black/10 px-2 py-1">
                  USD {'->'} {c.code}: <span className="font-semibold">{c.rate.toFixed(3)}</span>
                </div>
              ))}
            </div>
          ) : null}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="rounded-3xl border border-black/10 bg-white/80 p-4">
          <h3 className="text-lg font-semibold mb-2">Погода</h3>
          <div className="text-sm text-black/60">Минск</div>
          <div className="text-3xl font-semibold mt-2">{weather != null ? `${weather} °C` : '—'}</div>
        </div>
        <div className="rounded-3xl border border-black/10 bg-white/80 p-2">
          <h3 className="text-lg font-semibold px-2 pt-2">Карта района</h3>
          <div className="px-2 pt-2 flex gap-2">
            <input
              className="flex-1 rounded border border-black/20 px-3 py-2 text-sm"
              value={mapQuery}
              onChange={(e) => setMapQuery(e.target.value)}
              placeholder="Введите улицу или адрес"
            />
            <button className="rounded bg-brand-900 text-white px-3 py-2 text-sm" onClick={() => void searchOnMap()}>
              Найти
            </button>
          </div>
          {map ? (
            <iframe
              title="Карта"
              className="w-full h-64 rounded-2xl mt-2 border border-black/10"
              src={`https://www.openstreetmap.org/export/embed.html?bbox=${map.lon - 0.03}%2C${map.lat - 0.02}%2C${map.lon + 0.03}%2C${map.lat + 0.02}&layer=mapnik&marker=${map.lat}%2C${map.lon}`}
            />
          ) : (
            <div className="h-64 grid place-items-center text-sm text-black/60">Карта недоступна</div>
          )}
        </div>
      </div>
    </div>
  )
}


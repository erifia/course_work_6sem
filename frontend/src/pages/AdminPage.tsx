import { useEffect, useMemo, useState } from 'react'
import client from '../api/client'
import { useAuthStore } from '../store/authStore'
import { useNavigate } from 'react-router-dom'
import type {
  AdminStatsResponse,
  DistrictResponse,
  EstateResponse,
  RoleName,
  UserSummaryResponse,
} from '../types'

export default function AdminPage() {
  const navigate = useNavigate()
  const role = useAuthStore((s) => s.role)
  const user = useAuthStore((s) => s.user)

  const [tab, setTab] = useState<'estates' | 'estatesTable' | 'districts' | 'users' | 'analytics' | 'reports'>('estates')
  const [districts, setDistricts] = useState<DistrictResponse[]>([])
  const [users, setUsers] = useState<UserSummaryResponse[]>([])
  const [estates, setEstates] = useState<EstateResponse[]>([])
  const [stats, setStats] = useState<AdminStatsResponse | null>(null)

  const [name, setName] = useState('')
  const [avgPrice, setAvgPrice] = useState('')
  const [demandLevel, setDemandLevel] = useState('5')
  const [editingDistrictId, setEditingDistrictId] = useState<number | null>(null)

  const [error, setError] = useState<string | null>(null)
  const [loadingDistricts, setLoadingDistricts] = useState(false)
  const [loadingUsers, setLoadingUsers] = useState(false)
  const [loadingStats, setLoadingStats] = useState(false)
  const [loadingEstates, setLoadingEstates] = useState(false)
  const [changingUserId, setChangingUserId] = useState<number | null>(null)
  const [downloadingPdf, setDownloadingPdf] = useState(false)

  async function loadDistricts() {
    setLoadingDistricts(true)
    try {
      const resp = await client.get<DistrictResponse[]>('/api/districts')
      setDistricts(resp.data)
    } finally {
      setLoadingDistricts(false)
    }
  }

  async function loadUsers() {
    setLoadingUsers(true)
    try {
      const resp = await client.get<UserSummaryResponse[]>('/api/admin/users')
      setUsers(resp.data)
    } finally {
      setLoadingUsers(false)
    }
  }

  async function loadEstates() {
    setLoadingEstates(true)
    try {
      const resp = await client.get('/api/real-estate', { params: { page: 0, size: 12 } })
      setEstates(resp.data.content ?? [])
    } finally {
      setLoadingEstates(false)
    }
  }

  async function loadStats() {
    setLoadingStats(true)
    try {
      const resp = await client.get<AdminStatsResponse>('/api/admin/stats')
      setStats(resp.data)
    } finally {
      setLoadingStats(false)
    }
  }

  useEffect(() => {
    setError(null)
    ;(async () => {
      try {
        await Promise.all([loadDistricts(), loadUsers(), loadStats(), loadEstates()])
      } catch (e: any) {
        setError(e?.response?.data?.error ?? 'Не удалось загрузить данные админа')
      }
    })()
  }, [])

  async function createDistrict() {
    setError(null)
    if (!name.trim()) {
      setError('Введите название района')
      return
    }
    if (!avgPrice || Number(avgPrice) <= 0) {
      setError('Введите корректную среднюю цену района (USD)')
      return
    }
    if (!demandLevel || Number(demandLevel) < 1 || Number(demandLevel) > 10) {
      setError('Спрос должен быть от 1 до 10')
      return
    }
    setLoadingDistricts(true)
    try {
      await client.post('/api/districts', {
        districtName: name.trim(),
        avgPrice: Number(avgPrice),
        demandLevel: Number(demandLevel),
      })
      setName('')
      setAvgPrice('')
      setDemandLevel('5')
      await loadDistricts()
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка создания района')
    } finally {
      setLoadingDistricts(false)
    }
  }

  function startEditDistrict(d: DistrictResponse) {
    setEditingDistrictId(d.id)
    setName(d.districtName)
    setAvgPrice(d.avgPrice != null ? String(d.avgPrice) : '')
    setDemandLevel(d.demandLevel != null ? String(d.demandLevel) : '5')
  }

  function cancelEditDistrict() {
    setEditingDistrictId(null)
    setName('')
    setAvgPrice('')
    setDemandLevel('5')
  }

  async function saveDistrictEdit() {
    if (editingDistrictId == null) return
    setError(null)
    if (!name.trim()) {
      setError('Введите название района')
      return
    }
    if (!avgPrice || Number(avgPrice) <= 0) {
      setError('Введите корректную среднюю цену района (USD)')
      return
    }
    if (!demandLevel || Number(demandLevel) < 1 || Number(demandLevel) > 10) {
      setError('Спрос должен быть от 1 до 10')
      return
    }
    setLoadingDistricts(true)
    try {
      await client.put(`/api/districts/${editingDistrictId}`, {
        districtName: name.trim(),
        avgPrice: Number(avgPrice),
        demandLevel: Number(demandLevel),
      })
      cancelEditDistrict()
      await loadDistricts()
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка обновления района')
    } finally {
      setLoadingDistricts(false)
    }
  }

  async function changeRole(userId: number, nextRole: RoleName) {
    setError(null)
    setChangingUserId(userId)
    try {
      await client.patch(`/api/admin/users/${userId}/role`, null, { params: { role: nextRole } })
      await loadUsers()
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка смены роли')
    } finally {
      setChangingUserId(null)
    }
  }

  async function downloadAdminPdf() {
    setError(null)
    setDownloadingPdf(true)
    try {
      const resp = await client.get('/api/admin/report/pdf', { responseType: 'blob' })
      const blob = resp.data as Blob
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `admin-report.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка скачивания PDF')
    } finally {
      setDownloadingPdf(false)
    }
  }

  const maxPrice = useMemo(() => {
    return Math.max(...districts.map((d) => Number(d.avgPrice ?? 0)), 1)
  }, [districts])

  const maxDemand = useMemo(() => {
    return Math.max(...districts.map((d) => Number(d.demandLevel ?? 0)), 1)
  }, [districts])

  function toDonutSegments(items: Array<{ label: string; value: number; color: string }>) {
    const norm = items
      .map((x) => ({ ...x, value: Math.max(0, x.value) }))
      .filter((x) => x.value > 0)
    const normTotal = norm.reduce((s, x) => s + x.value, 0) || 1
    let offset = 25 // start at 12 o'clock
    return norm.map((x) => {
      const pct = (x.value / normTotal) * 100
      const seg = { ...x, pct, dash: `${pct} ${100 - pct}`, offset }
      offset -= pct
      return seg
    })
  }

  if (role !== 'ADMIN') {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">
        Доступно только для администратора.
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="rounded-3xl border border-black/10 bg-white/70 p-4 md:p-6 shadow-[0_24px_60px_rgba(10,31,68,0.08)]">
        <h1 className="text-3xl md:text-5xl font-semibold text-center">Панель управления администратора</h1>
        <div className="mt-5 flex flex-wrap gap-2">
          {[
            { id: 'estates', label: 'Управление недвижимостью' },
            { id: 'estatesTable', label: 'Все объекты (таблица)' },
            { id: 'districts', label: 'Управление районами' },
            { id: 'users', label: 'Управление пользователями' },
            { id: 'analytics', label: 'Аналитика' },
            { id: 'reports', label: 'Отчёты' },
          ].map((item) => (
            <button
              key={item.id}
              onClick={() => setTab(item.id as typeof tab)}
              className={`rounded-full px-5 py-2 text-sm font-semibold transition ${
                tab === item.id
                  ? 'bg-brand-900 text-white shadow'
                  : 'bg-brand-700/60 text-brand-900 hover:bg-brand-700'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {error ? <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-red-700 text-sm">{error}</div> : null}

      {tab === 'estates' ? (
        <div className="rounded-3xl border border-black/10 bg-white/70 p-4 md:p-6">
          <div className="flex items-center justify-between gap-3 mb-4">
            <h2 className="text-2xl font-semibold text-brand-500">Управление квартирами</h2>
            <button
              className="rounded-xl bg-brand-500/90 hover:bg-brand-500 px-5 py-2 font-semibold text-brand-900"
              onClick={() => navigate('/estates')}
            >
              + Добавить квартиру
            </button>
          </div>
          {loadingEstates ? <div className="text-sm text-brand-600">Загрузка...</div> : null}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {estates.map((e) => (
              <div key={e.id} className="rounded-3xl border border-black/10 bg-white p-4">
                <div className="aspect-[16/10] rounded-2xl bg-brand-700/40 mb-3 overflow-hidden">
                  {e.imagePath ? (
                    <img src={e.imagePath} alt={e.address} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full grid place-items-center text-brand-600 text-sm">Без изображения</div>
                  )}
                </div>
                <div className="font-semibold text-xl">{e.address}</div>
                <div className="text-sm text-brand-600 mt-2">Район: {e.districtName ?? 'Не указан'}</div>
                <div className="text-sm mt-1">Цена: {new Intl.NumberFormat('ru-RU').format(Number(e.price))} USD</div>
                <div className="text-sm mt-1">Площадь: {e.area} м²</div>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {tab === 'districts' ? (
        <div className="rounded-3xl border border-black/10 bg-white/70 p-4 md:p-6">
          <h2 className="text-2xl font-semibold text-brand-500 mb-4">Управление районами</h2>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <label className="block">
              <span className="text-sm text-black/60">Название района</span>
              <input
                className="mt-1 rounded-xl border border-black/20 px-3 py-2 bg-white w-full"
                placeholder="Напр. Центральный"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Средняя цена (USD)</span>
              <input
                type="number"
                min="1"
                step="0.01"
                className="mt-1 rounded-xl border border-black/20 px-3 py-2 bg-white w-full"
                placeholder="Напр. 1500"
                value={avgPrice}
                onChange={(e) => setAvgPrice(e.target.value)}
              />
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Спрос (1..10)</span>
              <input
                type="number"
                min="1"
                max="10"
                className="mt-1 rounded-xl border border-black/20 px-3 py-2 bg-white w-full"
                placeholder="5"
                value={demandLevel}
                onChange={(e) => setDemandLevel(e.target.value)}
              />
            </label>
            <button
              className="rounded-xl bg-brand-900 text-white py-2 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
              disabled={loadingDistricts || !name.trim()}
              onClick={() => void (editingDistrictId == null ? createDistrict() : saveDistrictEdit())}
            >
              {loadingDistricts ? 'Сохраняем...' : editingDistrictId == null ? 'Создать' : 'Сохранить'}
            </button>
            {editingDistrictId != null ? (
              <button
                className="rounded-xl border border-black/20 bg-white py-2 font-semibold hover:bg-black/5"
                onClick={cancelEditDistrict}
                disabled={loadingDistricts}
              >
                Отмена
              </button>
            ) : null}
          </div>

          <div className="mt-4 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3">
            {districts.map((d) => (
              <div key={d.id} className="rounded-2xl border border-black/10 bg-white p-4">
                <div className="flex items-start justify-between gap-2">
                  <div className="font-semibold">{d.districtName}</div>
                  <button
                    className="rounded-lg border border-black/15 px-2 py-1 text-xs hover:bg-black/5"
                    title="Редактировать район"
                    onClick={() => startEditDistrict(d)}
                  >
                    ✏️
                  </button>
                </div>
                <div className="text-sm text-brand-600 mt-1">
                  Средняя цена: {d.avgPrice ?? 'не задана'}, спрос: {d.demandLevel ?? 'не задан'}
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {tab === 'users' ? (
        <div className="rounded-3xl border border-black/10 bg-white/70 p-4 md:p-6">
          <h2 className="text-2xl font-semibold text-brand-500 mb-4">Управление пользователями</h2>
          {loadingUsers ? <div className="text-sm text-brand-600">Загрузка...</div> : null}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {users.map((u) => (
              <div key={u.id} className="rounded-2xl border border-black/10 bg-white p-4">
                <div className="flex items-start justify-between gap-2">
                  <div className="font-semibold text-xl">{u.username}</div>
                  <select
                    className="rounded-lg border border-brand-500/40 px-2 py-1 outline-none bg-brand-100 text-sm"
                    value={u.role}
                    onChange={(e) => void changeRole(u.id, e.target.value as RoleName)}
                    disabled={changingUserId === u.id}
                  >
                    <option value="CLIENT">CLIENT</option>
                    <option value="APPRAISER">APPRAISER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
                <div className="text-sm mt-2">
                  <span className="font-semibold">Email:</span> {u.email}
                </div>
                <div className="text-sm mt-1">
                  <span className="font-semibold">Роль:</span> {u.role}
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {tab === 'analytics' ? (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="rounded-2xl border border-black/10 bg-white p-4">
              <div className="text-sm text-brand-600">Пользователи</div>
              <div className="text-3xl font-semibold mt-1">{loadingStats ? '…' : stats?.usersCount ?? '—'}</div>
            </div>
            <div className="rounded-2xl border border-black/10 bg-white p-4">
              <div className="text-sm text-brand-600">Объекты</div>
              <div className="text-3xl font-semibold mt-1">{loadingStats ? '…' : stats?.estatesCount ?? '—'}</div>
            </div>
            <div className="rounded-2xl border border-black/10 bg-white p-4">
              <div className="text-sm text-brand-600">Оценки</div>
              <div className="text-3xl font-semibold mt-1">{loadingStats ? '…' : stats?.evaluationsCount ?? '—'}</div>
            </div>
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
            <div className="rounded-3xl border border-black/10 bg-white/70 p-5">
              <h3 className="text-xl font-semibold text-center text-brand-500 mb-4">Распределение цен по районам</h3>
              <div className="space-y-2">
                {districts.map((d) => {
                  const value = Number(d.avgPrice ?? 0)
                  const width = Math.max(6, (value / maxPrice) * 100)
                  return (
                    <div key={d.id}>
                      <div className="text-xs text-brand-600 mb-1">{d.districtName}</div>
                      <div className="h-7 rounded-lg bg-brand-700/40 overflow-hidden">
                        <div className="h-full bg-brand-500/70 flex items-center px-2 text-xs font-semibold" style={{ width: `${width}%` }}>
                          {new Intl.NumberFormat('ru-RU').format(value)} USD
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>

            <div className="rounded-3xl border border-black/10 bg-white/70 p-5">
              <h3 className="text-xl font-semibold text-center text-brand-500 mb-4">Уровни спроса по районам</h3>
              <div className="space-y-3">
                {districts.map((d) => {
                  const v = Number(d.demandLevel ?? 0)
                  const width = Math.max(6, (v / maxDemand) * 100)
                  return (
                    <div key={d.id}>
                      <div className="flex items-center justify-between text-xs text-brand-600 mb-1">
                        <span>{d.districtName}</span>
                        <span>{v}</span>
                      </div>
                      <div className="h-2 rounded-full bg-brand-700/40">
                        <div className="h-2 rounded-full bg-brand-500" style={{ width: `${width}%` }} />
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
            {(() => {
              const demandBuckets = [
                {
                  label: 'Низкий (1–3)',
                  value: districts.filter((d) => Number(d.demandLevel ?? 0) >= 1 && Number(d.demandLevel ?? 0) <= 3).length,
                  color: '#C7A87A',
                },
                {
                  label: 'Средний (4–7)',
                  value: districts.filter((d) => Number(d.demandLevel ?? 0) >= 4 && Number(d.demandLevel ?? 0) <= 7).length,
                  color: '#88A9D8',
                },
                {
                  label: 'Высокий (8–10)',
                  value: districts.filter((d) => Number(d.demandLevel ?? 0) >= 8 && Number(d.demandLevel ?? 0) <= 10).length,
                  color: '#143A66',
                },
              ]
              const demandSeg = toDonutSegments(demandBuckets)

              const prices = districts.map((d) => Number(d.avgPrice ?? 0)).filter((x) => x > 0)
              const priceBuckets = [
                { label: '< 1000', value: prices.filter((x) => x > 0 && x < 1000).length, color: '#E9D8BC' },
                { label: '1000–1500', value: prices.filter((x) => x >= 1000 && x < 1500).length, color: '#C7D7EE' },
                { label: '1500–2000', value: prices.filter((x) => x >= 1500 && x < 2000).length, color: '#88A9D8' },
                { label: '≥ 2000', value: prices.filter((x) => x >= 2000).length, color: '#143A66' },
              ]
              const priceSeg = toDonutSegments(priceBuckets)

              const donut = (segments: Array<{ label: string; value: number; color: string; pct: number; dash: string; offset: number }>) => (
                <div className="flex items-center gap-5">
                  <svg viewBox="0 0 42 42" className="h-28 w-28 shrink-0">
                    <circle cx="21" cy="21" r="15.915" fill="transparent" stroke="rgba(0,0,0,0.08)" strokeWidth="8" />
                    {segments.map((s) => (
                      <circle
                        key={s.label}
                        cx="21"
                        cy="21"
                        r="15.915"
                        fill="transparent"
                        stroke={s.color}
                        strokeWidth="8"
                        strokeDasharray={s.dash}
                        strokeDashoffset={s.offset}
                      />
                    ))}
                  </svg>
                  <div className="space-y-2 text-sm">
                    {segments.length === 0 ? <div className="text-black/60">Недостаточно данных.</div> : null}
                    {segments.map((s) => (
                      <div key={`legend-${s.label}`} className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-2">
                          <span className="inline-block h-2.5 w-2.5 rounded-full" style={{ backgroundColor: s.color }} />
                          <span className="text-black/70">{s.label}</span>
                        </div>
                        <div className="text-black/60">{s.value}</div>
                      </div>
                    ))}
                  </div>
                </div>
              )

              return (
                <>
                  <div className="rounded-3xl border border-black/10 bg-white/70 p-5">
                    <h3 className="text-xl font-semibold text-center text-brand-500 mb-4">Распределение спроса (donut)</h3>
                    {donut(demandSeg)}
                  </div>
                  <div className="rounded-3xl border border-black/10 bg-white/70 p-5">
                    <h3 className="text-xl font-semibold text-center text-brand-500 mb-4">Распределение цен по м² (donut)</h3>
                    {donut(priceSeg)}
                  </div>
                </>
              )
            })()}
          </div>
        </div>
      ) : null}

      {tab === 'reports' ? (
        <div className="rounded-3xl border border-black/10 bg-brand-100 p-5">
          <h2 className="text-2xl font-semibold">Отчёты</h2>

          <div className="mt-4 flex flex-wrap gap-3">
            <button
              className="rounded-xl bg-brand-900 text-white px-4 py-2 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
              onClick={() => void downloadAdminPdf()}
              disabled={downloadingPdf}
            >
              {downloadingPdf ? 'Скачиваем...' : 'Скачать админ-отчёт (PDF)'}
            </button>
            <button
              className="rounded-xl border border-brand-500/50 bg-white px-4 py-2 font-semibold hover:bg-brand-800"
              onClick={() => window.location.assign('/profile')}
            >
              Открыть кабинет для личного отчёта
            </button>
          </div>
          <div className="mt-5 rounded-2xl border border-brand-500/30 bg-white p-4">
            <div className="text-sm text-brand-600">Текущий администратор</div>
            <div className="font-semibold mt-1">{user?.username ?? '—'}</div>
            <div className="text-sm text-brand-600 mt-1">{user?.email ?? '—'}</div>
            <div className="text-sm mt-2">
              <span className="font-semibold">Итоговая статистика:</span>{' '}
              {stats ? `${stats.usersCount} пользователей, ${stats.estatesCount} объектов, ${stats.evaluationsCount} оценок` : '—'}
            </div>
          </div>
        </div>
      ) : null}

      {tab === 'estatesTable' ? (
        <div className="rounded-3xl border border-black/10 bg-white/80 p-4 md:p-6">
          <h2 className="text-2xl font-semibold text-brand-500 mb-4">Все объекты (табличный вид)</h2>
          {loadingEstates ? <div className="text-sm text-brand-600">Загрузка...</div> : null}
          {!loadingEstates && estates.length === 0 ? (
            <div className="text-sm text-brand-600">Объектов пока нет.</div>
          ) : null}
          {estates.length ? (
            <div className="overflow-auto rounded-2xl border border-black/10 bg-white">
              <table className="min-w-full text-sm">
                <thead className="bg-brand-100/80">
                  <tr>
                    <th className="px-3 py-2 text-left font-semibold">ID</th>
                    <th className="px-3 py-2 text-left font-semibold">Адрес</th>
                    <th className="px-3 py-2 text-left font-semibold">Район</th>
                    <th className="px-3 py-2 text-left font-semibold">Цена (USD)</th>
                    <th className="px-3 py-2 text-left font-semibold">Площадь</th>
                    <th className="px-3 py-2 text-left font-semibold">Комнаты</th>
                  </tr>
                </thead>
                <tbody>
                  {estates.map((e) => (
                    <tr key={e.id} className="border-t border-black/5 hover:bg-black/3">
                      <td className="px-3 py-2">{e.id}</td>
                      <td className="px-3 py-2">{e.address}</td>
                      <td className="px-3 py-2">{e.districtName ?? '—'}</td>
                      <td className="px-3 py-2">
                        {new Intl.NumberFormat('ru-RU').format(Number(e.price))} USD
                      </td>
                      <td className="px-3 py-2">{e.area} м²</td>
                      <td className="px-3 py-2">{e.rooms}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </div>
      ) : null}
    </div>
  )
}


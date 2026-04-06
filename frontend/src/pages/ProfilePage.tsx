import { useEffect, useMemo, useState } from 'react'
import client from '../api/client'
import { useAuthStore } from '../store/authStore'
import RecommendationsPage from './RecommendationsPage'
import type { DistrictResponse, EstateResponse } from '../types'

type Profile = {
  userAccountId: number
  username: string
  email: string
  role: 'ADMIN' | 'CLIENT' | 'APPRAISER'
  createdAt: string
}

type Evaluation = {
  id: number
  estateId: number
  address: string
  appraiserId: number
  appraiserName: string | null
  estimatedValue: number
  evaluationMethod: string | null
  notes: string | null
  createdAt: string
}

type Page<T> = {
  content: T[]
  totalElements?: number
  totalPages: number
  number: number
  size: number
}

type TabId = 'profile' | 'allEstates' | 'manualEvaluation' | 'recommendations' | 'evaluations' | 'report'
const FALLBACK_IMAGE = 'https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=1200&q=80'

export default function ProfilePage() {
  const authUser = useAuthStore((s) => s.user)
  const authRole = useAuthStore((s) => s.role)

  const [activeTab, setActiveTab] = useState<TabId>('profile')
  const [profile, setProfile] = useState<Profile | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [evalPage, setEvalPage] = useState<Page<Evaluation> | null>(null)
  const [loadingEvals, setLoadingEvals] = useState(false)

  const [downloadingPdf, setDownloadingPdf] = useState(false)
  const [districts, setDistricts] = useState<DistrictResponse[]>([])
  const [allEstates, setAllEstates] = useState<EstateResponse[]>([])
  const [manualResult, setManualResult] = useState<any>(null)
  const [manualLoading, setManualLoading] = useState(false)
  const [form, setForm] = useState({
    districtId: '',
    address: '',
    area: '',
    rooms: '1',
    floor: '1',
    totalFloors: '1',
    condition: 'среднее',
    description: '',
  })

  useEffect(() => {
    setError(null)
    ;(async () => {
      try {
        const resp = await client.get<Profile>('/api/auth/profile')
        setProfile(resp.data)
        const d = await client.get<DistrictResponse[]>('/api/districts')
        setDistricts(d.data)
      } catch (e: any) {
        setError(e?.response?.data?.error ?? 'Ошибка загрузки профиля')
      }
    })()
  }, [])

  const myId = useMemo(() => profile?.userAccountId ?? authUser?.userAccountId ?? null, [profile, authUser])

  useEffect(() => {
    if (activeTab !== 'evaluations') return
    if (!myId) return

    setLoadingEvals(true)
    setEvalPage(null)
    setError(null)

    ;(async () => {
      try {
        const resp = await client.get<Page<Evaluation>>('/api/evaluations', {
          params: { appraiserId: myId, page: 0, size: 20 },
        })
        setEvalPage(resp.data)
      } catch (e: any) {
        setError(e?.response?.data?.error ?? 'Ошибка загрузки оценок')
      } finally {
        setLoadingEvals(false)
      }
    })()
  }, [activeTab, myId])

  useEffect(() => {
    if (activeTab !== 'allEstates') return
    client
      .get('/api/real-estate', { params: { page: 0, size: 50, sortBy: 'createdAt', sortOrder: 'desc' } })
      .then((r) => setAllEstates(r.data.content ?? []))
      .catch((e: any) => setError(e?.response?.data?.error ?? 'Ошибка загрузки квартир'))
  }, [activeTab])

  async function downloadMyPdf() {
    if (!myId) return
    setError(null)
    setDownloadingPdf(true)
    try {
      const resp = await client.get('/api/reports/me/pdf', { responseType: 'blob' })
      const blob = resp.data as Blob
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `my-report.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка скачивания PDF')
    } finally {
      setDownloadingPdf(false)
    }
  }

  async function submitManualEvaluation() {
    setError(null)
    setManualLoading(true)
    setManualResult(null)
    try {
      const resp = await client.post('/api/evaluations/manual', {
        districtId: Number(form.districtId),
        address: form.address,
        area: Number(form.area),
        rooms: Number(form.rooms),
        floor: Number(form.floor),
        totalFloors: Number(form.totalFloors),
        condition: form.condition,
        description: form.description || null,
      })
      setManualResult(resp.data.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка расчета оценки')
    } finally {
      setManualLoading(false)
    }
  }

  if (!profile) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-semibold">Личный кабинет</h1>
        {error ? <div className="text-red-600 text-sm">{error}</div> : null}
        <div className="text-sm text-black/60">Загрузка...</div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl font-semibold">Личный кабинет</h1>
            <div className="text-sm text-black/60 mt-1">Роль: {profile.role}</div>
          </div>
          <div className="text-right">
            <div className="text-sm text-black/60">Пользователь</div>
            <div className="font-semibold">{profile.username}</div>
          </div>
        </div>

        {error ? <div className="text-red-600 text-sm mt-3">{error}</div> : null}
      </div>

      <div className="rounded-xl border border-black/10 bg-white/80 p-3">
        <div className="flex flex-wrap gap-2">
          <button
            className={`rounded border px-3 py-2 text-sm hover:bg-black/5 ${activeTab === 'profile' ? 'border-brand-600 bg-brand-600/10 font-semibold' : 'border-black/15'}`}
            onClick={() => setActiveTab('profile')}
          >
            Профиль
          </button>
          <button
            className={`rounded border px-3 py-2 text-sm hover:bg-black/5 ${activeTab === 'allEstates' ? 'border-brand-600 bg-brand-600/10 font-semibold' : 'border-black/15'}`}
            onClick={() => setActiveTab('allEstates')}
          >
            Все квартиры
          </button>
          <button
            className={`rounded border px-3 py-2 text-sm hover:bg-black/5 ${activeTab === 'manualEvaluation' ? 'border-brand-600 bg-brand-600/10 font-semibold' : 'border-black/15'}`}
            onClick={() => setActiveTab('manualEvaluation')}
          >
            Оценить квартиру
          </button>
          <button
            className={`rounded border px-3 py-2 text-sm hover:bg-black/5 ${activeTab === 'recommendations' ? 'border-brand-600 bg-brand-600/10 font-semibold' : 'border-black/15'}`}
            onClick={() => setActiveTab('recommendations')}
          >
            Подбор/Предпочтения
          </button>
          <button
            className={`rounded border px-3 py-2 text-sm hover:bg-black/5 ${activeTab === 'evaluations' ? 'border-brand-600 bg-brand-600/10 font-semibold' : 'border-black/15'}`}
            onClick={() => setActiveTab('evaluations')}
          >
            Мои оценки
          </button>
          <button
            className={`rounded border px-3 py-2 text-sm hover:bg-black/5 ${activeTab === 'report' ? 'border-brand-600 bg-brand-600/10 font-semibold' : 'border-black/15'}`}
            onClick={() => setActiveTab('report')}
          >
            Отчёт (PDF)
          </button>
        </div>
      </div>

      {activeTab === 'profile' ? (
        <div className="rounded-xl border border-black/10 bg-white/80 p-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="rounded-lg border border-black/10 bg-white p-3">
              <div className="text-sm text-black/60">Username</div>
              <div className="font-semibold mt-1">{profile.username}</div>
            </div>
            <div className="rounded-lg border border-black/10 bg-white p-3">
              <div className="text-sm text-black/60">Email</div>
              <div className="font-semibold mt-1">{profile.email}</div>
            </div>
            <div className="rounded-lg border border-black/10 bg-white p-3">
              <div className="text-sm text-black/60">Дата регистрации</div>
              <div className="font-semibold mt-1">
                {new Date(profile.createdAt).toLocaleDateString('ru-RU')}
              </div>
            </div>
          </div>
        </div>
      ) : null}

      {activeTab === 'recommendations' ? (
        <RecommendationsPage />
      ) : null}

      {activeTab === 'allEstates' ? (
        <div className="rounded-xl border border-black/10 bg-white/80 p-4">
          <h2 className="text-lg font-semibold mb-3">Список всех квартир</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {allEstates.map((e) => (
              <div key={e.id} className="rounded-lg border border-black/10 bg-white p-3">
                <div className="h-40 rounded-lg overflow-hidden bg-black/5 mb-2">
                  <img src={e.imagePath || FALLBACK_IMAGE} alt={e.address} className="w-full h-full object-cover" />
                </div>
                <div className="font-semibold">{e.address}</div>
                <div className="text-sm text-black/60 mt-1">
                  {e.districtName ?? '—'} / {e.rooms} комн / {e.area} м²
                </div>
                <div className="text-sm font-semibold mt-1">{new Intl.NumberFormat('ru-RU').format(e.price)} USD</div>
              </div>
            ))}
          </div>
          {allEstates.length === 0 ? <div className="text-sm text-black/60">Квартир пока нет.</div> : null}
        </div>
      ) : null}

      {activeTab === 'manualEvaluation' ? (
        <div className="rounded-xl border border-black/10 bg-white/80 p-4 space-y-3">
          <h2 className="text-lg font-semibold">Оценка своей квартиры</h2>
          <p className="text-sm text-black/60">
            Система берет среднюю цену за м² по району и применяет коэффициенты по ремонту и этажу. Результат сохраняется и виден админу.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <label className="block">
              <span className="text-sm text-black/60">Район</span>
              <select className="mt-1 w-full rounded border border-black/20 px-3 py-2 bg-white" value={form.districtId} onChange={(e) => setForm((p) => ({ ...p, districtId: e.target.value }))}>
                <option value="">Выберите район</option>
                {districts.map((d) => <option key={d.id} value={d.id}>{d.districtName}</option>)}
              </select>
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Адрес</span>
              <input className="mt-1 w-full rounded border border-black/20 px-3 py-2" value={form.address} onChange={(e) => setForm((p) => ({ ...p, address: e.target.value }))} />
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Площадь, м²</span>
              <input className="mt-1 w-full rounded border border-black/20 px-3 py-2" value={form.area} onChange={(e) => setForm((p) => ({ ...p, area: e.target.value }))} />
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Комнат</span>
              <select className="mt-1 w-full rounded border border-black/20 px-3 py-2 bg-white" value={form.rooms} onChange={(e) => setForm((p) => ({ ...p, rooms: e.target.value }))}>
                {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n}</option>)}
              </select>
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Этаж</span>
              <input className="mt-1 w-full rounded border border-black/20 px-3 py-2" value={form.floor} onChange={(e) => setForm((p) => ({ ...p, floor: e.target.value }))} />
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Этажность дома</span>
              <input className="mt-1 w-full rounded border border-black/20 px-3 py-2" value={form.totalFloors} onChange={(e) => setForm((p) => ({ ...p, totalFloors: e.target.value }))} />
            </label>
            <label className="block">
              <span className="text-sm text-black/60">Состояние ремонта</span>
              <select className="mt-1 w-full rounded border border-black/20 px-3 py-2 bg-white" value={form.condition} onChange={(e) => setForm((p) => ({ ...p, condition: e.target.value }))}>
                <option value="требует ремонта">требует ремонта</option>
                <option value="среднее">среднее</option>
                <option value="хорошее">хорошее</option>
                <option value="отличное">отличное</option>
              </select>
            </label>
            <label className="block md:col-span-2">
              <span className="text-sm text-black/60">Комментарий (опционально)</span>
              <input className="mt-1 w-full rounded border border-black/20 px-3 py-2" value={form.description} onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))} />
            </label>
          </div>
          <button
            className="rounded bg-brand-900 text-white py-2 px-4 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
            onClick={() => void submitManualEvaluation()}
            disabled={manualLoading || !form.districtId || !form.address || !form.area}
          >
            {manualLoading ? 'Считаем...' : 'Рассчитать и сохранить'}
          </button>
          {manualResult ? (
            <div className="rounded-lg border border-black/10 bg-white p-3 text-sm">
              <div><span className="text-black/60">Предварительная стоимость:</span> <span className="font-semibold">{new Intl.NumberFormat('ru-RU').format(manualResult.valuation.estimatedValue)} USD</span></div>
              <div className="mt-1 text-black/60">Сохраненная оценка ID: {manualResult.evaluation.id}</div>
            </div>
          ) : null}
        </div>
      ) : null}

      {activeTab === 'evaluations' ? (
        <div className="rounded-xl border border-black/10 bg-white/80 p-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-semibold">Мои оценки</h2>
            <button
              className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5"
              disabled={loadingEvals}
              onClick={() => {
                // re-trigger load by toggling state
                setEvalPage(null)
                setLoadingEvals(true)
                client
                  .get<Page<Evaluation>>('/api/evaluations', { params: { appraiserId: myId, page: 0, size: 20 } })
                  .then((r) => setEvalPage(r.data))
                  .catch((e: any) => setError(e?.response?.data?.error ?? 'Ошибка загрузки оценок'))
                  .finally(() => setLoadingEvals(false))
              }}
            >
              {loadingEvals ? 'Загрузка...' : 'Обновить'}
            </button>
          </div>

          {authRole !== 'APPRAISER' && authRole !== 'ADMIN' ? (
            <div className="text-sm text-black/60 mb-3">
              Для покупателя оценки не формируются, поэтому список может быть пуст.
            </div>
          ) : null}

          {loadingEvals ? <div className="text-sm text-black/60">Загрузка...</div> : null}
          {evalPage?.content?.length === 0 && !loadingEvals ? (
            <div className="text-sm text-black/60">Пока нет оценок.</div>
          ) : null}

          <div className="space-y-2">
            {evalPage?.content?.map((e) => (
              <div key={e.id} className="rounded-lg border border-black/10 bg-white p-3">
                <div className="flex items-center justify-between gap-2">
                  <div className="font-semibold">Оценка #{e.id}</div>
                  <div className="text-sm font-semibold">{new Intl.NumberFormat('ru-RU').format(e.estimatedValue)} $</div>
                </div>
                <div className="text-sm text-black/60 mt-1">
                  Объект: {e.address} / метод: {e.evaluationMethod ?? '—'}
                </div>
                {e.notes ? <div className="text-sm mt-2">{e.notes}</div> : null}
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {activeTab === 'report' ? (
        <div className="rounded-xl border border-black/10 bg-white/80 p-4 space-y-3">
          <h2 className="text-lg font-semibold">Мой PDF-отчёт</h2>
          <div className="text-sm text-black/60">
            В отчёт включается информация о пользователе и последние оценки (если вы оценщик/админ).
          </div>
          <button
            className="rounded bg-brand-600 text-white py-2 px-4 font-semibold hover:bg-brand-600/90 disabled:opacity-50"
            disabled={downloadingPdf}
            onClick={() => void downloadMyPdf()}
          >
            {downloadingPdf ? 'Генерируем...' : 'Скачать мой отчёт (PDF)'}
          </button>
        </div>
      ) : null}
    </div>
  )
}


import { useEffect, useMemo, useState } from 'react'
import client from '../api/client'
import { useAuthStore } from '../store/authStore'
import type { EstateResponse } from '../types'

type Profile = {
  userAccountId: number
  username: string
  email: string
  role: 'ADMIN' | 'CLIENT' | 'APPRAISER'
  createdAt: string
}

type TabId = 'profile' | 'allEstates' | 'report'
const FALLBACK_IMAGE = 'https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=1200&q=80'

export default function ProfilePage() {
  const authUser = useAuthStore((s) => s.user)

  const [activeTab, setActiveTab] = useState<TabId>('profile')
  const [profile, setProfile] = useState<Profile | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileForm, setProfileForm] = useState({ username: '', email: '' })

  const [downloadingPdf, setDownloadingPdf] = useState(false)
  const [allEstates, setAllEstates] = useState<EstateResponse[]>([])

  useEffect(() => {
    setError(null)
    ;(async () => {
      try {
        const resp = await client.get<Profile>('/api/auth/profile')
        setProfile(resp.data)
        setProfileForm({ username: resp.data.username, email: resp.data.email })
      } catch (e: any) {
        setError(e?.response?.data?.error ?? 'Ошибка загрузки профиля')
      }
    })()
  }, [])

  const myId = useMemo(() => profile?.userAccountId ?? authUser?.userAccountId ?? null, [profile, authUser])

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

          <div className="mt-4 rounded-xl border border-black/10 bg-white p-4">
            <div className="font-semibold mb-3">Редактирование данных</div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <label className="block">
                <span className="text-sm text-black/60">Логин</span>
                <input
                  className="mt-1 w-full rounded border border-black/20 px-3 py-2 bg-white"
                  value={profileForm.username}
                  onChange={(e) => setProfileForm((p) => ({ ...p, username: e.target.value }))}
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Email</span>
                <input
                  className="mt-1 w-full rounded border border-black/20 px-3 py-2 bg-white"
                  value={profileForm.email}
                  onChange={(e) => setProfileForm((p) => ({ ...p, email: e.target.value }))}
                />
              </label>
            </div>
            <div className="mt-3">
              <button
                className="rounded bg-brand-900 text-white px-4 py-2 text-sm font-semibold hover:bg-brand-900/90 disabled:opacity-50"
                disabled={savingProfile}
                onClick={async () => {
                  setError(null)
                  setSavingProfile(true)
                  try {
                    const resp = await client.put('/api/auth/profile', {
                      username: profileForm.username.trim(),
                      email: profileForm.email.trim(),
                    })
                    setProfile(resp.data)
                  } catch (e: any) {
                    setError(e?.response?.data?.error ?? 'Ошибка обновления профиля')
                  } finally {
                    setSavingProfile(false)
                  }
                }}
              >
                {savingProfile ? 'Сохраняем...' : 'Сохранить изменения'}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {/* Подбор/оценка/мои оценки вынесены на отдельные страницы */}

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

      {/* Убрано из кабинета: оценка/подбор/мои оценки */}

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


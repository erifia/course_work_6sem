import { useEffect, useMemo, useState } from 'react'
import client from '../api/client'
import type { DistrictResponse, RecommendationResponse, UserPreferenceResponse } from '../types'

export default function RecommendationsPage() {
  const [districts, setDistricts] = useState<DistrictResponse[]>([])
  const [pref, setPref] = useState<UserPreferenceResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [selectedDistricts, setSelectedDistricts] = useState<number[]>([])
  const [limit, setLimit] = useState(10)
  const [recs, setRecs] = useState<RecommendationResponse[] | null>(null)
  const [supportedCurrencies, setSupportedCurrencies] = useState<string[]>(['USD'])
  const [priceCurrency, setPriceCurrency] = useState('USD')
  const [priceRate, setPriceRate] = useState<number>(1)
  const [loading, setLoading] = useState(false)

  const conditionOptions = useMemo(
    () => ['требует ремонта', 'среднее', 'хорошее', 'отличное'],
    [],
  )

  useEffect(() => {
    ;(async () => {
      const d = await client.get('/api/districts')
      setDistricts(d.data)
      const p = await client.get('/api/recommendations/preferences')
      setPref(p.data.data)
      const c = await client.get('/api/external/currency/supported')
      setSupportedCurrencies(c.data.data ?? ['USD'])
    })().catch((e: any) => setError(e?.response?.data?.error ?? 'Ошибка загрузки'))
  }, [])

  async function savePref() {
    try {
      setError(null)
      const p = prefForm
      const payload = {
        minPrice: p.minPrice,
        maxPrice: p.maxPrice,
        minArea: p.minArea,
        maxArea: p.maxArea,
        minRooms: p.minRooms,
        maxRooms: p.maxRooms,
        minFloor: p.minFloor,
        maxFloor: p.maxFloor,
        condition: p.condition,
      }
      const resp = await client.put('/api/recommendations/preferences', payload)
      setPref(resp.data.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка сохранения предпочтений')
    }
  }

  async function generate() {
    setError(null)
    try {
      const districtIds = selectedDistricts.join(',')
      const resp = await client.post(
        `/api/recommendations/generate?districtIds=${encodeURIComponent(districtIds)}&limit=${limit}`,
      )
      setRecs(resp.data.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка генерации рекомендаций')
    }
  }

  async function matchAndSave() {
    setLoading(true)
    try {
      await savePref()
      localStorage.setItem('recommendations:selectedDistricts', JSON.stringify(selectedDistricts))
      await generate()
    } finally {
      setLoading(false)
    }
  }

  async function restoreSaved() {
    setLoading(true)
    try {
      const p = await client.get('/api/recommendations/preferences')
      setPref(p.data.data)
      const raw = localStorage.getItem('recommendations:selectedDistricts')
      if (raw) {
        const ids = JSON.parse(raw)
        if (Array.isArray(ids)) setSelectedDistricts(ids.map((x) => Number(x)).filter((x) => !Number.isNaN(x)))
      }
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка восстановления предпочтений')
    } finally {
      setLoading(false)
    }
  }

  function updatePref<K extends keyof UserPreferenceResponse>(key: K, value: UserPreferenceResponse[K]) {
    setPref((p) => {
      const base: UserPreferenceResponse = p ?? {
        preferenceId: 0,
        minPrice: null,
        maxPrice: null,
        minArea: null,
        maxArea: null,
        minRooms: null,
        maxRooms: null,
        minFloor: null,
        maxFloor: null,
        condition: null,
      }
      return { ...base, [key]: value }
    })
  }

  async function changePriceCurrency(next: string) {
    setPriceCurrency(next)
    if (next === 'USD') {
      setPriceRate(1)
      return
    }
    try {
      const resp = await client.get('/api/external/currency', { params: { target: next } })
      setPriceRate(Number(resp.data?.data?.rate ?? 1))
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка курса валют для цен')
      setPriceRate(1)
    }
  }

  const prefForm = pref ?? {
    preferenceId: 0,
    minPrice: null,
    maxPrice: null,
    minArea: null,
    maxArea: null,
    minRooms: null,
    maxRooms: null,
    minFloor: null,
    maxFloor: null,
    condition: null,
  }

  const exactMatches = (recs ?? []).filter((r) => Number(r.score) >= 8)
  const maybeLike = (recs ?? []).filter((r) => Number(r.score) < 8)
  const fallbackImage =
    'https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=1200&q=80'

  return (
    <div className="space-y-4">
      {error ? <div className="text-red-600 text-sm">{error}</div> : null}

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-1">Подбор квартир</h2>
        <p className="text-xs text-black/50 mb-3">Заполните пожелания, затем нажмите «Подобрать и сохранить предпочтения».</p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <label className="block">
            <span className="text-sm text-black/60">Мин. цена</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.minPrice ?? ''}
              onChange={(e) => updatePref('minPrice', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="Напр. 100000"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Макс. цена</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.maxPrice ?? ''}
              onChange={(e) => updatePref('maxPrice', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="Напр. 300000"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Мин. площадь</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.minArea ?? ''}
              onChange={(e) => updatePref('minArea', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="Напр. 40"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Макс. площадь</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.maxArea ?? ''}
              onChange={(e) => updatePref('maxArea', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="Напр. 100"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Мин. комнат</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.minRooms ?? ''}
              onChange={(e) => updatePref('minRooms', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="1"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Макс. комнат</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.maxRooms ?? ''}
              onChange={(e) => updatePref('maxRooms', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="4"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Мин. этаж</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.minFloor ?? ''}
              onChange={(e) => updatePref('minFloor', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="1"
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Макс. этаж</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.maxFloor ?? ''}
              onChange={(e) => updatePref('maxFloor', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="12"
            />
          </label>

          <label className="block md:col-span-3">
            <span className="text-sm text-black/60">Состояние</span>
            <select
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white"
              value={prefForm.condition ?? ''}
              onChange={(e) => updatePref('condition', e.target.value === '' ? null : e.target.value)}
            >
              <option value="">Любое</option>
              {conditionOptions.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>

          <div className="md:col-span-3">
            <span className="text-sm text-black/60">Предпочтительные районы</span>
            <div className="mt-2 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2">
              {districts.map((d) => {
                const checked = selectedDistricts.includes(d.id)
                return (
                  <label key={d.id} className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={(e) => {
                        setSelectedDistricts((prev) =>
                          e.target.checked ? [...prev, d.id] : prev.filter((x) => x !== d.id),
                        )
                      }}
                    />
                    <span>{d.districtName}</span>
                  </label>
                )
              })}
            </div>
          </div>

          <label className="block">
            <span className="text-sm text-black/60">Лимит вариантов</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Показывать цены в валюте</span>
            <select className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white" value={priceCurrency} onChange={(e) => void changePriceCurrency(e.target.value)}>
              {supportedCurrencies.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </label>
          <div className="md:col-span-3 flex flex-wrap gap-2">
            <button
              onClick={() => void matchAndSave()}
              className="rounded bg-brand-900 text-white py-2 px-4 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
              disabled={loading}
            >
              {loading ? 'Выполняем...' : 'Подобрать и сохранить предпочтения'}
            </button>
            <button
              onClick={() => void restoreSaved()}
              className="rounded border border-black/20 bg-white py-2 px-4 font-semibold hover:bg-black/5 disabled:opacity-50"
              disabled={loading}
            >
              Восстановить сохраненные предпочтения
            </button>
          </div>
        </div>
      </div>

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-3">Результаты подбора</h2>
        {!recs ? <div className="text-sm text-black/60">Заполните форму и нажмите кнопку подбора.</div> : null}

        {recs ? (
          <div className="space-y-5">
            <div>
              <h3 className="font-semibold mb-2">Подходят под требования (включая частичное совпадение)</h3>
              {exactMatches.length === 0 ? (
                <div className="text-sm text-black/60">По заданным параметрам точных совпадений нет.</div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {exactMatches.map((r) => (
                    <div
                      key={`exact-${r.recommendationId ?? r.estateId}`}
                      className="rounded-2xl border border-black/10 bg-white p-3 transition hover:shadow-lg hover:-translate-y-0.5"
                    >
                      <div className="h-36 rounded-xl overflow-hidden bg-black/5 mb-3">
                        <img
                          src={r.imagePath || fallbackImage}
                          alt={r.address}
                          className="w-full h-full object-cover"
                        />
                      </div>
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <div className="font-semibold">{r.address}</div>
                          <div className="text-sm text-black/60 mt-1">
                            {r.districtName} / {r.rooms} комн / {r.area} м²
                          </div>
                          <div className="text-sm text-black/60 mt-1">
                            Этаж: {r.floor}/{r.totalFloors} / {r.condition}
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="font-semibold">
                            {new Intl.NumberFormat('ru-RU').format(Math.round(r.price * priceRate))} {priceCurrency}
                          </div>
                          <div className="text-sm text-black/60">Баллы: {r.score}/12</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div>
              <h3 className="font-semibold mb-2">Также могут понравиться</h3>
              {maybeLike.length === 0 ? (
                <div className="text-sm text-black/60">Дополнительных вариантов нет.</div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {maybeLike.map((r) => (
                    <div
                      key={`soft-${r.recommendationId ?? r.estateId}`}
                      className="rounded-2xl border border-black/10 bg-white p-3 transition hover:shadow-lg hover:-translate-y-0.5"
                    >
                      <div className="h-36 rounded-xl overflow-hidden bg-black/5 mb-3">
                        <img
                          src={r.imagePath || fallbackImage}
                          alt={r.address}
                          className="w-full h-full object-cover"
                        />
                      </div>
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <div className="font-semibold">{r.address}</div>
                          <div className="text-sm text-black/60 mt-1">
                            {r.districtName} / {r.rooms} комн / {r.area} м²
                          </div>
                          <div className="text-sm text-black/60 mt-1">
                            Этаж: {r.floor}/{r.totalFloors} / {r.condition}
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="font-semibold">
                            {new Intl.NumberFormat('ru-RU').format(Math.round(r.price * priceRate))} {priceCurrency}
                          </div>
                          <div className="text-sm text-black/60">Баллы: {r.score}/12</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  )
}


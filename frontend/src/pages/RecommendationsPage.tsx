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

  return (
    <div className="space-y-4">
      {error ? <div className="text-red-600 text-sm">{error}</div> : null}

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-1">Предпочтения для подбора</h2>
        <p className="text-xs text-black/50 mb-3">Все цены указываются в долларах США (USD).</p>

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
            <span className="text-sm text-black/60">Макс. этаж</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={prefForm.maxFloor ?? ''}
              onChange={(e) => updatePref('maxFloor', e.target.value === '' ? null : Number(e.target.value))}
              placeholder="5"
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
        </div>

        <div className="mt-3">
          <button
            onClick={() => void savePref()}
            className="rounded bg-brand-900 text-white py-2 px-4 font-semibold hover:bg-brand-900/90"
          >
            Сохранить
          </button>
        </div>
      </div>

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-3">Сгенерировать умный подбор</h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <label className="block">
            <span className="text-sm text-black/60">Лимит</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
            />
          </label>
          <div className="md:col-span-2">
            <span className="text-sm text-black/60">Районы (для бонуса)</span>
            <div className="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-2">
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
        </div>

        <button
          className="mt-4 rounded bg-brand-900 text-white py-2 px-4 font-semibold hover:bg-brand-900/90"
          onClick={() => void generate()}
        >
          Подобрать
        </button>
      </div>

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-3">Рекомендации</h2>
        <div className="mb-3 max-w-xs">
          <label className="block">
            <span className="text-sm text-black/60">Показывать цены в валюте</span>
            <select className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white" value={priceCurrency} onChange={(e) => void changePriceCurrency(e.target.value)}>
              {supportedCurrencies.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </label>
        </div>
        {!recs ? <div className="text-sm text-black/60">Нажмите «Подобрать».</div> : null}
        {recs?.length ? (
          <div className="space-y-3">
            {recs.map((r) => (
              <div key={r.recommendationId ?? `${r.estateId}-${r.score}`} className="border border-black/10 rounded-lg p-3 bg-white">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="font-semibold">{r.address}</div>
                    <div className="text-sm text-black/60 mt-1">
                      {r.districtName} / {r.rooms} комн / {r.area} м2
                    </div>
                    <div className="text-sm text-black/60 mt-1">
                      Этаж: {r.floor}/{r.totalFloors} / {r.condition}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-semibold">
                      {new Intl.NumberFormat('ru-RU').format(Math.round(r.price * priceRate))} {priceCurrency}
                    </div>
                    <div className="text-sm text-black/60">
                      Счёт: {r.score}/{12} ({r.scorePercent}%)
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : null}
      </div>
    </div>
  )
}


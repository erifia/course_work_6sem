import { useEffect, useState } from 'react'
import client from '../api/client'
import type { DistrictResponse, EstateResponse, PageResponse } from '../types'

export default function SearchPage() {
  const [districts, setDistricts] = useState<DistrictResponse[]>([])
  const [districtId, setDistrictId] = useState<string>('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [minArea, setMinArea] = useState('')
  const [maxArea, setMaxArea] = useState('')
  const [minRooms, setMinRooms] = useState('')
  const [condition, setCondition] = useState('')
  const [results, setResults] = useState<PageResponse<EstateResponse> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void client
      .get<DistrictResponse[]>('/api/districts')
      .then((r) => setDistricts(r.data))
      .catch(() => setError('Не удалось загрузить районы'))
  }, [])

  async function search() {
    setLoading(true)
    setError(null)
    try {
      const resp = await client.get<PageResponse<EstateResponse>>('/api/real-estate', {
        params: {
          page: 0,
          size: 20,
          sortBy: 'createdAt',
          sortOrder: 'desc',
          districtId: districtId || undefined,
          minPrice: minPrice || undefined,
          maxPrice: maxPrice || undefined,
          minArea: minArea || undefined,
          maxArea: maxArea || undefined,
          minRooms: minRooms || undefined,
          condition: condition || undefined,
        },
      })
      setResults(resp.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка поиска')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-4">
      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <div className="flex items-center justify-between mb-1">
          <h1 className="text-2xl font-semibold">Поиск квартир по фильтрам</h1>
          <div className="text-xs text-black/50">Все цены указываются в долларах США (USD)</div>
        </div>
        <p className="text-sm text-black/60 mb-3">
          Здесь простой поиск по параметрам. Для “умного подбора” с учётом ваших предпочтений используйте раздел «Подбор».
        </p>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <select className="rounded border border-black/20 px-3 py-2 bg-white" value={districtId} onChange={(e) => setDistrictId(e.target.value)}>
            <option value="">Любой район</option>
            {districts.map((d) => (
              <option key={d.id} value={d.id}>
                {d.districtName}
              </option>
            ))}
          </select>
          <input className="rounded border border-black/20 px-3 py-2" placeholder="Мин. цена (USD)" value={minPrice} onChange={(e) => setMinPrice(e.target.value)} />
          <input className="rounded border border-black/20 px-3 py-2" placeholder="Макс. цена (USD)" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} />
          <input className="rounded border border-black/20 px-3 py-2" placeholder="Мин. комнат" value={minRooms} onChange={(e) => setMinRooms(e.target.value)} />
          <input className="rounded border border-black/20 px-3 py-2" placeholder="Мин. площадь" value={minArea} onChange={(e) => setMinArea(e.target.value)} />
          <input className="rounded border border-black/20 px-3 py-2" placeholder="Макс. площадь" value={maxArea} onChange={(e) => setMaxArea(e.target.value)} />
          <select className="rounded border border-black/20 px-3 py-2 bg-white" value={condition} onChange={(e) => setCondition(e.target.value)}>
            <option value="">Любое состояние</option>
            <option value="требует ремонта">требует ремонта</option>
            <option value="среднее">среднее</option>
            <option value="хорошее">хорошее</option>
            <option value="отличное">отличное</option>
          </select>
          <button className="rounded bg-brand-900 text-white py-2 font-semibold hover:bg-brand-900/90 disabled:opacity-50" onClick={() => void search()} disabled={loading}>
            {loading ? 'Ищем...' : 'Найти'}
          </button>
        </div>
      </div>

      {error ? <div className="text-sm text-red-600">{error}</div> : null}

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-lg font-semibold mb-2">Результаты</h2>
        {!results || results.content.length === 0 ? (
          <div className="text-sm text-black/60">Пока нет результатов. Задайте критерии и нажмите "Найти".</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {results.content.map((e) => (
              <div key={e.id} className="rounded-lg border border-black/10 bg-white p-3">
                <div className="font-semibold">{e.address}</div>
                <div className="text-sm text-black/60 mt-1">
                  {e.districtName ?? '—'} / {e.rooms} комн / {e.area} м2
                </div>
                <div className="mt-1 text-sm font-semibold">{new Intl.NumberFormat('ru-RU').format(e.price)} $</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}


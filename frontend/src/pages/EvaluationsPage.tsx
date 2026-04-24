import { useEffect, useState } from 'react'
import client from '../api/client'

type Evaluation = {
  id: number
  districtId: number
  districtName: string
  address: string
  rooms: number
  area: number
  floor: number
  totalFloors: number
  condition: string
  description: string | null
  appraiserId: number
  appraiserName: string | null
  estimatedValue: number
  createdAt: string
}

type Page<T> = {
  content: T[]
  totalPages: number
  number: number
}

type District = {
  id: number
  districtName: string
}

type ManualForm = {
  districtId: number
  address: string
  area: number
  rooms: number
  floor: number
  totalFloors: number
  condition: string
  description: string
}

export default function EvaluationsPage() {
  const [data, setData] = useState<Page<Evaluation> | null>(null)
  const [districts, setDistricts] = useState<District[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [resultText, setResultText] = useState<string | null>(null)
  const [selectedEvaluation, setSelectedEvaluation] = useState<Evaluation | null>(null)
  const [form, setForm] = useState<ManualForm>({
    districtId: 0,
    address: '',
    area: 50,
    rooms: 1,
    floor: 1,
    totalFloors: 9,
    condition: 'требует ремонта',
    description: '',
  })

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const resp = await client.get('/api/evaluations/manual', { params: { page: 0, size: 20 } })
      setData(resp.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка загрузки оценок')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    ;(async () => {
      try {
        const resp = await client.get('/api/districts')
        const items = (resp.data ?? []) as District[]
        setDistricts(items)
        if (items.length > 0) {
          setForm((prev) => ({ ...prev, districtId: prev.districtId || items[0].id }))
        }
      } catch {
        // districts are optional for initial render
      }
    })()
    void load()
  }, [])

  async function submitManualEvaluation() {
    if (!form.districtId || !form.address.trim() || form.area <= 0 || form.rooms <= 0) {
      setError('Заполните корректно район, адрес, площадь и количество комнат')
      return
    }
    setSaving(true)
    setError(null)
    setResultText(null)
    try {
      const payload = {
        districtId: form.districtId,
        address: form.address.trim(),
        area: form.area,
        rooms: form.rooms,
        floor: form.floor,
        totalFloors: form.totalFloors,
        condition: form.condition,
        description: form.description.trim() || null,
      }
      const resp = await client.post('/api/evaluations/manual', payload)
      const est = Number(resp.data?.data?.estimatedValue ?? 0)
      const id = resp.data?.data?.id
      setResultText(`Оценка рассчитана. ID: ${id}, итоговая стоимость: ${new Intl.NumberFormat('ru-RU').format(Math.round(est))} USD`)
      await load()
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка ручной оценки')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h1 className="text-2xl font-semibold mb-1">Оценка квартиры</h1>
        <p className="text-xs text-black/50 mb-3">Базовая валюта расчета: USD.</p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <label className="block">
            <span className="text-sm text-black/60">Район</span>
            <select
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white"
              value={form.districtId}
              onChange={(e) => setForm((prev) => ({ ...prev, districtId: Number(e.target.value) }))}
            >
              {districts.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.districtName}
                </option>
              ))}
            </select>
          </label>
          <label className="block md:col-span-2">
            <span className="text-sm text-black/60">Адрес</span>
            <input
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={form.address}
              onChange={(e) => setForm((prev) => ({ ...prev, address: e.target.value }))}
              placeholder="Минск, ул. ..."
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Площадь (м²)</span>
            <input
              type="number"
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={form.area}
              onChange={(e) => setForm((prev) => ({ ...prev, area: Number(e.target.value) }))}
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Комнат</span>
            <select
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white"
              value={form.rooms}
              onChange={(e) => setForm((prev) => ({ ...prev, rooms: Number(e.target.value) }))}
            >
              {[1, 2, 3, 4, 5].map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Этаж</span>
            <input
              type="number"
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={form.floor}
              onChange={(e) => setForm((prev) => ({ ...prev, floor: Number(e.target.value) }))}
            />
          </label>
          <label className="block">
            <span className="text-sm text-black/60">Этажность дома</span>
            <input
              type="number"
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white"
              value={form.totalFloors}
              onChange={(e) => setForm((prev) => ({ ...prev, totalFloors: Number(e.target.value) }))}
            />
          </label>
          <label className="block md:col-span-2">
            <span className="text-sm text-black/60">Состояние ремонта</span>
            <select
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white"
              value={form.condition}
              onChange={(e) => setForm((prev) => ({ ...prev, condition: e.target.value }))}
            >
              <option value="требует ремонта">требует ремонта</option>
              <option value="среднее">среднее</option>
              <option value="хорошее">хорошее</option>
              <option value="отличное">отличное</option>
            </select>
          </label>
          <label className="block md:col-span-3">
            <span className="text-sm text-black/60">Комментарий</span>
            <textarea
              className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none bg-white min-h-20"
              value={form.description}
              onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              placeholder="Дополнительная информация о квартире"
            />
          </label>
        </div>

        <div className="mt-3 flex flex-wrap gap-2">
          <button
            className="rounded bg-brand-900 text-white py-2 px-4 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
            onClick={() => void submitManualEvaluation()}
            disabled={saving}
          >
            {saving ? 'Считаем...' : 'Оценить квартиру'}
          </button>
          <button className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5" onClick={() => void load()}>
            Обновить список оценок
          </button>
        </div>
        {resultText ? <div className="text-sm text-green-700 mt-2">{resultText}</div> : null}
      </div>

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-3">Список оценок</h2>
        {loading ? <div className="text-sm text-black/60">Загрузка...</div> : null}
        {error ? <div className="text-sm text-red-600">{error}</div> : null}
        {!loading && data?.content?.length === 0 ? <div className="text-sm text-black/60">Оценок пока нет.</div> : null}
        <div className="space-y-2">
          {data?.content?.map((e) => (
            <div key={e.id} className="rounded-lg border border-black/10 bg-white p-3">
              <div className="flex items-center justify-between gap-2">
                <div className="font-semibold">Оценка #{e.id}</div>
                <div className="text-sm font-semibold">{new Intl.NumberFormat('ru-RU').format(e.estimatedValue)} $</div>
              </div>
              <div className="text-sm text-black/60 mt-1">
                {e.address}, {e.districtName} / {e.rooms} комн / {e.area} м²
              </div>
              <div className="mt-2">
                <button
                  className="rounded border border-black/20 px-2 py-1 text-xs hover:bg-black/5"
                  onClick={() => setSelectedEvaluation(e)}
                >
                  Детали оценки
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {selectedEvaluation ? (
        <div className="fixed inset-0 z-50 bg-black/40 p-4" onClick={() => setSelectedEvaluation(null)}>
          <div
            className="max-w-xl mx-auto mt-14 rounded-xl border border-black/10 bg-white p-4"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-lg font-semibold">Оценка #{selectedEvaluation.id}</h3>
              <button className="text-sm text-black/60 hover:text-black" onClick={() => setSelectedEvaluation(null)}>Закрыть</button>
            </div>
            <div className="text-sm space-y-1">
              <div><b>Адрес:</b> {selectedEvaluation.address}</div>
              <div><b>Район:</b> {selectedEvaluation.districtName}</div>
              <div><b>Площадь:</b> {selectedEvaluation.area} м²</div>
              <div><b>Комнат:</b> {selectedEvaluation.rooms}</div>
              <div><b>Этаж:</b> {selectedEvaluation.floor}/{selectedEvaluation.totalFloors}</div>
              <div><b>Состояние:</b> {selectedEvaluation.condition}</div>
              <div><b>Комментарий:</b> {selectedEvaluation.description || '—'}</div>
              <div><b>Оценщик:</b> {selectedEvaluation.appraiserName ?? selectedEvaluation.appraiserId}</div>
              <div><b>Стоимость:</b> {new Intl.NumberFormat('ru-RU').format(selectedEvaluation.estimatedValue)} USD</div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}


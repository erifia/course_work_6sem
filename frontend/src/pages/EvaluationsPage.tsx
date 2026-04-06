import { useEffect, useState } from 'react'
import client from '../api/client'

type Evaluation = {
  id: number
  estateId: number
  appraiserId: number
  appraiserUsername: string | null
  estimatedValue: number
  evaluationMethod: string | null
  notes: string | null
  createdAt: string
}

type Page<T> = {
  content: T[]
  totalPages: number
  number: number
}

export default function EvaluationsPage() {
  const [data, setData] = useState<Page<Evaluation> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const resp = await client.get('/api/evaluations', { params: { page: 0, size: 20 } })
      setData(resp.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка загрузки оценок')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  return (
    <div className="rounded-xl border border-black/10 bg-white/80 p-4">
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-2xl font-semibold">Оценки</h1>
        <button className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5" onClick={() => void load()}>
          Обновить
        </button>
      </div>
      <p className="text-xs text-black/50 mb-2">
        Администратор видит все оценки системы, оценщик — только свои, покупатель может просматривать итоговые значения.
      </p>
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
              Объект: {e.estateId}, оценщик: {e.appraiserUsername ?? e.appraiserId}, метод: {e.evaluationMethod ?? '—'}
            </div>
            {e.notes ? <div className="text-sm mt-2">{e.notes}</div> : null}
          </div>
        ))}
      </div>
    </div>
  )
}


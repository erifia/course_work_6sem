import { useEffect, useMemo, useState, type FormEvent, type ChangeEvent } from 'react'
import client from '../api/client'
import type {
  DistrictResponse,
  EstateComparisonData,
  EstatePredictionData,
  EstateResponse,
  PageResponse,
  ReviewResponse,
} from '../types'
import { useAuthStore } from '../store/authStore'

type EstateFormState = {
  address: string
  districtId: string
  rooms: string
  area: string
  price: string
  floor: string
  totalFloors: string
  condition: string
  description: string
  propertyType: string
  imagePath: string
}

const EMPTY_FORM: EstateFormState = {
  address: '',
  districtId: '',
  rooms: '1',
  area: '',
  price: '',
  floor: '1',
  totalFloors: '1',
  condition: 'среднее',
  description: '',
  propertyType: 'APARTMENT',
  imagePath: '',
}
const FALLBACK_IMAGE = 'https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=1200&q=80'

export default function EstatesPage() {
  const role = useAuthStore((s) => s.role)
  const accessToken = useAuthStore((s) => s.accessToken)

  const [districts, setDistricts] = useState<DistrictResponse[]>([])
  const [page, setPage] = useState(0)
  const [size] = useState(8)
  const [estates, setEstates] = useState<PageResponse<EstateResponse> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [districtId, setDistrictId] = useState<number | ''>('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [minRooms, setMinRooms] = useState('')
  const [condition, setCondition] = useState<string>('')

  const [predictionLoadingFor, setPredictionLoadingFor] = useState<number | null>(null)
  const [prediction, setPrediction] = useState<Record<number, EstatePredictionData>>({})

  const [compareLeft, setCompareLeft] = useState<string>('')
  const [compareRight, setCompareRight] = useState<string>('')
  const [compareLoading, setCompareLoading] = useState(false)
  const [comparison, setComparison] = useState<EstateComparisonData | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [estateForm, setEstateForm] = useState<EstateFormState>(EMPTY_FORM)
  const [formLoading, setFormLoading] = useState(false)
  const [uploadLoading, setUploadLoading] = useState(false)

  const [reviewsOpen, setReviewsOpen] = useState(false)
  const [reviewsEstateId, setReviewsEstateId] = useState<number | null>(null)
  const [reviews, setReviews] = useState<PageResponse<ReviewResponse> | null>(null)
  const [reviewRating, setReviewRating] = useState(5)
  const [reviewComment, setReviewComment] = useState('')
  const [reviewLoading, setReviewLoading] = useState(false)

  const canManageEstate = useMemo(() => role === 'ADMIN' || role === 'APPRAISER', [role])
  const canCreateEstate = useMemo(() => role === 'ADMIN', [role])

  useEffect(() => {
    if (!accessToken) return
    ;(async () => {
      const resp = await client.get('/api/districts')
      setDistricts(resp.data)
    })().catch((e) => setError(e?.response?.data?.error ?? 'Ошибка загрузки районов'))
  }, [accessToken])

  useEffect(() => {
    void loadEstates()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page])

  async function loadEstates() {
    setLoading(true)
    setError(null)
    try {
      const resp = await client.get('/api/real-estate', {
        params: {
          page,
          size,
          sortBy: 'createdAt',
          sortOrder: 'desc',
          districtId: districtId === '' ? undefined : districtId,
          minPrice: minPrice === '' ? undefined : minPrice,
          maxPrice: maxPrice === '' ? undefined : maxPrice,
          minRooms: minRooms === '' ? undefined : minRooms,
          condition: condition === '' ? undefined : condition,
        },
      })
      setEstates(resp.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка загрузки объектов')
    } finally {
      setLoading(false)
    }
  }

  async function onApplyFilters() {
    setPage(0)
    await loadEstates()
  }

  async function predict(estateId: number) {
    setPredictionLoadingFor(estateId)
    try {
      const resp = await client.get(`/api/real-estate/${estateId}/predict`, { params: { months: 12 } })
      setPrediction((prev) => ({ ...prev, [estateId]: resp.data.data }))
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка прогноза')
    } finally {
      setPredictionLoadingFor(null)
    }
  }

  function openCreateForm() {
    setEditingId(null)
    setEstateForm(EMPTY_FORM)
    setFormOpen(true)
  }

  function openEditForm(e: EstateResponse) {
    setEditingId(e.id)
    setEstateForm({
      address: e.address ?? '',
      districtId: String(e.districtId ?? ''),
      rooms: String(e.rooms ?? 1),
      area: String(e.area ?? ''),
      price: String(e.price ?? ''),
      floor: String(e.floor ?? 1),
      totalFloors: String(e.totalFloors ?? 1),
      condition: e.condition ?? 'среднее',
      description: e.description ?? '',
      propertyType: e.propertyType ?? 'APARTMENT',
      imagePath: e.imagePath ?? '',
    })
    setFormOpen(true)
  }

  async function submitEstateForm(event: FormEvent) {
    event.preventDefault()
    setError(null)
    const rooms = Number(estateForm.rooms)
    const area = Number(estateForm.area)
    const price = Number(estateForm.price)
    const floor = Number(estateForm.floor)
    const totalFloors = Number(estateForm.totalFloors)
    if (!estateForm.address.trim()) {
      setError('Введите адрес')
      return
    }
    if (!estateForm.districtId) {
      setError('Выберите район')
      return
    }
    if (!Number.isFinite(rooms) || rooms < 1) {
      setError('Количество комнат должно быть больше 0')
      return
    }
    if (!Number.isFinite(area) || area <= 0) {
      setError('Площадь должна быть больше 0')
      return
    }
    if (!Number.isFinite(price) || price < 0) {
      setError('Цена должна быть числом 0 или больше')
      return
    }
    if (!Number.isFinite(floor) || floor < 1 || !Number.isFinite(totalFloors) || totalFloors < 1) {
      setError('Этаж и этажность должны быть целыми числами >= 1')
      return
    }
    if (floor > totalFloors) {
      setError('Этаж не может быть больше этажности дома')
      return
    }
    setFormLoading(true)
    try {
      const payload = {
        address: estateForm.address.trim(),
        districtId: Number(estateForm.districtId),
        rooms,
        area,
        price,
        floor,
        totalFloors,
        condition: estateForm.condition.trim(),
        description: estateForm.description.trim() || null,
        propertyType: estateForm.propertyType.trim() || 'APARTMENT',
        imagePath: estateForm.imagePath.trim() || null,
      }

      if (editingId == null) {
        await client.post('/api/real-estate', payload)
      } else {
        await client.put(`/api/real-estate/${editingId}`, payload)
      }

      setFormOpen(false)
      await loadEstates()
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка сохранения объекта')
    } finally {
      setFormLoading(false)
    }
  }

  async function removeEstate(id: number) {
    const ok = window.confirm('Удалить объект?')
    if (!ok) return
    try {
      await client.delete(`/api/real-estate/${id}`)
      await loadEstates()
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка удаления')
    }
  }

  async function handleImageUpload(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadLoading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      const resp = await client.post('/api/files/images', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      const url: string = resp.data.url
      setEstateForm((p) => ({ ...p, imagePath: url }))
    } catch (err: any) {
      setError(err?.response?.data?.error ?? 'Ошибка загрузки изображения')
    } finally {
      setUploadLoading(false)
    }
  }

  async function runCompare() {
    if (!compareLeft || !compareRight) return
    setCompareLoading(true)
    try {
      const resp = await client.get('/api/real-estate/compare', {
        params: { id1: Number(compareLeft), id2: Number(compareRight) },
      })
      setComparison(resp.data.data)
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка сравнения')
    } finally {
      setCompareLoading(false)
    }
  }

  async function openReviews(estateId: number) {
    setReviewsEstateId(estateId)
    setReviewsOpen(true)
    const resp = await client.get(`/api/real-estate/${estateId}/reviews`, {
      params: { page: 0, size: 5 },
    })
    setReviews(resp.data)
  }

  async function submitReview() {
    if (!reviewsEstateId) return
    setReviewLoading(true)
    try {
      await client.post(`/api/real-estate/${reviewsEstateId}/reviews`, {
        rating: reviewRating,
        comment: reviewComment === '' ? null : reviewComment,
      })
      const resp = await client.get(`/api/real-estate/${reviewsEstateId}/reviews`, {
        params: { page: 0, size: 5 },
      })
      setReviews(resp.data)
      setReviewComment('')
    } catch (e: any) {
      setError(e?.response?.data?.error ?? 'Ошибка создания отзыва')
    } finally {
      setReviewLoading(false)
    }
  }

  const content = estates?.content ?? []

  return (
    <div className="space-y-4">
      <div className="rounded-xl border border-black/10 bg-white/80 p-4 transition hover:shadow-xl hover:-translate-y-0.5">
        <div className="flex items-center justify-between mb-1">
          <h2 className="text-xl font-semibold">Поиск</h2>
          <div className="text-xs text-black/50">Все цены указываются в долларах США (USD)</div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <div>
            <div className="text-sm text-black/60 mb-1">Район</div>
            <select
              className="w-full rounded border border-black/20 px-3 py-2 bg-white"
              value={districtId}
              onChange={(e) => setDistrictId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">Все</option>
              {districts.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.districtName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <div className="text-sm text-black/60 mb-1">Мин. цена (USD)</div>
            <input
              className="w-full rounded border border-black/20 px-3 py-2 outline-none"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              placeholder="Напр. 100000"
            />
          </div>

          <div>
            <div className="text-sm text-black/60 mb-1">Макс. цена (USD)</div>
            <input
              className="w-full rounded border border-black/20 px-3 py-2 outline-none"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              placeholder="Напр. 300000"
            />
          </div>

          <div>
            <div className="text-sm text-black/60 mb-1">Комнаты</div>
            <input
              className="w-full rounded border border-black/20 px-3 py-2 outline-none"
              value={minRooms}
              onChange={(e) => setMinRooms(e.target.value)}
              placeholder="Мин."
            />
          </div>

          <div className="md:col-span-2">
            <div className="text-sm text-black/60 mb-1">Состояние</div>
            <select
              className="w-full rounded border border-black/20 px-3 py-2 bg-white"
              value={condition}
              onChange={(e) => setCondition(e.target.value)}
            >
              <option value="">Любое</option>
              <option value="требует ремонта">требует ремонта</option>
              <option value="среднее">среднее</option>
              <option value="хорошее">хорошее</option>
              <option value="отличное">отличное</option>
            </select>
          </div>

          <div className="md:col-span-2 flex items-end">
            <button
              onClick={() => void onApplyFilters()}
              className="w-full rounded bg-brand-900 text-white py-2 font-semibold hover:bg-brand-900/90"
              disabled={loading}
            >
              Применить
            </button>
          </div>
        </div>
      </div>

      {error ? <div className="text-red-600 text-sm">{error}</div> : null}

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <div className="mb-3 flex items-center justify-between gap-3">
          <h2 className="text-xl font-semibold">Объекты</h2>
          {canCreateEstate ? (
            <button
              className="rounded bg-brand-900 text-white px-4 py-2 text-sm font-semibold hover:bg-brand-900/90"
              onClick={openCreateForm}
            >
              Добавить объект
            </button>
          ) : null}
        </div>
        {loading ? <div className="text-sm text-black/60">Загрузка...</div> : null}
        {!loading && content.length === 0 ? (
          <div className="text-sm text-black/60">Ничего не найдено.</div>
        ) : null}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {content.map((e) => (
            <div key={e.id} className="rounded-lg border border-black/10 p-3 bg-white transition hover:shadow-lg hover:-translate-y-0.5">
              <div className="h-44 rounded-lg overflow-hidden bg-black/5 mb-3">
                <img src={e.imagePath || FALLBACK_IMAGE} alt={e.address} className="w-full h-full object-cover" />
              </div>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="font-semibold">{e.address}</div>
                  <div className="text-sm text-black/60 mt-1">
                    Район: {e.districtName ?? '—'} / {e.rooms} комн / {e.area} м2
                  </div>
                  <div className="text-sm text-black/60 mt-1">
                    Этаж: {e.floor}/{e.totalFloors} / {e.condition}
                  </div>
                </div>
                <div className="text-right">
                  <div className="font-semibold">{new Intl.NumberFormat('ru-RU').format(e.price)} $</div>
                  <div className="text-sm text-black/60">{e.propertyType}</div>
                </div>
              </div>

              <div className="mt-3 flex gap-2">
                <button
                  className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5"
                  onClick={() => void openReviews(e.id)}
                >
                  Отзывы
                </button>
                <button
                  className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5"
                  onClick={() => void predict(e.id)}
                  disabled={predictionLoadingFor === e.id}
                >
                  {predictionLoadingFor === e.id ? 'Прогноз...' : 'Прогноз'}
                </button>
              </div>

              {prediction[e.id] ? (
                <div className="mt-2 text-xs text-black/60">
                  Через {prediction[e.id].months} мес.:{' '}
                  <span className="font-semibold">{new Intl.NumberFormat('ru-RU').format(prediction[e.id].predictedValue)} $</span>
                </div>
              ) : null}

              {canManageEstate ? (
                <div className="mt-3 flex gap-2">
                  <button
                    className="rounded border border-black/15 px-3 py-1.5 text-xs hover:bg-black/5"
                    onClick={() => openEditForm(e)}
                  >
                    Редактировать
                  </button>
                  <button
                    className="rounded border border-red-300 text-red-600 px-3 py-1.5 text-xs hover:bg-red-50"
                    onClick={() => void removeEstate(e.id)}
                  >
                    Удалить
                  </button>
                </div>
              ) : null}
            </div>
          ))}
        </div>

        <div className="mt-4 flex items-center justify-between">
          <button
            className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5 disabled:opacity-50"
            disabled={page <= 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Назад
          </button>
          <div className="text-sm text-black/60">
            Страница {estates?.number ?? 0} из {estates?.totalPages ?? 1}
          </div>
          <button
            className="rounded border border-black/15 px-3 py-2 text-sm hover:bg-black/5 disabled:opacity-50"
            disabled={estates ? page >= estates.totalPages - 1 : true}
            onClick={() => setPage((p) => p + 1)}
          >
            Дальше
          </button>
        </div>
      </div>

      <div className="rounded-xl border border-black/10 bg-white/80 p-4">
        <h2 className="text-xl font-semibold mb-3">Сравнение объектов</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <select
            className="rounded border border-black/20 px-3 py-2 bg-white"
            value={compareLeft}
            onChange={(e) => setCompareLeft(e.target.value)}
          >
            <option value="">Объект №1</option>
            {content.map((e) => (
              <option key={`left-${e.id}`} value={e.id}>
                {e.address}
              </option>
            ))}
          </select>
          <select
            className="rounded border border-black/20 px-3 py-2 bg-white"
            value={compareRight}
            onChange={(e) => setCompareRight(e.target.value)}
          >
            <option value="">Объект №2</option>
            {content.map((e) => (
              <option key={`right-${e.id}`} value={e.id}>
                {e.address}
              </option>
            ))}
          </select>
          <button
            className="rounded bg-brand-900 text-white px-4 py-2 text-sm font-semibold hover:bg-brand-900/90 disabled:opacity-50"
            onClick={() => void runCompare()}
            disabled={!compareLeft || !compareRight || compareLoading}
          >
            {compareLoading ? 'Сравниваем...' : 'Сравнить'}
          </button>
        </div>
        {comparison ? (
          <div className="mt-3 text-sm grid grid-cols-1 md:grid-cols-2 gap-2">
            <div className="rounded border border-black/10 p-3">
              <div className="font-semibold">{comparison.estate1.address}</div>
              <div>Цена: {new Intl.NumberFormat('ru-RU').format(comparison.estate1.price)} $</div>
              <div>Площадь: {comparison.estate1.area} м2</div>
              <div>Комнат: {comparison.estate1.rooms}</div>
            </div>
            <div className="rounded border border-black/10 p-3">
              <div className="font-semibold">{comparison.estate2.address}</div>
              <div>Цена: {new Intl.NumberFormat('ru-RU').format(comparison.estate2.price)} $</div>
              <div>Площадь: {comparison.estate2.area} м2</div>
              <div>Комнат: {comparison.estate2.rooms}</div>
            </div>
            <div className="md:col-span-2 rounded border border-black/10 p-3">
              Разница цены: <span className="font-semibold">{new Intl.NumberFormat('ru-RU').format(comparison.differences.priceDiff)} $</span>,{' '}
              площади: <span className="font-semibold">{comparison.differences.areaDiff} м2</span>, комнат:{' '}
              <span className="font-semibold">{comparison.differences.roomsDiff}</span>
            </div>
          </div>
        ) : null}
      </div>

      {reviewsOpen && reviewsEstateId ? (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-3 z-50" onClick={() => setReviewsOpen(false)}>
          <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-xl bg-white p-4" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">Отзывы</h3>
              <button
                className="text-sm border border-black/15 rounded px-3 py-2 hover:bg-black/5"
                onClick={() => setReviewsOpen(false)}
              >
                Закрыть
              </button>
            </div>

            <div className="mt-3 space-y-2">
              {reviews?.content?.length ? (
                reviews.content.map((r) => (
                  <div key={r.id} className="border border-black/10 rounded-lg p-3">
                    <div className="flex items-center justify-between">
                      <div className="font-semibold text-sm">{r.username}</div>
                      <div className="text-sm text-black/60">Оценка: {r.rating}/5</div>
                    </div>
                    {r.comment ? <div className="mt-2 text-sm text-black/70">{r.comment}</div> : null}
                  </div>
                ))
              ) : (
                <div className="text-sm text-black/60">Пока нет отзывов.</div>
              )}
            </div>

            <div className="mt-4 border-t pt-4">
              <div className="text-sm font-semibold mb-2">Добавить отзыв</div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <label className="block">
                  <span className="text-sm text-black/60">Рейтинг (1..5)</span>
                  <select
                    className="w-full mt-1 rounded border border-black/20 px-3 py-2 bg-white"
                    value={reviewRating}
                    onChange={(e) => setReviewRating(Number(e.target.value))}
                  >
                    {[1, 2, 3, 4, 5].map((n) => (
                      <option key={n} value={n}>
                        {n}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm text-black/60">Комментарий</span>
                  <input
                    className="w-full mt-1 rounded border border-black/20 px-3 py-2 outline-none"
                    value={reviewComment}
                    onChange={(e) => setReviewComment(e.target.value)}
                    placeholder="Короткий текст (опционально)"
                  />
                </label>
              </div>
              <button
                className="mt-3 rounded bg-brand-900 text-white py-2 font-semibold hover:bg-brand-900/90 disabled:opacity-50"
                onClick={() => void submitReview()}
                disabled={reviewLoading}
              >
                {reviewLoading ? 'Отправляем...' : 'Сохранить отзыв'}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {formOpen ? (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-3 z-50" onClick={() => setFormOpen(false)}>
          <div className="w-full max-w-3xl max-h-[92vh] overflow-y-auto rounded-xl bg-white p-4" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">{editingId == null ? 'Новый объект' : 'Редактирование объекта'}</h3>
              <button
                className="text-sm border border-black/15 rounded px-3 py-2 hover:bg-black/5"
                onClick={() => setFormOpen(false)}
              >
                Закрыть
              </button>
            </div>
            <form className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3" onSubmit={submitEstateForm}>
              <label className="block">
                <span className="text-sm text-black/60">Адрес</span>
                <input
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Город, улица, дом"
                  value={estateForm.address}
                  onChange={(e) => setEstateForm((p) => ({ ...p, address: e.target.value }))}
                  required
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Район</span>
                <select
                  className="mt-1 rounded border border-black/20 px-3 py-2 bg-white w-full"
                  value={estateForm.districtId}
                  onChange={(e) => setEstateForm((p) => ({ ...p, districtId: e.target.value }))}
                  required
                >
                  <option value="">Выберите район</option>
                  {districts.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.districtName}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Комнат</span>
                <input
                  type="number"
                  min="1"
                  step="1"
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Напр. 2"
                  value={estateForm.rooms}
                  onChange={(e) => setEstateForm((p) => ({ ...p, rooms: e.target.value }))}
                  required
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Площадь, м²</span>
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Напр. 55"
                  value={estateForm.area}
                  onChange={(e) => setEstateForm((p) => ({ ...p, area: e.target.value }))}
                  required
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Цена, USD</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Напр. 120000"
                  value={estateForm.price}
                  onChange={(e) => setEstateForm((p) => ({ ...p, price: e.target.value }))}
                  required
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Этаж</span>
                <input
                  type="number"
                  min="1"
                  step="1"
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Напр. 3"
                  value={estateForm.floor}
                  onChange={(e) => setEstateForm((p) => ({ ...p, floor: e.target.value }))}
                  required
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Всего этажей в доме</span>
                <input
                  type="number"
                  min="1"
                  step="1"
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Напр. 9"
                  value={estateForm.totalFloors}
                  onChange={(e) => setEstateForm((p) => ({ ...p, totalFloors: e.target.value }))}
                  required
                />
              </label>
              <label className="block">
                <span className="text-sm text-black/60">Состояние</span>
                <select
                  className="mt-1 rounded border border-black/20 px-3 py-2 bg-white w-full"
                  value={estateForm.condition}
                  onChange={(e) => setEstateForm((p) => ({ ...p, condition: e.target.value }))}
                  required
                >
                  <option value="требует ремонта">требует ремонта</option>
                  <option value="среднее">среднее</option>
                  <option value="хорошее">хорошее</option>
                  <option value="отличное">отличное</option>
                </select>
              </label>
              <label className="block md:col-span-2">
                <span className="text-sm text-black/60">Фото объекта</span>
                <input
                  type="file"
                  accept="image/*"
                  className="mt-1 block w-full text-sm text-black/70 file:mr-3 file:rounded-full file:border-0 file:bg-brand-900 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-brand-900/90"
                  onChange={handleImageUpload}
                />
                <div className="mt-1 text-xs text-black/50">
                  {uploadLoading ? 'Загружаем...' : 'Выберите файл с компьютера, он сохранится на сервере.'}
                </div>
                {estateForm.imagePath ? (
                  <div className="mt-2">
                    <div className="text-xs text-black/60 mb-1">Превью:</div>
                    <div className="h-40 rounded-lg border border-black/10 overflow-hidden bg-black/5">
                      <img src={estateForm.imagePath} alt="Превью объекта" className="w-full h-full object-cover" />
                    </div>
                  </div>
                ) : null}
              </label>
              <label className="block md:col-span-2">
                <span className="text-sm text-black/60">Описание</span>
                <textarea
                  className="mt-1 rounded border border-black/20 px-3 py-2 w-full"
                  placeholder="Краткое описание объекта"
                  value={estateForm.description}
                  onChange={(e) => setEstateForm((p) => ({ ...p, description: e.target.value }))}
                />
              </label>
              <div className="md:col-span-2 flex justify-end">
                <button
                  type="submit"
                  className="rounded bg-brand-900 text-white px-4 py-2 text-sm font-semibold hover:bg-brand-900/90 disabled:opacity-50"
                  disabled={formLoading}
                >
                  {formLoading ? 'Сохраняем...' : 'Сохранить'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}


'use client'

import { useEffect, useRef, useState } from 'react'
import { apiClient } from '@/lib/api-client'

interface CandidateListItem {
  id: string
  firstName: string | null
  lastName: string | null
  email: string
  phone: string | null
  currentTitle: string | null
  createdAt: string
  resumeCount: number
}

interface PageResponse {
  content: CandidateListItem[]
  totalElements: number
  totalPages: number
  number: number
}

export default function CandidatesPage() {
  const [data, setData] = useState<PageResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const loadCandidates = (p: number, q: string) => {
    setLoading(true)
    const params = new URLSearchParams({ page: String(p), size: '20' })
    if (q) params.set('search', q)
    apiClient.get<PageResponse>(`/api/candidates?${params.toString()}`)
      .then(res => setData(res.data))
      .catch(() => setError('Impossible de charger les candidats.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadCandidates(page, search) }, [page])

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value
    setSearch(val)
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      setPage(0)
      loadCandidates(0, val)
    }, 300)
  }

  const candidateName = (c: CandidateListItem) => {
    const full = [c.firstName, c.lastName].filter(Boolean).join(' ')
    return full || c.email
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold">Candidats</h1>
        {data && (
          <span className="text-sm text-gray-500">{data.totalElements} candidat(s)</span>
        )}
      </div>

      <div className="mb-4">
        <input
          type="text"
          value={search}
          onChange={handleSearchChange}
          placeholder="Rechercher par nom, prénom ou email..."
          className="w-full max-w-md px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded p-4 mb-4">{error}</div>
      )}

      {loading && (
        <div className="bg-white rounded-xl shadow overflow-hidden animate-pulse" aria-busy="true">
          <div className="bg-gray-50 border-b border-gray-200 px-4 py-3 flex gap-4">
            {['w-40', 'w-48', 'w-32', 'w-16', 'w-20'].map((w, i) => (
              <div key={i} className={`h-3 bg-gray-200 rounded ${w}`} />
            ))}
          </div>
          {[...Array(5)].map((_, i) => (
            <div key={i} className="px-4 py-4 border-b border-gray-100 flex gap-4 items-center">
              <div className="flex-1 h-4 bg-gray-200 rounded w-40" />
              <div className="flex-1 h-4 bg-gray-100 rounded w-48" />
              <div className="h-4 bg-gray-100 rounded w-32" />
              <div className="h-4 bg-gray-200 rounded w-12" />
              <div className="h-7 bg-gray-200 rounded w-14" />
            </div>
          ))}
        </div>
      )}

      {!loading && !error && data && data.content.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-500">
          Aucun candidat trouvé.
        </div>
      )}

      {!loading && data && data.content.length > 0 && (
        <>
          <div className="bg-white rounded-xl shadow overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Nom</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Email</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Titre</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase"># CVs</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.content.map(c => (
                  <tr key={c.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-900">{candidateName(c)}</td>
                    <td className="px-4 py-3 text-gray-600">{c.email}</td>
                    <td className="px-4 py-3 text-gray-500">{c.currentTitle ?? '—'}</td>
                    <td className="px-4 py-3">
                      <span className="inline-block bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full font-semibold">
                        {c.resumeCount}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <a
                        href={`/candidates/${c.id}`}
                        className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded hover:bg-blue-700"
                      >
                        Voir
                      </a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-4 mt-4">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={data.number === 0}
                className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40"
              >
                Précédent
              </button>
              <span className="text-sm text-gray-600">
                Page {data.number + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => setPage(p => p + 1)}
                disabled={data.number + 1 >= data.totalPages}
                className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40"
              >
                Suivant
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

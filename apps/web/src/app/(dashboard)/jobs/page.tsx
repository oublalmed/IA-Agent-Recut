'use client'

import { useEffect, useRef, useState } from 'react'
import { apiClient } from '@/lib/api-client'

interface Job {
  id: string
  title: string
  location?: string
  contractType?: string
  remotePolicy?: string
  status: 'DRAFT' | 'PUBLISHED' | 'PAUSED' | 'CLOSED'
  aiAnalyzedAt?: string
  createdAt: string
}

const STATUS_LABELS: Record<Job['status'], string> = {
  DRAFT: 'Brouillon',
  PUBLISHED: 'Publiée',
  PAUSED: 'Suspendue',
  CLOSED: 'Fermée',
}

const STATUS_COLORS: Record<Job['status'], string> = {
  DRAFT: 'bg-gray-100 text-gray-700',
  PUBLISHED: 'bg-green-100 text-green-700',
  PAUSED: 'bg-yellow-100 text-yellow-700',
  CLOSED: 'bg-red-100 text-red-700',
}

export default function JobsPage() {
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [jobs, setJobs] = useState<Job[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const loadJobs = (q: string, status: string) => {
    setLoading(true)
    const params = new URLSearchParams()
    if (q) params.set('search', q)
    if (status) params.set('status', status)
    apiClient.get<Job[]>(`/api/jobs?${params.toString()}`)
      .then(res => setJobs(res.data))
      .catch(() => setJobs([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadJobs(search, statusFilter)
  }, [statusFilter])

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value
    setSearch(val)
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => loadJobs(val, statusFilter), 300)
  }

  const mockJobs = jobs

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Offres d&apos;emploi</h1>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-sm font-medium"
        >
          + Créer une offre
        </button>
      </div>

      <div className="flex gap-3 mb-4">
        <input
          type="text"
          value={search}
          onChange={handleSearchChange}
          placeholder="Rechercher par titre..."
          className="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tous les statuts</option>
          <option value="DRAFT">Brouillon</option>
          <option value="PUBLISHED">Publiée</option>
          <option value="PAUSED">Suspendue</option>
          <option value="CLOSED">Fermée</option>
        </select>
      </div>

      {loading && (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400 animate-pulse">
          Chargement...
        </div>
      )}

      {!loading && mockJobs.length === 0 && (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <div className="text-gray-400 text-4xl mb-4">📋</div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Aucune offre pour l&apos;instant</h3>
          <p className="text-gray-500 text-sm mb-4">Créez votre première offre d&apos;emploi et laissez l&apos;IA l&apos;analyser.</p>
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
          >
            Créer une offre
          </button>
        </div>
      )}

      {!loading && mockJobs.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Titre</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lieu</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Contrat</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">IA</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {mockJobs.map((job) => (
                <tr key={job.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-medium text-gray-900">{job.title}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{job.location ?? '—'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{job.contractType ?? '—'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${STATUS_COLORS[job.status]}`}>
                      {STATUS_LABELS[job.status]}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    {job.aiAnalyzedAt ? (
                      <span className="text-green-600">✓ Analysée</span>
                    ) : (
                      <span className="text-gray-400">Non analysée</span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                    <a href={`/jobs/${job.id}`} className="text-blue-600 hover:text-blue-800 mr-4">Voir</a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center p-6 border-b">
              <h2 className="text-xl font-semibold">Créer une offre</h2>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-400 hover:text-gray-600 text-2xl leading-none">&times;</button>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Titre du poste *</label>
                <input type="text" placeholder="ex: Développeur Full Stack Senior" className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description *</label>
                <textarea rows={8} placeholder="Décrivez le poste, les responsabilités, le profil recherché..." className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Localisation</label>
                  <input type="text" placeholder="Paris, France" className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Télétravail</label>
                  <select className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">Choisir</option>
                    <option value="ONSITE">Présentiel</option>
                    <option value="HYBRID">Hybride</option>
                    <option value="REMOTE">Télétravail</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Type de contrat</label>
                  <select className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">Choisir</option>
                    <option value="CDI">CDI</option>
                    <option value="CDD">CDD</option>
                    <option value="FREELANCE">Freelance</option>
                    <option value="INTERNSHIP">Stage</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Expérience (années)</label>
                  <div className="flex gap-2">
                    <input type="number" placeholder="Min" min="0" className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    <input type="number" placeholder="Max" min="0" className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-3 p-6 border-t">
              <button onClick={() => setShowCreateModal(false)} className="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-50">Annuler</button>
              <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">Créer l&apos;offre</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

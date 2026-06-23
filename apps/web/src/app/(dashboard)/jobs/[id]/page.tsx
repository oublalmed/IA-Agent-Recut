'use client'

import { useEffect, useState } from 'react'
import { apiClient } from '@/lib/api-client'

interface JobStats {
  totalApplications: number
  avgMatchScore: number
  topScore: number
  pendingReview: number
}

function StatCard({ label, value, loading }: { label: string; value: string | number; loading: boolean }) {
  return (
    <div className="bg-white rounded-lg shadow p-4 flex flex-col gap-1 min-w-[130px]">
      {loading ? (
        <div className="animate-pulse">
          <div className="h-7 bg-gray-200 rounded w-14 mb-1" />
          <div className="h-3 bg-gray-100 rounded w-20" />
        </div>
      ) : (
        <>
          <span className="text-2xl font-bold text-gray-900">{value}</span>
          <span className="text-xs text-gray-500">{label}</span>
        </>
      )}
    </div>
  )
}

export default function JobDetailPage({ params }: { params: { id: string } }) {
  const jobId = params.id
  const [stats, setStats] = useState<JobStats | null>(null)
  const [statsLoading, setStatsLoading] = useState(true)

  useEffect(() => {
    apiClient.get<JobStats>(`/api/jobs/${jobId}/stats`)
      .then(res => setStats(res.data))
      .catch(() => setStats(null))
      .finally(() => setStatsLoading(false))
  }, [jobId])

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Détail de l&apos;offre</h1>

      <div className="flex gap-4 mb-6 flex-wrap">
        <StatCard
          label="Total candidatures"
          value={stats?.totalApplications ?? 0}
          loading={statsLoading}
        />
        <StatCard
          label="Score moyen"
          value={stats ? `${stats.avgMatchScore}%` : '—'}
          loading={statsLoading}
        />
        <StatCard
          label="Meilleur score"
          value={stats ? `${stats.topScore}%` : '—'}
          loading={statsLoading}
        />
        <StatCard
          label="En attente"
          value={stats?.pendingReview ?? 0}
          loading={statsLoading}
        />
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <p className="text-gray-500">Chargement de l&apos;offre {params.id}...</p>
        <div className="mt-4 flex gap-3">
          <button className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 text-sm">
            Analyser avec l&apos;IA
          </button>
          <a href={`/jobs/${params.id}/ranking`} className="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-50 text-sm">
            Voir le ranking
          </a>
        </div>
      </div>
    </div>
  )
}

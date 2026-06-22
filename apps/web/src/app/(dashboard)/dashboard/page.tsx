'use client'

import { useEffect, useState } from 'react'
import { apiClient } from '@/lib/api-client'

interface DashboardKpis {
  cvAnalyzed: number
  activeJobs: number
  avgMatchScore: number
  estimatedTimeSavedHours: number
}

function KpiCard({ title, value, subtitle }: { title: string; value: string | number; subtitle?: string }) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <p className="text-sm text-gray-500 mb-1">{title}</p>
      <p className="text-3xl font-bold text-gray-900">{value}</p>
      {subtitle && <p className="text-xs text-gray-400 mt-1">{subtitle}</p>}
    </div>
  )
}

export default function DashboardPage() {
  const [kpis, setKpis] = useState<DashboardKpis | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiClient.get<DashboardKpis>('/api/reports/dashboard')
      .then(res => setKpis(res.data))
      .catch(() => setError('Impossible de charger les données du tableau de bord.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Tableau de bord</h1>

      {loading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10" aria-busy="true" aria-label="Chargement...">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white rounded-lg shadow p-6 animate-pulse">
              <div className="h-3 bg-gray-200 rounded w-2/3 mb-3" />
              <div className="h-8 bg-gray-200 rounded w-1/2 mb-2" />
              <div className="h-2 bg-gray-100 rounded w-3/4" />
            </div>
          ))}
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded p-4 mb-6">{error}</div>
      )}

      {kpis && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
          <KpiCard
            title="CVs analysés"
            value={kpis.cvAnalyzed}
            subtitle="CVs traités avec succès"
          />
          <KpiCard
            title="Offres actives"
            value={kpis.activeJobs}
            subtitle="Offres publiées ou en cours"
          />
          <KpiCard
            title="Score moyen"
            value={`${kpis.avgMatchScore.toFixed(1)}%`}
            subtitle="Matching IA moyen (hors rejetés)"
          />
          <KpiCard
            title="Temps économisé"
            value={`${kpis.estimatedTimeSavedHours}h`}
            subtitle="Estimation (30 min / CV)"
          />
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold mb-4">Actions rapides</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="/jobs/new"
            className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
          >
            Publier une offre
          </a>
          <a
            href="/candidates"
            className="inline-flex items-center px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium"
          >
            Importer un CV
          </a>
          <a
            href="/jobs"
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
          >
            Voir toutes les offres
          </a>
        </div>
      </div>
    </div>
  )
}

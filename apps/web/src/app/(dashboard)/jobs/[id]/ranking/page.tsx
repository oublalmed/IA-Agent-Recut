'use client'

import { useEffect, useState } from 'react'
import { apiClient } from '@/lib/api-client'

interface CandidateRanking {
  applicationId: string
  candidateId: string
  candidateEmail: string | null
  candidateFirstName: string | null
  candidateLastName: string | null
  matchScore: number
  skillScore: number
  experienceScore: number
  strengths: string[]
  weaknesses: string[]
  recommendation: string | null
  status: string
  humanOverride: boolean
  cvUrl: string | null
}

type DecisionType = 'SELECTED' | 'REJECTED' | 'SHORTLISTED'

interface DecisionModalProps {
  applicationId: string
  candidateName: string
  onClose: () => void
  onSuccess: () => void
}

function ScoreBadge({ score }: { score: number }) {
  const pct = Math.round(score)
  let cls = 'text-red-700 bg-red-100'
  if (pct >= 70) cls = 'text-green-700 bg-green-100'
  else if (pct >= 40) cls = 'text-yellow-700 bg-yellow-100'
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-sm font-semibold ${cls}`}>
      {pct}%
    </span>
  )
}

function DecisionModal({ applicationId, candidateName, onClose, onSuccess }: DecisionModalProps) {
  const [decision, setDecision] = useState<DecisionType>('SHORTLISTED')
  const [justification, setJustification] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await apiClient.post(`/api/applications/${applicationId}/decision`, {
        finalStatus: decision,
        humanOverride: true,
        justification,
      })
      onSuccess()
      onClose()
    } catch {
      setError('Erreur lors de l\'enregistrement. Veuillez réessayer.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 className="text-lg font-bold mb-1">Enregistrer une décision</h3>
        <p className="text-sm text-gray-500 mb-4">{candidateName}</p>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded p-3 mb-4 text-sm">{error}</div>
        )}

        <form onSubmit={handleSubmit}>
          <fieldset className="mb-4">
            <legend className="text-sm font-medium text-gray-700 mb-2">Décision</legend>
            <div className="space-y-2">
              {(['SELECTED', 'SHORTLISTED', 'REJECTED'] as DecisionType[]).map(opt => (
                <label key={opt} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="decision"
                    value={opt}
                    checked={decision === opt}
                    onChange={() => setDecision(opt)}
                    className="accent-blue-600"
                  />
                  <span className="text-sm">
                    {opt === 'SELECTED' && 'Sélectionné'}
                    {opt === 'SHORTLISTED' && 'Présélectionné'}
                    {opt === 'REJECTED' && 'Rejeté'}
                  </span>
                </label>
              ))}
            </div>
          </fieldset>

          <div className="mb-5">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Justification
            </label>
            <textarea
              value={justification}
              onChange={e => setJustification(e.target.value)}
              rows={3}
              className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Motif de la décision (optionnel)"
            />
          </div>

          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              Annuler
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? 'Envoi...' : 'Confirmer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function JobRankingPage({ params }: { params: { id: string } }) {
  const jobId = params.id
  const [ranking, setRanking] = useState<CandidateRanking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [modalApp, setModalApp] = useState<{ id: string; name: string } | null>(null)

  const loadRanking = () => {
    setLoading(true)
    apiClient.get<CandidateRanking[]>(`/api/jobs/${jobId}/ranking`)
      .then(res => setRanking(res.data))
      .catch(() => setError('Impossible de charger le ranking.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadRanking() }, [jobId])

  const candidateName = (r: CandidateRanking) => {
    const full = [r.candidateFirstName, r.candidateLastName].filter(Boolean).join(' ')
    return full || r.candidateEmail || r.candidateId
  }

  return (
    <div>
      <div className="flex items-center gap-4 mb-6">
        <a
          href={`/jobs/${jobId}`}
          className="text-sm text-blue-600 hover:underline"
        >
          ← Retour à l&apos;offre
        </a>
        <h1 className="text-2xl font-bold">Ranking des candidats</h1>
      </div>

      {loading && (
        <div className="bg-white rounded-xl shadow overflow-hidden animate-pulse" aria-busy="true" aria-label="Chargement...">
          <div className="bg-gray-50 border-b border-gray-200 px-4 py-3 flex gap-4">
            {['w-6', 'w-32', 'w-16', 'w-24', 'w-32', 'w-20'].map((w, i) => (
              <div key={i} className={`h-3 bg-gray-200 rounded ${w}`} />
            ))}
          </div>
          {[...Array(5)].map((_, i) => (
            <div key={i} className="px-4 py-4 border-b border-gray-100 flex gap-4 items-center">
              <div className="h-4 bg-gray-200 rounded w-4" />
              <div className="flex-1 space-y-1">
                <div className="h-4 bg-gray-200 rounded w-40" />
                <div className="h-3 bg-gray-100 rounded w-28" />
              </div>
              <div className="h-6 bg-gray-200 rounded-full w-12" />
              <div className="h-4 bg-gray-100 rounded w-24" />
              <div className="h-4 bg-gray-100 rounded w-32" />
              <div className="h-7 bg-gray-200 rounded w-16" />
            </div>
          ))}
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded p-4">{error}</div>
      )}

      {!loading && !error && ranking.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-500">
          Aucun candidat n&apos;a encore été évalué pour cette offre.
        </div>
      )}

      {ranking.length > 0 && (
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">#</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Candidat</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Score</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Points forts</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Recommandation</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {ranking.map((r, idx) => (
                <tr key={r.applicationId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-600">{idx + 1}</td>
                  <td className="px-4 py-3">
                    <div className="font-medium text-gray-900">{candidateName(r)}</div>
                    {r.candidateEmail && (
                      <div className="text-xs text-gray-400">{r.candidateEmail}</div>
                    )}
                    {r.humanOverride && (
                      <span className="inline-block mt-1 text-xs bg-purple-100 text-purple-700 px-1.5 py-0.5 rounded">
                        Décision manuelle
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <ScoreBadge score={r.matchScore} />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-1">
                      {(r.strengths ?? []).slice(0, 3).map((s, i) => (
                        <span key={i} className="inline-block bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded-full">
                          {s}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3 max-w-xs">
                    <p className="text-gray-600 text-xs line-clamp-2">{r.recommendation ?? '—'}</p>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() =>
                          setModalApp({ id: r.applicationId, name: candidateName(r) })
                        }
                        className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded hover:bg-blue-700"
                      >
                        Décision
                      </button>
                      {r.cvUrl && (
                        <a
                          href={r.cvUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="px-3 py-1.5 text-xs bg-gray-100 text-gray-700 rounded hover:bg-gray-200 border border-gray-300"
                        >
                          Voir CV
                        </a>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalApp && (
        <DecisionModal
          applicationId={modalApp.id}
          candidateName={modalApp.name}
          onClose={() => setModalApp(null)}
          onSuccess={loadRanking}
        />
      )}
    </div>
  )
}

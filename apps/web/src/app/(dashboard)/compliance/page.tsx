'use client'

import { useEffect, useState } from 'react'

interface AuditDecision {
  id: string
  applicationId: string
  candidateName: string
  candidateEmail: string
  jobTitle: string
  decision: string
  justification: string | null
  aiScore: number
  humanOverride: boolean
  decidedAt: string
}

interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

const DECISION_COLORS: Record<string, string> = {
  HIRED: 'bg-green-100 text-green-800',
  SHORTLISTED: 'bg-blue-100 text-blue-800',
  REJECTED: 'bg-red-100 text-red-800',
  REVIEWED: 'bg-yellow-100 text-yellow-800',
  PENDING: 'bg-gray-100 text-gray-800',
}

export default function CompliancePage() {
  const [decisions, setDecisions] = useState<AuditDecision[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // GDPR form state
  const [candidateId, setCandidateId] = useState('')
  const [requestType, setRequestType] = useState<'ACCESS' | 'ERASURE' | 'PORTABILITY'>('ACCESS')
  const [submitting, setSubmitting] = useState(false)
  const [gdprSuccess, setGdprSuccess] = useState<string | null>(null)
  const [gdprError, setGdprError] = useState<string | null>(null)

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null
    fetch('/api/audit/decisions?size=50', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async (res) => {
        if (!res.ok) throw new Error('Failed to load audit log')
        return res.json() as Promise<PageResponse<AuditDecision>>
      })
      .then((page) => setDecisions(page.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const handleGdprSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!candidateId.trim()) {
      setGdprError('Please enter a candidate ID.')
      return
    }
    setSubmitting(true)
    setGdprError(null)
    setGdprSuccess(null)
    try {
      const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null
      const res = await fetch(`/api/candidates/${candidateId.trim()}/data-request`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ requestType }),
      })
      if (!res.ok) {
        const body = await res.json().catch(() => ({}))
        throw new Error(body?.message || `Error ${res.status}`)
      }
      setGdprSuccess(`Data request (${requestType}) submitted successfully.`)
      setCandidateId('')
    } catch (e: unknown) {
      setGdprError(e instanceof Error ? e.message : 'An error occurred')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-10">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Compliance</h1>
        <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
          This platform does not use protected characteristics (gender, age, origin, religion,
          disability) in any AI scoring or ranking decision.
        </div>
      </div>

      {/* Audit Log Section */}
      <section>
        <h2 className="text-xl font-semibold text-gray-800 mb-4">Audit Log</h2>
        {loading && <p className="text-gray-500">Loading audit decisions...</p>}
        {error && <p className="text-red-600">{error}</p>}
        {!loading && !error && decisions.length === 0 && (
          <p className="text-gray-500">No audit decisions recorded yet.</p>
        )}
        {!loading && decisions.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-gray-200">
            <table className="min-w-full divide-y divide-gray-200 bg-white text-sm">
              <thead className="bg-gray-50">
                <tr>
                  {['Candidate', 'Job', 'Decision', 'AI Score', 'Justification', 'Date'].map(
                    (h) => (
                      <th
                        key={h}
                        className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
                      >
                        {h}
                      </th>
                    )
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {decisions.map((d) => (
                  <tr key={d.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div className="font-medium text-gray-900">{d.candidateName}</div>
                      <div className="text-gray-500 text-xs">{d.candidateEmail}</div>
                    </td>
                    <td className="px-4 py-3 text-gray-700">{d.jobTitle}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${DECISION_COLORS[d.decision] ?? 'bg-gray-100 text-gray-800'}`}
                      >
                        {d.decision}
                        {d.humanOverride && (
                          <span className="ml-1 text-orange-600" title="Human override">
                            ✎
                          </span>
                        )}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-700">{d.aiScore?.toFixed(1)}</td>
                    <td className="px-4 py-3 text-gray-600 max-w-xs truncate">
                      {d.justification ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-gray-500 whitespace-nowrap">
                      {new Date(d.decidedAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* GDPR Request Section */}
      <section>
        <h2 className="text-xl font-semibold text-gray-800 mb-4">GDPR Data Request</h2>
        <form
          onSubmit={handleGdprSubmit}
          className="max-w-lg rounded-lg border border-gray-200 bg-white p-6 space-y-4"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="candidateId">
              Candidate ID (UUID)
            </label>
            <input
              id="candidateId"
              type="text"
              value={candidateId}
              onChange={(e) => setCandidateId(e.target.value)}
              placeholder="e.g. 123e4567-e89b-12d3-a456-426614174000"
              className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <fieldset>
            <legend className="block text-sm font-medium text-gray-700 mb-2">Request Type</legend>
            <div className="space-y-2">
              {(['ACCESS', 'ERASURE', 'PORTABILITY'] as const).map((type) => (
                <label key={type} className="flex items-center gap-2 text-sm text-gray-700">
                  <input
                    type="radio"
                    name="requestType"
                    value={type}
                    checked={requestType === type}
                    onChange={() => setRequestType(type)}
                  />
                  {type}
                  {type === 'ERASURE' && (
                    <span className="text-red-600 text-xs">(deletes resume records)</span>
                  )}
                </label>
              ))}
            </div>
          </fieldset>
          {gdprError && <p className="text-sm text-red-600">{gdprError}</p>}
          {gdprSuccess && <p className="text-sm text-green-600">{gdprSuccess}</p>}
          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Submitting...' : 'Submit Request'}
          </button>
        </form>
      </section>
    </div>
  )
}

export default function CandidateDetailPage({ params }: { params: { id: string } }) {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Profil candidat</h1>
      <div className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-1 bg-white rounded-lg shadow p-6">
          <div className="text-center">
            <div className="w-16 h-16 bg-gray-200 rounded-full mx-auto mb-3 flex items-center justify-center text-2xl text-gray-400">?</div>
            <h2 className="font-semibold text-lg">Candidat {params.id.slice(0, 8)}...</h2>
            <p className="text-sm text-gray-500">Chargement...</p>
          </div>
        </div>
        <div className="md:col-span-2 space-y-4">
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="font-semibold mb-3">CV & Extraction IA</h3>
            <div className="space-y-2">
              <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
                <span className="text-sm text-gray-700">cv_candidat.pdf</span>
                <span className="text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded-full">En cours d&apos;extraction</span>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="font-semibold mb-3">Compétences extraites</h3>
            <p className="text-sm text-gray-500">Disponible après extraction IA (Sprint S4)</p>
          </div>
        </div>
      </div>
    </div>
  )
}

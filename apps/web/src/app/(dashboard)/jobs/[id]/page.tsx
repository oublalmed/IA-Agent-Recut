export default function JobDetailPage({ params }: { params: { id: string } }) {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Détail de l&apos;offre</h1>
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

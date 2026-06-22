export default function JobRankingPage({ params }: { params: { id: string } }) {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Ranking des candidats</h1>
      <p className="text-gray-500 mb-4">Offre : {params.id}</p>
      <div className="bg-white rounded-lg shadow p-6 text-center text-gray-500">
        Le matching IA et le ranking seront disponibles après le Sprint S5.
      </div>
    </div>
  )
}

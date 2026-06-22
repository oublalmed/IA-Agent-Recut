export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="flex min-h-screen">
      <aside className="w-64 bg-gray-900 text-white p-4">
        <h2 className="text-xl font-bold mb-8">IA Recruiter</h2>
        <nav className="space-y-2">
          <a href="/dashboard" className="block px-3 py-2 rounded hover:bg-gray-700">Dashboard</a>
          <a href="/jobs" className="block px-3 py-2 rounded hover:bg-gray-700">Offres</a>
          <a href="/candidates" className="block px-3 py-2 rounded hover:bg-gray-700">Candidats</a>
          <a href="/company" className="block px-3 py-2 rounded hover:bg-gray-700">Entreprise</a>
          <a href="/audit" className="block px-3 py-2 rounded hover:bg-gray-700">Audit</a>
        </nav>
      </aside>
      <main className="flex-1 p-8 bg-gray-50">{children}</main>
    </div>
  )
}

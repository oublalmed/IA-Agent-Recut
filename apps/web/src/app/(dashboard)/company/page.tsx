'use client'

import { useState } from 'react'

interface CompanyData {
  id: string
  name: string
  website?: string
  industry?: string
  sizeRange?: string
  country?: string
  logoUrl?: string
}

interface TeamMember {
  id: string
  email: string
  firstName?: string
  lastName?: string
  role: string
  active: boolean
}

export default function CompanyPage() {
  const [activeTab, setActiveTab] = useState<'profile' | 'team'>('profile')

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Entreprise</h1>

      <div className="flex border-b mb-6">
        <button
          onClick={() => setActiveTab('profile')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'profile'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          Profil
        </button>
        <button
          onClick={() => setActiveTab('team')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'team'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          Équipe
        </button>
      </div>

      {activeTab === 'profile' && (
        <div className="bg-white rounded-lg shadow p-6 max-w-2xl">
          <h2 className="text-lg font-semibold mb-4">Profil entreprise</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nom</label>
              <input
                type="text"
                placeholder="Nom de l'entreprise"
                className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Site web</label>
              <input
                type="url"
                placeholder="https://..."
                className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Secteur</label>
                <input
                  type="text"
                  placeholder="Tech, Finance..."
                  className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Taille</label>
                <select className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">Sélectionner</option>
                  <option value="1-10">1-10</option>
                  <option value="11-50">11-50</option>
                  <option value="51-200">51-200</option>
                  <option value="201-500">201-500</option>
                  <option value="500+">500+</option>
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pays</label>
              <input
                type="text"
                placeholder="France"
                className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Logo</label>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center">
                <p className="text-sm text-gray-500">PNG, JPG, WebP — max 2MB</p>
                <input type="file" accept="image/*" className="mt-2" />
              </div>
            </div>
            <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
              Enregistrer
            </button>
          </div>
        </div>
      )}

      {activeTab === 'team' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">Membres de l'équipe</h2>
            <button className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700">
              Inviter un membre
            </button>
          </div>
          <div className="text-sm text-gray-500 text-center py-8">
            Connectez l'API pour afficher les membres de l'équipe
          </div>
        </div>
      )}
    </div>
  )
}

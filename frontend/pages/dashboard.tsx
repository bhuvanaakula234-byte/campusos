import { useEffect, useState } from 'react'
import api from '../lib/api'
import { useRouter } from 'next/router'
import { useAuth } from '../hooks/useAuth'

export default function Dashboard() {
  const [msg, setMsg] = useState('Loading...')
  const router = useRouter()

  const { user, loading } = useAuth()

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get('/api/health')
        setMsg(res.data.service + ' — frontend')
      } catch (err) {
        setMsg('Unable to reach backend')
      }
    }
    load()
  }, [])

  const logout = async () => {
    try {
      await api.post('/api/auth/logout')
    } catch (_) {}
    router.push('/login')
  }

  return (
    <main className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-semibold">Dashboard</h1>
          <button onClick={logout} className="px-4 py-2 bg-red-500 text-white rounded">Logout</button>
        </div>
        <div className="p-4 bg-white rounded shadow">{msg}</div>
      </div>
    </main>
  )
}

import { useState } from 'react'
import api from '../lib/api'
import { useRouter } from 'next/router'

export default function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [error, setError] = useState<string| null>(null)
  const router = useRouter()

  const submit = async (e: any) => {
    e.preventDefault()
    setError(null)
    try {
      await api.post('/api/auth/register', { email, password, fullName })
      router.push('/login')
    } catch (err: any) {
      setError(err?.response?.data || 'Registration failed')
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="p-8 bg-white rounded shadow w-full max-w-md">
        <h1 className="text-2xl font-semibold mb-4">Create an account</h1>
        <form onSubmit={submit} className="space-y-4">
          <input className="w-full p-3 border rounded" placeholder="Full name" value={fullName} onChange={e => setFullName(e.target.value)} />
          <input className="w-full p-3 border rounded" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
          <input className="w-full p-3 border rounded" placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} />
          {error && <div className="text-red-600">{error}</div>}
          <button className="w-full py-3 bg-indigo-600 text-white rounded">Create account</button>
        </form>
      </div>
    </main>
  )
}

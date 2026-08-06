import { useEffect, useState } from 'react'
import api from '../lib/api'

export default function Subjects() {
  const [subs, setSubs] = useState<any[]>([])

  useEffect(() => {
    api.get('/api/subjects').then(res => setSubs(res.data)).catch(() => setSubs([]))
  }, [])

  return (
    <main className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-2xl font-semibold mb-4">Subjects</h1>
        <div className="grid gap-4">
          {subs.map(s => (
            <div key={s.id} className="p-4 bg-white rounded shadow">
              <div className="font-semibold">{s.title} ({s.code})</div>
              <div className="text-sm text-gray-600">Semester: {s.semester}</div>
            </div>
          ))}
          {subs.length===0 && <div className="p-4 bg-white rounded">No subjects found</div>}
        </div>
      </div>
    </main>
  )
}

import { useEffect, useState } from 'react'
import api from '../lib/api'

export default function Timetable() {
  const [tt, setTt] = useState<any[]>([])

  useEffect(() => {
    api.get('/api/timetable').then(res => setTt(res.data)).catch(() => setTt([]))
  }, [])

  return (
    <main className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-2xl font-semibold mb-4">Timetable</h1>
        <div className="grid gap-4">
          {tt.map(e => (
            <div key={e.id} className="p-4 bg-white rounded shadow">
              <div className="font-semibold">{e.subject?.title || 'Unknown'}</div>
              <div className="text-sm text-gray-600">Day: {e.dayOfWeek} | {e.startTime} - {e.endTime} | {e.venue}</div>
            </div>
          ))}
          {tt.length===0 && <div className="p-4 bg-white rounded">No timetable entries</div>}
        </div>
      </div>
    </main>
  )
}

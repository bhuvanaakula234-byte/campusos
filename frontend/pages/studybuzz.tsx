import { useState } from 'react'
import api from '../lib/api'

export default function StudyBuzz() {
  const [messages, setMessages] = useState<any[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)

  const send = async () => {
    if (!input) return
    const userMsg = { role: 'user', text: input }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setLoading(true)
    try {
      const res = await api.post('/api/ai/studybuzz/chat', { message: input })
      const reply = res.data.reply || 'No response'
      setMessages(prev => [...prev, { role: 'assistant', text: reply }])
    } catch (err:any) {
      setMessages(prev => [...prev, { role: 'assistant', text: 'Error: ' + (err?.response?.data || err.message) }])
    } finally { setLoading(false) }
  }

  const [pdfUrl, setPdfUrl] = useState('')

  const summarizePdf = async () => {
    if (!pdfUrl) return
    setLoading(true)
    try {
      const res = await api.post('/api/ai/studybuzz/pdf-summarize', { url: pdfUrl })
      setMessages(prev => [...prev, { role: 'assistant', text: res.data.summary || JSON.stringify(res.data) }])
    } catch (err:any) {
      setMessages(prev => [...prev, { role: 'assistant', text: 'Error: ' + (err?.response?.data || err.message) }])
    } finally { setLoading(false) }
  }

  return (
    <main className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-2xl font-semibold mb-4">StudyBuzz</h1>
        <div className="border rounded p-4 bg-white h-[50vh] overflow-y-auto flex flex-col gap-3">
          {messages.map((m, i) => (
            <div key={i} className={m.role==='user'? 'self-end bg-indigo-50 p-3 rounded':'self-start bg-gray-100 p-3 rounded'}>
              <div className="text-sm text-gray-700 whitespace-pre-wrap">{m.text}</div>
            </div>
          ))}
          {messages.length===0 && <div className="text-gray-500">Ask StudyBuzz anything about your syllabus, generate quizzes, or summarize PDFs.</div>}
        </div>

        <div className="mt-4 flex gap-2">
          <input value={input} onChange={e=>setInput(e.target.value)} className="flex-1 p-3 border rounded" placeholder="Ask a question..." />
          <button onClick={send} disabled={loading} className="px-4 py-3 bg-indigo-600 text-white rounded">Send</button>
        </div>

        <div className="mt-4">
          <h2 className="font-semibold mb-2">Summarize PDF by URL</h2>
          <div className="flex gap-2">
            <input value={pdfUrl} onChange={e=>setPdfUrl(e.target.value)} className="flex-1 p-3 border rounded" placeholder="https://example.com/file.pdf" />
            <button onClick={summarizePdf} disabled={loading} className="px-4 py-3 bg-green-600 text-white rounded">Summarize</button>
          </div>
        </div>
      </div>
    </main>
  )
}

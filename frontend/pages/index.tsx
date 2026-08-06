import Link from 'next/link'

export default function Home() {
  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="p-8 bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-4xl">
        <h1 className="text-3xl font-semibold mb-2">CampusOS</h1>
        <p className="text-gray-600 dark:text-gray-300 mb-4">AI-powered smart college operating system — prototype frontend.</p>
        <div className="flex gap-3">
          <Link href="/login"><a className="px-4 py-2 bg-indigo-600 text-white rounded">Sign in</a></Link>
          <Link href="/register"><a className="px-4 py-2 bg-gray-200 rounded">Register</a></Link>
          <Link href="/subjects"><a className="px-4 py-2 bg-gray-200 rounded">Subjects</a></Link>
          <Link href="/timetable"><a className="px-4 py-2 bg-gray-200 rounded">Timetable</a></Link>
        </div>
      </div>
    </main>
  )
}

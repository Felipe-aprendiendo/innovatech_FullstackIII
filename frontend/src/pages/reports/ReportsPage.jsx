import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import reportService from '../../services/reportService'

function StatCard({ label, value, color = 'slate' }) {
  const colors = {
    slate:  'bg-slate-100 text-slate-800',
    blue:   'bg-blue-100 text-blue-800',
    yellow: 'bg-yellow-100 text-yellow-800',
    green:  'bg-green-100 text-green-800',
  }
  return (
    <div className={`rounded-xl p-5 ${colors[color]}`}>
      <p className="text-sm font-medium opacity-70">{label}</p>
      <p className="text-3xl font-bold mt-1">{value ?? '—'}</p>
    </div>
  )
}

function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState(null)

  useEffect(() => {
    reportService.getDashboard()
      .then(d => setDashboard(d))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="text-slate-500 text-center py-10">Cargando dashboard…</p>
  if (error)   return <p className="text-red-600 text-center py-10">{error}</p>
  if (!dashboard) return null

  const proyectos = dashboard.proyectos ?? []

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard label="Proyectos"      value={dashboard.totalProyectos}    color="slate"  />
        <StatCard label="Tareas totales" value={dashboard.totalTareas}       color="blue"   />
        <StatCard label="En progreso"    value={dashboard.tareasEnProgreso}  color="yellow" />
        <StatCard label="Completadas"    value={dashboard.tareasCompletadas} color="green"  />
      </div>

      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-slate-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">Proyecto</th>
              <th className="px-4 py-3 text-right">Total</th>
              <th className="px-4 py-3 text-right">Completadas</th>
              <th className="px-4 py-3 text-right">En progreso</th>
              <th className="px-4 py-3 text-right">Pendientes</th>
              <th className="px-4 py-3 text-right">Avance</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {proyectos.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-6 text-center text-slate-400">Sin datos de proyectos</td>
              </tr>
            )}
            {proyectos.map(p => (
              <tr key={p.projectId} className="hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3 font-medium text-slate-800">{p.nombreProyecto}</td>
                <td className="px-4 py-3 text-right text-slate-600">{p.totalTareas}</td>
                <td className="px-4 py-3 text-right text-green-700 font-semibold">{p.tareasCompletadas}</td>
                <td className="px-4 py-3 text-right text-yellow-700">{p.tareasEnProgreso}</td>
                <td className="px-4 py-3 text-right text-slate-500">{p.tareasPendientes}</td>
                <td className="px-4 py-3 text-right">
                  <span className="font-bold text-slate-900">
                    {typeof p.porcentajeAvance === 'number' ? `${p.porcentajeAvance.toFixed(1)}%` : '—'}
                  </span>
                  <div className="w-full bg-slate-200 rounded-full h-1.5 mt-1">
                    <div className="bg-blue-500 h-1.5 rounded-full"
                      style={{ width: `${Math.min(p.porcentajeAvance ?? 0, 100)}%` }} />
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function UserDashboard() {
  const [tasks, setTasks]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  useEffect(() => {
    reportService.getMyReport()
      .then(data => setTasks(Array.isArray(data) ? data : []))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const badgeEstado = {
    PENDIENTE:   'bg-slate-200 text-slate-700',
    EN_PROGRESO: 'bg-yellow-100 text-yellow-800',
    COMPLETADA:  'bg-green-100 text-green-700',
    CANCELADA:   'bg-red-100 text-red-700',
  }

  const badgePrioridad = {
    ALTA:  'bg-red-100 text-red-700',
    MEDIA: 'bg-orange-100 text-orange-700',
    BAJA:  'bg-slate-100 text-slate-600',
  }

  if (loading) return <p className="text-slate-500 text-center py-10">Cargando tus tareas…</p>
  if (error)   return <p className="text-red-600 text-center py-10">{error}</p>

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-slate-700">Mis tareas asignadas</h2>
      {tasks.length === 0 && (
        <p className="text-slate-400 text-center py-10">No tienes tareas asignadas.</p>
      )}
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-slate-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">Tarea</th>
              <th className="px-4 py-3 text-center">Prioridad</th>
              <th className="px-4 py-3 text-center">Estado</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {tasks.map(t => (
              <tr key={t.taskId} className="hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3 font-medium text-slate-800">{t.titulo}</td>
                <td className="px-4 py-3 text-center">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${badgePrioridad[t.prioridad] ?? 'bg-slate-100 text-slate-600'}`}>
                    {t.prioridad ?? '—'}
                  </span>
                </td>
                <td className="px-4 py-3 text-center">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${badgeEstado[t.estado] ?? 'bg-slate-100 text-slate-600'}`}>
                    {t.estado ?? '—'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default function ReportsPage() {
  const { user } = useAuth()
  const isAdminOrLead = user?.role === 'ADMIN' || user?.role === 'PROJECT_LEAD'

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-800 mb-6">
        {isAdminOrLead ? 'Dashboard de reportes' : 'Mi reporte'}
      </h1>
      {isAdminOrLead ? <AdminDashboard /> : <UserDashboard />}
    </div>
  )
}

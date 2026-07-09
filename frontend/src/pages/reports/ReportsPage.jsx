import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import reportService from '../../services/reportService'
import projectService from '../../services/projectService'

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

function ResumenTab({ projectFilter }) {
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

  const proyectos = (dashboard.proyectos ?? []).filter(p =>
    projectFilter === null || projectFilter?.has(p.projectId)
  )
  const totalTareas       = proyectos.reduce((s, p) => s + (p.totalTareas ?? 0), 0)
  const tareasEnProgreso  = proyectos.reduce((s, p) => s + (p.tareasEnProgreso ?? 0), 0)
  const tareasCompletadas = proyectos.reduce((s, p) => s + (p.tareasCompletadas ?? 0), 0)

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard label="Proyectos"      value={proyectos.length}    color="slate"  />
        <StatCard label="Tareas totales" value={totalTareas}         color="blue"   />
        <StatCard label="En progreso"    value={tareasEnProgreso}    color="yellow" />
        <StatCard label="Completadas"    value={tareasCompletadas}   color="green"  />
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

function KpiTab({ projectFilter }) {
  const [projects, setProjects]       = useState([])
  const [selectedId, setSelectedId]   = useState('')
  const [kpi, setKpi]                 = useState(null)
  const [loadingKpi, setLoadingKpi]   = useState(false)
  const [error, setError]             = useState(null)

  useEffect(() => {
    projectService.getAll()
      .then(data => setProjects(Array.isArray(data) ? data : []))
      .catch(() => {})
  }, [])

  const handleSelect = async (e) => {
    const id = e.target.value
    setSelectedId(id)
    setKpi(null)
    setError(null)
    if (!id) return
    try {
      setLoadingKpi(true)
      const data = await reportService.getKpiByProject(id)
      setKpi(data)
    } catch (err) {
      setError(err.message || 'Error al cargar KPI.')
    } finally {
      setLoadingKpi(false)
    }
  }

  const visibleProjects = projectFilter === null
    ? projects
    : projects.filter(p => projectFilter?.has(p.id))

  return (
    <div className="space-y-6">
      <select value={selectedId} onChange={handleSelect}
        className="w-full max-w-sm rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-blue-400 shadow-sm">
        <option value="">— Selecciona un proyecto —</option>
        {visibleProjects.map(p => (
          <option key={p.id} value={p.id}>{p.nombre}</option>
        ))}
      </select>

      {loadingKpi && <p className="text-slate-500 text-center py-8">Cargando KPI…</p>}
      {error && <p className="text-red-600 text-center py-8">{error}</p>}

      {kpi && (
        <div className="space-y-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard label="Total tareas"  value={kpi.totalTareas}       color="slate"  />
            <StatCard label="Completadas"   value={kpi.tareasCompletadas} color="green"  />
            <StatCard label="En progreso"   value={kpi.tareasEnProgreso}  color="yellow" />
            <StatCard label="Pendientes"    value={kpi.tareasPendientes}  color="blue"   />
          </div>
          <div className="bg-white rounded-xl shadow p-6">
            <p className="text-sm text-slate-500 mb-2">Porcentaje de avance</p>
            <p className="text-4xl font-black text-slate-900">
              {typeof kpi.porcentajeAvance === 'number' ? `${Number(kpi.porcentajeAvance).toFixed(1)}%` : '—'}
            </p>
            <div className="mt-3 w-full bg-slate-200 rounded-full h-3">
              <div className="bg-blue-500 h-3 rounded-full transition-all"
                style={{ width: `${Math.min(kpi.porcentajeAvance ?? 0, 100)}%` }} />
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function TareasTab({ projectFilter }) {
  const [tasks, setTasks]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  const estadoBadge = {
    PENDIENTE:   'bg-slate-200 text-slate-700',
    EN_PROGRESO: 'bg-yellow-100 text-yellow-800',
    COMPLETADA:  'bg-green-100 text-green-700',
    CANCELADA:   'bg-red-100 text-red-700',
  }

  const prioridadBadge = {
    ALTA:  'bg-rose-100 text-rose-700',
    MEDIA: 'bg-orange-100 text-orange-700',
    BAJA:  'bg-slate-100 text-slate-600',
  }

  useEffect(() => {
    reportService.getTasksReport()
      .then(data => setTasks(Array.isArray(data) ? data : []))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="text-slate-500 text-center py-10">Cargando tareas…</p>
  if (error)   return <p className="text-red-600 text-center py-10">{error}</p>

  const visibleTasks = projectFilter === null
    ? tasks
    : tasks.filter(t => projectFilter?.has(t.projectId))

  return (
    <div className="bg-white rounded-xl shadow overflow-x-auto">
      <table className="min-w-full text-sm">
        <thead className="bg-slate-50 text-slate-600 uppercase text-xs">
          <tr>
            <th className="px-4 py-3 text-left">#</th>
            <th className="px-4 py-3 text-left">Tarea</th>
            <th className="px-4 py-3 text-center">Prioridad</th>
            <th className="px-4 py-3 text-center">Estado</th>
            <th className="px-4 py-3 text-center">Proyecto ID</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {visibleTasks.length === 0 && (
            <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-400">Sin tareas disponibles</td></tr>
          )}
          {visibleTasks.map(t => (
            <tr key={t.taskId} className="hover:bg-slate-50 transition-colors">
              <td className="px-4 py-3 text-slate-400">{t.taskId}</td>
              <td className="px-4 py-3 font-medium text-slate-800">{t.titulo}</td>
              <td className="px-4 py-3 text-center">
                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${prioridadBadge[t.prioridad] ?? 'bg-slate-100 text-slate-600'}`}>
                  {t.prioridad ?? '—'}
                </span>
              </td>
              <td className="px-4 py-3 text-center">
                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${estadoBadge[t.estado] ?? 'bg-slate-100 text-slate-600'}`}>
                  {t.estado ?? '—'}
                </span>
              </td>
              <td className="px-4 py-3 text-center text-slate-500">{t.projectId ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function AdminDashboard() {
  const { user } = useAuth()
  const [tab, setTab] = useState('resumen')
  // null = ADMIN (no filter); undefined = loading for PROJECT_LEAD; Set = lead's project IDs
  const [myProjectIds, setMyProjectIds] = useState(
    user?.role === 'PROJECT_LEAD' ? undefined : null
  )

  useEffect(() => {
    if (user?.role === 'PROJECT_LEAD') {
      projectService.getAll()
        .then(data => {
          const arr = Array.isArray(data) ? data : []
          const ids = new Set(
            arr.filter(p => Number(p.responsableId) === Number(user.id)).map(p => p.id)
          )
          setMyProjectIds(ids)
        })
        .catch(() => setMyProjectIds(new Set()))
    }
  }, [user])

  const tabs = [
    { key: 'resumen',  label: 'Resumen' },
    { key: 'kpi',      label: 'KPI por Proyecto' },
    { key: 'tareas',   label: 'Reporte de Tareas' },
  ]

  return (
    <div className="space-y-6">
      <div className="flex gap-2 flex-wrap">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`rounded-xl px-5 py-2 text-sm font-semibold transition ${tab === t.key ? 'bg-slate-950 text-white' : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {myProjectIds === undefined ? (
        <p className="text-slate-500 text-center py-10">Cargando proyectos...</p>
      ) : (
        <>
          {tab === 'resumen' && <ResumenTab projectFilter={myProjectIds} />}
          {tab === 'kpi'     && <KpiTab projectFilter={myProjectIds} />}
          {tab === 'tareas'  && <TareasTab projectFilter={myProjectIds} />}
        </>
      )}
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

  const total = tasks.length
  const completadas = tasks.filter(t => t.estado === 'COMPLETADA').length
  const porcentaje = total > 0 ? Math.round((completadas / total) * 100) : 0

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-slate-700">Mis tareas asignadas</h2>

      <div className="grid grid-cols-3 gap-4">
        <div className="rounded-xl bg-blue-50 p-4 text-center">
          <p className="text-3xl font-black text-blue-800">{total}</p>
          <p className="mt-1 text-xs text-blue-600">Total asignadas</p>
        </div>
        <div className="rounded-xl bg-emerald-50 p-4 text-center">
          <p className="text-3xl font-black text-emerald-800">{completadas}</p>
          <p className="mt-1 text-xs text-emerald-600">Completadas</p>
        </div>
        <div className="rounded-xl bg-slate-100 p-4 text-center">
          <p className="text-3xl font-black text-slate-800">{porcentaje}%</p>
          <p className="mt-1 text-xs text-slate-600">Avance</p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow p-4">
        <div className="flex justify-between text-xs text-slate-500 mb-1.5">
          <span>Progreso general</span>
          <span>{completadas} de {total} completadas</span>
        </div>
        <div className="w-full bg-slate-200 rounded-full h-3">
          <div className="bg-emerald-500 h-3 rounded-full transition-all duration-500"
            style={{ width: `${porcentaje}%` }} />
        </div>
      </div>

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

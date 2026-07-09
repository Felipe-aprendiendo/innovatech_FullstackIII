import { useState, useEffect, useCallback, useMemo } from 'react'
import { useAuth } from '../../context/AuthContext'
import taskService from '../../services/taskService'
import projectService from '../../services/projectService'
import userService from '../../services/userService'
import TaskFormModal from '../../components/TaskFormModal'

const estadoStyles = {
  PENDIENTE:   'bg-slate-100 text-slate-700 border-slate-300',
  EN_PROGRESO: 'bg-sky-100 text-sky-700 border-sky-200',
  COMPLETADA:  'bg-emerald-100 text-emerald-700 border-emerald-200',
  CANCELADA:   'bg-red-100 text-red-700 border-red-200',
}

const prioridadStyles = {
  ALTA:  'bg-rose-500 text-white',
  MEDIA: 'bg-amber-400 text-slate-950',
  BAJA:  'bg-emerald-400 text-slate-950',
}

const nextEstados = {
  PENDIENTE:   ['EN_PROGRESO'],
  EN_PROGRESO: ['PENDIENTE', 'COMPLETADA', 'CANCELADA'],
  COMPLETADA:  [],
  CANCELADA:   [],
}

export default function TasksPage() {
  const { user } = useAuth()
  const [tasks, setTasks] = useState([])
  const [projects, setProjects] = useState([])
  const [userMap, setUserMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroProyecto, setFiltroProyecto] = useState('')
  const [filtroResponsable, setFiltroResponsable] = useState('')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [editTask, setEditTask] = useState(null)
  const [commentTask, setCommentTask] = useState(null)
  const [commentText, setCommentText] = useState('')
  const [commentError, setCommentError] = useState('')
  const [loadingComments, setLoadingComments] = useState(false)

  const canCreate = user?.role === 'ADMIN' || user?.role === 'PROJECT_LEAD'
  const canDelete = user?.role === 'ADMIN'
  const canChangeStatus = (task) =>
    user?.role === 'ADMIN' || user?.role === 'PROJECT_LEAD' || Number(task.responsableId) === Number(user?.id)

  // Projects available for filter dropdown (role-filtered)
  const filtroProyectos = useMemo(() => {
    if (user?.role === 'ADMIN') return projects
    if (user?.role === 'PROJECT_LEAD') return projects.filter(p => Number(p.responsableId) === Number(user?.id))
    return []
  }, [projects, user])

  // Client-side role-based visibility filter applied after fetch
  const visibleTasks = useMemo(() => {
    if (user?.role === 'USER') {
      return tasks.filter(t => Number(t.responsableId) === Number(user?.id))
    }
    if (user?.role === 'PROJECT_LEAD') {
      const myProjectIds = new Set(
        projects.filter(p => Number(p.responsableId) === Number(user?.id)).map(p => p.id)
      )
      if (myProjectIds.size === 0) return tasks
      return tasks.filter(t => myProjectIds.has(t.projectId))
    }
    return tasks
  }, [tasks, projects, user])

  const getProjectName = (projectId) => {
    const p = projects.find(p => p.id === projectId || p.id === Number(projectId))
    return p?.nombre ?? `Proyecto #${projectId}`
  }

  const getUserName = (id) => {
    const name = userMap[id] ?? userMap[Number(id)]
    return name ? ` (${name})` : ''
  }

  // Sorted users list for responsable filter dropdown
  const sortedUsers = useMemo(() =>
    Object.entries(userMap)
      .map(([id, name]) => ({ id: Number(id), name }))
      .sort((a, b) => a.name.localeCompare(b.name)),
    [userMap]
  )

  useEffect(() => {
    projectService.getAll()
      .then(data => setProjects(Array.isArray(data) ? data : []))
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      userService.getAll()
        .then(data => {
          const arr = Array.isArray(data) ? data : (data?.data ?? [])
          const map = {}
          arr.forEach(u => {
            map[u.id] = `${u.name ?? ''}${u.lastName ? ' ' + u.lastName : ''}`.trim()
          })
          setUserMap(map)
        })
        .catch(() => {})
    }
  }, [user?.role])

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const params = {}
      if (filtroProyecto) params.projectId = filtroProyecto
      if (filtroEstado) params.estado = filtroEstado
      if (filtroResponsable) params.responsableId = filtroResponsable
      const res = await taskService.getAll(params)
      setTasks(Array.isArray(res) ? res : (res?.data ?? []))
    } catch (err) {
      setError(err.message || 'Error al cargar tareas.')
    } finally {
      setLoading(false)
    }
  }, [filtroEstado, filtroProyecto, filtroResponsable])

  useEffect(() => { load() }, [load])

  const handleChangeStatus = async (task, nuevoEstado) => {
    if (!window.confirm(`¿Cambiar estado a ${nuevoEstado}?`)) return
    try {
      await taskService.updateStatus(task.id, nuevoEstado)
      load()
    } catch (err) {
      alert(err.message)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Eliminar esta tarea?')) return
    try {
      await taskService.delete(id)
      load()
    } catch (err) {
      alert(err.message)
    }
  }

  const openCommentModal = async (task) => {
    setCommentTask(task)
    setCommentText('')
    setCommentError('')
    try {
      setLoadingComments(true)
      const res = await taskService.getById(task.id)
      setCommentTask(res?.data ?? res)
    } catch {
      // keep the summary task data if fetch fails
    } finally {
      setLoadingComments(false)
    }
  }

  const handleAddComment = async (e) => {
    e.preventDefault()
    if (!commentText.trim()) return
    try {
      setCommentError('')
      await taskService.addComment(commentTask.id, commentText.trim())
      setCommentText('')
      const res = await taskService.getById(commentTask.id)
      setCommentTask(res?.data ?? res)
      load()
    } catch (err) {
      setCommentError(err.message)
    }
  }

  return (
    <>
      <main className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 text-slate-900">
        <section className="mx-auto w-full max-w-7xl px-6 py-10">

          <div className="mb-8 flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-3xl font-black text-slate-950">Tareas</h1>
              <p className="mt-1 text-sm text-slate-500">{visibleTasks.length} tarea(s) en total</p>
            </div>
            <div className="flex items-center gap-3 flex-wrap">
              {filtroProyectos.length > 0 && (
                <select value={filtroProyecto} onChange={e => setFiltroProyecto(e.target.value)}
                  className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none focus:border-blue-400">
                  <option value="">Todos los proyectos</option>
                  {filtroProyectos.map(p => (
                    <option key={p.id} value={p.id}>{p.nombre}</option>
                  ))}
                </select>
              )}
              <select value={filtroEstado} onChange={e => setFiltroEstado(e.target.value)}
                className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none focus:border-blue-400">
                <option value="">Todos los estados</option>
                <option value="PENDIENTE">Pendiente</option>
                <option value="EN_PROGRESO">En progreso</option>
                <option value="COMPLETADA">Completada</option>
                <option value="CANCELADA">Cancelada</option>
              </select>
              {user?.role === 'ADMIN' && sortedUsers.length > 0 && (
                <select value={filtroResponsable} onChange={e => setFiltroResponsable(e.target.value)}
                  className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none focus:border-blue-400">
                  <option value="">Todos los responsables</option>
                  {sortedUsers.map(u => (
                    <option key={u.id} value={u.id}>{u.name || `#${u.id}`}</option>
                  ))}
                </select>
              )}
              {canCreate && (
                <button onClick={() => { setEditTask(null); setIsFormOpen(true) }}
                  className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600 transition">
                  + Nueva tarea
                </button>
              )}
            </div>
          </div>

          {loading ? (
            <div className="flex min-h-[200px] items-center justify-center">
              <p className="text-slate-500">Cargando tareas...</p>
            </div>
          ) : error ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-center">
              <p className="text-red-700">{error}</p>
              <button onClick={load} className="mt-4 rounded-xl bg-red-600 px-4 py-2 text-sm font-bold text-white">Reintentar</button>
            </div>
          ) : visibleTasks.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center">
              <p className="text-slate-500">No hay tareas para mostrar.</p>
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {visibleTasks.map(task => (
                <div key={task.id} className="rounded-2xl border border-slate-200 bg-white p-5">
                  <div className="flex items-start justify-between gap-4 flex-wrap">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <span className="text-xs text-slate-400">#{task.id}</span>
                        <span className={`rounded-full border px-2.5 py-0.5 text-xs font-bold uppercase ${estadoStyles[task.estado] ?? ''}`}>
                          {task.estado}
                        </span>
                        <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold uppercase ${prioridadStyles[task.prioridad] ?? ''}`}>
                          {task.prioridad}
                        </span>
                      </div>
                      <p className="text-base font-semibold text-slate-900">{task.titulo}</p>
                      {task.descripcion && <p className="mt-1 text-sm text-slate-500">{task.descripcion}</p>}
                      <div className="mt-2 flex gap-4 text-xs text-slate-400 flex-wrap">
                        <span className="font-medium text-slate-500">{getProjectName(task.projectId)}</span>
                        {task.fechaLimite && <span>Fecha límite: {task.fechaLimite}</span>}
                        {task.responsableId && (
                          <span>Responsable: #{task.responsableId}{getUserName(task.responsableId)}</span>
                        )}
                        {task.comentarios?.length > 0 && (
                          <button onClick={() => openCommentModal(task)} className="text-blue-500 hover:underline">
                            {task.comentarios.length} comentario(s)
                          </button>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-2 flex-wrap">
                      {canChangeStatus(task) && (nextEstados[task.estado] ?? []).map(s => (
                        <button key={s} onClick={() => handleChangeStatus(task, s)}
                          className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100 transition">
                          → {s}
                        </button>
                      ))}
                      {canCreate && task.estado !== 'COMPLETADA' && task.estado !== 'CANCELADA' && (
                        <button onClick={() => { setEditTask(task); setIsFormOpen(true) }}
                          className="rounded-xl border border-blue-200 px-3 py-1.5 text-xs font-semibold text-blue-700 hover:bg-blue-50 transition">
                          Editar
                        </button>
                      )}
                      <button onClick={() => openCommentModal(task)}
                        className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 transition">
                        Comentar
                      </button>
                      {canDelete && (
                        <button onClick={() => handleDelete(task.id)}
                          className="rounded-xl border border-red-200 px-3 py-1.5 text-xs font-semibold text-red-600 hover:bg-red-50 transition">
                          Eliminar
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>

      {isFormOpen && (
        <TaskFormModal
          task={editTask}
          onClose={() => setIsFormOpen(false)}
          onSaved={() => { setIsFormOpen(false); load() }}
        />
      )}

      {commentTask && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4">
          <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
              <h3 className="font-semibold text-slate-900">Comentarios — {commentTask.titulo}</h3>
              <button onClick={() => setCommentTask(null)} className="text-slate-400 hover:text-slate-600 text-xl">×</button>
            </div>
            <div className="max-h-64 overflow-y-auto px-6 py-4 space-y-3">
              {loadingComments
                ? <p className="text-sm text-slate-400 text-center py-2">Cargando comentarios...</p>
                : (commentTask.comentarios ?? []).length === 0
                  ? <p className="text-sm text-slate-400">Sin comentarios aún.</p>
                  : commentTask.comentarios.map(c => (
                    <div key={c.id} className="rounded-xl bg-slate-50 p-3">
                      <p className="text-xs text-slate-400 mb-1">Usuario #{c.userId}{getUserName(c.userId)}</p>
                      <p className="text-sm text-slate-700">{c.contenido}</p>
                    </div>
                  ))
              }
            </div>
            <form onSubmit={handleAddComment} className="border-t border-slate-200 px-6 py-4">
              <textarea value={commentText} onChange={e => setCommentText(e.target.value)} rows={2} placeholder="Escribe un comentario..."
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-blue-400 resize-none" />
              {commentError && <p className="mt-1 text-xs text-red-600">{commentError}</p>}
              <div className="mt-3 flex justify-end gap-2">
                <button type="button" onClick={() => setCommentTask(null)}
                  className="rounded-xl border border-slate-200 px-4 py-2 text-sm text-slate-600">Cerrar</button>
                <button type="submit"
                  className="rounded-xl bg-slate-950 px-4 py-2 text-sm font-bold text-white hover:bg-blue-600">Comentar</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}

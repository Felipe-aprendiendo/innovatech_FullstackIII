import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import projectService from '../services/projectService'
import ProjectEditModal from '../pages/projects/ProjectEditModal'

const priorityStyles = {
  ALTA: 'bg-rose-500 text-white',
  MEDIA: 'bg-amber-400 text-slate-950',
  BAJA: 'bg-emerald-400 text-slate-950',
}

const statusStyles = {
  PLANIFICADO: 'border-slate-300 bg-slate-100 text-slate-700',
  EN_PROGRESO: 'border-sky-200 bg-sky-100 text-sky-700',
  COMPLETADO: 'border-emerald-200 bg-emerald-100 text-emerald-700',
  CERRADO: 'border-zinc-300 bg-zinc-200 text-zinc-700',
}

const nextStatuses = {
  PLANIFICADO: ['EN_PROGRESO'],
  EN_PROGRESO: ['COMPLETADO'],
  COMPLETADO: [],
  CERRADO: [],
}

export default function ProjectCard({ project, onRefresh, responsableNombre }) {
  const { user } = useAuth()
  const [isEditOpen, setIsEditOpen] = useState(false)
  const [actionError, setActionError] = useState('')

  const canModify = user?.role === 'ADMIN' ||
    (user?.role === 'PROJECT_LEAD' && project.responsableId === user.id)
  const canDelete = user?.role === 'ADMIN'

  const isClosed = project.estado === 'CERRADO' || project.estado === 'COMPLETADO'
  const availableNext = nextStatuses[project.estado] ?? []

  const handleChangeStatus = async (nuevoEstado) => {
    if (!window.confirm(`¿Cambiar estado a ${nuevoEstado}?`)) return
    try {
      setActionError('')
      await projectService.changeStatus(project.id, nuevoEstado)
      onRefresh()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleClose = async () => {
    if (!window.confirm('¿Cerrar este proyecto? Esta acción no se puede deshacer.')) return
    try {
      setActionError('')
      await projectService.close(project.id)
      onRefresh()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm(`¿Eliminar el proyecto "${project.nombre}"? Esta acción no se puede deshacer.`)) return
    try {
      setActionError('')
      await projectService.delete(project.id)
      onRefresh()
    } catch (err) {
      setActionError(err.message)
    }
  }

  return (
    <>
      <article className="group relative overflow-hidden rounded-[2rem] border border-white/70 bg-slate-950 p-6 text-white shadow-[0_24px_80px_rgba(15,23,42,0.28)] transition duration-300 hover:-translate-y-1">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_rgba(250,204,21,0.24),_transparent_30%)] opacity-90" />

        <div className="relative flex h-full flex-col">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-400">Proyecto #{project.id}</p>
              <h2 className="mt-3 text-2xl font-black tracking-tight">{project.nombre}</h2>
            </div>
            <span className={`inline-flex rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.22em] ${priorityStyles[project.prioridad] ?? 'bg-slate-300 text-slate-900'}`}>
              {project.prioridad}
            </span>
          </div>

          <div className="mt-6 rounded-[1.5rem] border border-white/10 bg-white/6 p-5 backdrop-blur-sm">
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-400">Descripción</p>
            <p className="mt-3 min-h-16 text-sm leading-7 text-slate-200">
              {project.descripcion || 'Sin descripción disponible.'}
            </p>
            <p className="mt-3 text-xs text-slate-400">
              Responsable #{project.responsableId}{responsableNombre ? ` — ${responsableNombre}` : ''}
            </p>
          </div>

          <div className="mt-6 flex flex-wrap items-center justify-between gap-4">
            <span className={`inline-flex rounded-full border px-4 py-2 text-xs font-bold uppercase tracking-[0.22em] ${statusStyles[project.estado] ?? 'border-slate-300 bg-slate-100 text-slate-700'}`}>
              {project.estado}
            </span>

            {canModify && !isClosed && (
              <div className="flex flex-wrap gap-2">
                <button onClick={() => setIsEditOpen(true)}
                  className="rounded-full bg-white/10 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-amber-400 hover:text-slate-950">
                  Editar
                </button>
                {availableNext.map(s => (
                  <button key={s} onClick={() => handleChangeStatus(s)}
                    className="rounded-full bg-sky-500/80 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-sky-400">
                    → {s}
                  </button>
                ))}
                <button onClick={handleClose}
                  className="rounded-full bg-red-500/70 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-red-500">
                  Cerrar
                </button>
              </div>
            )}
          </div>

          {canDelete && (
            <div className="mt-4 flex justify-end">
              <button onClick={handleDelete}
                className="rounded-full bg-red-600/80 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-red-500">
                Eliminar proyecto
              </button>
            </div>
          )}

          {actionError && (
            <p className="mt-3 text-xs text-red-400">{actionError}</p>
          )}
        </div>
      </article>

      {isEditOpen && (
        <ProjectEditModal
          project={project}
          onClose={() => setIsEditOpen(false)}
          onUpdated={onRefresh}
        />
      )}
    </>
  )
}

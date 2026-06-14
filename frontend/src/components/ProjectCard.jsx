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

function ProjectCard({ project }) {
  const priorityClass =
    priorityStyles[project.prioridad] ?? 'bg-slate-300 text-slate-900'
  const statusClass =
    statusStyles[project.estado] ?? 'border-slate-300 bg-slate-100 text-slate-700'

  return (
    <article className="group relative overflow-hidden rounded-[2rem] border border-white/70 bg-slate-950 p-6 text-white shadow-[0_24px_80px_rgba(15,23,42,0.28)] transition duration-300 hover:-translate-y-1 hover:shadow-[0_30px_90px_rgba(15,23,42,0.34)]">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_rgba(250,204,21,0.24),_transparent_30%),radial-gradient(circle_at_bottom_left,_rgba(56,189,248,0.18),_transparent_28%)] opacity-90" />

      <div className="relative flex h-full flex-col">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-400">
              Proyecto #{project.id}
            </p>
            <h2 className="mt-3 text-2xl font-black tracking-tight text-white">
              {project.nombre}
            </h2>
          </div>

          <span
            className={`inline-flex rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.22em] ${priorityClass}`}
          >
            {project.prioridad}
          </span>
        </div>

        <div className="mt-6 rounded-[1.5rem] border border-white/10 bg-white/6 p-5 backdrop-blur-sm">
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-400">
            Descripcion del proyecto
          </p>
          <p className="mt-3 min-h-24 text-sm leading-7 text-slate-200">
            {project.descripcion || 'Sin descripcion disponible.'}
          </p>
        </div>

        <div className="mt-6 flex items-center justify-between gap-4">
          <span
            className={`inline-flex rounded-full border px-4 py-2 text-xs font-bold uppercase tracking-[0.22em] ${statusClass}`}
          >
            {project.estado}
          </span>
        </div>
      </div>
    </article>
  )
}

export default ProjectCard

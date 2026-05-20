function ProjectFormModal({
  isOpen,
  formValues,
  isSubmitting,
  errorMessage,
  onClose,
  onChange,
  onSubmit,
}) {
  if (!isOpen) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4 py-8 backdrop-blur-sm">
      <div className="w-full max-w-3xl overflow-hidden rounded-[2rem] border border-white/70 bg-white shadow-[0_30px_120px_rgba(15,23,42,0.32)]">
        <div className="flex items-start justify-between gap-4 border-b border-slate-200 bg-[linear-gradient(135deg,_rgba(250,204,21,0.14),_rgba(56,189,248,0.10))] px-6 py-6 sm:px-8">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-500">
              Nuevo proyecto
            </p>
            <h2 className="mt-3 text-3xl font-black tracking-tight text-slate-950">
              Crear proyecto
            </h2>
            <p className="mt-2 text-sm leading-6 text-slate-600">
              Completa los campos requeridos. El proyecto se creara con estado
              inicial <span className="font-semibold">PLANIFICADO</span>.
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-slate-950 hover:text-slate-950 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Cerrar
          </button>
        </div>

        <form onSubmit={onSubmit} className="space-y-6 px-6 py-6 sm:px-8 sm:py-8">
          <div className="grid gap-5 md:grid-cols-2">
            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-semibold text-slate-700">
                Nombre del proyecto
              </span>
              <input
                required
                type="text"
                name="nombre"
                value={formValues.nombre}
                onChange={onChange}
                placeholder="Ej. Plataforma de seguimiento academico"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-amber-400 focus:bg-white"
              />
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-semibold text-slate-700">
                Descripcion
              </span>
              <textarea
                name="descripcion"
                value={formValues.descripcion}
                onChange={onChange}
                rows="4"
                placeholder="Describe brevemente el objetivo del proyecto"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-amber-400 focus:bg-white"
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-semibold text-slate-700">
                Prioridad
              </span>
              <select
                name="prioridad"
                value={formValues.prioridad}
                onChange={onChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-amber-400 focus:bg-white"
              >
                <option value="ALTA">ALTA</option>
                <option value="MEDIA">MEDIA</option>
                <option value="BAJA">BAJA</option>
              </select>
            </label>

            <label>
              <span className="mb-2 block text-sm font-semibold text-slate-700">
                Responsable ID
              </span>
              <input
                required
                min="1"
                type="number"
                name="responsableId"
                value={formValues.responsableId}
                onChange={onChange}
                placeholder="Ej. 12"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-amber-400 focus:bg-white"
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-semibold text-slate-700">
                Fecha de inicio
              </span>
              <input
                type="date"
                name="fechaInicio"
                value={formValues.fechaInicio}
                onChange={onChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-amber-400 focus:bg-white"
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-semibold text-slate-700">
                Fecha de fin
              </span>
              <input
                type="date"
                name="fechaFin"
                value={formValues.fechaFin}
                onChange={onChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-amber-400 focus:bg-white"
              />
            </label>
          </div>

          {errorMessage ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
              {errorMessage}
            </div>
          ) : null}

          <div className="flex flex-col-reverse gap-3 border-t border-slate-200 pt-6 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="rounded-full border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-950 hover:text-slate-950 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-full bg-slate-950 px-6 py-3 text-sm font-bold text-white transition hover:bg-amber-400 hover:text-slate-950 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isSubmitting ? 'Guardando...' : 'Guardar Proyecto'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ProjectFormModal

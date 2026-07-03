import { useState, useEffect } from 'react'
import projectService from '../../services/projectService'

export default function ProjectEditModal({ project, onClose, onUpdated }) {
  const [form, setForm] = useState({
    nombre: '',
    descripcion: '',
    prioridad: 'MEDIA',
    fechaInicio: '',
    fechaFin: '',
    responsableId: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (project) {
      setForm({
        nombre: project.nombre ?? '',
        descripcion: project.descripcion ?? '',
        prioridad: project.prioridad ?? 'MEDIA',
        fechaInicio: project.fechaInicio ?? '',
        fechaFin: project.fechaFin ?? '',
        responsableId: project.responsableId ?? '',
      })
    }
  }, [project])

  if (!project) return null

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.nombre.trim()) { setError('El nombre es obligatorio.'); return }

    try {
      setSubmitting(true)
      setError('')
      await projectService.update(project.id, {
        nombre: form.nombre.trim(),
        descripcion: form.descripcion.trim(),
        prioridad: form.prioridad,
        fechaInicio: form.fechaInicio || null,
        fechaFin: form.fechaFin || null,
        responsableId: Number(form.responsableId),
      })
      onUpdated()
      onClose()
    } catch (err) {
      setError(err.message || 'Error al actualizar el proyecto.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4 backdrop-blur-sm">
      <div className="w-full max-w-2xl overflow-hidden rounded-[2rem] border border-white/70 bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-6 py-5">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-slate-500">Editar proyecto</p>
            <h2 className="mt-1 text-2xl font-black text-slate-950">{project.nombre}</h2>
          </div>
          <button onClick={onClose} disabled={submitting}
            className="rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-600 hover:border-slate-950 disabled:opacity-50">
            Cerrar
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 px-6 py-6">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="md:col-span-2">
              <span className="mb-1 block text-sm font-semibold text-slate-700">Nombre del proyecto</span>
              <input required type="text" name="nombre" value={form.nombre} onChange={handleChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>
            <label className="md:col-span-2">
              <span className="mb-1 block text-sm font-semibold text-slate-700">Descripción</span>
              <textarea name="descripcion" value={form.descripcion} onChange={handleChange} rows={3}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>
            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">Prioridad</span>
              <select name="prioridad" value={form.prioridad} onChange={handleChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none">
                <option value="ALTA">ALTA</option>
                <option value="MEDIA">MEDIA</option>
                <option value="BAJA">BAJA</option>
              </select>
            </label>
            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">Responsable ID</span>
              <input type="number" name="responsableId" value={form.responsableId} onChange={handleChange} min="1"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none" />
            </label>
            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">Fecha inicio</span>
              <input type="date" name="fechaInicio" value={form.fechaInicio} onChange={handleChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none" />
            </label>
            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">Fecha fin</span>
              <input type="date" name="fechaFin" value={form.fechaFin} onChange={handleChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none" />
            </label>
          </div>

          {error && <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

          <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">
            <button type="button" onClick={onClose} disabled={submitting}
              className="rounded-full border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 hover:border-slate-950 disabled:opacity-50">
              Cancelar
            </button>
            <button type="submit" disabled={submitting}
              className="rounded-full bg-slate-950 px-6 py-3 text-sm font-bold text-white hover:bg-amber-400 hover:text-slate-950 disabled:opacity-50">
              {submitting ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

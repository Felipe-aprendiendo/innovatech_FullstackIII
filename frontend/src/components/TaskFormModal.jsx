import { useState, useEffect } from 'react'
import taskService from '../services/taskService'

const emptyForm = {
  titulo: '',
  descripcion: '',
  prioridad: 'MEDIA',
  projectId: '',
  responsableId: '',
  fechaLimite: '',
}

export default function TaskFormModal({ task, onClose, onSaved }) {
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const isEdit = !!task

  useEffect(() => {
    if (task) {
      setForm({
        titulo: task.titulo ?? '',
        descripcion: task.descripcion ?? '',
        prioridad: task.prioridad ?? 'MEDIA',
        projectId: task.projectId ?? '',
        responsableId: task.responsableId ?? '',
        fechaLimite: task.fechaLimite ?? '',
      })
    } else {
      setForm(emptyForm)
    }
  }, [task])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.titulo.trim()) { setError('El título es obligatorio.'); return }
    if (!form.projectId) { setError('El ID del proyecto es obligatorio.'); return }
    if (!form.responsableId) { setError('El ID del responsable es obligatorio.'); return }

    const payload = {
      titulo: form.titulo.trim(),
      descripcion: form.descripcion.trim(),
      prioridad: form.prioridad,
      projectId: Number(form.projectId),
      responsableId: Number(form.responsableId),
      fechaLimite: form.fechaLimite || null,
    }

    try {
      setSubmitting(true)
      setError('')
      if (isEdit) {
        await taskService.update(task.id, payload)
      } else {
        await taskService.create(payload)
      }
      onSaved()
    } catch (err) {
      setError(err.message || 'Error al guardar la tarea.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4 backdrop-blur-sm">
      <div className="w-full max-w-2xl overflow-hidden rounded-[2rem] border border-white/70 bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-6 py-5">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-slate-500">
              {isEdit ? 'Editar tarea' : 'Nueva tarea'}
            </p>
            <h2 className="mt-1 text-2xl font-black text-slate-950">
              {isEdit ? task.titulo : 'Crear tarea'}
            </h2>
          </div>
          <button onClick={onClose} disabled={submitting}
            className="rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-600 hover:border-slate-950 disabled:opacity-50">
            Cerrar
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 px-6 py-6">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="md:col-span-2">
              <span className="mb-1 block text-sm font-semibold text-slate-700">Título</span>
              <input required type="text" name="titulo" value={form.titulo} onChange={handleChange}
                placeholder="Ej: Diseñar pantalla de login"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>

            <label className="md:col-span-2">
              <span className="mb-1 block text-sm font-semibold text-slate-700">Descripción</span>
              <textarea name="descripcion" value={form.descripcion} onChange={handleChange} rows={3}
                placeholder="Describe brevemente la tarea"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400 resize-none" />
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
              <span className="mb-1 block text-sm font-semibold text-slate-700">Fecha límite</span>
              <input type="date" name="fechaLimite" value={form.fechaLimite} onChange={handleChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none" />
            </label>

            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">ID del proyecto</span>
              <input required type="number" name="projectId" value={form.projectId} onChange={handleChange} min="1"
                placeholder="Ej: 1"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>

            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">ID del responsable</span>
              <input required type="number" name="responsableId" value={form.responsableId} onChange={handleChange} min="1"
                placeholder="Ej: 2"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>
          </div>

          {error && (
            <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
          )}

          <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">
            <button type="button" onClick={onClose} disabled={submitting}
              className="rounded-full border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 hover:border-slate-950 disabled:opacity-50">
              Cancelar
            </button>
            <button type="submit" disabled={submitting}
              className="rounded-full bg-slate-950 px-6 py-3 text-sm font-bold text-white hover:bg-amber-400 hover:text-slate-950 disabled:opacity-50">
              {submitting ? 'Guardando...' : (isEdit ? 'Guardar cambios' : 'Crear tarea')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}


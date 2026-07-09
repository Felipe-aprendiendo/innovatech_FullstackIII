import { useState } from 'react'
import authService from '../services/authService'

export default function ChangePasswordModal({ onClose }) {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleChange = (e) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (form.newPassword !== form.confirm) {
      setError('Las contraseñas nuevas no coinciden.')
      return
    }
    try {
      setSubmitting(true)
      setError('')
      await authService.changePassword(form.currentPassword, form.newPassword)
      setSuccess(true)
    } catch (err) {
      setError(err.message || 'Error al cambiar contraseña.')
    } finally {
      setSubmitting(false)
    }
  }

  const fields = [
    { name: 'currentPassword', label: 'Contraseña actual' },
    { name: 'newPassword',     label: 'Nueva contraseña' },
    { name: 'confirm',         label: 'Confirmar nueva contraseña' },
  ]

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h3 className="font-semibold text-slate-900">Cambiar contraseña</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-xl leading-none">×</button>
        </div>

        {success ? (
          <div className="px-6 py-10 text-center space-y-4">
            <p className="text-emerald-600 font-semibold">Contraseña actualizada correctamente.</p>
            <button onClick={onClose}
              className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600">
              Cerrar
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
            {fields.map(({ name, label }) => (
              <label key={name} className="block">
                <span className="mb-1 block text-sm font-semibold text-slate-700">{label}</span>
                <input
                  type="password"
                  name={name}
                  value={form[name]}
                  onChange={handleChange}
                  required
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-blue-400"
                />
              </label>
            ))}

            {error && <p className="text-sm text-red-600">{error}</p>}

            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={onClose} disabled={submitting}
                className="rounded-xl border border-slate-200 px-4 py-2 text-sm text-slate-600 disabled:opacity-50">
                Cancelar
              </button>
              <button type="submit" disabled={submitting}
                className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600 disabled:opacity-50">
                {submitting ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

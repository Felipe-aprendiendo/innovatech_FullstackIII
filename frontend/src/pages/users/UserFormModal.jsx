import { useState, useEffect } from 'react'
import userService from '../../services/userService'

const emptyForm = { name: '', lastName: '', email: '', password: '', roleId: '' }

export default function UserFormModal({ user, onClose, onSaved }) {
  const [form, setForm] = useState(emptyForm)
  const [roles, setRoles] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const isEdit = !!user

  useEffect(() => {
    userService.getRoles().then(setRoles).catch(() => {})
  }, [])

  useEffect(() => {
    setForm(user
      ? {
          name:     user.name ?? '',
          lastName: user.lastName ?? '',
          email:    user.email ?? '',
          password: '',
          roleId:   String(user.roles?.[0]?.id ?? ''),
        }
      : emptyForm
    )
    setError('')
  }, [user])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name.trim() || !form.email.trim()) { setError('Nombre y email son obligatorios.'); return }
    if (!isEdit && !form.password) { setError('La contraseña es obligatoria al crear un usuario.'); return }

    const payload = {
      name:     form.name.trim(),
      lastName: form.lastName.trim(),
      email:    form.email.trim(),
      roleIds:  form.roleId ? [Number(form.roleId)] : [],
      ...(form.password ? { password: form.password } : {}),
    }

    try {
      setSubmitting(true)
      setError('')
      if (isEdit) {
        await userService.update(user.id, payload)
      } else {
        await userService.create(payload)
      }
      onSaved()
    } catch (err) {
      setError(err.message || 'Error al guardar el usuario.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4 backdrop-blur-sm">
      <div className="w-full max-w-lg overflow-hidden rounded-[2rem] border border-white/70 bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-6 py-5">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-slate-500">
              {isEdit ? 'Editar usuario' : 'Nuevo usuario'}
            </p>
            <h2 className="mt-1 text-xl font-black text-slate-950">
              {isEdit ? `${user.name} ${user.lastName ?? ''}` : 'Crear cuenta'}
            </h2>
          </div>
          <button onClick={onClose} disabled={submitting}
            className="rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-600 hover:border-slate-950 disabled:opacity-50">
            Cerrar
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 px-6 py-6">
          <div className="grid gap-4 sm:grid-cols-2">
            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">Nombre</span>
              <input required type="text" name="name" value={form.name} onChange={handleChange}
                placeholder="Juan"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>
            <label>
              <span className="mb-1 block text-sm font-semibold text-slate-700">Apellido</span>
              <input type="text" name="lastName" value={form.lastName} onChange={handleChange}
                placeholder="Pérez"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>
            <label className="sm:col-span-2">
              <span className="mb-1 block text-sm font-semibold text-slate-700">Email</span>
              <input required type="email" name="email" value={form.email} onChange={handleChange}
                placeholder="juan@empresa.com"
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
            </label>
            {!isEdit && (
              <label className="sm:col-span-2">
                <span className="mb-1 block text-sm font-semibold text-slate-700">Contraseña</span>
                <input required type="password" name="password" value={form.password} onChange={handleChange}
                  placeholder="Mínimo 8 caracteres, mayúscula, número y símbolo"
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-400" />
              </label>
            )}
            <label className="sm:col-span-2">
              <span className="mb-1 block text-sm font-semibold text-slate-700">Rol</span>
              <select name="roleId" value={form.roleId} onChange={handleChange}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none">
                <option value="">Sin rol</option>
                {roles.map(r => (
                  <option key={r.id} value={r.id}>
                    {r.name.replace('ROLE_', '')}
                  </option>
                ))}
              </select>
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
              {submitting ? 'Guardando...' : (isEdit ? 'Guardar cambios' : 'Crear usuario')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

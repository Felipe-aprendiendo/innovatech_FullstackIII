import { useState, useEffect, useCallback } from 'react'
import userService from '../../services/userService'
import UserFormModal from './UserFormModal'

const rolStyles = {
  ADMIN:        'bg-violet-100 text-violet-700',
  PROJECT_LEAD: 'bg-blue-100 text-blue-700',
  USER:         'bg-slate-100 text-slate-600',
}

function primaryRole(user) {
  return user.roles?.[0]?.name?.replace('ROLE_', '') ?? '—'
}

export default function UsersPage() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [editUser, setEditUser] = useState(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const res = await userService.getAll()
      setUsers(Array.isArray(res) ? res : (res?.data ?? []))
    } catch (err) {
      setError(err.message || 'Error al cargar usuarios.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handleToggle = async (user) => {
    const accion = user.enabled ? 'desactivar' : 'activar'
    if (!window.confirm(`¿${accion} al usuario ${user.name}?`)) return
    try {
      await userService.toggle(user.id)
      load()
    } catch (err) {
      alert(err.message)
    }
  }

  const handleDelete = async (user) => {
    if (!window.confirm(`¿Eliminar a ${user.name}? Esta acción no se puede deshacer.`)) return
    try {
      await userService.delete(user.id)
      load()
    } catch (err) {
      alert(err.message)
    }
  }

  return (
    <>
      <main className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
        <section className="mx-auto w-full max-w-6xl px-6 py-10">

          <div className="mb-6 flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-black text-slate-950">Usuarios</h1>
              <p className="mt-1 text-sm text-slate-500">{users.length} usuario(s) registrado(s)</p>
            </div>
            <button onClick={() => { setEditUser(null); setIsFormOpen(true) }}
              className="rounded-xl bg-slate-950 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-600 transition">
              + Nuevo usuario
            </button>
          </div>

          {loading ? (
            <div className="flex min-h-[200px] items-center justify-center">
              <p className="text-slate-500">Cargando usuarios...</p>
            </div>
          ) : error ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-center">
              <p className="text-red-700">{error}</p>
              <button onClick={load} className="mt-4 rounded-xl bg-red-600 px-4 py-2 text-sm font-bold text-white">Reintentar</button>
            </div>
          ) : (
            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 w-8">#</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">Nombre</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">Email</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 w-32">Rol</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 w-24">Estado</th>
                    <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-slate-500 w-36">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {users.map(u => {
                    const rol = primaryRole(u)
                    return (
                      <tr key={u.id} className="hover:bg-slate-50 transition">
                        <td className="px-4 py-3 text-slate-400">{u.id}</td>
                        <td className="px-4 py-3 font-medium text-slate-900 truncate">{u.name} {u.lastName}</td>
                        <td className="px-4 py-3 text-slate-600 truncate">{u.email}</td>
                        <td className="px-4 py-3">
                          <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${rolStyles[rol] ?? 'bg-slate-100 text-slate-600'}`}>
                            {rol}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`text-xs font-semibold ${u.enabled ? 'text-emerald-600' : 'text-red-500'}`}>
                            {u.enabled ? 'ACTIVO' : 'INACTIVO'}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-1.5">
                            <button onClick={() => { setEditUser(u); setIsFormOpen(true) }}
                              className="rounded-lg border border-slate-200 px-2.5 py-1 text-xs font-semibold text-slate-600 hover:bg-slate-100">
                              Editar
                            </button>
                            <button onClick={() => handleToggle(u)}
                              className={`rounded-lg border px-2.5 py-1 text-xs font-semibold transition ${u.enabled ? 'border-amber-200 text-amber-700 hover:bg-amber-50' : 'border-emerald-200 text-emerald-700 hover:bg-emerald-50'}`}>
                              {u.enabled ? 'Desactivar' : 'Activar'}
                            </button>
                            <button onClick={() => handleDelete(u)}
                              className="rounded-lg border border-red-200 px-2.5 py-1 text-xs font-semibold text-red-600 hover:bg-red-50">
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      {isFormOpen && (
        <UserFormModal
          user={editUser}
          onClose={() => setIsFormOpen(false)}
          onSaved={() => { setIsFormOpen(false); load() }}
        />
      )}
    </>
  )
}


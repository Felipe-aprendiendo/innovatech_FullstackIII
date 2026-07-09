import { useState, useEffect, useCallback } from 'react'
import roleService from '../../services/roleService'
import permissionService from '../../services/permissionService'

function PermissionModal({ permission, onClose, onSaved }) {
  const [form, setForm] = useState({ name: '', description: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (permission) setForm({ name: permission.name ?? '', description: permission.description ?? '' })
    else setForm({ name: '', description: '' })
  }, [permission])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) { setError('El nombre es obligatorio.'); return }
    try {
      setSubmitting(true)
      setError('')
      permission ? await permissionService.update(permission.id, form) : await permissionService.create(form)
      onSaved()
    } catch (err) {
      setError(err.message || 'Error al guardar permiso.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h3 className="font-semibold text-slate-900">{permission ? 'Editar permiso' : 'Nuevo permiso'}</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-xl leading-none">×</button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
          <label className="block">
            <span className="mb-1 block text-sm font-semibold text-slate-700">Nombre técnico</span>
            <input type="text" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
              required placeholder="ej: PROJECT_WRITE"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-blue-400" />
          </label>
          <label className="block">
            <span className="mb-1 block text-sm font-semibold text-slate-700">Descripción</span>
            <input type="text" value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
              placeholder="ej: Permite crear y editar proyectos"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-blue-400" />
          </label>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} disabled={submitting}
              className="rounded-xl border border-slate-200 px-4 py-2 text-sm text-slate-600 disabled:opacity-50">Cancelar</button>
            <button type="submit" disabled={submitting}
              className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600 disabled:opacity-50">
              {submitting ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function RoleModal({ role, permissions, onClose, onSaved }) {
  const [form, setForm] = useState({ name: '', description: '', permissionIds: [] })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (role) {
      setForm({
        name: role.name ?? '',
        description: role.description ?? '',
        permissionIds: role.permissions?.map(p => p.id) ?? [],
      })
    } else {
      setForm({ name: '', description: '', permissionIds: [] })
    }
  }, [role])

  const togglePermission = (id) =>
    setForm(prev => ({
      ...prev,
      permissionIds: prev.permissionIds.includes(id)
        ? prev.permissionIds.filter(p => p !== id)
        : [...prev.permissionIds, id],
    }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) { setError('El nombre es obligatorio.'); return }
    try {
      setSubmitting(true)
      setError('')
      role ? await roleService.update(role.id, form) : await roleService.create(form)
      onSaved()
    } catch (err) {
      setError(err.message || 'Error al guardar rol.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h3 className="font-semibold text-slate-900">{role ? 'Editar rol' : 'Nuevo rol'}</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-xl leading-none">×</button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
          <label className="block">
            <span className="mb-1 block text-sm font-semibold text-slate-700">Nombre</span>
            <input type="text" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
              required placeholder="ej: ROLE_PROJECT_LEAD"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-blue-400" />
          </label>
          <label className="block">
            <span className="mb-1 block text-sm font-semibold text-slate-700">Descripción</span>
            <input type="text" value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
              placeholder="ej: Líder de proyecto"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-blue-400" />
          </label>
          <div>
            <p className="mb-2 text-sm font-semibold text-slate-700">Permisos asignados</p>
            <div className="max-h-48 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-2">
              {permissions.length === 0
                ? <p className="text-sm text-slate-400">Sin permisos disponibles</p>
                : permissions.map(p => (
                  <label key={p.id} className="flex items-center gap-2 cursor-pointer select-none">
                    <input type="checkbox" checked={form.permissionIds.includes(p.id)}
                      onChange={() => togglePermission(p.id)}
                      className="rounded border-slate-300 accent-slate-950" />
                    <span className="text-sm font-medium text-slate-700">{p.name}</span>
                    {p.description && <span className="text-xs text-slate-400">— {p.description}</span>}
                  </label>
                ))
              }
            </div>
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} disabled={submitting}
              className="rounded-xl border border-slate-200 px-4 py-2 text-sm text-slate-600 disabled:opacity-50">Cancelar</button>
            <button type="submit" disabled={submitting}
              className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600 disabled:opacity-50">
              {submitting ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function RolesPage() {
  const [tab, setTab] = useState('roles')
  const [roles, setRoles] = useState([])
  const [permissions, setPermissions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [roleModal, setRoleModal] = useState(null)
  const [permModal, setPermModal] = useState(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const [r, p] = await Promise.all([roleService.getAll(), permissionService.getAll()])
      setRoles(Array.isArray(r) ? r : (r?.data ?? []))
      setPermissions(Array.isArray(p) ? p : (p?.data ?? []))
    } catch (err) {
      setError(err.message || 'Error al cargar datos.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handleDeleteRole = async (role) => {
    if (!window.confirm(`¿Eliminar el rol "${role.name}"?`)) return
    try { await roleService.delete(role.id); load() } catch (err) { alert(err.message) }
  }

  const handleDeletePerm = async (perm) => {
    if (!window.confirm(`¿Eliminar el permiso "${perm.name}"?`)) return
    try { await permissionService.delete(perm.id); load() } catch (err) { alert(err.message) }
  }

  return (
    <>
      <main className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
        <section className="mx-auto w-full max-w-5xl px-6 py-10">
          <div className="mb-6">
            <h1 className="text-3xl font-black text-slate-950">Roles y Permisos</h1>
            <p className="mt-1 text-sm text-slate-500">Gestión del catálogo de roles y permisos del sistema</p>
          </div>

          <div className="mb-5 flex gap-2">
            {[['roles', 'Roles'], ['permisos', 'Permisos']].map(([key, label]) => (
              <button key={key} onClick={() => setTab(key)}
                className={`rounded-xl px-5 py-2 text-sm font-semibold transition ${tab === key ? 'bg-slate-950 text-white' : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'}`}>
                {label}
              </button>
            ))}
          </div>

          {loading ? (
            <div className="flex min-h-[200px] items-center justify-center">
              <p className="text-slate-500">Cargando...</p>
            </div>
          ) : error ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-center">
              <p className="text-red-700">{error}</p>
              <button onClick={load} className="mt-4 rounded-xl bg-red-600 px-4 py-2 text-sm font-bold text-white">Reintentar</button>
            </div>
          ) : tab === 'roles' ? (
            <div>
              <div className="mb-4 flex justify-end">
                <button onClick={() => setRoleModal('new')}
                  className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600 transition">
                  + Nuevo rol
                </button>
              </div>
              <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-slate-200 bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-500">
                      <th className="px-4 py-3 text-left w-10">#</th>
                      <th className="px-4 py-3 text-left">Nombre</th>
                      <th className="px-4 py-3 text-left">Descripción</th>
                      <th className="px-4 py-3 text-center w-24">Permisos</th>
                      <th className="px-4 py-3 text-right w-36">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {roles.length === 0 && (
                      <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-400">Sin roles registrados</td></tr>
                    )}
                    {roles.map(r => (
                      <tr key={r.id} className="hover:bg-slate-50 transition">
                        <td className="px-4 py-3 text-slate-400">{r.id}</td>
                        <td className="px-4 py-3 font-medium text-slate-900">{r.name}</td>
                        <td className="px-4 py-3 text-slate-600">{r.description ?? '—'}</td>
                        <td className="px-4 py-3 text-center text-slate-500">{r.permissions?.length ?? 0}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-1.5">
                            <button onClick={() => setRoleModal(r)}
                              className="rounded-lg border border-slate-200 px-2.5 py-1 text-xs font-semibold text-slate-600 hover:bg-slate-100">
                              Editar
                            </button>
                            <button onClick={() => handleDeleteRole(r)}
                              className="rounded-lg border border-red-200 px-2.5 py-1 text-xs font-semibold text-red-600 hover:bg-red-50">
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            <div>
              <div className="mb-4 flex justify-end">
                <button onClick={() => setPermModal('new')}
                  className="rounded-xl bg-slate-950 px-5 py-2 text-sm font-bold text-white hover:bg-blue-600 transition">
                  + Nuevo permiso
                </button>
              </div>
              <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-slate-200 bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-500">
                      <th className="px-4 py-3 text-left w-10">#</th>
                      <th className="px-4 py-3 text-left">Nombre</th>
                      <th className="px-4 py-3 text-left">Descripción</th>
                      <th className="px-4 py-3 text-right w-36">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {permissions.length === 0 && (
                      <tr><td colSpan={4} className="px-4 py-8 text-center text-slate-400">Sin permisos registrados</td></tr>
                    )}
                    {permissions.map(p => (
                      <tr key={p.id} className="hover:bg-slate-50 transition">
                        <td className="px-4 py-3 text-slate-400">{p.id}</td>
                        <td className="px-4 py-3 font-medium text-slate-900">{p.name}</td>
                        <td className="px-4 py-3 text-slate-600">{p.description ?? '—'}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-1.5">
                            <button onClick={() => setPermModal(p)}
                              className="rounded-lg border border-slate-200 px-2.5 py-1 text-xs font-semibold text-slate-600 hover:bg-slate-100">
                              Editar
                            </button>
                            <button onClick={() => handleDeletePerm(p)}
                              className="rounded-lg border border-red-200 px-2.5 py-1 text-xs font-semibold text-red-600 hover:bg-red-50">
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </section>
      </main>

      {roleModal && (
        <RoleModal
          role={roleModal === 'new' ? null : roleModal}
          permissions={permissions}
          onClose={() => setRoleModal(null)}
          onSaved={() => { setRoleModal(null); load() }}
        />
      )}

      {permModal && (
        <PermissionModal
          permission={permModal === 'new' ? null : permModal}
          onClose={() => setPermModal(null)}
          onSaved={() => { setPermModal(null); load() }}
        />
      )}
    </>
  )
}

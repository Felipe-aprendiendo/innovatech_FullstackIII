import { apiFetch } from './api'

const roleService = {
  getAll: () => apiFetch('/api/v1/roles'),
  create: (data) => apiFetch('/api/v1/roles', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => apiFetch(`/api/v1/roles/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => apiFetch(`/api/v1/roles/${id}`, { method: 'DELETE' }),
}

export default roleService

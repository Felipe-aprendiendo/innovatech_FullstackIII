import { apiFetch } from './api'

const permissionService = {
  getAll: () => apiFetch('/api/v1/permissions'),
  create: (data) => apiFetch('/api/v1/permissions', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => apiFetch(`/api/v1/permissions/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => apiFetch(`/api/v1/permissions/${id}`, { method: 'DELETE' }),
}

export default permissionService

import { apiFetch } from './api'

const userService = {
  getAll: () =>
    apiFetch('/api/v1/users'),

  getById: (id) =>
    apiFetch(`/api/v1/users/${id}`),

  getRoles: () =>
    apiFetch('/api/v1/roles'),

  create: (data) =>
    apiFetch('/api/v1/users', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  update: (id, data) =>
    apiFetch(`/api/v1/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  toggle: (id) =>
    apiFetch(`/api/v1/users/${id}/toggle`, {
      method: 'PATCH',
    }),

  delete: (id) =>
    apiFetch(`/api/v1/users/${id}`, { method: 'DELETE' }),
}

export default userService


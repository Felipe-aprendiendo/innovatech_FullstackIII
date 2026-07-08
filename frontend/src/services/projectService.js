import { apiFetch } from './api'

const projectService = {
  getAll: () =>
    apiFetch('/api/v1/projects'),

  getById: (id) =>
    apiFetch(`/api/v1/projects/${id}`),

  create: (data) =>
    apiFetch('/api/v1/projects', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  update: (id, data) =>
    apiFetch(`/api/v1/projects/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  changeStatus: (id, estado) =>
    apiFetch(`/api/v1/projects/${id}/status?estado=${estado}`, { method: 'PATCH' }),

  close: (id) =>
    apiFetch(`/api/v1/projects/${id}/close`, {
      method: 'PATCH',
    }),

  delete: (id) =>
    apiFetch(`/api/v1/projects/${id}`, {
      method: 'DELETE',
    }),
}

export default projectService

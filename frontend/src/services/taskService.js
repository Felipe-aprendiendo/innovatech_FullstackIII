import { apiFetch } from './api'

const taskService = {
  getAll: (params) => {
    const query = new URLSearchParams()
    if (params?.projectId) query.set('projectId', params.projectId)
    if (params?.estado) query.set('estado', params.estado)
    if (params?.responsableId) query.set('responsableId', params.responsableId)
    const qs = query.toString()
    return apiFetch(`/api/v1/tasks${qs ? '?' + qs : ''}`)
  },

  getById: (id) =>
    apiFetch(`/api/v1/tasks/${id}`),

  getByProject: (projectId) =>
    apiFetch(`/api/v1/tasks/project/${projectId}`),

  create: (data) =>
    apiFetch('/api/v1/tasks', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  update: (id, data) =>
    apiFetch(`/api/v1/tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  updateStatus: (id, nuevoEstado) =>
    apiFetch(`/api/v1/tasks/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ nuevoEstado }),
    }),

  delete: (id) =>
    apiFetch(`/api/v1/tasks/${id}`, { method: 'DELETE' }),

  addComment: (taskId, contenido) =>
    apiFetch(`/api/v1/tasks/${taskId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ contenido }),
    }),
}

export default taskService


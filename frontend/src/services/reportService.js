import { apiFetch } from './api'

const reportService = {
  getDashboard: () =>
    apiFetch('/api/v1/reports/dashboard').then(r => r.data ?? r),

  getProjectsReport: () =>
    apiFetch('/api/v1/reports/projects').then(r => r.data ?? r),

  getTasksReport: () =>
    apiFetch('/api/v1/reports/tasks').then(r => r.data ?? r),

  getMyReport: () =>
    apiFetch('/api/v1/reports/my').then(r => r.data ?? r),

  getAllKpis: () =>
    apiFetch('/api/v1/reports/kpis').then(r => r.data ?? r),

  getKpiByProject: (projectId) =>
    apiFetch(`/api/v1/reports/kpis/${projectId}`).then(r => r.data ?? r),
}

export default reportService

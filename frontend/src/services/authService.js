import { apiFetch } from './api'

const authService = {
  changePassword: (currentPassword, newPassword) =>
    apiFetch('/api/v1/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword }),
    }),
}

export default authService

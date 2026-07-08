const API_BASE = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000').replace(/\/$/, '')

export function getTokens() {
  try { return JSON.parse(localStorage.getItem('tokens') ?? 'null') } catch { return null }
}

export function saveTokens(tokens) {
  localStorage.setItem('tokens', JSON.stringify(tokens))
  if (tokens?.accessToken) {
    document.cookie = `accessToken=${tokens.accessToken}; path=/; SameSite=Lax`
  }
}

export function clearTokens() {
  localStorage.removeItem('tokens')
  document.cookie = 'accessToken=; path=/; max-age=0'
}

export async function apiFetch(path, options = {}) {
  const tokens = getTokens()
  const headers = {
    'Content-Type': 'application/json',
    ...(tokens?.accessToken ? { Authorization: `Bearer ${tokens.accessToken}` } : {}),
    ...options.headers,
  }

  let response = await fetch(API_BASE + path, { ...options, headers })

  if (response.status === 401 && tokens?.refreshToken) {
    const refreshRes = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: tokens.refreshToken }),
    })
    if (refreshRes.ok) {
      const newTokens = await refreshRes.json()
      saveTokens(newTokens)
      const retryHeaders = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${newTokens.accessToken}`,
        ...options.headers,
      }
      response = await fetch(API_BASE + path, { ...options, headers: retryHeaders })
    } else {
      clearTokens()
      window.location.href = '/login'
      return
    }
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}))
    throw new Error(errorData.message || `Error ${response.status}`)
  }
  if (response.status === 204) return null
  return response.json()
}

import { useAuth } from '../context/AuthContext'
import { useState, useCallback } from 'react'

export function useFetch() {
  const { getAuthHeader, refreshAccessToken, logout } = useAuth()
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const fetchWithToken = useCallback(
    async (url, options = {}) => {
      setLoading(true)
      setError(null)

      try {
        const headers = {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
          ...options.headers,
        }

        let response = await fetch(url, {
          ...options,
          headers,
        })

        // Si el token expiró (401), intentar refrescar
        if (response.status === 401) {
          try {
            const refreshedTokens = await refreshAccessToken()
            const newHeaders = {
              'Content-Type': 'application/json',
              ...(refreshedTokens?.accessToken
                ? { Authorization: `Bearer ${refreshedTokens.accessToken}` }
                : {}),
              ...options.headers,
            }
            response = await fetch(url, {
              ...options,
              headers: newHeaders,
            })
            if (response.status === 401) {
              await logout()
              throw new Error('Sesion expirada o sin permisos. Inicia sesion nuevamente.')
            }
          } catch (refreshError) {
            throw new Error('Sesión expirada, por favor inicia sesión nuevamente')
          }
        }

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}))
          throw new Error(errorData.message || `Error HTTP: ${response.status}`)
        }

        const data = await response.json()
        return data
      } catch (err) {
        setError(err.message)
        throw err
      } finally {
        setLoading(false)
      }
    },
    [getAuthHeader, logout, refreshAccessToken]
  )

  return { fetchWithToken, error, loading }
}

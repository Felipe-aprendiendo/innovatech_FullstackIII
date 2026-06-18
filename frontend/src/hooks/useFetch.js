import { useAuth } from '../context/AuthContext'
import { useState, useCallback } from 'react'

export function useFetch() {
  const { getAuthHeader, refreshAccessToken } = useAuth()
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
            await refreshAccessToken()
            const newHeaders = {
              'Content-Type': 'application/json',
              ...getAuthHeader(),
              ...options.headers,
            }
            response = await fetch(url, {
              ...options,
              headers: newHeaders,
            })
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
    [getAuthHeader, refreshAccessToken]
  )

  return { fetchWithToken, error, loading }
}

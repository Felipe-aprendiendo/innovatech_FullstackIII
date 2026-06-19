import { createContext, useContext, useState, useEffect, useCallback } from 'react'

const AuthContext = createContext(null)

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000').replace(/\/$/, '')
const AUTH_API_URL = `${API_BASE_URL}/api/v1/auth`

function buildUserFromTokens(storedTokens) {
  if (!storedTokens?.accessToken || !storedTokens?.email) {
    return null
  }

  return {
    id: storedTokens.userId ?? null,
    email: storedTokens.email,
    permissions: storedTokens.permissions || [],
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [tokens, setTokens] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Cargar tokens del localStorage al montar
  useEffect(() => {
    const storedTokens = localStorage.getItem('tokens')
    if (storedTokens) {
      try {
        const parsedTokens = JSON.parse(storedTokens)
        const storedUser = buildUserFromTokens(parsedTokens)

        if (storedUser) {
          setTokens(parsedTokens)
          setUser(storedUser)
        } else {
          localStorage.removeItem('tokens')
        }
      } catch (e) {
        localStorage.removeItem('tokens')
      }
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    setError(null)
    try {
      const response = await fetch(`${AUTH_API_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || 'Error en el login')
      }

      const data = await response.json()
      setTokens(data)
      setUser(buildUserFromTokens(data))
      localStorage.setItem('tokens', JSON.stringify(data))
      return data
    } catch (err) {
      setError(err.message)
      throw err
    }
  }, [])

  const logout = useCallback(async () => {
    if (tokens?.refreshToken) {
      try {
        await fetch(`${AUTH_API_URL}/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(tokens.accessToken ? { Authorization: `Bearer ${tokens.accessToken}` } : {}),
          },
          body: JSON.stringify({ refreshToken: tokens.refreshToken }),
        })
      } catch (err) {
        console.error('Error en logout:', err)
      }
    }
    setUser(null)
    setTokens(null)
    localStorage.removeItem('tokens')
    setError(null)
  }, [tokens])

  const refreshAccessToken = useCallback(async () => {
    if (!tokens?.refreshToken) {
      throw new Error('No refresh token disponible')
    }

    try {
      const response = await fetch(`${AUTH_API_URL}/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: tokens.refreshToken }),
      })

      if (!response.ok) {
        throw new Error('Error al refrescar token')
      }

      const newTokens = await response.json()
      setTokens(newTokens)
      setUser(buildUserFromTokens(newTokens))
      localStorage.setItem('tokens', JSON.stringify(newTokens))
      return newTokens
    } catch (err) {
      // Token expirado, hacer logout
      await logout()
      throw err
    }
  }, [tokens, logout])

  const getAuthHeader = useCallback(() => {
    return tokens?.accessToken ? { Authorization: `Bearer ${tokens.accessToken}` } : {}
  }, [tokens])

  const value = {
    user,
    tokens,
    loading,
    error,
    isAuthenticated: !!tokens?.accessToken && !!user,
    login,
    logout,
    refreshAccessToken,
    getAuthHeader,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}

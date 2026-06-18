import { createContext, useContext, useState, useEffect, useCallback } from 'react'

const AuthContext = createContext(null)

const AUTH_API_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000'

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
        setTokens(JSON.parse(storedTokens))
      } catch (e) {
        localStorage.removeItem('tokens')
      }
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    setError(null)
    try {
      const response = await fetch(`${AUTH_API_URL}/auth/login`, {
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
      setUser({
        id: data.userId,
        email: data.email,
        permissions: data.permissions || [],
      })
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
        await fetch(`${AUTH_API_URL}/auth/logout`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
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
      const response = await fetch(`${AUTH_API_URL}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: tokens.refreshToken }),
      })

      if (!response.ok) {
        throw new Error('Error al refrescar token')
      }

      const newTokens = await response.json()
      setTokens(newTokens)
      localStorage.setItem('tokens', JSON.stringify(newTokens))
      return newTokens.accessToken
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
    isAuthenticated: !!user && !!tokens,
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

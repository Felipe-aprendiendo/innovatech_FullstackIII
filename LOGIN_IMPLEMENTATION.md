# Sistema de Autenticación - Frontend

## Descripción

Se ha implementado un sistema completo de autenticación JWT en el frontend con los siguientes componentes:

### 📁 Nuevos Archivos

#### Contexto de Autenticación
- **`src/context/AuthContext.jsx`** - Maneja todo el estado de autenticación
  - Gestiona login/logout
  - Almacena tokens en localStorage
  - Proporciona datos del usuario autenticado
  - Hook `useAuth()` para acceder al contexto

#### Componentes
- **`src/components/Login.jsx`** - Pantalla de login con validación
  - Formulario email/password
  - Validación de credenciales
  - Manejo de errores
  - Interfaz moderna con Tailwind CSS

- **`src/components/Navbar.jsx`** - Barra de navegación
  - Muestra email del usuario
  - Botón de logout
  - Navegación principal

- **`src/components/ProtectedRoute.jsx`** - Componente para rutas protegidas
  - Verifica autenticación
  - Loading spinner
  - Redirige a login si no está autenticado

#### Hooks
- **`src/hooks/useFetch.js`** - Hook personalizado con interceptor JWT
  - Añade automáticamente token a headers
  - Refresca token si expira (401)
  - Manejo de errores centralizado

### 🔧 Flujo de Autenticación

1. **Inicio**: Usuario ve pantalla de login
2. **Login**: Envía credenciales a `/auth/login`
3. **Tokens**: Recibe accessToken y refreshToken
4. **Storage**: Los tokens se guardan en localStorage
5. **Dashboard**: Se muestra Navbar + ProjectsPage protegida
6. **Requests**: Todas las peticiones incluyen token JWT automáticamente
7. **Refresh**: Si token expira, se refresca automáticamente
8. **Logout**: Limpia tokens del localStorage

### 📝 Uso en Componentes

```javascript
import { useAuth } from '../context/AuthContext'
import { useFetch } from '../hooks/useFetch'

function MyComponent() {
  const { user, logout, isAuthenticated } = useAuth()
  const { fetchWithToken } = useFetch()

  const handleFetch = async () => {
    const data = await fetchWithToken('http://api.example.com/data')
  }

  return (
    <>
      <p>Hola {user?.email}</p>
      <button onClick={logout}>Logout</button>
    </>
  )
}
```

### 🔐 Estructura de AuthContext

```javascript
const authContext = {
  user: {              // Datos del usuario
    id: number,
    email: string,
    permissions: string[]
  },
  tokens: {           // Tokens JWT
    accessToken: string,
    refreshToken: string,
    expiresIn: number
  },
  loading: boolean,   // Estado de carga inicial
  error: string,      // Mensaje de error
  isAuthenticated: boolean,

  // Métodos
  login(email, password),
  logout(),
  refreshAccessToken(),
  getAuthHeader()     // { Authorization: 'Bearer ...' }
}
```

### 🚀 Configuración

1. Copiar `.env.example` a `.env`:
   ```bash
   cp frontend/.env.example frontend/.env
   ```

2. Verificar URLs en `.env`:
   ```
   VITE_API_BASE_URL=http://localhost:8000
   VITE_PROJECTS_API_URL=http://localhost:8003/api/v1/projects
   ```

3. Iniciar el frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### ✅ Credenciales de Prueba

El Login muestra credenciales de prueba en la interfaz. Para obtener credenciales reales:

1. Crear usuario en users-service
2. Registrar credenciales en auth-service
3. Usar email/password para login

### 🔄 Interceptor JWT Automático

El hook `useFetch()` añade automáticamente el token JWT a todas las peticiones:

```javascript
// Antes (sin token)
const response = await fetch('/api/data')

// Ahora (con token automático)
const { fetchWithToken } = useFetch()
const data = await fetchWithToken('/api/data')
// Automáticamente: Authorization: Bearer {token}
```

### 🛡️ Seguridad

- ✅ Tokens guardados en localStorage (accesible desde JS)
- ✅ Refresh automático de tokens expirados
- ✅ Logout limpia tokens del storage
- ✅ CORS configurado en gateway
- ⚠️ Para producción: Usar HttpOnly cookies en lugar de localStorage

### 📊 Commits Realizados

1. `feat: agregar Context de Autenticación`
2. `feat: agregar componente de Login`
3. `feat: agregar componente ProtectedRoute para rutas protegidas`
4. `feat: agregar componente Navbar con botón de logout`
5. `feat: agregar hook useFetch con interceptor JWT`
6. `feat: integrar autenticación en App.jsx`
7. `chore: agregar .env.example para configuración del frontend`


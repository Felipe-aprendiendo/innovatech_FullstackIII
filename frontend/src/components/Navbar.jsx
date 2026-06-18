import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()

  const handleLogout = async () => {
    if (window.confirm('¿Estás seguro de que quieres cerrar sesión?')) {
      await logout()
    }
  }

  return (
    <nav className="bg-gradient-to-r from-blue-600 to-indigo-700 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-8">
            <h1 className="text-2xl font-bold">InnovaTech</h1>
            <div className="hidden md:flex gap-6">
              <a href="#" className="hover:opacity-80 transition">
                Proyectos
              </a>
              <a href="#" className="hover:opacity-80 transition">
                Tareas
              </a>
              <a href="#" className="hover:opacity-80 transition">
                Reportes
              </a>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="text-sm text-blue-100">
              <p>{user?.email}</p>
            </div>
            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-500 hover:bg-red-600 rounded-lg transition font-medium"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>
    </nav>
  )
}

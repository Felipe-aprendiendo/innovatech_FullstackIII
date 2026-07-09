import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ChangePasswordModal from './ChangePasswordModal'

export default function Navbar() {
  const { user, logout } = useAuth()
  const [showPasswordModal, setShowPasswordModal] = useState(false)

  const handleLogout = async () => {
    if (window.confirm('¿Estás seguro de que quieres cerrar sesión?')) {
      await logout()
    }
  }

  return (
    <>
      <nav className="bg-gradient-to-r from-blue-600 to-indigo-700 text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-8">
              <h1 className="text-2xl font-bold">InnovaTech</h1>
              <div className="hidden md:flex gap-6">
                <Link to="/projects" className="hover:opacity-80 transition">Proyectos</Link>
                <Link to="/tasks" className="hover:opacity-80 transition">Tareas</Link>
                <Link to="/reports" className="hover:opacity-80 transition">Reportes</Link>
                {user?.role === 'ADMIN' && (
                  <>
                    <Link to="/users" className="hover:opacity-80 transition">Usuarios</Link>
                    <Link to="/roles" className="hover:opacity-80 transition">Roles</Link>
                  </>
                )}
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="text-sm text-blue-100">
                <p>{user?.email}</p>
                {user?.role && <p className="text-xs opacity-70">{user.role}</p>}
              </div>
              <button
                onClick={() => setShowPasswordModal(true)}
                className="px-3 py-2 bg-white/15 hover:bg-white/25 rounded-lg transition text-sm font-medium"
              >
                Contraseña
              </button>
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

      {showPasswordModal && (
        <ChangePasswordModal onClose={() => setShowPasswordModal(false)} />
      )}
    </>
  )
}

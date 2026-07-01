import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function RoleGuard({ roles }) {
  const { user } = useAuth()
  if (!roles || roles.includes(user?.role)) return <Outlet />
  return <Navigate to="/projects" replace />
}

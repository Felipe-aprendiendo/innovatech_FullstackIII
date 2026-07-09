import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Login from './components/Login'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import RoleGuard from './components/shared/RoleGuard'
import ProjectsPage from './pages/projects/ProjectsPage'
import TasksPage from './pages/tasks/TasksPage'
import UsersPage from './pages/users/UsersPage'
import ReportsPage from './pages/reports/ReportsPage'
import RolesPage from './pages/admin/RolesPage'

function Layout() {
  return (
    <>
      <Navbar />
      <Outlet />
    </>
  )
}

function LoginRoute() {
  const { isAuthenticated } = useAuth()
  if (isAuthenticated) return <Navigate to="/projects" replace />
  return <Login />
}

function DefaultRedirect() {
  const { user } = useAuth()
  return <Navigate to={user?.role === 'USER' ? '/tasks' : '/projects'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginRoute />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route index element={<DefaultRedirect />} />
          <Route path="/projects" element={<ProjectsPage />} />
          <Route path="/tasks" element={<TasksPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route element={<RoleGuard roles={['ADMIN']} />}>
            <Route path="/users" element={<UsersPage />} />
            <Route path="/roles" element={<RolesPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<DefaultRedirect />} />
    </Routes>
  )
}

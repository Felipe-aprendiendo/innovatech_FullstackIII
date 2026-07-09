import { useCallback, useEffect, useState } from 'react'
import ProjectCard from '../../components/ProjectCard'
import ProjectFormModal from '../../components/ProjectFormModal'
import { useFetch } from '../../hooks/useFetch'
import userService from '../../services/userService'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000').replace(/\/$/, '')
const PROJECTS_API_URL =
  import.meta.env.VITE_PROJECTS_API_URL ?? `${API_BASE_URL}/api/v1/projects`

const PRIORITY_ORDER = {
  ALTA: 3,
  MEDIA: 2,
  BAJA: 1,
}

const initialProjectForm = {
  nombre: '',
  descripcion: '',
  prioridad: 'MEDIA',
  fechaInicio: '',
  fechaFin: '',
  responsableId: '',
}

export default function ProjectsPage() {
  const [projects, setProjects] = useState([])
  const [userMap, setUserMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [criterioOrden, setCriterioOrden] = useState('prioridad')
  const [errorMessage, setErrorMessage] = useState('')
  const [isProjectFormOpen, setIsProjectFormOpen] = useState(false)
  const [isCreatingProject, setIsCreatingProject] = useState(false)
  const [projectForm, setProjectForm] = useState(initialProjectForm)
  const [createProjectError, setCreateProjectError] = useState('')
  const { fetchWithToken } = useFetch()

  const loadProjects = useCallback(async () => {
    try {
      setLoading(true)
      setErrorMessage('')
      const data = await fetchWithToken(PROJECTS_API_URL)
      setProjects(Array.isArray(data) ? data : [])
      // Fetch users for responsible names (succeeds for ADMIN, silently ignored for PROJECT_LEAD)
      try {
        const usersData = await userService.getAll()
        const usersArr = Array.isArray(usersData) ? usersData : (usersData?.data ?? [])
        const map = {}
        usersArr.forEach(u => { map[u.id] = `${u.name ?? ''}${u.lastName ? ' ' + u.lastName : ''}`.trim() })
        setUserMap(map)
      } catch {
        // non-ADMIN users cannot list users; responsible names won't show
      }
    } catch (error) {
      setErrorMessage(error.message || 'Ocurrio un error al cargar los proyectos.')
    } finally {
      setLoading(false)
    }
  }, [fetchWithToken])

  useEffect(() => {
    loadProjects()
  }, [loadProjects])

  const openProjectForm = () => {
    setCreateProjectError('')
    setProjectForm(initialProjectForm)
    setIsProjectFormOpen(true)
  }

  const closeProjectForm = () => {
    if (isCreatingProject) return
    setIsProjectFormOpen(false)
    setCreateProjectError('')
  }

  const handleProjectFormChange = (event) => {
    const { name, value } = event.target
    setProjectForm((currentForm) => ({ ...currentForm, [name]: value }))
  }

  const handleCreateProject = async (event) => {
    event.preventDefault()
    if (!projectForm.nombre.trim() || !projectForm.responsableId) {
      setCreateProjectError('Completa nombre y responsable antes de guardar.')
      return
    }
    try {
      setIsCreatingProject(true)
      setCreateProjectError('')
      const payload = {
        nombre: projectForm.nombre.trim(),
        descripcion: projectForm.descripcion.trim(),
        prioridad: projectForm.prioridad,
        fechaInicio: projectForm.fechaInicio || null,
        fechaFin: projectForm.fechaFin || null,
        responsableId: Number(projectForm.responsableId),
      }
      const createdProject = await fetchWithToken(PROJECTS_API_URL, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      setProjects((currentProjects) => [createdProject, ...currentProjects])
      setIsProjectFormOpen(false)
      setProjectForm(initialProjectForm)
    } catch (error) {
      setCreateProjectError(error.message || 'Ocurrio un error al crear el proyecto.')
    } finally {
      setIsCreatingProject(false)
    }
  }

  const orderedProjects = [...projects].sort((projectA, projectB) => {
    if (criterioOrden === 'id') {
      return (projectB.id ?? 0) - (projectA.id ?? 0)
    }
    return (
      (PRIORITY_ORDER[projectB.prioridad] ?? 0) -
      (PRIORITY_ORDER[projectA.prioridad] ?? 0)
    )
  })

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(250,204,21,0.20),_transparent_28%),linear-gradient(180deg,_#f8fafc_0%,_#e2e8f0_52%,_#cbd5e1_100%)] text-slate-900">
      <section className="mx-auto flex min-h-screen w-full max-w-7xl flex-col px-6 py-10 sm:px-8 lg:px-12">
        <div className="mb-8 flex flex-col gap-6 rounded-[2rem] border border-white/60 bg-white/70 p-8 shadow-[0_24px_80px_rgba(15,23,42,0.12)] backdrop-blur xl:flex-row xl:items-end xl:justify-between">
          <div className="max-w-3xl">
            <span className="inline-flex rounded-full border border-amber-300 bg-amber-100 px-4 py-1 text-xs font-semibold uppercase tracking-[0.28em] text-amber-800">
              Panel de proyectos
            </span>
            <h1 className="mt-4 text-4xl font-black tracking-tight text-slate-950 sm:text-5xl">
              Proyectos en seguimiento visual
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600 sm:text-lg">
              Cada tarjeta usa el modelo de proyectos: nombre como titulo,
              descripcion como especialidad, prioridad visible y estado operativo.
            </p>
            <div className="mt-6">
              <button
                type="button"
                onClick={openProjectForm}
                className="inline-flex items-center rounded-full bg-slate-950 px-6 py-3 text-sm font-bold text-white shadow-lg transition hover:bg-amber-400 hover:text-slate-950"
              >
                Agregar Proyecto
              </button>
            </div>
          </div>

          <div className="flex w-full flex-col gap-4 xl:max-w-xs">
            <label className="text-sm font-semibold uppercase tracking-[0.18em] text-slate-500">
              Ordenar proyectos
            </label>
            <select
              value={criterioOrden}
              onChange={(event) => setCriterioOrden(event.target.value)}
              className="rounded-2xl border border-slate-200 bg-slate-950 px-4 py-3 text-sm font-medium text-white shadow-lg outline-none transition focus:border-amber-400"
            >
              <option value="prioridad">Prioridad</option>
              <option value="id">ID</option>
            </select>

            <div className="grid grid-cols-3 gap-3 text-center">
              <div className="rounded-2xl border border-slate-200 bg-white px-3 py-4">
                <p className="text-2xl font-black text-slate-950">{projects.length}</p>
                <p className="mt-1 text-xs uppercase tracking-[0.2em] text-slate-500">Total</p>
              </div>
              <div className="rounded-2xl border border-slate-200 bg-white px-3 py-4">
                <p className="text-2xl font-black text-amber-600">
                  {projects.filter((project) => project.prioridad === 'ALTA').length}
                </p>
                <p className="mt-1 text-xs uppercase tracking-[0.2em] text-slate-500">Alta</p>
              </div>
              <div className="rounded-2xl border border-slate-200 bg-white px-3 py-4">
                <p className="text-2xl font-black text-emerald-600">
                  {projects.filter(
                    (project) => project.estado === 'COMPLETADO' || project.estado === 'CERRADO',
                  ).length}
                </p>
                <p className="mt-1 text-xs uppercase tracking-[0.2em] text-slate-500">Finalizados</p>
              </div>
            </div>
          </div>
        </div>

        {loading ? (
          <div className="flex min-h-[260px] items-center justify-center rounded-[2rem] border border-dashed border-slate-300 bg-white/60 px-6 text-center shadow-[0_18px_60px_rgba(15,23,42,0.08)]">
            <p className="text-lg font-semibold text-slate-600">Cargando proyectos...</p>
          </div>
        ) : errorMessage ? (
          <div className="rounded-[2rem] border border-red-200 bg-red-50 px-6 py-10 text-center shadow-[0_18px_60px_rgba(239,68,68,0.12)]">
            <p className="text-lg font-semibold text-red-700">{errorMessage}</p>
            <p className="mt-2 text-sm text-red-500">
              Verifica que el servicio de proyectos este disponible en{' '}
              <span className="font-semibold">{PROJECTS_API_URL}</span>.
            </p>
            <button
              type="button"
              onClick={loadProjects}
              className="mt-6 inline-flex items-center rounded-full bg-red-600 px-5 py-3 text-sm font-bold text-white transition hover:bg-red-700"
            >
              Reintentar
            </button>
          </div>
        ) : orderedProjects.length === 0 ? (
          <div className="rounded-[2rem] border border-slate-200 bg-white/70 px-6 py-10 text-center shadow-[0_18px_60px_rgba(15,23,42,0.08)]">
            <p className="text-lg font-semibold text-slate-700">No hay proyectos para mostrar.</p>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 2xl:grid-cols-3">
            {orderedProjects.map((project) => (
              <ProjectCard key={project.id} project={project} onRefresh={loadProjects} responsableNombre={userMap[project.responsableId] ?? ''} />
            ))}
          </div>
        )}
      </section>

      <ProjectFormModal
        isOpen={isProjectFormOpen}
        formValues={projectForm}
        isSubmitting={isCreatingProject}
        errorMessage={createProjectError}
        onClose={closeProjectForm}
        onChange={handleProjectFormChange}
        onSubmit={handleCreateProject}
      />
    </main>
  )
}

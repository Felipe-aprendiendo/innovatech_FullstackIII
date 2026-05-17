package cl.innovatech.projects_service.service;

import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.dto.ProjectResponse;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.exception.ResourceNotFoundException;
import cl.innovatech.projects_service.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectResponse create(ProjectRequest request) {
        Project project = Project.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .prioridad(request.prioridad() != null ? request.prioridad() : Project.Prioridad.MEDIA)
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .responsableId(request.responsableId())
                .estado(Project.Estado.PLANIFICADO)
                .build();

        return toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> findAll() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse findById(Long id) {
        return toResponse(getProject(id));
    }

    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getProject(id);

        if (project.getEstado() == Project.Estado.CERRADO) {
            throw new IllegalStateException("No se puede modificar un proyecto cerrado");
        }

        project.setNombre(request.nombre());
        project.setDescripcion(request.descripcion());
        project.setPrioridad(request.prioridad() != null ? request.prioridad() : project.getPrioridad());
        project.setFechaInicio(request.fechaInicio());
        project.setFechaFin(request.fechaFin());
        project.setResponsableId(request.responsableId());

        return toResponse(projectRepository.save(project));
    }

    public void delete(Long id) {
        Project project = getProject(id);
        projectRepository.delete(project);
    }

    public ProjectResponse changeStatus(Long id, Project.Estado estado) {
        Project project = getProject(id);

        if (project.getEstado() == Project.Estado.CERRADO) {
            throw new IllegalStateException("No se puede cambiar el estado de un proyecto cerrado");
        }

        project.setEstado(estado);
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse close(Long id) {
        Project project = getProject(id);
        project.setEstado(Project.Estado.CERRADO);
        return toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> findByEstado(Project.Estado estado) {
        return projectRepository.findByEstado(estado)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProjectResponse> findByResponsableId(Long responsableId) {
        return projectRepository.findByResponsableId(responsableId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getNombre(),
                project.getDescripcion(),
                project.getEstado(),
                project.getPrioridad(),
                project.getFechaInicio(),
                project.getFechaFin(),
                project.getResponsableId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
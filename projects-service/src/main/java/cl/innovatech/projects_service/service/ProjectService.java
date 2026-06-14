package cl.innovatech.projects_service.service;

import cl.innovatech.projects_service.client.UsersClient;
import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.dto.ProjectResponse;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.exception.ResourceNotFoundException;
import cl.innovatech.projects_service.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Map<Project.Estado, List<Project.Estado>> ALLOWED_STATUS_TRANSITIONS =
            new EnumMap<>(Project.Estado.class);

    static {
        ALLOWED_STATUS_TRANSITIONS.put(Project.Estado.PLANIFICADO,
                List.of(Project.Estado.EN_PROGRESO, Project.Estado.CERRADO));
        ALLOWED_STATUS_TRANSITIONS.put(Project.Estado.EN_PROGRESO,
                List.of(Project.Estado.COMPLETADO, Project.Estado.CERRADO));
        ALLOWED_STATUS_TRANSITIONS.put(Project.Estado.COMPLETADO,
                List.of(Project.Estado.CERRADO));
        ALLOWED_STATUS_TRANSITIONS.put(Project.Estado.CERRADO, List.of());
    }

    private final ProjectRepository projectRepository;
    private final UsersClient usersClient;

    public ProjectResponse create(ProjectRequest request) {
        validateProjectDates(request);
        Set<Long> miembroIds = normalizeMemberIds(request.responsableId(), request.miembroIds());
        validateAssignedUsers(request.responsableId(), miembroIds);

        Project project = Project.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .prioridad(request.prioridad())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .responsableId(request.responsableId())
                .miembroIds(miembroIds)
                .estado(Project.Estado.PLANIFICADO)
                .build();

        return toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> findAll(Project.Estado estado, Long responsableId, Long miembroId) {
        return projectRepository.findAll()
                .stream()
                .filter(project -> estado == null || project.getEstado() == estado)
                .filter(project -> responsableId == null || Objects.equals(project.getResponsableId(), responsableId))
                .filter(project -> miembroId == null || project.getMiembroIds().contains(miembroId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse findById(Long id) {
        return toResponse(getProject(id));
    }

    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getProject(id);
        validateProjectDates(request);

        if (project.getEstado() == Project.Estado.CERRADO) {
            throw new IllegalStateException("No se puede modificar un proyecto cerrado");
        }

        Set<Long> miembroIds = normalizeMemberIds(request.responsableId(), request.miembroIds());
        validateAssignedUsers(request.responsableId(), miembroIds);

        project.setNombre(request.nombre());
        project.setDescripcion(request.descripcion());
        project.setPrioridad(request.prioridad());
        project.setFechaInicio(request.fechaInicio());
        project.setFechaFin(request.fechaFin());
        project.setResponsableId(request.responsableId());
        project.setMiembroIds(miembroIds);

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

        validateStatusTransition(project.getEstado(), estado);
        project.setEstado(estado);
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse close(Long id) {
        Project project = getProject(id);

        if (project.getEstado() == Project.Estado.CERRADO) {
            return toResponse(project);
        }

        project.setEstado(Project.Estado.CERRADO);
        if (project.getFechaFin() == null) {
            project.setFechaFin(LocalDate.now());
        }
        return toResponse(projectRepository.save(project));
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
    }

    private void validateProjectDates(ProjectRequest request) {
        if (request.fechaInicio() != null && request.fechaFin() != null
                && request.fechaFin().isBefore(request.fechaInicio())) {
            throw new IllegalArgumentException("La fechaFin no puede ser anterior a la fechaInicio");
        }
    }

    private void validateStatusTransition(Project.Estado currentStatus, Project.Estado newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        List<Project.Estado> allowedTransitions = ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, List.of());
        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                    "No se puede cambiar el estado de " + currentStatus + " a " + newStatus
            );
        }
    }

    private Set<Long> normalizeMemberIds(Long responsableId, Collection<Long> miembroIds) {
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        if (miembroIds != null) {
            miembroIds.stream()
                    .filter(Objects::nonNull)
                    .forEach(normalized::add);
        }
        normalized.add(responsableId);
        return normalized;
    }

    private void validateAssignedUsers(Long responsableId, Set<Long> miembroIds) {
        validateActiveUser(responsableId, "El responsable");
        miembroIds.forEach(memberId -> validateActiveUser(memberId, "El miembro"));
    }

    private void validateActiveUser(Long userId, String label) {
        UsersClient.UserSummary userSummary = usersClient.getUserSummary(userId);
        if (!Boolean.TRUE.equals(userSummary.enabled())) {
            throw new IllegalStateException(label + " no está habilitado: " + userId);
        }
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
                project.getMiembroIds(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}

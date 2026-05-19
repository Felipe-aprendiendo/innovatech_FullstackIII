package cl.innovatech.tasks.service;

import cl.innovatech.tasks.client.ProjectsClient;
import cl.innovatech.tasks.client.UsersClient;
import cl.innovatech.tasks.dto.*;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.entity.Task.EstadoTarea;
import cl.innovatech.tasks.entity.TaskComment;
import cl.innovatech.tasks.exception.ForbiddenException;
import cl.innovatech.tasks.exception.InvalidStateTransitionException;
import cl.innovatech.tasks.exception.ResourceNotFoundException;
import cl.innovatech.tasks.mapper.TaskMapper;
import cl.innovatech.tasks.repository.TaskCommentRepository;
import cl.innovatech.tasks.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final EventPublisherService eventPublisherService;
    private final ProjectsClient projectsClient;
    private final UsersClient usersClient;
    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public TaskResponse create(CreateTaskRequest request, Long userId, String userRole) {
        if (!isAdminOrLead(userRole)) {
            throw new ForbiddenException("Solo ADMIN y PROJECT_LEAD pueden crear tareas");
        }
        if (!projectsClient.projectExists(request.getProjectId())) {
            throw new ResourceNotFoundException("Proyecto no encontrado: " + request.getProjectId());
        }
        if (!usersClient.userExists(request.getResponsableId())) {
            throw new ResourceNotFoundException("Responsable no encontrado: " + request.getResponsableId());
        }
        Task task = taskMapper.toEntity(request, userId);
        Task saved = taskRepository.save(task);
        log.info("Tarea creada: id={}, proyecto={}", saved.getId(), saved.getProjectId());
        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(Long projectId, EstadoTarea estado, Long responsableId,
                                      Long userId, String userRole) {
        List<Task> tasks;

        if (projectId != null && estado != null) {
            tasks = taskRepository.findByProjectIdAndEstado(projectId, estado);
        } else if (projectId != null) {
            tasks = taskRepository.findByProjectId(projectId);
        } else if (estado != null) {
            tasks = taskRepository.findByEstado(estado);
        } else if (responsableId != null) {
            tasks = taskRepository.findByResponsableId(responsableId);
        } else {
            tasks = taskRepository.findAll();
        }

        // USER solo ve sus tareas asignadas
        if ("USER".equals(userRole)) {
            tasks = tasks.stream()
                    .filter(t -> t.getResponsableId().equals(userId))
                    .collect(Collectors.toList());
        }

        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id, Long userId, String userRole) {
        Task task = getTaskOrThrow(id);
        checkReadAccess(task, userId, userRole);
        List<TaskComment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(id);
        return taskMapper.toResponseWithComments(task, comments);
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request, Long userId, String userRole) {
        Task task = getTaskOrThrow(id);
        checkWriteAccess(task, userId, userRole);

        if (request.getTitulo() != null) task.setTitulo(request.getTitulo());
        if (request.getDescripcion() != null) task.setDescripcion(request.getDescripcion());
        if (request.getPrioridad() != null) task.setPrioridad(request.getPrioridad());
        if (request.getFechaLimite() != null) task.setFechaLimite(request.getFechaLimite());
        if (request.getResponsableId() != null) {
            if (!usersClient.userExists(request.getResponsableId())) {
                throw new ResourceNotFoundException("Responsable no encontrado: " + request.getResponsableId());
            }
            task.setResponsableId(request.getResponsableId());
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long id, UpdateStatusRequest request, Long userId, String userRole) {
        Task task = getTaskOrThrow(id);
        checkWriteAccess(task, userId, userRole);
        validateTransition(task.getEstado(), request.getNuevoEstado());

        EstadoTarea estadoAnterior = task.getEstado();
        task.setEstado(request.getNuevoEstado());
        Task saved = taskRepository.save(task);

        publishStatusChangedEvent(saved, estadoAnterior);
        log.info("Estado tarea {} cambiado: {} → {}", id, estadoAnterior, request.getNuevoEstado());
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new ForbiddenException("Solo ADMIN puede eliminar tareas");
        }
        Task task = getTaskOrThrow(id);
        taskRepository.delete(task);
        log.info("Tarea eliminada: id={}", id);
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request, Long userId, String userRole) {
        Task task = getTaskOrThrow(taskId);
        checkReadAccess(task, userId, userRole);

        TaskComment comment = TaskComment.builder()
                .task(task)
                .userId(userId)
                .contenido(request.getContenido())
                .build();

        TaskComment saved = commentRepository.save(comment);
        return taskMapper.toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findByProject(Long projectId, Long userId, String userRole) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if ("USER".equals(userRole)) {
            tasks = tasks.stream()
                    .filter(t -> t.getResponsableId().equals(userId))
                    .collect(Collectors.toList());
        }
        return tasks.stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada: " + id));
    }

    private void checkReadAccess(Task task, Long userId, String userRole) {
        if ("ADMIN".equals(userRole) || "PROJECT_LEAD".equals(userRole)) return;
        if (!task.getResponsableId().equals(userId)) {
            throw new ForbiddenException("No tienes acceso a esta tarea");
        }
    }

    private void checkWriteAccess(Task task, Long userId, String userRole) {
        if ("ADMIN".equals(userRole)) return;
        if ("PROJECT_LEAD".equals(userRole)) return;
        // USER solo puede modificar si es el responsable asignado
        if (!task.getResponsableId().equals(userId)) {
            throw new ForbiddenException("Solo puedes modificar tus propias tareas asignadas");
        }
    }

    private boolean isAdminOrLead(String userRole) {
        return "ADMIN".equals(userRole) || "PROJECT_LEAD".equals(userRole);
    }

    private void validateTransition(EstadoTarea actual, EstadoTarea nuevo) {
        boolean valid = switch (actual) {
            case PENDIENTE -> nuevo == EstadoTarea.EN_PROGRESO;
            case EN_PROGRESO -> nuevo == EstadoTarea.COMPLETADA || nuevo == EstadoTarea.CANCELADA;
            case COMPLETADA, CANCELADA -> false;
        };
        if (!valid) {
            throw new InvalidStateTransitionException(
                    "Transición inválida: " + actual + " → " + nuevo);
        }
    }

    private void publishStatusChangedEvent(Task task, EstadoTarea estadoAnterior) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "taskId", task.getId(),
                    "projectId", task.getProjectId(),
                    "estadoAnterior", estadoAnterior.name(),
                    "estadoNuevo", task.getEstado().name(),
                    "responsableId", task.getResponsableId()
            ));
            eventPublisherService.publishToOutbox("TASK_STATUS_CHANGED", payload);
        } catch (JsonProcessingException ex) {
            log.error("Error serializando evento de cambio de estado para tarea {}", task.getId(), ex);
        }
    }
}

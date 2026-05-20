package cl.innovatech.tasks.mapper;

import cl.innovatech.tasks.dto.CommentResponse;
import cl.innovatech.tasks.dto.CreateTaskRequest;
import cl.innovatech.tasks.dto.TaskResponse;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.entity.TaskComment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public Task toEntity(CreateTaskRequest request, Long createdBy) {
        return Task.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad() != null ? request.getPrioridad() : Task.PrioridadTarea.MEDIA)
                .fechaLimite(request.getFechaLimite())
                .projectId(request.getProjectId())
                .responsableId(request.getResponsableId())
                .createdBy(createdBy)
                .build();
    }

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .titulo(task.getTitulo())
                .descripcion(task.getDescripcion())
                .estado(task.getEstado())
                .prioridad(task.getPrioridad())
                .fechaLimite(task.getFechaLimite())
                .projectId(task.getProjectId())
                .responsableId(task.getResponsableId())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .comentarios(Collections.emptyList())
                .build();
    }

    public TaskResponse toResponseWithComments(Task task, List<TaskComment> comments) {
        TaskResponse response = toResponse(task);
        response.setComentarios(comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList()));
        return response;
    }

    public CommentResponse toCommentResponse(TaskComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .userId(comment.getUserId())
                .contenido(comment.getContenido())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}

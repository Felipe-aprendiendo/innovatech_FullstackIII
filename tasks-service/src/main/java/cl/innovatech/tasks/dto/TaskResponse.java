package cl.innovatech.tasks.dto;

import cl.innovatech.tasks.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private Task.EstadoTarea estado;
    private Task.PrioridadTarea prioridad;
    private LocalDate fechaLimite;
    private Long projectId;
    private Long responsableId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> comentarios;
}

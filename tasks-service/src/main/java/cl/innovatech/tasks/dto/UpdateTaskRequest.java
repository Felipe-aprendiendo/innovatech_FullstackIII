package cl.innovatech.tasks.dto;

import cl.innovatech.tasks.entity.Task;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String titulo;

    private String descripcion;

    private Task.PrioridadTarea prioridad;

    private LocalDate fechaLimite;

    private Long responsableId;
}

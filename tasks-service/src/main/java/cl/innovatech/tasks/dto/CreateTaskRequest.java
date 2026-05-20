package cl.innovatech.tasks.dto;

import cl.innovatech.tasks.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String titulo;

    private String descripcion;

    private Task.PrioridadTarea prioridad;

    private LocalDate fechaLimite;

    @NotNull(message = "El proyecto es obligatorio")
    private Long projectId;

    @NotNull(message = "El responsable es obligatorio")
    private Long responsableId;
}

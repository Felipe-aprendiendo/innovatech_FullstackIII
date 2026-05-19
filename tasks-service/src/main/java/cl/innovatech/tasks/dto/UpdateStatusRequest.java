package cl.innovatech.tasks.dto;

import cl.innovatech.tasks.entity.Task;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private Task.EstadoTarea nuevoEstado;
}

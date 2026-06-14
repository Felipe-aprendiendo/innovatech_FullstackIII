package cl.innovatech.projects_service.dto;

import cl.innovatech.projects_service.entity.Project;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 2000, message = "La descripcion no puede exceder 2000 caracteres")
        String descripcion,

        @NotNull(message = "La prioridad es obligatoria")
        Project.Prioridad prioridad,

        @NotNull(message = "La fechaInicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fechaFin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "El responsableId es obligatorio")
        @Positive(message = "El responsableId debe ser mayor que cero")
        Long responsableId
) {

    @AssertTrue(message = "La fechaFin no puede ser anterior a la fechaInicio")
    public boolean isDateRangeValid() {
        if (fechaInicio == null || fechaFin == null) {
            return true;
        }
        return !fechaFin.isBefore(fechaInicio);
    }
}

package cl.innovatech.projects_service.dto;

import cl.innovatech.projects_service.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String descripcion,

        Project.Prioridad prioridad,

        LocalDate fechaInicio,

        LocalDate fechaFin,

        @NotNull(message = "El responsableId es obligatorio")
        Long responsableId
) {
}
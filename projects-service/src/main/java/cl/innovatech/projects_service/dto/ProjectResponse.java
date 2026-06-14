package cl.innovatech.projects_service.dto;

import cl.innovatech.projects_service.entity.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record ProjectResponse(
        Long id,
        String nombre,
        String descripcion,
        Project.Estado estado,
        Project.Prioridad prioridad,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long responsableId,
        Set<Long> miembroIds,
        ProjectProgress avance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

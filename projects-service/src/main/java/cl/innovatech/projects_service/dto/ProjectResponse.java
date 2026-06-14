package cl.innovatech.projects_service.dto;

import cl.innovatech.projects_service.entity.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String nombre,
        String descripcion,
        Project.Estado estado,
        Project.Prioridad prioridad,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long responsableId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
package cl.innovatech.projects_service.dto;

public record ProjectProgress(
        int totalTareas,
        int tareasPendientes,
        int tareasEnProgreso,
        int tareasCompletadas,
        int tareasCanceladas,
        int porcentajeAvance
) {
}

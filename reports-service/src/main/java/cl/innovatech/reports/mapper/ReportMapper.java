package cl.innovatech.reports.mapper;

import cl.innovatech.reports.dto.KpiResponse;
import cl.innovatech.reports.dto.ProjectReportResponse;
import cl.innovatech.reports.entity.KpiSnapshot;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public KpiResponse toKpiResponse(KpiSnapshot snapshot) {
        return KpiResponse.builder()
                .projectId(snapshot.getProjectId())
                .totalTareas(snapshot.getTotalTareas())
                .tareasCompletadas(snapshot.getTareasCompletadas())
                .tareasEnProgreso(snapshot.getTareasEnProgreso())
                .tareasPendientes(snapshot.getTareasPendientes())
                .porcentajeAvance(snapshot.getPorcentajeAvance())
                .calculadoAt(snapshot.getCalculadoAt())
                .build();
    }

    public ProjectReportResponse toProjectReport(KpiSnapshot snapshot, String nombreProyecto) {
        return ProjectReportResponse.builder()
                .projectId(snapshot.getProjectId())
                .nombreProyecto(nombreProyecto)
                .totalTareas(snapshot.getTotalTareas())
                .tareasCompletadas(snapshot.getTareasCompletadas())
                .tareasEnProgreso(snapshot.getTareasEnProgreso())
                .tareasPendientes(snapshot.getTareasPendientes())
                .porcentajeAvance(snapshot.getPorcentajeAvance())
                .build();
    }
}

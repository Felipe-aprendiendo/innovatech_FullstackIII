package cl.innovatech.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Integer totalProyectos;
    private Integer totalTareas;
    private Integer tareasPendientes;
    private Integer tareasEnProgreso;
    private Integer tareasCompletadas;
    private List<ProjectReportResponse> proyectos;
}

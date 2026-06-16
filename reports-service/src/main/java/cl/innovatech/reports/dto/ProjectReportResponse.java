package cl.innovatech.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReportResponse {

    private Long projectId;
    private String nombreProyecto;
    private Integer totalTareas;
    private Integer tareasCompletadas;
    private Integer tareasEnProgreso;
    private Integer tareasPendientes;
    private BigDecimal porcentajeAvance;
}

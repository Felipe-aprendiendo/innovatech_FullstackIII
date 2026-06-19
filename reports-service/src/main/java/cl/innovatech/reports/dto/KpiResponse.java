package cl.innovatech.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiResponse {

    private Long projectId;
    private Integer totalTareas;
    private Integer tareasCompletadas;
    private Integer tareasEnProgreso;
    private Integer tareasPendientes;
    private BigDecimal porcentajeAvance;
    private LocalDateTime calculadoAt;
}

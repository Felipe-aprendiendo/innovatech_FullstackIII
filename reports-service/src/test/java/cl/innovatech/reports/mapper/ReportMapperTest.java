package cl.innovatech.reports.mapper;

import cl.innovatech.reports.dto.KpiResponse;
import cl.innovatech.reports.dto.ProjectReportResponse;
import cl.innovatech.reports.entity.KpiSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReportMapperTest {

    private final ReportMapper reportMapper = new ReportMapper();

    @Test
    void toKpiResponse_mapeatodosLosCamposCorrectamente() {
        LocalDateTime ahora = LocalDateTime.now();
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(10L)
                .totalTareas(8).tareasCompletadas(4).tareasEnProgreso(2).tareasPendientes(2)
                .porcentajeAvance(BigDecimal.valueOf(50.00))
                .calculadoAt(ahora)
                .build();

        KpiResponse result = reportMapper.toKpiResponse(snapshot);

        assertThat(result.getProjectId()).isEqualTo(10L);
        assertThat(result.getTotalTareas()).isEqualTo(8);
        assertThat(result.getTareasCompletadas()).isEqualTo(4);
        assertThat(result.getTareasEnProgreso()).isEqualTo(2);
        assertThat(result.getTareasPendientes()).isEqualTo(2);
        assertThat(result.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(result.getCalculadoAt()).isEqualTo(ahora);
    }

    @Test
    void toProjectReport_mapeatodosLosCamposIncluidoNombre() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(2L).projectId(20L)
                .totalTareas(3).tareasCompletadas(3).tareasEnProgreso(0).tareasPendientes(0)
                .porcentajeAvance(BigDecimal.valueOf(100.00))
                .calculadoAt(LocalDateTime.now())
                .build();

        ProjectReportResponse result = reportMapper.toProjectReport(snapshot, "Proyecto Gamma");

        assertThat(result.getProjectId()).isEqualTo(20L);
        assertThat(result.getNombreProyecto()).isEqualTo("Proyecto Gamma");
        assertThat(result.getTotalTareas()).isEqualTo(3);
        assertThat(result.getTareasCompletadas()).isEqualTo(3);
        assertThat(result.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }
}

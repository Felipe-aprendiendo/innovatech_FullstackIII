package cl.innovatech.reports.service;

import cl.innovatech.reports.client.TasksClient;
import cl.innovatech.reports.dto.TaskReportResponse;
import cl.innovatech.reports.entity.KpiSnapshot;
import cl.innovatech.reports.repository.KpiSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorServiceTest {

    @Mock
    private KpiSnapshotRepository kpiSnapshotRepository;

    @Mock
    private TasksClient tasksClient;

    @InjectMocks
    private KpiCalculatorService kpiCalculatorService;

    @Test
    void recalculate_conTareasMixtas_calculaPorcentajeCorrectamente() {
        given(tasksClient.getTasksByProject(1L)).willReturn(List.of(
                TaskReportResponse.builder().taskId(1L).estado("COMPLETADA").build(),
                TaskReportResponse.builder().taskId(2L).estado("COMPLETADA").build(),
                TaskReportResponse.builder().taskId(3L).estado("EN_PROGRESO").build(),
                TaskReportResponse.builder().taskId(4L).estado("PENDIENTE").build()
        ));
        given(kpiSnapshotRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        KpiSnapshot result = kpiCalculatorService.recalculate(1L);

        assertThat(result.getTotalTareas()).isEqualTo(4);
        assertThat(result.getTareasCompletadas()).isEqualTo(2);
        assertThat(result.getTareasEnProgreso()).isEqualTo(1);
        assertThat(result.getTareasPendientes()).isEqualTo(1);
        assertThat(result.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        verify(kpiSnapshotRepository).save(any(KpiSnapshot.class));
    }

    @Test
    void recalculate_sinTareas_retornaPorcentajeCero() {
        given(tasksClient.getTasksByProject(2L)).willReturn(List.of());
        given(kpiSnapshotRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        KpiSnapshot result = kpiCalculatorService.recalculate(2L);

        assertThat(result.getTotalTareas()).isEqualTo(0);
        assertThat(result.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void recalculate_todasCompletadas_retornaCienPorciento() {
        given(tasksClient.getTasksByProject(3L)).willReturn(List.of(
                TaskReportResponse.builder().taskId(1L).estado("COMPLETADA").build(),
                TaskReportResponse.builder().taskId(2L).estado("COMPLETADA").build()
        ));
        given(kpiSnapshotRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        KpiSnapshot result = kpiCalculatorService.recalculate(3L);

        assertThat(result.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(result.getTareasCompletadas()).isEqualTo(2);
    }

    @Test
    void recalculate_asignaProjectIdYGuardaSnapshot() {
        given(tasksClient.getTasksByProject(99L)).willReturn(List.of());
        given(kpiSnapshotRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        KpiSnapshot result = kpiCalculatorService.recalculate(99L);

        assertThat(result.getProjectId()).isEqualTo(99L);
        verify(kpiSnapshotRepository).save(any(KpiSnapshot.class));
    }
}

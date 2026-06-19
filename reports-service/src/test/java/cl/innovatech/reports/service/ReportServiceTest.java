package cl.innovatech.reports.service;

import cl.innovatech.reports.client.ProjectsClient;
import cl.innovatech.reports.client.TasksClient;
import cl.innovatech.reports.dto.DashboardResponse;
import cl.innovatech.reports.dto.KpiResponse;
import cl.innovatech.reports.dto.ProjectReportResponse;
import cl.innovatech.reports.dto.TaskReportResponse;
import cl.innovatech.reports.entity.KpiSnapshot;
import cl.innovatech.reports.exception.ForbiddenException;
import cl.innovatech.reports.mapper.ReportMapper;
import cl.innovatech.reports.repository.KpiSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private KpiSnapshotRepository kpiSnapshotRepository;
    @Mock private KpiCalculatorService kpiCalculatorService;
    @Mock private ProjectsClient projectsClient;
    @Mock private TasksClient tasksClient;

    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportMapper = new ReportMapper();
        reportService = new ReportService(
                kpiSnapshotRepository, kpiCalculatorService,
                projectsClient, tasksClient, reportMapper);
    }

    @Test
    void getKpiByProject_cuandoSnapshotExiste_retornaSinLlamarRest() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L)
                .projectId(10L)
                .totalTareas(5)
                .tareasCompletadas(3)
                .tareasEnProgreso(1)
                .tareasPendientes(1)
                .porcentajeAvance(BigDecimal.valueOf(60.00))
                .calculadoAt(LocalDateTime.now())
                .build();

        given(kpiSnapshotRepository.findTopByProjectIdOrderByCalculadoAtDesc(10L))
                .willReturn(Optional.of(snapshot));

        KpiResponse result = reportService.getKpiByProject(10L, 1L, "ADMIN");

        assertThat(result.getProjectId()).isEqualTo(10L);
        assertThat(result.getTareasCompletadas()).isEqualTo(3);
        assertThat(result.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        verify(kpiCalculatorService, never()).recalculate(any());
    }

    @Test
    void getKpiByProject_cuandoNoHaySnapshot_calculaViaRest() {
        KpiSnapshot calculado = KpiSnapshot.builder()
                .id(2L)
                .projectId(10L)
                .totalTareas(4)
                .tareasCompletadas(2)
                .tareasEnProgreso(1)
                .tareasPendientes(1)
                .porcentajeAvance(BigDecimal.valueOf(50.00))
                .calculadoAt(LocalDateTime.now())
                .build();

        given(kpiSnapshotRepository.findTopByProjectIdOrderByCalculadoAtDesc(10L))
                .willReturn(Optional.empty());
        given(kpiCalculatorService.recalculate(10L)).willReturn(calculado);

        KpiResponse result = reportService.getKpiByProject(10L, 1L, "ADMIN");

        assertThat(result.getTareasCompletadas()).isEqualTo(2);
        verify(kpiCalculatorService).recalculate(10L);
    }

    @Test
    void getDashboard_comoUser_lanzaForbidden() {
        assertThatThrownBy(() -> reportService.getDashboard(1L, "USER"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("PROJECT_LEAD");
    }

    @Test
    void getAllKpis_comoUser_lanzaForbidden() {
        assertThatThrownBy(() -> reportService.getAllKpis(1L, "USER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getMyReport_retornaTareasDelUsuario() {
        List<TaskReportResponse> tareas = List.of(
                TaskReportResponse.builder()
                        .taskId(1L).titulo("Mi tarea").estado("PENDIENTE")
                        .prioridad("MEDIA").responsableId(5L).build()
        );

        given(tasksClient.getTasksByUser(5L)).willReturn(tareas);

        List<TaskReportResponse> result = reportService.getMyReport(5L, "USER");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitulo()).isEqualTo("Mi tarea");
    }

    @Test
    void getDashboard_comoAdmin_retornaDashboardConTotales() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(1L)
                .totalTareas(5).tareasCompletadas(3).tareasEnProgreso(1).tareasPendientes(1)
                .porcentajeAvance(BigDecimal.valueOf(60.00))
                .calculadoAt(LocalDateTime.now())
                .build();

        given(kpiSnapshotRepository.findAllByOrderByCalculadoAtDesc()).willReturn(List.of(snapshot));
        given(projectsClient.getProjectName(1L)).willReturn("Proyecto Alpha");

        DashboardResponse result = reportService.getDashboard(1L, "ADMIN");

        assertThat(result.getTotalProyectos()).isEqualTo(1);
        assertThat(result.getTotalTareas()).isEqualTo(5);
        assertThat(result.getTareasCompletadas()).isEqualTo(3);
        assertThat(result.getTareasEnProgreso()).isEqualTo(1);
    }

    @Test
    void getProjectsReport_comoUser_lanzaForbidden() {
        assertThatThrownBy(() -> reportService.getProjectsReport(1L, "USER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getProjectsReport_comoAdmin_retornaListaDeProyectos() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(2L)
                .totalTareas(3).tareasCompletadas(1).tareasEnProgreso(1).tareasPendientes(1)
                .porcentajeAvance(BigDecimal.valueOf(33.33))
                .calculadoAt(LocalDateTime.now())
                .build();

        given(kpiSnapshotRepository.findAllByOrderByCalculadoAtDesc()).willReturn(List.of(snapshot));
        given(projectsClient.getProjectName(2L)).willReturn("Proyecto Beta");

        List<ProjectReportResponse> result = reportService.getProjectsReport(1L, "ADMIN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombreProyecto()).isEqualTo("Proyecto Beta");
    }

    @Test
    void getAllKpis_comoAdmin_retornaListaDeKpis() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(3L)
                .totalTareas(2).tareasCompletadas(2).tareasEnProgreso(0).tareasPendientes(0)
                .porcentajeAvance(BigDecimal.valueOf(100.00))
                .calculadoAt(LocalDateTime.now())
                .build();

        given(kpiSnapshotRepository.findAllByOrderByCalculadoAtDesc()).willReturn(List.of(snapshot));

        List<KpiResponse> result = reportService.getAllKpis(1L, "ADMIN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(3L);
        assertThat(result.get(0).getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void getTasksReport_retornaTareasDeTodosLosProyectos() {
        given(projectsClient.getAllProjectIds()).willReturn(List.of(1L, 2L));
        given(tasksClient.getTasksByProject(1L)).willReturn(
                List.of(TaskReportResponse.builder().taskId(1L).titulo("Tarea A").build())
        );
        given(tasksClient.getTasksByProject(2L)).willReturn(
                List.of(TaskReportResponse.builder().taskId(2L).titulo("Tarea B").build())
        );

        List<TaskReportResponse> result = reportService.getTasksReport(1L, "PROJECT_LEAD");

        assertThat(result).hasSize(2);
    }
}

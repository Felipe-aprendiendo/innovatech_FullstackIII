package cl.innovatech.reports.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    // ─── KpiResponse ───────────────────────────────────────────────

    @Test
    void kpiResponse_builder_inicializaTodosLosCampos() {
        LocalDateTime now = LocalDateTime.now();

        KpiResponse kpi = KpiResponse.builder()
                .projectId(1L)
                .totalTareas(20)
                .tareasCompletadas(10)
                .tareasEnProgreso(5)
                .tareasPendientes(5)
                .porcentajeAvance(new BigDecimal("50.00"))
                .calculadoAt(now)
                .build();

        assertThat(kpi.getProjectId()).isEqualTo(1L);
        assertThat(kpi.getTotalTareas()).isEqualTo(20);
        assertThat(kpi.getTareasCompletadas()).isEqualTo(10);
        assertThat(kpi.getTareasEnProgreso()).isEqualTo(5);
        assertThat(kpi.getTareasPendientes()).isEqualTo(5);
        assertThat(kpi.getPorcentajeAvance()).isEqualByComparingTo("50.00");
        assertThat(kpi.getCalculadoAt()).isEqualTo(now);
    }

    @Test
    void kpiResponse_noArgsConstructor_creaInstanciaVacia() {
        KpiResponse kpi = new KpiResponse();

        assertThat(kpi.getProjectId()).isNull();
        assertThat(kpi.getTotalTareas()).isNull();
    }

    @Test
    void kpiResponse_setters_actualizanCampos() {
        KpiResponse kpi = new KpiResponse();
        kpi.setProjectId(5L);
        kpi.setTotalTareas(30);
        kpi.setTareasCompletadas(20);
        kpi.setTareasEnProgreso(5);
        kpi.setTareasPendientes(5);
        kpi.setPorcentajeAvance(new BigDecimal("66.67"));
        kpi.setCalculadoAt(LocalDateTime.now());

        assertThat(kpi.getProjectId()).isEqualTo(5L);
        assertThat(kpi.getTotalTareas()).isEqualTo(30);
        assertThat(kpi.getPorcentajeAvance()).isEqualByComparingTo("66.67");
    }

    @Test
    void kpiResponse_equals_conMismosValores_sonIguales() {
        LocalDateTime now = LocalDateTime.now();
        KpiResponse k1 = new KpiResponse(1L, 10, 5, 3, 2, BigDecimal.TEN, now);
        KpiResponse k2 = new KpiResponse(1L, 10, 5, 3, 2, BigDecimal.TEN, now);

        assertThat(k1).isEqualTo(k2);
        assertThat(k1.hashCode()).isEqualTo(k2.hashCode());
    }

    @Test
    void kpiResponse_toString_contieneInformacion() {
        KpiResponse kpi = KpiResponse.builder().projectId(1L).totalTareas(5).build();

        assertThat(kpi.toString()).contains("KpiResponse");
    }

    // ─── ProjectReportResponse ─────────────────────────────────────

    @Test
    void projectReportResponse_builder_inicializaTodosLosCampos() {
        ProjectReportResponse report = ProjectReportResponse.builder()
                .projectId(1L)
                .nombreProyecto("Proyecto Alpha")
                .totalTareas(15)
                .tareasCompletadas(8)
                .tareasEnProgreso(4)
                .tareasPendientes(3)
                .porcentajeAvance(new BigDecimal("53.33"))
                .build();

        assertThat(report.getProjectId()).isEqualTo(1L);
        assertThat(report.getNombreProyecto()).isEqualTo("Proyecto Alpha");
        assertThat(report.getTotalTareas()).isEqualTo(15);
        assertThat(report.getTareasCompletadas()).isEqualTo(8);
        assertThat(report.getTareasEnProgreso()).isEqualTo(4);
        assertThat(report.getTareasPendientes()).isEqualTo(3);
        assertThat(report.getPorcentajeAvance()).isEqualByComparingTo("53.33");
    }

    @Test
    void projectReportResponse_noArgsConstructor_creaInstanciaVacia() {
        ProjectReportResponse report = new ProjectReportResponse();

        assertThat(report.getProjectId()).isNull();
        assertThat(report.getNombreProyecto()).isNull();
    }

    @Test
    void projectReportResponse_setters_actualizanCampos() {
        ProjectReportResponse report = new ProjectReportResponse();
        report.setProjectId(2L);
        report.setNombreProyecto("Proyecto Beta");
        report.setTotalTareas(10);
        report.setTareasCompletadas(5);
        report.setTareasEnProgreso(3);
        report.setTareasPendientes(2);
        report.setPorcentajeAvance(new BigDecimal("50.00"));

        assertThat(report.getProjectId()).isEqualTo(2L);
        assertThat(report.getNombreProyecto()).isEqualTo("Proyecto Beta");
    }

    @Test
    void projectReportResponse_equals_conMismosValores_sonIguales() {
        ProjectReportResponse r1 = new ProjectReportResponse(1L, "Alpha", 10, 5, 3, 2, BigDecimal.TEN);
        ProjectReportResponse r2 = new ProjectReportResponse(1L, "Alpha", 10, 5, 3, 2, BigDecimal.TEN);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void projectReportResponse_toString_contieneInformacion() {
        ProjectReportResponse report = ProjectReportResponse.builder()
                .projectId(1L).nombreProyecto("Test").build();

        assertThat(report.toString()).contains("ProjectReportResponse");
    }

    // ─── DashboardResponse ─────────────────────────────────────────

    @Test
    void dashboardResponse_builder_inicializaTodosLosCampos() {
        ProjectReportResponse proj = ProjectReportResponse.builder()
                .projectId(1L).nombreProyecto("Alpha").build();

        DashboardResponse dashboard = DashboardResponse.builder()
                .totalProyectos(3)
                .totalTareas(50)
                .tareasPendientes(20)
                .tareasEnProgreso(15)
                .tareasCompletadas(15)
                .proyectos(List.of(proj))
                .build();

        assertThat(dashboard.getTotalProyectos()).isEqualTo(3);
        assertThat(dashboard.getTotalTareas()).isEqualTo(50);
        assertThat(dashboard.getTareasPendientes()).isEqualTo(20);
        assertThat(dashboard.getTareasEnProgreso()).isEqualTo(15);
        assertThat(dashboard.getTareasCompletadas()).isEqualTo(15);
        assertThat(dashboard.getProyectos()).hasSize(1);
    }

    @Test
    void dashboardResponse_noArgsConstructor_creaInstanciaVacia() {
        DashboardResponse dashboard = new DashboardResponse();

        assertThat(dashboard.getTotalProyectos()).isNull();
        assertThat(dashboard.getProyectos()).isNull();
    }

    @Test
    void dashboardResponse_setters_actualizanCampos() {
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setTotalProyectos(5);
        dashboard.setTotalTareas(100);
        dashboard.setTareasPendientes(40);
        dashboard.setTareasEnProgreso(30);
        dashboard.setTareasCompletadas(30);
        dashboard.setProyectos(List.of());

        assertThat(dashboard.getTotalProyectos()).isEqualTo(5);
        assertThat(dashboard.getTotalTareas()).isEqualTo(100);
        assertThat(dashboard.getProyectos()).isEmpty();
    }

    @Test
    void dashboardResponse_equals_conMismosValores_sonIguales() {
        DashboardResponse d1 = new DashboardResponse(2, 10, 4, 3, 3, List.of());
        DashboardResponse d2 = new DashboardResponse(2, 10, 4, 3, 3, List.of());

        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    void dashboardResponse_toString_contieneInformacion() {
        DashboardResponse dashboard = DashboardResponse.builder()
                .totalProyectos(2).totalTareas(10).build();

        assertThat(dashboard.toString()).contains("DashboardResponse");
    }

    // ─── TaskReportResponse ────────────────────────────────────────

    @Test
    void taskReportResponse_builder_inicializaTodosLosCampos() {
        TaskReportResponse task = TaskReportResponse.builder()
                .taskId(1L)
                .titulo("Tarea Test")
                .estado("PENDIENTE")
                .prioridad("ALTA")
                .projectId(10L)
                .responsableId(5L)
                .build();

        assertThat(task.getTaskId()).isEqualTo(1L);
        assertThat(task.getTitulo()).isEqualTo("Tarea Test");
        assertThat(task.getEstado()).isEqualTo("PENDIENTE");
        assertThat(task.getPrioridad()).isEqualTo("ALTA");
        assertThat(task.getProjectId()).isEqualTo(10L);
        assertThat(task.getResponsableId()).isEqualTo(5L);
    }

    @Test
    void taskReportResponse_noArgsConstructor_creaInstanciaVacia() {
        TaskReportResponse task = new TaskReportResponse();

        assertThat(task.getTaskId()).isNull();
        assertThat(task.getTitulo()).isNull();
    }

    @Test
    void taskReportResponse_setters_actualizanCampos() {
        TaskReportResponse task = new TaskReportResponse();
        task.setTaskId(2L);
        task.setTitulo("Nueva tarea");
        task.setEstado("EN_PROGRESO");
        task.setPrioridad("MEDIA");
        task.setProjectId(3L);
        task.setResponsableId(7L);

        assertThat(task.getTaskId()).isEqualTo(2L);
        assertThat(task.getTitulo()).isEqualTo("Nueva tarea");
        assertThat(task.getEstado()).isEqualTo("EN_PROGRESO");
    }

    @Test
    void taskReportResponse_equals_conMismosValores_sonIguales() {
        TaskReportResponse t1 = new TaskReportResponse(1L, "T", "PENDIENTE", "ALTA", 1L, 2L);
        TaskReportResponse t2 = new TaskReportResponse(1L, "T", "PENDIENTE", "ALTA", 1L, 2L);

        assertThat(t1).isEqualTo(t2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    void taskReportResponse_toString_contieneInformacion() {
        TaskReportResponse task = TaskReportResponse.builder().taskId(1L).titulo("Test").build();

        assertThat(task.toString()).contains("TaskReportResponse");
    }
}

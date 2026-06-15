package cl.innovatech.reports.controller;

import cl.innovatech.reports.dto.DashboardResponse;
import cl.innovatech.reports.dto.KpiResponse;
import cl.innovatech.reports.dto.TaskReportResponse;
import cl.innovatech.reports.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @Test
    void getDashboard_comoAdmin_retorna200() throws Exception {
        DashboardResponse dashboard = DashboardResponse.builder()
                .totalProyectos(2)
                .totalTareas(10)
                .tareasPendientes(4)
                .tareasEnProgreso(3)
                .tareasCompletadas(3)
                .proyectos(List.of())
                .build();

        given(reportService.getDashboard(any(), any())).willReturn(dashboard);

        mockMvc.perform(get("/api/v1/reports/dashboard")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProyectos").value(2))
                .andExpect(jsonPath("$.data.totalTareas").value(10));
    }

    @Test
    void getKpiByProject_retorna200ConDatos() throws Exception {
        KpiResponse kpi = KpiResponse.builder()
                .projectId(1L)
                .totalTareas(5)
                .tareasCompletadas(3)
                .tareasEnProgreso(1)
                .tareasPendientes(1)
                .porcentajeAvance(BigDecimal.valueOf(60.00))
                .calculadoAt(LocalDateTime.now())
                .build();

        given(reportService.getKpiByProject(any(), any(), any())).willReturn(kpi);

        mockMvc.perform(get("/api/v1/reports/kpis/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PROJECT_LEAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTareas").value(5))
                .andExpect(jsonPath("$.data.tareasCompletadas").value(3));
    }

    @Test
    void getMyReport_retornaTareasPersonales() throws Exception {
        List<TaskReportResponse> tareas = List.of(
                TaskReportResponse.builder()
                        .taskId(1L).titulo("Mi tarea").estado("PENDIENTE")
                        .prioridad("ALTA").build()
        );

        given(reportService.getMyReport(any(), any())).willReturn(tareas);

        mockMvc.perform(get("/api/v1/reports/my")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].titulo").value("Mi tarea"));
    }
}

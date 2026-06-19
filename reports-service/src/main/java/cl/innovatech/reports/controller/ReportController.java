package cl.innovatech.reports.controller;

import cl.innovatech.reports.dto.ApiResponse;
import cl.innovatech.reports.dto.DashboardResponse;
import cl.innovatech.reports.dto.KpiResponse;
import cl.innovatech.reports.dto.ProjectReportResponse;
import cl.innovatech.reports.dto.TaskReportResponse;
import cl.innovatech.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "KPIs, dashboard y reportes consolidados del sistema")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Vista consolidada de KPIs y resumen ejecutivo")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dashboard obtenido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardResponse.class),
                examples = @ExampleObject(
                    name = "dashboard",
                    value = """
                        {
                          "success": true,
                          "message": "Dashboard obtenido",
                          "data": {
                            "totalProyectos": 3,
                            "totalTareas": 12,
                            "tareasPendientes": 4,
                            "tareasEnProgreso": 5,
                            "tareasCompletadas": 3,
                            "proyectos": [
                              {
                                "projectId": 1,
                                "nombreProyecto": "Portal Interno",
                                "totalTareas": 6,
                                "tareasCompletadas": 2,
                                "tareasEnProgreso": 3,
                                "tareasPendientes": 1,
                                "porcentajeAvance": 33.33
                              }
                            ]
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Rol sin acceso al dashboard")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_ADMIN")
            @RequestHeader("X-User-Role") String userRole) {

        DashboardResponse dashboard = reportService.getDashboard(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard obtenido", dashboard));
    }

    @Operation(summary = "Reporte de avance por proyecto")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte de proyectos obtenido"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Rol sin acceso al reporte")
    })
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectReportResponse>>> getProjectsReport(
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_PROJECT_LEAD")
            @RequestHeader("X-User-Role") String userRole) {

        List<ProjectReportResponse> report = reportService.getProjectsReport(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Reporte de proyectos obtenido", report));
    }

    @Operation(summary = "Reporte de tareas por estado y prioridad")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte de tareas obtenido")
    })
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TaskReportResponse>>> getTasksReport(
            @Parameter(description = "ID del usuario autenticado", example = "2")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_MEMBER")
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskReportResponse> report = reportService.getTasksReport(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Reporte de tareas obtenido", report));
    }

    @Operation(summary = "KPIs globales o del equipo")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KPIs obtenidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Rol sin acceso a KPIs globales")
    })
    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<List<KpiResponse>>> getAllKpis(
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_ADMIN")
            @RequestHeader("X-User-Role") String userRole) {

        List<KpiResponse> kpis = reportService.getAllKpis(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("KPIs obtenidos", kpis));
    }

    @Operation(summary = "KPIs de un proyecto especifico")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KPI del proyecto obtenido"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Rol sin acceso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Proyecto no encontrado en reportes")
    })
    @GetMapping("/kpis/{projectId}")
    public ResponseEntity<ApiResponse<KpiResponse>> getKpiByProject(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("projectId") Long projectId,
            @Parameter(description = "ID del usuario autenticado", example = "2")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_PROJECT_LEAD")
            @RequestHeader("X-User-Role") String userRole) {

        KpiResponse kpi = reportService.getKpiByProject(projectId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("KPI del proyecto obtenido", kpi));
    }

    @Operation(summary = "Reporte personal del usuario autenticado")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte personal obtenido")
    })
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TaskReportResponse>>> getMyReport(
            @Parameter(description = "ID del usuario autenticado", example = "2")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_MEMBER")
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskReportResponse> report = reportService.getMyReport(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Reporte personal obtenido", report));
    }
}

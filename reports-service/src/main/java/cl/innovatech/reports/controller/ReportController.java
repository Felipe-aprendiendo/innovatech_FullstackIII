package cl.innovatech.reports.controller;

import cl.innovatech.reports.dto.*;
import cl.innovatech.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reportes y KPIs del sistema")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Vista consolidada de KPIs y resúmenes (ADMIN, PROJECT_LEAD)")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        DashboardResponse dashboard = reportService.getDashboard(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard obtenido", dashboard));
    }

    @Operation(summary = "Reporte de avance por proyecto (ADMIN, PROJECT_LEAD)")
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectReportResponse>>> getProjectsReport(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<ProjectReportResponse> report = reportService.getProjectsReport(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Reporte de proyectos obtenido", report));
    }

    @Operation(summary = "Reporte de tareas por estado/prioridad (todos los roles)")
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TaskReportResponse>>> getTasksReport(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskReportResponse> report = reportService.getTasksReport(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Reporte de tareas obtenido", report));
    }

    @Operation(summary = "KPIs globales o de equipo (ADMIN, PROJECT_LEAD)")
    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<List<KpiResponse>>> getAllKpis(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<KpiResponse> kpis = reportService.getAllKpis(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("KPIs obtenidos", kpis));
    }

    @Operation(summary = "KPIs de un proyecto específico (todos los roles)")
    @GetMapping("/kpis/{projectId}")
    public ResponseEntity<ApiResponse<KpiResponse>> getKpiByProject(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        KpiResponse kpi = reportService.getKpiByProject(projectId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("KPI del proyecto obtenido", kpi));
    }

    @Operation(summary = "Reporte personal del usuario autenticado")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TaskReportResponse>>> getMyReport(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskReportResponse> report = reportService.getMyReport(userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Reporte personal obtenido", report));
    }
}

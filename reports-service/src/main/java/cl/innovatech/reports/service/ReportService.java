package cl.innovatech.reports.service;

import cl.innovatech.reports.client.ProjectsClient;
import cl.innovatech.reports.client.TasksClient;
import cl.innovatech.reports.dto.*;
import cl.innovatech.reports.entity.KpiSnapshot;
import cl.innovatech.reports.exception.ForbiddenException;
import cl.innovatech.reports.exception.ResourceNotFoundException;
import cl.innovatech.reports.mapper.ReportMapper;
import cl.innovatech.reports.repository.KpiSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final KpiSnapshotRepository kpiSnapshotRepository;
    private final KpiCalculatorService kpiCalculatorService;
    private final ProjectsClient projectsClient;
    private final TasksClient tasksClient;
    private final ReportMapper reportMapper;

    public DashboardResponse getDashboard(Long userId, String userRole) {
        if ("USER".equals(userRole)) {
            throw new ForbiddenException("Acceso denegado: se requiere ADMIN o PROJECT_LEAD");
        }

        List<KpiSnapshot> snapshots = latestPerProject();

        List<ProjectReportResponse> proyectos = snapshots.stream()
                .map(s -> {
                    String nombre = projectsClient.getProjectName(s.getProjectId());
                    return reportMapper.toProjectReport(s, nombre);
                })
                .toList();

        int totalTareas = proyectos.stream().mapToInt(ProjectReportResponse::getTotalTareas).sum();
        int pendientes = proyectos.stream().mapToInt(ProjectReportResponse::getTareasPendientes).sum();
        int enProgreso = proyectos.stream().mapToInt(ProjectReportResponse::getTareasEnProgreso).sum();
        int completadas = proyectos.stream().mapToInt(ProjectReportResponse::getTareasCompletadas).sum();

        return DashboardResponse.builder()
                .totalProyectos(proyectos.size())
                .totalTareas(totalTareas)
                .tareasPendientes(pendientes)
                .tareasEnProgreso(enProgreso)
                .tareasCompletadas(completadas)
                .proyectos(proyectos)
                .build();
    }

    public List<ProjectReportResponse> getProjectsReport(Long userId, String userRole) {
        if ("USER".equals(userRole)) {
            throw new ForbiddenException("Acceso denegado: se requiere ADMIN o PROJECT_LEAD");
        }

        List<KpiSnapshot> snapshots = latestPerProject();
        return snapshots.stream()
                .map(s -> {
                    String nombre = projectsClient.getProjectName(s.getProjectId());
                    return reportMapper.toProjectReport(s, nombre);
                })
                .toList();
    }

    public List<TaskReportResponse> getTasksReport(Long userId, String userRole) {
        List<Long> projectIds = projectsClient.getAllProjectIds();
        return projectIds.stream()
                .flatMap(pid -> tasksClient.getTasksByProject(pid).stream())
                .toList();
    }

    public List<KpiResponse> getAllKpis(Long userId, String userRole) {
        if ("USER".equals(userRole)) {
            throw new ForbiddenException("Acceso denegado: se requiere ADMIN o PROJECT_LEAD");
        }

        List<KpiSnapshot> snapshots = kpiSnapshotRepository.findAllByOrderByCalculadoAtDesc();
        return snapshots.stream()
                .map(reportMapper::toKpiResponse)
                .toList();
    }

    public KpiResponse getKpiByProject(Long projectId, Long userId, String userRole) {
        return kpiSnapshotRepository.findTopByProjectIdOrderByCalculadoAtDesc(projectId)
                .map(reportMapper::toKpiResponse)
                .orElseGet(() -> {
                    // Fallback: calcular en tiempo real vía REST
                    log.info("KPI no encontrado en cache para proyecto {}. Calculando via REST.", projectId);
                    KpiSnapshot snapshot = kpiCalculatorService.recalculate(projectId);
                    return reportMapper.toKpiResponse(snapshot);
                });
    }

    public List<TaskReportResponse> getMyReport(Long userId, String userRole) {
        return tasksClient.getTasksByUser(userId);
    }

    private List<KpiSnapshot> latestPerProject() {
        return kpiSnapshotRepository.findAllByOrderByCalculadoAtDesc()
                .stream()
                .collect(Collectors.toMap(
                        KpiSnapshot::getProjectId,
                        s -> s,
                        (a, b) -> a
                ))
                .values()
                .stream()
                .toList();
    }
}

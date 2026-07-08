package cl.innovatech.reports.service;

import cl.innovatech.reports.client.TasksClient;
import cl.innovatech.reports.dto.TaskReportResponse;
import cl.innovatech.reports.entity.KpiSnapshot;
import cl.innovatech.reports.repository.KpiSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiCalculatorService {

    private final KpiSnapshotRepository kpiSnapshotRepository;
    private final TasksClient tasksClient;

    @Transactional
    public KpiSnapshot recalculate(Long projectId) {
        List<TaskReportResponse> tasks = tasksClient.getTasksByProject(projectId);

        int total = tasks.size();
        int completadas = (int) tasks.stream().filter(t -> "COMPLETADA".equals(t.getEstado())).count();
        int enProgreso = (int) tasks.stream().filter(t -> "EN_PROGRESO".equals(t.getEstado())).count();
        int pendientes = (int) tasks.stream().filter(t -> "PENDIENTE".equals(t.getEstado())).count();

        BigDecimal porcentaje = total > 0
                ? BigDecimal.valueOf((double) completadas / total * 100).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        KpiSnapshot snapshot = kpiSnapshotRepository
                .findTopByProjectIdOrderByCalculadoAtDesc(projectId)
                .map(existing -> {
                    existing.setTotalTareas(total);
                    existing.setTareasCompletadas(completadas);
                    existing.setTareasEnProgreso(enProgreso);
                    existing.setTareasPendientes(pendientes);
                    existing.setPorcentajeAvance(porcentaje);
                    existing.setCalculadoAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> KpiSnapshot.builder()
                        .projectId(projectId)
                        .totalTareas(total)
                        .tareasCompletadas(completadas)
                        .tareasEnProgreso(enProgreso)
                        .tareasPendientes(pendientes)
                        .porcentajeAvance(porcentaje)
                        .calculadoAt(LocalDateTime.now())
                        .build());

        KpiSnapshot saved = kpiSnapshotRepository.save(snapshot);
        log.info("KPI recalculado para proyecto {}: {}% avance ({}/{} completadas)",
                projectId, porcentaje, completadas, total);
        return saved;
    }
}

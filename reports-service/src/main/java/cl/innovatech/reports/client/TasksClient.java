package cl.innovatech.reports.client;

import cl.innovatech.reports.dto.TaskReportResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasksClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.tasks-url}")
    private String tasksUrl;

    @Retry(name = "tasks-client", fallbackMethod = "getTasksByProjectFallback")
    public List<TaskReportResponse> getTasksByProject(Long projectId) {
        try {
            var response = restTemplate.getForObject(
                    tasksUrl + "/api/v1/tasks/project/" + projectId,
                    Map.class);
            if (response != null && response.get("data") instanceof List<?> data) {
                return data.stream()
                        .filter(item -> item instanceof Map<?, ?>)
                        .map(item -> {
                            Map<?, ?> map = (Map<?, ?>) item;
                            return TaskReportResponse.builder()
                                    .taskId(((Number) map.get("id")).longValue())
                                    .titulo((String) map.get("titulo"))
                                    .estado((String) map.get("estado"))
                                    .prioridad((String) map.get("prioridad"))
                                    .projectId(projectId)
                                    .responsableId(map.get("responsableId") != null
                                            ? ((Number) map.get("responsableId")).longValue() : null)
                                    .build();
                        })
                        .toList();
            }
        } catch (Exception ex) {
            log.warn("Error obteniendo tareas del proyecto {}: {}", projectId, ex.getMessage());
            throw ex;
        }
        return List.of();
    }

    public List<TaskReportResponse> getTasksByProjectFallback(Long projectId, Exception ex) {
        log.warn("Fallback getTasksByProject para projectId={}: {}", projectId, ex.getMessage());
        return List.of();
    }

    @Retry(name = "tasks-client", fallbackMethod = "getTasksByUserFallback")
    public List<TaskReportResponse> getTasksByUser(Long userId) {
        try {
            var response = restTemplate.getForObject(
                    tasksUrl + "/api/v1/tasks?responsableId=" + userId,
                    Map.class);
            if (response != null && response.get("data") instanceof List<?> data) {
                return data.stream()
                        .filter(item -> item instanceof Map<?, ?>)
                        .map(item -> {
                            Map<?, ?> map = (Map<?, ?>) item;
                            return TaskReportResponse.builder()
                                    .taskId(((Number) map.get("id")).longValue())
                                    .titulo((String) map.get("titulo"))
                                    .estado((String) map.get("estado"))
                                    .prioridad((String) map.get("prioridad"))
                                    .responsableId(userId)
                                    .projectId(map.get("projectId") != null
                                            ? ((Number) map.get("projectId")).longValue() : null)
                                    .build();
                        })
                        .toList();
            }
        } catch (Exception ex) {
            log.warn("Error obteniendo tareas del usuario {}: {}", userId, ex.getMessage());
            throw ex;
        }
        return List.of();
    }

    public List<TaskReportResponse> getTasksByUserFallback(Long userId, Exception ex) {
        log.warn("Fallback getTasksByUser para userId={}: {}", userId, ex.getMessage());
        return List.of();
    }
}

package cl.innovatech.projects_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TasksClient {

    private static final ParameterizedTypeReference<ApiResponse<List<TaskSummary>>> TASKS_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestTemplate restTemplate;
    private final String tasksUrl;
    private final Long integrationUserId;
    private final String integrationUserRole;

    public TasksClient(RestTemplate restTemplate,
                       @Value("${app.services.tasks-url}") String tasksUrl,
                       @Value("${app.integration.user-id:1}") Long integrationUserId,
                       @Value("${app.integration.user-role:ADMIN}") String integrationUserRole) {
        this.restTemplate = restTemplate;
        this.tasksUrl = tasksUrl;
        this.integrationUserId = integrationUserId;
        this.integrationUserRole = integrationUserRole;
    }

    public List<TaskSummary> findByProject(Long projectId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", integrationUserId.toString());
        headers.set("X-User-Role", integrationUserRole);

        try {
            ResponseEntity<ApiResponse<List<TaskSummary>>> response = restTemplate.exchange(
                    tasksUrl + "/api/v1/tasks/project/" + projectId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    TASKS_RESPONSE_TYPE
            );

            ApiResponse<List<TaskSummary>> body = response.getBody();
            if (body == null || body.data() == null) {
                return List.of();
            }
            return body.data();
        } catch (RestClientException ex) {
            log.warn("Error al consultar tareas del proyecto {}: {}", projectId, ex.getMessage());
            throw new IllegalStateException("No se pudo calcular el avance del proyecto " + projectId);
        }
    }

    public record TaskSummary(Long id, String estado) {
    }

    public record ApiResponse<T>(boolean success, String message, T data, LocalDateTime timestamp) {
    }
}

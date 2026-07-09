package cl.innovatech.reports.client;

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
public class ProjectsClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.projects-url}")
    private String projectsUrl;

    @Retry(name = "projects-client", fallbackMethod = "getProjectNameFallback")
    public String getProjectName(Long projectId) {
        try {
            var response = restTemplate.getForObject(
                    projectsUrl + "/api/v1/projects/" + projectId,
                    Map.class);
            if (response != null && response.get("nombre") instanceof String nombre) {
                return nombre;
            }
        } catch (Exception ex) {
            log.warn("Error obteniendo nombre del proyecto {}: {}", projectId, ex.getMessage());
            throw ex;
        }
        return "Proyecto " + projectId;
    }

    public String getProjectNameFallback(Long projectId, Exception ex) {
        log.warn("Fallback getProjectName para projectId={}: {}", projectId, ex.getMessage());
        return "Proyecto " + projectId;
    }

    @Retry(name = "projects-client", fallbackMethod = "getAllProjectIdsFallback")
    public List<Long> getAllProjectIds() {
        try {
            var response = restTemplate.getForObject(
                    projectsUrl + "/api/v1/projects",
                    List.class);
            if (response != null) {
                return response.stream()
                        .filter(item -> item instanceof Map<?, ?>)
                        .map(item -> ((Number) ((Map<?, ?>) item).get("id")).longValue())
                        .toList();
            }
        } catch (Exception ex) {
            log.warn("Error obteniendo proyectos: {}", ex.getMessage());
            throw ex;
        }
        return List.of();
    }

    public List<Long> getAllProjectIdsFallback(Exception ex) {
        log.warn("Fallback getAllProjectIds: {}", ex.getMessage());
        return List.of();
    }
}

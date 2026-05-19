package cl.innovatech.tasks.client;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ProjectsClient {

    private final RestTemplate restTemplate;
    private final String projectsUrl;

    public ProjectsClient(RestTemplate restTemplate,
                          @Value("${app.services.projects-url}") String projectsUrl) {
        this.restTemplate = restTemplate;
        this.projectsUrl = projectsUrl;
    }

    @Retry(name = "projects-client", fallbackMethod = "projectExistsFallback")
    public boolean projectExists(Long projectId) {
        try {
            restTemplate.getForObject(projectsUrl + "/api/v1/projects/" + projectId, Object.class);
            return true;
        } catch (Exception ex) {
            log.warn("Error al verificar proyecto {}: {}", projectId, ex.getMessage());
            throw ex;
        }
    }

    public boolean projectExistsFallback(Long projectId, Exception ex) {
        log.error("Fallback: no se pudo verificar proyecto {} después de reintentos", projectId);
        return false;
    }
}

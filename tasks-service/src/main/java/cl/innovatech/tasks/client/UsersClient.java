package cl.innovatech.tasks.client;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class UsersClient {

    private final RestTemplate restTemplate;
    private final String usersUrl;

    public UsersClient(RestTemplate restTemplate,
                       @Value("${app.services.users-url}") String usersUrl) {
        this.restTemplate = restTemplate;
        this.usersUrl = usersUrl;
    }

    @Retry(name = "users-client", fallbackMethod = "userExistsFallback")
    public boolean userExists(Long userId) {
        try {
            restTemplate.getForObject(usersUrl + "/api/v1/users/" + userId, Object.class);
            return true;
        } catch (Exception ex) {
            log.warn("Error al verificar usuario {}: {}", userId, ex.getMessage());
            throw ex;
        }
    }

    public boolean userExistsFallback(Long userId, Exception ex) {
        log.error("Fallback: no se pudo verificar usuario {} después de reintentos", userId);
        return false;
    }
}

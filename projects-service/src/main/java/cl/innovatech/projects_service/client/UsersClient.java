package cl.innovatech.projects_service.client;

import cl.innovatech.projects_service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
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

    public UserSummary getUserSummary(Long userId) {
        try {
            UserSummary user = restTemplate.getForObject(usersUrl + "/api/v1/users/" + userId, UserSummary.class);
            if (user == null || user.id() == null) {
                throw new ResourceNotFoundException("Usuario no encontrado: " + userId);
            }
            return user;
        } catch (HttpStatusCodeException ex) {
            if (HttpStatusCode.valueOf(404).equals(ex.getStatusCode())) {
                throw new ResourceNotFoundException("Usuario no encontrado: " + userId);
            }
            log.warn("Error HTTP al consultar usuario {}: {}", userId, ex.getMessage());
            throw new IllegalStateException("No se pudo validar el usuario " + userId);
        } catch (RestClientException ex) {
            log.warn("Error al consultar usuario {}: {}", userId, ex.getMessage());
            throw new IllegalStateException("No se pudo validar el usuario " + userId);
        }
    }

    public record UserSummary(Long id, Boolean enabled) {
    }
}

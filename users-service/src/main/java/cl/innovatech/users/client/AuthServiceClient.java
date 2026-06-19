package cl.innovatech.users.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceClient(
            RestTemplate restTemplate,
            @Value("${auth-service.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public void syncCredentials(Long userId, String email, String password, boolean active) {
        String url = authServiceUrl + "/api/v1/auth/internal/users/" + userId;
        SyncCredentialsRequest body = new SyncCredentialsRequest(email, password, active);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body), Void.class);
        } catch (Exception ex) {
            log.error("Error sincronizando credenciales en auth-service para userId {}: {}", userId, ex.getMessage());
            throw new IllegalStateException("No se pudieron sincronizar las credenciales con auth-service", ex);
        }
    }

    public void deleteCredentials(Long userId) {
        String url = authServiceUrl + "/api/v1/auth/internal/users/" + userId;

        try {
            restTemplate.delete(url);
        } catch (Exception ex) {
            log.error("Error eliminando credenciales en auth-service para userId {}: {}", userId, ex.getMessage());
            throw new IllegalStateException("No se pudieron eliminar las credenciales en auth-service", ex);
        }
    }

    public record SyncCredentialsRequest(String email, String password, Boolean active) {}
}

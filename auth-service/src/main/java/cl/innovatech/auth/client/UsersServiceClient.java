package cl.innovatech.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Cliente REST hacia users-service.
 *
 * auth-service necesita consultar a users-service para:
 *  1. Verificar que el usuario existe y está ACTIVO antes de emitir un token.
 *  2. Obtener la lista de permisos del usuario (roles → permisos) para incluirla en el JWT.
 *  3. Registrar el authUserId en users-service al crear una cuenta nueva.
 *
 * Protegido con CircuitBreaker + Retry de Resilience4j.
 */
@Component
@Slf4j
public class UsersServiceClient {

    private static final String CB = "users-service";

    private final RestTemplate restTemplate;
    private final String usersServiceUrl;

    public UsersServiceClient(
            RestTemplate restTemplate,
            @Value("${users-service.url}") String usersServiceUrl) {
        this.restTemplate   = restTemplate;
        this.usersServiceUrl = usersServiceUrl;
    }

    // ─── Consultas ────────────────────────────────────────────────────────

    /**
     * Verifica que el usuario existe en users-service y está en estado ACTIVO.
     * Si users-service no responde, el circuit breaker retorna false → login rechazado
     * con mensaje genérico (no leakea información de disponibilidad del servicio).
     */
    @CircuitBreaker(name = CB, fallbackMethod = "isUserActiveFallback")
    @Retry(name = CB)
    public boolean isUserActive(Long usersServiceId) {
        String url = usersServiceUrl + "/api/v1/users/" + usersServiceId;
        try {
            ResponseEntity<UserStatusResponse> resp =
                restTemplate.getForEntity(url, UserStatusResponse.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) return false;
            return Boolean.TRUE.equals(resp.getBody().enabled());
        } catch (Exception ex) {
            log.error("Error consultando estado de usuario {}: {}", usersServiceId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Obtiene la lista de nombres de permisos del usuario para incluirlos en el JWT.
     * Ej: ["USUARIOS_LEER", "PROYECTOS_CREAR", "TAREAS_EDITAR"]
     */
    @CircuitBreaker(name = CB, fallbackMethod = "getPermissionsFallback")
    @Retry(name = CB)
    public List<String> getUserPermissions(Long usersServiceId) {
        String url = usersServiceUrl + "/api/v1/users/" + usersServiceId;
        try {
            ResponseEntity<UserDetailResponse> resp =
                restTemplate.getForEntity(url, UserDetailResponse.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return List.of();
            }

            return resp.getBody().roles().stream()
                .filter(Objects::nonNull)
                .flatMap(role -> role.permissions().stream())
                .filter(Objects::nonNull)
                .map(PermissionDto::name)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        } catch (Exception ex) {
            log.error("Error obteniendo permisos de usuario {}: {}", usersServiceId, ex.getMessage());
            throw ex;
        }
    }

    @CircuitBreaker(name = CB, fallbackMethod = "getPrimaryRoleFallback")
    @Retry(name = CB)
    public String getPrimaryRole(Long usersServiceId) {
        String url = usersServiceUrl + "/api/v1/users/" + usersServiceId;
        try {
            ResponseEntity<UserDetailResponse> resp =
                restTemplate.getForEntity(url, UserDetailResponse.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return "ROLE_USER";
            }

            return resp.getBody().roles().stream()
                .filter(Objects::nonNull)
                .map(RoleDto::name)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("ROLE_USER");
        } catch (Exception ex) {
            log.error("Error obteniendo rol principal de usuario {}: {}", usersServiceId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Notifica a users-service que se creó una cuenta en auth-service.
     * Guarda el authUserId en el campo auth_user_id del usuario.
     */
    @CircuitBreaker(name = CB, fallbackMethod = "syncAuthIdFallback")
    @Retry(name = CB)
    public void syncAuthUserId(Long usersServiceId, Long authUserId) {
        String url = usersServiceUrl + "/api/v1/users/" + usersServiceId;
        try {
            SyncAuthIdRequest body = new SyncAuthIdRequest(authUserId);
            restTemplate.patchForObject(url, body, Void.class);
        } catch (Exception ex) {
            log.error("Error sincronizando authUserId {}: {}", authUserId, ex.getMessage());
            throw ex;
        }
    }

    // ─── Fallbacks ────────────────────────────────────────────────────────

    public boolean isUserActiveFallback(Long usersServiceId, Exception ex) {
        log.warn("CircuitBreaker activo para users-service (isUserActive). userId={}", usersServiceId);
        return false;
    }

    public List<String> getPermissionsFallback(Long usersServiceId, Exception ex) {
        log.warn("CircuitBreaker activo para users-service (getPermissions). userId={}", usersServiceId);
        return List.of();
    }

    public String getPrimaryRoleFallback(Long usersServiceId, Exception ex) {
        log.warn("CircuitBreaker activo para users-service (getPrimaryRole). userId={}", usersServiceId);
        return "ROLE_USER";
    }

    public void syncAuthIdFallback(Long usersServiceId, Long authUserId, Exception ex) {
        log.warn("CircuitBreaker activo para users-service (syncAuthId). userId={}", usersServiceId);
    }

    // ─── Inner records (respuestas de users-service) ──────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserStatusResponse(Long id, Boolean enabled) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PermissionDto(
        Long id,
        @JsonProperty("name") String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoleDto(
        Long id,
        @JsonProperty("name") String name,
        @JsonProperty("permissions") List<PermissionDto> permissions
    ) {
        public List<PermissionDto> permissions() {
            return permissions == null ? List.of() : permissions;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserDetailResponse(
        Long id,
        Boolean enabled,
        List<RoleDto> roles
    ) {
        public List<RoleDto> roles() {
            return roles == null ? List.of() : roles;
        }
    }

    public record SyncAuthIdRequest(Long authUserId) {}
}

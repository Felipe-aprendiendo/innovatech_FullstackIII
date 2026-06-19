package cl.innovatech.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
public class GatewayAccessPolicy {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/v1/auth",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator"
    );

    private static final List<String> ADMIN_ONLY_PREFIXES = List.of(
            "/api/v1/users",
            "/api/v1/roles",
            "/api/v1/permissions"
    );

    public boolean isPublic(ServerWebExchange exchange) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return true;
        }

        String path = exchange.getRequest().getURI().getPath();
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    public boolean requiresAdmin(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return ADMIN_ONLY_PREFIXES.stream().anyMatch(path::startsWith);
    }
}

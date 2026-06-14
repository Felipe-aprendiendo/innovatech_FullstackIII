package cl.innovatech.gateway.security;

import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final GatewayAccessPolicy accessPolicy;
    private final GatewayErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            GatewayAccessPolicy accessPolicy,
            GatewayErrorResponseWriter errorResponseWriter
    ) {
        this.jwtService = jwtService;
        this.accessPolicy = accessPolicy;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (accessPolicy.isPublic(exchange)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader)) {
            return errorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "Esta ruta requiere un token JWT.");
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            return errorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "El header Authorization debe usar formato Bearer.");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return errorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "El token JWT no puede estar vacio.");
        }

        try {
            AuthenticatedUser user = jwtService.parse(token);
            if (accessPolicy.requiresAdmin(exchange) && !user.isAdmin()) {
                return errorResponseWriter.write(exchange, HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso.");
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.set("X-User-Id", String.valueOf(user.userId()));
                        headers.set("X-User-Role", user.role());
                        if (StringUtils.hasText(user.email())) {
                            headers.set("X-User-Email", user.email());
                        }
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (IllegalArgumentException | JwtException exception) {
            return errorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

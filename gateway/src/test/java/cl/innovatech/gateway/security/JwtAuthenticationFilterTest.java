package cl.innovatech.gateway.security;

import cl.innovatech.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "clave-segura-para-jwt-en-tests-1234567890";

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(SECRET);

        JwtService jwtService = new JwtService(properties);
        filter = new JwtAuthenticationFilter(
                jwtService,
                new GatewayAccessPolicy(),
                new GatewayErrorResponseWriter(new com.fasterxml.jackson.databind.ObjectMapper())
        );
    }

    @Test
    void shouldPassPublicRouteWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build()
        );
        CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();

        filter.filter(exchange, chain).block();

        assertSame(exchange, chain.exchange);
    }

    @Test
    void shouldRejectProtectedRouteWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects").build()
        );

        filter.filter(exchange, new CapturingGatewayFilterChain()).block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void shouldRejectProtectedRouteWithInvalidToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
                        .build()
        );

        filter.filter(exchange, new CapturingGatewayFilterChain()).block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void shouldRejectProtectedRouteWithNonBearerAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Basic abc123")
                        .build()
        );

        filter.filter(exchange, new CapturingGatewayFilterChain()).block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void shouldRejectProtectedRouteWithBlankBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer   ")
                        .build()
        );

        filter.filter(exchange, new CapturingGatewayFilterChain()).block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void shouldRejectAdminRouteForNonAdminRole() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createToken(15L, "USER", "user@innovatech.cl"))
                        .build()
        );

        filter.filter(exchange, new CapturingGatewayFilterChain()).block();

        assertEquals(403, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void shouldPropagateUserHeadersWhenTokenIsValid() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createToken(7L, "ADMIN", "admin@innovatech.cl"))
                        .build()
        );
        CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();

        filter.filter(exchange, chain).block();

        assertNotNull(chain.exchange);
        assertEquals("7", chain.exchange.getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("ADMIN", chain.exchange.getRequest().getHeaders().getFirst("X-User-Role"));
        assertEquals("admin@innovatech.cl", chain.exchange.getRequest().getHeaders().getFirst("X-User-Email"));
    }

    @Test
    void shouldExposeFilterOrder() {
        assertEquals(-100, filter.getOrder());
    }

    private String createToken(Long userId, String role, String email) {
        return Jwts.builder()
                .claims(Map.of(
                        "userId", userId,
                        "role", role,
                        "email", email
                ))
                .subject(email)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private static class CapturingGatewayFilterChain implements GatewayFilterChain {

        private ServerWebExchange exchange;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.exchange = exchange;
            return Mono.empty();
        }
    }
}

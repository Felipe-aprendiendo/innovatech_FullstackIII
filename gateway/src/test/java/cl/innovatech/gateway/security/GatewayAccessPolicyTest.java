package cl.innovatech.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayAccessPolicyTest {

    private final GatewayAccessPolicy policy = new GatewayAccessPolicy();

    @Test
    void shouldTreatOptionsRequestsAsPublic() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/v1/projects").build()
        );

        assertTrue(policy.isPublic(exchange));
    }

    @Test
    void shouldTreatAuthRoutesAsPublic() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/auth/login").build()
        );

        assertTrue(policy.isPublic(exchange));
    }

    @Test
    void shouldTreatProjectsRoutesAsProtected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects").build()
        );

        assertFalse(policy.isPublic(exchange));
    }

    @Test
    void shouldRequireAdminForUsersRoute() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/1").build()
        );

        assertTrue(policy.requiresAdmin(exchange));
    }

    @Test
    void shouldNotRequireAdminForTasksRoute() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/tasks").build()
        );

        assertFalse(policy.requiresAdmin(exchange));
    }
}

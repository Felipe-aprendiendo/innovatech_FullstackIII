package cl.innovatech.gateway.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewaySecurityModelsTest {

    @Test
    void authenticatedUserShouldDetectAdminRoleIgnoringCase() {
        AuthenticatedUser admin = new AuthenticatedUser(1L, "admin", "admin@innovatech.cl");
        AuthenticatedUser user = new AuthenticatedUser(2L, "USER", "user@innovatech.cl");

        assertTrue(admin.isAdmin());
        assertFalse(user.isAdmin());
    }

    @Test
    void gatewayErrorResponseShouldExposeRecordValues() {
        Instant timestamp = Instant.parse("2026-06-19T03:00:00Z");
        GatewayErrorResponse response = new GatewayErrorResponse(
                timestamp,
                403,
                "Forbidden",
                "No tienes permisos para acceder a este recurso.",
                "/api/v1/users"
        );

        assertEquals(timestamp, response.timestamp());
        assertEquals(403, response.status());
        assertEquals("Forbidden", response.error());
        assertEquals("No tienes permisos para acceder a este recurso.", response.message());
        assertEquals("/api/v1/users", response.path());
    }
}

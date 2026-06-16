package cl.innovatech.gateway.security;

import cl.innovatech.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtServiceTest {

    @Test
    void shouldParseUserIdRoleAndEmailFromToken() {
        String secret = "clave-segura-para-jwt-en-tests-1234567890";
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(secret);

        JwtService jwtService = new JwtService(properties);
        String token = Jwts.builder()
                .claims(Map.of(
                        "userId", 21L,
                        "role", "ROLE_ADMIN",
                        "email", "admin@innovatech.cl"
                ))
                .subject("admin@innovatech.cl")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        AuthenticatedUser user = jwtService.parse(token);

        assertEquals(21L, user.userId());
        assertEquals("ADMIN", user.role());
        assertEquals("admin@innovatech.cl", user.email());
    }

    @Test
    void shouldParseCollectionRoleAndNumericSubject() {
        String secret = "clave-segura-para-jwt-en-tests-1234567890";
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(secret);

        JwtService jwtService = new JwtService(properties);
        String token = Jwts.builder()
                .claims(Map.of(
                        "roles", List.of("USER")
                ))
                .subject("33")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        AuthenticatedUser user = jwtService.parse(token);

        assertEquals(33L, user.userId());
        assertEquals("USER", user.role());
        assertNull(user.email());
    }
}

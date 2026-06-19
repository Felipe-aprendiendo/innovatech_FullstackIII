package cl.innovatech.gateway.security;

import cl.innovatech.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void shouldParseHexSecretAndClaimsAliases() {
        String hexSecret = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(hexSecret);

        JwtService jwtService = new JwtService(properties);
        String token = Jwts.builder()
                .claims(Map.of(
                        "usuarioId", "44",
                        "rol", "ROLE_PROJECT_LEAD",
                        "correo", "lead@innovatech.cl"
                ))
                .subject("lead@innovatech.cl")
                .signWith(Keys.hmacShaKeyFor(java.util.HexFormat.of().parseHex(hexSecret)))
                .compact();

        AuthenticatedUser user = jwtService.parse(token);

        assertEquals(44L, user.userId());
        assertEquals("PROJECT_LEAD", user.role());
        assertEquals("lead@innovatech.cl", user.email());
    }

    @Test
    void shouldRejectBlankSecret() {
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(" ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(properties)
        );

        assertEquals("JWT_SECRET no puede estar vacio.", exception.getMessage());
    }

    @Test
    void shouldRejectTokenWithoutUserId() {
        String secret = "clave-segura-para-jwt-en-tests-1234567890";
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(secret);

        JwtService jwtService = new JwtService(properties);
        String token = Jwts.builder()
                .claim("role", "ADMIN")
                .subject("admin@innovatech.cl")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parse(token)
        );

        assertEquals("El token JWT no contiene un userId valido.", exception.getMessage());
    }

    @Test
    void shouldRejectTokenWithoutRole() {
        String secret = "clave-segura-para-jwt-en-tests-1234567890";
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(secret);

        JwtService jwtService = new JwtService(properties);
        String token = Jwts.builder()
                .claim("userId", 55L)
                .subject("55")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parse(token)
        );

        assertEquals("El token JWT no contiene un rol valido.", exception.getMessage());
    }
}

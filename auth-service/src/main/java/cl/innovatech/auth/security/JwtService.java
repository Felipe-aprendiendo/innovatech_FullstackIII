package cl.innovatech.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * Servicio central de JWT.
 *
 * Responsabilidades:
 *  - Generar access tokens (corto plazo, firmados HS256)
 *  - Generar refresh tokens (opaco, UUID aleatorio)
 *  - Validar y parsear tokens
 *  - Exponer el secret key para que otros servicios puedan verificar (via /validate)
 *
 * El access token incluye en los claims:
 *   sub      → email del usuario
 *   userId   → ID en users-service
 *   permissions → lista de permisos del rol (USUARIOS_LEER, PROYECTOS_CREAR, …)
 *   type     → "access"
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessExp,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshExp) {

        byte[] keyBytes = Decoders.BASE64.decode(
            Base64.getEncoder().encodeToString(secret.getBytes()));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs  = accessExp;
        this.refreshTokenExpirationMs = refreshExp;
    }

    // ─── Generación ──────────────────────────────────────────────────────

    /**
     * Genera un access token JWT con los datos del usuario y sus permisos.
     */
    public String generateAccessToken(
            String email,
            Long usersServiceId,
            String primaryRole,
            List<String> permissions) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        String resolvedRole = primaryRole != null && !primaryRole.isBlank() ? primaryRole : "ROLE_USER";

        return Jwts.builder()
            .subject(email)
            .claim("userId",      usersServiceId)
            .claim("role",        resolvedRole)
            .claim("roles",       List.of(resolvedRole))
            .claim("permissions", permissions)
            .claim("type",        "access")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    /**
     * Genera un refresh token opaco (UUID v4).
     * Se almacena su hash SHA-256 en la DB y también en Redis.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // ─── Validación ──────────────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    // ─── Extracción de claims ─────────────────────────────────────────────

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Object perms = parseClaims(token).get("permissions");
        if (perms instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public Long extractUserId(String token) {
        Object uid = parseClaims(token).get("userId");
        if (uid instanceof Integer i) return i.longValue();
        if (uid instanceof Long l)    return l;
        return null;
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // ─── Utilidades ───────────────────────────────────────────────────────

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}

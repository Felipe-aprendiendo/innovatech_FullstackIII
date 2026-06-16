package cl.innovatech.gateway.security;

import cl.innovatech.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(GatewayJwtProperties properties) {
        String secret = properties.getSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT_SECRET no puede estar vacio.");
        }

        this.secretKey = Keys.hmacShaKeyFor(resolveSecretBytes(secret));
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = extractUserId(claims);
        String role = extractRole(claims);
        String email = extractEmail(claims);

        return new AuthenticatedUser(userId, role, email);
    }

    private byte[] resolveSecretBytes(String secret) {
        if (secret.matches("^[0-9a-fA-F]+$") && secret.length() % 2 == 0) {
            return HexFormat.of().parseHex(secret);
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private Long extractUserId(Claims claims) {
        Object rawUserId = firstNonNull(
                claims.get("userId"),
                claims.get("id"),
                claims.get("uid"),
                claims.get("usuarioId")
        );

        if (rawUserId instanceof Number number) {
            return number.longValue();
        }

        if (rawUserId instanceof String text && StringUtils.hasText(text)) {
            return Long.parseLong(text);
        }

        String subject = claims.getSubject();
        if (StringUtils.hasText(subject) && subject.matches("\\d+")) {
            return Long.parseLong(subject);
        }

        throw new IllegalArgumentException("El token JWT no contiene un userId valido.");
    }

    private String extractRole(Claims claims) {
        Object rawRole = firstNonNull(
                claims.get("role"),
                claims.get("rol"),
                claims.get("roles"),
                claims.get("authorities")
        );

        if (rawRole instanceof String text && StringUtils.hasText(text)) {
            return normalizeRole(text);
        }

        if (rawRole instanceof Collection<?> collection && !collection.isEmpty()) {
            Object firstRole = collection.iterator().next();
            if (firstRole != null) {
                return normalizeRole(firstRole.toString());
            }
        }

        throw new IllegalArgumentException("El token JWT no contiene un rol valido.");
    }

    private String extractEmail(Claims claims) {
        Object rawEmail = firstNonNull(claims.get("email"), claims.get("correo"));
        if (rawEmail instanceof String text && StringUtils.hasText(text)) {
            return text;
        }

        String subject = claims.getSubject();
        return StringUtils.hasText(subject) && subject.contains("@") ? subject : null;
    }

    private Object firstNonNull(Object... values) {
        return Arrays.stream(values)
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }
}

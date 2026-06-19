package cl.innovatech.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * Gestiona la lista negra (blacklist) de refresh tokens usando Redis.
 *
 * Cuando un usuario hace logout o un refresh token se rota, el token viejo
 * se añade a la blacklist en Redis con TTL = tiempo restante hasta expiración.
 * Así Redis se auto-limpia sin jobs manuales.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Añade un refresh token a la blacklist.
     *
     * @param rawToken  token en texto plano (se hashea antes de guardar)
     * @param ttl       tiempo hasta que Redis lo borre automáticamente
     */
    public void blacklist(String rawToken, Duration ttl) {
        String hash = sha256(rawToken);
        redisTemplate.opsForValue().set(PREFIX + hash, "revoked", ttl);
        log.debug("Token añadido a blacklist con TTL {}s", ttl.getSeconds());
    }

    /**
     * Verifica si un refresh token ha sido revocado.
     */
    public boolean isBlacklisted(String rawToken) {
        String hash = sha256(rawToken);
        Boolean exists = redisTemplate.hasKey(PREFIX + hash);
        return Boolean.TRUE.equals(exists);
    }

    // ─── Utilidad ─────────────────────────────────────────────────────────

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}

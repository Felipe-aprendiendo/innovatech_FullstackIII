package cl.innovatech.auth.service;

import cl.innovatech.auth.client.UsersServiceClient;
import cl.innovatech.auth.dto.AuthDTOs.*;
import cl.innovatech.auth.entity.*;
import cl.innovatech.auth.exception.*;
import cl.innovatech.auth.repository.*;
import cl.innovatech.auth.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final AuthUserRepository    authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLoginRepository  auditLoginRepository;
    private final JwtService            jwtService;
    private final PasswordEncoder       passwordEncoder;
    private final TokenBlacklistService blacklistService;
    private final UsersServiceClient    usersServiceClient;

    // ─── Registro ─────────────────────────────────────────────────────────

    /**
     * Crea credenciales de autenticación para un usuario ya existente en users-service.
     * Flujo: users-service crea el perfil → llama a auth-service para crear las credenciales.
     */
    @Transactional
    public AuthUserResponse registrar(RegisterRequest req) {
        if (authUserRepository.existsByEmail(req.getEmail())) {
            throw new EmailDuplicadoException(req.getEmail());
        }

        AuthUser user = AuthUser.builder()
            .email(req.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .usersServiceId(req.getUsersServiceId())
            .activo(true)
            .build();

        AuthUser saved = authUserRepository.save(user);

        // Notificar a users-service del ID de auth para vincular ambos registros
        if (req.getUsersServiceId() != null) {
            usersServiceClient.syncAuthUserId(req.getUsersServiceId(), saved.getId());
        }

        log.info("Credenciales creadas para: {}", saved.getEmail());
        return toResponse(saved);
    }

    // ─── Login ────────────────────────────────────────────────────────────

    @Transactional
    public TokenResponse login(LoginRequest req, HttpServletRequest httpReq) {
        String ip        = extractIp(httpReq);
        String userAgent = httpReq.getHeader("User-Agent");
        String email     = req.getEmail().toLowerCase().trim();

        AuthUser user = authUserRepository.findByEmail(email)
            .orElseThrow(() -> {
                registrarAudit(email, false, ip, userAgent, "Usuario no encontrado");
                return new CredencialesInvalidasException();
            });

        // Verificar cuenta no bloqueada
        if (user.getBloqueado()) {
            registrarAudit(email, false, ip, userAgent, "Cuenta bloqueada");
            throw new CuentaBloqueadaException(email);
        }

        // Verificar cuenta activa
        if (!user.getActivo()) {
            registrarAudit(email, false, ip, userAgent, "Cuenta inactiva");
            throw new CuentaInactivaException(email);
        }

        // Verificar contraseña
        log.info("PASSWORD INPUT: '{}'", req.getPassword());
        log.info("HASH EN DB: '{}'", user.getPasswordHash());
        log.info("ENCODER CLASS: {}", passwordEncoder.getClass().getName());
        log.info("MATCH RESULT: {}", passwordEncoder.matches( req.getPassword(), user.getPasswordHash()));
        log.info("INPUT: [{}]", req.getPassword());
        log.info("HASH: [{}]", user.getPasswordHash());
        log.info("MATCH: {}", passwordEncoder.matches(req.getPassword(), user.getPasswordHash()));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            user.incrementarIntentosFallidos();
            authUserRepository.save(user);
            registrarAudit(email, false, ip, userAgent,
                "Contraseña incorrecta (intento " + user.getIntentosFallidos() + ")");
            throw new CredencialesInvalidasException();
        }

        // Verificar que el usuario está ACTIVO en users-service
        if (user.getUsersServiceId() != null) {
            boolean activeInUsers = usersServiceClient.isUserActive(user.getUsersServiceId());
            if (!activeInUsers) {
                registrarAudit(email, false, ip, userAgent, "Usuario inactivo en users-service");
                throw new CuentaInactivaException(email);
            }
        }

        // Obtener permisos desde users-service
        List<String> permissions = user.getUsersServiceId() != null
            ? usersServiceClient.getUserPermissions(user.getUsersServiceId())
            : List.of();

        // Generar tokens
        String accessToken  = jwtService.generateAccessToken(email, user.getUsersServiceId(), permissions);
        String refreshToken = jwtService.generateRefreshToken();

        // Persistir refresh token
        persistRefreshToken(user, refreshToken, ip, userAgent);

        // Actualizar estado de login
        user.resetearIntentosFallidos();
        user.setUltimoLogin(LocalDateTime.now());
        authUserRepository.save(user);

        registrarAudit(email, true, ip, userAgent, "Login exitoso");
        log.info("Login exitoso para: {}", email);

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
            .userId(user.getUsersServiceId())
            .email(email)
            .permissions(permissions)
            .build();
    }

    // ─── Refresh ──────────────────────────────────────────────────────────

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest req, HttpServletRequest httpReq) {
        String rawToken = req.getRefreshToken();

        // Verificar blacklist en Redis primero (rápido)
        if (blacklistService.isBlacklisted(rawToken)) {
            throw new TokenInvalidoException("Refresh token revocado");
        }

        // Buscar en DB por hash
        String hash = TokenBlacklistService.sha256(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new TokenInvalidoException("Refresh token no encontrado"));

        if (!stored.isValid()) {
            throw new TokenInvalidoException("Refresh token expirado o revocado");
        }

        AuthUser user = stored.getAuthUser();

        if (!user.getActivo() || user.getBloqueado()) {
            throw new CuentaBloqueadaException(user.getEmail());
        }

        // Obtener permisos actualizados
        List<String> permissions = user.getUsersServiceId() != null
            ? usersServiceClient.getUserPermissions(user.getUsersServiceId())
            : List.of();

        // Rotar: revocar el token viejo, emitir uno nuevo
        stored.revoke();
        blacklistService.blacklist(rawToken,
            Duration.ofMillis(jwtService.getRefreshTokenExpirationMs()));

        String newAccessToken  = jwtService.generateAccessToken(
            user.getEmail(), user.getUsersServiceId(), permissions);
        String newRefreshToken = jwtService.generateRefreshToken();

        persistRefreshToken(user, newRefreshToken,
            extractIp(httpReq), httpReq.getHeader("User-Agent"));

        return TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
            .userId(user.getUsersServiceId())
            .email(user.getEmail())
            .permissions(permissions)
            .build();
    }

    // ─── Logout ───────────────────────────────────────────────────────────

    @Transactional
    public void logout(RefreshTokenRequest req) {
        String hash = TokenBlacklistService.sha256(req.getRefreshToken());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
            rt.revoke();
            refreshTokenRepository.save(rt);
        });
        blacklistService.blacklist(req.getRefreshToken(),
            Duration.ofMillis(jwtService.getRefreshTokenExpirationMs()));
        log.debug("Logout completado");
    }

    // ─── Validar token (para otros microservicios) ────────────────────────

    public ValidateTokenResponse validateToken(String token) {
        try {
            if (!jwtService.isTokenValid(token)) {
                return ValidateTokenResponse.builder().valid(false).build();
            }
            return ValidateTokenResponse.builder()
                .valid(true)
                .email(jwtService.extractEmail(token))
                .usersServiceId(jwtService.extractUserId(token))
                .permissions(jwtService.extractPermissions(token))
                .build();
        } catch (Exception e) {
            return ValidateTokenResponse.builder().valid(false).build();
        }
    }

    // ─── Cambio de contraseña ─────────────────────────────────────────────

    @Transactional
    public void cambiarPassword(String email, ChangePasswordRequest req) {
        AuthUser user = authUserRepository.findByEmail(email)
            .orElseThrow(() -> new CredencialesInvalidasException());

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);

        // Revocar todos los refresh tokens activos al cambiar contraseña
        refreshTokenRepository.revokeAllByUser(user);
        authUserRepository.save(user);
        log.info("Contraseña actualizada para: {}", email);
    }

    // ─── Verificar existencia (para users-service) ────────────────────────

    public boolean existsById(String authUserId) {
        try {
            return authUserRepository.existsById(Long.parseLong(authUserId));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ─── Limpieza programada ──────────────────────────────────────────────

    @Scheduled(cron = "0 0 3 * * *") // 3 AM cada día
    @Transactional
    public void limpiarTokensExpirados() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.info("Tokens expirados/revocados eliminados: {}", deleted);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────

    private void persistRefreshToken(AuthUser user, String rawToken,
                                     String ip, String userAgent) {
        String hash = TokenBlacklistService.sha256(rawToken);
        LocalDateTime expires = LocalDateTime.now()
            .plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000);

        RefreshToken rt = RefreshToken.builder()
            .authUser(user)
            .tokenHash(hash)
            .expiresAt(expires)
            .ipAddress(ip)
            .userAgent(userAgent)
            .build();
        refreshTokenRepository.save(rt);
    }

    private void registrarAudit(String email, boolean exitoso,
                                 String ip, String ua, String mensaje) {
        AuditLogin audit = AuditLogin.builder()
            .email(email).exitoso(exitoso)
            .ipAddress(ip).userAgent(ua)
            .mensaje(mensaje).build();
        auditLoginRepository.save(audit);
    }

    private String extractIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank())
            ? xff.split(",")[0].trim()
            : req.getRemoteAddr();
    }

    private AuthUserResponse toResponse(AuthUser u) {
        return AuthUserResponse.builder()
            .id(u.getId())
            .email(u.getEmail())
            .usersServiceId(u.getUsersServiceId())
            .activo(u.getActivo())
            .bloqueado(u.getBloqueado())
            .ultimoLogin(u.getUltimoLogin())
            .createdAt(u.getCreatedAt())
            .build();
    }

    public String generarHashTest(String password) {
        return passwordEncoder.encode(password);
    }
}


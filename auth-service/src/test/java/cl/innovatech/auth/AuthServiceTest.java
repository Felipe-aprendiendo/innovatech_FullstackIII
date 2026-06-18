package cl.innovatech.auth;

import cl.innovatech.auth.client.UsersServiceClient;
import cl.innovatech.auth.dto.AuthDTOs.*;
import cl.innovatech.auth.entity.AuthUser;
import cl.innovatech.auth.entity.RefreshToken;
import cl.innovatech.auth.exception.*;
import cl.innovatech.auth.repository.*;
import cl.innovatech.auth.security.JwtService;
import cl.innovatech.auth.service.AuthService;
import cl.innovatech.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Pruebas unitarias con Mockito")
class AuthServiceTest {

    @Mock private AuthUserRepository     authUserRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuditLoginRepository   auditLoginRepository;
    @Mock private TokenBlacklistService  blacklistService;
    @Mock private UsersServiceClient     usersServiceClient;

    @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4); // rounds bajos para test
    @Spy private JwtService jwtService = new JwtService(
        "innovatech-secret-key-2024-must-be-at-least-256-bits-long",
        900_000L, 604_800_000L);

    @InjectMocks
    private AuthService authService;

    private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    // ── Helpers ────────────────────────────────────────────────────────────

    private AuthUser buildUser(Long id, String email, String rawPassword, Long usersServiceId) {
        return AuthUser.builder()
            .id(id)
            .email(email)
            .passwordHash(passwordEncoder.encode(rawPassword))
            .usersServiceId(usersServiceId)
            .activo(true)
            .bloqueado(false)
            .intentosFallidos(0)
            .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // registrar
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("Crea credenciales correctamente para un email nuevo")
        void registra_email_nuevo() {
            when(authUserRepository.existsByEmail("nuevo@innovatech.cl")).thenReturn(false);
            when(authUserRepository.save(any())).thenAnswer(inv -> {
                AuthUser u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });
            doNothing().when(usersServiceClient).syncAuthUserId(anyLong(), anyLong());

            RegisterRequest req = RegisterRequest.builder()
                .email("nuevo@innovatech.cl")
                .password("Segura2024!")
                .usersServiceId(5L)
                .build();

            AuthUserResponse response = authService.registrar(req);

            assertThat(response.getEmail()).isEqualTo("nuevo@innovatech.cl");
            assertThat(response.getUsersServiceId()).isEqualTo(5L);
            assertThat(response.getActivo()).isTrue();
            verify(authUserRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Lanza EmailDuplicadoException si el email ya existe")
        void lanza_excepcion_email_duplicado() {
            when(authUserRepository.existsByEmail("dup@test.cl")).thenReturn(true);

            RegisterRequest req = RegisterRequest.builder()
                .email("dup@test.cl").password("Pass1234!").build();

            assertThatThrownBy(() -> authService.registrar(req))
                .isInstanceOf(EmailDuplicadoException.class)
                .hasMessageContaining("dup@test.cl");

            verify(authUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("Notifica a users-service el authUserId tras crear la cuenta")
        void notifica_users_service() {
            when(authUserRepository.existsByEmail(anyString())).thenReturn(false);
            when(authUserRepository.save(any())).thenAnswer(inv -> {
                AuthUser u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });

            RegisterRequest req = RegisterRequest.builder()
                .email("x@innovatech.cl").password("Pass1234!").usersServiceId(7L).build();

            authService.registrar(req);

            verify(usersServiceClient, times(1)).syncAuthUserId(7L, 10L);
        }

        @Test
        @DisplayName("No notifica a users-service si usersServiceId es nulo")
        void no_notifica_si_id_es_nulo() {
            when(authUserRepository.existsByEmail(anyString())).thenReturn(false);
            when(authUserRepository.save(any())).thenAnswer(inv -> {
                AuthUser u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            RegisterRequest req = RegisterRequest.builder()
                .email("sinid@innovatech.cl").password("Pass1234!").build();

            authService.registrar(req);

            verify(usersServiceClient, never()).syncAuthUserId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Encripta la contraseña antes de guardar")
        void encripta_password() {
            when(authUserRepository.existsByEmail(anyString())).thenReturn(false);
            when(authUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RegisterRequest req = RegisterRequest.builder()
                .email("y@innovatech.cl").password("PlanText123!").build();

            authService.registrar(req);

            ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
            verify(authUserRepository).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("PlanText123!");
            assertThat(passwordEncoder.matches("PlanText123!", captor.getValue().getPasswordHash()))
                .isTrue();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // login
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Login exitoso retorna access y refresh token")
        void login_exitoso() {
            String email = "admin@innovatech.cl";
            String rawPass = "Admin2024!";
            AuthUser user = buildUser(1L, email, rawPass, 10L);

            when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(usersServiceClient.isUserActive(10L)).thenReturn(true);
            when(usersServiceClient.getUserPermissions(10L))
                .thenReturn(List.of("USUARIOS_LEER", "PROYECTOS_CREAR"));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder().email(email).password(rawPass).build();
            TokenResponse response = authService.login(req, mockRequest);

            assertThat(response.getAccessToken()).isNotBlank();
            assertThat(response.getRefreshToken()).isNotBlank();
            assertThat(response.getEmail()).isEqualTo(email);
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getPermissions()).containsExactlyInAnyOrder(
                "USUARIOS_LEER", "PROYECTOS_CREAR");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Lanza CredencialesInvalidasException si el usuario no existe")
        void lanza_excepcion_usuario_no_existe() {
            when(authUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("noexiste@innovatech.cl").password("cualquiera1!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Lanza CredencialesInvalidasException con contraseña incorrecta")
        void lanza_excepcion_password_incorrecto() {
            AuthUser user = buildUser(1L, "u@test.cl", "CorrectPass1!", 1L);
            when(authUserRepository.findByEmail("u@test.cl")).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("u@test.cl").password("WrongPass1!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Incrementa intentos fallidos al fallar el login")
        void incrementa_intentos_fallidos() {
            AuthUser user = buildUser(1L, "u@test.cl", "CorrectPass1!", 1L);
            when(authUserRepository.findByEmail("u@test.cl")).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("u@test.cl").password("WrongPass1!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CredencialesInvalidasException.class);

            assertThat(user.getIntentosFallidos()).isEqualTo(1);
        }

        @Test
        @DisplayName("Lanza CuentaBloqueadaException si la cuenta está bloqueada")
        void lanza_excepcion_cuenta_bloqueada() {
            AuthUser user = buildUser(1L, "bloq@test.cl", "Pass1234!", 1L);
            user.setBloqueado(true);
            when(authUserRepository.findByEmail("bloq@test.cl")).thenReturn(Optional.of(user));
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("bloq@test.cl").password("Pass1234!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CuentaBloqueadaException.class);
        }

        @Test
        @DisplayName("Lanza CuentaInactivaException si la cuenta está inactiva")
        void lanza_excepcion_cuenta_inactiva() {
            AuthUser user = buildUser(1L, "inactivo@test.cl", "Pass1234!", 1L);
            user.setActivo(false);
            when(authUserRepository.findByEmail("inactivo@test.cl")).thenReturn(Optional.of(user));
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("inactivo@test.cl").password("Pass1234!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CuentaInactivaException.class);
        }

        @Test
        @DisplayName("Lanza CuentaInactivaException si users-service reporta usuario inactivo")
        void lanza_excepcion_inactivo_en_users_service() {
            AuthUser user = buildUser(1L, "u2@test.cl", "Pass1234!", 1L);
            when(authUserRepository.findByEmail("u2@test.cl")).thenReturn(Optional.of(user));
            when(usersServiceClient.isUserActive(1L)).thenReturn(false);
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("u2@test.cl").password("Pass1234!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CuentaInactivaException.class);
        }

        @Test
        @DisplayName("Resetea intentos fallidos tras login exitoso")
        void resetea_intentos_fallidos_tras_exito() {
            AuthUser user = buildUser(1L, "ok@test.cl", "Pass1234!", 1L);
            user.setIntentosFallidos(3);

            when(authUserRepository.findByEmail("ok@test.cl")).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(usersServiceClient.isUserActive(1L)).thenReturn(true);
            when(usersServiceClient.getUserPermissions(1L)).thenReturn(List.of());
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("ok@test.cl").password("Pass1234!").build();

            authService.login(req, mockRequest);

            assertThat(user.getIntentosFallidos()).isZero();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // refresh
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("Renueva tokens correctamente con refresh token válido")
        void renueva_tokens_correctamente() {
            AuthUser user = buildUser(1L, "u@test.cl", "Pass1234!", 1L);
            RefreshToken stored = RefreshToken.builder()
                .id(1L).authUser(user)
                .tokenHash(TokenBlacklistService.sha256("raw-token-123"))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

            when(blacklistService.isBlacklisted("raw-token-123")).thenReturn(false);
            when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));
            when(usersServiceClient.getUserPermissions(1L)).thenReturn(List.of("USUARIOS_LEER"));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(blacklistService).blacklist(anyString(), any());

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("raw-token-123").build();

            TokenResponse response = authService.refresh(req, mockRequest);

            assertThat(response.getAccessToken()).isNotBlank();
            assertThat(response.getRefreshToken()).isNotBlank();
            assertThat(response.getRefreshToken()).isNotEqualTo("raw-token-123");
        }

        @Test
        @DisplayName("Lanza TokenInvalidoException si el token está en blacklist")
        void lanza_excepcion_token_en_blacklist() {
            when(blacklistService.isBlacklisted("revoked-token")).thenReturn(true);

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("revoked-token").build();

            assertThatThrownBy(() -> authService.refresh(req, mockRequest))
                .isInstanceOf(TokenInvalidoException.class);
        }

        @Test
        @DisplayName("Lanza TokenInvalidoException si el token no existe en BD")
        void lanza_excepcion_token_no_encontrado() {
            when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("token-inexistente").build();

            assertThatThrownBy(() -> authService.refresh(req, mockRequest))
                .isInstanceOf(TokenInvalidoException.class);
        }

        @Test
        @DisplayName("Lanza TokenInvalidoException si el token está expirado")
        void lanza_excepcion_token_expirado() {
            AuthUser user = buildUser(1L, "u@test.cl", "Pass1234!", 1L);
            RefreshToken expired = RefreshToken.builder()
                .id(1L).authUser(user)
                .tokenHash(TokenBlacklistService.sha256("expirado"))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

            when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
            when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expired));

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("expirado").build();

            assertThatThrownBy(() -> authService.refresh(req, mockRequest))
                .isInstanceOf(TokenInvalidoException.class);
        }

        @Test
        @DisplayName("Lanza CuentaBloqueadaException si el usuario está bloqueado")
        void lanza_excepcion_usuario_bloqueado() {
            AuthUser user = buildUser(1L, "u@test.cl", "Pass1234!", 1L);
            user.setBloqueado(true);
            RefreshToken stored = RefreshToken.builder()
                .id(1L).authUser(user)
                .tokenHash(TokenBlacklistService.sha256("token-valido"))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

            when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
            when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("token-valido").build();

            assertThatThrownBy(() -> authService.refresh(req, mockRequest))
                .isInstanceOf(CuentaBloqueadaException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // logout
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("Revoca el refresh token correctamente")
        void revoca_token_correctamente() {
            AuthUser user = buildUser(1L, "u@test.cl", "Pass1234!", 1L);
            RefreshToken stored = RefreshToken.builder()
                .id(1L).authUser(user)
                .tokenHash(TokenBlacklistService.sha256("mi-token"))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

            when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(blacklistService).blacklist(anyString(), any());

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("mi-token").build();

            authService.logout(req);

            assertThat(stored.getRevoked()).isTrue();
            verify(blacklistService, times(1)).blacklist(eq("mi-token"), any());
        }

        @Test
        @DisplayName("No lanza excepción si el token no existe en BD")
        void no_lanza_excepcion_token_inexistente() {
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
            doNothing().when(blacklistService).blacklist(anyString(), any());

            RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("no-existe").build();

            assertThatCode(() -> authService.logout(req)).doesNotThrowAnyException();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // validateToken
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("Retorna valid=true para un token correcto")
        void retorna_valido_para_token_correcto() {
            String token = jwtService.generateAccessToken(
                "admin@innovatech.cl", 1L, List.of("USUARIOS_LEER"));

            ValidateTokenResponse response = authService.validateToken(token);

            assertThat(response.isValid()).isTrue();
            assertThat(response.getEmail()).isEqualTo("admin@innovatech.cl");
            assertThat(response.getUsersServiceId()).isEqualTo(1L);
            assertThat(response.getPermissions()).contains("USUARIOS_LEER");
        }

        @Test
        @DisplayName("Retorna valid=false para un token inválido")
        void retorna_invalido_para_token_malformado() {
            ValidateTokenResponse response = authService.validateToken("token-malformado");

            assertThat(response.isValid()).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // cambiarPassword
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("cambiarPassword()")
    class CambiarPassword {

        @Test
        @DisplayName("Cambia la contraseña correctamente con credenciales válidas")
        void cambia_password_correctamente() {
            AuthUser user = buildUser(1L, "u@test.cl", "OldPass1!", 1L);
            when(authUserRepository.findByEmail("u@test.cl")).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(refreshTokenRepository.revokeAllByUser(any())).thenReturn(2);

            ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("OldPass1!")
                .newPassword("NewPass2024!")
                .build();

            authService.cambiarPassword("u@test.cl", req);

            assertThat(passwordEncoder.matches("NewPass2024!", user.getPasswordHash())).isTrue();
            verify(refreshTokenRepository, times(1)).revokeAllByUser(user);
        }

        @Test
        @DisplayName("Lanza CredencialesInvalidasException si la contraseña actual es incorrecta")
        void lanza_excepcion_password_actual_incorrecto() {
            AuthUser user = buildUser(1L, "u@test.cl", "OldPass1!", 1L);
            when(authUserRepository.findByEmail("u@test.cl")).thenReturn(Optional.of(user));

            ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("PasswordIncorrecta!")
                .newPassword("NewPass2024!")
                .build();

            assertThatThrownBy(() -> authService.cambiarPassword("u@test.cl", req))
                .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Revoca todos los refresh tokens activos al cambiar contraseña")
        void revoca_refresh_tokens_activos() {
            AuthUser user = buildUser(1L, "u@test.cl", "OldPass1!", 1L);
            when(authUserRepository.findByEmail("u@test.cl")).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(refreshTokenRepository.revokeAllByUser(user)).thenReturn(3);

            ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("OldPass1!")
                .newPassword("NewPass2024!")
                .build();

            authService.cambiarPassword("u@test.cl", req);

            verify(refreshTokenRepository, times(1)).revokeAllByUser(user);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // existsById
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("existsById()")
    class ExistsById {

        @Test
        @DisplayName("Retorna true si el ID existe")
        void retorna_true_si_existe() {
            when(authUserRepository.existsById(1L)).thenReturn(true);
            assertThat(authService.existsById("1")).isTrue();
        }

        @Test
        @DisplayName("Retorna false si el ID no existe")
        void retorna_false_si_no_existe() {
            when(authUserRepository.existsById(99L)).thenReturn(false);
            assertThat(authService.existsById("99")).isFalse();
        }

        @Test
        @DisplayName("Retorna false si el ID no es numérico")
        void retorna_false_si_id_invalido() {
            assertThat(authService.existsById("no-es-numero")).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // limpiarTokensExpirados
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("limpiarTokensExpirados()")
    class LimpiarTokensExpirados {

        @Test
        @DisplayName("Invoca la limpieza de tokens en el repositorio")
        void invoca_limpieza() {
            when(refreshTokenRepository.deleteExpiredAndRevoked(any())).thenReturn(5);

            authService.limpiarTokensExpirados();

            verify(refreshTokenRepository, times(1)).deleteExpiredAndRevoked(any());
        }
    }
}
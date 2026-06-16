package cl.innovatech.auth;

import cl.innovatech.auth.client.UsersServiceClient;
import cl.innovatech.auth.dto.AuthDTOs.*;
import cl.innovatech.auth.entity.AuthUser;
import cl.innovatech.auth.exception.*;
import cl.innovatech.auth.repository.*;
import cl.innovatech.auth.security.JwtService;
import cl.innovatech.auth.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// ═══════════════════════════════════════════════════════════════════════════
// UNIT TESTS — JwtService
// ═══════════════════════════════════════════════════════════════════════════
@DisplayName("JwtService — Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
            "innovatech-secret-key-2024-must-be-at-least-256-bits-long",
            900_000L,    // 15 min
            604_800_000L // 7 días
        );
    }

    @Test
    @DisplayName("Genera access token con claims correctos")
    void genera_access_token_con_claims() {
        String token = jwtService.generateAccessToken(
            "admin@innovatech.cl", 1L,
            List.of("USUARIOS_LEER", "PROYECTOS_CREAR"));

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@innovatech.cl");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        assertThat(jwtService.extractPermissions(token))
            .containsExactlyInAnyOrder("USUARIOS_LEER", "PROYECTOS_CREAR");
    }

    @Test
    @DisplayName("Valida token correctamente firmado")
    void valida_token_correcto() {
        String token = jwtService.generateAccessToken("user@test.cl", 2L, List.of());
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Rechaza token con firma diferente")
    void rechaza_token_firma_invalida() {
        // Token generado con secret diferente
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWNrZXIifQ.INVALID";
        assertThat(jwtService.isTokenValid(fakeToken)).isFalse();
    }

    @Test
    @DisplayName("Genera refresh tokens únicos (UUID)")
    void genera_refresh_tokens_unicos() {
        String t1 = jwtService.generateRefreshToken();
        String t2 = jwtService.generateRefreshToken();

        assertThat(t1).isNotBlank();
        assertThat(t2).isNotBlank();
        assertThat(t1).isNotEqualTo(t2);
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// UNIT TESTS — TokenBlacklistService (sin Redis — lógica de hash)
// ═══════════════════════════════════════════════════════════════════════════
@DisplayName("TokenBlacklistService — hash SHA-256")
class TokenBlacklistHashTest {

    @Test
    @DisplayName("sha256 produce hash consistente")
    void sha256_consistente() {
        String token = "mi-refresh-token-secreto";
        String hash1 = TokenBlacklistService.sha256(token);
        String hash2 = TokenBlacklistService.sha256(token);

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // hex de 32 bytes
    }

    @Test
    @DisplayName("sha256 produce hashes distintos para tokens distintos")
    void sha256_distintos() {
        String h1 = TokenBlacklistService.sha256("token-a");
        String h2 = TokenBlacklistService.sha256("token-b");

        assertThat(h1).isNotEqualTo(h2);
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// UNIT TESTS — AuthService (con Mockito)
// ═══════════════════════════════════════════════════════════════════════════
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Unit Tests")
class AuthServiceTest {

    @Mock AuthUserRepository     authUserRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock AuditLoginRepository   auditLoginRepository;
    @Mock TokenBlacklistService  blacklistService;
    @Mock UsersServiceClient     usersServiceClient;

    @Spy  PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4); // rounds bajos para test
    @Spy  JwtService jwtService = new JwtService(
            "innovatech-secret-key-2024-must-be-at-least-256-bits-long",
            900_000L, 604_800_000L);

    @InjectMocks AuthService authService;

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    // ─── login ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Login exitoso retorna tokens JWT")
        void login_exitoso() {
            String email = "admin@innovatech.cl";
            String rawPass = "Admin2024!";

            AuthUser user = AuthUser.builder()
                .id(1L).email(email)
                .passwordHash(passwordEncoder.encode(rawPass))
                .usersServiceId(10L)
                .activo(true).bloqueado(false).intentosFallidos(0)
                .build();

            when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(usersServiceClient.isUserActive(10L)).thenReturn(true);
            when(usersServiceClient.getUserPermissions(10L))
                .thenReturn(List.of("USUARIOS_LEER", "PROYECTOS_CREAR"));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder().email(email).password(rawPass).build();
            TokenResponse resp = authService.login(req, mockRequest);

            assertThat(resp.getAccessToken()).isNotBlank();
            assertThat(resp.getRefreshToken()).isNotBlank();
            assertThat(resp.getEmail()).isEqualTo(email);
            assertThat(resp.getPermissions()).contains("USUARIOS_LEER");
        }

        @Test
        @DisplayName("Lanza CredencialesInvalidasException si el usuario no existe")
        void login_usuario_no_existe() {
            when(authUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("noexiste@innovatech.cl").password("cualquier").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Lanza CredencialesInvalidasException con contraseña incorrecta")
        void login_password_incorrecto() {
            AuthUser user = AuthUser.builder()
                .id(1L).email("u@test.cl")
                .passwordHash(passwordEncoder.encode("CorrectPass1!"))
                .activo(true).bloqueado(false).intentosFallidos(0)
                .build();

            when(authUserRepository.findByEmail("u@test.cl")).thenReturn(Optional.of(user));
            when(authUserRepository.save(any())).thenReturn(user);
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("u@test.cl").password("WrongPass1!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Lanza CuentaBloqueadaException si la cuenta está bloqueada")
        void login_cuenta_bloqueada() {
            AuthUser user = AuthUser.builder()
                .id(1L).email("bloq@test.cl")
                .passwordHash("cualquiera")
                .activo(true).bloqueado(true).intentosFallidos(5)
                .build();

            when(authUserRepository.findByEmail("bloq@test.cl")).thenReturn(Optional.of(user));
            when(auditLoginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LoginRequest req = LoginRequest.builder()
                .email("bloq@test.cl").password("Pass1234!").build();

            assertThatThrownBy(() -> authService.login(req, mockRequest))
                .isInstanceOf(CuentaBloqueadaException.class);
        }
    }

    // ─── registrar ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("Registra correctamente con email nuevo")
        void registra_email_nuevo() {
            when(authUserRepository.existsByEmail(anyString())).thenReturn(false);
            when(authUserRepository.save(any())).thenAnswer(inv -> {
                AuthUser u = inv.getArgument(0);
                // Simular ID generado por DB
                try {
                    var f = AuthUser.class.getDeclaredField("id");
                    f.setAccessible(true);
                    f.set(u, 1L);
                } catch (Exception ignored) {}
                return u;
            });
            doNothing().when(usersServiceClient).syncAuthUserId(anyLong(), anyLong());

            RegisterRequest req = RegisterRequest.builder()
                .email("nuevo@innovatech.cl")
                .password("Segura2024!")
                .usersServiceId(5L)
                .build();

            AuthUserResponse resp = authService.registrar(req);

            assertThat(resp.getEmail()).isEqualTo("nuevo@innovatech.cl");
            assertThat(resp.getUsersServiceId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Lanza EmailDuplicadoException si el email ya existe")
        void registra_email_duplicado() {
            when(authUserRepository.existsByEmail("dup@test.cl")).thenReturn(true);

            RegisterRequest req = RegisterRequest.builder()
                .email("dup@test.cl").password("Pass1234!").build();

            assertThatThrownBy(() -> authService.registrar(req))
                .isInstanceOf(EmailDuplicadoException.class);
        }
    }

    // ─── AuthUser.incrementarIntentosFallidos ────────────────────────────

    @Nested
    @DisplayName("AuthUser — lógica de bloqueo")
    class BloqueoLogic {

        @Test
        @DisplayName("Se bloquea automáticamente al llegar a 5 intentos fallidos")
        void bloqueo_en_5_intentos() {
            AuthUser user = AuthUser.builder()
                .email("test@test.cl").passwordHash("x")
                .activo(true).bloqueado(false).intentosFallidos(0)
                .build();

            for (int i = 0; i < 5; i++) user.incrementarIntentosFallidos();

            assertThat(user.getBloqueado()).isTrue();
            assertThat(user.getIntentosFallidos()).isEqualTo(5);
        }

        @Test
        @DisplayName("Reset restaura intentos y desbloquea")
        void reset_desbloquea() {
            AuthUser user = AuthUser.builder()
                .email("test@test.cl").passwordHash("x")
                .activo(true).bloqueado(true).intentosFallidos(5)
                .build();

            user.resetearIntentosFallidos();

            assertThat(user.getBloqueado()).isFalse();
            assertThat(user.getIntentosFallidos()).isZero();
        }
    }
}

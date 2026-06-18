package cl.innovatech.auth;

import cl.innovatech.auth.security.JwtService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService — Pruebas unitarias")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
            "innovatech-secret-key-2024-must-be-at-least-256-bits-long",
            900_000L,     // 15 min
            604_800_000L  // 7 días
        );
    }

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessToken {

        @Test
        @DisplayName("Genera un token JWT no vacío")
        void genera_token_no_vacio() {
            String token = jwtService.generateAccessToken(
                "admin@innovatech.cl", 1L, List.of("USUARIOS_LEER"));

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        }

        @Test
        @DisplayName("El token incluye el email como subject")
        void incluye_email_como_subject() {
            String token = jwtService.generateAccessToken(
                "user@innovatech.cl", 5L, List.of());

            assertThat(jwtService.extractEmail(token)).isEqualTo("user@innovatech.cl");
        }

        @Test
        @DisplayName("El token incluye el userId correcto")
        void incluye_userid_correcto() {
            String token = jwtService.generateAccessToken(
                "user@innovatech.cl", 42L, List.of());

            assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        }

        @Test
        @DisplayName("El token incluye la lista de permisos")
        void incluye_permisos() {
            String token = jwtService.generateAccessToken(
                "admin@innovatech.cl", 1L,
                List.of("USUARIOS_LEER", "PROYECTOS_CREAR", "TAREAS_EDITAR"));

            assertThat(jwtService.extractPermissions(token))
                .containsExactlyInAnyOrder("USUARIOS_LEER", "PROYECTOS_CREAR", "TAREAS_EDITAR");
        }

        @Test
        @DisplayName("Genera permisos vacíos si la lista es vacía")
        void permisos_vacios() {
            String token = jwtService.generateAccessToken("user@innovatech.cl", 1L, List.of());

            assertThat(jwtService.extractPermissions(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshToken {

        @Test
        @DisplayName("Genera un refresh token no vacío")
        void genera_refresh_token() {
            String token = jwtService.generateRefreshToken();
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("Genera tokens únicos en cada llamada")
        void genera_tokens_unicos() {
            String t1 = jwtService.generateRefreshToken();
            String t2 = jwtService.generateRefreshToken();

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("El formato corresponde a un UUID válido")
        void formato_uuid_valido() {
            String token = jwtService.generateRefreshToken();
            assertThat(token).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("Retorna true para un token recién generado")
        void token_recien_generado_es_valido() {
            String token = jwtService.generateAccessToken("user@innovatech.cl", 1L, List.of());
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Retorna false para un token con firma inválida")
        void token_firma_invalida_es_invalido() {
            String fakeToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWNrZXIifQ.firmaInvalida";
            assertThat(jwtService.isTokenValid(fakeToken)).isFalse();
        }

        @Test
        @DisplayName("Retorna false para un string que no es JWT")
        void string_no_jwt_es_invalido() {
            assertThat(jwtService.isTokenValid("esto-no-es-un-jwt")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para un token firmado con otro secret")
        void token_otro_secret_es_invalido() {
            JwtService otroServicio = new JwtService(
                "otro-secret-completamente-diferente-de-256-bits-minimo", 900_000L, 604_800_000L);
            String tokenAjeno = otroServicio.generateAccessToken("x@x.cl", 1L, List.of());

            assertThat(jwtService.isTokenValid(tokenAjeno)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractExpiration()")
    class ExtractExpiration {

        @Test
        @DisplayName("La expiración es posterior al momento actual")
        void expiracion_es_futura() {
            String token = jwtService.generateAccessToken("user@innovatech.cl", 1L, List.of());

            assertThat(jwtService.extractExpiration(token))
                .isAfter(new java.util.Date());
        }
    }

    @Nested
    @DisplayName("Getters de configuración")
    class Getters {

        @Test
        @DisplayName("getAccessTokenExpirationMs retorna el valor configurado")
        void retorna_access_expiration() {
            assertThat(jwtService.getAccessTokenExpirationMs()).isEqualTo(900_000L);
        }

        @Test
        @DisplayName("getRefreshTokenExpirationMs retorna el valor configurado")
        void retorna_refresh_expiration() {
            assertThat(jwtService.getRefreshTokenExpirationMs()).isEqualTo(604_800_000L);
        }
    }
}
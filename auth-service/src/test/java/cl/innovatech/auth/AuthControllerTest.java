package cl.innovatech.auth;

import cl.innovatech.auth.controller.AuthController;
import cl.innovatech.auth.dto.AuthDTOs.*;
import cl.innovatech.auth.exception.*;
import cl.innovatech.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — Pruebas unitarias con MockMvc")
class AuthControllerTest {

    @Mock private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    // ── POST /auth/register ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register retorna 201 al crear credenciales")
    void register_retorna_201() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
            .email("nuevo@innovatech.cl").password("Segura2024!").usersServiceId(5L).build();

        AuthUserResponse res = AuthUserResponse.builder()
            .id(1L).email("nuevo@innovatech.cl").usersServiceId(5L).activo(true).build();

        when(authService.registrar(any())).thenReturn(res);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("nuevo@innovatech.cl"));
    }

    @Test
    @DisplayName("POST /auth/register retorna 409 si el email ya existe")
    void register_retorna_409_email_duplicado() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
            .email("dup@innovatech.cl").password("Segura2024!").build();

        when(authService.registrar(any()))
            .thenThrow(new EmailDuplicadoException("dup@innovatech.cl"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/register retorna 400 si el email es inválido")
    void register_retorna_400_email_invalido() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
            .email("no-es-email").password("Segura2024!").build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── POST /auth/login ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login retorna 200 con tokens al autenticar correctamente")
    void login_retorna_200_con_tokens() throws Exception {
        LoginRequest req = LoginRequest.builder()
            .email("admin@innovatech.cl").password("Admin2024!").build();

        TokenResponse res = TokenResponse.builder()
            .accessToken("fake-access-token")
            .refreshToken("fake-refresh-token")
            .tokenType("Bearer")
            .expiresIn(900L)
            .userId(1L)
            .email("admin@innovatech.cl")
            .permissions(List.of("USUARIOS_LEER"))
            .build();

        when(authService.login(any(), any())).thenReturn(res);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("fake-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("fake-refresh-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login retorna 401 con credenciales incorrectas")
    void login_retorna_401_credenciales_incorrectas() throws Exception {
        LoginRequest req = LoginRequest.builder()
            .email("admin@innovatech.cl").password("PassIncorrecto1!").build();

        when(authService.login(any(), any()))
            .thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login retorna 403 si la cuenta está bloqueada")
    void login_retorna_403_cuenta_bloqueada() throws Exception {
        LoginRequest req = LoginRequest.builder()
            .email("bloqueado@innovatech.cl").password("Pass1234!").build();

        when(authService.login(any(), any()))
            .thenThrow(new CuentaBloqueadaException("bloqueado@innovatech.cl"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /auth/login retorna 400 si falta la contraseña")
    void login_retorna_400_password_faltante() throws Exception {
        LoginRequest req = LoginRequest.builder()
            .email("admin@innovatech.cl").password("").build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── POST /auth/refresh ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/refresh retorna 200 con tokens renovados")
    void refresh_retorna_200() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
            .refreshToken("token-valido").build();

        TokenResponse res = TokenResponse.builder()
            .accessToken("new-access").refreshToken("new-refresh")
            .tokenType("Bearer").expiresIn(900L).build();

        when(authService.refresh(any(), any())).thenReturn(res);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    @DisplayName("POST /auth/refresh retorna 401 con token inválido")
    void refresh_retorna_401_token_invalido() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
            .refreshToken("token-invalido").build();

        when(authService.refresh(any(), any()))
            .thenThrow(new TokenInvalidoException("Refresh token expirado o revocado"));

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── POST /auth/logout ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/logout retorna 204 al cerrar sesión")
    void logout_retorna_204() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
            .refreshToken("token-a-revocar").build();

        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());

        verify(authService, times(1)).logout(any());
    }

    // ── GET /auth/validate ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /auth/validate retorna 200 con valid=true para token correcto")
    void validate_retorna_200_token_valido() throws Exception {
        ValidateTokenResponse res = ValidateTokenResponse.builder()
            .valid(true).email("admin@innovatech.cl")
            .usersServiceId(1L).permissions(List.of("USUARIOS_LEER")).build();

        when(authService.validateToken("token-correcto")).thenReturn(res);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer token-correcto"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.email").value("admin@innovatech.cl"));
    }

    @Test
    @DisplayName("GET /auth/validate retorna valid=false para token inválido")
    void validate_retorna_valid_false() throws Exception {
        ValidateTokenResponse res = ValidateTokenResponse.builder().valid(false).build();

        when(authService.validateToken(anyString())).thenReturn(res);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer token-malo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(false));
    }

    

  

   
}
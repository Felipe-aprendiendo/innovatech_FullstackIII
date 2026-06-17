package cl.innovatech.users;

import cl.innovatech.users.controller.UserController;
import cl.innovatech.users.dto.*;
import cl.innovatech.users.exception.EmailAlreadyExistsException;
import cl.innovatech.users.exception.GlobalExceptionHandler;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — Pruebas unitarias con MockMvc")
class UserControllerTest {

    @Mock private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new UserController(userService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    private UserResponseDTO buildResponse(Long id, String name, String email) {
        return UserResponseDTO.builder()
            .id(id).name(name).lastName("Apellido")
            .email(email).enabled(true).roles(new HashSet<>()).build();
    }

    private UserRequestDTO buildRequest(String name, String email, String password) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName(name);
        dto.setLastName("Apellido");
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    // ── GET /api/v1/users ────────────────────────────────────────────────

    @Test @DisplayName("GET /users retorna 200 con lista de usuarios")
    void get_all_retorna_200() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
            buildResponse(1L, "Ana", "ana@innovatech.cl"),
            buildResponse(2L, "Luis", "luis@innovatech.cl")));

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Ana"))
            .andExpect(jsonPath("$[1].name").value("Luis"));
    }

    @Test @DisplayName("GET /users retorna 200 con lista vacía")
    void get_all_lista_vacia() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/v1/users/{id} ───────────────────────────────────────────

    @Test @DisplayName("GET /users/{id} retorna 200 con usuario")
    void get_by_id_retorna_200() throws Exception {
        when(userService.findById(1L)).thenReturn(
            buildResponse(1L, "María", "m@innovatech.cl"));

        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("María"))
            .andExpect(jsonPath("$.email").value("m@innovatech.cl"));
    }

    @Test @DisplayName("GET /users/{id} retorna 404 si no existe")
    void get_by_id_retorna_404() throws Exception {
        when(userService.findById(99L))
            .thenThrow(new ResourceNotFoundException("Usuario no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/users/99"))
            .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/users ───────────────────────────────────────────────

    @Test @DisplayName("POST /users retorna 201 al crear usuario")
    void post_crea_usuario_retorna_201() throws Exception {
        UserRequestDTO req = buildRequest("Juan", "juan@innovatech.cl", "Password1!");
        UserResponseDTO res = buildResponse(1L, "Juan", "juan@innovatech.cl");
        when(userService.create(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Juan"));
    }

    @Test @DisplayName("POST /users retorna 409 si email duplicado")
    void post_retorna_409_email_duplicado() throws Exception {
        UserRequestDTO req = buildRequest("Ana", "dup@innovatech.cl", "Password1!");
        when(userService.create(any()))
            .thenThrow(new EmailAlreadyExistsException("El email ya está registrado"));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test @DisplayName("POST /users retorna 400 si nombre vacío")
    void post_retorna_400_nombre_vacio() throws Exception {
        UserRequestDTO req = buildRequest("", "x@innovatech.cl", "Password1!");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /users retorna 400 si email inválido")
    void post_retorna_400_email_invalido() throws Exception {
        UserRequestDTO req = buildRequest("Juan", "no-es-email", "Password1!");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── PUT /api/v1/users/{id} ───────────────────────────────────────────

    @Test @DisplayName("PUT /users/{id} retorna 200 al actualizar")
    void put_actualiza_usuario_retorna_200() throws Exception {
        UserRequestDTO req = buildRequest("Nuevo", "nuevo@innovatech.cl", "Password1!");
        UserResponseDTO res = buildResponse(1L, "Nuevo", "nuevo@innovatech.cl");
        when(userService.update(eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nuevo"));
    }

    @Test @DisplayName("PUT /users/{id} retorna 404 si no existe")
    void put_retorna_404_no_existe() throws Exception {
        UserRequestDTO req = buildRequest("X", "x@innovatech.cl", "Password1!");
        when(userService.update(eq(99L), any()))
            .thenThrow(new ResourceNotFoundException("Usuario no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/users/{id} ─────────────────────────────────────────

    @Test @DisplayName("DELETE /users/{id} retorna 204 al eliminar")
    void delete_retorna_204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
            .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test @DisplayName("DELETE /users/{id} retorna 404 si no existe")
    void delete_retorna_404_no_existe() throws Exception {
        doThrow(new ResourceNotFoundException("Usuario no encontrado con id: 99"))
            .when(userService).delete(99L);

        mockMvc.perform(delete("/api/v1/users/99"))
            .andExpect(status().isNotFound());
    }

    // ── PATCH /api/v1/users/{id}/toggle ──────────────────────────────────

    @Test @DisplayName("PATCH /users/{id}/toggle retorna 200 al cambiar estado")
    void patch_toggle_retorna_200() throws Exception {
        UserResponseDTO res = buildResponse(1L, "Ana", "ana@innovatech.cl");
        res.setEnabled(false);
        when(userService.toggleEnabled(1L)).thenReturn(res);

        mockMvc.perform(patch("/api/v1/users/1/toggle"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test @DisplayName("PATCH /users/{id}/toggle retorna 404 si no existe")
    void patch_toggle_retorna_404() throws Exception {
        when(userService.toggleEnabled(99L))
            .thenThrow(new ResourceNotFoundException("Usuario no encontrado con id: 99"));

        mockMvc.perform(patch("/api/v1/users/99/toggle"))
            .andExpect(status().isNotFound());
    }
}
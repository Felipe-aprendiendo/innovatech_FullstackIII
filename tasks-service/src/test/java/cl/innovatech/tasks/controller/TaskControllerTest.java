package cl.innovatech.tasks.controller;

import cl.innovatech.tasks.dto.CommentResponse;
import cl.innovatech.tasks.dto.TaskResponse;
import cl.innovatech.tasks.dto.UpdateStatusRequest;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.exception.ForbiddenException;
import cl.innovatech.tasks.exception.InvalidStateTransitionException;
import cl.innovatech.tasks.exception.ResourceNotFoundException;
import cl.innovatech.tasks.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = {TaskController.class, CommentController.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void findAll_retorna200ConListaDeTareas() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .titulo("Tarea test")
                .estado(Task.EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .build();

        given(taskService.findAll(any(), any(), any(), any(), any())).willReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].titulo").value("Tarea test"));
    }

    @Test
    void updateStatus_estadoValido_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .titulo("Tarea test")
                .estado(Task.EstadoTarea.EN_PROGRESO)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .build();

        given(taskService.updateStatus(eq(1L), any(UpdateStatusRequest.class), any(), any()))
                .willReturn(task);

        String body = """
                {"nuevoEstado": "EN_PROGRESO"}
                """;

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("EN_PROGRESO"));
    }

    @Test
    void delete_comoAdmin_retorna200() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void create_comoAdmin_retorna201() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Nueva tarea").estado(Task.EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.ALTA).build();

        given(taskService.create(any(), any(), any())).willReturn(task);

        String body = """
                {"titulo":"Nueva tarea","projectId":1,"responsableId":2}
                """;

        mockMvc.perform(post("/api/v1/tasks")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.titulo").value("Nueva tarea"));
    }

    @Test
    void findById_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea detalle").estado(Task.EstadoTarea.EN_PROGRESO)
                .prioridad(Task.PrioridadTarea.MEDIA).build();

        given(taskService.findById(eq(1L), any(), any())).willReturn(task);

        mockMvc.perform(get("/api/v1/tasks/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titulo").value("Tarea detalle"));
    }

    @Test
    void findByProject_retorna200() throws Exception {
        given(taskService.findByProject(eq(1L), any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks/project/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void addComment_retorna201() throws Exception {
        CommentResponse comment = CommentResponse.builder()
                .id(1L).taskId(1L).userId(1L).contenido("Comentario de prueba").build();

        given(taskService.addComment(eq(1L), any(), any(), any())).willReturn(comment);

        String body = """
                {"contenido":"Comentario de prueba"}
                """;

        mockMvc.perform(post("/api/v1/tasks/1/comments")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.contenido").value("Comentario de prueba"));
    }

    @Test
    void findById_tareaNoExiste_retorna404() throws Exception {
        given(taskService.findById(eq(99L), any(), any()))
                .willThrow(new ResourceNotFoundException("Tarea no encontrada: 99"));

        mockMvc.perform(get("/api/v1/tasks/99")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateStatus_transicionInvalida_retorna422() throws Exception {
        given(taskService.updateStatus(eq(1L), any(), any(), any()))
                .willThrow(new InvalidStateTransitionException("Transición inválida: COMPLETADA → PENDIENTE"));

        String body = """
                {"nuevoEstado": "PENDIENTE"}
                """;

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void delete_comoUser_retorna403() throws Exception {
        willThrow(new ForbiddenException("Solo ADMIN puede eliminar tareas"))
                .given(taskService).delete(any(), any());

        mockMvc.perform(delete("/api/v1/tasks/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── Additional Controller Tests ───────────────────────────────

    @Test
    void findAll_conParametroProjectId_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea proyecto").estado(Task.EstadoTarea.PENDIENTE)
                .projectId(5L).build();

        given(taskService.findAll(eq(5L), isNull(), isNull(), any(), any()))
                .willReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks?projectId=5")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].projectId").value(5));
    }

    @Test
    void findAll_conParametroEstado_retorna200() throws Exception {
        given(taskService.findAll(isNull(), eq(Task.EstadoTarea.EN_PROGRESO), isNull(), any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks?estado=EN_PROGRESO")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void findAll_conParametroResponsableId_retorna200() throws Exception {
        given(taskService.findAll(isNull(), isNull(), eq(10L), any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks?responsableId=10")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void update_comoProjectLead_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea actualizada").estado(Task.EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.ALTA).build();

        given(taskService.update(eq(1L), any(), any(), any())).willReturn(task);

        String body = """
                {"titulo":"Tarea actualizada","prioridad":"ALTA"}
                """;

        mockMvc.perform(put("/api/v1/tasks/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PROJECT_LEAD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titulo").value("Tarea actualizada"));
    }

    @Test
    void findById_comoUser_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea del usuario").estado(Task.EstadoTarea.PENDIENTE)
                .responsableId(1L).build();

        given(taskService.findById(eq(1L), eq(1L), eq("USER"))).willReturn(task);

        mockMvc.perform(get("/api/v1/tasks/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk());
    }

    @Test
    void create_conDescripcionLarga_retorna201() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea").descripcion("Descripción muy larga " .repeat(10))
                .estado(Task.EstadoTarea.PENDIENTE).build();

        given(taskService.create(any(), any(), any())).willReturn(task);

        String body = """
                {"titulo":"Tarea","descripcion":"Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga Descripción muy larga ","projectId":1,"responsableId":2}
                """;

        mockMvc.perform(post("/api/v1/tasks")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void updateStatus_comoPriorityLead_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).estado(Task.EstadoTarea.COMPLETADA).build();

        given(taskService.updateStatus(eq(1L), any(), any(), any())).willReturn(task);

        String body = """
                {"nuevoEstado": "COMPLETADA"}
                """;

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PROJECT_LEAD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_conCommentarioLargo_retorna201() throws Exception {
        String contenidoLargo = "a".repeat(500);
        CommentResponse comment = CommentResponse.builder()
                .id(1L).taskId(1L).userId(1L).contenido(contenidoLargo).build();

        given(taskService.addComment(eq(1L), any(), any(), any())).willReturn(comment);

        String body = String.format("""
                {"contenido":"%s"}
                """, contenidoLargo);

        mockMvc.perform(post("/api/v1/tasks/1/comments")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void findByProject_conProyectoValido_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea proyecto 5").projectId(5L).build();

        given(taskService.findByProject(eq(5L), any(), any()))
                .willReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks/project/5")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].projectId").value(5));
    }

    @Test
    void findById_tareaInterna_retorna404() throws Exception {
        given(taskService.findById(eq(999L), any(), any()))
                .willThrow(new ResourceNotFoundException("Tarea no encontrada: 999"));

        mockMvc.perform(get("/api/v1/tasks/999")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_conPrioridadBaja_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).prioridad(Task.PrioridadTarea.BAJA).build();

        given(taskService.update(eq(1L), any(), any(), any())).willReturn(task);

        String body = """
                {"prioridad":"BAJA"}
                """;

        mockMvc.perform(put("/api/v1/tasks/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prioridad").value("BAJA"));
    }

    @Test
    void create_conFechaLimite_retorna201() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Tarea con fecha").estado(Task.EstadoTarea.PENDIENTE)
                .build();

        given(taskService.create(any(), any(), any())).willReturn(task);

        String body = """
                {"titulo":"Tarea con fecha","projectId":1,"responsableId":2,"fechaLimite":"2024-12-31"}
                """;

        mockMvc.perform(post("/api/v1/tasks")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void findAll_sinFiltros_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).titulo("Todas las tareas").build();

        given(taskService.findAll(isNull(), isNull(), isNull(), any(), any()))
                .willReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PROJECT_LEAD"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_aCompletada_retorna200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).estado(Task.EstadoTarea.COMPLETADA).build();

        given(taskService.updateStatus(eq(1L), any(), any(), any())).willReturn(task);

        String body = """
                {"nuevoEstado": "COMPLETADA"}
                """;

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_comoProjectLead_retorna201() throws Exception {
        CommentResponse comment = CommentResponse.builder()
                .id(1L).taskId(1L).userId(2L).contenido("Comentario de lider").build();

        given(taskService.addComment(eq(1L), any(), any(), any())).willReturn(comment);

        String body = """
                {"contenido":"Comentario de lider"}
                """;

        mockMvc.perform(post("/api/v1/tasks/1/comments")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "PROJECT_LEAD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}

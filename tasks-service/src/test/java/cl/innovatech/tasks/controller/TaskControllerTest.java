package cl.innovatech.tasks.controller;

import cl.innovatech.tasks.dto.TaskResponse;
import cl.innovatech.tasks.dto.UpdateStatusRequest;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}

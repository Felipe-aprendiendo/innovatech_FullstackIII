package cl.innovatech.projects_service.controller;

import cl.innovatech.projects_service.config.SecurityConfig;
import cl.innovatech.projects_service.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void shouldRejectInvalidProjectRequest() throws Exception {
        String invalidPayload = """
                {
                  "nombre": "",
                  "descripcion": "",
                  "prioridad": null,
                  "fechaInicio": null,
                  "fechaFin": null,
                  "responsableId": 0
                }
                """;

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.fields.descripcion").value("La descripcion es obligatoria"))
                .andExpect(jsonPath("$.fields.prioridad").value("La prioridad es obligatoria"))
                .andExpect(jsonPath("$.fields.fechaInicio").value("La fechaInicio es obligatoria"))
                .andExpect(jsonPath("$.fields.fechaFin").value("La fechaFin es obligatoria"))
                .andExpect(jsonPath("$.fields.responsableId").value("El responsableId debe ser mayor que cero"));
    }
}

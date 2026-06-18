package cl.innovatech.reports.client;

import cl.innovatech.reports.dto.TaskReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ClientsTest {

    @Mock
    private RestTemplate restTemplate;

    private ProjectsClient projectsClient;
    private TasksClient tasksClient;

    @BeforeEach
    void setUp() {
        projectsClient = new ProjectsClient(restTemplate);
        ReflectionTestUtils.setField(projectsClient, "projectsUrl", "http://localhost:8003");

        tasksClient = new TasksClient(restTemplate);
        ReflectionTestUtils.setField(tasksClient, "tasksUrl", "http://localhost:8004");
    }

    // ─── ProjectsClient: getProjectName ───────────────────────────

    @Test
    void getProjectName_conRespuestaValida_retornaNombre() {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", "Proyecto Alpha");
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        given(restTemplate.getForObject(
                "http://localhost:8003/api/v1/projects/1", Map.class))
                .willReturn(response);

        String result = projectsClient.getProjectName(1L);

        assertThat(result).isEqualTo("Proyecto Alpha");
    }

    @Test
    void getProjectName_conRespuestaNull_retornaFallbackConId() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(null);

        String result = projectsClient.getProjectName(5L);

        assertThat(result).isEqualTo("Proyecto 5");
    }

    @Test
    void getProjectName_conDataNoEsMap_retornaFallbackConId() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", "no-es-un-map");

        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(response);

        String result = projectsClient.getProjectName(3L);

        assertThat(result).isEqualTo("Proyecto 3");
    }

    @Test
    void getProjectName_conExcepcion_lanzaExcepcion() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willThrow(new RuntimeException("Service unavailable"));

        try {
            projectsClient.getProjectName(1L);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void getProjectNameFallback_retornaFallbackConId() {
        Exception ex = new RuntimeException("error");

        String result = projectsClient.getProjectNameFallback(99L, ex);

        assertThat(result).isEqualTo("Proyecto 99");
    }

    // ─── ProjectsClient: getAllProjectIds ──────────────────────────

    @Test
    void getAllProjectIds_conRespuestaValida_retornaLista() {
        Map<String, Object> proj1 = new HashMap<>();
        proj1.put("id", 1);
        Map<String, Object> proj2 = new HashMap<>();
        proj2.put("id", 2);

        Map<String, Object> response = new HashMap<>();
        response.put("data", List.of(proj1, proj2));

        given(restTemplate.getForObject(
                "http://localhost:8003/api/v1/projects", Map.class))
                .willReturn(response);

        List<Long> result = projectsClient.getAllProjectIds();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void getAllProjectIds_conRespuestaNull_retornaListaVacia() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(null);

        List<Long> result = projectsClient.getAllProjectIds();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllProjectIds_conDataNoEsLista_retornaListaVacia() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", "no-es-lista");

        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(response);

        List<Long> result = projectsClient.getAllProjectIds();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllProjectIds_conExcepcion_lanzaExcepcion() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willThrow(new RuntimeException("Connection refused"));

        try {
            projectsClient.getAllProjectIds();
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void getAllProjectIdsFallback_retornaListaVacia() {
        Exception ex = new RuntimeException("error");

        List<Long> result = projectsClient.getAllProjectIdsFallback(ex);

        assertThat(result).isEmpty();
    }

    // ─── TasksClient: getTasksByProject ───────────────────────────

    @Test
    void getTasksByProject_conRespuestaValida_retornaLista() {
        Map<String, Object> task = new HashMap<>();
        task.put("id", 1);
        task.put("titulo", "Tarea Test");
        task.put("estado", "PENDIENTE");
        task.put("prioridad", "ALTA");
        task.put("responsableId", 10);

        Map<String, Object> response = new HashMap<>();
        response.put("data", List.of(task));

        given(restTemplate.getForObject(
                "http://localhost:8004/api/v1/tasks/project/1", Map.class))
                .willReturn(response);

        List<TaskReportResponse> result = tasksClient.getTasksByProject(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitulo()).isEqualTo("Tarea Test");
        assertThat(result.get(0).getEstado()).isEqualTo("PENDIENTE");
        assertThat(result.get(0).getPrioridad()).isEqualTo("ALTA");
        assertThat(result.get(0).getProjectId()).isEqualTo(1L);
        assertThat(result.get(0).getResponsableId()).isEqualTo(10L);
    }

    @Test
    void getTasksByProject_conResponsableIdNull_retornaTareaConNull() {
        Map<String, Object> task = new HashMap<>();
        task.put("id", 2);
        task.put("titulo", "Tarea sin responsable");
        task.put("estado", "EN_PROGRESO");
        task.put("prioridad", "MEDIA");
        task.put("responsableId", null);

        Map<String, Object> response = new HashMap<>();
        response.put("data", List.of(task));

        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(response);

        List<TaskReportResponse> result = tasksClient.getTasksByProject(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getResponsableId()).isNull();
    }

    @Test
    void getTasksByProject_conRespuestaNull_retornaListaVacia() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(null);

        List<TaskReportResponse> result = tasksClient.getTasksByProject(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTasksByProject_conExcepcion_lanzaExcepcion() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willThrow(new RuntimeException("Timeout"));

        try {
            tasksClient.getTasksByProject(1L);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void getTasksByProjectFallback_retornaListaVacia() {
        Exception ex = new RuntimeException("error");

        List<TaskReportResponse> result = tasksClient.getTasksByProjectFallback(1L, ex);

        assertThat(result).isEmpty();
    }

    // ─── TasksClient: getTasksByUser ──────────────────────────────

    @Test
    void getTasksByUser_conRespuestaValida_retornaLista() {
        Map<String, Object> task = new HashMap<>();
        task.put("id", 3);
        task.put("titulo", "Tarea del usuario");
        task.put("estado", "COMPLETADA");
        task.put("prioridad", "BAJA");
        task.put("projectId", 5);

        Map<String, Object> response = new HashMap<>();
        response.put("data", List.of(task));

        given(restTemplate.getForObject(
                "http://localhost:8004/api/v1/tasks?responsableId=10", Map.class))
                .willReturn(response);

        List<TaskReportResponse> result = tasksClient.getTasksByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitulo()).isEqualTo("Tarea del usuario");
        assertThat(result.get(0).getResponsableId()).isEqualTo(10L);
        assertThat(result.get(0).getProjectId()).isEqualTo(5L);
    }

    @Test
    void getTasksByUser_conProjectIdNull_retornaTareaConNull() {
        Map<String, Object> task = new HashMap<>();
        task.put("id", 4);
        task.put("titulo", "Tarea sin proyecto");
        task.put("estado", "PENDIENTE");
        task.put("prioridad", "ALTA");
        task.put("projectId", null);

        Map<String, Object> response = new HashMap<>();
        response.put("data", List.of(task));

        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(response);

        List<TaskReportResponse> result = tasksClient.getTasksByUser(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isNull();
    }

    @Test
    void getTasksByUser_conRespuestaNull_retornaListaVacia() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(null);

        List<TaskReportResponse> result = tasksClient.getTasksByUser(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTasksByUser_conExcepcion_lanzaExcepcion() {
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willThrow(new RuntimeException("Network error"));

        try {
            tasksClient.getTasksByUser(1L);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void getTasksByUserFallback_retornaListaVacia() {
        Exception ex = new RuntimeException("error");

        List<TaskReportResponse> result = tasksClient.getTasksByUserFallback(1L, ex);

        assertThat(result).isEmpty();
    }
}

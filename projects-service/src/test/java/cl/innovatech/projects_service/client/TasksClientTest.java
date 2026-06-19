package cl.innovatech.projects_service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TasksClientTest {

    private MockRestServiceServer server;
    private TasksClient tasksClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        tasksClient = new TasksClient(restTemplate, "http://tasks-service", 77L, "ADMIN");
    }

    @Test
    void shouldReturnTasksAndSendIntegrationHeaders() {
        server.expect(requestTo("http://tasks-service/api/v1/tasks/project/5"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header("X-User-Id", "77"))
                .andExpect(header("X-User-Role", "ADMIN"))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "message": "ok",
                          "data": [
                            {"id": 1, "estado": "PENDIENTE"},
                            {"id": 2, "estado": "COMPLETADA"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<TasksClient.TaskSummary> response = tasksClient.findByProject(5L);

        assertEquals(2, response.size());
        assertEquals("PENDIENTE", response.get(0).estado());
        assertEquals("COMPLETADA", response.get(1).estado());
        server.verify();
    }

    @Test
    void shouldReturnEmptyListWhenBodyDoesNotContainData() {
        server.expect(requestTo("http://tasks-service/api/v1/tasks/project/6"))
                .andRespond(withSuccess("""
                        {"success": true, "message": "ok"}
                        """, MediaType.APPLICATION_JSON));

        List<TasksClient.TaskSummary> response = tasksClient.findByProject(6L);

        assertEquals(List.of(), response);
        server.verify();
    }

    @Test
    void shouldThrowIllegalStateWhenRemoteRequestFails() {
        server.expect(requestTo("http://tasks-service/api/v1/tasks/project/7"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tasksClient.findByProject(7L)
        );

        assertEquals("No se pudo calcular el avance del proyecto 7", exception.getMessage());
        server.verify();
    }
}

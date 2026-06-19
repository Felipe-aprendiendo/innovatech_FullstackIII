package cl.innovatech.projects_service.client;

import cl.innovatech.projects_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsersClientTest {

    private MockRestServiceServer server;
    private UsersClient usersClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        usersClient = new UsersClient(restTemplate, "http://users-service");
    }

    @Test
    void shouldReturnUserSummaryWhenResponseIsValid() {
        server.expect(requestTo("http://users-service/api/v1/users/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":5,"enabled":true}
                        """, MediaType.APPLICATION_JSON));

        UsersClient.UserSummary response = usersClient.getUserSummary(5L);

        assertEquals(5L, response.id());
        assertEquals(true, response.enabled());
        server.verify();
    }

    @Test
    void shouldThrowNotFoundWhenUserBodyIsEmpty() {
        server.expect(requestTo("http://users-service/api/v1/users/8"))
                .andRespond(withSuccess("""
                        {"enabled":true}
                        """, MediaType.APPLICATION_JSON));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> usersClient.getUserSummary(8L)
        );

        assertEquals("Usuario no encontrado: 8", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldThrowNotFoundWhenRemoteReturns404() {
        server.expect(requestTo("http://users-service/api/v1/users/9"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> usersClient.getUserSummary(9L)
        );

        assertEquals("Usuario no encontrado: 9", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldThrowIllegalStateWhenRemoteReturnsUnexpectedStatus() {
        server.expect(requestTo("http://users-service/api/v1/users/10"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> usersClient.getUserSummary(10L)
        );

        assertEquals("No se pudo validar el usuario 10", exception.getMessage());
        server.verify();
    }
}

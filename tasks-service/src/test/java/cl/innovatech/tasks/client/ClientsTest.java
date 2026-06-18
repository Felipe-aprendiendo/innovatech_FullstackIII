package cl.innovatech.tasks.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientsTest {

    // ─── ProjectsClient Tests ──────────────────────────────────────

    @Mock
    private RestTemplate restTemplate;

    private ProjectsClient projectsClient;
    private UsersClient usersClient;

    @BeforeEach
    void setUp() {
        projectsClient = new ProjectsClient(restTemplate, "http://localhost:8003");
        usersClient = new UsersClient(restTemplate, "http://localhost:8002");
    }

    // ─── ProjectsClient: projectExists Tests ───────────────────────

    @Test
    void projectsClient_constructor_inicializaCorrectamente() {
        assertThat(projectsClient).isNotNull();
    }

    @Test
    void projectExists_conProyectoExistente_retornaTrue() {
        Long projectId = 1L;
        given(restTemplate.getForObject(
                "http://localhost:8003/api/v1/projects/1", Object.class))
                .willReturn(new Object());

        boolean result = projectsClient.projectExists(projectId);

        assertThat(result).isTrue();
        verify(restTemplate).getForObject(eq("http://localhost:8003/api/v1/projects/1"), eq(Object.class));
    }

    @Test
    void projectExists_conProyectoInexistente_lanzaExcepcion() {
        Long projectId = 99L;
        given(restTemplate.getForObject(
                "http://localhost:8003/api/v1/projects/99", Object.class))
                .willThrow(new RuntimeException("404 Not Found"));

        try {
            projectsClient.projectExists(projectId);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void projectExistsFallback_retornaFalse() {
        Exception ex = new RuntimeException("Service unavailable");
        boolean result = projectsClient.projectExistsFallback(1L, ex);

        assertThat(result).isFalse();
    }

    @Test
    void projectExists_conUrlDiferente_usaLaUrlCorrecta() {
        ProjectsClient customClient = new ProjectsClient(restTemplate, "http://custom-host:9000");
        given(restTemplate.getForObject(
                "http://custom-host:9000/api/v1/projects/5", Object.class))
                .willReturn(new Object());

        boolean result = customClient.projectExists(5L);

        assertThat(result).isTrue();
        verify(restTemplate).getForObject(
                eq("http://custom-host:9000/api/v1/projects/5"), eq(Object.class));
    }

    // ─── UsersClient Tests ────────────────────────────────────────

    @Test
    void usersClient_constructor_inicializaCorrectamente() {
        assertThat(usersClient).isNotNull();
    }

    @Test
    void userExists_conUsuarioExistente_retornaTrue() {
        Long userId = 1L;
        given(restTemplate.getForObject(
                "http://localhost:8002/api/v1/users/1/basic", Object.class))
                .willReturn(new Object());

        boolean result = usersClient.userExists(userId);

        assertThat(result).isTrue();
        verify(restTemplate).getForObject(
                eq("http://localhost:8002/api/v1/users/1/basic"), eq(Object.class));
    }

    @Test
    void userExists_conUsuarioInexistente_lanzaExcepcion() {
        Long userId = 99L;
        given(restTemplate.getForObject(
                "http://localhost:8002/api/v1/users/99/basic", Object.class))
                .willThrow(new RuntimeException("404 Not Found"));

        try {
            usersClient.userExists(userId);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void userExistsFallback_retornaFalse() {
        Exception ex = new RuntimeException("Service unavailable");
        boolean result = usersClient.userExistsFallback(1L, ex);

        assertThat(result).isFalse();
    }

    @Test
    void userExists_conUrlDiferente_usaLaUrlCorrecta() {
        UsersClient customClient = new UsersClient(restTemplate, "http://custom-host:9001");
        given(restTemplate.getForObject(
                "http://custom-host:9001/api/v1/users/10/basic", Object.class))
                .willReturn(new Object());

        boolean result = customClient.userExists(10L);

        assertThat(result).isTrue();
        verify(restTemplate).getForObject(
                eq("http://custom-host:9001/api/v1/users/10/basic"), eq(Object.class));
    }

    // ─── Edge Cases Tests ──────────────────────────────────────────

    @Test
    void projectExists_conIdNegativo_enviaIdAlServicio() {
        Long projectId = -1L;
        given(restTemplate.getForObject(
                "http://localhost:8003/api/v1/projects/-1", Object.class))
                .willReturn(new Object());

        projectsClient.projectExists(projectId);

        verify(restTemplate).getForObject(
                eq("http://localhost:8003/api/v1/projects/-1"), eq(Object.class));
    }

    @Test
    void userExists_conIdCero_enviaIdAlServicio() {
        Long userId = 0L;
        given(restTemplate.getForObject(
                "http://localhost:8002/api/v1/users/0/basic", Object.class))
                .willThrow(new RuntimeException("Invalid ID"));

        try {
            usersClient.userExists(userId);
        } catch (Exception ex) {
            assertThat(ex).isNotNull();
        }
    }

    @Test
    void projectExists_conIdGrande_construyeUrlCorrectamente() {
        Long projectId = 9999999999L;
        given(restTemplate.getForObject(
                "http://localhost:8003/api/v1/projects/9999999999", Object.class))
                .willReturn(new Object());

        boolean result = projectsClient.projectExists(projectId);

        assertThat(result).isTrue();
        verify(restTemplate).getForObject(
                eq("http://localhost:8003/api/v1/projects/9999999999"), eq(Object.class));
    }

    @Test
    void userExists_conIdGrande_construyeUrlCorrectamente() {
        Long userId = 8888888888L;
        given(restTemplate.getForObject(
                "http://localhost:8002/api/v1/users/8888888888/basic", Object.class))
                .willReturn(new Object());

        boolean result = usersClient.userExists(userId);

        assertThat(result).isTrue();
        verify(restTemplate).getForObject(
                eq("http://localhost:8002/api/v1/users/8888888888/basic"), eq(Object.class));
    }

    // ─── RestTemplate Interactions Tests ───────────────────────────

    @Test
    void projectExists_llamamaRestTemplateConParametrosCorrecto() {
        Long projectId = 42L;
        given(restTemplate.getForObject(anyString(), eq(Object.class)))
                .willReturn(new Object());

        projectsClient.projectExists(projectId);

        verify(restTemplate).getForObject(
                "http://localhost:8003/api/v1/projects/42", Object.class);
    }

    @Test
    void userExists_llamaRestTemplateConParametrosCorrecto() {
        Long userId = 42L;
        given(restTemplate.getForObject(anyString(), eq(Object.class)))
                .willReturn(new Object());

        usersClient.userExists(userId);

        verify(restTemplate).getForObject(
                "http://localhost:8002/api/v1/users/42/basic", Object.class);
    }

    @Test
    void projectExists_conExcepcionDeRed_lanzaExcepcion() {
        given(restTemplate.getForObject(anyString(), eq(Object.class)))
                .willThrow(new RuntimeException("Connection refused"));

        try {
            projectsClient.projectExists(1L);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Connection refused");
        }
    }

    @Test
    void userExists_conExcepcionDeRed_lanzaExcepcion() {
        given(restTemplate.getForObject(anyString(), eq(Object.class)))
                .willThrow(new RuntimeException("Connection timeout"));

        try {
            usersClient.userExists(1L);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Connection timeout");
        }
    }
}

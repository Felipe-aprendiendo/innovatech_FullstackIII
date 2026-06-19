package cl.innovatech.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayPropertiesTest {

    @Test
    void shouldUpdateJwtSecret() {
        GatewayJwtProperties properties = new GatewayJwtProperties();

        properties.setSecret("nuevo-secreto-super-seguro-de-32-caracteres");

        assertEquals("nuevo-secreto-super-seguro-de-32-caracteres", properties.getSecret());
    }

    @Test
    void shouldUpdateServiceUrls() {
        GatewayServicesProperties properties = new GatewayServicesProperties();

        properties.setAuth("http://localhost:9001");
        properties.setUsers("http://localhost:9002");
        properties.setProjects("http://localhost:9003");
        properties.setTasks("http://localhost:9004");
        properties.setReports("http://localhost:9005");

        assertEquals("http://localhost:9001", properties.getAuth());
        assertEquals("http://localhost:9002", properties.getUsers());
        assertEquals("http://localhost:9003", properties.getProjects());
        assertEquals("http://localhost:9004", properties.getTasks());
        assertEquals("http://localhost:9005", properties.getReports());
    }

    @Test
    void shouldConfigureCorsListsAndCreateFilter() {
        GatewayCorsProperties properties = new GatewayCorsProperties();
        properties.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:4173"));
        properties.setAllowedMethods(List.of("GET", "POST"));
        properties.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        properties.setExposedHeaders(List.of("Authorization"));

        GatewayCorsConfig config = new GatewayCorsConfig();
        CorsWebFilter filter = config.corsWebFilter(properties);

        assertEquals(2, properties.getAllowedOrigins().size());
        assertTrue(properties.getAllowedMethods().contains("GET"));
        assertTrue(properties.getAllowedHeaders().contains("Authorization"));
        assertEquals(List.of("Authorization"), properties.getExposedHeaders());
        assertNotNull(filter);
    }
}

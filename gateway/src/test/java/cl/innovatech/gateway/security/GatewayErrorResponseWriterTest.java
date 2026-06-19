package cl.innovatech.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayErrorResponseWriterTest {

    @Test
    void shouldWriteJsonErrorResponse() {
        GatewayErrorResponseWriter writer = new GatewayErrorResponseWriter(new ObjectMapper().findAndRegisterModules());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects").build()
        );

        writer.write(exchange, HttpStatus.UNAUTHORIZED, "Esta ruta requiere un token JWT.").block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
        String body = exchange.getResponse().getBodyAsString().block();
        try {
            var json = new ObjectMapper().readTree(body);
            assertEquals(401, json.get("status").asInt());
            assertEquals("Esta ruta requiere un token JWT.", json.get("message").asText());
            assertEquals("/api/v1/projects", json.get("path").asText());
        } catch (JsonProcessingException exception) {
            throw new AssertionError("No se pudo leer el JSON generado: " + body, exception);
        }
    }

    @Test
    void shouldFallbackWhenSerializationFails() {
        ObjectMapper objectMapper = new ObjectMapper() {
            @Override
            public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
                throw new JsonProcessingException("boom") {
                };
            }
        };
        GatewayErrorResponseWriter writer = new GatewayErrorResponseWriter(objectMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users").build()
        );

        writer.write(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "error").block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertTrue(body.contains("\"status\":500"));
        assertTrue(body.contains("No se pudo serializar la respuesta de error."));
    }
}

package cl.innovatech.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class GatewayErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public GatewayErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        GatewayErrorResponse response = new GatewayErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getURI().getPath()
        );

        byte[] responseBody = serialize(response);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody)));
    }

    private byte[] serialize(GatewayErrorResponse response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException exception) {
            return ("{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"No se pudo serializar la respuesta de error.\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
    }
}

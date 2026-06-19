package cl.innovatech.projects_service.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldBuildNotFoundErrorResponse() {
        Map<String, Object> response = handler.handleNotFound(
                new ResourceNotFoundException("Proyecto no encontrado con ID: 55")
        );

        assertFalse((Boolean) response.get("success"));
        assertEquals("Proyecto no encontrado con ID: 55", response.get("message"));
    }

    @Test
    void shouldBuildIllegalStateErrorResponse() {
        Map<String, Object> response = handler.handleIllegalState(
                new IllegalStateException("No se puede modificar un proyecto cerrado")
        );

        assertFalse((Boolean) response.get("success"));
        assertEquals("No se puede modificar un proyecto cerrado", response.get("message"));
    }

    @Test
    void shouldBuildGenericErrorResponseWithFallbackMessage() {
        Map<String, Object> response = handler.handleGenericException(new Exception());

        assertFalse((Boolean) response.get("success"));
        assertEquals("Error interno del servidor", response.get("message"));
    }
}

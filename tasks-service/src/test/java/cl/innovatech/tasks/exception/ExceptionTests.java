package cl.innovatech.tasks.exception;

import cl.innovatech.tasks.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionTests {

    // ─── InnovatechException Tests ─────────────────────────────────

    @Test
    void innovatechException_conMensaje_seCreaCorrecto() {
        String mensaje = "Error de aplicación";
        InnovatechException ex = new InnovatechException(mensaje);

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    @Test
    void innovatechException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> {
            throw new InnovatechException("Test error");
        }).isInstanceOf(InnovatechException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");
    }

    @Test
    void innovatechException_conMensajeNull_acepta() {
        InnovatechException ex = new InnovatechException(null);

        assertThat(ex.getMessage()).isNull();
    }

    @Test
    void innovatechException_conMensajeVacio_acepta() {
        InnovatechException ex = new InnovatechException("");

        assertThat(ex.getMessage()).isEmpty();
    }

    // ─── ResourceNotFoundException Tests ───────────────────────────

    @Test
    void resourceNotFoundException_conMensaje_seCreaCorrecto() {
        String mensaje = "Recurso no encontrado";
        ResourceNotFoundException ex = new ResourceNotFoundException(mensaje);

        assertThat(ex).isInstanceOf(InnovatechException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    @Test
    void resourceNotFoundException_heredaDe_InnovatechException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");

        assertThat(ex).isInstanceOf(InnovatechException.class);
    }

    @Test
    void resourceNotFoundException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException("Recurso no encontrado");
        }).isInstanceOf(ResourceNotFoundException.class)
                .isInstanceOf(InnovatechException.class);
    }

    @Test
    void resourceNotFoundException_conMensajeEspecifico_mantieneMensaje() {
        String mensaje = "Tarea con ID 123 no encontrada";
        ResourceNotFoundException ex = new ResourceNotFoundException(mensaje);

        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    // ─── ForbiddenException Tests ──────────────────────────────────

    @Test
    void forbiddenException_conMensaje_seCreaCorrecto() {
        String mensaje = "Acceso denegado";
        ForbiddenException ex = new ForbiddenException(mensaje);

        assertThat(ex).isInstanceOf(InnovatechException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    @Test
    void forbiddenException_heredaDe_InnovatechException() {
        ForbiddenException ex = new ForbiddenException("Forbidden");

        assertThat(ex).isInstanceOf(InnovatechException.class);
    }

    @Test
    void forbiddenException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> {
            throw new ForbiddenException("Acceso denegado");
        }).isInstanceOf(ForbiddenException.class)
                .isInstanceOf(InnovatechException.class);
    }

    @Test
    void forbiddenException_conMensajeEspecifico_mantieneMensaje() {
        String mensaje = "Solo ADMIN puede eliminar tareas";
        ForbiddenException ex = new ForbiddenException(mensaje);

        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    // ─── InvalidStateTransitionException Tests ─────────────────────

    @Test
    void invalidStateTransitionException_conMensaje_seCreaCorrecto() {
        String mensaje = "Transición inválida";
        InvalidStateTransitionException ex = new InvalidStateTransitionException(mensaje);

        assertThat(ex).isInstanceOf(InnovatechException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    @Test
    void invalidStateTransitionException_heredaDe_InnovatechException() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException("Invalid");

        assertThat(ex).isInstanceOf(InnovatechException.class);
    }

    @Test
    void invalidStateTransitionException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> {
            throw new InvalidStateTransitionException("Transición inválida");
        }).isInstanceOf(InvalidStateTransitionException.class)
                .isInstanceOf(InnovatechException.class);
    }

    @Test
    void invalidStateTransitionException_conMensajeEspecifico_mantieneMensaje() {
        String mensaje = "Transición inválida: COMPLETADA → PENDIENTE";
        InvalidStateTransitionException ex = new InvalidStateTransitionException(mensaje);

        assertThat(ex.getMessage()).isEqualTo(mensaje);
    }

    // ─── GlobalExceptionHandler Tests ──────────────────────────────

    @ExtendWith(MockitoExtension.class)
    public static class GlobalExceptionHandlerTests {

        @InjectMocks
        private GlobalExceptionHandler handler;

        @Test
        void handleNotFound_retorna404() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Tarea no encontrada");
            ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Tarea no encontrada");
        }

        @Test
        void handleForbidden_retorna403() {
            ForbiddenException ex = new ForbiddenException("Acceso denegado");
            ResponseEntity<ApiResponse<Void>> response = handler.handleForbidden(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Acceso denegado");
        }

        @Test
        void handleInvalidTransition_retorna422() {
            InvalidStateTransitionException ex = new InvalidStateTransitionException("Transición inválida");
            ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidTransition(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Transición inválida");
        }

        @Test
        void handleValidation_retorna400() {
            FieldError fieldError = new FieldError("object", "field", "Error message");
            List<FieldError> errors = new ArrayList<>();
            errors.add(fieldError);

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(errors);

            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        void handleGeneric_retorna400() {
            InnovatechException ex = new InnovatechException("Error genérico");
            ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Error genérico");
        }

        @Test
        void handleUnexpected_retorna500() {
            Exception ex = new Exception("Error inesperado");
            ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        void handleNotFound_conMensajeDiferente_loMantiene() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Proyecto ID 999 no encontrado");
            ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

            assertThat(response.getBody().getMessage()).isEqualTo("Proyecto ID 999 no encontrado");
        }

        @Test
        void handleForbidden_conMensajeDiferente_loMantiene() {
            ForbiddenException ex = new ForbiddenException("Solo administradores pueden hacer esto");
            ResponseEntity<ApiResponse<Void>> response = handler.handleForbidden(ex);

            assertThat(response.getBody().getMessage()).isEqualTo("Solo administradores pueden hacer esto");
        }

        @Test
        void handleInvalidTransition_conMensajeDiferente_loMantiene() {
            InvalidStateTransitionException ex = new InvalidStateTransitionException("No se puede pasar de COMPLETADA a EN_PROGRESO");
            ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidTransition(ex);

            assertThat(response.getBody().getMessage()).isEqualTo("No se puede pasar de COMPLETADA a EN_PROGRESO");
        }

        @Test
        void handleGeneric_conMensajeDiferente_loMantiene() {
            InnovatechException ex = new InnovatechException("Error de negocio específico");
            ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(ex);

            assertThat(response.getBody().getMessage()).isEqualTo("Error de negocio específico");
        }
    }

    // ─── Exception Inheritance Tests ───────────────────────────────

    @Test
    void exceptionsInheritance_verificarJerarquia() {
        // Verificar que todas heredan de RuntimeException
        assertThat(new InnovatechException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ResourceNotFoundException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new ForbiddenException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new InvalidStateTransitionException("test")).isInstanceOf(RuntimeException.class);

        // Verificar herencia específica
        assertThat(new ResourceNotFoundException("test")).isInstanceOf(InnovatechException.class);
        assertThat(new ForbiddenException("test")).isInstanceOf(InnovatechException.class);
        assertThat(new InvalidStateTransitionException("test")).isInstanceOf(InnovatechException.class);
    }

    @Test
    void exceptions_puedenSerCapturadasPorPadre() {
        List<InnovatechException> exceptions = List.of(
                new InnovatechException("Base"),
                new ResourceNotFoundException("Not found"),
                new ForbiddenException("Forbidden"),
                new InvalidStateTransitionException("Invalid transition")
        );

        for (InnovatechException ex : exceptions) {
            assertThat(ex).isInstanceOf(InnovatechException.class);
        }
    }

    @Test
    void exceptions_stackTrace_disponible() {
        InnovatechException ex = new InnovatechException("Test");

        assertThat(ex.getStackTrace()).isNotNull();
        assertThat(ex.getStackTrace().length).isGreaterThan(0);
    }
}

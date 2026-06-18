package cl.innovatech.reports.exception;

import cl.innovatech.reports.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ─── InnovatechException ───────────────────────────────────────

    @Test
    void innovatechException_conMensaje_seCreaCorrecto() {
        InnovatechException ex = new InnovatechException("Error base");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("Error base");
    }

    @Test
    void innovatechException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> { throw new InnovatechException("error"); })
                .isInstanceOf(InnovatechException.class)
                .hasMessage("error");
    }

    @Test
    void innovatechException_conMensajeNull_acepta() {
        InnovatechException ex = new InnovatechException(null);
        assertThat(ex.getMessage()).isNull();
    }

    // ─── ResourceNotFoundException ─────────────────────────────────

    @Test
    void resourceNotFoundException_heredaDeInnovatechException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("No encontrado");

        assertThat(ex).isInstanceOf(InnovatechException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("No encontrado");
    }

    @Test
    void resourceNotFoundException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> { throw new ResourceNotFoundException("recurso faltante"); })
                .isInstanceOf(ResourceNotFoundException.class)
                .isInstanceOf(InnovatechException.class);
    }

    // ─── ForbiddenException ────────────────────────────────────────

    @Test
    void forbiddenException_heredaDeInnovatechException() {
        ForbiddenException ex = new ForbiddenException("Acceso denegado");

        assertThat(ex).isInstanceOf(InnovatechException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("Acceso denegado");
    }

    @Test
    void forbiddenException_alLanzar_esCapturable() {
        assertThatThrownBy(() -> { throw new ForbiddenException("forbidden"); })
                .isInstanceOf(ForbiddenException.class)
                .isInstanceOf(InnovatechException.class);
    }

    // ─── GlobalExceptionHandler ────────────────────────────────────

    @Test
    void handleNotFound_retorna404ConMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Reporte no encontrado");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Reporte no encontrado");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleForbidden_retorna403ConMensaje() {
        ForbiddenException ex = new ForbiddenException("Solo ADMIN puede ver esto");

        ResponseEntity<ApiResponse<Void>> response = handler.handleForbidden(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Solo ADMIN puede ver esto");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleGeneric_retorna400ConMensaje() {
        InnovatechException ex = new InnovatechException("Error de negocio");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Error de negocio");
    }

    @Test
    void handleUnexpected_retorna500() {
        Exception ex = new Exception("Error inesperado");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleNotFound_preservaMensajeOriginal() {
        ResourceNotFoundException ex = new ResourceNotFoundException("KPI proyecto 99 no encontrado");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getBody().getMessage()).contains("99");
    }

    @Test
    void handleForbidden_preservaMensajeOriginal() {
        ForbiddenException ex = new ForbiddenException("Rol insuficiente: USER");

        ResponseEntity<ApiResponse<Void>> response = handler.handleForbidden(ex);

        assertThat(response.getBody().getMessage()).contains("USER");
    }

    @Test
    void jerarquiaExcepciones_todasHereданDeRuntimeException() {
        assertThat(new InnovatechException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new ResourceNotFoundException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new ForbiddenException("x")).isInstanceOf(RuntimeException.class);
    }
}

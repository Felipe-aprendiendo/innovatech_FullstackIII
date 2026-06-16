package cl.innovatech.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    record ErrorResponse(LocalDateTime timestamp, int status,
                         String error, String message, String path) {}

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(
            CredencialesInvalidasException ex, WebRequest req) {
        // Delay artificial anti-timing-attack
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ErrorResponse> handleBloqueada(
            CuentaBloqueadaException ex, WebRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<ErrorResponse> handleInactiva(
            CuentaInactivaException ex, WebRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleToken(
            TokenInvalidoException ex, WebRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleEmail(
            EmailDuplicadoException ex, WebRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, WebRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validación fallida");
        body.put("campos", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest req) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String msg, WebRequest req) {
        return ResponseEntity.status(status).body(new ErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(),
            msg, req.getDescription(false).replace("uri=", "")
        ));
    }
}

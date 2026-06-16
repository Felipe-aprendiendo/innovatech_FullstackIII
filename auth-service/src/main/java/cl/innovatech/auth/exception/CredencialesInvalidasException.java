package cl.innovatech.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        // Mensaje genérico — no revela si el email existe o no
        super("Credenciales incorrectas");
    }
}

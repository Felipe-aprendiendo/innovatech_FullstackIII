package cl.innovatech.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CuentaBloqueadaException extends RuntimeException {
    public CuentaBloqueadaException(String email) {
        super("Cuenta bloqueada por múltiples intentos fallidos: " + email);
    }
}

package cl.innovatech.gateway.security;

public record AuthenticatedUser(Long userId, String role, String email) {
}

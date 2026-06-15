package cl.innovatech.gateway.security;

public record AuthenticatedUser(Long userId, String role, String email) {

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}

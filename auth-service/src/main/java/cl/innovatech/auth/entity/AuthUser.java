package cl.innovatech.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa SOLO las credenciales de autenticación.
 * Los datos de perfil (nombre, RUT, teléfono, etc.) viven en users-service.
 * La columna usersServiceId es el puente entre ambos microservicios.
 */
@Entity
@Table(name = "auth_users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** ID del mismo usuario en users-service */
    @Column(name = "users_service_id")
    private Long usersServiceId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bloqueado = false;

    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private Integer intentosFallidos = 0;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private Boolean mustChangePassword = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── helpers ──────────────────────────────────────────────────────────

    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 5) {
            this.bloqueado = true;
        }
    }

    public void resetearIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueado = false;
    }
}

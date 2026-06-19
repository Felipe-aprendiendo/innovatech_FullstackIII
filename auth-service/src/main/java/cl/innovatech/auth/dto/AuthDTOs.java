package cl.innovatech.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class AuthDTOs {

    // ─── Requests ─────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo"
        )
        private String password;

        /** ID del usuario ya creado en users-service */
        private Long usersServiceId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefreshTokenRequest {

        @NotBlank(message = "El refresh token es obligatorio")
        private String refreshToken;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangePasswordRequest {

        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo"
        )
        private String newPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SyncCredentialsRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        private String email;

        @Pattern(
            regexp = "^$|^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo"
        )
        private String password;

        @NotNull(message = "El estado activo es obligatorio")
        private Boolean active;
    }

    // ─── Responses ────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long   expiresIn;           // segundos hasta expiración del access token
        private Long   userId;              // ID en users-service
        private String email;
        private List<String> permissions;   // permisos incluidos en el JWT
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthUserResponse {
        private Long   id;
        private String email;
        private Long   usersServiceId;
        private Boolean activo;
        private Boolean bloqueado;
        private LocalDateTime ultimoLogin;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ValidateTokenResponse {
        private boolean valid;
        private String  email;
        private Long    usersServiceId;
        private List<String> permissions;
    }
}

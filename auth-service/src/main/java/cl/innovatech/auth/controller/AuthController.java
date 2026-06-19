package cl.innovatech.auth.controller;

import cl.innovatech.auth.dto.AuthDTOs.AuthUserResponse;
import cl.innovatech.auth.dto.AuthDTOs.ChangePasswordRequest;
import cl.innovatech.auth.dto.AuthDTOs.LoginRequest;
import cl.innovatech.auth.dto.AuthDTOs.RefreshTokenRequest;
import cl.innovatech.auth.dto.AuthDTOs.RegisterRequest;
import cl.innovatech.auth.dto.AuthDTOs.SyncCredentialsRequest;
import cl.innovatech.auth.dto.AuthDTOs.TokenResponse;
import cl.innovatech.auth.dto.AuthDTOs.ValidateTokenResponse;
import cl.innovatech.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Endpoints de login, registro, refresh y logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Crear credenciales para un usuario de users-service")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Credenciales creadas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthUserResponse.class),
                            examples = @ExampleObject(
                                    name = "registroExitoso",
                                    value = """
                                            {
                                              "id": 2,
                                              "email": "codex.verifier@innovatech.cl",
                                              "usersServiceId": 3,
                                              "activo": true,
                                              "bloqueado": false,
                                              "ultimoLogin": null,
                                              "createdAt": "2026-06-19T08:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<AuthUserResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de credenciales que seran vinculadas a un usuario existente en users-service",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "registro",
                                    value = """
                                            {
                                              "email": "codex.verifier@innovatech.cl",
                                              "password": "Codex2026!",
                                              "usersServiceId": 3
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registrar(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login con email y contrasena y retorno de access plus refresh token")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(
                                    name = "loginExitoso",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.access",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh",
                                              "tokenType": "Bearer",
                                              "expiresIn": 900,
                                              "userId": 1,
                                              "email": "admin@innovatech.cl",
                                              "permissions": ["ROLE_ADMIN"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o inactiva")
    })
    public ResponseEntity<TokenResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email y contrasena del usuario",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "loginAdmin",
                                    value = """
                                            {
                                              "email": "admin@innovatech.cl",
                                              "password": "Admin2024!"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.login(req, httpReq));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Obtener nuevo access token usando el refresh token")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(
                                    name = "refreshExitoso",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.newAccess",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.newRefresh",
                                              "tokenType": "Bearer",
                                              "expiresIn": 900,
                                              "userId": 1,
                                              "email": "admin@innovatech.cl",
                                              "permissions": ["ROLE_ADMIN"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token invalido o expirado")
    })
    public ResponseEntity<TokenResponse> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token emitido previamente en login",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "refresh",
                                    value = """
                                            {
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RefreshTokenRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.refresh(req, httpReq));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revocar refresh token y cerrar sesion")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Sesion cerrada y refresh token revocado")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token a revocar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "logout",
                                    value = """
                                            {
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RefreshTokenRequest req) {
        authService.logout(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contrasena del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Contrasena actualizada")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Contrasena actual y nueva contrasena segura",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(
                                    name = "changePassword",
                                    value = """
                                            {
                                              "currentPassword": "Admin2024!",
                                              "newPassword": "Admin2026!"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ChangePasswordRequest req) {
        authService.cambiarPassword(userDetails.getUsername(), req);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/users/{usersServiceId}")
    @Operation(summary = "Sincronizar credenciales desde users-service")
    public ResponseEntity<AuthUserResponse> syncCredentials(
            @Parameter(description = "ID del usuario en users-service", example = "3")
            @PathVariable("usersServiceId") Long usersServiceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email, password y estado activo a sincronizar en auth-service",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SyncCredentialsRequest.class),
                            examples = @ExampleObject(
                                    name = "syncCredentials",
                                    value = """
                                            {
                                              "email": "codex.verifier@innovatech.cl",
                                              "password": "Codex2026!",
                                              "active": true
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody SyncCredentialsRequest req) {
        return ResponseEntity.ok(authService.syncCredentials(usersServiceId, req));
    }

    @DeleteMapping("/internal/users/{usersServiceId}")
    @Operation(summary = "Eliminar credenciales vinculadas a un usuario de users-service")
    public ResponseEntity<Void> deleteCredentials(
            @Parameter(description = "ID del usuario en users-service", example = "3")
            @PathVariable("usersServiceId") Long usersServiceId) {
        authService.deleteCredentialsByUsersServiceId(usersServiceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar access token usado por el gateway y otros servicios")
    @ApiResponse(
            responseCode = "200",
            description = "Token validado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidateTokenResponse.class),
                    examples = @ExampleObject(
                            name = "tokenValido",
                            value = """
                                    {
                                      "valid": true,
                                      "email": "admin@innovatech.cl",
                                      "usersServiceId": 1,
                                      "permissions": ["ROLE_ADMIN"]
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ValidateTokenResponse> validateToken(
            @Parameter(
                    description = "Header Authorization con Bearer token",
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9.access"
            )
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @GetMapping("/users/{id}/exists")
    @Operation(summary = "Verificar si existe un authUser por ID para users-service")
    public ResponseEntity<Boolean> existsById(
            @Parameter(description = "ID del authUser", example = "1")
            @PathVariable String id) {
        return ResponseEntity.ok(authService.existsById(id));
    }

    @GetMapping("/test-hash")
    @Operation(summary = "Generar un hash de prueba para validaciones internas")
    public ResponseEntity<String> testHash() {
        String hash = authService.generarHashTest("password");
        return ResponseEntity.ok(hash);
    }
}

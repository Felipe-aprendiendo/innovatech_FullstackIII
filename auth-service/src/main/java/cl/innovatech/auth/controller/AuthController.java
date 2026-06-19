package cl.innovatech.auth.controller;

import cl.innovatech.auth.dto.AuthDTOs.*;
import cl.innovatech.auth.service.AuthService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login, registro, refresh y logout")
public class AuthController {

    private final AuthService authService;

    // ─── POST /auth/register ──────────────────────────────────────────────
    @PostMapping("/register")
    @Operation(summary = "Crear credenciales para un usuario de users-service")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Credenciales creadas"),
        @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<AuthUserResponse> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.registrar(req));
    }

    // ─── POST /auth/login ─────────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(summary = "Login con email y contraseña — retorna access + refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
        @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o inactiva")
    })
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.login(req, httpReq));
    }

    // ─── POST /auth/refresh ───────────────────────────────────────────────
    @PostMapping("/refresh")
    @Operation(summary = "Obtener nuevo access token usando el refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens renovados"),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(authService.refresh(req, httpReq));
    }

    // ─── POST /auth/logout ────────────────────────────────────────────────
    @PostMapping("/logout")
    @Operation(summary = "Revocar refresh token y cerrar sesión")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest req) {
        authService.logout(req);
        return ResponseEntity.noContent().build();
    }

    // ─── POST /auth/change-password ───────────────────────────────────────
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Contraseña actualizada")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest req) {
        authService.cambiarPassword(userDetails.getUsername(), req);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /auth/validate ───────────────────────────────────────────────
    @PutMapping("/internal/users/{usersServiceId}")
    @Operation(summary = "Sincronizar credenciales desde users-service")
    public ResponseEntity<AuthUserResponse> syncCredentials(
            @PathVariable("usersServiceId") Long usersServiceId,
            @Valid @RequestBody SyncCredentialsRequest req) {
        return ResponseEntity.ok(authService.syncCredentials(usersServiceId, req));
    }

    @DeleteMapping("/internal/users/{usersServiceId}")
    @Operation(summary = "Eliminar credenciales vinculadas a un usuario de users-service")
    public ResponseEntity<Void> deleteCredentials(@PathVariable("usersServiceId") Long usersServiceId) {
        authService.deleteCredentialsByUsersServiceId(usersServiceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar access token (usado por el Gateway y otros servicios)")
    public ResponseEntity<ValidateTokenResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ")
            ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(authService.validateToken(token));
    }

    // ─── GET /auth/users/{id}/exists ──────────────────────────────────────
    @GetMapping("/users/{id}/exists")
    @Operation(summary = "Verificar si existe un authUser por ID (para users-service)")
    public ResponseEntity<Boolean> existsById(@PathVariable String id) {
        return ResponseEntity.ok(authService.existsById(id));
    }

    @GetMapping("/test-hash")
    public ResponseEntity<String> testHash() {
        String hash = authService.generarHashTest("password");
        return ResponseEntity.ok(hash);
}
}

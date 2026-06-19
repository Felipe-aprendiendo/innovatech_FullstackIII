package cl.innovatech.users.controller;

import cl.innovatech.users.dto.UserRequestDTO;
import cl.innovatech.users.dto.UserResponseDTO;
import cl.innovatech.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Administracion de usuarios, roles asignados y estado habilitado")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Listado de usuarios",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class),
                examples = @ExampleObject(
                    name = "usuarios",
                    value = """
                        [
                          {
                            "id": 1,
                            "name": "Codex",
                            "lastName": "Admin",
                            "email": "admin@innovatech.cl",
                            "enabled": true,
                            "roles": [
                              {
                                "id": 1,
                                "name": "ROLE_ADMIN",
                                "description": "Administrador global",
                                "permissions": [
                                  {
                                    "id": 1,
                                    "name": "PROJECT_READ",
                                    "description": "Puede visualizar proyectos"
                                  }
                                ]
                              }
                            ],
                            "createdAt": "2026-06-19T08:10:00",
                            "updatedAt": "2026-06-19T08:10:00"
                          }
                        ]
                        """
                )
            )
        )
    })
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDTO> findById(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del usuario y roles a sincronizar con auth-service",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserRequestDTO.class),
            examples = @ExampleObject(
                name = "userRequest",
                value = """
                    {
                      "name": "Codex",
                      "lastName": "Verifier",
                      "email": "codex.verifier@innovatech.cl",
                      "password": "Codex2026!",
                      "roleIds": [1, 2]
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class),
                examples = @ExampleObject(
                    name = "userCreated",
                    value = """
                        {
                          "id": 2,
                          "name": "Codex",
                          "lastName": "Verifier",
                          "email": "codex.verifier@innovatech.cl",
                          "enabled": true,
                          "roles": [
                            {
                              "id": 2,
                              "name": "ROLE_PROJECT_LEAD",
                              "description": "Lider de proyecto",
                              "permissions": []
                            }
                          ],
                          "createdAt": "2026-06-19T08:35:00",
                          "updatedAt": "2026-06-19T08:35:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos actualizados del usuario",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserRequestDTO.class),
            examples = @ExampleObject(
                name = "userUpdate",
                value = """
                    {
                      "name": "Codex",
                      "lastName": "Admin",
                      "email": "admin@innovatech.cl",
                      "password": "",
                      "roleIds": [1]
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "409", description = "Email duplicado")
    })
    public ResponseEntity<UserResponseDTO> update(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del usuario", example = "2")
            @PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activar o desactivar usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDTO> toggle(
            @Parameter(description = "ID del usuario", example = "2")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.toggleEnabled(id));
    }
}

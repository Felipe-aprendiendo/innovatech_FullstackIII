package cl.innovatech.users.controller;

import cl.innovatech.users.dto.PermissionRequestDTO;
import cl.innovatech.users.dto.PermissionResponseDTO;
import cl.innovatech.users.service.PermissionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permisos", description = "Permisos que se asignan a roles y usuarios")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @Operation(summary = "Listar permisos")
    public ResponseEntity<List<PermissionResponseDTO>> findAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener permiso por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Permiso encontrado"),
        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    })
    public ResponseEntity<PermissionResponseDTO> findById(
            @Parameter(description = "ID del permiso", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear permiso")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Nombre tecnico del permiso y descripcion funcional",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PermissionRequestDTO.class),
            examples = @ExampleObject(
                name = "permissionRequest",
                value = """
                    {
                      "name": "PROJECT_WRITE",
                      "description": "Permite crear y editar proyectos"
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Permiso creado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<PermissionResponseDTO> create(@Valid @RequestBody PermissionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar permiso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Permiso actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    })
    public ResponseEntity<PermissionResponseDTO> update(
            @Parameter(description = "ID del permiso", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequestDTO dto) {
        return ResponseEntity.ok(permissionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar permiso")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Permiso eliminado"),
        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del permiso", example = "3")
            @PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

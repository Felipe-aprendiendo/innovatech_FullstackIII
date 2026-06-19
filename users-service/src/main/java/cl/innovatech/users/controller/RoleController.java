package cl.innovatech.users.controller;

import cl.innovatech.users.dto.RoleRequestDTO;
import cl.innovatech.users.dto.RoleResponseDTO;
import cl.innovatech.users.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Catalogo de roles y permisos asociados")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Listar roles")
    public ResponseEntity<List<RoleResponseDTO>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol encontrado"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    public ResponseEntity<RoleResponseDTO> findById(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear rol")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Nombre, descripcion y permisos que componen el rol",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RoleRequestDTO.class),
            examples = @ExampleObject(
                name = "roleRequest",
                value = """
                    {
                      "name": "ROLE_PROJECT_LEAD",
                      "description": "Lider de proyecto",
                      "permissionIds": [1, 2, 3]
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rol creado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<RoleResponseDTO> create(@Valid @RequestBody RoleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    public ResponseEntity<RoleResponseDTO> update(
            @Parameter(description = "ID del rol", example = "2")
            @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO dto) {
        return ResponseEntity.ok(roleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Rol eliminado"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del rol", example = "2")
            @PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

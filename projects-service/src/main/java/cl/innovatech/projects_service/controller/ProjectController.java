package cl.innovatech.projects_service.controller;

import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.dto.ProjectResponse;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.service.ProjectService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Gestion y seguimiento de proyectos")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Crear proyecto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Proyecto creado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponse.class),
                            examples = @ExampleObject(
                                    name = "projectCreated",
                                    value = """
                                            {
                                              "id": 1,
                                              "nombre": "Portal Interno",
                                              "descripcion": "Proyecto de soporte operativo",
                                              "estado": "PLANIFICADO",
                                              "prioridad": "MEDIA",
                                              "fechaInicio": "2026-06-19",
                                              "fechaFin": "2026-07-30",
                                              "responsableId": 1,
                                              "miembroIds": [1, 2],
                                              "avance": {
                                                "totalTareas": 0,
                                                "tareasPendientes": 0,
                                                "tareasEnProgreso": 0,
                                                "tareasCompletadas": 0,
                                                "tareasCanceladas": 0,
                                                "porcentajeAvance": 0
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o usuarios deshabilitados")
    })
    public ProjectResponse create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos base del proyecto a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectRequest.class),
                            examples = @ExampleObject(
                                    name = "projectRequest",
                                    value = """
                                            {
                                              "nombre": "Portal Interno",
                                              "descripcion": "Proyecto de soporte operativo",
                                              "prioridad": "MEDIA",
                                              "fechaInicio": "2026-06-19",
                                              "fechaFin": "2026-07-30",
                                              "responsableId": 1,
                                              "miembroIds": [1, 2]
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar proyectos con filtros opcionales")
    public List<ProjectResponse> findAll(
            @Parameter(description = "Filtrar por estado", example = "EN_PROGRESO")
            @RequestParam(name = "estado", required = false) Project.Estado estado,
            @Parameter(description = "Filtrar por responsable", example = "1")
            @RequestParam(name = "responsableId", required = false) Long responsableId,
            @Parameter(description = "Filtrar por miembro del proyecto", example = "2")
            @RequestParam(name = "miembroId", required = false) Long miembroId
    ) {
        return projectService.findAll(estado, responsableId, miembroId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto por ID")
    public ProjectResponse findById(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("id") Long id) {
        return projectService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyecto actualizado"),
            @ApiResponse(responseCode = "400", description = "Proyecto cerrado o datos invalidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ProjectResponse update(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del proyecto",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectRequest.class),
                            examples = @ExampleObject(
                                    name = "projectUpdate",
                                    value = """
                                            {
                                              "nombre": "Portal Interno Mejorado",
                                              "descripcion": "Proyecto de soporte operativo actualizado",
                                              "prioridad": "ALTA",
                                              "fechaInicio": "2026-06-19",
                                              "fechaFin": "2026-08-15",
                                              "responsableId": 1,
                                              "miembroIds": [1, 2, 3]
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ProjectRequest request
    ) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proyecto")
    public void delete(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("id") Long id) {
        projectService.delete(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de proyecto")
    public ProjectResponse changeStatus(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("id") Long id,
            @Parameter(description = "Nuevo estado del proyecto", example = "EN_PROGRESO")
            @RequestParam(name = "estado") Project.Estado estado
    ) {
        return projectService.changeStatus(id, estado);
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Cerrar proyecto y completar fecha de cierre si falta")
    public ProjectResponse close(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("id") Long id) {
        return projectService.close(id);
    }
}

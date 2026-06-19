package cl.innovatech.tasks.controller;

import cl.innovatech.tasks.dto.ApiResponse;
import cl.innovatech.tasks.dto.CreateTaskRequest;
import cl.innovatech.tasks.dto.TaskResponse;
import cl.innovatech.tasks.dto.UpdateStatusRequest;
import cl.innovatech.tasks.dto.UpdateTaskRequest;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestion de tareas, estados y asignaciones")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Listar tareas con filtros opcionales")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tareas obtenidas"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos para ver las tareas")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findAll(
            @Parameter(description = "Filtrar por proyecto", example = "1")
            @RequestParam(name = "projectId", required = false) Long projectId,
            @Parameter(description = "Filtrar por estado", example = "PENDIENTE")
            @RequestParam(name = "estado", required = false) Task.EstadoTarea estado,
            @Parameter(description = "Filtrar por responsable", example = "2")
            @RequestParam(name = "responsableId", required = false) Long responsableId,
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_ADMIN")
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskResponse> tasks = taskService.findAll(projectId, estado, responsableId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tareas obtenidas", tasks));
    }

    @Operation(summary = "Crear tarea")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos principales de la tarea a crear",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CreateTaskRequest.class),
            examples = @ExampleObject(
                name = "taskRequest",
                value = """
                    {
                      "titulo": "Diseñar tablero Kanban",
                      "descripcion": "Construir vista inicial para seguimiento del proyecto",
                      "prioridad": "ALTA",
                      "fechaLimite": "2026-06-30",
                      "projectId": 1,
                      "responsableId": 2
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tarea creada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @Valid @RequestBody CreateTaskRequest request,
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_PROJECT_LEAD")
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.create(request, userId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tarea creada exitosamente", task));
    }

    @Operation(summary = "Obtener detalle de tarea con comentarios")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarea encontrada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> findById(
            @Parameter(description = "ID de la tarea", example = "10")
            @PathVariable("id") Long id,
            @Parameter(description = "ID del usuario autenticado", example = "2")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_MEMBER")
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.findById(id, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tarea obtenida", task));
    }

    @Operation(summary = "Actualizar tarea")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Campos editables de la tarea",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UpdateTaskRequest.class),
            examples = @ExampleObject(
                name = "taskUpdate",
                value = """
                    {
                      "titulo": "Diseñar tablero Kanban responsive",
                      "descripcion": "Ajustar interfaz para desktop y mobile",
                      "prioridad": "MEDIA",
                      "fechaLimite": "2026-07-05",
                      "responsableId": 3
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarea actualizada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @Parameter(description = "ID de la tarea", example = "10")
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_PROJECT_LEAD")
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.update(id, request, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tarea actualizada", task));
    }

    @Operation(summary = "Cambiar estado de tarea")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Nuevo estado a aplicar a la tarea",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UpdateStatusRequest.class),
            examples = @ExampleObject(
                name = "statusUpdate",
                value = """
                    {
                      "nuevoEstado": "EN_PROGRESO"
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transicion invalida"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @Parameter(description = "ID de la tarea", example = "10")
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @Parameter(description = "ID del usuario autenticado", example = "2")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_MEMBER")
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.updateStatus(id, request, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", task));
    }

    @Operation(summary = "Eliminar tarea")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarea eliminada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Solo administradores pueden eliminar tareas"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID de la tarea", example = "10")
            @PathVariable("id") Long id,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_ADMIN")
            @RequestHeader("X-User-Role") String userRole) {

        taskService.delete(id, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tarea eliminada", null));
    }

    @Operation(summary = "Listar tareas de un proyecto")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado del proyecto"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findByProject(
            @Parameter(description = "ID del proyecto", example = "1")
            @PathVariable("projectId") Long projectId,
            @Parameter(description = "ID del usuario autenticado", example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_PROJECT_LEAD")
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskResponse> tasks = taskService.findByProject(projectId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tareas del proyecto obtenidas", tasks));
    }
}

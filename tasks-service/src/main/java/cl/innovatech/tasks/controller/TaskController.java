package cl.innovatech.tasks.controller;

import cl.innovatech.tasks.dto.*;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestión de tareas")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Listar tareas con filtros opcionales")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Task.EstadoTarea estado,
            @RequestParam(required = false) Long responsableId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskResponse> tasks = taskService.findAll(projectId, estado, responsableId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tareas obtenidas", tasks));
    }

    @Operation(summary = "Crear tarea")
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.create(request, userId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tarea creada exitosamente", task));
    }

    @Operation(summary = "Obtener detalle de tarea con comentarios")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> findById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.findById(id, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tarea obtenida", task));
    }

    @Operation(summary = "Actualizar tarea")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.update(id, request, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tarea actualizada", task));
    }

    @Operation(summary = "Cambiar estado de tarea")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        TaskResponse task = taskService.updateStatus(id, request, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", task));
    }

    @Operation(summary = "Eliminar tarea (solo ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String userRole) {

        taskService.delete(id, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tarea eliminada", null));
    }

    @Operation(summary = "Listar tareas de un proyecto")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findByProject(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<TaskResponse> tasks = taskService.findByProject(projectId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.ok("Tareas del proyecto obtenidas", tasks));
    }
}

package cl.innovatech.tasks.controller;

import cl.innovatech.tasks.dto.ApiResponse;
import cl.innovatech.tasks.dto.CommentRequest;
import cl.innovatech.tasks.dto.CommentResponse;
import cl.innovatech.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comentarios de tareas")
public class CommentController {

    private final TaskService taskService;

    @Operation(summary = "Agregar comentario a una tarea")
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody CommentRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        CommentResponse comment = taskService.addComment(id, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comentario agregado", comment));
    }
}

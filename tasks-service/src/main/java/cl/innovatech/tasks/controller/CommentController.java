package cl.innovatech.tasks.controller;

import cl.innovatech.tasks.dto.ApiResponse;
import cl.innovatech.tasks.dto.CommentRequest;
import cl.innovatech.tasks.dto.CommentResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comentarios asociados a tareas")
public class CommentController {

    private final TaskService taskService;

    @Operation(summary = "Agregar comentario a una tarea")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Contenido del comentario a registrar",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CommentRequest.class),
            examples = @ExampleObject(
                name = "commentRequest",
                value = """
                    {
                      "contenido": "Se avanzo con el primer prototipo del tablero."
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comentario agregado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @Parameter(description = "ID de la tarea", example = "10")
            @PathVariable("id") Long id,
            @Valid @RequestBody CommentRequest request,
            @Parameter(description = "ID del usuario autenticado", example = "2")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Rol del usuario autenticado", example = "ROLE_MEMBER")
            @RequestHeader("X-User-Role") String userRole) {

        CommentResponse comment = taskService.addComment(id, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comentario agregado", comment));
    }
}

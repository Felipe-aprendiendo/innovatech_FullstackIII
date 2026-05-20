package cl.innovatech.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long taskId;
    private Long userId;
    private String contenido;
    private LocalDateTime createdAt;
}

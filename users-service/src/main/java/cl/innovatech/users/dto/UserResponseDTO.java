package cl.innovatech.users.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private Boolean enabled;
    private Set<RoleResponseDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package cl.innovatech.users.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponseDTO {
    private Long id;
    private String name;
    private String description;
}

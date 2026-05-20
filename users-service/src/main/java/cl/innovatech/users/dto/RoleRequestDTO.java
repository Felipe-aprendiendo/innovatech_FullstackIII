package cl.innovatech.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class RoleRequestDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String name;

    private String description;

    private Set<Long> permissionIds;
}
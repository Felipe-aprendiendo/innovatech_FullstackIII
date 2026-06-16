package cl.innovatech.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequestDTO {

    @NotBlank(message = "El nombre del permiso es obligatorio")
    private String name;

    private String description;
}
package cl.innovatech.users;

import cl.innovatech.users.dto.PermissionRequestDTO;
import cl.innovatech.users.dto.PermissionResponseDTO;
import cl.innovatech.users.entity.Permission;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.repository.PermissionRepository;
import cl.innovatech.users.service.PermissionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService — Pruebas unitarias con Mockito")
class PermissionServiceTest {

    @Mock private PermissionRepository permissionRepository;
    @InjectMocks private PermissionService permissionService;

    private Permission buildPermission(Long id, String name) {
        return Permission.builder().id(id).name(name).description("Descripcion").build();
    }

    private PermissionRequestDTO buildRequest(String name) {
        PermissionRequestDTO dto = new PermissionRequestDTO();
        dto.setName(name);
        dto.setDescription("Descripcion");
        return dto;
    }

    @Nested @DisplayName("findAll()")
    class FindAll {

        @Test @DisplayName("Retorna lista de permisos")
        void retorna_lista_permisos() {
            when(permissionRepository.findAll()).thenReturn(List.of(
                buildPermission(1L, "READ"),
                buildPermission(2L, "WRITE"),
                buildPermission(3L, "DELETE")));

            List<PermissionResponseDTO> result = permissionService.findAll();

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getName()).isEqualTo("READ");
        }

        @Test @DisplayName("Retorna lista vacía si no hay permisos")
        void retorna_lista_vacia() {
            when(permissionRepository.findAll()).thenReturn(List.of());
            assertThat(permissionService.findAll()).isEmpty();
        }
    }

    @Nested @DisplayName("findById()")
    class FindById {

        @Test @DisplayName("Retorna permiso existente por ID")
        void retorna_permiso_existente() {
            when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(buildPermission(1L, "READ")));

            PermissionResponseDTO response = permissionService.findById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("READ");
        }

        @Test @DisplayName("Lanza ResourceNotFoundException si no existe")
        void lanza_excepcion_no_existe() {
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    @Nested @DisplayName("create()")
    class Create {

        @Test @DisplayName("Crea permiso correctamente")
        void crea_permiso_correctamente() {
            PermissionRequestDTO dto = buildRequest("EXECUTE");
            Permission saved = buildPermission(1L, "EXECUTE");
            when(permissionRepository.save(any())).thenReturn(saved);

            PermissionResponseDTO response = permissionService.create(dto);

            assertThat(response.getName()).isEqualTo("EXECUTE");
            verify(permissionRepository, times(1)).save(any());
        }

        @Test @DisplayName("Mapea descripción correctamente")
        void mapea_descripcion() {
            PermissionRequestDTO dto = buildRequest("READ");
            dto.setDescription("Permiso de lectura");
            Permission saved = Permission.builder()
                .id(1L).name("READ").description("Permiso de lectura").build();
            when(permissionRepository.save(any())).thenReturn(saved);

            PermissionResponseDTO response = permissionService.create(dto);

            assertThat(response.getDescription()).isEqualTo("Permiso de lectura");
        }
    }

    @Nested @DisplayName("update()")
    class Update {

        @Test @DisplayName("Actualiza nombre y descripción")
        void actualiza_permiso() {
            Permission perm = buildPermission(1L, "OLD");
            when(permissionRepository.findById(1L)).thenReturn(Optional.of(perm));
            when(permissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PermissionRequestDTO dto = buildRequest("NEW");
            dto.setDescription("Nueva descripcion");

            PermissionResponseDTO response = permissionService.update(1L, dto);

            assertThat(response.getName()).isEqualTo("NEW");
            assertThat(response.getDescription()).isEqualTo("Nueva descripcion");
        }

        @Test @DisplayName("Lanza ResourceNotFoundException si no existe")
        void lanza_excepcion_no_existe() {
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.update(99L, buildRequest("X")))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("delete()")
    class Delete {

        @Test @DisplayName("Elimina permiso existente")
        void elimina_permiso_existente() {
            when(permissionRepository.existsById(1L)).thenReturn(true);
            doNothing().when(permissionRepository).deleteById(1L);

            assertThatCode(() -> permissionService.delete(1L)).doesNotThrowAnyException();
            verify(permissionRepository).deleteById(1L);
        }

        @Test @DisplayName("Lanza ResourceNotFoundException si no existe")
        void lanza_excepcion_no_existe() {
            when(permissionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> permissionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(permissionRepository, never()).deleteById(any());
        }
    }
}
package cl.innovatech.users;

import cl.innovatech.users.dto.*;
import cl.innovatech.users.entity.*;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.repository.*;
import cl.innovatech.users.service.RoleService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService — Pruebas unitarias con Mockito")
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;

    @InjectMocks private RoleService roleService;

    private Role buildRole(Long id, String name) {
        return Role.builder().id(id).name(name)
                .description("Descripcion").permissions(new HashSet<>()).build();
    }

    private RoleRequestDTO buildRequest(String name) {
        RoleRequestDTO dto = new RoleRequestDTO();
        dto.setName(name);
        dto.setDescription("Descripcion");
        return dto;
    }

    @Nested @DisplayName("findAll()")
    class FindAll {

        @Test @DisplayName("Retorna lista de roles")
        void retorna_lista_roles() {
            when(roleRepository.findAll()).thenReturn(List.of(
                buildRole(1L, "ADMIN"), buildRole(2L, "USER")));

            List<RoleResponseDTO> result = roleService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("ADMIN");
        }

        @Test @DisplayName("Retorna lista vacía si no hay roles")
        void retorna_lista_vacia() {
            when(roleRepository.findAll()).thenReturn(List.of());
            assertThat(roleService.findAll()).isEmpty();
        }
    }

    @Nested @DisplayName("findById()")
    class FindById {

        @Test @DisplayName("Retorna rol existente por ID")
        void retorna_rol_existente() {
            when(roleRepository.findById(1L)).thenReturn(Optional.of(buildRole(1L, "ADMIN")));
            RoleResponseDTO response = roleService.findById(1L);
            assertThat(response.getName()).isEqualTo("ADMIN");
        }

        @Test @DisplayName("Lanza ResourceNotFoundException si no existe")
        void lanza_excepcion_no_existe() {
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> roleService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    @Nested @DisplayName("create()")
    class Create {

        @Test @DisplayName("Crea rol correctamente")
        void crea_rol_correctamente() {
            RoleRequestDTO dto = buildRequest("MANAGER");
            Role saved = buildRole(1L, "MANAGER");
            when(roleRepository.save(any())).thenReturn(saved);

            RoleResponseDTO response = roleService.create(dto);

            assertThat(response.getName()).isEqualTo("MANAGER");
            verify(roleRepository, times(1)).save(any());
        }

        @Test @DisplayName("Asigna permisos al crear rol")
        void asigna_permisos_al_crear() {
            RoleRequestDTO dto = buildRequest("ADMIN");
            dto.setPermissionIds(Set.of(1L));

            Permission perm = Permission.builder().id(1L).name("READ").build();
            Role saved = buildRole(1L, "ADMIN");
            saved.setPermissions(Set.of(perm));

            when(permissionRepository.findById(1L)).thenReturn(Optional.of(perm));
            when(roleRepository.save(any())).thenReturn(saved);

            RoleResponseDTO response = roleService.create(dto);

            assertThat(response.getPermissions()).hasSize(1);
        }

        @Test @DisplayName("Lanza excepción si permiso no existe")
        void lanza_excepcion_permiso_no_existe() {
            RoleRequestDTO dto = buildRequest("ADMIN");
            dto.setPermissionIds(Set.of(99L));
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    @Nested @DisplayName("update()")
    class Update {

        @Test @DisplayName("Actualiza nombre y descripción")
        void actualiza_nombre_descripcion() {
            Role role = buildRole(1L, "VIEJO");
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(roleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RoleRequestDTO dto = buildRequest("NUEVO");
            RoleResponseDTO response = roleService.update(1L, dto);

            assertThat(response.getName()).isEqualTo("NUEVO");
        }

        @Test @DisplayName("Lanza ResourceNotFoundException si no existe")
        void lanza_excepcion_no_existe() {
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> roleService.update(99L, buildRequest("X")))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("delete()")
    class Delete {

        @Test @DisplayName("Elimina rol existente")
        void elimina_rol_existente() {
            when(roleRepository.existsById(1L)).thenReturn(true);
            doNothing().when(roleRepository).deleteById(1L);

            assertThatCode(() -> roleService.delete(1L)).doesNotThrowAnyException();
            verify(roleRepository).deleteById(1L);
        }

        @Test @DisplayName("Lanza ResourceNotFoundException si no existe")
        void lanza_excepcion_no_existe() {
            when(roleRepository.existsById(99L)).thenReturn(false);
            assertThatThrownBy(() -> roleService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
            verify(roleRepository, never()).deleteById(any());
        }
    }
}

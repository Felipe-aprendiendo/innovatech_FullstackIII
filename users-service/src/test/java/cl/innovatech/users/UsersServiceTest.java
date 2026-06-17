package cl.innovatech.users;

import cl.innovatech.users.dto.UserRequestDTO;
import cl.innovatech.users.dto.UserResponseDTO;
import cl.innovatech.users.entity.Role;
import cl.innovatech.users.entity.User;
import cl.innovatech.users.exception.EmailAlreadyExistsException;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.repository.RoleRepository;
import cl.innovatech.users.repository.UserRepository;
import cl.innovatech.users.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Pruebas unitarias con Mockito")
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private RoleRepository  roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ── Helpers ────────────────────────────────────────────────────────────

    private User buildUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .lastName("Apellido")
                .email(email)
                .password("hashedPassword")
                .enabled(true)
                .roles(new HashSet<>())
                .build();
    }

    private UserRequestDTO buildRequest(String name, String email, String password) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName(name);
        dto.setLastName("Apellido");
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════
    // findAll
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Retorna lista con todos los usuarios")
        void retorna_lista_completa() {
            List<User> users = List.of(
                buildUser(1L, "Ana",   "ana@innovatech.cl"),
                buildUser(2L, "Luis",  "luis@innovatech.cl"),
                buildUser(3L, "Pedro", "pedro@innovatech.cl")
            );
            when(userRepository.findAll()).thenReturn(users);

            List<UserResponseDTO> result = userService.findAll();

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getName()).isEqualTo("Ana");
            assertThat(result.get(1).getName()).isEqualTo("Luis");
        }

        @Test
        @DisplayName("Retorna lista vacía si no hay usuarios")
        void retorna_lista_vacia() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponseDTO> result = userService.findAll();

            assertThat(result).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // findById
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna usuario existente por ID")
        void retorna_usuario_existente() {
            User user = buildUser(1L, "María", "m@innovatech.cl");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponseDTO response = userService.findById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("María");
            assertThat(response.getEmail()).isEqualTo("m@innovatech.cl");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el ID no existe")
        void lanza_excepcion_id_no_existe() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Mapea correctamente enabled al response")
        void mapea_enabled_correctamente() {
            User user = buildUser(1L, "Carlos", "c@innovatech.cl");
            user.setEnabled(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponseDTO response = userService.findById(1L);

            assertThat(response.getEnabled()).isTrue();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // create
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Crea usuario correctamente con datos válidos")
        void crea_usuario_correctamente() {
            UserRequestDTO dto = buildRequest("Juan", "juan@innovatech.cl", "Password1!");
            User saved = buildUser(1L, "Juan", "juan@innovatech.cl");

            when(userRepository.existsByEmail("juan@innovatech.cl")).thenReturn(false);
            when(passwordEncoder.encode("Password1!")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(saved);

            UserResponseDTO response = userService.create(dto);

            assertThat(response.getName()).isEqualTo("Juan");
            assertThat(response.getEmail()).isEqualTo("juan@innovatech.cl");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Lanza EmailAlreadyExistsException si el email ya existe")
        void lanza_excepcion_email_duplicado() {
            UserRequestDTO dto = buildRequest("Ana", "dup@innovatech.cl", "Password1!");
            when(userRepository.existsByEmail("dup@innovatech.cl")).thenReturn(true);

            assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("dup@innovatech.cl");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Encripta la contraseña antes de guardar")
        void encripta_password() {
            UserRequestDTO dto = buildRequest("Pedro", "p@innovatech.cl", "Password1!");
            User saved = buildUser(1L, "Pedro", "p@innovatech.cl");

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("Password1!")).thenReturn("$2a$10$hashedValue");
            when(userRepository.save(any())).thenReturn(saved);

            userService.create(dto);

            verify(passwordEncoder, times(1)).encode("Password1!");
        }

        @Test
        @DisplayName("Asigna enabled=true por defecto al crear")
        void asigna_enabled_por_defecto() {
            UserRequestDTO dto = buildRequest("Laura", "l@innovatech.cl", "Password1!");
            User saved = buildUser(1L, "Laura", "l@innovatech.cl");

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any())).thenReturn(saved);

            UserResponseDTO response = userService.create(dto);

            assertThat(response.getEnabled()).isTrue();
        }

        @Test
        @DisplayName("Asigna roles cuando se proveen roleIds")
        void asigna_roles_al_crear() {
            UserRequestDTO dto = buildRequest("Omar", "o@innovatech.cl", "Password1!");
            dto.setRoleIds(Set.of(1L));

            Role role = Role.builder().id(1L).name("ADMIN")
                .description("Administrador").permissions(new HashSet<>()).build();
            User saved = buildUser(1L, "Omar", "o@innovatech.cl");
            saved.setRoles(Set.of(role));

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(userRepository.save(any())).thenReturn(saved);

            UserResponseDTO response = userService.create(dto);

            assertThat(response.getRoles()).hasSize(1);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // update
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Actualiza nombre y apellido correctamente")
        void actualiza_nombre_y_apellido() {
            User user = buildUser(1L, "Viejo", "v@innovatech.cl");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserRequestDTO dto = buildRequest("Nuevo", "v@innovatech.cl", null);
            dto.setPassword(null);

            UserResponseDTO response = userService.update(1L, dto);

            assertThat(response.getName()).isEqualTo("Nuevo");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el usuario no existe")
        void lanza_excepcion_usuario_no_existe() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            UserRequestDTO dto = buildRequest("X", "x@innovatech.cl", "Pass1234!");

            assertThatThrownBy(() -> userService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Lanza EmailAlreadyExistsException si el nuevo email ya está en uso")
        void lanza_excepcion_email_en_uso() {
            User user = buildUser(1L, "Luis", "luis@innovatech.cl");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("dup@innovatech.cl")).thenReturn(true);

            UserRequestDTO dto = buildRequest("Luis", "dup@innovatech.cl", "Pass1234!");

            assertThatThrownBy(() -> userService.update(1L, dto))
                .isInstanceOf(EmailAlreadyExistsException.class);
        }

        @Test
        @DisplayName("No encripta password si viene nulo o vacío")
        void no_encripta_si_password_nulo() {
            User user = buildUser(1L, "María", "m@innovatech.cl");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserRequestDTO dto = buildRequest("María", "m@innovatech.cl", null);
            dto.setPassword(null);

            userService.update(1L, dto);

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("Encripta password si viene un valor nuevo")
        void encripta_si_password_nuevo() {
            User user = buildUser(1L, "Carlos", "c@innovatech.cl");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NuevoPass1!")).thenReturn("newHashed");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserRequestDTO dto = buildRequest("Carlos", "c@innovatech.cl", "NuevoPass1!");

            userService.update(1L, dto);

            verify(passwordEncoder, times(1)).encode("NuevoPass1!");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // delete
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Elimina usuario existente sin excepciones")
        void elimina_usuario_existente() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            assertThatCode(() -> userService.delete(1L))
                .doesNotThrowAnyException();

            verify(userRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el ID no existe")
        void lanza_excepcion_id_no_existe() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

            verify(userRepository, never()).deleteById(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // toggleEnabled
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("toggleEnabled()")
    class ToggleEnabled {

        @Test
        @DisplayName("Desactiva usuario activo")
        void desactiva_usuario_activo() {
            User user = buildUser(1L, "Ana", "a@innovatech.cl");
            user.setEnabled(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserResponseDTO response = userService.toggleEnabled(1L);

            assertThat(response.getEnabled()).isFalse();
        }

        @Test
        @DisplayName("Activa usuario inactivo")
        void activa_usuario_inactivo() {
            User user = buildUser(1L, "Luis", "l@innovatech.cl");
            user.setEnabled(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserResponseDTO response = userService.toggleEnabled(1L);

            assertThat(response.getEnabled()).isTrue();
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el usuario no existe")
        void lanza_excepcion_usuario_no_existe() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.toggleEnabled(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

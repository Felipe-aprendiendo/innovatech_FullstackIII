package cl.innovatech.users.service;

import cl.innovatech.users.client.AuthServiceClient;
import cl.innovatech.users.dto.PermissionResponseDTO;
import cl.innovatech.users.dto.RoleResponseDTO;
import cl.innovatech.users.dto.UserRequestDTO;
import cl.innovatech.users.dto.UserResponseDTO;
import cl.innovatech.users.entity.Role;
import cl.innovatech.users.entity.User;
import cl.innovatech.users.exception.EmailAlreadyExistsException;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.repository.RoleRepository;
import cl.innovatech.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthServiceClient authServiceClient;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return toResponse(user);
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya esta registrado: " + dto.getEmail());
        }

        validatePasswordForCreate(dto);
        Set<Role> roles = resolveRoles(dto.getRoleIds());

        User user = User.builder()
                .name(dto.getName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .roles(roles)
                .build();
        user.markCredentialsManagedByAuth();

        User savedUser = userRepository.saveAndFlush(user);
        authServiceClient.syncCredentials(
                savedUser.getId(),
                savedUser.getEmail(),
                dto.getPassword(),
                Boolean.TRUE.equals(savedUser.getEnabled()));
        return toResponse(savedUser);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya esta en uso: " + dto.getEmail());
        }

        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.markCredentialsManagedByAuth();

        if (dto.getRoleIds() != null) {
            user.setRoles(resolveRoles(dto.getRoleIds()));
        }

        User savedUser = userRepository.saveAndFlush(user);
        authServiceClient.syncCredentials(
                savedUser.getId(),
                savedUser.getEmail(),
                dto.getPassword(),
                Boolean.TRUE.equals(savedUser.getEnabled()));
        return toResponse(savedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        authServiceClient.deleteCredentials(id);
        userRepository.delete(user);
    }

    @Transactional
    public UserResponseDTO toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        user.setEnabled(!user.getEnabled());
        user.markCredentialsManagedByAuth();

        User savedUser = userRepository.saveAndFlush(user);
        authServiceClient.syncCredentials(
                savedUser.getId(),
                savedUser.getEmail(),
                null,
                Boolean.TRUE.equals(savedUser.getEnabled()));
        return toResponse(savedUser);
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        return roleIds.stream()
                .map(rid -> roleRepository.findById(rid)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + rid)))
                .collect(Collectors.toSet());
    }

    private void validatePasswordForCreate(UserRequestDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria al crear un usuario");
        }
    }

    public UserResponseDTO toResponse(User u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .enabled(u.getEnabled())
                .roles(u.getRoles().stream()
                        .map(r -> RoleResponseDTO.builder()
                                .id(r.getId())
                                .name(r.getName())
                                .description(r.getDescription())
                                .permissions(r.getPermissions().stream()
                                        .map(p -> PermissionResponseDTO.builder()
                                                .id(p.getId())
                                                .name(p.getName())
                                                .description(p.getDescription())
                                                .build())
                                        .collect(Collectors.toSet()))
                                .build())
                        .collect(Collectors.toSet()))
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}

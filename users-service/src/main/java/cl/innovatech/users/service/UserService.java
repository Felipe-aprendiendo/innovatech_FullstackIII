package cl.innovatech.users.service;

import cl.innovatech.users.dto.*;
import cl.innovatech.users.entity.*;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.exception.EmailAlreadyExistsException;
import cl.innovatech.users.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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
            throw new EmailAlreadyExistsException("El email ya está registrado: " + dto.getEmail());
        }

        Set<Role> roles = resolveRoles(dto.getRoleIds());

        User user = User.builder()
                .name(dto.getName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(roles)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso: " + dto.getEmail());
        }

        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoleIds() != null) {
            user.setRoles(resolveRoles(dto.getRoleIds()));
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponseDTO toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        user.setEnabled(!user.getEnabled());
        return toResponse(userRepository.save(user));
    }

    // --- Mappers ---

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return new HashSet<>();
        return roleIds.stream()
                .map(rid -> roleRepository.findById(rid)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + rid)))
                .collect(Collectors.toSet());
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
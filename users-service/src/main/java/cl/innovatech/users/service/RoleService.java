package cl.innovatech.users.service;

import cl.innovatech.users.dto.*;
import cl.innovatech.users.entity.*;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponseDTO> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponseDTO findById(Long id) {
        return toResponse(roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id)));
    }

    @Transactional
    public RoleResponseDTO create(RoleRequestDTO dto) {
        Role role = Role.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .permissions(resolvePermissions(dto.getPermissionIds()))
                .build();
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponseDTO update(Long id, RoleRequestDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        if (dto.getPermissionIds() != null) {
            role.setPermissions(resolvePermissions(dto.getPermissionIds()));
        }
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol no encontrado con id: " + id);
        }
        roleRepository.deleteById(id);
    }

    private Set<Permission> resolvePermissions(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return ids.stream()
                .map(pid -> permissionRepository.findById(pid)
                        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + pid)))
                .collect(Collectors.toSet());
    }

    public RoleResponseDTO toResponse(Role r) {
        return RoleResponseDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .permissions(r.getPermissions().stream()
                        .map(p -> PermissionResponseDTO.builder()
                                .id(p.getId()).name(p.getName()).description(p.getDescription()).build())
                        .collect(Collectors.toSet()))
                .build();
    }
}

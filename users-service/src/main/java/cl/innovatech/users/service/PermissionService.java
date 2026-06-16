package cl.innovatech.users.service;

import cl.innovatech.users.dto.PermissionRequestDTO;
import cl.innovatech.users.dto.PermissionResponseDTO;
import cl.innovatech.users.entity.Permission;
import cl.innovatech.users.exception.ResourceNotFoundException;
import cl.innovatech.users.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    // ✅ Sin "= null" — Lombok inyecta esto automáticamente via constructor
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> findAll() {
        return permissionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionResponseDTO findById(Long id) {
        return toResponse(permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permiso no encontrado con id: " + id)));
    }

    @Transactional
    public PermissionResponseDTO create(PermissionRequestDTO dto) {
        Permission p = Permission.builder()
                .name(dto.getName())
                .description(dto.getDescription()) // ✅ no olvides mapear description
                .build();
        return toResponse(permissionRepository.save(p));
    }

    @Transactional
    public PermissionResponseDTO update(Long id, PermissionRequestDTO dto) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permiso no encontrado con id: " + id));
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        return toResponse(permissionRepository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Permiso no encontrado con id: " + id);
        }
        permissionRepository.deleteById(id);
    }

    // ✅ Usando getters directos de Lombok (@Data), sin reflexión
    private PermissionResponseDTO toResponse(Permission p) {
        return PermissionResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build();
    }
}
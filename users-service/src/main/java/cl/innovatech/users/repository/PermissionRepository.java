// PermissionRepository.java
package cl.innovatech.users.repository;

import cl.innovatech.users.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);
}
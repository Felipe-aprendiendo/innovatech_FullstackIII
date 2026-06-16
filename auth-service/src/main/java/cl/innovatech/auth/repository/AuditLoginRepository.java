package cl.innovatech.auth.repository;

import cl.innovatech.auth.entity.AuditLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLoginRepository extends JpaRepository<AuditLogin, Long> {
    Page<AuditLogin> findByEmailOrderByCreatedAtDesc(String email, Pageable pageable);
}

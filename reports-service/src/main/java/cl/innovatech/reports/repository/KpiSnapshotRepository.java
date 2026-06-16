package cl.innovatech.reports.repository;

import cl.innovatech.reports.entity.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {

    Optional<KpiSnapshot> findTopByProjectIdOrderByCalculadoAtDesc(Long projectId);

    List<KpiSnapshot> findAllByOrderByCalculadoAtDesc();
}

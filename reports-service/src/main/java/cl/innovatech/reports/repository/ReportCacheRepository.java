package cl.innovatech.reports.repository;

import cl.innovatech.reports.entity.ReportCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportCacheRepository extends JpaRepository<ReportCache, Long> {

    List<ReportCache> findByGeneradoPorOrderByCreatedAtDesc(Long generadoPor);

    List<ReportCache> findByTipoOrderByCreatedAtDesc(String tipo);
}

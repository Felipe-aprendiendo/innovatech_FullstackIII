package cl.innovatech.reports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column(name = "filtros", columnDefinition = "JSON")
    private String filtros;

    @Column(name = "resultado", nullable = false, columnDefinition = "JSON")
    private String resultado;

    @Column(name = "generado_por", nullable = false)
    private Long generadoPor;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

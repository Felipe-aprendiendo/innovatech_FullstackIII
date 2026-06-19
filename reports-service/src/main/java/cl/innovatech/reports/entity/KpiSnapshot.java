package cl.innovatech.reports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "total_tareas", nullable = false)
    private Integer totalTareas;

    @Column(name = "tareas_completadas", nullable = false)
    private Integer tareasCompletadas;

    @Column(name = "tareas_en_progreso", nullable = false)
    private Integer tareasEnProgreso;

    @Column(name = "tareas_pendientes", nullable = false)
    private Integer tareasPendientes;

    @Column(name = "porcentaje_avance", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvance;

    @Column(name = "calculado_at")
    private LocalDateTime calculadoAt;

    @PrePersist
    protected void onCreate() {
        calculadoAt = LocalDateTime.now();
    }
}

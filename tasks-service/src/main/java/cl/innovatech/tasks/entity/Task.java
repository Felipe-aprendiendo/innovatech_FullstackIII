package cl.innovatech.tasks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTarea estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadTarea prioridad;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "responsable_id", nullable = false)
    private Long responsableId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estado == null) estado = EstadoTarea.PENDIENTE;
        if (prioridad == null) prioridad = PrioridadTarea.MEDIA;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EstadoTarea {
        PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA
    }

    public enum PrioridadTarea {
        ALTA, MEDIA, BAJA
    }
}

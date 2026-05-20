package cl.innovatech.projects_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.PLANIFICADO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Prioridad prioridad = Prioridad.MEDIA;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Column(nullable = false)
    private Long responsableId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Estado {
        PLANIFICADO,
        EN_PROGRESO,
        COMPLETADO,
        CERRADO
    }

    public enum Prioridad {
        ALTA,
        MEDIA,
        BAJA
    }
}
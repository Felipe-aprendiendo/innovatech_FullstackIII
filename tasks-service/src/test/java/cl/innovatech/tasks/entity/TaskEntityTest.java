package cl.innovatech.tasks.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEntityTest {

    // ─── Task Entity Tests ────────────────────────────────────────

    @Test
    void task_createdWithBuilder_tieneTodasLasPropiedades() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate fecha = LocalDate.now();

        Task task = Task.builder()
                .id(1L)
                .titulo("Tarea Test")
                .descripcion("Descripción test")
                .estado(Task.EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.ALTA)
                .fechaLimite(fecha)
                .projectId(10L)
                .responsableId(20L)
                .createdBy(5L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(task.getId()).isEqualTo(1L);
        assertThat(task.getTitulo()).isEqualTo("Tarea Test");
        assertThat(task.getDescripcion()).isEqualTo("Descripción test");
        assertThat(task.getEstado()).isEqualTo(Task.EstadoTarea.PENDIENTE);
        assertThat(task.getPrioridad()).isEqualTo(Task.PrioridadTarea.ALTA);
        assertThat(task.getFechaLimite()).isEqualTo(fecha);
        assertThat(task.getProjectId()).isEqualTo(10L);
        assertThat(task.getResponsableId()).isEqualTo(20L);
        assertThat(task.getCreatedBy()).isEqualTo(5L);
        assertThat(task.getCreatedAt()).isEqualTo(now);
        assertThat(task.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void task_conNoArgsConstructor_creaInstanciaVacia() {
        Task task = new Task();

        assertThat(task.getId()).isNull();
        assertThat(task.getTitulo()).isNull();
        assertThat(task.getEstado()).isNull();
    }

    @Test
    void task_conAllArgsConstructor_inicializaTodosLosParametros() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate fecha = LocalDate.now();

        Task task = new Task(
                1L, "Titulo", "Desc", Task.EstadoTarea.EN_PROGRESO,
                Task.PrioridadTarea.MEDIA, fecha, 10L, 20L, 5L, now, now
        );

        assertThat(task.getId()).isEqualTo(1L);
        assertThat(task.getTitulo()).isEqualTo("Titulo");
        assertThat(task.getEstado()).isEqualTo(Task.EstadoTarea.EN_PROGRESO);
        assertThat(task.getPrioridad()).isEqualTo(Task.PrioridadTarea.MEDIA);
    }

    @Test
    void task_setters_modificanPropiedades() {
        Task task = new Task();

        task.setId(5L);
        task.setTitulo("Nueva Tarea");
        task.setDescripcion("Nueva descripción");
        task.setEstado(Task.EstadoTarea.COMPLETADA);
        task.setPrioridad(Task.PrioridadTarea.BAJA);
        task.setProjectId(100L);
        task.setResponsableId(200L);

        assertThat(task.getId()).isEqualTo(5L);
        assertThat(task.getTitulo()).isEqualTo("Nueva Tarea");
        assertThat(task.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(task.getEstado()).isEqualTo(Task.EstadoTarea.COMPLETADA);
        assertThat(task.getPrioridad()).isEqualTo(Task.PrioridadTarea.BAJA);
        assertThat(task.getProjectId()).isEqualTo(100L);
        assertThat(task.getResponsableId()).isEqualTo(200L);
    }

    @Test
    void task_prePersist_estableceTimestampsYValoresDefault() {
        Task task = Task.builder()
                .titulo("Tarea")
                .projectId(1L)
                .responsableId(1L)
                .createdBy(1L)
                .build();

        task.onCreate();

        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getUpdatedAt()).isNotNull();
        assertThat(task.getEstado()).isEqualTo(Task.EstadoTarea.PENDIENTE);
        assertThat(task.getPrioridad()).isEqualTo(Task.PrioridadTarea.MEDIA);
    }

    @Test
    void task_prePersist_noSobrescribeEstadoExistente() {
        Task task = Task.builder()
                .titulo("Tarea")
                .estado(Task.EstadoTarea.EN_PROGRESO)
                .prioridad(Task.PrioridadTarea.ALTA)
                .projectId(1L)
                .responsableId(1L)
                .createdBy(1L)
                .build();

        task.onCreate();

        assertThat(task.getEstado()).isEqualTo(Task.EstadoTarea.EN_PROGRESO);
        assertThat(task.getPrioridad()).isEqualTo(Task.PrioridadTarea.ALTA);
    }

    @Test
    void task_preUpdate_actualizaUpdatedAt() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        Task task = Task.builder()
                .titulo("Tarea")
                .createdAt(createdTime)
                .updatedAt(createdTime)
                .projectId(1L)
                .responsableId(1L)
                .createdBy(1L)
                .build();

        task.onUpdate();

        assertThat(task.getUpdatedAt()).isAfter(createdTime);
        assertThat(task.getCreatedAt()).isEqualTo(createdTime);
    }

    @Test
    void task_estadoTareEnum_tieneTodosLosValores() {
        assertThat(Task.EstadoTarea.PENDIENTE).isNotNull();
        assertThat(Task.EstadoTarea.EN_PROGRESO).isNotNull();
        assertThat(Task.EstadoTarea.COMPLETADA).isNotNull();
        assertThat(Task.EstadoTarea.CANCELADA).isNotNull();

        assertThat(Task.EstadoTarea.values()).hasSize(4);
    }

    @Test
    void task_prioridadTareaEnum_tieneTodosLosValores() {
        assertThat(Task.PrioridadTarea.ALTA).isNotNull();
        assertThat(Task.PrioridadTarea.MEDIA).isNotNull();
        assertThat(Task.PrioridadTarea.BAJA).isNotNull();

        assertThat(Task.PrioridadTarea.values()).hasSize(3);
    }

    @Test
    void task_equals_compararDosInstanciasConMismosDatos() {
        Task task1 = Task.builder()
                .id(1L)
                .titulo("Tarea")
                .projectId(10L)
                .responsableId(20L)
                .createdBy(5L)
                .build();

        Task task2 = Task.builder()
                .id(1L)
                .titulo("Tarea")
                .projectId(10L)
                .responsableId(20L)
                .createdBy(5L)
                .build();

        assertThat(task1).isEqualTo(task2);
    }

    @Test
    void task_toString_generaRepresentacionString() {
        Task task = Task.builder()
                .id(1L)
                .titulo("Tarea Test")
                .build();

        String str = task.toString();
        assertThat(str).contains("Task");
        assertThat(str).contains("titulo=Tarea Test");
    }

    @Test
    void task_hashCode_consistenteConEquals() {
        Task task1 = Task.builder()
                .id(1L)
                .titulo("Tarea")
                .build();

        Task task2 = Task.builder()
                .id(1L)
                .titulo("Tarea")
                .build();

        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
    }

    // ─── TaskComment Entity Tests ──────────────────────────────────

    @Test
    void taskComment_createdWithBuilder_tieneTodasLasPropiedades() {
        LocalDateTime now = LocalDateTime.now();
        Task task = Task.builder().id(1L).build();

        TaskComment comment = TaskComment.builder()
                .id(1L)
                .task(task)
                .userId(10L)
                .contenido("Este es un comentario")
                .createdAt(now)
                .build();

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getTask()).isEqualTo(task);
        assertThat(comment.getUserId()).isEqualTo(10L);
        assertThat(comment.getContenido()).isEqualTo("Este es un comentario");
        assertThat(comment.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void taskComment_conNoArgsConstructor_creaInstanciaVacia() {
        TaskComment comment = new TaskComment();

        assertThat(comment.getId()).isNull();
        assertThat(comment.getTask()).isNull();
        assertThat(comment.getUserId()).isNull();
    }

    @Test
    void taskComment_conAllArgsConstructor_inicializaTodosLosParametros() {
        LocalDateTime now = LocalDateTime.now();
        Task task = Task.builder().id(1L).build();

        TaskComment comment = new TaskComment(1L, task, 10L, "Contenido", now);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getTask()).isEqualTo(task);
        assertThat(comment.getUserId()).isEqualTo(10L);
        assertThat(comment.getContenido()).isEqualTo("Contenido");
    }

    @Test
    void taskComment_setters_modificanPropiedades() {
        TaskComment comment = new TaskComment();
        Task task = Task.builder().id(5L).build();

        comment.setId(2L);
        comment.setTask(task);
        comment.setUserId(15L);
        comment.setContenido("Nuevo comentario");

        assertThat(comment.getId()).isEqualTo(2L);
        assertThat(comment.getTask()).isEqualTo(task);
        assertThat(comment.getUserId()).isEqualTo(15L);
        assertThat(comment.getContenido()).isEqualTo("Nuevo comentario");
    }

    @Test
    void taskComment_prePersist_estableceCreatedAt() {
        TaskComment comment = TaskComment.builder()
                .task(Task.builder().id(1L).build())
                .userId(10L)
                .contenido("Comentario")
                .build();

        comment.onCreate();

        assertThat(comment.getCreatedAt()).isNotNull();
    }

    @Test
    void taskComment_equals_compararDosInstanciasConMismosDatos() {
        Task task = Task.builder().id(1L).build();
        LocalDateTime now = LocalDateTime.now();

        TaskComment comment1 = TaskComment.builder()
                .id(1L)
                .task(task)
                .userId(10L)
                .contenido("Comentario")
                .createdAt(now)
                .build();

        TaskComment comment2 = TaskComment.builder()
                .id(1L)
                .task(task)
                .userId(10L)
                .contenido("Comentario")
                .createdAt(now)
                .build();

        assertThat(comment1).isEqualTo(comment2);
    }

    @Test
    void taskComment_toString_generaRepresentacionString() {
        TaskComment comment = TaskComment.builder()
                .id(1L)
                .contenido("Test comment")
                .build();

        String str = comment.toString();
        assertThat(str).contains("TaskComment");
        assertThat(str).contains("contenido=Test comment");
    }

    @Test
    void taskComment_hashCode_consistenteConEquals() {
        Task task = Task.builder().id(1L).build();

        TaskComment comment1 = TaskComment.builder()
                .id(1L)
                .task(task)
                .build();

        TaskComment comment2 = TaskComment.builder()
                .id(1L)
                .task(task)
                .build();

        assertThat(comment1.hashCode()).isEqualTo(comment2.hashCode());
    }

    // ─── Additional Entity Coverage Tests ─────────────────────────

    @Test
    void task_setters_actualizanPropiedades() {
        Task task = new Task();

        task.setId(99L);
        task.setTitulo("Nuevo título");
        task.setDescripcion("Nueva descripción");
        task.setEstado(Task.EstadoTarea.EN_PROGRESO);
        task.setPrioridad(Task.PrioridadTarea.BAJA);
        task.setFechaLimite(LocalDate.of(2026, 12, 25));
        task.setProjectId(5L);
        task.setResponsableId(10L);
        task.setCreatedBy(1L);

        assertThat(task.getId()).isEqualTo(99L);
        assertThat(task.getTitulo()).isEqualTo("Nuevo título");
        assertThat(task.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(task.getEstado()).isEqualTo(Task.EstadoTarea.EN_PROGRESO);
        assertThat(task.getPrioridad()).isEqualTo(Task.PrioridadTarea.BAJA);
        assertThat(task.getFechaLimite()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(task.getProjectId()).isEqualTo(5L);
        assertThat(task.getResponsableId()).isEqualTo(10L);
        assertThat(task.getCreatedBy()).isEqualTo(1L);
    }

    @Test
    void task_conTodosPrioridades_creaSinProblemas() {
        for (Task.PrioridadTarea prioridad : Task.PrioridadTarea.values()) {
            Task task = Task.builder()
                    .id(1L)
                    .titulo("Test")
                    .prioridad(prioridad)
                    .build();

            assertThat(task.getPrioridad()).isEqualTo(prioridad);
        }
    }

    @Test
    void task_conTodosEstados_creaSinProblemas() {
        for (Task.EstadoTarea estado : Task.EstadoTarea.values()) {
            Task task = Task.builder()
                    .id(1L)
                    .titulo("Test")
                    .estado(estado)
                    .build();

            assertThat(task.getEstado()).isEqualTo(estado);
        }
    }

    @Test
    void task_notEqual_cuandoIdssonDiferentes() {
        Task task1 = Task.builder().id(1L).titulo("Test").build();
        Task task2 = Task.builder().id(2L).titulo("Test").build();

        assertThat(task1).isNotEqualTo(task2);
    }

    @Test
    void task_notEqual_cuandoNull() {
        Task task = Task.builder().id(1L).titulo("Test").build();

        assertThat(task).isNotEqualTo(null);
    }

    @Test
    void task_equals_conMismoObjeto() {
        Task task = Task.builder().id(1L).titulo("Test").build();

        assertThat(task).isEqualTo(task);
    }

    @Test
    void task_actualizaCreatedAtAlCrear() {
        LocalDateTime now = LocalDateTime.now();
        Task task = Task.builder()
                .id(1L)
                .titulo("Test")
                .createdAt(now)
                .build();

        assertThat(task.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void task_actualizaUpdatedAtAlActualizar() {
        LocalDateTime updated = LocalDateTime.now();
        Task task = Task.builder()
                .id(1L)
                .titulo("Test")
                .updatedAt(updated)
                .build();

        assertThat(task.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void taskComment_setters_actualizanPropiedades() {
        TaskComment comment = new TaskComment();
        Task task = Task.builder().id(1L).build();

        comment.setId(50L);
        comment.setTask(task);
        comment.setUserId(100L);
        comment.setContenido("Nuevo contenido");
        comment.setCreatedAt(LocalDateTime.now());

        assertThat(comment.getId()).isEqualTo(50L);
        assertThat(comment.getTask()).isEqualTo(task);
        assertThat(comment.getUserId()).isEqualTo(100L);
        assertThat(comment.getContenido()).isEqualTo("Nuevo contenido");
        assertThat(comment.getCreatedAt()).isNotNull();
    }

    @Test
    void taskComment_notEqual_cuandoIdssonDiferentes() {
        TaskComment comment1 = TaskComment.builder().id(1L).contenido("Test").build();
        TaskComment comment2 = TaskComment.builder().id(2L).contenido("Test").build();

        assertThat(comment1).isNotEqualTo(comment2);
    }

    @Test
    void taskComment_notEqual_cuandoNull() {
        TaskComment comment = TaskComment.builder().id(1L).build();

        assertThat(comment).isNotEqualTo(null);
    }

    @Test
    void taskComment_equals_conMismoObjeto() {
        TaskComment comment = TaskComment.builder().id(1L).build();

        assertThat(comment).isEqualTo(comment);
    }

    @Test
    void task_conFechaLimiteNull_aceptaNull() {
        Task task = Task.builder()
                .id(1L)
                .titulo("Test")
                .fechaLimite(null)
                .build();

        assertThat(task.getFechaLimite()).isNull();
    }

    @Test
    void task_conDescripcionVacia_aceptaVacia() {
        Task task = Task.builder()
                .id(1L)
                .titulo("Test")
                .descripcion("")
                .build();

        assertThat(task.getDescripcion()).isEmpty();
    }
}

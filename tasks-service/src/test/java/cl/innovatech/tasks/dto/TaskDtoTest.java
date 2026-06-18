package cl.innovatech.tasks.dto;

import cl.innovatech.tasks.entity.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskDtoTest {

    // ─── TaskResponse Tests ────────────────────────────────────────

    @Test
    void taskResponse_createdWithBuilder_tieneTodasLasPropiedades() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate fecha = LocalDate.now();

        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .titulo("Test Task")
                .descripcion("Test Description")
                .estado(Task.EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.ALTA)
                .fechaLimite(fecha)
                .projectId(10L)
                .responsableId(20L)
                .createdBy(5L)
                .createdAt(now)
                .updatedAt(now)
                .comentarios(List.of())
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitulo()).isEqualTo("Test Task");
        assertThat(response.getDescripcion()).isEqualTo("Test Description");
        assertThat(response.getEstado()).isEqualTo(Task.EstadoTarea.PENDIENTE);
        assertThat(response.getPrioridad()).isEqualTo(Task.PrioridadTarea.ALTA);
        assertThat(response.getFechaLimite()).isEqualTo(fecha);
        assertThat(response.getProjectId()).isEqualTo(10L);
        assertThat(response.getResponsableId()).isEqualTo(20L);
        assertThat(response.getCreatedBy()).isEqualTo(5L);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
        assertThat(response.getComentarios()).isEmpty();
    }

    @Test
    void taskResponse_conNoArgsConstructor_creaInstanciaVacia() {
        TaskResponse response = new TaskResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getTitulo()).isNull();
        assertThat(response.getEstado()).isNull();
    }

    @Test
    void taskResponse_conAllArgsConstructor_inicializaTodosLosParametros() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate fecha = LocalDate.now();

        TaskResponse response = new TaskResponse(
                1L, "Titulo", "Desc", Task.EstadoTarea.EN_PROGRESO,
                Task.PrioridadTarea.MEDIA, fecha, 10L, 20L, 5L, now, now, List.of()
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitulo()).isEqualTo("Titulo");
        assertThat(response.getEstado()).isEqualTo(Task.EstadoTarea.EN_PROGRESO);
    }

    @Test
    void taskResponse_setters_modificanPropiedades() {
        TaskResponse response = new TaskResponse();

        response.setId(5L);
        response.setTitulo("Nueva Tarea");
        response.setEstado(Task.EstadoTarea.COMPLETADA);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitulo()).isEqualTo("Nueva Tarea");
        assertThat(response.getEstado()).isEqualTo(Task.EstadoTarea.COMPLETADA);
    }

    // ─── CreateTaskRequest Tests ──────────────────────────────────

    @Test
    void createTaskRequest_conValoresValidos_creaExitosamente() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Nueva Tarea");
        request.setDescripcion("Descripción");
        request.setPrioridad(Task.PrioridadTarea.ALTA);
        request.setFechaLimite(LocalDate.now().plusDays(5));
        request.setProjectId(1L);
        request.setResponsableId(2L);

        assertThat(request.getTitulo()).isEqualTo("Nueva Tarea");
        assertThat(request.getProjectId()).isEqualTo(1L);
        assertThat(request.getResponsableId()).isEqualTo(2L);
    }

    @Test
    void createTaskRequest_gettersRetornanValoresCorrectos() {
        CreateTaskRequest request = new CreateTaskRequest();
        LocalDate fecha = LocalDate.now();

        request.setTitulo("Titulo");
        request.setDescripcion("Desc");
        request.setPrioridad(Task.PrioridadTarea.MEDIA);
        request.setFechaLimite(fecha);
        request.setProjectId(10L);
        request.setResponsableId(20L);

        assertThat(request.getTitulo()).isEqualTo("Titulo");
        assertThat(request.getDescripcion()).isEqualTo("Desc");
        assertThat(request.getPrioridad()).isEqualTo(Task.PrioridadTarea.MEDIA);
        assertThat(request.getFechaLimite()).isEqualTo(fecha);
        assertThat(request.getProjectId()).isEqualTo(10L);
        assertThat(request.getResponsableId()).isEqualTo(20L);
    }

    // ─── UpdateTaskRequest Tests ──────────────────────────────────

    @Test
    void updateTaskRequest_conValoresOpcionales_permiteActualizacionParcial() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitulo("Titulo Actualizado");
        request.setResponsableId(15L);

        assertThat(request.getTitulo()).isEqualTo("Titulo Actualizado");
        assertThat(request.getResponsableId()).isEqualTo(15L);
        assertThat(request.getDescripcion()).isNull();
    }

    // ─── UpdateStatusRequest Tests ────────────────────────────────

    @Test
    void updateStatusRequest_conNuevoEstado_setYGetFuncionan() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setNuevoEstado(Task.EstadoTarea.EN_PROGRESO);

        assertThat(request.getNuevoEstado()).isEqualTo(Task.EstadoTarea.EN_PROGRESO);
    }

    // ─── CommentResponse Tests ────────────────────────────────────

    @Test
    void commentResponse_createdWithBuilder_tieneTodasLasPropiedades() {
        LocalDateTime now = LocalDateTime.now();

        CommentResponse response = CommentResponse.builder()
                .id(1L)
                .taskId(5L)
                .userId(10L)
                .contenido("Este es un comentario")
                .createdAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTaskId()).isEqualTo(5L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getContenido()).isEqualTo("Este es un comentario");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void commentResponse_conNoArgsConstructor_creaInstanciaVacia() {
        CommentResponse response = new CommentResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getTaskId()).isNull();
    }

    @Test
    void commentResponse_setters_modificanPropiedades() {
        CommentResponse response = new CommentResponse();

        response.setId(1L);
        response.setTaskId(5L);
        response.setContenido("Nuevo comentario");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTaskId()).isEqualTo(5L);
        assertThat(response.getContenido()).isEqualTo("Nuevo comentario");
    }

    // ─── CommentRequest Tests ─────────────────────────────────────

    @Test
    void commentRequest_conContenido_setYGetFuncionan() {
        CommentRequest request = new CommentRequest();
        request.setContenido("Contenido del comentario");

        assertThat(request.getContenido()).isEqualTo("Contenido del comentario");
    }

    // ─── Equality and toString Tests ───────────────────────────────

    @Test
    void taskResponse_equals_compararDosInstanciasConMismosDatos() {
        TaskResponse response1 = TaskResponse.builder()
                .id(1L)
                .titulo("Test")
                .build();

        TaskResponse response2 = TaskResponse.builder()
                .id(1L)
                .titulo("Test")
                .build();

        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void taskResponse_toString_generaRepresentacionString() {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .titulo("Test")
                .build();

        String str = response.toString();
        assertThat(str).contains("TaskResponse");
        assertThat(str).contains("id=1");
    }

    @Test
    void commentResponse_hashCode_consistenteConEquals() {
        CommentResponse response1 = CommentResponse.builder()
                .id(1L)
                .taskId(5L)
                .build();

        CommentResponse response2 = CommentResponse.builder()
                .id(1L)
                .taskId(5L)
                .build();

        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }
}

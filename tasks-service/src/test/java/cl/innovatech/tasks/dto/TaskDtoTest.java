package cl.innovatech.tasks.dto;

import cl.innovatech.tasks.entity.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // ─── Extended TaskResponse Tests ────────────────────────────────

    @Test
    void taskResponse_conComentariosMultiples_mantieneLaLista() {
        CommentResponse comment1 = CommentResponse.builder().id(1L).build();
        CommentResponse comment2 = CommentResponse.builder().id(2L).build();
        List<CommentResponse> comentarios = List.of(comment1, comment2);

        TaskResponse response = TaskResponse.builder()
                .comentarios(comentarios)
                .build();

        assertThat(response.getComentarios()).hasSize(2);
        assertThat(response.getComentarios()).containsExactly(comment1, comment2);
    }

    @Test
    void taskResponse_conComentariosNull_permiteNull() {
        TaskResponse response = TaskResponse.builder()
                .comentarios(null)
                .build();

        assertThat(response.getComentarios()).isNull();
    }

    @Test
    void taskResponse_modificarComentarios_setYGetFuncionan() {
        TaskResponse response = new TaskResponse();
        List<CommentResponse> comentarios = new ArrayList<>();

        response.setComentarios(comentarios);
        assertThat(response.getComentarios()).isEqualTo(comentarios);
    }

    @Test
    void taskResponse_conEstadosVariados_almacenaCorrectamente() {
        for (Task.EstadoTarea estado : Task.EstadoTarea.values()) {
            TaskResponse response = TaskResponse.builder()
                    .estado(estado)
                    .build();

            assertThat(response.getEstado()).isEqualTo(estado);
        }
    }

    @Test
    void taskResponse_conPrioridadesVariadas_almacenaCorrectamente() {
        for (Task.PrioridadTarea prioridad : Task.PrioridadTarea.values()) {
            TaskResponse response = TaskResponse.builder()
                    .prioridad(prioridad)
                    .build();

            assertThat(response.getPrioridad()).isEqualTo(prioridad);
        }
    }

    // ─── Extended CreateTaskRequest Tests ──────────────────────────

    @Test
    void createTaskRequest_conTodosLosValoresNull_estaVacio() {
        CreateTaskRequest request = new CreateTaskRequest();

        assertThat(request.getTitulo()).isNull();
        assertThat(request.getDescripcion()).isNull();
        assertThat(request.getPrioridad()).isNull();
        assertThat(request.getFechaLimite()).isNull();
        assertThat(request.getProjectId()).isNull();
        assertThat(request.getResponsableId()).isNull();
    }

    @Test
    void createTaskRequest_modificarMultiplesCampos_settersIndependientes() {
        CreateTaskRequest request = new CreateTaskRequest();

        request.setTitulo("Titulo");
        request.setProjectId(100L);

        assertThat(request.getTitulo()).isEqualTo("Titulo");
        assertThat(request.getProjectId()).isEqualTo(100L);
        assertThat(request.getResponsableId()).isNull();
    }

    @Test
    void createTaskRequest_conDescripcionNull_estaPermitido() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Titulo");
        request.setProjectId(1L);
        request.setResponsableId(1L);

        assertThat(request.getDescripcion()).isNull();
    }

    @Test
    void createTaskRequest_conFechaLimiteNull_estaPermitido() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Titulo");
        request.setFechaLimite(null);

        assertThat(request.getFechaLimite()).isNull();
    }

    // ─── Extended UpdateTaskRequest Tests ──────────────────────────

    @Test
    void updateTaskRequest_sololoConTitulo_otrosCamposNull() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitulo("Nuevo Titulo");

        assertThat(request.getTitulo()).isEqualTo("Nuevo Titulo");
        assertThat(request.getDescripcion()).isNull();
        assertThat(request.getPrioridad()).isNull();
        assertThat(request.getResponsableId()).isNull();
    }

    @Test
    void updateTaskRequest_conMultiplosCampos_settersIndependientes() {
        UpdateTaskRequest request = new UpdateTaskRequest();

        request.setTitulo("Titulo");
        request.setPrioridad(Task.PrioridadTarea.BAJA);
        request.setResponsableId(50L);

        assertThat(request.getTitulo()).isEqualTo("Titulo");
        assertThat(request.getPrioridad()).isEqualTo(Task.PrioridadTarea.BAJA);
        assertThat(request.getResponsableId()).isEqualTo(50L);
        assertThat(request.getDescripcion()).isNull();
    }

    // ─── Extended UpdateStatusRequest Tests ────────────────────────

    @Test
    void updateStatusRequest_conTodosLosEstados_funcionaParaCadaUno() {
        for (Task.EstadoTarea estado : Task.EstadoTarea.values()) {
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setNuevoEstado(estado);

            assertThat(request.getNuevoEstado()).isEqualTo(estado);
        }
    }

    @Test
    void updateStatusRequest_modificandoEstadoMultiplesVeces_ultimoValorWins() {
        UpdateStatusRequest request = new UpdateStatusRequest();

        request.setNuevoEstado(Task.EstadoTarea.PENDIENTE);
        request.setNuevoEstado(Task.EstadoTarea.EN_PROGRESO);
        request.setNuevoEstado(Task.EstadoTarea.COMPLETADA);

        assertThat(request.getNuevoEstado()).isEqualTo(Task.EstadoTarea.COMPLETADA);
    }

    // ─── Extended CommentResponse Tests ────────────────────────────

    @Test
    void commentResponse_conTodosLosValores_mantieneLaIntegridad() {
        LocalDateTime now = LocalDateTime.now();

        CommentResponse response = CommentResponse.builder()
                .id(1L)
                .taskId(5L)
                .userId(10L)
                .contenido("Contenido largo que podría ser un comentario real en la aplicación")
                .createdAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTaskId()).isEqualTo(5L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getContenido())
                .isEqualTo("Contenido largo que podría ser un comentario real en la aplicación");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void commentResponse_conContenidoVacio_estaPermitido() {
        CommentResponse response = CommentResponse.builder()
                .id(1L)
                .contenido("")
                .build();

        assertThat(response.getContenido()).isEmpty();
    }

    @Test
    void commentResponse_modificandoTaskId_setYGetFuncionan() {
        CommentResponse response = new CommentResponse();
        response.setTaskId(100L);

        assertThat(response.getTaskId()).isEqualTo(100L);
    }

    // ─── Extended CommentRequest Tests ────────────────────────────

    @Test
    void commentRequest_conContenidoVacio_estaPermitido() {
        CommentRequest request = new CommentRequest();
        request.setContenido("");

        assertThat(request.getContenido()).isEmpty();
    }

    @Test
    void commentRequest_conContenidoNull_estaPermitido() {
        CommentRequest request = new CommentRequest();
        request.setContenido(null);

        assertThat(request.getContenido()).isNull();
    }

    @Test
    void commentRequest_conContenidoMuyLargo_seAlmacenaCompleto() {
        CommentRequest request = new CommentRequest();
        String contenidoLargo = "a".repeat(1000);
        request.setContenido(contenidoLargo);

        assertThat(request.getContenido()).hasSize(1000);
        assertThat(request.getContenido()).isEqualTo(contenidoLargo);
    }

    // ─── Null Safety Tests ────────────────────────────────────────

    @Test
    void taskResponse_todosCamposNull_funcionaCorrectamente() {
        TaskResponse response = new TaskResponse(null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(response.getId()).isNull();
        assertThat(response.getTitulo()).isNull();
        assertThat(response.getComentarios()).isNull();
    }

    @Test
    void createTaskRequest_conTituloCortisimo_estaPermitido() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("a");

        assertThat(request.getTitulo()).isEqualTo("a");
    }

    @Test
    void updateTaskRequest_conFechaFutura_seAlmacenaCorrectamente() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        LocalDate futuro = LocalDate.now().plusYears(10);
        request.setFechaLimite(futuro);

        assertThat(request.getFechaLimite()).isEqualTo(futuro);
    }

    @Test
    void updateTaskRequest_conFechaPasada_seAlmacenaCorrectamente() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        LocalDate pasado = LocalDate.now().minusYears(1);
        request.setFechaLimite(pasado);

        assertThat(request.getFechaLimite()).isEqualTo(pasado);
    }
}

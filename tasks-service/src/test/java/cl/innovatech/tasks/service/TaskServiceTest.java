package cl.innovatech.tasks.service;

import cl.innovatech.tasks.client.ProjectsClient;
import cl.innovatech.tasks.client.UsersClient;
import cl.innovatech.tasks.dto.*;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.entity.Task.EstadoTarea;
import cl.innovatech.tasks.entity.TaskComment;
import cl.innovatech.tasks.exception.ForbiddenException;
import cl.innovatech.tasks.exception.InvalidStateTransitionException;
import cl.innovatech.tasks.exception.ResourceNotFoundException;
import cl.innovatech.tasks.mapper.TaskMapper;
import cl.innovatech.tasks.repository.TaskCommentRepository;
import cl.innovatech.tasks.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskCommentRepository commentRepository;
    @Mock private EventPublisherService eventPublisherService;
    @Mock private ProjectsClient projectsClient;
    @Mock private UsersClient usersClient;

    private TaskMapper taskMapper;
    private ObjectMapper objectMapper;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
        objectMapper = new ObjectMapper();
        taskService = new TaskService(taskRepository, commentRepository, eventPublisherService,
                projectsClient, usersClient, taskMapper, objectMapper);
    }

    @Test
    void create_comoAdmin_creaExitosamente() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Tarea de prueba");
        request.setProjectId(1L);
        request.setResponsableId(2L);

        Task savedTask = Task.builder()
                .id(1L)
                .titulo("Tarea de prueba")
                .estado(EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .projectId(1L)
                .responsableId(2L)
                .createdBy(10L)
                .build();

        given(projectsClient.projectExists(1L)).willReturn(true);
        given(usersClient.userExists(2L)).willReturn(true);
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);

        TaskResponse result = taskService.create(request, 10L, "ADMIN");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitulo()).isEqualTo("Tarea de prueba");
        assertThat(result.getEstado()).isEqualTo(EstadoTarea.PENDIENTE);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void create_comoUser_lanzaForbidden() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Tarea");
        request.setProjectId(1L);
        request.setResponsableId(2L);

        assertThatThrownBy(() -> taskService.create(request, 1L, "USER"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("PROJECT_LEAD");
    }

    @Test
    void updateStatus_transicionValida_pendienteAEnProgreso() {
        Task task = Task.builder()
                .id(1L)
                .estado(EstadoTarea.PENDIENTE)
                .responsableId(5L)
                .projectId(1L)
                .createdBy(1L)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .titulo("Test")
                .build();

        Task updated = Task.builder()
                .id(1L)
                .estado(EstadoTarea.EN_PROGRESO)
                .responsableId(5L)
                .projectId(1L)
                .createdBy(1L)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .titulo("Test")
                .build();

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setNuevoEstado(EstadoTarea.EN_PROGRESO);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any(Task.class))).willReturn(updated);

        TaskResponse result = taskService.updateStatus(1L, request, 99L, "ADMIN");

        assertThat(result.getEstado()).isEqualTo(EstadoTarea.EN_PROGRESO);
    }

    @Test
    void updateStatus_transicionInvalida_completadaAPendiente_lanzaExcepcion() {
        Task task = Task.builder()
                .id(1L)
                .estado(EstadoTarea.COMPLETADA)
                .responsableId(5L)
                .projectId(1L)
                .createdBy(1L)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .titulo("Test")
                .build();

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setNuevoEstado(EstadoTarea.PENDIENTE);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateStatus(1L, request, 99L, "ADMIN"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("COMPLETADA");
    }

    @Test
    void updateStatus_transicionInvalida_enProgresoAPendiente_lanzaExcepcion() {
        Task task = Task.builder()
                .id(1L)
                .estado(EstadoTarea.EN_PROGRESO)
                .responsableId(5L)
                .projectId(1L)
                .createdBy(1L)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .titulo("Test")
                .build();

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setNuevoEstado(EstadoTarea.PENDIENTE);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateStatus(1L, request, 99L, "ADMIN"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void delete_comoUser_lanzaForbidden() {
        assertThatThrownBy(() -> taskService.delete(1L, "USER"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    void delete_comoAdmin_eliminaTarea() {
        Task task = buildTask(1L, EstadoTarea.PENDIENTE, 5L);
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        taskService.delete(1L, "ADMIN");

        verify(taskRepository).delete(task);
    }

    @Test
    void create_proyectoNoExiste_lanzaNotFound() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Tarea");
        request.setProjectId(99L);
        request.setResponsableId(1L);

        given(projectsClient.projectExists(99L)).willReturn(false);

        assertThatThrownBy(() -> taskService.create(request, 1L, "ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_responsableNoExiste_lanzaNotFound() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Tarea");
        request.setProjectId(1L);
        request.setResponsableId(99L);

        given(projectsClient.projectExists(1L)).willReturn(true);
        given(usersClient.userExists(99L)).willReturn(false);

        assertThatThrownBy(() -> taskService.create(request, 1L, "ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_sinFiltros_retornaTodasLasTareas() {
        given(taskRepository.findAll()).willReturn(List.of(buildTask(1L, EstadoTarea.PENDIENTE, 1L)));

        var result = taskService.findAll(null, null, null, 1L, "ADMIN");

        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_conProjectId_filtraPorProyecto() {
        given(taskRepository.findByProjectId(1L)).willReturn(List.of(buildTask(1L, EstadoTarea.PENDIENTE, 1L)));

        var result = taskService.findAll(1L, null, null, 1L, "ADMIN");

        assertThat(result).hasSize(1);
        verify(taskRepository).findByProjectId(1L);
    }

    @Test
    void findAll_conEstado_filtraPorEstado() {
        given(taskRepository.findByEstado(EstadoTarea.PENDIENTE))
                .willReturn(List.of(buildTask(1L, EstadoTarea.PENDIENTE, 1L)));

        var result = taskService.findAll(null, EstadoTarea.PENDIENTE, null, 1L, "ADMIN");

        assertThat(result).hasSize(1);
        verify(taskRepository).findByEstado(EstadoTarea.PENDIENTE);
    }

    @Test
    void findAll_conResponsableId_filtraPorResponsable() {
        given(taskRepository.findByResponsableId(5L)).willReturn(List.of(buildTask(1L, EstadoTarea.PENDIENTE, 5L)));

        var result = taskService.findAll(null, null, 5L, 1L, "ADMIN");

        assertThat(result).hasSize(1);
        verify(taskRepository).findByResponsableId(5L);
    }

    @Test
    void findAll_conProjectIdYEstado_combinaFiltros() {
        given(taskRepository.findByProjectIdAndEstado(1L, EstadoTarea.EN_PROGRESO))
                .willReturn(List.of(buildTask(1L, EstadoTarea.EN_PROGRESO, 1L)));

        var result = taskService.findAll(1L, EstadoTarea.EN_PROGRESO, null, 1L, "ADMIN");

        assertThat(result).hasSize(1);
        verify(taskRepository).findByProjectIdAndEstado(1L, EstadoTarea.EN_PROGRESO);
    }

    @Test
    void findAll_comoUser_soloVeSusTareasAsignadas() {
        Task suya = buildTask(1L, EstadoTarea.PENDIENTE, 10L);
        Task ajena = buildTask(2L, EstadoTarea.PENDIENTE, 99L);
        given(taskRepository.findAll()).willReturn(List.of(suya, ajena));

        var result = taskService.findAll(null, null, null, 10L, "USER");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void findById_comoAdmin_retornaTareaConComentarios() {
        Task task = buildTask(1L, EstadoTarea.PENDIENTE, 5L);
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(commentRepository.findByTaskIdOrderByCreatedAtAsc(1L)).willReturn(List.of());

        var result = taskService.findById(1L, 1L, "ADMIN");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_comoUserNoResponsable_lanzaForbidden() {
        Task task = buildTask(1L, EstadoTarea.PENDIENTE, 5L);
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findById(1L, 99L, "USER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findById_tareaNoExiste_lanzaNotFound() {
        given(taskRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L, 1L, "ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_comoAdmin_actualizaCampos() {
        Task task = buildTask(1L, EstadoTarea.PENDIENTE, 5L);
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any())).willReturn(task);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitulo("Nuevo título");
        request.setDescripcion("Nueva descripcion");

        var result = taskService.update(1L, request, 1L, "ADMIN");

        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void update_comoUserNoResponsable_lanzaForbidden() {
        Task task = buildTask(1L, EstadoTarea.PENDIENTE, 5L);
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(1L, new UpdateTaskRequest(), 99L, "USER"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateStatus_enProgresoACompletada_transicionValida() {
        Task task = buildTask(1L, EstadoTarea.EN_PROGRESO, 5L);
        Task saved = buildTask(1L, EstadoTarea.COMPLETADA, 5L);

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setNuevoEstado(EstadoTarea.COMPLETADA);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any())).willReturn(saved);

        var result = taskService.updateStatus(1L, request, 1L, "ADMIN");

        assertThat(result.getEstado()).isEqualTo(EstadoTarea.COMPLETADA);
    }

    @Test
    void updateStatus_enProgresoACancelada_transicionValida() {
        Task task = buildTask(1L, EstadoTarea.EN_PROGRESO, 5L);
        Task saved = buildTask(1L, EstadoTarea.CANCELADA, 5L);

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setNuevoEstado(EstadoTarea.CANCELADA);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any())).willReturn(saved);

        var result = taskService.updateStatus(1L, request, 1L, "ADMIN");

        assertThat(result.getEstado()).isEqualTo(EstadoTarea.CANCELADA);
    }

    @Test
    void addComment_comoAdmin_agregaComentario() {
        Task task = buildTask(1L, EstadoTarea.PENDIENTE, 5L);
        TaskComment comment = TaskComment.builder()
                .id(1L).task(task).userId(1L).contenido("Comentario de prueba").build();

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(commentRepository.save(any())).willReturn(comment);

        CommentRequest request = new CommentRequest();
        request.setContenido("Comentario de prueba");

        var result = taskService.addComment(1L, request, 1L, "ADMIN");

        assertThat(result.getContenido()).isEqualTo("Comentario de prueba");
        verify(commentRepository).save(any(TaskComment.class));
    }

    @Test
    void findByProject_comoAdmin_retornaTodasLasTareas() {
        given(taskRepository.findByProjectId(1L)).willReturn(List.of(
                buildTask(1L, EstadoTarea.PENDIENTE, 5L),
                buildTask(2L, EstadoTarea.EN_PROGRESO, 6L)
        ));

        var result = taskService.findByProject(1L, 1L, "ADMIN");

        assertThat(result).hasSize(2);
    }

    @Test
    void findByProject_comoUser_soloVeSusTareas() {
        given(taskRepository.findByProjectId(1L)).willReturn(List.of(
                buildTask(1L, EstadoTarea.PENDIENTE, 10L),
                buildTask(2L, EstadoTarea.EN_PROGRESO, 99L)
        ));

        var result = taskService.findByProject(1L, 10L, "USER");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Task buildTask(Long id, EstadoTarea estado, Long responsableId) {
        return Task.builder()
                .id(id).titulo("Tarea " + id).estado(estado)
                .prioridad(Task.PrioridadTarea.MEDIA)
                .projectId(1L).responsableId(responsableId).createdBy(1L)
                .build();
    }
}

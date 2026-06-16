package cl.innovatech.tasks.service;

import cl.innovatech.tasks.client.ProjectsClient;
import cl.innovatech.tasks.client.UsersClient;
import cl.innovatech.tasks.dto.CreateTaskRequest;
import cl.innovatech.tasks.dto.TaskResponse;
import cl.innovatech.tasks.dto.UpdateStatusRequest;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.entity.Task.EstadoTarea;
import cl.innovatech.tasks.exception.ForbiddenException;
import cl.innovatech.tasks.exception.InvalidStateTransitionException;
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
}

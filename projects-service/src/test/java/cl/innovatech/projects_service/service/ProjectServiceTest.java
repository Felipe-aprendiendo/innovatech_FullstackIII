package cl.innovatech.projects_service.service;

import cl.innovatech.projects_service.client.TasksClient;
import cl.innovatech.projects_service.client.UsersClient;
import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UsersClient usersClient;

    @Mock
    private TasksClient tasksClient;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldRejectInvalidDateRangeOnCreate() {
        ProjectRequest request = new ProjectRequest(
                "Portal Interno",
                "Proyecto de soporte operativo",
                Project.Prioridad.MEDIA,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 10),
                5L,
                Set.of(8L)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectService.create(request)
        );

        assertEquals("La fechaFin no puede ser anterior a la fechaInicio", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Project project = project(
                10L,
                Project.Estado.PLANIFICADO,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> projectService.changeStatus(10L, Project.Estado.COMPLETADO)
        );

        assertEquals(
                "No se puede cambiar el estado de PLANIFICADO a COMPLETADO",
                exception.getMessage()
        );
    }

    @Test
    void shouldCloseProjectAndFillEndDateWhenMissing() {
        Project project = project(20L, Project.Estado.EN_PROGRESO, LocalDate.of(2026, 6, 1), null);

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tasksClient.findByProject(20L)).thenReturn(List.of());

        var response = projectService.close(20L);

        assertEquals(Project.Estado.CERRADO, response.estado());
        assertEquals(LocalDate.now(), response.fechaFin());
        verify(projectRepository).save(project);
    }

    @Test
    void shouldNormalizeMembersAndIncludeResponsibleOnCreate() {
        ProjectRequest request = new ProjectRequest(
                "Portal Interno",
                "Proyecto de soporte operativo",
                Project.Prioridad.MEDIA,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                5L,
                Set.of(8L, 13L)
        );

        when(usersClient.getUserSummary(5L)).thenReturn(new UsersClient.UserSummary(5L, true));
        when(usersClient.getUserSummary(8L)).thenReturn(new UsersClient.UserSummary(8L, true));
        when(usersClient.getUserSummary(13L)).thenReturn(new UsersClient.UserSummary(13L, true));
        when(tasksClient.findByProject(77L)).thenReturn(List.of());
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(77L);
            return project;
        });

        var response = projectService.create(request);

        assertEquals(77L, response.id());
        assertEquals(new LinkedHashSet<>(Set.of(8L, 13L, 5L)), response.miembroIds());
    }

    @Test
    void shouldRejectDisabledMember() {
        ProjectRequest request = new ProjectRequest(
                "Portal Interno",
                "Proyecto de soporte operativo",
                Project.Prioridad.MEDIA,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                5L,
                Set.of(9L)
        );

        when(usersClient.getUserSummary(5L)).thenReturn(new UsersClient.UserSummary(5L, true));
        when(usersClient.getUserSummary(9L)).thenReturn(new UsersClient.UserSummary(9L, false));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> projectService.create(request)
        );

        assertEquals("El miembro no está habilitado: 9", exception.getMessage());
    }

    @Test
    void shouldCalculateProjectProgressFromTasks() {
        Project project = project(
                50L,
                Project.Estado.EN_PROGRESO,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(projectRepository.findById(50L)).thenReturn(Optional.of(project));
        when(tasksClient.findByProject(50L)).thenReturn(List.of(
                new TasksClient.TaskSummary(1L, "PENDIENTE"),
                new TasksClient.TaskSummary(2L, "EN_PROGRESO"),
                new TasksClient.TaskSummary(3L, "COMPLETADA"),
                new TasksClient.TaskSummary(4L, "COMPLETADA")
        ));

        var response = projectService.findById(50L);

        assertEquals(4, response.avance().totalTareas());
        assertEquals(1, response.avance().tareasPendientes());
        assertEquals(1, response.avance().tareasEnProgreso());
        assertEquals(2, response.avance().tareasCompletadas());
        assertEquals(50, response.avance().porcentajeAvance());
    }

    private Project project(Long id, Project.Estado estado, LocalDate fechaInicio, LocalDate fechaFin) {
        return Project.builder()
                .id(id)
                .nombre("Proyecto Base")
                .descripcion("Descripcion")
                .estado(estado)
                .prioridad(Project.Prioridad.MEDIA)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .responsableId(99L)
                .build();
    }
}

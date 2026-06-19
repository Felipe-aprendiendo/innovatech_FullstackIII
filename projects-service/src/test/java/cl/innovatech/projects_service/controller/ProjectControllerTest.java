package cl.innovatech.projects_service.controller;

import cl.innovatech.projects_service.dto.ProjectProgress;
import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.dto.ProjectResponse;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Test
    void createShouldDelegateToService() {
        ProjectRequest request = request();
        ProjectResponse response = response(10L);

        when(projectService.create(request)).thenReturn(response);

        ProjectResponse result = projectController.create(request);

        assertSame(response, result);
        verify(projectService).create(request);
    }

    @Test
    void findAllShouldDelegateToService() {
        List<ProjectResponse> response = List.of(response(11L));

        when(projectService.findAll(Project.Estado.EN_PROGRESO, 5L, 8L)).thenReturn(response);

        List<ProjectResponse> result = projectController.findAll(Project.Estado.EN_PROGRESO, 5L, 8L);

        assertSame(response, result);
        verify(projectService).findAll(Project.Estado.EN_PROGRESO, 5L, 8L);
    }

    @Test
    void findByIdShouldDelegateToService() {
        ProjectResponse response = response(12L);

        when(projectService.findById(12L)).thenReturn(response);

        ProjectResponse result = projectController.findById(12L);

        assertSame(response, result);
        verify(projectService).findById(12L);
    }

    @Test
    void updateShouldDelegateToService() {
        ProjectRequest request = request();
        ProjectResponse response = response(13L);

        when(projectService.update(13L, request)).thenReturn(response);

        ProjectResponse result = projectController.update(13L, request);

        assertSame(response, result);
        verify(projectService).update(13L, request);
    }

    @Test
    void deleteShouldDelegateToService() {
        projectController.delete(14L);

        verify(projectService).delete(14L);
    }

    @Test
    void changeStatusShouldDelegateToService() {
        ProjectResponse response = response(15L);

        when(projectService.changeStatus(15L, Project.Estado.COMPLETADO)).thenReturn(response);

        ProjectResponse result = projectController.changeStatus(15L, Project.Estado.COMPLETADO);

        assertSame(response, result);
        verify(projectService).changeStatus(15L, Project.Estado.COMPLETADO);
    }

    @Test
    void closeShouldDelegateToService() {
        ProjectResponse response = response(16L);

        when(projectService.close(16L)).thenReturn(response);

        ProjectResponse result = projectController.close(16L);

        assertSame(response, result);
        verify(projectService).close(16L);
    }

    private ProjectRequest request() {
        return new ProjectRequest(
                "Portal Interno",
                "Proyecto de soporte operativo",
                Project.Prioridad.MEDIA,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                5L,
                Set.of(8L, 13L)
        );
    }

    private ProjectResponse response(Long id) {
        return new ProjectResponse(
                id,
                "Proyecto " + id,
                "Descripcion",
                Project.Estado.EN_PROGRESO,
                Project.Prioridad.MEDIA,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                5L,
                Set.of(5L, 8L),
                new ProjectProgress(4, 1, 1, 2, 0, 50),
                LocalDateTime.of(2026, 6, 1, 8, 0),
                LocalDateTime.of(2026, 6, 2, 8, 0)
        );
    }
}

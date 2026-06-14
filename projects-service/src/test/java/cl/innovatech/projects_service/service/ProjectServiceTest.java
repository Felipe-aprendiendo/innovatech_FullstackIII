package cl.innovatech.projects_service.service;

import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

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
                5L
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

        var response = projectService.close(20L);

        assertEquals(Project.Estado.CERRADO, response.estado());
        assertEquals(LocalDate.now(), response.fechaFin());
        verify(projectRepository).save(project);
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

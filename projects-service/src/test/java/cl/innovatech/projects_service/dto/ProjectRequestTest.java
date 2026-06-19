package cl.innovatech.projects_service.dto;

import cl.innovatech.projects_service.entity.Project;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectRequestTest {

    @Test
    void shouldValidateDateRangeWhenEndDateIsAfterStartDate() {
        ProjectRequest request = request(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        );

        assertTrue(request.isDateRangeValid());
    }

    @Test
    void shouldInvalidateDateRangeWhenEndDateIsBeforeStartDate() {
        ProjectRequest request = request(
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 6, 1)
        );

        assertFalse(request.isDateRangeValid());
    }

    @Test
    void shouldAcceptMissingDatesDuringBeanValidationPhase() {
        ProjectRequest request = request(null, null);

        assertTrue(request.isDateRangeValid());
    }

    private ProjectRequest request(LocalDate fechaInicio, LocalDate fechaFin) {
        return new ProjectRequest(
                "Proyecto",
                "Descripcion",
                Project.Prioridad.MEDIA,
                fechaInicio,
                fechaFin,
                5L,
                Set.of(5L, 8L)
        );
    }
}

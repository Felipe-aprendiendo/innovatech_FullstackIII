package cl.innovatech.projects_service.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectEntityTest {

    @Test
    void prePersistShouldInitializeCreatedAndUpdatedAt() {
        Project project = Project.builder()
                .nombre("Proyecto")
                .descripcion("Descripcion")
                .responsableId(5L)
                .build();

        project.prePersist();

        assertNotNull(project.getCreatedAt());
        assertNotNull(project.getUpdatedAt());
        assertEquals(project.getCreatedAt(), project.getUpdatedAt());
    }

    @Test
    void preUpdateShouldRefreshUpdatedAt() {
        Project project = Project.builder()
                .nombre("Proyecto")
                .descripcion("Descripcion")
                .responsableId(5L)
                .build();
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 6, 1, 10, 30);
        project.setUpdatedAt(previousUpdatedAt);

        project.preUpdate();

        assertNotNull(project.getUpdatedAt());
        assertTrue(project.getUpdatedAt().isAfter(previousUpdatedAt));
    }
}

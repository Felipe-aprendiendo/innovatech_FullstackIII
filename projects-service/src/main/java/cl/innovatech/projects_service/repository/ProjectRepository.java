package cl.innovatech.projects_service.repository;

import cl.innovatech.projects_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByEstado(Project.Estado estado);

    List<Project> findByResponsableId(Long responsableId);
}
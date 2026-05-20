package cl.innovatech.tasks.repository;

import cl.innovatech.tasks.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByResponsableId(Long responsableId);

    List<Task> findByEstado(Task.EstadoTarea estado);

    List<Task> findByPrioridad(Task.PrioridadTarea prioridad);

    List<Task> findByProjectIdAndEstado(Long projectId, Task.EstadoTarea estado);

    List<Task> findByResponsableIdAndEstado(Long responsableId, Task.EstadoTarea estado);
}

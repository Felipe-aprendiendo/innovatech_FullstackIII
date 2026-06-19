package cl.innovatech.tasks.mapper;

import cl.innovatech.tasks.dto.CommentResponse;
import cl.innovatech.tasks.dto.CreateTaskRequest;
import cl.innovatech.tasks.dto.TaskResponse;
import cl.innovatech.tasks.entity.Task;
import cl.innovatech.tasks.entity.TaskComment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private final TaskMapper taskMapper = new TaskMapper();

    @Test
    void toEntity_mapeatodosLosCampos() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Tarea test");
        request.setDescripcion("Descripcion");
        request.setPrioridad(Task.PrioridadTarea.ALTA);
        request.setProjectId(1L);
        request.setResponsableId(2L);

        Task result = taskMapper.toEntity(request, 10L);

        assertThat(result.getTitulo()).isEqualTo("Tarea test");
        assertThat(result.getDescripcion()).isEqualTo("Descripcion");
        assertThat(result.getPrioridad()).isEqualTo(Task.PrioridadTarea.ALTA);
        assertThat(result.getProjectId()).isEqualTo(1L);
        assertThat(result.getResponsableId()).isEqualTo(2L);
        assertThat(result.getCreatedBy()).isEqualTo(10L);
    }

    @Test
    void toEntity_sinPrioridad_asignaPrioridadMedia() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitulo("Tarea sin prioridad");
        request.setProjectId(1L);
        request.setResponsableId(1L);

        Task result = taskMapper.toEntity(request, 1L);

        assertThat(result.getPrioridad()).isEqualTo(Task.PrioridadTarea.MEDIA);
    }

    @Test
    void toResponse_mapeatodosLosCampos() {
        Task task = Task.builder()
                .id(1L).titulo("Mi tarea").estado(Task.EstadoTarea.PENDIENTE)
                .prioridad(Task.PrioridadTarea.BAJA).projectId(2L)
                .responsableId(3L).createdBy(4L).build();

        TaskResponse result = taskMapper.toResponse(task);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitulo()).isEqualTo("Mi tarea");
        assertThat(result.getEstado()).isEqualTo(Task.EstadoTarea.PENDIENTE);
        assertThat(result.getPrioridad()).isEqualTo(Task.PrioridadTarea.BAJA);
        assertThat(result.getProjectId()).isEqualTo(2L);
        assertThat(result.getComentarios()).isEmpty();
    }

    @Test
    void toResponseWithComments_incluyeListaDeComentarios() {
        Task task = Task.builder()
                .id(1L).titulo("Tarea").estado(Task.EstadoTarea.EN_PROGRESO)
                .prioridad(Task.PrioridadTarea.MEDIA).projectId(1L)
                .responsableId(1L).createdBy(1L).build();

        TaskComment comment = TaskComment.builder()
                .id(10L).task(task).userId(5L).contenido("Buen avance").build();

        TaskResponse result = taskMapper.toResponseWithComments(task, List.of(comment));

        assertThat(result.getComentarios()).hasSize(1);
        assertThat(result.getComentarios().get(0).getContenido()).isEqualTo("Buen avance");
    }

    @Test
    void toCommentResponse_mapeatodosLosCampos() {
        Task task = Task.builder().id(1L).build();
        TaskComment comment = TaskComment.builder()
                .id(20L).task(task).userId(7L).contenido("Comentario X").build();

        CommentResponse result = taskMapper.toCommentResponse(comment);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getContenido()).isEqualTo("Comentario X");
    }
}

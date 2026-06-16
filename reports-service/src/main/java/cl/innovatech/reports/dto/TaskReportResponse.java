package cl.innovatech.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskReportResponse {

    private Long taskId;
    private String titulo;
    private String estado;
    private String prioridad;
    private Long projectId;
    private Long responsableId;
}

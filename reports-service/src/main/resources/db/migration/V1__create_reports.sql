CREATE TABLE kpi_snapshots (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    total_tareas    INT NOT NULL DEFAULT 0,
    tareas_completadas  INT NOT NULL DEFAULT 0,
    tareas_en_progreso  INT NOT NULL DEFAULT 0,
    tareas_pendientes   INT NOT NULL DEFAULT 0,
    porcentaje_avance   DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    calculado_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id)
);

CREATE TABLE report_cache (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo            VARCHAR(100) NOT NULL,
    filtros         JSON,
    resultado       JSON NOT NULL,
    generado_por    BIGINT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

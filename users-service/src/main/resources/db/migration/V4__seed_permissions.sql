INSERT INTO permissions (name, description)
SELECT 'PROJECT_READ', 'Puede visualizar proyectos'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PROJECT_READ');

INSERT INTO permissions (name, description)
SELECT 'PROJECT_WRITE', 'Puede crear y editar proyectos'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PROJECT_WRITE');

INSERT INTO permissions (name, description)
SELECT 'PROJECT_DELETE', 'Puede eliminar proyectos'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PROJECT_DELETE');

INSERT INTO permissions (name, description)
SELECT 'TASK_READ', 'Puede visualizar tareas'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TASK_READ');

INSERT INTO permissions (name, description)
SELECT 'TASK_WRITE', 'Puede crear y editar tareas'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TASK_WRITE');

INSERT INTO permissions (name, description)
SELECT 'TASK_DELETE', 'Puede eliminar tareas'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TASK_DELETE');

INSERT INTO permissions (name, description)
SELECT 'USER_READ', 'Puede visualizar usuarios'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_READ');

INSERT INTO permissions (name, description)
SELECT 'USER_WRITE', 'Puede crear y editar usuarios'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_WRITE');

INSERT INTO permissions (name, description)
SELECT 'REPORT_READ', 'Puede visualizar reportes'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'REPORT_READ');

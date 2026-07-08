INSERT INTO roles (name, description)
SELECT 'ROLE_PROJECT_LEAD', 'Lider de proyecto'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_PROJECT_LEAD');

INSERT INTO roles (name, description)
SELECT 'ROLE_USER', 'Usuario estandar'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER');

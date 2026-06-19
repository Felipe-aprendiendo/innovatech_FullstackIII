INSERT INTO roles (id, name, description)
SELECT 1, 'ROLE_ADMIN', 'Rol administrador inicial'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN'
);

UPDATE roles
SET description = 'Rol administrador inicial'
WHERE name = 'ROLE_ADMIN';

INSERT INTO users (id, email, name, last_name, password, enabled)
SELECT 1, 'admin@innovatech.cl', 'Admin', NULL, '{AUTH_MANAGED_EXTERNALLY}', b'1'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@innovatech.cl'
);

UPDATE users
SET name = 'Admin',
    last_name = NULL,
    password = '{AUTH_MANAGED_EXTERNALLY}',
    enabled = b'1'
WHERE email = 'admin@innovatech.cl';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@innovatech.cl'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

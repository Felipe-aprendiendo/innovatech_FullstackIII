-- ============================================================
-- V1__Create_auth_tables.sql
-- Base de datos INDEPENDIENTE: innovatech_auth
-- Gestiona credenciales, tokens y auditoría de autenticación.
-- NO almacena datos de perfil (eso es responsabilidad de users-service).
-- ============================================================

-- -------------------------------------------------------
-- TABLA: auth_users
-- Credenciales de acceso solamente.
-- El campo users_service_id enlaza con users-service.
-- -------------------------------------------------------
CREATE TABLE auth_users (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    email               VARCHAR(150)    NOT NULL COMMENT 'Correo corporativo (igual que en users-service)',
    password_hash       VARCHAR(255)    NOT NULL COMMENT 'BCrypt hash',
    users_service_id    BIGINT          NULL     COMMENT 'ID en users-service (sincronizado)',
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    bloqueado           BOOLEAN         NOT NULL DEFAULT FALSE,
    intentos_fallidos   INT             NOT NULL DEFAULT 0,
    ultimo_login        DATETIME        NULL,
    password_changed_at DATETIME        NULL,
    must_change_password BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_users            PRIMARY KEY (id),
    CONSTRAINT uq_auth_users_email      UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- TABLA: refresh_tokens
-- Persiste refresh tokens emitidos (respaldo de Redis).
-- -------------------------------------------------------
CREATE TABLE refresh_tokens (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    auth_user_id    BIGINT          NOT NULL,
    token_hash      VARCHAR(255)    NOT NULL COMMENT 'SHA-256 del token',
    expires_at      DATETIME        NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    revoked_at      DATETIME        NULL,
    user_agent      VARCHAR(255)    NULL,
    ip_address      VARCHAR(45)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_refresh_tokens            PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash        UNIQUE (token_hash),
    CONSTRAINT fk_rt_auth_user  FOREIGN KEY (auth_user_id)
        REFERENCES auth_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- TABLA: audit_login
-- Registro de todos los intentos de login.
-- -------------------------------------------------------
CREATE TABLE audit_login (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    email           VARCHAR(150)    NOT NULL,
    exitoso         BOOLEAN         NOT NULL,
    ip_address      VARCHAR(45)     NULL,
    user_agent      VARCHAR(255)    NULL,
    mensaje         VARCHAR(255)    NULL COMMENT 'Motivo de fallo si aplica',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_login PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- ÍNDICES
-- -------------------------------------------------------
CREATE INDEX idx_auth_users_email          ON auth_users(email);
CREATE INDEX idx_refresh_tokens_user       ON refresh_tokens(auth_user_id);
CREATE INDEX idx_refresh_tokens_expires    ON refresh_tokens(expires_at);
CREATE INDEX idx_audit_login_email         ON audit_login(email);
CREATE INDEX idx_audit_login_created       ON audit_login(created_at);

-- -------------------------------------------------------
-- DATOS INICIALES
-- Admin: admin@innovatech.cl / Admin2024!
-- Hash generado con BCrypt rounds=12
-- -------------------------------------------------------
INSERT INTO auth_users (email, password_hash, activo, must_change_password)
VALUES (
    'admin@innovatech.cl',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    TRUE,
    FALSE
);

-- ArenaCode Backend - Identity Core
-- Cria tabelas users, roles, user_roles e dados iniciais

-- Tabela users
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               CITEXT NULL,
    password_hash       VARCHAR(255) NULL,
    display_name        VARCHAR(80) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    is_guest            BOOLEAN NOT NULL DEFAULT FALSE,
    current_rating      INTEGER NOT NULL DEFAULT 1000,
    preferred_language  VARCHAR(30) NOT NULL DEFAULT 'JAVA_21',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ NULL,

    -- Constraints de domínio
    CONSTRAINT users_status_check CHECK (status IN ('GUEST', 'ACTIVE', 'SUSPENDED', 'BANNED', 'DELETED')),
    CONSTRAINT users_preferred_language_check CHECK (preferred_language IN ('JAVA_21')),
    CONSTRAINT users_current_rating_check CHECK (current_rating >= 0),
    CONSTRAINT users_guest_status_consistency CHECK (
        (status = 'GUEST' AND is_guest = TRUE) OR
        (status != 'GUEST')
    ),
    CONSTRAINT users_guest_email_password_check CHECK (
        (is_guest = TRUE AND email IS NULL AND password_hash IS NULL) OR
        (is_guest = FALSE)
    ),

    -- Índice único case-insensitive em email (CITEXT já garante)
    CONSTRAINT users_email_unique UNIQUE (email)
);

-- Índice em status para consultas frequentes
CREATE INDEX users_status_idx ON users (status);

-- Trigger de updated_at (usa função genrica fn_set_updated_at se existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'fn_set_updated_at') THEN
        CREATE TRIGGER users_set_updated_at
            BEFORE UPDATE ON users
            FOR EACH ROW
            EXECUTE FUNCTION fn_set_updated_at();
    END IF;
END $$;

-- Tabela roles
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Tabela user_roles (associao muitos-para-muitos)
CREATE TABLE user_roles (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- Índice em role_id para consultas por papel
CREATE INDEX user_roles_role_id_idx ON user_roles (role_id);

-- Insero de roles iniciais
INSERT INTO roles (code, description) VALUES
    ('ADMIN', 'Administrador da plataforma com acesso total'),
    ('MODERATOR', 'Moderador com poderes de gesto de contedo e usurios'),
    ('USER', 'Usurio padro da plataforma'),
    ('SPECTATOR', 'Espectador com acesso limitado a visualizao');

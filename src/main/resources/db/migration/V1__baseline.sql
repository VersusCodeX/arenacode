-- Baseline de infraestrutura do ArenaCode Backend.
-- Nao cria tabelas de dominio (users, problems, matches, submissions, etc.).
-- Objetivo: validar a conexao com PostgreSQL e a execucao do Flyway.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE app_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metadata_key VARCHAR(100) NOT NULL UNIQUE,
    metadata_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_app_metadata_set_updated_at
    BEFORE UPDATE ON app_metadata
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

INSERT INTO app_metadata (metadata_key, metadata_value)
VALUES ('schema_version_label', 'V1');

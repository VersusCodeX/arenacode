-- ArenaCode Backend - Problem Catalog
-- Cria tabelas problems e test_cases

-- Tabela problems
CREATE TABLE problems (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug                    VARCHAR(120) NOT NULL,
    title                   VARCHAR(150) NOT NULL,
    statement               TEXT NOT NULL,
    input_specification     TEXT NULL,
    output_specification    TEXT NULL,
    constraints_description TEXT NULL,
    difficulty              VARCHAR(20) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    time_limit_ms           INTEGER NOT NULL DEFAULT 2000,
    memory_limit_kb         INTEGER NOT NULL DEFAULT 262144,
    created_by              UUID NULL REFERENCES users(id) ON DELETE SET NULL,
    version                 BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at            TIMESTAMPTZ NULL,
    archived_at             TIMESTAMPTZ NULL,

    -- Constraints de domínio
    CONSTRAINT problems_slug_unique UNIQUE (slug),
    CONSTRAINT problems_slug_format_check CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT problems_title_not_blank_check CHECK (length(btrim(title)) > 0),
    CONSTRAINT problems_statement_not_blank_check CHECK (length(btrim(statement)) > 0),
    CONSTRAINT problems_difficulty_check CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT problems_status_check CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT problems_time_limit_check CHECK (time_limit_ms BETWEEN 100 AND 10000),
    CONSTRAINT problems_memory_limit_check CHECK (memory_limit_kb BETWEEN 16384 AND 1048576),
    CONSTRAINT problems_published_at_consistency CHECK (
        (status = 'DRAFT') OR
        (status = 'PUBLISHED' AND published_at IS NOT NULL) OR
        (status = 'ARCHIVED' AND archived_at IS NOT NULL)
    )
);

-- Índice para listagem do catálogo por status/dificuldade
CREATE INDEX problems_status_difficulty_idx ON problems (status, difficulty);

-- Índice em created_by para consultas por autor
CREATE INDEX problems_created_by_idx ON problems (created_by);

CREATE TRIGGER problems_set_updated_at
    BEFORE UPDATE ON problems
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

-- Tabela test_cases
CREATE TABLE test_cases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id      UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    ordinal         INTEGER NOT NULL,
    input           TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    visibility      VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    weight          INTEGER NOT NULL DEFAULT 1,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Constraints de domínio
    CONSTRAINT test_cases_visibility_check CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT test_cases_ordinal_check CHECK (ordinal >= 1),
    CONSTRAINT test_cases_weight_check CHECK (weight BETWEEN 1 AND 100),
    CONSTRAINT test_cases_input_size_check CHECK (octet_length(input) <= 1048576),
    CONSTRAINT test_cases_expected_output_size_check CHECK (octet_length(expected_output) <= 1048576),

    -- Ordem única por problema; deferrable para permitir reordenação na mesma transação
    CONSTRAINT test_cases_problem_ordinal_unique UNIQUE (problem_id, ordinal)
        DEFERRABLE INITIALLY DEFERRED
);

CREATE TRIGGER test_cases_set_updated_at
    BEFORE UPDATE ON test_cases
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

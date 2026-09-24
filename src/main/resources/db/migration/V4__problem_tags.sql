-- ArenaCode Backend - Problem Catalog: Tags
-- Cria tabelas tags e problem_tags, e evolui constraints do catalogo de problemas
-- (V3__problem_catalog.sql ja existe e e imutavel; ajustes de constraint entram aqui)

-- Tabela tags
CREATE TABLE tags (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(80) NOT NULL,
    normalized_name VARCHAR(80) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT tags_normalized_name_unique UNIQUE (normalized_name),
    CONSTRAINT tags_name_not_blank_check CHECK (length(btrim(name)) > 0)
);

-- Tabela problem_tags (associacao muitos-para-muitos)
CREATE TABLE problem_tags (
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    tag_id     UUID NOT NULL REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY (problem_id, tag_id)
);

-- Indice em tag_id para consultas "quais problemas tem esta tag"
CREATE INDEX problem_tags_tag_id_idx ON problem_tags (tag_id);

-- Indice de test cases por problema, habilitados, em ordem (usado pelo julgamento)
CREATE INDEX test_cases_problem_enabled_ordinal_idx
    ON test_cases (problem_id, enabled, ordinal);

-- Indice de problemas publicados por dificuldade e data de publicacao (catalogo publico)
CREATE INDEX problems_published_difficulty_date_idx
    ON problems (difficulty, published_at DESC)
    WHERE status = 'PUBLISHED';

-- Amplia os limites maximos de tempo/memoria aceitos pelo dominio (Problem.MAX_TIME_LIMIT_MS
-- passou de 10s para 60s, e Problem.MAX_MEMORY_LIMIT_KB de 1 GiB para 2 GiB)
ALTER TABLE problems DROP CONSTRAINT problems_time_limit_check;
ALTER TABLE problems ADD CONSTRAINT problems_time_limit_check
    CHECK (time_limit_ms BETWEEN 100 AND 60000);

ALTER TABLE problems DROP CONSTRAINT problems_memory_limit_check;
ALTER TABLE problems ADD CONSTRAINT problems_memory_limit_check
    CHECK (memory_limit_kb BETWEEN 16384 AND 2097152);

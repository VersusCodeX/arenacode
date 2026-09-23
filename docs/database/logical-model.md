# ArenaCode - Logical Database Model

Este documento descreve o modelo lgico do banco de dados do ArenaCode.

## Identity Module

### users

Armazena usurios da plataforma, incluindo convidados e usurios registrados.

| Coluna             | Tipo          | Restries                                     |
|--------------------|---------------|------------------------------------------------|
| id                 | UUID          | PRIMARY KEY, DEFAULT gen_random_uuid()         |
| email              | CITEXT        | UNIQUE (NULLs mltiplos permitidos)            |
| password_hash      | VARCHAR(255)  | NULL                                           |
| display_name       | VARCHAR(80)   | NOT NULL                                       |
| status             | VARCHAR(20)   | NOT NULL, CHECK (GUEST, ACTIVE, SUSPENDED, BANNED, DELETED) |
| is_guest           | BOOLEAN       | NOT NULL, DEFAULT FALSE                        |
| current_rating     | INTEGER       | NOT NULL, DEFAULT 1000, CHECK >= 0             |
| preferred_language | VARCHAR(30)   | NOT NULL, DEFAULT 'JAVA_21', CHECK (JAVA_21)   |
| created_at         | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW()                        |
| updated_at         | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW()                        |
| deleted_at         | TIMESTAMPTZ   | NULL                                           |

**Constraints adicionais:**
- Se `status = 'GUEST'`, ento `is_guest = TRUE`.
- Se `is_guest = TRUE`, ento `email` e `password_hash` devem ser NULL.
- `email` nico case-insensitive (via CITEXT).

### roles

Pap is de autorizao do sistema.

| Coluna      | Tipo          | Restries                             |
|-------------|---------------|----------------------------------------|
| id          | UUID          | PRIMARY KEY, DEFAULT gen_random_uuid() |
| code        | VARCHAR(40)   | NOT NULL, UNIQUE                       |
| description | VARCHAR(255)  | NOT NULL                               |
| created_at  | TIMESTAMPTZ   | NOT NULL, DEFAULT NOW()                |

**Roles iniciais:** ADMIN, MODERATOR, USER, SPECTATOR.

### user_roles

Associao muitos-para-muitos entre usurios e pap is.

| Coluna    | Tipo        | Restries                               |
|-----------|-------------|------------------------------------------|
| user_id   | UUID        | NOT NULL, FK → users(id), ON DELETE CASCADE |
| role_id   | UUID        | NOT NULL, FK → roles(id), ON DELETE CASCADE |
| granted_at| TIMESTAMPTZ | NOT NULL, DEFAULT NOW()                  |
| PRIMARY KEY | (user_id, role_id) |                                  |

**Í«ndices:**
- `user_roles_role_id_idx` em `role_id`.
- `users_status_idx` em `status`.

## Problem Module

### problems

Catálogo de problemas de programação usados nas partidas.

| Coluna                  | Tipo         | Restrições                                                  |
|-------------------------|--------------|-------------------------------------------------------------|
| id                      | UUID         | PRIMARY KEY, DEFAULT gen_random_uuid()                      |
| slug                    | VARCHAR(120) | NOT NULL, UNIQUE, CHECK kebab-case minúsculo                |
| title                   | VARCHAR(150) | NOT NULL, CHECK não vazio                                   |
| statement               | TEXT         | NOT NULL, CHECK não vazio                                   |
| input_specification     | TEXT         | NULL                                                        |
| output_specification    | TEXT         | NULL                                                        |
| constraints_description | TEXT         | NULL                                                        |
| difficulty              | VARCHAR(20)  | NOT NULL, CHECK (EASY, MEDIUM, HARD, EXPERT)                |
| status                  | VARCHAR(20)  | NOT NULL, DEFAULT 'DRAFT', CHECK (DRAFT, PUBLISHED, ARCHIVED) |
| time_limit_ms           | INTEGER      | NOT NULL, DEFAULT 2000, CHECK 100..10000                    |
| memory_limit_kb         | INTEGER      | NOT NULL, DEFAULT 262144, CHECK 16384..1048576              |
| created_by              | UUID         | NULL, FK → users(id), ON DELETE SET NULL                    |
| version                 | BIGINT       | NOT NULL, DEFAULT 0 (locking otimista)                      |
| created_at              | TIMESTAMPTZ  | NOT NULL, DEFAULT NOW()                                     |
| updated_at              | TIMESTAMPTZ  | NOT NULL, DEFAULT NOW()                                     |
| published_at            | TIMESTAMPTZ  | NULL                                                        |
| archived_at             | TIMESTAMPTZ  | NULL                                                        |

**Constraints adicionais:**
- Se `status = 'PUBLISHED'`, então `published_at` é obrigatório.
- Se `status = 'ARCHIVED'`, então `archived_at` é obrigatório.

**Índices:**
- `problems_status_difficulty_idx` em `(status, difficulty)`.
- `problems_created_by_idx` em `created_by`.

### test_cases

Casos de teste de um problema. `PUBLIC` são exemplos exibidos ao jogador; `PRIVATE` são usados apenas no julgamento e nunca expostos.

| Coluna          | Tipo        | Restrições                                             |
|-----------------|-------------|--------------------------------------------------------|
| id              | UUID        | PRIMARY KEY, DEFAULT gen_random_uuid()                 |
| problem_id      | UUID        | NOT NULL, FK → problems(id), ON DELETE CASCADE         |
| ordinal         | INTEGER     | NOT NULL, CHECK >= 1                                   |
| input           | TEXT        | NOT NULL, CHECK até 1 MiB                              |
| expected_output | TEXT        | NOT NULL, CHECK até 1 MiB                              |
| visibility      | VARCHAR(20) | NOT NULL, DEFAULT 'PRIVATE', CHECK (PUBLIC, PRIVATE)   |
| weight          | INTEGER     | NOT NULL, DEFAULT 1, CHECK 1..100                      |
| enabled         | BOOLEAN     | NOT NULL, DEFAULT TRUE                                 |
| created_at      | TIMESTAMPTZ | NOT NULL, DEFAULT NOW()                                |
| updated_at      | TIMESTAMPTZ | NOT NULL, DEFAULT NOW()                                |

**Constraints adicionais:**
- `(problem_id, ordinal)` único, `DEFERRABLE INITIALLY DEFERRED` para permitir reordenação na mesma transação.

**Regras de domínio (aplicadas na entidade `Problem`):**
- Apenas problemas `DRAFT` podem ser publicados, e somente com pelo menos um caso `PRIVATE` habilitado.
- Um problema `PUBLISHED` não pode ficar sem caso `PRIVATE` habilitado (remover, desabilitar ou tornar público o último é bloqueado).
- Problemas `ARCHIVED` não podem ser alterados.
- Apenas problemas `PUBLISHED` com caso `PRIVATE` habilitado podem ser usados em partidas.

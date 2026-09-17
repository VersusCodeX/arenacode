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

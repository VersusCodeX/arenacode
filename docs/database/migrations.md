# ArenaCode - Database Migrations

Este documento lista todas as migrations do Flyway aplicadas ao banco de dados.

## Migrations

### V1__baseline.sql

**Descri o:** Migration inicial que estabelece a baseline do schema.

**M dulo:** foundation  
**Data:** 2026-09-08

### V2__identity_core.sql

**Descri o:** Cria a estrutura base do m dulo Identity.

**M dulo:** identity  
**Data:** 2026-09-16

**Tabelas criadas:**
- `users` - Armazena usurios com constraints de dom nio e trigger de `updated_at`.
- `roles` - Pap is de autorizao com 4 pap is iniciais (ADMIN, MODERATOR, USER, SPECTATOR).
- `user_roles` - Associao muitos-para-muitos entre usurios e pap is.

**Í«ndices criados:**
- `users_status_idx` em `users(status)`.
- `user_roles_role_id_idx` em `user_roles(role_id)`.

**Constraints:**
- `users_status_check` - Valida status permitido.
- `users_preferred_language_check` - Valida linguagem de programao.
- `users_current_rating_check` - Garante rating >= 0.
- `users_guest_status_consistency` - Garante consistncia entre status GUEST e is_guest.
- `users_guest_email_password_check` - Garante que guests tenham email/password nulos.
- `users_email_unique` - Unique em email case-insensitive via CITEXT.

**Dependncias:** Nenhuma (migration independente do m dulo identity).

### V3__problem_catalog.sql

**Descrição:** Cria a estrutura base do módulo Problem (catálogo de problemas e casos de teste).

**Módulo:** problem  
**Data:** 2026-09-23

**Tabelas criadas:**
- `problems` - Problemas de programação com slug único, dificuldade, status de publicação, limites de execução, locking otimista (`version`) e trigger de `updated_at`.
- `test_cases` - Casos de teste de cada problema, com visibilidade pública/privada, ordem, peso, flag de habilitação e trigger de `updated_at`.

**Índices criados:**
- `problems_status_difficulty_idx` em `problems(status, difficulty)`.
- `problems_created_by_idx` em `problems(created_by)`.
- Consultas por `test_cases(problem_id)` usam o índice da constraint `test_cases_problem_ordinal_unique`.

**Constraints:**
- `problems_slug_unique` / `problems_slug_format_check` - Slug único em kebab-case minúsculo (`^[a-z0-9]+(-[a-z0-9]+)*$`).
- `problems_title_not_blank_check` / `problems_statement_not_blank_check` - Título e enunciado não podem ser vazios.
- `problems_difficulty_check` - Valida dificuldade (EASY, MEDIUM, HARD, EXPERT).
- `problems_status_check` - Valida status (DRAFT, PUBLISHED, ARCHIVED).
- `problems_time_limit_check` - Limite de tempo entre 100 e 10000 ms.
- `problems_memory_limit_check` - Limite de memória entre 16384 e 1048576 KB.
- `problems_published_at_consistency` - `PUBLISHED` exige `published_at`; `ARCHIVED` exige `archived_at`.
- `test_cases_visibility_check` - Valida visibilidade (PUBLIC, PRIVATE).
- `test_cases_ordinal_check` - Ordinal >= 1.
- `test_cases_weight_check` - Peso entre 1 e 100.
- `test_cases_input_size_check` / `test_cases_expected_output_size_check` - Entrada e saída esperada com no máximo 1 MiB.
- `test_cases_problem_ordinal_unique` - Ordem única por problema; `DEFERRABLE INITIALLY DEFERRED` para permitir reordenação na mesma transação.

**Dependências:** V1 (`fn_set_updated_at`) e V2 (`users`, referenciada por `problems.created_by` com `ON DELETE SET NULL`).

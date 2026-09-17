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

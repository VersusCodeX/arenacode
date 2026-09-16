# Convençªµes de Arquitetura - ArenaCode Backend

## Package base

Todo código Java reside sob `com.arenacode.arenacode`.

## Arquitetura

Monó¡¡¡lito modular. Módulos futuros (packages):

- `shared` - Utilitá¡¡|rios comuns, exceçªµes base, constants
- `identity` - Usuá¡¡|rios, autenticaçª£o, autorizaçª£o
- `problem` - Problemas, testes, categorias, tags
- `match` - Partidas, rankings temporá¡¡|rios
- `submission` - Submissõ£µ£s, julgamentos, resultados
- `rating` - Elo, Glicko, histó|rico de rating
- `recommendation` - Recomendaçª£o de problemas baseada em performance
- `audit` - Logs de auditoria, trilhas de decisõ£µ£s
- `platform` - Configuraçªµes da plataforma, feature flags

## Frontend

O frontend React/TypeScript **nã£µ|o está| neste repositó¡¡¡rio**. Será desenvolvido separadamente.

## Banco de dados

- **Flyway** é| a ú|nica fonte oficial de criaçª£o e evoluçª£o do schema.
- Migrations em `src/main/resources/db/migration/`.
- Formato: `V<versao>__<descricao>.sql` (ex: `V001__create_users_table.sql`).
- **JPA/Hibernate** usa `ddl-auto=validate`. Nunca use `update`, `create` ou `create-drop`.
- **DBeaver** é| ferramenta de inspeçª£o, consultas SQL, testes de triggers/procedures e `EXPLAIN ANALYZE`.

## Segredos

**Proibido** commitar segredos reais no Git:

- Tokens
- Senhas
- Credenciais de API
- Chaves privadas

Use `.env` local (ignorada) e `.env.example` (com nomes, sem valores).

## Nomenclatura

### Có|digo Java

- Classes: **PascalCase** em inglês (`UserRepository`, `SubmissionService`)
- Mé|todos/variá¡¡|veis: **camelCase** em inglês (`findActiveUsers`, `calculateRating`)
- Constants: `UPPER_SNAKE_CASE` (`MAX_SUBMISSION_SIZE`)
- Exceptions: `*Exception` (`SubmissionNotFoundException`)
- DTOs: `*Dto` (`UserDto`, `SubmissionResultDto`)
- Entities: `*Entity` ou nome do domí¡¡|nio (`User`, `Problem`)
- Repositories: `*Repository` (`ProblemRepository`)
- Services: `*Service` (`MatchmakingService`)
- Controllers: `*Controller` (`SubmissionController`)

### Migrations SQL

```
V001__create_users_table.sql
V002__create_problems_table.sql
V003__add_rating_columns_to_users.sql
```

### Commits

Padrã£µ|o **Conventional Commits**:

```
feat: add user registration endpoint
fix: correct rating calculation for draws
docs: update architecture conventions
refactor: extract validation logic to shared module
test: add integration tests for submission flow
chore: update Gradle wrapper
```

## Módulos e dependê£µ|ncias

- `shared` nã£µ|o depende de nenhum módulo de domí¡¡|nio.
- Módulos de domí¡¡|nio podem depender de `shared`.
- Evite dependê£µ|ncias circulares entre módulos de domí¡¡|nio.
- `platform` pode depender de todos (configuraçª£o global).

## Entidades JPA

**Nã£µ|o exponha entidades JPA diretamente em endpoints.** Use DTOs para respostas de API.

## Pró|ximos passos

1. Definir ADRs para decisõ£µ£s arquiteturais críticas
2. Modelar schema inicial no DBeaver
3. Criar migrations Flyway iniciais
4. Implementar módulos na ordem: `shared` → `identity` → `problem` → `submission` → `rating` → `match` → `recommendation` → `audit` → `platform`

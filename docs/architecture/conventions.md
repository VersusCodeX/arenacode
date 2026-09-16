# Convençªµes de Arquitetura - ArenaCode Backend

## Package base

Todo código Java reside sob `com.arenacode.arenacode`.

## Arquitetura

Monó¡¡¡lito modular organizado por contextos de domí¡¡|nio. Cada módulo possui responsabilidades bem definidas:

### Módulos

| Módulo | Responsabilidade |
|--------|------------------|
| `shared` | Utilitá¡¡|rios comuns, exceçªµes base, constants, componentes de segurança, observabilidade e helpers funcionais transversais a todos os módulos. |
| `identity` | Gestã£µ|o de usuá¡¡|rios, autenticaçª£o, autorizaçª£o, perfis de acesso e controle de sessõ£µ£s. |
| `problem` | Catá¡¡|logo de problemas de programaçª£o, testes automatizados, categorias, tags, dificuldade e metadados. |
| `match` | Matchmaking, criaçª£o de partidas, rankings temporá¡¡|rios, histó|rico de confrontos e estatí¡¡|sticas de jogos. |
| `submission` | Submissã£µ|o de código, julgamento automático, execuçª£o em sandbox, resultados e histó|rico de tentativas. |
| `rating` | Sistemas de rating (Elo/Glicko), cálculo de performance, histó|rico de rating, leaderboard e divisõ£µ£s. |
| `recommendation` | Recomendaçª£o de problemas baseada em performance, similaridade, vetores de embedding e preferê£µ|ncias do usuá¡¡|rio. |
| `audit` | Logs de auditoria, trilhas de decisõ£µ£s, eventos de conformidade e rastreabilidade de açÃµÃµes no sistema. |
| `platform` | Configuraçªµes globais, feature flags, saúde da aplicaçª£o (health checks), tratamento de exceçªµes e componentes web compartilhados. |

### Estrutura interna dos módulos

Cada módulo de domí¡¡|nio segue a estrutura Hexagonal/Ports & Adapters:

```
module/
├── domain/           # Entidades, value objects, regras de negó|cio puras
├── application/      # Casos de uso, serviços de aplicaçª£o
├── adapter/
│   ├── in/         # Adapters de entrada (HTTP, WebSocket, mensageria)
│   │   ├── web/
│   │   ├── websocket/
│   │   └── messaging/
│   └── out/        # Adapters de saída (banco, mensageria, sandbox, integraçªµes)
│       ├── persistence/
│       ├── messaging/
│       ├── sandbox/
│       └── vector/
└── config/           # Configuraçªµes especí¡¡|ficas do módulo (beans Spring)
```

### Módulo shared

Estrutura especial para componentes transversais:

```
shared/
├── domain/           # Interfaces comuns, value objects gené¡¡|ricos
├── exception/        # Exceçªµes gené¡¡|ricas e tratamentos de erro
├── security/         # Utilitá¡¡|rios criptográ¡¡|ficos, validaçª£o de tokens
├── observability/    # Logging estruturado, métricas, tracing
└── util/             # Helpers de string, data, validaçªµes, funçªµes funcionais
```

### Módulo platform

Estrutura especial para componentes de plataforma:

```
platform/
├── config/           # Configuraçªµes globais, beans compartilhados
├── exception/        # Handlers de erro globais, respostas padronizadas
├── health/           # Health checks, readiness/liveness, métricas
└── web/              # Filtros, interceptores, CORS, configuraçªµes HTTP
```

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

## Regras de arquitetura

### 1. Isolamento entre módulos

- **Um módulo nã£µ|o deve acessar diretamente adapters internos de outro módulo.**
- Comunicaçª£o entre módulos ocorre via interfaces p ú|blicas (controllers REST, eventos de domí¡¡|nio ou services bem definidos).
- Evite dependê£µ|ncias circulares entre módulos de domí¡¡|nio.

### 2. Camada de domí¡¡|nio pura

- **Classes de domí¡¡|nio (entidades, value objects, services de domí¡¡|nio) nã£µ|o devem depender de:**
  - Spring Framework (`@Autowired`, `@Component`, etc.)
  - JPA/Hibernate (`@Entity`, `@Repository`, etc.)
  - RabbitMQ, Redis, Docker ou qualquer infraestrutura externa
- Domí¡¡|nio deve ser POJO puro, testá¡¡|vel sem container Spring.

### 3. Adapters de entrada

- **Adapters de entrada (`adapter/in/`) sã£µ|o responsáveis por receber:**
  - Requisiçªµes HTTP (controllers REST)
  - Mensagens WebSocket
  - Mensagens de filas (RabbitMQ, Kafka)
- Convertem requisiçªµes externas em chamadas para `application/`.
- Contê£µ|m DTOs de entrada e validaçª£o de schema.

### 4. Adapters de saída

- **Adapters de saída (`adapter/out/`) lidam com:**
  - Persistê£µ|ncia em banco de dados (JPA, JDBC)
  - Mensageria (produtores/consumidores RabbitMQ)
  - Sandbox de execuçª£o de código
  - Bancos vetoriais (similaridade)
  - Integraçªµes externas (APIs de terceiros)
- Implementam interfaces definidas em `application/` ou `domain/`.

### 5. Shared e Platform

- `shared` nã£µ|o depende de nenhum módulo de domí¡¡|nio.
- Módulos de domí¡¡|nio podem depender de `shared`.
- `platform` pode depender de todos (configuraçª£o global, health, exception handling).

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

## Entidades JPA

**Nã£µ|o exponha entidades JPA diretamente em endpoints.** Use DTOs para respostas de API.

## Pró|ximos passos

1. Definir ADRs para decisõ£µ£s arquiteturais críticas
2. Modelar schema inicial no DBeaver
3. Criar migrations Flyway iniciais
4. Implementar módulos na ordem: `shared` → `identity` → `problem` → `submission` → `rating` → `match` → `recommendation` → `audit` → `platform`

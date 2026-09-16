# Configuração de Ambiente - ArenaCode Backend

## Visão geral dos profiles

O backend utiliza profiles do Spring Boot para separar configurações por ambiente. O profile padrão é `dev`.

| Profile | Arquivo | Uso |
|---------|---------|-----|
| `dev` | `application-dev.yaml` | Desenvolvimento local, logs verbosos, SQL visível para depuração, datasource com defaults locais de conveniência. |
| `test` | `application-test.yaml` | Execução de testes automatizados. O datasource real é injetado dinamicamente pelo Testcontainers (`@ServiceConnection`), nunca aponta para banco de produção. |
| `prod` | `application-prod.yaml` | Produção. Logs reduzidos, sem detalhes sensíveis expostos, sem credenciais no arquivo — exige `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` via ambiente. |

O profile ativo é selecionado pela variável de ambiente `SPRING_PROFILES_ACTIVE`.

## Datasource e PostgreSQL

O datasource agora está configurado em `application.yaml` (base) usando variáveis de ambiente, sem defaults:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: ${DB_POOL_MAX_SIZE:10}
      minimum-idle: ${DB_POOL_MIN_IDLE:2}
      connection-timeout: ${DB_POOL_CONNECTION_TIMEOUT:30000}
```

Em `application-dev.yaml`, esses mesmos valores recebem **defaults de conveniência apenas para desenvolvimento local** (`jdbc:postgresql://localhost:5432/arenacode`, usuário/senha `arenacode`), que podem ser sobrescritos por `.env` local. Em `test`, o Testcontainers injeta a conexão real dinamicamente via `@ServiceConnection`, sem necessidade de configuração estática. Em `prod`, as três variáveis (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) são obrigatórias e não possuem default.

### Pool de conexões (HikariCP)

| Variável | Default (base) | Default (dev) | Descrição |
|----------|-----------------|---------------|------------|
| `DB_POOL_MAX_SIZE` | `10` | `5` | Tamanho máximo do pool de conexões. |
| `DB_POOL_MIN_IDLE` | `2` | `1` | Número mínimo de conexões ociosas mantidas no pool. |
| `DB_POOL_CONNECTION_TIMEOUT` | `30000` | `30000` | Tempo máximo (ms) de espera por uma conexão do pool. |

## Variáveis de ambiente

### Obrigatórias agora (fora do profile dev)

| Variável | Descrição |
|----------|------------|
| `DATABASE_URL` | URL JDBC de conexão com o PostgreSQL (ex.: `jdbc:postgresql://host:5432/arenacode`). Obrigatória em `prod`; em `dev`, tem default local. |
| `DATABASE_USERNAME` | Usuário do banco de dados. Obrigatória em `prod`; em `dev`, tem default local. |
| `DATABASE_PASSWORD` | Senha do banco de dados. Obrigatória em `prod`; em `dev`, tem default local. |

### Opcionais atualmente

| Variável | Default | Descrição |
|----------|---------|------------|
| `SERVER_PORT` | `8080` | Porta HTTP do servidor embutido. |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile ativo (`dev`, `test`, `prod`). |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` / `DB_POOL_CONNECTION_TIMEOUT` | ver tabela acima | Ajustes finos do pool HikariCP. |

### Necessárias futuramente (outras integrações previstas)

| Variável | Descrição |
|----------|------------|
| `JWT_SECRET` | Chave de assinatura de tokens JWT (quando autenticação for implementada). |
| `REDIS_HOST` / `REDIS_PORT` | Conexão com Redis (cache/sessões, quando adotado). |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | Conexão com RabbitMQ (mensageria assíncrona, quando adotado). |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | Login social via GitHub (quando OAuth2 for implementado). |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Login social via Google (quando OAuth2 for implementado). |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS (relevante quando o frontend estiver integrado). |

Todas as variáveis estão documentadas em `.env.example` na raiz do repositório, apenas com os nomes, sem valores reais.

## Regras de segurança

- **Nunca commitar o arquivo `.env`** ou qualquer arquivo com valores reais de credenciais. Ele está listado no `.gitignore`.
- Apenas `.env.example` (sem valores) deve ser versionado.
- Os defaults de `application-dev.yaml` (`arenacode`/`arenacode`) são apenas conveniência local e nunca devem ser usados fora do ambiente de desenvolvimento do próprio desenvolvedor.
- Em produção, os valores sensíveis devem ser injetados pelo ambiente de execução (variáveis de ambiente do sistema, secret manager, etc.), nunca por arquivo versionado.

## JPA e schema do banco

- `spring.jpa.hibernate.ddl-auto` está fixado em **`validate`** em todos os profiles.
- O Hibernate **nunca** cria, atualiza ou recria tabelas automaticamente.
- **Flyway** é a única fonte oficial de criação e evolução do schema do banco, através das migrations em `src/main/resources/db/migration/`. Ver `docs/database/migrations.md` para detalhes e convenções.
- `spring.flyway.enabled=true` garante que as migrations sejam aplicadas automaticamente na inicialização.

## Testes de integração com Testcontainers

Os testes que exercitam o datasource real (`ArenacodeApplicationTests`, `BaselineMigrationIntegrationTest`) usam **Testcontainers** com `@ServiceConnection` para provisionar um PostgreSQL efêmero automaticamente, sem exigir instalação local do banco nem Docker Compose. Requer apenas Docker disponível na máquina/CI que executa `./gradlew test`.

# ArenaCode Backend

Backend Java do ArenaCode, uma plataforma competitiva de programacao.

## Objetivo

Prover API REST para submissoes de codigo, julgamento automatico, ranking, matchmaking e recomendacao de problemas.

## Aviso importante

Este repositorio contem **apenas o backend**. O frontend React/TypeScript sera desenvolvido em outro repositorio.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Gradle (Kotlin DSL)
- PostgreSQL
- Flyway (migrations)
- JPA/Hibernate (ddl-auto=validate)
- OpenAPI 3.0 (Springdoc)

## Pre-requisitos

- JDK 21+
- Gradle 8+
- PostgreSQL 15+ (ou Docker apenas para rodar os testes de integracao via Testcontainers)
- DBeaver (opcional, para inspecao do banco)

## Estrutura de diretorios

```
arenacode-backend/
├── src/
│   ├── main/
│   │   ├── java/com/arenacode/arenacode/
│   │   └── resources/
│   │       └── db/migration/
│   └── test/
├── docs/
│   ├── architecture/
│   │   ├── adr/
│   │   └── conventions.md
│   ├── database/
│   ├── api/
│   ├── operations/
│   └── demo/
├── build.gradle.kts
├── .env.example
└── README.md
```

## Comandos Gradle

```bash
# Build
./gradlew build

# Testes
./gradlew test

# Executar
./gradlew bootRun

# Limpar
./gradlew clean
```

## Qualidade de codigo

```bash
# Formatar codigo automaticamente
./gradlew spotlessApply

# Verificar formatacao (sem modificar arquivos)
./gradlew spotlessCheck

# Analise estatica de bugs
./gradlew spotbugsMain

# Verificacao completa (spotlessCheck + spotbugsMain + test + ...)
./gradlew check
```

Relatorios em `build/reports/`. Detalhes em [docs/operations/code-quality.md](docs/operations/code-quality.md).

## Health checks

- Endpoint tecnico simples: `GET /api/v1/health`
- Spring Boot Actuator: `GET /actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`

Detalhes completos em [docs/operations/local-development.md](docs/operations/local-development.md).

## OpenAPI / Swagger

Documentacao da API disponivel em:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

Detalhes em [docs/api/openapi.md](docs/api/openapi.md).

## Documentacao

- [Arquitetura e convencoes](docs/architecture/conventions.md)
- [Decisoes de arquitetura (ADRs)](docs/architecture/adr/)
- [Modelo de dados](docs/database/)
- [Migrations](docs/database/migrations.md)
- [API](docs/api/)
- [OpenAPI](docs/api/openapi.md)
- [Operacoes](docs/operations/)
- [Configuracao de ambiente](docs/operations/configuration.md)
- [Desenvolvimento local](docs/operations/local-development.md)
- [Qualidade de codigo](docs/operations/code-quality.md)
- [Demo](docs/demo/)

## Status

**Fundacao** - Estrutura inicial organizada, PostgreSQL/Flyway configurados com migration baseline, health checks (tecnico + Actuator) implementados, OpenAPI e tratamento de erros padronizados, ferramentas de qualidade de codigo (Spotless, SpotBugs) configuradas. Ainda sem funcionalidades de dominio.

## Licenca

Proprietario. Todos os direitos reservados.

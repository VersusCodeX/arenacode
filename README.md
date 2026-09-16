# ArenaCode Backend

Backend Java do ArenaCode, uma plataforma competitiva de programação.

## Objetivo

Prover API REST para submissões de código, julgamento automático, ranking, matchmaking e recomendação de problemas.

## Aviso importante

Este repositório contém **apenas o backend**. O frontend React/TypeScript será desenvolvido em outro repositório.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Gradle (Kotlin DSL)
- PostgreSQL
- Flyway (migrations)
- JPA/Hibernate (ddl-auto=validate)

## Pré-requisitos

- JDK 21+
- Gradle 8+
- PostgreSQL 15+ (ou Docker apenas para rodar os testes de integração via Testcontainers)
- DBeaver (opcional, para inspeção do banco)

## Estrutura de diretórios

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

## Health checks

- Endpoint técnico simples: `GET /api/v1/health`
- Spring Boot Actuator: `GET /actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`

Detalhes completos em [docs/operations/local-development.md](docs/operations/local-development.md).

## OpenAPI / Swagger

Ainda **não configurado**. O Springdoc OpenAPI será adicionado em um próximo commit, junto com os primeiros endpoints de domínio.

## Documentação

- [Arquitetura e convenções](docs/architecture/conventions.md)
- [Decisões de arquitetura (ADRs)](docs/architecture/adr/)
- [Modelo de dados](docs/database/)
- [Migrations](docs/database/migrations.md)
- [API](docs/api/)
- [Operações](docs/operations/)
- [Configuração de ambiente](docs/operations/configuration.md)
- [Desenvolvimento local](docs/operations/local-development.md)
- [Demo](docs/demo/)

## Status

**Fundação** - Estrutura inicial organizada, PostgreSQL/Flyway configurados com migration baseline, health checks (técnico + Actuator) implementados. Ainda sem funcionalidades de domínio.

## Licença

Proprietário. Todos os direitos reservados.

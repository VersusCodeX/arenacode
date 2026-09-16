# ArenaCode Backend

Backend Java do ArenaCode, uma plataforma competitiva de programação.

## Objetivo

Prover API REST para submissõ£µ£s de código, julgamento automático, ranking, matchmaking e recomendaçª£o de problemas.

## Aviso importante

Este repositó¡¡¡rio contém **apenas o backend**. O frontend React/TypeScript será desenvolvido em outro repositó¡¡¡rio.

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
- PostgreSQL 15+
- DBeaver (opcional, para inspeçª£o do banco)

## Estrutura de diretó¡¡¡rios

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

## Documentaçª£o

- [Arquitetura e convençªµes](docs/architecture/conventions.md)
- [Decisõ£µ£s de arquitetura (ADRs)](docs/architecture/adr/)
- [Modelo de dados](docs/database/)
- [API](docs/api/)
- [Operaçªµes](docs/operations/)
- [Demo](docs/demo/)

## Status

**Fundaçª£o** - Estrutura inicial organizada, sem funcionalidades de negó|cio implementadas.

## Licençª£o

Proprietá¡¡|rio. Todos os direitos reservados.

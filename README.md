# ArenaCode Backend

[![Backend CI](https://github.com/VersusCodeX/arenacode/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/VersusCodeX/arenacode/actions/workflows/backend-ci.yml)

Backend Java do ArenaCode, uma plataforma competitiva de programação. Este repositório contém somente o backend; o frontend React/TypeScript será mantido em repositório separado.

## Estado atual

O épico **Fundação** está concluído: estrutura do backend, PostgreSQL/Flyway, validação de schema via JPA, health checks, OpenAPI, ferramentas de qualidade e CI estão documentados e configurados.

Autenticação, problemas, partidas, matchmaking, ranking, submissões, julgamento de código, worker/sandbox e frontend pertencem aos **próximos épicos**. Este README não promete essas funcionalidades como disponíveis agora.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Gradle com Kotlin DSL e Gradle Wrapper
- PostgreSQL
- Flyway
- JPA/Hibernate com `ddl-auto=validate`
- OpenAPI 3.0 com Springdoc
- Spotless e SpotBugs
- GitHub Actions

## Como rodar

Pré-requisitos: JDK 21, Docker Engine, Docker Compose e Git. IntelliJ IDEA ou VS Code são recomendados; DBeaver e Bruno/Postman são opcionais.

```bash
git clone git@github.com:VersusCodeX/arenacode.git
cd arenacode
cp .env.example .env
docker compose up -d postgres
./gradlew test
./gradlew bootRun
```

Com a aplicação em execução:

- Health técnico: <http://localhost:8080/api/v1/health>
- Actuator: <http://localhost:8080/actuator/health>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Para encerrar:

```bash
docker compose down
# ou, para remover também os dados locais
docker compose down -v
```

Leia o guia completo em [Desenvolvimento local](docs/operations/local-development.md).

## Qualidade

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew spotbugsMain
./gradlew test
./gradlew check
./gradlew bootJar
```

Os relatórios são gerados em `build/reports/`. Veja [Qualidade de código](docs/operations/code-quality.md).

## CI

Todo push para `main` e todo pull request direcionado a `main` executa o workflow **Backend CI**. Todo pull request deve passar os checks de formatação, testes, análise estática e geração do JAR antes de ser integrado.

Veja [CI/CD](docs/operations/ci.md).

## Documentação

### Arquitetura e decisões

- [Convenções arquiteturais](docs/architecture/conventions.md)
- [ADR 0001 — Monólito modular primeiro](docs/architecture/adr/0001-modular-monolith-first.md)
- [ADR 0002 — Flyway como fonte de verdade do schema](docs/architecture/adr/0002-flyway-schema-source-of-truth.md)
- [ADR 0003 — PostgreSQL como banco transacional](docs/architecture/adr/0003-postgresql-transactional-database.md)
- [ADR 0004 — Frontend em repositório separado](docs/architecture/adr/0004-separate-frontend-repository.md)
- [ADR 0005 — Fronteira de sandbox para código não confiável](docs/architecture/adr/0005-untrusted-code-sandbox-boundary.md)
- [Arquitetura C4](docs/arenacode-c4-architecture.md)
- [Diagrama de classes](docs/arenacode-class-diagram.md)

### Operação e integração

- [Desenvolvimento local](docs/operations/local-development.md)
- [Configuração de ambiente](docs/operations/configuration.md)
- [Qualidade de código](docs/operations/code-quality.md)
- [CI/CD](docs/operations/ci.md)
- [Migrations](docs/database/migrations.md)
- [API e OpenAPI](docs/api/openapi.md)
- [Roteiro de demonstração da Fundação](docs/demo/foundation-demo.md)
- [Requisitos do projeto](docs/requirements.md)

## Roadmap resumido

1. **Fundação — concluído:** ambiente local, PostgreSQL/Flyway, validação JPA, observabilidade técnica, OpenAPI, qualidade e CI.
2. **Identidade e acesso — próximo:** autenticação, autorização e gestão de perfis.
3. **Domínio competitivo:** problemas, submissões, julgamento, ranking, partidas e matchmaking.
4. **Execução segura:** worker e sandbox isolados para código não confiável.
5. **Experiência e inteligência:** frontend React/TypeScript, recursos em tempo real e recomendações/IA quando os requisitos estiverem definidos.

## Licença

Proprietário. Todos os direitos reservados.

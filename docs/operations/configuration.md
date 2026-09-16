# Configuração de Ambiente - ArenaCode Backend

## Visão geral dos profiles

O backend utiliza profiles do Spring Boot para separar configurações por ambiente. O profile padrão é `dev`.

| Profile | Arquivo | Uso |
|---------|---------|-----|
| `dev` | `application-dev.yaml` | Desenvolvimento local, logs verbosos, SQL visível para depuração. |
| `test` | `application-test.yaml` | Execução de testes automatizados. Nunca aponta para banco de produção. Preparado para uso futuro com Testcontainers. |
| `prod` | `application-prod.yaml` | Produção. Logs reduzidos, sem detalhes sensíveis expostos, sem credenciais no arquivo. |

O profile ativo é selecionado pela variável de ambiente `SPRING_PROFILES_ACTIVE`.

## Exclusão temporária de autoconfiguração (IMPORTANTE)

Como nenhum `spring.datasource.*` foi configurado ainda (o PostgreSQL será conectado em uma etapa futura), o `application.yaml` desativa temporariamente as seguintes autoconfigurações do Spring Boot, via `spring.autoconfigure.exclude`:

- `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration`
- `org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration`
- `org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration`
- `org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration`

**Motivo:** sem essa exclusão, o Spring Boot tenta criar automaticamente um bean `DataSource` na inicialização (pois `spring-boot-starter-data-jpa` e Flyway estão no classpath), o que faz o contexto da aplicação falhar ao subir — incluindo em testes (`ArenacodeApplicationTests#contextLoads`), com `DataSourceBeanCreationException`.

**Esta exclusão é temporária.** Ela deve ser **removida** no commit em que o datasource real do PostgreSQL for configurado (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` via `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`). A partir daí, `spring.jpa.hibernate.ddl-auto=validate` e `spring.flyway.enabled=true` voltam a ter efeito normalmente.

## Variáveis de ambiente

### Obrigatórias atualmente

Nenhuma variável é estritamente obrigatória nesta etapa, pois o datasource ainda não está conectado (autoconfiguração desativada, ver seção acima). A aplicação roda com valores padrão em `dev`.

| Variável | Default | Descrição |
|----------|---------|------------|
| `SERVER_PORT` | `8080` | Porta HTTP do servidor embutido. |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile ativo (`dev`, `test`, `prod`). |

### Necessárias futuramente (quando o PostgreSQL for conectado)

| Variável | Descrição |
|----------|------------|
| `DATABASE_URL` | URL JDBC de conexão com o PostgreSQL. |
| `DATABASE_USERNAME` | Usuário do banco de dados. |
| `DATABASE_PASSWORD` | Senha do banco de dados. |

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
- Nenhum arquivo `application-*.yaml` deste repositório contém senhas, tokens ou URLs concretas de infraestrutura.
- Em produção, os valores sensíveis devem ser injetados pelo ambiente de execução (variáveis de ambiente do sistema, secret manager, etc.), nunca por arquivo versionado.

## JPA e schema do banco

- `spring.jpa.hibernate.ddl-auto` está fixado em **`validate`** em todos os profiles (a partir do momento em que a autoconfiguração de JPA for reativada).
- O Hibernate **nunca** cria, atualiza ou recria tabelas automaticamente.
- **Flyway** é a única fonte oficial de criação e evolução do schema do banco, através das migrations em `src/main/resources/db/migration/`.
- `spring.flyway.enabled=true` garante que as migrations sejam aplicadas automaticamente na inicialização, quando existirem e quando a autoconfiguração do Flyway estiver ativa.

## Observações importantes

- **Nenhum datasource está configurado ainda.** A conexão com o PostgreSQL será adicionada em uma etapa posterior, removendo a exclusão de autoconfiguração descrita acima.
- **Nenhuma migration SQL existe ainda.** O diretório `src/main/resources/db/migration/` será populado quando o schema inicial for definido.
- Os endpoints do Actuator expostos (`health`, `info`) são mínimos e não expõem detalhes sensíveis por padrão (`show-details: never` em `prod` e `test`).

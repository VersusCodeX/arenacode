# Migrations - ArenaCode Backend

## Fonte oficial do schema

**Flyway** é a única fonte oficial de criação e evolução do schema do banco de dados. Nenhuma tabela, extensão, função ou trigger deve ser criada manualmente via DBeaver ou qualquer outra ferramenta fora das migrations versionadas.

`spring.jpa.hibernate.ddl-auto` está fixado em `validate`: o Hibernate apenas confere se as entidades JPA correspondem ao schema existente, mas nunca cria, altera ou remove tabelas.

## Localização

As migrations residem em `src/main/resources/db/migration/` e são aplicadas automaticamente na inicialização da aplicação (`spring.flyway.enabled=true`).

## Convenção de nomenclatura

```
V<numero>__<descricao_em_snake_case>.sql
```

- `<numero>`: inteiro sequencial (`1`, `2`, `3`, ...). Não requer zero-padding.
- `<descricao_em_snake_case>`: breve, em inglês, usando `_` como separador.
- Duplo underscore (`__`) entre o número e a descrição é obrigatório (padrão do Flyway).

Exemplos:
```
V1__baseline.sql
V2__create_users_table.sql
V3__create_problems_table.sql
```

## Regras

- **Migrations aplicadas nunca devem ser editadas.** Uma vez mesclada na branch principal, uma migration é imutável. Correções exigem uma nova migration.
- **Cada migration deve ser idempotente sempre que possível** (`CREATE EXTENSION IF NOT EXISTS`, `CREATE TABLE IF NOT EXISTS` quando fizer sentido).
- **Nenhuma migration deve conter dados sensíveis** (senhas, tokens, PII real).
- Extensões do PostgreSQL (como `pgcrypto`, `citext`) devem ser habilitadas via migration, nunca manualmente.
- Toda migration deve ser validada localmente (`./gradlew test`, com Testcontainers) antes do merge.

## Migrations existentes

| Versão | Arquivo | Descrição |
|--------|---------|------------|
| V1 | `V1__baseline.sql` | Migration baseline de infraestrutura: habilita extensões `pgcrypto` e `citext`, cria a tabela `app_metadata`, a função `fn_set_updated_at()` e a trigger `trg_app_metadata_set_updated_at`. Insere o seed `schema_version_label = V1`. **Não cria nenhuma tabela de domínio** (users, problems, matches, submissions, etc.) — seu único objetivo é validar a conexão com PostgreSQL e a execução do Flyway. |

## Validação via testes

A execução da migration é validada automaticamente por testes de integração com **Testcontainers** (PostgreSQL real em container efêmero), sem depender de infraestrutura local instalada:

- `ArenacodeApplicationTests`: valida que o contexto Spring sobe com sucesso, o que implica conexão bem-sucedida com o PostgreSQL e execução sem erros do Flyway.
- `BaselineMigrationIntegrationTest`: valida explicitamente, via JDBC puro (sem entidades JPA), que:
  - a tabela `flyway_schema_history` existe;
  - a tabela `app_metadata` existe;
  - o seed `schema_version_label = V1` foi inserido corretamente;
  - a trigger `trg_app_metadata_set_updated_at` atualiza `updated_at` ao alterar `metadata_value`.

## Como adicionar uma nova migration

1. Crie o arquivo em `src/main/resources/db/migration/` seguindo a convenção de nomenclatura.
2. Escreva SQL puro e compatível com PostgreSQL.
3. Rode `./gradlew test` para validar que a migration é aplicada sem erros.
4. Atualize `docs/database/logical-model.md` se a migration introduzir ou alterar entidades de domínio.
5. Registre a nova migration na tabela de "Migrations existentes" acima.

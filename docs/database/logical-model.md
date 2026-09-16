# Modelo Lógico de Dados - ArenaCode Backend

## Status atual

O backend ainda está em fase de fundação. **Nenhuma tabela de domínio foi criada.** Este documento será expandido conforme cada módulo (`identity`, `problem`, `match`, `submission`, `rating`, `recommendation`, `audit`) definir suas próprias entidades e migrations.

## Baseline atual

A única migration existente (`V1__baseline.sql`) estabelece apenas infraestrutura de banco, sem modelar domínio de negócio. Ela existe para validar a conexão com PostgreSQL e a execução do Flyway antes de qualquer modelagem real.

### Extensões habilitadas

| Extensão | Finalidade |
|----------|------------|
| `pgcrypto` | Fornece `gen_random_uuid()`, usada como default da chave primária `id` de `app_metadata` e, futuramente, de outras entidades que adotarem UUID como identificador. |
| `citext` | Tipo de texto case-insensitive, útil para colunas como e-mail ou username em módulos futuros (ex.: `identity`). Habilitada preventivamente na baseline. |

### Tabela: `app_metadata`

Tabela de infraestrutura, **não pertence a nenhum módulo de domínio**. Usada para armazenar metadados operacionais da aplicação (ex.: versão do schema).

| Coluna | Tipo | Restrições | Descrição |
|--------|------|-------------|------------|
| `id` | UUID | PK, default `gen_random_uuid()` | Identificador único do registro. |
| `metadata_key` | VARCHAR(100) | NOT NULL, UNIQUE | Chave do metadado (ex.: `schema_version_label`). |
| `metadata_value` | TEXT | NOT NULL | Valor do metadado. |
| `created_at` | TIMESTAMPTZ | NOT NULL, default `NOW()` | Data de criação do registro. |
| `updated_at` | TIMESTAMPTZ | NOT NULL, default `NOW()` | Data da última atualização, mantida automaticamente pela trigger abaixo. |

### Função: `fn_set_updated_at()`

Função PL/pgSQL genérica que define `NEW.updated_at = NOW()` e retorna `NEW`. Reutilizável por outras tabelas que adotarem o mesmo padrão de auditoria de atualização em migrations futuras.

### Trigger: `trg_app_metadata_set_updated_at`

Disparada `BEFORE UPDATE` em `app_metadata`, garantindo que `updated_at` reflita sempre o momento da última modificação, sem depender da aplicação para definir esse valor.

### Seed inicial

| `metadata_key` | `metadata_value` |
|----------------|-------------------|
| `schema_version_label` | `V1` |

## Próximos passos

A modelagem de entidades de domínio (usuários, problemas, partidas, submissões, ratings, etc.) será documentada aqui conforme cada módulo evoluir, sempre acompanhada da migration Flyway correspondente.

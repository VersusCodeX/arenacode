# ADR 0002: Flyway como fonte de verdade do schema

- **Status:** Accepted
- **Data:** 2026-09-16

## Contexto

O backend usa PostgreSQL e JPA/Hibernate. O schema precisa ser reproduzível em desenvolvimento, testes e ambientes futuros, com histórico auditável de cada mudança. Alterações manuais no banco podem criar divergências entre máquinas, dificultar revisão e tornar incidentes difíceis de reproduzir.

Ferramentas de administração, como DBeaver, são úteis para inspeção e consultas, mas não devem se tornar um mecanismo informal de evolução permanente do schema.

## Decisão

O Flyway é a única fonte de criação e evolução permanente do schema do banco de dados. Toda mudança estrutural deve ser registrada em migration versionada no repositório.

O JPA/Hibernate usará `ddl-auto=validate`, validando a compatibilidade entre o modelo mapeado e o schema existente, sem criar, alterar ou remover objetos de banco. O DBeaver pode ser utilizado para visualização e diagnóstico, mas não para criar mudanças permanentes de schema.

## Consequências positivas

- O schema é versionado, revisável e reproduzível a partir do repositório.
- Ambientes novos podem ser inicializados pelo mesmo conjunto de migrations.
- O histórico de mudanças facilita auditoria, diagnóstico e rollback planejado.
- `ddl-auto=validate` reduz o risco de alterações implícitas provocadas pela aplicação.
- A separação entre migration e mapeamento deixa responsabilidades explícitas.

## Consequências negativas

- Mudanças de dados e schema exigem planejamento e migrations adicionais.
- Desenvolvedores precisam seguir a disciplina de criar migrations para toda mudança persistente.
- Erros em migrations já distribuídas não devem ser corrigidos por edição retroativa; normalmente exigem uma nova migration corretiva.
- Consultas ou ajustes exploratórios feitos no DBeaver precisam ser descartados ou formalizados em migration.

## Alternativas consideradas

- **Hibernate `ddl-auto=update`:** rejeitada porque pode produzir alterações implícitas e não versionadas, com comportamento difícil de auditar.
- **Schema criado manualmente via DBeaver:** rejeitada pela falta de reprodutibilidade e revisão por código.
- **Scripts SQL sem ferramenta de migration:** rejeitada porque exigiria construir controle manual de ordem, execução e histórico.

## Relação com requisitos do projeto

A decisão atende ao requisito de PostgreSQL com Flyway e à configuração JPA/Hibernate em modo de validação, garantindo uma base de dados reproduzível para os próximos épicos.

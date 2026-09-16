# ADR 0003: PostgreSQL como banco transacional

- **Status:** Accepted
- **Data:** 2026-09-16

## Contexto

O ArenaCode deverá manter relações entre usuários, problemas, submissões, resultados de julgamento, partidas, rankings, auditoria e permissões. Esse domínio exige consistência transacional, consultas relacionais e capacidade de evolução sem introduzir bancos especializados prematuramente.

Além do modelo relacional, há necessidades prováveis de consultas analíticas e semiestruturadas, além da possibilidade futura de recomendação assistida por vetores.

## Decisão

PostgreSQL será a fonte de verdade transacional do ArenaCode.

A escolha se baseia em suporte a relações complexas, transações ACID, procedures, triggers, window functions, JSONB e uma evolução possível para busca vetorial com pgvector quando esse requisito existir. Recursos futuros serão adotados apenas quando houver um caso de uso definido.

## Consequências positivas

- Integridade referencial e transações ACID protegem operações que envolvem múltiplas entidades.
- SQL relacional e window functions apoiam ranking, histórico e consultas analíticas.
- JSONB permite acomodar metadados variáveis sem abandonar o modelo transacional.
- Procedures e triggers ficam disponíveis para casos de consistência que realmente precisem estar no banco.
- Há caminho técnico para pgvector em funcionalidades futuras de recomendação ou IA.

## Consequências negativas

- Modelagem relacional exige cuidado com índices, migrations e consultas em crescimento.
- Funcionalidades de busca textual, filas ou cache podem demandar componentes complementares no futuro.
- pgvector não é uma decisão de implementação imediata e requer avaliação de capacidade, índices e operação quando for adotado.
- Uso excessivo de triggers ou lógica no banco pode reduzir clareza; esses recursos devem ser excepcionais e documentados.

## Alternativas consideradas

- **Banco NoSQL como fonte principal:** rejeitada porque o domínio possui relacionamentos e consistência transacional importantes.
- **Múltiplos bancos desde a Fundação:** rejeitada por aumentar operação e sincronização sem necessidade comprovada.
- **Banco em memória ou arquivos locais:** rejeitada porque não atende às necessidades de persistência relacional e evolução do produto.

## Relação com requisitos do projeto

A decisão atende ao requisito de PostgreSQL e prepara o modelo de dados para ranking, auditoria, relações de domínio e futuras capacidades de recomendação, mantendo a fonte de verdade transacional única.

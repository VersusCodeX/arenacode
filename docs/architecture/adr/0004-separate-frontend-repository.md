# ADR 0004: Frontend em repositório separado

- **Status:** Accepted
- **Data:** 2026-09-16

## Contexto

O ArenaCode terá um frontend React/TypeScript e um backend Java/Spring Boot. As duas aplicações têm linguagens, ferramentas, dependências, testes, ciclos de entrega e responsabilidades diferentes.

Este repositório foi iniciado para concentrar o backend: API, regras de domínio, persistência, segurança, operações e documentação de integração. Misturar o frontend no mesmo repositório não traz benefício comprovado nesta fase.

## Decisão

O frontend React/TypeScript será mantido em um repositório separado.

A integração ocorrerá por contratos HTTP REST documentados em OpenAPI e por WebSocket onde comunicação em tempo real for necessária. Este repositório permanece focado no backend Java.

## Consequências positivas

- Frontend e backend têm ciclos de build, testes, dependências e deploy independentes.
- O repositório backend mantém foco em Java, Spring Boot, banco de dados e contratos de API.
- OpenAPI oferece uma base explícita para alinhamento, geração de clientes e validação de contratos.
- WebSocket fica disponível como mecanismo de integração em tempo real sem acoplar os repositórios.
- Equipes podem trabalhar de forma mais independente quando o produto crescer.

## Consequências negativas

- Mudanças de contrato exigem coordenação entre repositórios.
- Integração ponta a ponta requer ambientes e automação próprios.
- Decisões de versionamento e compatibilidade da API precisam ser mantidas com disciplina.
- Não há mudanças atômicas únicas que incluam frontend e backend em um só commit.

## Alternativas consideradas

- **Monorepo com frontend e backend:** rejeitada nesta fase porque mistura toolchains e reduz o foco do repositório backend sem necessidade comprovada.
- **Frontend servido pelo Spring Boot:** rejeitada por acoplar build e deploy de aplicações com responsabilidades distintas.
- **Integração direta com banco:** rejeitada porque viola encapsulamento e ignora os contratos REST/OpenAPI e WebSocket.

## Relação com requisitos do projeto

A decisão atende ao requisito de frontend React/TypeScript em outro repositório e define REST/OpenAPI e WebSocket como fronteiras de integração com o backend.

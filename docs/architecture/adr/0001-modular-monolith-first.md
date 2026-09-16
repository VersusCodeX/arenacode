# ADR 0001: Monólito modular primeiro

- **Status:** Accepted
- **Data:** 2026-09-16

## Contexto

O ArenaCode está na fase de Fundação. O produto deverá evoluir para abranger API, persistência, autenticação, catálogo e recomendação de problemas, submissões, julgamento, partidas, ranking e integração com componentes de execução de código e IA. Esses contextos têm responsabilidades distintas, mas ainda não há volume, equipes independentes, limites operacionais ou evidência de escala que justifiquem a complexidade distribuída desde o início.

Executar código submetido por usuários e integrar recursos de IA exigirá isolamento e escalabilidade próprios. Essas necessidades, porém, não exigem que a API de negócio comece como um conjunto de microsserviços.

## Decisão

O ArenaCode começará como um **monólito modular** Java/Spring Boot. Os módulos de domínio manterão limites explícitos de responsabilidades, contratos e dependências, mesmo sendo entregues no mesmo processo e repositório.

O worker de julgamento/sandbox e componentes de IA poderão ser extraídos posteriormente quando houver necessidade comprovada de isolamento, escalabilidade, tecnologia especializada ou ciclos de entrega independentes. Não iniciaremos com microsserviços.

## Consequências positivas

- Menor custo operacional na fase inicial: um build, uma aplicação, uma observabilidade e um fluxo de deploy.
- Desenvolvimento, depuração e testes de integrações entre módulos ficam mais simples.
- Transações e evolução de dados são mais diretas enquanto o domínio ainda está sendo descoberto.
- Os limites modulares reduzem o acoplamento e preservam um caminho de extração posterior.
- A equipe pode concentrar esforço no valor de negócio, segurança e qualidade da plataforma.

## Consequências negativas

- Uma implantação inicial reúne módulos no mesmo artefato e processo.
- Disciplina arquitetural é necessária para evitar dependências indevidas entre módulos.
- A extração futura de worker/sandbox ou IA exigirá contratos operacionais, telemetria e migração gradual.
- Escalabilidade independente de cada módulo não estará disponível até a extração ser justificada.

## Alternativas consideradas

- **Microsserviços desde o início:** rejeitada pelo custo de rede, observabilidade, deploy, contratos distribuídos e consistência de dados antes de haver necessidade comprovada.
- **Monólito sem modularização:** rejeitada porque aumentaria o acoplamento e tornaria extrações futuras mais caras.
- **Worker e API no mesmo processo:** rejeitada como arquitetura-alvo por misturar trabalho não confiável/intensivo com o processo que atende requisições HTTP.

## Relação com requisitos do projeto

A decisão suporta a evolução incremental do backend Java e atende à necessidade de, no futuro, separar avaliação de código submetido e capacidades de IA, sem antecipar a complexidade de microsserviços durante o épico Fundação.

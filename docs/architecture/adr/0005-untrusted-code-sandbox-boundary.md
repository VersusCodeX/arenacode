git add README.md docs/architecture/adr docs/operations/local-development.md docs/demo/foundation-demo.md# ADR 0005: Fronteira de sandbox para código não confiável

- **Status:** Accepted
- **Data:** 2026-09-16

## Contexto

A proposta do ArenaCode inclui submissão e julgamento de código de usuários. Código submetido é não confiável: pode conter loops infinitos, consumo excessivo de CPU ou memória, acesso indevido a rede ou arquivos e tentativas de exploração do ambiente.

O processo da API deve permanecer responsável por autenticação, orquestração, persistência e consulta de resultados. Executar código não confiável dentro desse mesmo processo comprometeria disponibilidade, segurança e previsibilidade operacional.

## Decisão

Código submetido por usuários **não será executado dentro do processo da API**.

Em um épico posterior, a avaliação será feita por um worker e sandbox Docker isolados, com limites de recursos, tempo, filesystem, rede e mecanismos de coleta de resultado apropriados. Este ADR registra somente a fronteira arquitetural; nenhum sandbox, worker ou execução de submissão é implementado na Fundação.

## Consequências positivas

- A API permanece isolada de processos arbitrários e intensivos em recursos.
- O julgamento poderá escalar de forma independente no futuro.
- Limites de CPU, memória, tempo, rede e filesystem podem ser aplicados no ambiente de execução.
- A fronteira torna mais clara a separação entre recebimento de submissão, fila/orquestração, execução e persistência de resultado.
- A implementação futura pode evoluir mecanismos de isolamento sem reescrever a API inteira.

## Consequências negativas

- O julgamento real de submissões ainda não existe nesta fase.
- A solução posterior exigirá trabalho adicional de isolamento, imagens, políticas de segurança, observabilidade, filas e operação.
- Docker sozinho não elimina todos os riscos; o desenho futuro precisará de defesa em profundidade e revisão de segurança.
- Haverá comunicação assíncrona ou integração entre API e worker a projetar quando o épico for iniciado.

## Alternativas consideradas

- **Executar código no processo da API:** rejeitada por expor disponibilidade e segurança do backend.
- **Executar via `ProcessBuilder` no host da API:** rejeitada por não oferecer isolamento suficiente.
- **Implementar sandbox agora:** rejeitada porque foge do escopo da Fundação e deve ser desenhada com requisitos de segurança e operação específicos.

## Relação com requisitos do projeto

A decisão atende ao requisito de julgamento automático futuro e estabelece explicitamente que autenticação, problemas, partidas e sandbox pertencem aos próximos épicos, não ao escopo já entregue pela Fundação.

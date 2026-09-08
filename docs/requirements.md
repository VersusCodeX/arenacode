# ArenaCode — Requisitos Funcionais e Não Funcionais

> Documento de requisitos da plataforma interativa de batalhas de programação em tempo real.

## Visão do produto

O **ArenaCode** é uma plataforma web hospedada em que jogadores participam de batalhas 1v1 de programação, submetem código para avaliação em ambiente isolado, acompanham resultados em tempo real e evoluem por meio de ranking, replays e recomendações personalizadas.

O sistema será desenvolvido com **Java como linguagem principal**, priorizando uma entrega pública e interativa desde o MVP. Recursos avançados serão implementados incrementalmente conforme o roadmap do projeto.

## Escopo e prioridades

| Prioridade | Significado |
|---|---|
| **P0** | Essencial para uma demo pública e interativa do MVP |
| **P1** | Essencial para confiabilidade, segurança e demonstração técnica avançada |
| **P2** | Evolução importante após o fluxo principal estar estável |
| **P3** | Recurso avançado/experimental, implementado somente após as fases anteriores |

---

# Requisitos Funcionais

## RF01 — Gestão de Usuários

| ID | Prioridade | Requisito |
|---|---:|---|
| RF01.1 | P0 | O sistema deve permitir cadastro de usuários por e-mail e senha. |
| RF01.2 | P1 | O sistema deve permitir login social via OAuth2/OpenID Connect com Google e GitHub. |
| RF01.3 | P0 | O sistema deve permitir login anônimo com sessão temporária, sem persistência de conta permanente. |
| RF01.4 | P1 | O sistema deve permitir edição de perfil, incluindo nome de exibição, avatar, linguagem preferida e dados públicos de rating. |
| RF01.5 | P1 | O sistema deve permitir recuperação de senha via e-mail com token temporário e de uso único. |
| RF01.6 | P1 | O sistema deve permitir logout e invalidação de sessões ativas. |

### Critérios de aceite

- Um visitante deve conseguir jogar como convidado sem fornecer credenciais.
- Usuários autenticados devem ter e-mail único e senhas armazenadas exclusivamente como hash seguro.
- Um usuário suspenso ou banido não pode criar partidas, entrar no matchmaking ou submeter código.

## RF02 — Gestão de Problemas

| ID | Prioridade | Requisito |
|---|---:|---|
| RF02.1 | P0 | O sistema deve permitir o cadastro de problemas com título, descrição, exemplos e restrições. |
| RF02.2 | P0 | O sistema deve permitir associar múltiplos casos de teste a cada problema, contendo entrada, saída esperada e visibilidade pública ou privada. |
| RF02.3 | P0 | O sistema deve permitir classificar problemas por dificuldade e tags, como `array`, `grafo` e `DP`. |
| RF02.4 | P2 | O sistema deve permitir importar problemas de fontes externas autorizadas, como GitHub e integrações baseadas em API/web scraping quando permitido. |
| RF02.5 | P2 | O sistema deve permitir relacionar pré-requisitos entre problemas. |
| RF02.6 | P1 | O sistema deve permitir busca e filtros por tags, dificuldade e estatísticas de acerto. |

### Regras de negócio

- Apenas problemas publicados, com limites de execução e pelo menos um caso de teste privado habilitado, podem ser usados em partidas.
- Casos de teste privados não podem ser enviados ao navegador, aparecer em eventos WebSocket, ser incluídos em logs públicos ou expostos em replays.
- Apenas `MODERATOR` e `ADMIN` podem criar, alterar, publicar ou arquivar problemas.

## RF03 — Sistema de Batalhas

| ID | Prioridade | Requisito |
|---|---:|---|
| RF03.1 | P0 | O sistema deve permitir criar salas de batalha 1v1 públicas ou privadas. |
| RF03.2 | P0 | O sistema deve permitir que dois jogadores entrem na mesma sala e recebam o mesmo problema simultaneamente. |
| RF03.3 | P0 | O sistema deve permitir desafiar outro jogador por link direto de uma sala privada. |
| RF03.4 | P1 | O sistema deve permitir matchmaking automático, encontrando oponente com rating similar, inicialmente em uma faixa de mais ou menos 200 pontos. |
| RF03.5 | P1 | O sistema deve permitir que moderador ou administrador crie batalhas com problemas customizados. |
| RF03.6 | P1 | O sistema deve permitir que espectadores entrem em salas públicas para assistir partidas em andamento. |
| RF03.7 | P1 | O sistema deve permitir que espectadores visualizem código-fonte somente após o término da partida ou mediante autorização explícita. |
| RF03.8 | P1 | O sistema deve permitir replay de partidas finalizadas, incluindo linha do tempo de submissões, resultados e tempo. |

### Regras de negócio

- Uma partida ativa deve possuir dois jogadores com lados distintos.
- Um jogador não pode participar simultaneamente de mais de uma partida ativa.
- Oponente e espectadores recebem somente progresso agregado durante a partida; o código-fonte permanece privado.
- O estado de uma partida deve seguir transições controladas: `WAITING_FOR_PLAYERS`, `READY`, `ACTIVE`, `FINISHING`, `FINISHED`, `CANCELLED` e `EXPIRED`.

## RF04 — Submissão e Execução de Código

| ID | Prioridade | Requisito |
|---|---:|---|
| RF04.1 | P0 | O sistema deve permitir submissão de código inicialmente em Java e, em fases posteriores, em Python, JavaScript, C e outras linguagens suportadas. |
| RF04.2 | P0 | O sistema deve executar código submetido em ambiente isolado, como container Docker sandbox. |
| RF04.3 | P0 | O sistema deve executar todos os casos de teste habilitados de um problema contra uma submissão. |
| RF04.4 | P0 | O sistema deve retornar o resultado por caso de teste, incluindo aceito, resposta errada, erro de compilação, erro de execução, limite de tempo e limite de memória. |
| RF04.5 | P0 | O sistema deve medir tempo de execução e consumo de memória por submissão. |
| RF04.6 | P0 | O sistema deve permitir reenvio de solução para o mesmo problema dentro dos limites de uso definidos. |
| RF04.7 | P1 | O sistema deve detectar e prevenir submissões duplicadas em curto intervalo por meio de chave de idempotência. |

### Regras de negócio

- A criação de submissão deve retornar `202 Accepted`, pois o processamento ocorre de forma assíncrona.
- A mesma combinação de usuário e `Idempotency-Key` deve devolver a submissão já criada, sem duplicar execução ou efeitos de negócio.
- Uma submissão em estado terminal não pode retornar a estado pendente ou em execução.
- O sandbox não pode ter acesso à rede externa, banco de dados, broker de mensagens ou segredos de infraestrutura.

## RF05 — Ranking e Rating

| ID | Prioridade | Requisito |
|---|---:|---|
| RF05.1 | P1 | O sistema deve calcular rating de jogadores a partir de vitórias, derrotas e empates, usando Elo inicialmente e permitindo evolução para Glicko. |
| RF05.2 | P1 | O sistema deve exibir ranking global paginado, como top 100 e top 1000. |
| RF05.3 | P2 | O sistema deve exibir ranking por linguagem de programação. |
| RF05.4 | P2 | O sistema deve exibir ranking por dificuldade de problema. |
| RF05.5 | P1 | O sistema deve exibir a evolução do rating de cada jogador ao longo do tempo. |
| RF05.6 | P2 | O sistema deve permitir comparar o rating e estatísticas de dois jogadores. |

### Regras de negócio

- O rating dos dois participantes de uma partida deve ser alterado na mesma transação.
- Caso qualquer etapa da finalização falhe, a operação deve sofrer rollback; nenhum participante pode ter alteração isolada de rating.
- O histórico de rating deve registrar valor anterior, valor posterior, variação, motivo e partida de origem.

## RF06 — Recomendação de Problemas

| ID | Prioridade | Requisito |
|---|---:|---|
| RF06.1 | P2 | O sistema deve recomendar problemas semelhantes aos que o jogador errou, com base em embeddings de texto e, quando aplicável, de código. |
| RF06.2 | P2 | O sistema deve recomendar problemas conforme histórico de acertos, erros, dificuldade, tags e evolução do jogador. |
| RF06.3 | P1 | O sistema deve permitir favoritar problemas ou marcá-los para praticar posteriormente. |
| RF06.4 | P2 | O sistema deve permitir consultar problemas semanticamente similares a um problema específico. |

## RF07 — Detecção de Similaridade

| ID | Prioridade | Requisito |
|---|---:|---|
| RF07.1 | P2 | O sistema deve calcular similaridade entre submissões de jogadores diferentes para o mesmo problema. |
| RF07.2 | P2 | O sistema deve alertar moderadores quando a similaridade ultrapassar limiar configurável, por exemplo 90%. |
| RF07.3 | P2 | O sistema deve permitir que o jogador consulte soluções otimizadas semelhantes à sua após o término da partida, respeitando regras de visibilidade. |

### Regras de negócio

- Um alerta de similaridade é indício para revisão humana, não uma punição automática.
- A análise deve combinar mecanismos lexicais, como normalização/hashes, e semânticos, como embeddings.
- A indisponibilidade de IA ou busca vetorial não pode impedir partidas, execuções ou cálculo de rating.

## RF08 — Notificações e Eventos em Tempo Real

| ID | Prioridade | Requisito |
|---|---:|---|
| RF08.1 | P0 | O sistema deve notificar jogadores em tempo real sobre início de partida, resultado de submissões e fim da partida via WebSocket. |
| RF08.2 | P1 | O sistema deve notificar espectadores sobre eventos da partida, exibindo progresso agregado, como quantidade de testes aprovados. |
| RF08.3 | P0 | O sistema deve notificar jogadores sobre desafios recebidos por link de sala privada. |
| RF08.4 | P1 | O sistema deve manter histórico de notificações no perfil do usuário. |

### Regras de negócio

- Eventos devem conter sequência monotônica por partida para permitir detecção de lacunas após reconexão.
- O cliente que reconectar deve poder consultar um snapshot atual da partida e retomar a assinatura de eventos.
- O WebSocket deve validar JWT no handshake e verificar permissão antes de assinar tópico de sala.

## RF09 — Auditoria e Logs

| ID | Prioridade | Requisito |
|---|---:|---|
| RF09.1 | P1 | O sistema deve registrar ações críticas, como criação de partida, submissão, alteração de rating e ações administrativas, em tabela de auditoria. |
| RF09.2 | P1 | O sistema deve permitir que administradores consultem auditoria por usuário, partida, recurso ou período. |
| RF09.3 | P1 | O sistema deve registrar tentativas de acesso não autorizado, falhas de autenticação e violações de políticas RBAC/ABAC. |

### Regras de negócio

- Auditoria deve registrar ator, ação, recurso, data/hora, identificadores de correlação e alterações relevantes antes/depois quando aplicável.
- Dados sensíveis, tokens, senhas, segredos e casos de teste privados não podem ser registrados em logs de auditoria.

## RF10 — Papéis e Permissões

| ID | Prioridade | Requisito |
|---|---:|---|
| RF10.1 | P0 | O sistema deve definir os papéis `ADMIN`, `MODERATOR`, `USER` e `SPECTATOR`; uma sessão convidada possui permissões restritas. |
| RF10.2 | P1 | O sistema deve permitir que administradores banam usuários, editem problemas e consultem auditoria completa. |
| RF10.3 | P1 | O sistema deve permitir que moderadores editem problemas e gerenciem alertas de similaridade. |
| RF10.4 | P0 | O sistema deve restringir partidas privadas a jogadores convidados e espectadores autorizados. |
| RF10.5 | P1 | O sistema deve restringir acessos usando atributos como dono do recurso, visibilidade, status da partida, faixa de rating e vínculo do usuário com a partida. |

### Modelo de autorização

- **RBAC** controla permissões por papel: administrar usuários, publicar problemas, revisar alertas e consultar auditoria.
- **ABAC** controla acesso contextual: dono da submissão, participante da partida, convidado, visibilidade pública/privada e estado do recurso.

## RF11 — Integrações Externas

| ID | Prioridade | Requisito |
|---|---:|---|
| RF11.1 | P2 | O sistema deve aceitar webhook assinado do GitHub para importar ou atualizar problemas autorizados. |
| RF11.2 | P3 | O sistema deve permitir integração opcional com Discord para notificações de partidas. |
| RF11.3 | P1 | O sistema deve permitir exportar dados autorizados, como submissões e histórico de rating, em CSV ou JSON. |

## RF12 — Interface do Usuário

| ID | Prioridade | Requisito |
|---|---:|---|
| RF12.1 | P0 | O sistema deve fornecer editor de código no navegador com realce de sintaxe, inicialmente via Monaco Editor ou equivalente. |
| RF12.2 | P0 | O sistema deve exibir timer de partida atualizado em tempo real. |
| RF12.3 | P0 | O sistema deve exibir placar em tempo real, incluindo testes aprovados, tempo e status. |
| RF12.4 | P1 | O sistema deve exibir replay de partida em linha do tempo interativa. |
| RF12.5 | P1 | O sistema deve exibir ranking, perfis de jogadores e histórico de partidas. |
| RF12.6 | P0 | O sistema deve ser responsivo e utilizável em desktop e dispositivos móveis. |

---

# Requisitos Não Funcionais

## RNF01 — Desempenho

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF01.1 | P2 | O sistema deve suportar pelo menos 1.000 usuários simultâneos sem degradação significativa. |
| RNF01.2 | P1 | A confirmação inicial de uma submissão deve responder em menos de 2 segundos no percentil 95; a avaliação completa é assíncrona e depende dos limites do problema. |
| RNF01.3 | P1 | A atualização de eventos WebSocket deve ter latência inferior a 200 ms no percentil 95 no ambiente alvo. |
| RNF01.4 | P2 | O sistema deve ser capaz de processar pelo menos 100 submissões por segundo em pico, condicionado à capacidade de workers e sandbox. |
| RNF01.5 | P1 | Consultas de ranking devem retornar em menos de 500 ms no percentil 95. |

## RNF02 — Escalabilidade

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF02.1 | P2 | O sistema deve escalar horizontalmente sob carga usando HPA no Kubernetes. |
| RNF02.2 | P2 | O sistema deve suportar replicação primária-réplica do banco para consultas de leitura. |
| RNF02.3 | P2 | O sistema deve usar consistent hashing para distribuir sessões WebSocket por partida entre instâncias. |
| RNF02.4 | P1 | O sistema deve permitir adicionar workers de execução sem indisponibilidade. |

## RNF03 — Disponibilidade e Resiliência

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF03.1 | P2 | O sistema deve buscar disponibilidade mínima de 99,5% no ambiente publicado. |
| RNF03.2 | P0 | O sistema deve expor health checks de liveness e readiness para monitoramento e orquestração. |
| RNF03.3 | P1 | O sistema deve aplicar retry automático com backoff, timeout e circuit breaker em chamadas externas, como webhooks, e-mail e provedores de embeddings. |
| RNF03.4 | P1 | O sistema deve operar em modo degradado quando componentes não críticos estiverem indisponíveis, por exemplo sem recomendações se a busca vetorial falhar. |

## RNF04 — Segurança

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF04.1 | P0 | O sistema deve usar JWT com expiração e refresh token para autenticação. |
| RNF04.2 | P1 | O sistema deve usar OAuth2/OIDC para login social e validar emissor, assinatura, audiência, expiração, escopos e PKCE quando aplicável. |
| RNF04.3 | P1 | O sistema deve aplicar RBAC e ABAC em todas as operações sensíveis. |
| RNF04.4 | P1 | O sistema deve mitigar riscos relevantes do OWASP Top 10, incluindo injeção, XSS, CSRF quando aplicável e falhas de autenticação/autorização. |
| RNF04.5 | P1 | O sistema deve aplicar rate limiting por usuário, IP e rota. |
| RNF04.6 | P1 | O sistema deve manter segredos fora do código usando Vault, Kubernetes Secrets ou equivalente. |
| RNF04.7 | P0 | O sistema deve usar HTTPS/WSS em comunicações externas no ambiente publicado. |
| RNF04.8 | P0 | O sandbox deve executar código isoladamente, com recursos limitados, usuário sem privilégios e rede externa bloqueada. |

## RNF05 — Confiabilidade e Consistência

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF05.1 | P1 | O sistema deve garantir idempotência em operações sensíveis, principalmente submissões e futuras operações financeiras caso sejam adicionadas. |
| RNF05.2 | P1 | O sistema deve usar transações ACID com commit e rollback explícitos para operações críticas. |
| RNF05.3 | P1 | O sistema deve usar triggers e stored procedures para validação e auditoria de regras de negócio selecionadas. |
| RNF05.4 | P1 | O sistema deve produzir logs estruturados para operações críticas. |
| RNF05.5 | P1 | O sistema deve expor métricas compatíveis com Prometheus. |

### Consistência de eventos

- O sistema deve usar padrão **transactional outbox** para persistir alteração de negócio e evento na mesma transação.
- A mensageria deve ser tratada como entrega **at-least-once**; consumidores precisam ser idempotentes.
- O worker deve confirmar a mensagem somente após persistir resultado terminal ou decidir encaminhar para retry/DLQ.

## RNF06 — Observabilidade

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF06.1 | P1 | O sistema deve produzir logs estruturados em JSON para requisições HTTP e eventos WebSocket. |
| RNF06.2 | P1 | O sistema deve expor métricas de latência, throughput, erros, filas, conexões e sandbox via Prometheus. |
| RNF06.3 | P2 | O sistema deve disponibilizar dashboards Grafana para saúde e capacidade. |
| RNF06.4 | P2 | O sistema deve possuir alertas para falhas críticas, como crescimento contínuo de filas, erro acima de 5%, indisponibilidade de dependências e aumento de timeout no sandbox. |

### Contexto mínimo de logs

- `traceId`, `spanId`, `requestId`, `matchId` e `submissionId` quando aplicáveis.
- `userId` pseudonimizado ou minimizado, rota, código HTTP, duração e resultado.
- Nenhum log pode conter senha, token, segredo, conteúdo de test case privado ou dados pessoais desnecessários.

## RNF07 — Manutenibilidade

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF07.1 | P0 | O código deve seguir convenções de nomenclatura, formatação e estrutura de pacotes consistentes para Java/Spring. |
| RNF07.2 | P1 | O sistema deve possuir testes unitários, integração e end-to-end, buscando cobertura mínima de 80% nas regras críticas. |
| RNF07.3 | P0 | O sistema deve possuir pipeline CI/CD com lint, testes, build e deploy automatizado. |
| RNF07.4 | P0 | O sistema deve possuir documentação completa, incluindo README, diagramas C4, OpenAPI/Swagger e decisões arquiteturais. |
| RNF07.5 | P0 | A API deve utilizar versionamento explícito, como `/api/v1` e `/api/v2`. |

### Práticas exigidas

- Migrations versionadas por Flyway.
- Testcontainers para testes de integração de PostgreSQL, Redis e RabbitMQ quando aplicável.
- DTOs separados de entidades JPA.
- Análise estática e formatação no CI, como Checkstyle/Spotless e SpotBugs/Sonar.
- ADRs para decisões arquiteturais relevantes.

## RNF08 — Portabilidade e Implantação

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF08.1 | P0 | O sistema deve ser containerizado usando Docker com builds multi-stage. |
| RNF08.2 | P2 | O sistema deve poder ser orquestrado com Kubernetes, incluindo Deployments, Services, ConfigMaps e Secrets. |
| RNF08.3 | P2 | O sistema deve poder ser implantado em AWS, GCP, Azure ou ambiente on-premise, evitando dependência rígida de fornecedor. |
| RNF08.4 | P2 | O sistema deve evoluir para suportar múltiplas linguagens de submissão, incluindo Java, Python, JavaScript e C. |

## RNF09 — Usabilidade e Acessibilidade

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF09.1 | P0 | O sistema deve fornecer interface intuitiva para jogadores e espectadores. |
| RNF09.2 | P0 | O sistema deve apresentar feedback visual claro para estados de submissão, como aceito, erro, timeout e limite de memória. |
| RNF09.3 | P1 | O sistema deve oferecer tutorial ou onboarding para novos usuários. |
| RNF09.4 | P1 | O sistema deve buscar conformidade com WCAG 2.1 nível AA. |

### Diretrizes de interface

- A tela de partida deve deixar claros problema, timer, editor, ação de submissão, progresso e estado da conexão.
- Estados assíncronos devem informar o usuário: `enviando`, `na fila`, `executando`, `resultado disponível` e `falha temporária`.
- A interface deve funcionar em telas pequenas, mas a experiência de edição de código pode orientar o uso de desktop para melhor ergonomia.

## RNF10 — Baixo Nível e Otimização

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF10.1 | P3 | O sistema deve implementar memory pool customizado em C, acessível via JNI, para parsing de logs de execução ou outro ponto quente comprovado. |
| RNF10.2 | P3 | O sistema deve otimizar buffers e reduzir alocações dinâmicas em hot loops de processamento. |
| RNF10.3 | P3 | O sistema deve documentar benchmarks comparando desempenho antes e depois da otimização nativa. |

### Condição de implementação

O uso de JNI/C só deve ser introduzido depois de medir gargalo real com ferramentas como JFR e JMH. Deve existir fallback Java funcional caso a biblioteca nativa não esteja disponível.

## RNF11 — Inteligência Artificial e Dados

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF11.1 | P2 | O sistema deve gerar embeddings para problemas e, quando autorizado, para códigos submetidos. |
| RNF11.2 | P2 | O sistema deve armazenar embeddings em banco vetorial, inicialmente pgvector ou, se justificado, Qdrant. |
| RNF11.3 | P2 | O sistema deve usar busca por similaridade para recomendação e detecção assistida de similaridade entre soluções. |
| RNF11.4 | P2 | O sistema deve possuir pipeline de ingestão de múltiplas fontes, como APIs, scraping autorizado e webhooks. |

### Restrições de IA

- Embeddings e recomendações devem ser processados por workers assíncronos.
- Dados derivados de IA não podem ser fonte única de decisão punitiva.
- Falhas no pipeline de IA não podem bloquear o fluxo crítico de partidas e submissões.

## RNF12 — Conformidade e Auditoria

| ID | Prioridade | Requisito |
|---|---:|---|
| RNF12.1 | P1 | O sistema deve registrar todas as ações críticas em tabela de auditoria. |
| RNF12.2 | P2 | O sistema deve permitir exportar logs para ferramentas externas, como ELK, Loki ou Datadog. |
| RNF12.3 | P1 | O sistema deve adotar boas práticas de LGPD/GDPR, incluindo consentimento, exclusão e portabilidade de dados pessoais. |

### Privacidade

- O usuário deve poder solicitar exportação dos dados pessoais autorizados.
- O usuário deve poder solicitar exclusão/anomização de conta, respeitando retenção mínima de registros de auditoria quando legalmente necessária.
- Dados pessoais devem ser minimizados; identificadores em logs devem ser pseudonimizados quando possível.

---

# Rastreabilidade Técnica

| Necessidade | Solução arquitetural prevista |
|---|---|
| API REST versionada | Spring Boot, OpenAPI e endpoints sob `/api/v1` |
| Tempo real | WebSocket com JWT no handshake, eventos ordenados e recuperação por snapshot |
| Execução assíncrona | RabbitMQ, worker Java e transactional outbox |
| Isolamento de código | Containers Docker descartáveis, sem rede, com limites de CPU/memória/PID/tempo |
| Dados relacionais | PostgreSQL com migrations Flyway, índices, joins, window functions, triggers e procedures |
| Cache/controle temporário | Redis para rate limit, presença, locks curtos e cache de ranking |
| Autorização | Spring Security, RBAC e políticas ABAC por atributos de usuário/recurso/contexto |
| Vetores/IA | pgvector inicialmente; worker de embeddings e recomendação híbrida |
| Observabilidade | Actuator, Micrometer, Prometheus, Grafana, logs JSON e OpenTelemetry |
| Escala | Docker inicialmente; Kubernetes, HPA, read replica e consistent hashing em fase avançada |

# Critérios globais de aceite

- Um visitante consegue entrar como convidado, criar uma sala privada e compartilhar convite.
- Dois jogadores em navegadores distintos conseguem jogar a mesma partida e ver atualizações sincronizadas sem recarregar a página.
- Uma submissão Java é recebida, processada fora da API em sandbox isolado e atualiza o placar em tempo real.
- Uma execução maliciosa, travada ou excessiva é encerrada sem acesso à rede e sem comprometer host ou demais usuários.
- A repetição da mesma operação idempotente não cria submissões, resultados, notificações ou atualizações de rating duplicadas.
- A finalização de uma partida atualiza resultados, histórico, rating e auditoria de maneira atômica.
- Ranking, replay e histórico não expõem código durante uma partida nem casos de teste privados.
- O repositório fornece documentação arquitetural, instruções de execução local, contrato OpenAPI, migrations e pipeline de CI.

# Documentos relacionados

- [Arquitetura C4](architecture/arenacode-c4-architecture.md)
- [Diagrama de Classes](architecture/arenacode-class-diagram.md)
- [Roadmap](../README.md#roadmap)
- [Issues do projeto](https://github.com/AnDrELuIzzz/arenacode/issues)

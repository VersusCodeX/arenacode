# Desenvolvimento Local - ArenaCode Backend

## Endpoint tecnico vs. Actuator

O backend expoe dois mecanismos distintos de verificacao de saude, com propositos diferentes:

| Aspecto | Endpoint tecnico (`/api/v1/health`) | Spring Boot Actuator (`/actuator/health`) |
|---------|--------------------------------------|---------------------------------------------|
| Objetivo | Verificacao simples de que a aplicacao esta viva, para consumidores externos leves. | Verificacao completa de saude, incluindo dependencias (banco, disco, etc.), usada por orquestradores. |
| Detalhes expostos | Apenas `application`, `status` e `timestamp`. Nenhum detalhe de infraestrutura. | Estado agregado por padrao (`show-details: never`); componentes individuais (banco, disco) nao sao expostos publicamente. |
| Formato | DTO customizado (`HealthCheckResponse`). | Formato padrao do Spring Boot Actuator. |
| Substitui o outro? | Nao. E um endpoint complementar, mais simples. | Nao. Continua sendo a fonte oficial e completa de health checks. |
| Uso recomendado | Smoke tests simples, scripts, verificacoes rapidas. | Probes de liveness/readiness em orquestradores, monitoramento operacional. |

## Exemplos de uso (curl)

### Endpoint tecnico

```bash
curl http://localhost:8080/api/v1/health
```

Resposta esperada:

```json
{
  "application": "arenacode",
  "status": "UP",
  "timestamp": "2026-09-16T21:48:00.123456Z"
}
```

### Actuator - saude geral

```bash
curl http://localhost:8080/actuator/health
```

Retorna `200 OK` com `{"status":"UP"}` quando a aplicacao e suas dependencias essenciais (como o PostgreSQL) estao disponiveis.

### Actuator - liveness

```bash
curl http://localhost:8080/actuator/health/liveness
```

### Actuator - readiness

```bash
curl http://localhost:8080/actuator/health/readiness
```

## O que significam liveness e readiness

- **Liveness** (`/actuator/health/liveness`): indica se o processo da aplicacao esta em execucao e nao travado (deadlock, loop infinito, etc.). Se ficar `DOWN`, o orquestrador (ex.: Kubernetes) deve reiniciar o container/processo. Nao depende do banco de dados — reflete apenas o estado interno (`livenessState`) da JVM/Spring.
- **Readiness** (`/actuator/health/readiness`): indica se a aplicacao esta pronta para receber trafego. Inclui o estado interno (`readinessState`) **e** a disponibilidade do PostgreSQL (indicador `db`). Se o banco cair, a readiness fica `DOWN` e o orquestrador deve parar de rotear requisicoes para esta instancia, sem necessariamente reinicia-la.

Essa distincao evita reinicios inuteis quando o problema e uma dependencia externa (banco fora do ar), e nao a aplicacao em si.

## Endpoints do Actuator expostos

Apenas os seguintes endpoints estao habilitados (`management.endpoints.web.exposure.include`):

- `health` (com grupos `liveness` e `readiness`)
- `info`

**Nao estao expostos**: `env`, `beans`, `configprops`, `heapdump`, `threaddump`, `loggers`, `metrics` ou `prometheus`. Metricas via Prometheus poderao ser habilitadas em um commit futuro, quando a dependencia Micrometer/Prometheus for adicionada explicitamente.

## Comandos de validacao

```bash
./gradlew test
./gradlew bootRun

curl http://localhost:8080/api/v1/health
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

Os testes de integracao (`HealthEndpointsIntegrationTest`) usam Testcontainers para validar esses mesmos endpoints com um PostgreSQL real, sem exigir instalacao manual do banco.

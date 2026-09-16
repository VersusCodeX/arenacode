# Roteiro de demonstração: Fundação

Este roteiro demonstra a base técnica entregue pelo épico Fundação. Ele não apresenta funcionalidades de domínio ainda não implementadas.

## Objetivo

Demonstrar que um novo desenvolvedor consegue iniciar o backend, subir o PostgreSQL, aplicar migrations Flyway, consultar health checks e OpenAPI e verificar os checks de qualidade e CI.

## Preparação

Na raiz do repositório:

```bash
cp .env.example .env
docker compose up -d postgres
docker compose ps
```

Mostre que o PostgreSQL está em execução.

## Demonstração

### 1. Executar qualidade e testes

```bash
./gradlew spotlessCheck
./gradlew test
./gradlew spotbugsMain
./gradlew bootJar
```

Resultado esperado: todos terminam com `BUILD SUCCESSFUL`.

### 2. Iniciar o backend

```bash
./gradlew bootRun
```

No log, destaque a execução do Flyway. As migrations versionadas criam ou atualizam o schema local.

### 3. Confirmar PostgreSQL e Flyway

Em outro terminal:

```bash
docker compose ps
```

Opcionalmente, conecte-se via DBeaver usando as variáveis de `.env` e mostre o schema e `flyway_schema_history`.

### 4. Consultar health checks

```bash
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/actuator/health
```

Resultado esperado: respostas HTTP de sucesso.

### 5. Abrir Swagger

Abra <http://localhost:8080/swagger-ui.html>.

Mostre os endpoints técnicos disponíveis. Não apresente autenticação, problemas, partidas, submissões ou sandbox como entregas atuais.

### 6. Mostrar CI

Abra <https://github.com/VersusCodeX/arenacode/actions> e selecione uma execução do workflow **Backend CI**.

Destaque os checks:

- `spotlessCheck`
- `test`
- `spotbugsMain`
- `bootJar`

O workflow não executa deploy, publicação de imagem, uso de secrets ou integração com cloud.

## Encerramento

Pare a aplicação com `Ctrl+C` e encerre o banco:

```bash
docker compose down
```

Para descartar dados locais e demonstrar a aplicação das migrations do zero:

```bash
docker compose down -v
```

## Limites da Fundação

Autenticação, problemas, partidas, submissões/julgamento, worker/sandbox e frontend são entregas dos próximos épicos.

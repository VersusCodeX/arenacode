# CI/CD - ArenaCode Backend

## Visao geral

O pipeline de CI no GitHub Actions executa automaticamente build, testes, formatacao e analise estatica em cada push para `main` e em pull requests direcionados a `main`.

## Workflow

Arquivo: `.github/workflows/backend-ci.yml`

Badge de status no topo do [README.md](../../README.md).

## Gatilhos

| Evento | Condicao |
|--------|----------|
| `push` | Branch `main` |
| `pull_request` | Branch alvo `main` |

## Checks executados

1. **Spotless** (`./gradlew spotlessCheck`) — valida formatacao do codigo.
2. **Testes** (`./gradlew test`) — testes unitarios e de integracao (Testcontainers).
3. **SpotBugs** (`./gradlew spotbugsMain`) — analise estatica de bugs.
4. **Build JAR** (`./gradlew bootJar`) — valida que o projeto compila e gera artefato executavel.

### Por que nao rodar `./gradlew check`?

O comando `check` executa internamente `spotlessCheck`, `spotbugsMain` e `test`. Como o workflow ja roda cada um separadamente, rodar `check` duplicaria trabalho sem beneficio. A estrategia atual:

- Mantem passos independentes para melhor visibilidade no GitHub Actions.
- Permite upload de artefatos especificos por tipo de falha.
- Evita execucao redundante de tarefas.

## Ambiente

- Runner: `ubuntu-latest`
- JDK: 21 (Temurin)
- Cache: Gradle (habilitado via `actions/setup-java`)
- Docker: disponivel no runner (necessario para Testcontainers)
- Gradle Wrapper: validado com `gradle/actions/wrapper-validation@v4`

## Testcontainers

O runner do GitHub Actions ja inclui Docker. Os testes de integracao usam Testcontainers para subir PostgreSQL e outros servicos sob demanda — **nao e necessario** declarar servicos no YAML do workflow.

## Artefatos (em caso de falha)

| Artefato | Caminho | Retencao |
|----------|---------|----------|
| Test reports | `build/reports/tests/` | 7 dias |
| SpotBugs report | `build/reports/spotbugs/main/` | 7 dias |
| Gradle build report | `build/reports/` | 7 dias |

Para baixar: acesse a execucao do workflow no GitHub → secao "Artifacts".

## Reproduzir localmente

Para validar antes de push:

```bash
# Formatacao
./gradlew spotlessCheck

# Testes
./gradlew test

# Analise estatica
./gradlew spotbugsMain

# Build
./gradlew bootJar
```

Ou, para rodar tudo de uma vez (porem menos visibilidade):

```bash
./gradlew check
```

## Onde encontrar relatorios no GitHub Actions

1. Acesse https://github.com/VersusCodeX/arenacode/actions
2. Clique na execucao do workflow (ex.: "Backend CI #123")
3. Expanda o passo que falhou para ver o log completo
4. Se houver falha, baixe os artefatos na secao "Artifacts" no final da pagina

## Permissoes

O workflow usa permissoes minimas (`contents: read`). Nao ha tokens personalizados, secrets ou permissoes de escrita.

## Proximos passos (fora do escopo atual)

- Integrar relatorios SpotBugs com dashboard (ex.: SonarQube, Codecov).
- Adicionar cache de dependencias mais agressivo (Gradle `--build-cache`).
- Publicar imagem Docker em registry (CD).
- Deploy automatico em ambiente de staging/producao.

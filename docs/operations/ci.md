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
2. **Testes** (`./gradlew test`) — testes unitarios e de integracao (PostgreSQL do service container).
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
- PostgreSQL: service container `postgres:16-alpine` em `localhost:5432`
- Gradle Wrapper: validado com `gradle/actions/wrapper-validation@v4`

## Banco de dados nos testes

Os testes de integracao usam o perfil `test`, que conecta em um PostgreSQL em `localhost:5432` com as credenciais de desenvolvimento do `docker-compose.yml`. No CI, esse banco e um service container declarado no workflow, com health check para os testes so comecarem quando ele estiver pronto. Localmente, use o PostgreSQL do `docker-compose.yml`.

## Artefatos (em caso de falha)

| Artefato | Caminho | Retencao |
|----------|---------|----------|
| Test reports | `build/reports/tests/` | 7 dias |
| SpotBugs report | `build/reports/spotbugs/main/` | 7 dias |
| Gradle build report | `build/reports/` | 7 dias |

Para baixar: acesse a execucao do workflow no GitHub → secao "Artifacts".

## Reproduzir localmente

Para validar antes de push (com o PostgreSQL do `docker-compose.yml` rodando, necessario para os testes):

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

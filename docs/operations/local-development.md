# Desenvolvimento local

Este guia permite iniciar o backend ArenaCode localmente a partir de um clone limpo do repositório. O épico Fundação entrega infraestrutura, migrations, health checks, OpenAPI, qualidade e CI; autenticação, problemas, partidas, submissões/julgamento e sandbox ainda pertencem aos próximos épicos.

## Pré-requisitos

Instale ou tenha acesso a:

- JDK 21.
- Docker Engine em execução.
- Docker Compose (`docker compose`).
- Git.
- IntelliJ IDEA ou VS Code, opcionalmente com suporte a Java/Gradle.
- DBeaver, opcional, para inspecionar o PostgreSQL.
- Bruno ou Postman, opcionais, para testar endpoints HTTP.

Verifique o ambiente:

```bash
java -version
docker --version
docker compose version
git --version
```

## Início rápido

1. Clone o repositório e entre no diretório:

```bash
git clone git@github.com:VersusCodeX/arenacode.git
cd arenacode
```

2. Crie o arquivo de ambiente local a partir do exemplo:

```bash
cp .env.example .env
```

3. Inicie apenas o PostgreSQL:

```bash
docker compose up -d postgres
docker compose ps
```

4. Execute os testes:

```bash
./gradlew test
```

5. Inicie a aplicação:

```bash
./gradlew bootRun
```

6. Em outro terminal, valide os endpoints:

```bash
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/actuator/health
```

Abra também o Swagger UI em <http://localhost:8080/swagger-ui.html>.

## Encerramento

Pare a aplicação iniciada com `bootRun` usando `Ctrl+C`.

Para parar containers e preservar os dados:

```bash
docker compose down
```

Para remover containers e volumes/dados locais:

```bash
docker compose down -v
```

Use `down -v` somente quando quiser reinicializar completamente o banco local.

## DBeaver

Com o PostgreSQL iniciado, crie uma conexão PostgreSQL usando os valores definidos no `.env` e no `docker-compose.yml`. Em uma configuração local comum:

- Host: `localhost`
- Porta: `5432`
- Database, usuário e senha: valores do `.env`

No DBeaver:

1. Abra **Database > New Database Connection > PostgreSQL**.
2. Informe os dados da configuração local.
3. Clique em **Test Connection**.
4. Expanda o database e os schemas para visualizar tabelas e a tabela de histórico do Flyway.

O DBeaver deve ser usado para inspeção e consulta. Não crie mudanças permanentes de schema manualmente: toda mudança deve ser uma migration Flyway versionada.

## Validar Flyway

O Flyway executa migrations pendentes durante a inicialização da aplicação.

1. Inicie o PostgreSQL:

```bash
docker compose up -d postgres
```

2. Execute:

```bash
./gradlew bootRun
```

3. Confirme no log que o Flyway concluiu sem erros.
4. Consulte `GET /actuator/health`.
5. Opcionalmente, no DBeaver, consulte a tabela `flyway_schema_history`.

Após `docker compose down -v`, o próximo `bootRun` cria novamente o banco local e reaplica as migrations.

## Qualidade e testes

Execute os checks usados pelo CI:

```bash
./gradlew spotlessCheck
./gradlew spotbugsMain
./gradlew test
./gradlew check
```

Caso o Spotless aponte formatação inválida, corrija automaticamente:

```bash
./gradlew spotlessApply
```

Os relatórios ficam em `build/reports/`.

## Troubleshooting

### Porta 5432 ocupada

Verifique qual processo ou container usa a porta:

```bash
ss -ltnp | grep 5432
docker ps
```

Pare o serviço concorrente ou ajuste sua configuração local.

### Docker sem permissão

Confirme que o Docker está em execução e que seu usuário pode acessar o socket. Em Linux:

```bash
sudo usermod -aG docker "$USER"
```

Saia da sessão e entre novamente após executar o comando. Evite usar `sudo` como solução permanente para Gradle ou Docker.

### Java errado

Confirme as versões:

```bash
java -version
./gradlew -version
```

O projeto requer JDK 21. Ajuste `JAVA_HOME` ou a SDK configurada na IDE.

### Flyway falhando

Leia a primeira mensagem de erro do Flyway. Confirme que o PostgreSQL está disponível, que `.env` corresponde à configuração local e que não houve alterações manuais no schema.

Para reinicializar dados locais descartáveis:

```bash
docker compose down -v
docker compose up -d postgres
./gradlew bootRun
```

### Banco indisponível

Verifique estado e logs:

```bash
docker compose ps
docker compose logs postgres
```

Confirme host, porta, database, usuário e senha antes de reiniciar a aplicação.

### Gradle Wrapper sem permissão

Em sistemas Unix:

```bash
chmod +x gradlew
./gradlew --version
```

No Windows, use `gradlew.bat`.

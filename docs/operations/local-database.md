# Banco local com PostgreSQL

## Objetivo

O Docker Compose fornece um PostgreSQL 16 reproduzível para o desenvolvimento local do ArenaCode Backend, sem exigir a instalação do PostgreSQL diretamente no sistema operacional. O container usa o volume nomeado `postgres_data`, permitindo preservar os dados entre reinicializações.

As variáveis de conexão são lidas do arquivo `.env` local. Se uma variável não estiver definida, o Compose usa os valores de fallback destinados exclusivamente ao desenvolvimento local.

## Comandos principais

Validar a configuração renderizada pelo Docker Compose:

```bash
docker compose config
```

Iniciar somente o PostgreSQL:

```bash
docker compose up -d postgres
```

Verificar o estado do serviço e o healthcheck:

```bash
docker compose ps
```

Acompanhar os logs do PostgreSQL:

```bash
docker compose logs -f postgres
```

Parar e remover o container, preservando os dados do volume nomeado:

```bash
docker compose down
```

Parar e remover o container e também o volume `postgres_data`:

```bash
docker compose down -v
```

`docker compose down` preserva os dados porque remove os containers e a rede, mas mantém o volume nomeado. Já `docker compose down -v` remove também o volume e todos os dados locais do banco; use-o quando precisar de uma recriação limpa.

## Conexão via terminal

Substitua `<usuario>` e `<banco>` pelos valores de `POSTGRES_USER` e `POSTGRES_DB` definidos no `.env`:

```bash
docker compose exec postgres psql -U <usuario> -d <banco>
```

Por exemplo, usando os fallbacks padrão:

```bash
docker compose exec postgres psql -U arenacode -d arenacode
```

Dentro do `psql`, a consulta abaixo confirma a versão do servidor:

```sql
SELECT version();
```

## Conexão pelo DBeaver

Use os valores configurados no `.env` local:

- Host: `localhost`
- Porta: valor de `POSTGRES_PORT`
- Banco: valor de `POSTGRES_DB`
- Usuário: valor de `POSTGRES_USER`
- Senha: valor de `POSTGRES_PASSWORD`

O DBeaver pode ser usado para inspeção do banco, queries manuais, testes e execução de `EXPLAIN ANALYZE` durante a investigação de consultas.

## Alterações de schema

Alterações permanentes de schema devem ser feitas exclusivamente por migrations Flyway, que são a fonte oficial de criação e evolução do banco. Não aplique alterações permanentes manualmente apenas pelo DBeaver, pois isso deixa o schema divergente do histórico versionado do projeto.

# OpenAPI (Swagger) - ArenaCode Backend

## Visao geral

O backend expoe sua documentacao de API no formato OpenAPI 3.0, gerada automaticamente pelo **Springdoc OpenAPI**.

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| OpenAPI YAML | http://localhost:8080/v3/api-docs.yaml |

## Configuracao

A configuracao esta em `application.yaml` (`springdoc.*`) e no bean `OpenApiConfig`.

- **Titulo**: ArenaCode API
- **Versao**: 0.1.0
- **Descricao**: API REST do backend do ArenaCode, uma plataforma competitiva de programacao.
- **Licenca**: A definir (MIT provavel)
- **Servidor local**: http://localhost:8080
- **Esquema de seguranca**: Bearer JWT (documentado, mas **nao exigido** nos endpoints tecnicos atuais)

## Endpoints tecnicos documentados

### GET /api/v1/health

Endpoint tecnico de saude. Retorna um DTO simples com `application`, `status` e `timestamp`.

**Respostas**:
- `200 OK`: Saude da aplicacao.

### POST /api/v1/validation-example

Endpoint de demonstracao de validacao de entrada.

**Requisicao**:
```json
{
  "name": "exemplo",
  "quantity": 10
}
```

**Respostas**:
- `204 No Content`: Payload valido.
- `400 Bad Request` (Problem Details): Payload invalido.

## Respostas de erro (Problem Details, RFC 9457)

Todos os erros da API seguem o formato `application/problem+json`, com os seguintes campos:

| Campo | Tipo | Descricao |
|-------|------|------------|
| `type` | URI | Identificador do tipo de problema (ex.: `https://arenacode.dev/problems/validation-error`). |
| `title` | string | Titulo curto do problema. |
| `status` | number | Codigo HTTP correspondente. |
| `detail` | string | Descricao legivel do erro. |
| `timestamp` | string (ISO-8601) | Instante do erro. |
| `violations` | array (opcional) | Lista de violacoes de campo (para erros de validacao). |

### Codigos de erro padronizados

| Codigo | Cenario | Tipo de problema |
|--------|---------|------------------|
| 400 | Payload invalido (Bean Validation) | `validation-error` |
| 404 | Recurso nao encontrado | `resource-not-found` |
| 409 | Conflito de estado | `conflict` |
| 422 | Violacao de regra de negocio | `business-rule-violation` |
| 500 | Erro interno inesperado | `internal-server-error` |

### Exemplo de resposta 400

```json
{
  "type": "https://arenacode.dev/problems/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Falha de validacao de entrada",
  "timestamp": "2026-09-16T19:15:00.123456Z",
  "violations": [
    {
      "field": "name",
      "message": "name e obrigatorio"
    }
  ]
}
```

## Proximos passos

- Adicionar endpoints de dominio (usuarios, problemas, partidas, submissoes) com documentacao OpenAPI.
- Implementar autenticacao JWT e exigir o esquema `Bearer Authentication` nos endpoints protegidos.
- Refinar tipos de problema e mensagens conforme a evolucao da API.

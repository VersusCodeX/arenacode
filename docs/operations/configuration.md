# Configuração - Variáveis de Ambiente

## JWT

| Variável | Descrição | Exemplo | Obrigatório |
|-----------|------------|---------|------------|
| `JWT_SECRET` | Chave secreta para assinar/verificar tokens JWT | `minha-chave-secreta-forte` | Sim (exceto dev) |
| `APP_JWT_ISSUER` | Issuer do token JWT | `arenacode.dev` | Não (padrão: arenacode.dev) |
| `app.jwt.access-token-ttl` | TTL do access token | `15m` | Não (padrão: 15 min) |

## Desenvolvimento

Em `dev`, se `JWT_SECRET` não for informado, será usado um valor fixo identificado como desenvolvimento. **Não use em produção**.

## Produção

Defina `JWT_SECRET` com um valor forte e único por ambiente.

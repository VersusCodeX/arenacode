# Autenticação JWT

## Visão geral

A API ArenaCode usa tokens JWT Bearer para autenticação stateless.

## Login

### POST /api/v1/auth/login

**Request:**
```json
{"email": "usuario@exemplo.com", "password": "Senha123!"}
```

**Response 200:**
```json
{"accessToken": "eyJhbG...", "tokenType": "Bearer", "expiresIn": 900, "id": "uuid", "email": "usuario@exemplo.com", "displayName": "Usuario", "status": "ACTIVE", "roles": ["USER"]}
```

**Respostas de erro:**
- `400`: Payload inválido
- `401`: Credenciais inválidas (genérico, não revela se e-mail existe)
- `403`: Conta desabilitada (SUSPENDED, BANNED, DELETED)

## Uso do token

Header: `Authorization: Bearer <token>`

## GET /api/v1/me

Retorna perfil do usuário autenticado.

**Response 200:**
```json
{"id": "uuid", "email": "usuario@exemplo.com", "displayName": "Usuario", "status": "ACTIVE", "roles": ["USER"]}
```

**Respostas de erro:**
- `401`: Não autenticado ou token inválido/expirado

## Claims do token

- `sub`: userId (UUID string)
- `iss`: issuer (ex: arenacode.dev)
- `iat`: issue time
- `exp`: expiração (15 min)
- `email`: e-mail
- `displayName`: nome
- `roles`: ["USER", ...]

## Segurança

- Token expira em 15 minutos.
- Usuários com status SUSPENDED, BANNED ou DELETED não autenticam.
- JWT_SECRET obrigatório em produção.

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
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "usuario@exemplo.com",
    "displayName": "Usuario",
    "status": "ACTIVE",
    "roles": ["USER"]
  }
}
```

**Respostas de erro:**
- `400`: Payload inválido
- `401`: Credenciais inválidas (genérico, não revela se e-mail existe)
- `403`: Conta desabilitada (SUSPENDED, BANNED, DELETED)

## Sessão de convidado (guest)

### POST /api/v1/auth/guest

Permite que um visitante experimente o ArenaCode sem cadastro, criando um usuário temporário com
status `GUEST` e obtendo um access token JWT de curta duração. Pensado para demos públicas: o
convidado entra em uma sala privada e participa de uma batalha como jogador comum (papel `USER`),
sem acesso a nenhum recurso administrativo.

**Request (corpo opcional):**
```json
{"displayName": "Visitante Curioso"}
```

- `displayName` é opcional, com no máximo 80 caracteres.
- Se omitido (ou em branco), o servidor gera um nome no formato `Guest-XXXX` (número aleatório não
  sensível).
- Corpo da requisição pode ser omitido inteiramente (`POST` sem payload).

**Response 201:**
```json
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "user": {
    "id": "uuid",
    "displayName": "Guest-4821",
    "status": "GUEST",
    "roles": ["USER"],
    "isGuest": true
  }
}
```

Retorna `201 Created` (e não `200`) porque a chamada efetivamente cria um novo registro em
`users`, assim como `POST /api/v1/auth/register`.

**Respostas de erro:**
- `400`: `displayName` inválido (maior que 80 caracteres)

**Regras do usuário convidado:**
- `status = GUEST`, `is_guest = true`, `email = null`, `password_hash = null`.
- `current_rating = 1000` e `preferred_language = JAVA_21` (mesmos defaults de um usuário
  registrado).
- Recebe apenas o papel `USER` — nunca `ADMIN` ou `MODERATOR`.
- Não é gerado refresh token para sessões de convidado.
- O token expira em 2 horas (bem mais curto que o token de 15 minutos do login com senha, mas
  suficiente para uma demo/partida completa).
- A criação do usuário e a atribuição do papel `USER` ocorrem na mesma transação: se algo falhar,
  nenhum registro parcial fica salvo.

**Retenção de dados (nota para operação):**
- Convidados são temporários por natureza. Esta versão **não** implementa exclusão automática ou
  anonimização do registro.
- Uma futura rotina de limpeza deve tratar linhas de `users` com `is_guest = true` como candidatas
  a remoção ou anonimização após um período de inatividade.
- Nenhum dado pessoal além do `displayName` (que o próprio visitante escolhe) é coletado nesse
  fluxo — sem e-mail, sem IP persistente.

## Uso do token

Header: `Authorization: Bearer <token>`

## GET /api/v1/me

Retorna perfil do usuário autenticado. Funciona também para convidados autenticados com o token
emitido por `POST /api/v1/auth/guest`.

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
- `exp`: expiração (15 min para login normal, 2h para sessão de convidado)
- `displayName`: nome
- `roles`: ["USER", ...]
- `isGuest`: `true` apenas em tokens emitidos por `POST /api/v1/auth/guest`; ausente/`false` em
  tokens de login normal.

## Segurança

- Token expira em 15 minutos (login normal) ou 2 horas (sessão de convidado).
- Usuários com status SUSPENDED, BANNED ou DELETED não autenticam.
- JWT_SECRET obrigatório em produção.

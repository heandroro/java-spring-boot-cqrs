# API Reference - Orders Service

Documentação completa dos endpoints REST do Orders Service.

## 📋 Índice

- [Base URL](#base-url)
- [Autenticação](#autenticação)
- [Endpoints](#endpoints)
  - [POST /orders](#1-criar-pedido)
  - [GET /orders](#2-listar-pedidos)
  - [GET /orders/{id}](#3-obter-pedido-por-id)
- [Tratamento de Erros](#tratamento-de-erros)
- [Paginação](#paginação)

---

## Base URL

```
http://localhost:8080
```

**Swagger UI**: http://localhost:8080/swagger-ui.html  
**OpenAPI Docs**: http://localhost:8080/v3/api-docs

---

## Autenticação

Todos os endpoints requerem autenticação JWT via header `Authorization`:

```
Authorization: Bearer <JWT_TOKEN>
```

### Payload JWT Esperado

```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "scopes": ["orders:read", "orders:write"],
  "iat": 1703084400,
  "exp": 1703170800
}
```

### Escopos

- **orders:read** - Ler pedidos
- **orders:write** - Criar pedidos
- **admin** - Permissões administrativas (ver todos os pedidos)

### Regras de Autorização

- **Usuários comuns**: Veem apenas seus próprios pedidos
- **Admins**: Veem todos os pedidos
- **customerId** é extraído do JWT token automaticamente

---

## Endpoints

### 1. Criar Pedido

Cria um novo pedido para o cliente autenticado.

**Endpoint**: `POST /orders`  
**Scope Requerido**: `orders:write`  
**Database**: PostgreSQL Primary (Write)

#### Request

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "productId": "p123",
        "quantity": 2,
        "pricePerUnit": 99.99
      },
      {
        "productId": "p456",
        "quantity": 1,
        "pricePerUnit": 49.99
      }
    ]
  }'
```

#### Request Body

```json
{
  "customerId": "string (UUID)",
  "items": [
    {
      "productId": "string",
      "quantity": "integer (>= 1)",
      "pricePerUnit": "number (> 0)"
    }
  ]
}
```

#### Response (201 Created)

```json
{
  "id": "ord-550e8400-e29b-41d4-a716-446655440000",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "pending",
  "total": 249.97,
  "items": [
    {
      "productId": "p123",
      "quantity": 2,
      "pricePerUnit": 99.99,
      "subtotal": 199.98
    },
    {
      "productId": "p456",
      "quantity": 1,
      "pricePerUnit": 49.99,
      "subtotal": 49.99
    }
  ],
  "createdAt": "2026-02-25T10:30:00Z"
}
```

#### Validações

- ✅ Pedido deve ter pelo menos 1 item
- ✅ Quantidade de cada item deve ser >= 1
- ✅ Preço unitário deve ser > 0
- ✅ Total é calculado automaticamente
- ✅ Status inicial é sempre "pending"
- ✅ customerId deve corresponder ao usuário autenticado

#### Erros Possíveis

- **400 VALIDATION_ERROR**: Items vazios, quantidade inválida, preço inválido
- **401 AUTHENTICATION_ERROR**: JWT token inválido ou ausente
- **403 AUTHORIZATION_ERROR**: customerId não corresponde ao token

---

### 2. Listar Pedidos

Lista pedidos com paginação e filtros opcionais.

**Endpoint**: `GET /orders`  
**Scope Requerido**: `orders:read`  
**Database**: PostgreSQL Replica (Read)

#### Request

```bash
curl -X GET "http://localhost:8080/orders?limit=20&offset=0&status=pending" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

#### Query Parameters

| Parâmetro | Tipo | Obrigatório | Default | Descrição |
|-----------|------|-------------|---------|-----------|
| `limit` | integer | Não | 20 | Número de resultados (máx: 100) |
| `offset` | integer | Não | 0 | Offset para paginação |
| `status` | string | Não | - | Filtrar por status (pending, confirmed, shipped, delivered) |

#### Response (200 OK)

```json
{
  "data": [
    {
      "id": "ord-1",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "pending",
      "total": 249.97,
      "items": [],
      "createdAt": "2026-02-25T10:30:00Z"
    }
  ],
  "totalCount": 42,
  "limit": 20,
  "offset": 0
}
```

#### Regras de Autorização

- **Usuário comum**: Vê apenas seus próprios pedidos
- **Admin**: Vê todos os pedidos do sistema

#### Ordenação

Resultados ordenados por `createdAt DESC` (mais novo primeiro)

#### Erros Possíveis

- **400 VALIDATION_ERROR**: Limit > 100 ou offset < 0
- **401 AUTHENTICATION_ERROR**: JWT token inválido ou ausente

---

### 3. Obter Pedido por ID

Busca um pedido específico por ID.

**Endpoint**: `GET /orders/{orderId}`  
**Scope Requerido**: `orders:read`  
**Database**: PostgreSQL Replica (Read)

#### Request

```bash
curl -X GET http://localhost:8080/orders/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

#### Path Parameters

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `orderId` | UUID | ID do pedido |

#### Response (200 OK)

```json
{
  "id": "ord-550e8400-e29b-41d4-a716-446655440000",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "confirmed",
  "total": 249.97,
  "items": [
    {
      "productId": "p123",
      "quantity": 2,
      "pricePerUnit": 99.99,
      "subtotal": 199.98
    }
  ],
  "createdAt": "2026-02-25T10:30:00Z"
}
```

#### Regras de Autorização

- **Usuário comum**: Pode ver apenas seus próprios pedidos
- **Admin**: Pode ver qualquer pedido

#### Erros Possíveis

- **401 AUTHENTICATION_ERROR**: JWT token inválido ou ausente
- **403 AUTHORIZATION_ERROR**: Usuário não tem permissão para ver este pedido
- **404 NOT_FOUND**: Pedido não existe

---

## Tratamento de Erros

Todos os erros seguem formato estruturado:

### Formato de Erro

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid input data",
  "path": "/orders",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "details": [
    "items: Items cannot be empty"
  ]
}
```

### Códigos de Erro

| Código | HTTP Status | Descrição |
|--------|-------------|-----------|
| `VALIDATION_ERROR` | 400 | Entrada inválida (validação falhou) |
| `AUTHENTICATION_ERROR` | 401 | JWT token ausente ou inválido |
| `AUTHORIZATION_ERROR` | 403 | Usuário não tem permissão |
| `NOT_FOUND` | 404 | Recurso não encontrado |
| `RATE_LIMIT_EXCEEDED` | 429 | Rate limit excedido |
| `INTERNAL_ERROR` | 500 | Erro interno do servidor |

### Exemplos de Erros

#### 400 - Validation Error

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid order data",
  "path": "/orders",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "abc-123",
  "details": [
    "items: Items cannot be empty",
    "items[0].quantity: must be greater than or equal to 1"
  ]
}
```

#### 401 - Authentication Error

```json
{
  "code": "AUTHENTICATION_ERROR",
  "message": "JWT token is missing or invalid",
  "path": "/orders",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "abc-123"
}
```

#### 403 - Authorization Error

```json
{
  "code": "AUTHORIZATION_ERROR",
  "message": "You don't have permission to access this order",
  "path": "/orders/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "abc-123"
}
```

#### 404 - Not Found

```json
{
  "code": "NOT_FOUND",
  "message": "Order not found",
  "path": "/orders/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "abc-123"
}
```

---

## Paginação

### Parâmetros de Paginação

- **limit**: Número de resultados por página (default: 20, máximo: 100)
- **offset**: Número de registros a pular (default: 0)

### Exemplo de Resposta Paginada

```json
{
  "data": [...],
  "totalCount": 150,
  "limit": 20,
  "offset": 40
}
```

### Cálculo de Páginas

```javascript
// Página atual
const currentPage = Math.floor(offset / limit) + 1;

// Total de páginas
const totalPages = Math.ceil(totalCount / limit);

// Próxima página
const nextOffset = offset + limit;

// Página anterior
const prevOffset = Math.max(0, offset - limit);
```

### Navegação

```bash
# Primeira página
GET /orders?limit=20&offset=0

# Segunda página
GET /orders?limit=20&offset=20

# Terceira página
GET /orders?limit=20&offset=40
```

---

## Status de Pedidos

| Status | Descrição |
|--------|-----------|
| `pending` | Pedido criado, aguardando confirmação |
| `confirmed` | Pedido confirmado |
| `shipped` | Pedido enviado |
| `delivered` | Pedido entregue |

**Nota**: Atualmente apenas `pending` é usado. Outros status serão implementados em versões futuras.

---

## Rate Limiting

**Nota**: Rate limiting não está implementado na versão atual (v2.0.0). Planejado para v2.1.

---

## Documentação Relacionada

- 🏗️ [Arquitetura](ARCHITECTURE.md) - CQRS e diagramas
- 🔐 [Segurança](SECURITY.md) - JWT e autorizações
- 💻 [Desenvolvimento](DEVELOPMENT.md) - Como testar a API
- 🗄️ [Database](DATABASE.md) - Schema e dados

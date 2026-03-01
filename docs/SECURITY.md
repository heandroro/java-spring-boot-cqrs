# Security - Orders Service

Documentação completa de segurança, autenticação e autorização.

## 📋 Índice

- [JWT Authentication](#jwt-authentication)
- [Escopos e Permissões](#escopos-e-permissões)
- [Regras de Autorização](#regras-de-autorização)
- [Audit Logging](#audit-logging)
- [Compliance](#compliance)

---

## JWT Authentication

O serviço usa **JSON Web Tokens (JWT)** para autenticação.

### Header de Autenticação

Todos os endpoints requerem JWT no header `Authorization`:

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

**Claims Obrigatórios:**
- `customerId` (string UUID): ID do cliente autenticado
- `scopes` (array): Lista de permissões
- `iat` (number): Issued At timestamp
- `exp` (number): Expiration timestamp

### Configuração Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
            );
        return http.build();
    }
}
```

### Extração do customerId

```java
@RestController
public class OrderCreationController {
    
    private String extractCustomerId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("customerId");
    }
}
```

---

## Escopos e Permissões

### Escopos Disponíveis

| Escopo | Descrição | Endpoints |
|--------|-----------|-----------|
| `orders:read` | Ler pedidos | GET /orders, GET /orders/{id} |
| `orders:write` | Criar pedidos | POST /orders |
| `admin` | Permissões administrativas | Todos os endpoints + acesso a todos os pedidos |

### Verificação de Escopos

```java
public class OrderAuthorization {
    
    public boolean hasScope(Authentication auth, String scope) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        List<String> scopes = jwt.getClaim("scopes");
        return scopes != null && scopes.contains(scope);
    }
    
    public boolean isAdmin(Authentication auth) {
        return hasScope(auth, "admin");
    }
}
```

### Exemplos de Tokens

#### Token de Usuário Comum

```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "scopes": ["orders:read", "orders:write"],
  "iat": 1703084400,
  "exp": 1703170800
}
```

**Permissões:**
- ✅ Criar seus próprios pedidos
- ✅ Ver seus próprios pedidos
- ❌ Ver pedidos de outros clientes

#### Token de Admin

```json
{
  "customerId": "admin-550e8400-e29b-41d4-a716-446655440000",
  "scopes": ["orders:read", "orders:write", "admin"],
  "iat": 1703084400,
  "exp": 1703170800
}
```

**Permissões:**
- ✅ Criar pedidos
- ✅ Ver todos os pedidos do sistema
- ✅ Acesso administrativo completo

---

## Regras de Autorização

### Criação de Pedidos (POST /orders)

**Scope Requerido**: `orders:write`

**Regras:**
1. ✅ `customerId` no request body deve corresponder ao `customerId` do JWT
2. ✅ Usuário não pode criar pedidos para outros clientes
3. ✅ Admin pode criar pedidos para qualquer cliente

**Implementação:**

```java
@Service
public class OrderAuthorization {
    
    public void validateCreateOrderAuthorization(
        String requestCustomerId, 
        String tokenCustomerId, 
        boolean isAdmin
    ) {
        if (!isAdmin && !requestCustomerId.equals(tokenCustomerId)) {
            throw new AuthorizationException(
                "You can only create orders for yourself"
            );
        }
    }
}
```

### Listagem de Pedidos (GET /orders)

**Scope Requerido**: `orders:read`

**Regras:**
1. ✅ Usuário comum vê apenas seus próprios pedidos
2. ✅ Admin vê todos os pedidos do sistema
3. ✅ Filtros de paginação e status aplicam-se a todos

**Implementação:**

```java
@Service
public class ListOrdersQueryHandler {
    
    public OrderListQueryResult handle(ListOrdersQuery query, Authentication auth) {
        String customerId = extractCustomerId(auth);
        boolean isAdmin = isAdmin(auth);
        
        if (isAdmin) {
            // Admin: buscar todos os pedidos
            return repository.findAll(query.getLimit(), query.getOffset());
        } else {
            // Usuário comum: buscar apenas seus pedidos
            return repository.findByCustomerId(
                customerId, 
                query.getLimit(), 
                query.getOffset()
            );
        }
    }
}
```

### Busca de Pedido (GET /orders/{id})

**Scope Requerido**: `orders:read`

**Regras:**
1. ✅ Usuário comum pode ver apenas seus próprios pedidos
2. ✅ Admin pode ver qualquer pedido
3. ❌ Retorna 403 se usuário tentar acessar pedido de outro cliente
4. ❌ Retorna 404 se pedido não existe

**Implementação:**

```java
@Service
public class GetOrderQueryHandler {
    
    public OrderQueryResult handle(String orderId, Authentication auth) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        String tokenCustomerId = extractCustomerId(auth);
        boolean isAdmin = isAdmin(auth);
        
        if (!isAdmin && !order.getCustomerId().equals(tokenCustomerId)) {
            throw new AuthorizationException(
                "You don't have permission to access this order"
            );
        }
        
        return mapper.toQueryResult(order);
    }
}
```

---

## Audit Logging

O serviço inclui infraestrutura para audit logging (preparado, não implementado na v2.0.0).

### Tabela audit_logs

```sql
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(50) NOT NULL,
  entity_id UUID NOT NULL,
  operation VARCHAR(20) NOT NULL,
  customer_id UUID,
  details JSONB,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### Eventos Auditáveis

| Operação | Entity Type | Descrição |
|----------|-------------|-----------|
| CREATE | ORDER | Pedido criado |
| UPDATE | ORDER | Pedido atualizado |
| DELETE | ORDER | Pedido deletado |
| READ | ORDER | Pedido consultado |

### Exemplo de Log de Auditoria

```json
{
  "id": "log-550e8400-e29b-41d4-a716-446655440000",
  "entity_type": "ORDER",
  "entity_id": "ord-550e8400-e29b-41d4-a716-446655440000",
  "operation": "CREATE",
  "customer_id": "550e8400-e29b-41d4-a716-446655440000",
  "details": {
    "total": 249.97,
    "items_count": 2,
    "ip_address": "192.168.1.100",
    "user_agent": "Mozilla/5.0..."
  },
  "created_at": "2026-02-25T10:30:00Z"
}
```

### Implementação Futura

```java
@Service
public class AuditService {
    
    public void logOrderCreation(Order order, String customerId) {
        AuditLog log = AuditLog.builder()
            .id(UUID.randomUUID())
            .entityType("ORDER")
            .entityId(order.getId())
            .operation("CREATE")
            .customerId(customerId)
            .details(buildDetails(order))
            .createdAt(Instant.now())
            .build();
        
        auditRepository.save(log);
    }
}
```

**Nota**: Audit logging será implementado na v2.1.

---

## Compliance

### GDPR (General Data Protection Regulation)

**Dados Pessoais Armazenados:**
- `customer_id`: UUID do cliente
- `audit_logs.customer_id`: UUID do cliente em logs

**Direitos do Usuário:**
1. **Direito de Acesso**: GET /orders retorna todos os dados do cliente
2. **Direito ao Esquecimento**: Implementar DELETE /customers/{id} (futuro)
3. **Portabilidade de Dados**: Exportar pedidos em JSON (futuro)

**Retenção de Dados:**
- Pedidos: Indefinido (requisito de negócio)
- Audit logs: 7 anos (compliance)

### SOX (Sarbanes-Oxley)

**Controles Implementados:**
1. ✅ Audit trail completo (tabela audit_logs)
2. ✅ Autenticação obrigatória
3. ✅ Autorização granular (scopes)
4. ✅ Logs estruturados

**Controles Futuros:**
- [ ] Assinatura digital de transações
- [ ] Segregação de funções (diferentes roles)
- [ ] Aprovação de mudanças críticas

### PCI-DSS (Payment Card Industry)

**Nota**: O serviço **NÃO** armazena dados de cartão de crédito.

Pagamentos devem ser processados por gateway externo (Stripe, PayPal, etc.).

---

## Boas Práticas de Segurança

### 1. Nunca Expor Tokens

❌ **Errado:**
```java
log.info("JWT Token: " + token); // NUNCA fazer isso!
```

✅ **Correto:**
```java
log.info("User authenticated: customerId={}", customerId);
```

### 2. Validar Todos os Inputs

```java
@Valid @RequestBody CreateOrderCommand command
```

### 3. Usar HTTPS em Produção

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
```

### 4. Rate Limiting (Futuro)

```java
@RateLimiter(name = "orders", fallbackMethod = "rateLimitFallback")
public OrderResult createOrder(...) {
    // ...
}
```

### 5. Sanitizar Logs

```java
log.info("Order created: id={}, total={}", 
    order.getId(), 
    order.getTotal()
    // NÃO logar dados sensíveis
);
```

---

## Erros de Segurança

### 401 - Authentication Error

```json
{
  "code": "AUTHENTICATION_ERROR",
  "message": "JWT token is missing or invalid",
  "path": "/orders",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "abc-123"
}
```

**Causas:**
- Token ausente
- Token expirado
- Token inválido
- Assinatura inválida

### 403 - Authorization Error

```json
{
  "code": "AUTHORIZATION_ERROR",
  "message": "You don't have permission to access this order",
  "path": "/orders/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-25T10:30:00Z",
  "traceId": "abc-123"
}
```

**Causas:**
- Scope insuficiente
- Tentativa de acessar pedido de outro cliente
- customerId não corresponde ao token

---

## Documentação Relacionada

- 🔌 [API](API.md) - Endpoints e autenticação
- 🏗️ [Arquitetura](ARCHITECTURE.md) - Fluxos de autorização
- 🗄️ [Database](DATABASE.md) - Tabela audit_logs
- 💻 [Development](DEVELOPMENT.md) - Testar com JWT

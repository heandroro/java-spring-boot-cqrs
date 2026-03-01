# Roadmap - Orders Service

Plano de evolução do Orders Service.

## 📋 Versões

- [v2.0.0 - Atual](#v200---atual)
- [v2.1.0 - Próxima Release](#v210---próxima-release)
- [v3.0.0 - Futuro](#v300---futuro)

---

## v2.0.0 - Atual

**Data de Release**: 2026-03-01  
**Status**: ✅ **Completo**

### Features Implementadas

#### CQRS com Read Replicas
- ✅ Separação física de databases (Primary + Replica)
- ✅ Command Side → PostgreSQL Primary (R/W)
- ✅ Query Side → PostgreSQL Replica (R/O)
- ✅ Streaming Replication assíncrona
- ✅ Transaction Managers separados

#### API REST
- ✅ POST /orders - Criar pedido
- ✅ GET /orders - Listar pedidos (paginação)
- ✅ GET /orders/{id} - Buscar pedido por ID

#### Segurança
- ✅ JWT Authentication
- ✅ Scopes (orders:read, orders:write, admin)
- ✅ Autorização granular (admin vs customer)
- ✅ Spring Security 6.x

#### Qualidade
- ✅ 99% cobertura de instruções
- ✅ 95% cobertura de branches
- ✅ 106 testes (101 unitários + 5 integração)
- ✅ Validação de Mermaid diagrams

#### Documentação
- ✅ README.md macro (~300 linhas)
- ✅ docs/ARCHITECTURE.md (12 diagramas Mermaid)
- ✅ docs/API.md (endpoints completos)
- ✅ docs/DEVELOPMENT.md (setup e testes)
- ✅ docs/DATABASE.md (schema e replicação)
- ✅ docs/SECURITY.md (JWT e autorizações)
- ✅ docs/DEPLOYMENT.md (Docker e monitoring)
- ✅ docs/CQRS-READ-REPLICAS.md (guia técnico)

#### Infraestrutura
- ✅ Docker Compose (Primary + Replica + App)
- ✅ Flyway migrations
- ✅ Health checks
- ✅ Database metrics

---

## v2.1.0 - Próxima Release

**Data Estimada**: 2026-Q2  
**Status**: 🔄 **Planejado**

### Features Planejadas

#### 1. Audit Logging Completo
**Prioridade**: Alta  
**Esforço**: 3 dias

- [ ] Implementar `AuditService`
- [ ] Logar todas as operações (CREATE, UPDATE, DELETE, READ)
- [ ] Incluir IP address e user agent
- [ ] Endpoint GET /audit-logs (admin only)
- [ ] Retenção de 7 anos (compliance)

**Benefícios:**
- Compliance (GDPR, SOX)
- Debugging de mudanças
- Detecção de acessos suspeitos

#### 2. Update Order Status
**Prioridade**: Alta  
**Esforço**: 2 dias

- [ ] Endpoint PATCH /orders/{id}/status
- [ ] Transições válidas: pending → confirmed → shipped → delivered
- [ ] Validação de transições
- [ ] Autorização (apenas admin ou owner)
- [ ] Testes completos

**Benefícios:**
- Fluxo completo de pedido
- Rastreamento de status

#### 3. Rate Limiting
**Prioridade**: Média  
**Esforço**: 2 dias

- [ ] Implementar rate limiting com Resilience4j
- [ ] Limites por endpoint:
  - POST /orders: 10 req/min por usuário
  - GET /orders: 100 req/min por usuário
  - GET /orders/{id}: 100 req/min por usuário
- [ ] Header `X-RateLimit-Remaining`
- [ ] Erro 429 quando exceder

**Benefícios:**
- Proteção contra abuso
- Estabilidade do serviço

#### 4. CI/CD Pipeline
**Prioridade**: Média  
**Esforço**: 3 dias

- [ ] GitHub Actions workflow
- [ ] Build automático
- [ ] Testes automáticos
- [ ] Coverage report (Codecov)
- [ ] Deploy automático (staging)
- [ ] Badges reais no README

**Benefícios:**
- Automação de deploy
- Qualidade garantida
- Badges dinâmicos

#### 5. Delete Order
**Prioridade**: Baixa  
**Esforço**: 1 dia

- [ ] Endpoint DELETE /orders/{id}
- [ ] Soft delete (flag deleted_at)
- [ ] Apenas admin ou owner
- [ ] Cascade para order_items
- [ ] Audit log

**Benefícios:**
- GDPR compliance (direito ao esquecimento)
- Gestão de pedidos

#### 6. Webhooks
**Prioridade**: Baixa  
**Esforço**: 5 dias

- [ ] Configurar webhooks por cliente
- [ ] Eventos: order.created, order.updated, order.deleted
- [ ] Retry automático (3 tentativas)
- [ ] Assinatura HMAC
- [ ] Endpoint POST /webhooks (admin)

**Benefícios:**
- Integração com sistemas externos
- Notificações em tempo real

---

## v3.0.0 - Futuro

**Data Estimada**: 2026-Q4  
**Status**: 💡 **Ideias**

### Features Futuras

#### 1. Event Sourcing
**Prioridade**: Alta  
**Esforço**: 3 semanas

- [ ] Implementar Event Store
- [ ] Eventos de domínio (OrderCreated, OrderConfirmed, etc.)
- [ ] Event replay para reconstruir estado
- [ ] Snapshots para performance
- [ ] Projeções para queries

**Benefícios:**
- Histórico completo de mudanças
- Auditoria nativa
- Temporal queries

#### 2. AsyncAPI / Event-Driven
**Prioridade**: Alta  
**Esforço**: 2 semanas

- [ ] Kafka ou RabbitMQ
- [ ] Publicar eventos de pedido
- [ ] Consumir eventos de pagamento
- [ ] AsyncAPI spec
- [ ] Dead letter queue

**Benefícios:**
- Desacoplamento de serviços
- Escalabilidade
- Resiliência

#### 3. GraphQL API
**Prioridade**: Média  
**Esforço**: 2 semanas

- [ ] Endpoint GraphQL /graphql
- [ ] Queries: order, orders
- [ ] Mutations: createOrder, updateOrderStatus
- [ ] Subscriptions: orderUpdated
- [ ] GraphQL Playground

**Benefícios:**
- Flexibilidade de queries
- Redução de over-fetching
- Real-time updates

#### 4. Real-time Updates (WebSockets)
**Prioridade**: Média  
**Esforço**: 1 semana

- [ ] WebSocket endpoint /ws/orders
- [ ] Notificações de status
- [ ] Server-Sent Events (SSE)
- [ ] Autenticação JWT via WebSocket

**Benefícios:**
- Updates em tempo real
- Melhor UX

#### 5. Advanced Analytics
**Prioridade**: Baixa  
**Esforço**: 2 semanas

- [ ] Endpoint GET /analytics/orders
- [ ] Métricas: total vendas, pedidos por dia, produtos mais vendidos
- [ ] Agregações por período
- [ ] Cache de analytics
- [ ] Dashboards

**Benefícios:**
- Insights de negócio
- Tomada de decisão

#### 6. Multi-tenancy
**Prioridade**: Baixa  
**Esforço**: 3 semanas

- [ ] Suporte a múltiplos tenants
- [ ] Isolamento de dados por tenant
- [ ] Configuração por tenant
- [ ] Billing por tenant

**Benefícios:**
- SaaS ready
- Escalabilidade de negócio

#### 7. Kubernetes Deployment
**Prioridade**: Média  
**Esforço**: 1 semana

- [ ] Helm charts
- [ ] Deployment manifests
- [ ] Service mesh (Istio)
- [ ] Auto-scaling (HPA)
- [ ] Monitoring (Prometheus + Grafana)

**Benefícios:**
- Produção-ready
- Auto-scaling
- Observabilidade

#### 8. Payment Integration
**Prioridade**: Alta  
**Esforço**: 2 semanas

- [ ] Integração com Stripe/PayPal
- [ ] Endpoint POST /orders/{id}/payment
- [ ] Webhooks de pagamento
- [ ] Refunds
- [ ] PCI-DSS compliance

**Benefícios:**
- Fluxo completo de e-commerce
- Monetização

---

## Backlog de Melhorias

### Performance
- [ ] Cache de queries com Redis
- [ ] Connection pooling otimizado
- [ ] Índices adicionais
- [ ] Query optimization

### Segurança
- [ ] 2FA (Two-Factor Authentication)
- [ ] API Key authentication (alternativa a JWT)
- [ ] IP whitelisting
- [ ] Encryption at rest

### Observabilidade
- [ ] Distributed tracing (Jaeger)
- [ ] Metrics (Prometheus)
- [ ] Dashboards (Grafana)
- [ ] Alerting (PagerDuty)

### Developer Experience
- [ ] SDK clients (Java, Python, Node.js)
- [ ] Postman collection
- [ ] Code generation (OpenAPI)
- [ ] Mock server

### Testing
- [ ] Contract testing (Pact)
- [ ] Performance testing (JMeter)
- [ ] Chaos engineering (Chaos Monkey)
- [ ] Security testing (OWASP ZAP)

---

## Critérios de Priorização

### Alta Prioridade
- Impacto direto no negócio
- Compliance obrigatório
- Segurança crítica

### Média Prioridade
- Melhoria de UX
- Eficiência operacional
- Qualidade de código

### Baixa Prioridade
- Nice to have
- Experimentação
- Otimizações incrementais

---

## Processo de Release

### 1. Planejamento
- Definir features da release
- Estimar esforço
- Alocar recursos

### 2. Desenvolvimento
- Implementar features
- Escrever testes
- Code review

### 3. Testes
- Testes unitários (> 95% coverage)
- Testes de integração
- Testes manuais

### 4. Documentação
- Atualizar docs/
- Atualizar README.md
- Changelog

### 5. Deploy
- Deploy em staging
- Testes de aceitação
- Deploy em produção

### 6. Monitoramento
- Verificar métricas
- Verificar logs
- Verificar alertas

---

## Feedback e Sugestões

Tem ideias para o roadmap? Abra uma issue no GitHub:

```
Title: [ROADMAP] Sugestão de feature
Labels: enhancement, roadmap

Descrição:
- Feature: [nome da feature]
- Problema que resolve: [descrição]
- Benefícios: [lista de benefícios]
- Prioridade sugerida: [alta/média/baixa]
```

---

## Documentação Relacionada

- 🏗️ [Arquitetura](ARCHITECTURE.md) - Decisões arquiteturais
- 🔌 [API](API.md) - Endpoints atuais
- 💻 [Development](DEVELOPMENT.md) - Como contribuir
- 🚀 [Deployment](DEPLOYMENT.md) - Como fazer deploy

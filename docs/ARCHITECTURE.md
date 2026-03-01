# Arquitetura - Orders Service

Este documento detalha a arquitetura CQRS (Command Query Responsibility Segregation) com Read Replicas do Orders Service.

## 📋 Índice

- [CQRS Overview](#cqrs-overview-com-read-replicas)
- [CQRS Architecture Diagram](#cqrs-architecture-diagram)
- [CQRS Flow Diagrams](#cqrs-flow-diagrams)
- [Package Structure](#package-structure)
- [Test Coverage](#test-coverage-overview)
- [CQRS Benefits](#cqrs-benefits-achieved)

---

## CQRS Overview com Read Replicas

```mermaid
flowchart LR
    subgraph "Command Side - Write"
        POST[POST /orders]
        OCC[OrderCreationController]
        OCH[CreateOrderCommandHandler]
        OCR[OrderCommandRepository]
        DBP[(PostgreSQL Primary<br/>Port: 5432<br/>R/W)]
        
        POST --> OCC --> OCH --> OCR --> DBP
    end
    
    subgraph "Query Side - Read"
        GET1[GET /orders]
        GET2[GET /orders/:id]
        OQC[OrderQueryController]
        GQH[GetOrderQueryHandler]
        LQH[ListOrdersQueryHandler]
        OQR[OrderQueryRepository]
        DBR[(PostgreSQL Replica<br/>Port: 5433<br/>R/O)]
        
        GET1 --> OQC
        GET2 --> OQC
        OQC --> GQH --> OQR
        OQC --> LQH --> OQR
        OQR --> DBR
    end
    
    DBP -.->|Streaming<br/>Replication| DBR
    
    style DBP fill:#e8f5e8,stroke:#4caf50,stroke-width:3px
    style DBR fill:#fff3e0,stroke:#ff9800,stroke-width:3px
    style POST fill:#e1f5fe
    style OCC fill:#e1f5fe
    style OCH fill:#e1f5fe
    style GET1 fill:#f3e5f5
    style GET2 fill:#f3e5f5
    style OQC fill:#f3e5f5
    style GQH fill:#f3e5f5
    style LQH fill:#f3e5f5
```

### Arquitetura CQRS com Separação Física de Bases

#### 1. Command Side (Write Operations)

- `POST /orders` → `OrderCreationController` → `CreateOrderCommandHandler` → `OrderCommandRepository`
- Conecta ao **PostgreSQL Primary** (porta 5432) com acesso Read/Write
- Transaction Manager: `commandTransactionManager`

#### 2. Query Side (Read Operations)

- `GET /orders` → `OrderQueryController` → `GetOrderQueryHandler` → `OrderQueryRepository`
- `GET /orders/{id}` → `OrderQueryController` → `ListOrdersQueryHandler` → `OrderQueryRepository`
- Conecta ao **PostgreSQL Replica** (porta 5433) com acesso Read-Only
- Transaction Manager: `queryTransactionManager`

#### 3. Replicação

- Streaming Replication assíncrona do Primary para Replica
- Eventual Consistency (delay típico < 1s)
- Réplica pode ser promovida a Primary em caso de failover

#### 4. Benefícios

- ✅ **Performance**: Queries não impactam writes
- ✅ **Escalabilidade**: Adicionar réplicas conforme demanda
- ✅ **Disponibilidade**: Failover automático
- ✅ **Segurança**: Query side read-only

📚 **Documentação Técnica Completa**: Ver [CQRS-READ-REPLICAS.md](CQRS-READ-REPLICAS.md)

---

## CQRS Architecture Diagram

```mermaid
flowchart TD
    subgraph "Domain Layer (Shared)"
        O[Order Entity]
        OI[OrderItem Entity]
        OS[OrderStatus Enum]
    end

    subgraph "Shared Components"
        SC[SecurityConfig]
        OC[OpenApiConfig]
        WC[WebConfig]
        GEH[GlobalExceptionHandler]
        L[Logger]
    end

    subgraph "Command Side (Write)"
        CC[OrderCreationController]
        CH[CreateOrderCommandHandler]
        CR[OrderCommandRepository]
        CM[OrderCommandMapper]
        CV[OrderValidator]
        CA[OrderAuthorization]
    end

    subgraph "Query Side (Read)"
        QC[OrderQueryController]
        QH1[GetOrderQueryHandler]
        QH2[ListOrdersQueryHandler]
        QR[OrderQueryRepository]
        QM[OrderQueryMapper]
        QA[OrderAuthorization]
    end

    CC --> CH
    CH --> CR
    CH --> CM
    CH --> CV
    CH --> CA

    QC --> QH1
    QC --> QH2
    QH1 --> QR
    QH2 --> QR
    QH1 --> QM
    QH2 --> QM
    QH1 --> QA
    QH2 --> QA

    CH --> O
    QH1 --> O
    QH2 --> O
    O --> OI
    O --> OS

    CC -.-> SC
    QC -.-> SC
    CC -.-> GEH
    QC -.-> GEH

    subgraph "Database - Write"
        DBP[(PostgreSQL Primary)]
    end

    subgraph "Database - Read"
        DBR[(PostgreSQL Replica)]
    end

    CR --> DBP
    QR --> DBR
    DBP -.-> DBR

    style CC fill:#e1f5fe
    style CH fill:#e1f5fe
    style QC fill:#f3e5f5
    style QH1 fill:#f3e5f5
    style QH2 fill:#f3e5f5
    style DBP fill:#e8f5e8,stroke:#4caf50,stroke-width:2px
    style DBR fill:#fff3e0,stroke:#ff9800,stroke-width:2px
```

### Camadas da Arquitetura

#### Domain Layer (Shared)
- **Order Entity**: Entidade principal de pedido
- **OrderItem Entity**: Itens do pedido
- **OrderStatus Enum**: Estados do pedido (pending, confirmed, shipped, delivered)

#### Shared Components
- **SecurityConfig**: Configuração Spring Security + JWT
- **OpenApiConfig**: Configuração OpenAPI/Swagger
- **WebConfig**: Configuração CORS e Web
- **GlobalExceptionHandler**: Tratamento global de exceções
- **Logger**: Logging estruturado

#### Command Side (Write)
- **OrderCreationController**: REST controller para criação
- **CreateOrderCommandHandler**: Lógica de negócio de criação
- **OrderCommandRepository**: Repositório de escrita
- **OrderCommandMapper**: Mapeamento de DTOs
- **OrderValidator**: Validações de negócio
- **OrderAuthorization**: Regras de autorização

#### Query Side (Read)
- **OrderQueryController**: REST controller para consultas
- **GetOrderQueryHandler**: Handler para buscar pedido específico
- **ListOrdersQueryHandler**: Handler para listar pedidos
- **OrderQueryRepository**: Repositório de leitura
- **OrderQueryMapper**: Mapeamento de DTOs
- **OrderAuthorization**: Regras de autorização

---

## CQRS Flow Diagrams

### Command Flow (Create Order)

```mermaid
sequenceDiagram
    participant Client
    participant OCC as OrderCreationController
    participant OCH as CreateOrderCommandHandler
    participant OCR as OrderCommandRepository
    participant DBP as PostgreSQL Primary

    Client->>OCC: POST /orders
    OCC->>OCC: extractCustomerId()
    OCC->>OCH: handle(command, customerId)
    OCH->>OCH: validateCreateOrderAuthorization()
    OCH->>OCH: validate items not empty
    loop For each item
        OCH->>OCH: validateItem(item)
        OCH->>OCH: mapper.toItemEntity(item)
        OCH->>OCH: item.calculateSubtotal()
    end
    OCH->>OCH: order.calculateTotal()
    OCH->>OCH: validateOrderTotal()
    OCH->>OCR: save(order)
    OCR->>DBP: INSERT order + items
    OCH->>OCH: mapper.toResult(savedOrder)
    OCH-->>OCC: CreateOrderResult
    OCC-->>Client: 201 Created
```

#### Fluxo de Criação de Pedido

1. **Cliente** envia `POST /orders` com dados do pedido
2. **Controller** extrai `customerId` do JWT token
3. **Handler** valida autorização (usuário pode criar pedido?)
4. **Handler** valida que há pelo menos 1 item
5. **Loop** para cada item:
   - Valida item (quantidade > 0, preço > 0)
   - Mapeia DTO para entidade
   - Calcula subtotal
6. **Handler** calcula total do pedido
7. **Handler** valida total (não pode ser zero)
8. **Repository** salva pedido no **PostgreSQL Primary**
9. **Handler** mapeia entidade para DTO de resultado
10. **Controller** retorna `201 Created`

---

### Query Flow (List Orders)

```mermaid
sequenceDiagram
    participant Client
    participant OQC as OrderQueryController
    participant LQH as ListOrdersQueryHandler
    participant OQR as OrderQueryRepository
    participant DBR as PostgreSQL Replica

    Client->>OQC: GET /orders?limit=20&offset=0
    OQC->>OQC: extractCustomerId()
    OQC->>OQC: isAdmin()
    OQC->>LQH: handle(query)
    LQH->>LQH: apply filters (admin/customer + status)
    LQH->>OQR: findBy...() with pagination
    OQR->>DBR: SELECT with conditions
    DBR-->>OQR: ResultSet
    OQR-->>LQH: Page<Order>
    LQH->>LQH: mapper.toQueryResults(orders)
    LQH->>OQR: count total
    OQR->>DBR: COUNT query
    DBR-->>OQR: totalCount
    LQH-->>OQC: OrderListQueryResult
    OQC-->>Client: 200 OK
```

#### Fluxo de Listagem de Pedidos

1. **Cliente** envia `GET /orders` com parâmetros de paginação
2. **Controller** extrai `customerId` do JWT token
3. **Controller** verifica se usuário é admin
4. **Handler** aplica filtros:
   - Admin: vê todos os pedidos
   - Usuário comum: vê apenas seus pedidos
   - Filtro opcional por status
5. **Repository** executa query paginada no **PostgreSQL Replica**
6. **Handler** mapeia entidades para DTOs
7. **Repository** executa COUNT para total de registros
8. **Controller** retorna `200 OK` com dados paginados

---

## Package Structure

### Macro View - High Level Packages

```mermaid
flowchart TD
    subgraph "com.company.orders"
        CMD[command<br/>Write Operations]
        QRY[query<br/>Read Operations]
        DOM[domain<br/>Business Entities]
        SHR[shared<br/>Common Components]
    end
    
    CMD --> DOM
    QRY --> DOM
    CMD --> SHR
    QRY --> SHR
    
    style CMD fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    style QRY fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    style DOM fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style SHR fill:#e0f2f1,stroke:#004d40,stroke-width:2px
```

### Command Side - Detailed Structure

```mermaid
flowchart LR
    subgraph "command"
        direction TB
        CTRL[controller<br/>OrderCreationController]
        HAND[handler<br/>CreateOrderCommandHandler]
        REPO[repository<br/>OrderCommandRepository]
        MODL[model<br/>CreateOrderCommand<br/>CreateOrderResult]
        SERV[service<br/>OrderValidator<br/>OrderAuthorization]
    end
    
    CTRL --> HAND
    HAND --> REPO
    HAND --> MODL
    HAND --> SERV
    
    style CTRL fill:#e1f5fe
    style HAND fill:#e1f5fe
    style REPO fill:#e1f5fe
    style MODL fill:#e1f5fe
    style SERV fill:#e1f5fe
```

**Componentes:**
- **controller**: REST endpoints para comandos
- **handler**: Lógica de negócio de comandos
- **repository**: Acesso ao PostgreSQL Primary
- **model**: DTOs de comando e resultado
- **service**: Validações e autorizações

### Query Side - Detailed Structure

```mermaid
flowchart LR
    subgraph "query"
        direction TB
        QCTRL[controller<br/>OrderQueryController]
        QHAND1[handler<br/>GetOrderQueryHandler]
        QHAND2[handler<br/>ListOrdersQueryHandler]
        QREPO[repository<br/>OrderQueryRepository]
        QMODL[model<br/>GetOrderQuery<br/>ListOrdersQuery<br/>OrderQueryResult]
    end
    
    QCTRL --> QHAND1
    QCTRL --> QHAND2
    QHAND1 --> QREPO
    QHAND2 --> QREPO
    QHAND1 --> QMODL
    QHAND2 --> QMODL
    
    style QCTRL fill:#f3e5f5
    style QHAND1 fill:#f3e5f5
    style QHAND2 fill:#f3e5f5
    style QREPO fill:#f3e5f5
    style QMODL fill:#f3e5f5
```

**Componentes:**
- **controller**: REST endpoints para queries
- **handler**: Lógica de negócio de queries
- **repository**: Acesso ao PostgreSQL Replica
- **model**: DTOs de query e resultado

### Domain Layer - Detailed Structure

```mermaid
flowchart TD
    subgraph "domain"
        direction LR
        ENT[entity<br/>Order<br/>OrderItem]
        ENUM[enums<br/>OrderStatus]
    end
    
    ENT --> ENUM
    
    style ENT fill:#fff9c4
    style ENUM fill:#fff9c4
```

**Componentes:**
- **entity**: Entidades JPA (Order, OrderItem)
- **enums**: Enumerações (OrderStatus)

### Shared Components - Detailed Structure

```mermaid
flowchart TD
    subgraph "shared"
        direction TB
        CONF[config<br/>CommandDataSourceConfig<br/>QueryDataSourceConfig<br/>SecurityConfig]
        MAPP[mapper<br/>OrderCommandMapper<br/>OrderQueryMapper]
        SMOD[model<br/>OrderItemDto]
        EXCP[exception<br/>OrderException<br/>ResourceNotFoundException]
        MNTR[monitoring<br/>DatabaseMetrics]
    end
    
    style CONF fill:#e0f2f1
    style MAPP fill:#e0f2f1
    style SMOD fill:#e0f2f1
    style EXCP fill:#e0f2f1
    style MNTR fill:#e0f2f1
```

**Componentes:**
- **config**: Configurações (DataSources, Security, OpenAPI)
- **mapper**: MapStruct mappers (Command, Query)
- **model**: DTOs compartilhados
- **exception**: Exceções customizadas e handlers
- **monitoring**: Métricas de replicação e health checks

---

## Test Coverage Overview

```mermaid
pie title Test Distribution (106 tests)
    "Command Layer" : 20
    "Query Layer" : 31
    "Domain Layer" : 11
    "Shared Layer" : 39
    "Integration Tests" : 5
```

### Distribuição de Testes

- **Command Layer** (20 testes): Controllers e handlers de escrita
- **Query Layer** (31 testes): Controllers e handlers de leitura
- **Domain Layer** (11 testes): Entidades e lógica de domínio
- **Shared Layer** (39 testes): Validações, autorizações, exceções
- **Integration Tests** (5 testes): Testes end-to-end

**Cobertura Total**: 99% instruções + 95% branches

Ver [DEVELOPMENT.md](DEVELOPMENT.md) para detalhes completos de testes.

---

## CQRS Benefits Achieved

```mermaid
mindmap
  root((CQRS Benefits))
    Performance
      Optimized Queries
        Read vs Write separation
      Independent Scaling
        Command/Query sides
    Security
      Specific Authorization
        Command vs Query rules
      Scoped Permissions
        orders:read vs orders:write
    Maintainability
      Clear Responsibilities
        Single purpose classes
      Easy Testing
        Isolated components
    Scalability
      Independent Deployment
        Command/Query services
      Database Optimization
        Read replicas possible
    Flexibility
      Independent Evolution
        Read/Write models
      Technology Choices
        Different DBs possible
```

### Benefícios Alcançados

#### Performance
- **Optimized Queries**: Separação entre leitura e escrita permite otimizações específicas
- **Independent Scaling**: Command e Query podem escalar independentemente

#### Security
- **Specific Authorization**: Regras diferentes para comandos e queries
- **Scoped Permissions**: `orders:read` vs `orders:write`

#### Maintainability
- **Clear Responsibilities**: Cada classe tem propósito único
- **Easy Testing**: Componentes isolados facilitam testes

#### Scalability
- **Independent Deployment**: Command e Query podem ser deployados separadamente
- **Database Optimization**: Read replicas para queries, primary para writes

#### Flexibility
- **Independent Evolution**: Modelos de leitura e escrita evoluem independentemente
- **Technology Choices**: Possibilidade de usar diferentes DBs para cada lado

---

## Regras de Negócio

### Criação de Pedidos

1. ✅ Pedido deve ter pelo menos 1 item
2. ✅ Quantidade de cada item deve ser >= 1
3. ✅ Preço unitário deve ser > 0
4. ✅ Total é calculado automaticamente (soma dos subtotais)
5. ✅ Subtotal de cada item = quantidade × preço unitário
6. ✅ Status inicial é sempre "pending"
7. ✅ customerId deve corresponder ao usuário autenticado

### Listagem de Pedidos

1. ✅ Usuários veem apenas seus próprios pedidos
2. ✅ Admins veem todos os pedidos
3. ✅ Resultados ordenados por createdAt DESC (mais novo primeiro)
4. ✅ Paginação: limit máximo de 100, padrão 20
5. ✅ Filtro opcional por status

### Consulta de Pedido

1. ✅ Usuário só pode ver seus próprios pedidos
2. ✅ Admin pode ver qualquer pedido
3. ✅ Retorna 404 se pedido não existe
4. ✅ Retorna 403 se usuário não tem permissão

---

## Documentação Relacionada

- 📖 [CQRS Read Replicas](CQRS-READ-REPLICAS.md) - Guia técnico completo
- 🔌 [API](API.md) - Endpoints e exemplos
- 💻 [Development](DEVELOPMENT.md) - Setup e testes
- 🗄️ [Database](DATABASE.md) - Schema e replicação
- 🔐 [Security](SECURITY.md) - JWT e autorizações
- 🚀 [Deployment](DEPLOYMENT.md) - Docker e monitoramento

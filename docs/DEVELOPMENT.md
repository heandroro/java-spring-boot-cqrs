# Guia de Desenvolvimento - Orders Service

Guia completo para desenvolvedores do Orders Service.

## 📋 Índice

- [Pré-requisitos](#pré-requisitos)
- [Setup Local](#setup-local)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Executar Testes](#executar-testes)
- [Cobertura de Testes](#cobertura-de-testes)
- [Validação Mermaid](#validação-mermaid)
- [Troubleshooting](#troubleshooting)

---

## Pré-requisitos

- **Java 25** instalado
- **Maven 3.9+** instalado
- **Docker** e **Docker Compose** (para PostgreSQL)
- **Git** para controle de versão

---

## Setup Local

### 1. Clonar Repositório

```bash
git clone <repository-url>
cd poc-sdd-example
```

### 2. Rodar PostgreSQL com Docker

```bash
# Iniciar Primary e Replica
docker-compose up -d postgres-primary postgres-replica

# Verificar status
docker ps
```

### 3. Rodar Aplicação

```bash
# Compilar e rodar
mvn spring-boot:run

# Ou compilar e rodar JAR
mvn clean package
java -jar target/domain-service-1.0.0-SNAPSHOT.jar
```

### 4. Acessar Aplicação

- **API Base**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## Estrutura do Projeto

```
poc-sdd-example/
├── src/main/java/com/company/orders/
│   ├── OrdersApplication.java              # Aplicação Spring Boot principal
│   ├── command/                            # Write Operations (CQRS)
│   │   ├── controller/
│   │   │   └── OrderCreationController.java    # POST /orders
│   │   ├── handler/
│   │   │   └── CreateOrderCommandHandler.java  # Lógica de criação
│   │   ├── repository/
│   │   │   └── OrderCommandRepository.java     # Write operations
│   │   ├── service/
│   │   │   └── OrderAuthorization.java         # Autorização de comandos
│   │   └── model/
│   │       ├── CreateOrderCommand.java         # Command DTO
│   │       └── CreateOrderResult.java          # Result DTO
│   ├── query/                             # Read Operations (CQRS)
│   │   ├── controller/
│   │   │   └── OrderQueryController.java       # GET /orders, GET /orders/{id}
│   │   ├── handler/
│   │   │   ├── GetOrderQueryHandler.java       # Handler para buscar pedido
│   │   │   └── ListOrdersQueryHandler.java     # Handler para listar pedidos
│   │   ├── repository/
│   │   │   └── OrderQueryRepository.java       # Read operations
│   │   └── model/
│   │       ├── GetOrderQuery.java              # Query DTO
│   │       ├── ListOrdersQuery.java            # Query DTO
│   │       ├── OrderQueryResult.java           # Result DTO
│   │       └── OrderListQueryResult.java       # Paginated result
│   ├── domain/                            # Domain Entities
│   │   ├── entity/
│   │   │   ├── Order.java                      # Entidade JPA
│   │   │   ├── OrderItem.java                  # Item entity
│   │   │   └── AuditLog.java                   # Audit trail
│   │   └── enums/
│   │       └── OrderStatus.java                # Status enum
│   └── shared/                            # Shared Components
│       ├── config/
│       │   ├── CommandDataSourceConfig.java    # Command DataSource
│       │   ├── QueryDataSourceConfig.java      # Query DataSource
│       │   ├── SecurityConfig.java             # Spring Security + JWT
│       │   ├── OpenApiConfig.java              # Configuração OpenAPI
│       │   └── WebConfig.java                  # CORS
│       ├── mapper/
│       │   ├── OrderCommandMapper.java         # Command mappers
│       │   └── OrderQueryMapper.java           # Query mappers
│       ├── model/
│       │   ├── OrderItemDto.java               # Shared DTO
│       │   └── ErrorResponse.java              # Error DTO
│       ├── exception/
│       │   ├── OrderException.java             # Exceções customizadas
│       │   ├── ResourceNotFoundException.java  # 404
│       │   ├── AuthorizationException.java     # 403
│       │   └── GlobalExceptionHandler.java     # @ControllerAdvice
│       ├── monitoring/
│       │   └── DatabaseMetrics.java            # Métricas de replicação
│       ├── util/
│       │   └── Logger.java                     # Logging estruturado
│       └── service/
│           └── OrderValidator.java             # Validação compartilhada
│
├── src/test/java/com/company/orders/
│   ├── command/
│   │   ├── controller/
│   │   │   └── OrderCreationControllerTest.java
│   │   └── handler/
│   │       └── CreateOrderCommandHandlerTest.java
│   ├── query/
│   │   ├── controller/
│   │   │   └── OrderQueryController Test.java
│   │   └── handler/
│   │       ├── GetOrderQueryHandlerTest.java
│   │       └── ListOrdersQueryHandlerTest.java
│   ├── domain/entity/
│   │   └── OrderTest.java
│   ├── shared/
│   │   ├── exception/
│   │   │   ├── ExceptionsTest.java
│   │   │   └── GlobalExceptionHandlerTest.java
│   │   ├── service/
│   │   │   └── OrderValidatorTest.java
│   │   └── util/
│   │       └── LoggerTest.java
│   └── integration/
│       └── OrderIntegrationTest.java
│
├── src/main/resources/
│   ├── application.yml                     # Configuração principal
│   ├── application-test.yml                # Configuração de testes
│   ├── db/migration/
│   │   └── V1__orders_schema.sql           # Schema inicial
│   └── openapi-spec.yaml                   # OpenAPI 3.1.0 spec
│
├── docs/                                   # Documentação
│   ├── ARCHITECTURE.md
│   ├── API.md
│   ├── DATABASE.md
│   ├── SECURITY.md
│   ├── DEPLOYMENT.md
│   ├── ROADMAP.md
│   └── CQRS-READ-REPLICAS.md
│
├── pom.xml                                 # Dependências Maven
├── docker-compose.yml                      # PostgreSQL + app
├── validate-mermaid.py                     # Validador Mermaid
├── mermaid-validator.sh                    # Script validador
└── README.md                               # Documentação principal
```

---

## Executar Testes

### Todos os Testes

```bash
mvn test
```

### Apenas Testes Unitários

```bash
mvn test -Dtest='!*IntegrationTest'
```

### Apenas Testes de Integração

```bash
mvn test -Dtest='*IntegrationTest'
```

### Com Cobertura Detalhada

```bash
mvn clean verify
```

### Gerar Relatório de Cobertura

```bash
mvn jacoco:report

# Relatório em: target/site/jacoco/index.html
```

---

## Cobertura de Testes

### Métricas Atuais

| Métrica | Valor | Meta | Status |
|---------|-------|------|--------|
| **Instruções** | 99% | 95% | ✅ **Ultrapassada** |
| **Branches** | 95% | 90% | ✅ **Ultrapassada** |
| **Testes Totais** | 106 | - | ✅ **Completos** |

### Suite de Testes

#### Testes Unitários (101 testes)

**Command Layer** (20 testes):
- `OrderCreationControllerTest` - 9 testes
  - Branches de autenticação/autorização
  - Validação de entrada
  - Tratamento de erros
- `CreateOrderCommandHandlerTest` - 11 testes
  - Lógica de negócio
  - Validações de itens
  - Cálculo de totais

**Query Layer** (31 testes):
- `OrderQueryControllerTest` - 11 testes
  - Branches de autenticação/autorização
  - Paginação
  - Filtros
- `GetOrderQueryHandlerTest` - 10 testes
  - Busca por ID
  - Autorização
  - Tratamento de erros
- `ListOrdersQueryHandlerTest` - 10 testes
  - Paginação
  - Filtros por status
  - Admin vs customer

**Domain Layer** (11 testes):
- `OrderTest` - 11 testes
  - Entidade e relacionamentos
  - Cálculos de subtotal e total
  - Validações de domínio

**Shared Layer** (39 testes):
- `OrderValidatorTest` - 12 testes
  - Validações de negócio
  - Validação de itens
  - Validação de totais
- `OrderAuthorizationTest` - 7 testes
  - Regras de acesso
  - Admin vs customer
  - Permissões
- `ExceptionsTest` - 6 testes
  - Exceções customizadas
  - Mensagens de erro
- `GlobalExceptionHandlerTest` - 6 testes
  - Tratamento global
  - Códigos HTTP
  - Formato de resposta
- `LoggerTest` - 8 testes
  - Logging estruturado
  - Níveis de log

#### Testes de Integração (5 testes)

- `OrderIntegrationTest` - 5 testes
  - Fluxo completo end-to-end
  - Criação → Listagem → Busca
  - Validação de autorização
  - Paginação

### Branches Cobertos

#### Controllers (85%+ cobertura)
- **OrderQueryController**: `extractCustomerId()`, `isAdmin()` - todos os branches
- **OrderCreationController**: `extractCustomerId()` - todos os branches

#### Handlers (100% cobertura)
- **CreateOrderCommandHandler**: Lógica de criação, validações, autorizações
- **GetOrderQueryHandler**: Busca, autorização, tratamento de erros
- **ListOrdersQueryHandler**: Paginação, filtros, admin vs customer

#### Services (100% cobertura)
- **OrderValidator**: Validações de itens, totais, regras de negócio
- **OrderAuthorization**: Regras de acesso, permissões

---

## Validação Mermaid

O projeto inclui ferramentas para validar diagramas Mermaid antes de commitar.

### Validador Python (Recomendado)

```bash
# Executar validador
python3 validate-mermaid.py

# Saída esperada
✅ All 11 Mermaid diagrams have valid syntax!
```

### Validador Bash

```bash
# Executar script
./mermaid-validator.sh

# Com Mermaid CLI instalado
npm install -g @mermaid-js/mermaid-cli
```

### Validação Online

1. **Mermaid Live Editor**: https://mermaid.live/
   - Copie o código Mermaid
   - Cole no editor
   - Verifique renderização

2. **GitHub Preview**
   - Diagramas são validados automaticamente no GitHub
   - Se houver erro, diagrama não será exibido

### Checklist de Validação

✅ **Antes de Commitar:**
1. Executar `python3 validate-mermaid.py`
2. Testar no [Mermaid Live Editor](https://mermaid.live/)
3. Verificar sintaxe básica
4. Evitar subgraphs aninhados (não suportados no GitHub)
5. Usar nomes únicos para nós

❌ **Erros Comuns:**
- Subgraphs aninhados
- Conflitos de nomes de nós
- Sintaxe complexa não suportada

---

## Troubleshooting

### Problema: Aplicação não inicia

**Erro**: `Connection refused to PostgreSQL`

**Solução**:
```bash
# Verificar se PostgreSQL está rodando
docker ps

# Reiniciar containers
docker-compose restart postgres-primary postgres-replica
```

### Problema: Testes falhando

**Erro**: `Database connection error in tests`

**Solução**:
```bash
# Usar perfil de teste
mvn test -Dspring.profiles.active=test

# Limpar e recompilar
mvn clean install
```

### Problema: Porta 8080 em uso

**Erro**: `Port 8080 is already in use`

**Solução**:
```bash
# Encontrar processo usando porta 8080
lsof -i :8080

# Matar processo
kill -9 <PID>

# Ou usar porta diferente
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Problema: Replicação não funciona

**Erro**: `Replication lag too high`

**Solução**:
```bash
# Verificar status da replicação
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"

# Verificar lag
docker exec -it orders-db-replica psql -U postgres -c "SELECT now() - pg_last_xact_replay_timestamp() AS replication_lag;"

# Reiniciar replica
docker-compose restart postgres-replica
```

### Problema: Jacoco não gera relatório

**Erro**: `Jacoco report not found`

**Solução**:
```bash
# Executar com verify
mvn clean verify

# Relatório em: target/site/jacoco/index.html
open target/site/jacoco/index.html
```

---

## Boas Práticas

### Commits

Usar conventional commits:
```bash
feat(scope): descrição concisa
fix(scope): descrição concisa
docs(scope): descrição concisa
refactor(scope): descrição concisa
```

### Testes

- Escrever testes antes de implementar (TDD)
- Manter cobertura > 95%
- Testar branches de autorização
- Incluir testes de integração

### Código

- Seguir convenções Java
- Usar Lombok para reduzir boilerplate
- Documentar métodos públicos
- Validar entradas

### Documentação

- Atualizar docs/ quando mudar arquitetura
- Validar diagramas Mermaid antes de commitar
- Manter README.md atualizado
- Documentar decisões arquiteturais

---

## Documentação Relacionada

- 🏗️ [Arquitetura](ARCHITECTURE.md) - CQRS e diagramas
- 🔌 [API](API.md) - Endpoints e exemplos
- 🗄️ [Database](DATABASE.md) - Schema e replicação
- 🔐 [Security](SECURITY.md) - JWT e autorizações
- 🚀 [Deployment](DEPLOYMENT.md) - Docker e CI/CD
- 🗺️ [Roadmap](ROADMAP.md) - Versões futuras

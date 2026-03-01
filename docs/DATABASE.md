# Database - Orders Service

Documentação completa do banco de dados PostgreSQL com replicação.

## 📋 Índice

- [Schema](#schema)
- [Migrações Flyway](#migrações-flyway)
- [Replicação PostgreSQL](#replicação-postgresql)
- [Índices e Performance](#índices-e-performance)
- [Backup e Restore](#backup-e-restore)

---

## Schema

O serviço utiliza 3 tabelas principais no PostgreSQL 16.

### 1. orders - Pedidos Principais

```sql
CREATE TABLE orders (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL,
  status VARCHAR(20) CHECK (status IN ('pending', 'confirmed', 'shipped', 'delivered')),
  total NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
```

**Colunas:**
- `id`: UUID único do pedido
- `customer_id`: UUID do cliente (extraído do JWT)
- `status`: Status do pedido (pending, confirmed, shipped, delivered)
- `total`: Valor total do pedido (calculado automaticamente)
- `created_at`: Data/hora de criação
- `updated_at`: Data/hora da última atualização

### 2. order_items - Itens do Pedido

```sql
CREATE TABLE order_items (
  id UUID PRIMARY KEY,
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  product_id VARCHAR(255) NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  price_per_unit NUMERIC(10, 2) NOT NULL,
  subtotal NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

**Colunas:**
- `id`: UUID único do item
- `order_id`: Referência ao pedido (CASCADE DELETE)
- `product_id`: ID do produto
- `quantity`: Quantidade (>= 1)
- `price_per_unit`: Preço unitário (> 0)
- `subtotal`: Subtotal calculado (quantity × price_per_unit)
- `created_at`: Data/hora de criação

### 3. audit_logs - Auditoria

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

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_customer ON audit_logs(customer_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
```

**Colunas:**
- `id`: UUID único do log
- `entity_type`: Tipo de entidade (ORDER, ORDER_ITEM)
- `entity_id`: ID da entidade auditada
- `operation`: Operação (CREATE, UPDATE, DELETE)
- `customer_id`: ID do cliente que executou a operação
- `details`: Detalhes adicionais em JSON
- `created_at`: Data/hora do evento

**Nota**: Audit logging está preparado mas não implementado na v2.0.0.

---

## Migrações Flyway

O projeto usa Flyway para versionamento de schema.

### Estrutura de Migrações

```
src/main/resources/db/migration/
└── V1__orders_schema.sql
```

### V1__orders_schema.sql

```sql
-- Criar tabela orders
CREATE TABLE orders (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL,
  status VARCHAR(20) CHECK (status IN ('pending', 'confirmed', 'shipped', 'delivered')),
  total NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Criar tabela order_items
CREATE TABLE order_items (
  id UUID PRIMARY KEY,
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  product_id VARCHAR(255) NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  price_per_unit NUMERIC(10, 2) NOT NULL,
  subtotal NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Criar tabela audit_logs
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(50) NOT NULL,
  entity_id UUID NOT NULL,
  operation VARCHAR(20) NOT NULL,
  customer_id UUID,
  details JSONB,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Criar índices
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_customer ON audit_logs(customer_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
```

### Executar Migrações

Flyway executa automaticamente ao iniciar a aplicação:

```bash
mvn spring-boot:run
```

### Verificar Status de Migrações

```bash
mvn flyway:info
```

---

## Replicação PostgreSQL

O serviço usa PostgreSQL com **Streaming Replication** para separar leitura e escrita.

### Arquitetura de Replicação

```
PostgreSQL Primary (Port 5432)
    ↓ Streaming Replication
PostgreSQL Replica (Port 5433)
```

- **Primary**: Read/Write - Usado pelo Command Side
- **Replica**: Read-Only - Usado pelo Query Side

### Configuração Docker Compose

```yaml
services:
  postgres-primary:
    image: postgres:16
    container_name: orders-db-primary
    environment:
      POSTGRES_DB: orders_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_primary_data:/var/lib/postgresql/data
    command: >
      postgres
      -c wal_level=replica
      -c max_wal_senders=3
      -c max_replication_slots=3

  postgres-replica:
    image: postgres:16
    container_name: orders-db-replica
    environment:
      POSTGRES_DB: orders_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres_replica_data:/var/lib/postgresql/data
    depends_on:
      - postgres-primary
```

### Iniciar Replicação

```bash
# Iniciar Primary e Replica
docker-compose up -d postgres-primary postgres-replica

# Verificar status
docker ps
```

### Verificar Status da Replicação

```bash
# No Primary - Ver conexões de replicação
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"

# Saída esperada:
#  pid  | usename  | application_name | client_addr | state     | sync_state
# ------+----------+------------------+-------------+-----------+------------
#  123  | postgres | replica          | 172.18.0.3  | streaming | async
```

### Verificar Lag de Replicação

```bash
# No Replica - Ver atraso de replicação
docker exec -it orders-db-replica psql -U postgres -c "SELECT now() - pg_last_xact_replay_timestamp() AS replication_lag;"

# Saída esperada:
#  replication_lag
# -----------------
#  00:00:00.123456
```

**Lag típico**: < 1 segundo

### Eventual Consistency

- Replicação é **assíncrona**
- Delay típico: < 1s
- Queries podem ver dados ligeiramente desatualizados
- Aceitável para a maioria dos casos de uso

### Failover

Em caso de falha do Primary:

```bash
# Promover Replica a Primary
docker exec -it orders-db-replica pg_ctl promote

# Reconfigurar aplicação para usar nova Primary
# Atualizar application.yml
```

---

## Índices e Performance

### Índices Criados

**orders:**
- `idx_orders_customer_id`: Busca por cliente
- `idx_orders_status`: Filtro por status
- `idx_orders_created_at`: Ordenação por data (DESC)

**order_items:**
- `idx_order_items_order_id`: Join com orders
- `idx_order_items_product_id`: Busca por produto

**audit_logs:**
- `idx_audit_logs_entity`: Busca por entidade
- `idx_audit_logs_customer`: Busca por cliente
- `idx_audit_logs_created_at`: Ordenação por data

### Queries Otimizadas

#### Listar pedidos de um cliente

```sql
SELECT * FROM orders
WHERE customer_id = ?
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

-- Usa: idx_orders_customer_id + idx_orders_created_at
```

#### Buscar pedido com itens

```sql
SELECT o.*, oi.*
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.id = ?;

-- Usa: Primary Key + idx_order_items_order_id
```

#### Filtrar por status

```sql
SELECT * FROM orders
WHERE customer_id = ? AND status = 'pending'
ORDER BY created_at DESC;

-- Usa: idx_orders_customer_id + idx_orders_status
```

### Análise de Performance

```sql
-- Analisar query plan
EXPLAIN ANALYZE
SELECT * FROM orders
WHERE customer_id = '550e8400-e29b-41d4-a716-446655440000'
ORDER BY created_at DESC
LIMIT 20;
```

---

## Backup e Restore

### Backup Manual

```bash
# Backup do Primary
docker exec orders-db-primary pg_dump -U postgres orders_db > backup.sql

# Backup com timestamp
docker exec orders-db-primary pg_dump -U postgres orders_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restore

```bash
# Restore do backup
docker exec -i orders-db-primary psql -U postgres orders_db < backup.sql
```

### Backup Automático (Recomendado)

Configurar cron job para backups diários:

```bash
# Adicionar ao crontab
0 2 * * * docker exec orders-db-primary pg_dump -U postgres orders_db > /backups/orders_$(date +\%Y\%m\%d).sql
```

### Backup de Dados e Schema

```bash
# Apenas schema
docker exec orders-db-primary pg_dump -U postgres --schema-only orders_db > schema.sql

# Apenas dados
docker exec orders-db-primary pg_dump -U postgres --data-only orders_db > data.sql
```

---

## Monitoramento

### Métricas de Replicação

O serviço expõe métricas via `DatabaseMetrics`:

```java
@Component
public class DatabaseMetrics {
    public Map<String, Object> getReplicationMetrics() {
        // Retorna:
        // - is_replica: boolean
        // - replication_lag_seconds: double
        // - replication_lag_ms: double
        // - status: "healthy" | "error"
    }
}
```

### Health Checks

```bash
# Verificar saúde da aplicação
curl http://localhost:8080/actuator/health

# Resposta esperada:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" }
#   }
# }
```

---

## Troubleshooting

### Problema: Replicação parada

```bash
# Verificar logs do Replica
docker logs orders-db-replica

# Reiniciar Replica
docker-compose restart postgres-replica
```

### Problema: Lag muito alto

```bash
# Verificar carga do Primary
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_activity;"

# Verificar tamanho do WAL
docker exec -it orders-db-primary psql -U postgres -c "SELECT pg_current_wal_lsn();"
```

### Problema: Conexão recusada

```bash
# Verificar se containers estão rodando
docker ps

# Verificar logs
docker logs orders-db-primary
docker logs orders-db-replica

# Reiniciar containers
docker-compose restart
```

---

## Documentação Relacionada

- 🏗️ [Arquitetura](ARCHITECTURE.md) - CQRS e separação de databases
- 🔌 [API](API.md) - Endpoints que usam o database
- 💻 [Development](DEVELOPMENT.md) - Setup local
- 🚀 [Deployment](DEPLOYMENT.md) - Deploy com Docker

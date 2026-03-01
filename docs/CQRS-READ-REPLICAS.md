# CQRS com Read Replicas - Guia Completo

## 📋 Visão Geral

Este projeto implementa uma arquitetura CQRS (Command Query Responsibility Segregation) avançada com separação física de bases de dados:

- **Command Side**: PostgreSQL Primary (Read/Write) - Porta 5432
- **Query Side**: PostgreSQL Replica (Read-Only) - Porta 5433
- **Replicação**: Streaming Replication entre Primary e Replica

## 🏗️ Arquitetura

```
┌─────────────────────────────────────┐    ┌─────────────────────────────────────┐
│        Command Side (Write)         │    │        Query Side (Read)          │
├─────────────────────────────────────┤    ├─────────────────────────────────────┤
│  OrderCreationController            │    │  OrderQueryController             │
│  ↓                                  │    │  ↓                                │
│  CreateOrderCommandHandler          │    │  GetOrderQueryHandler            │
│  ↓                                  │    │  ListOrdersQueryHandler           │
│  OrderCommandRepository             │    │  ↓                                │
│  ↓                                  │    │  OrderQueryRepository             │
│  PostgreSQL Primary (R/W)           │◄───┤  PostgreSQL Replica (R/O)        │
│  Port: 5432                         │    │  Port: 5433                       │
└─────────────────────────────────────┘    └─────────────────────────────────────┘
                                       ▲
                                       │
                                Streaming Replication
```

## 🚀 Como Executar

### 1. Iniciar Infraestrutura

```bash
# Iniciar PostgreSQL Primary e Replica
docker-compose up -d postgres-primary postgres-replica

# Verificar status da replicação
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"

# Verificar lag de replicação
docker exec -it orders-db-replica psql -U postgres -c "SELECT now() - pg_last_xact_replay_timestamp() AS replication_lag;"
```

### 2. Executar Aplicação

```bash
# Executar aplicação localmente
mvn spring-boot:run

# Ou com Docker
docker-compose up --build
```

### 3. Testar Endpoints

```bash
# Command Side - Criar pedido (escreve no Primary)
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "123e4567-e89b-12d3-a456-426614174000",
    "items": [
      {
        "productId": "123e4567-e89b-12d3-a456-426614174001",
        "quantity": 2,
        "pricePerUnit": 29.99
      }
    ]
  }'

# Query Side - Listar pedidos (lê da Replica)
curl http://localhost:8080/orders?limit=10&offset=0
```

## 🔧 Configuração

### DataSources

A aplicação está configurada com dois datasources separados:

**Command DataSource** (`application.yml`):
```yaml
spring:
  datasource:
    command:
      jdbc-url: jdbc:postgresql://localhost:5432/orders_db
      username: postgres
      password: postgres
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
```

**Query DataSource** (`application.yml`):
```yaml
spring:
  datasource:
    query:
      jdbc-url: jdbc:postgresql://localhost:5433/orders_db
      username: postgres
      password: postgres
      hikari:
        maximum-pool-size: 20
        minimum-idle: 10
        read-only: true
```

### Transaction Managers

- **Command Side**: `commandTransactionManager` (read/write)
- **Query Side**: `queryTransactionManager` (read-only)

## 📊 Monitoramento

### Verificar Status da Replicação

```bash
# No Primary - Ver réplicas conectadas
docker exec -it orders-db-primary psql -U postgres -c "
SELECT 
  client_addr,
  state,
  sync_state,
  replay_lag
FROM pg_stat_replication;
"

# Na Replica - Ver lag de replicação
docker exec -it orders-db-replica psql -U postgres -c "
SELECT 
  now() - pg_last_xact_replay_timestamp() AS replication_lag,
  pg_is_in_recovery() AS is_replica;
"
```

### Métricas Importantes

1. **Replication Lag**: Tempo de atraso entre Primary e Replica
2. **Connection Pool**: Uso de conexões em cada datasource
3. **Query Performance**: Tempo de resposta das queries na Replica
4. **Write Performance**: Tempo de resposta dos comandos no Primary

## 🎯 Benefícios

### Performance
- ✅ **Queries Otimizadas**: Réplica dedicada para leituras complexas
- ✅ **Writes Rápidos**: Primary sem carga de queries
- ✅ **Escalabilidade Horizontal**: Adicionar réplicas conforme demanda

### Disponibilidade
- ✅ **Failover**: Réplica pode ser promovida a Primary
- ✅ **Load Balancing**: Distribuir queries entre réplicas
- ✅ **Zero Downtime**: Manutenção sem impacto

### Segurança
- ✅ **Read-Only**: Query side não pode modificar dados
- ✅ **Isolamento**: Separação clara de responsabilidades
- ✅ **Auditoria**: Rastreamento de operações write vs read

## ⚠️ Considerações

### Eventual Consistency

A replicação é **assíncrona**, o que significa:
- Pequeno delay na replicação (geralmente < 1 segundo)
- Queries podem retornar dados ligeiramente desatualizados
- Aceitável para maioria dos casos de uso

**Exemplo**:
```
1. Cliente cria pedido (POST /orders) → Escreve no Primary
2. Cliente lista pedidos (GET /orders) → Lê da Replica
3. Pedido pode não aparecer imediatamente se replicação ainda não ocorreu
```

### Quando Usar Read Replicas

✅ **Recomendado para**:
- Alta carga de leitura (e-commerce, analytics)
- Queries complexas (relatórios, agregações)
- Disponibilidade crítica (sistemas 24/7)
- Escalabilidade horizontal necessária

❌ **Não recomendado para**:
- Baixa carga de leitura
- Requisitos de consistência imediata
- Orçamento limitado
- Equipe pequena sem experiência em replicação

## 🔄 Troubleshooting

### Problema: Replica não está sincronizando

```bash
# Verificar se replicação está ativa
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"

# Se vazio, verificar logs
docker logs orders-db-replica

# Reiniciar replica
docker-compose restart postgres-replica
```

### Problema: Lag de replicação muito alto

```bash
# Verificar lag
docker exec -it orders-db-replica psql -U postgres -c "
SELECT now() - pg_last_xact_replay_timestamp() AS lag;
"

# Possíveis causas:
# 1. Rede lenta entre Primary e Replica
# 2. Replica com hardware insuficiente
# 3. Muitas escritas no Primary

# Solução: Aumentar recursos da Replica ou adicionar mais réplicas
```

### Problema: Aplicação não conecta ao datasource correto

```bash
# Verificar configuração
cat src/main/resources/application.yml

# Verificar logs da aplicação
docker logs orders-service-app | grep -i datasource

# Testar conexão manual
docker exec -it orders-db-primary psql -U postgres -d orders_db
docker exec -it orders-db-replica psql -U postgres -d orders_db
```

## 📚 Próximos Passos

### Melhorias Futuras

1. **Múltiplas Réplicas**: Adicionar mais réplicas para load balancing
2. **Routing Inteligente**: Implementar routing entre réplicas
3. **Caching**: Adicionar Redis para queries frequentes
4. **Monitoring**: Prometheus + Grafana para métricas
5. **Alertas**: Alertas para replication lag alto

### Evolução da Arquitetura

```
Current: CQRS com Read Replicas
    ↓
Next: CQRS com Event Sourcing
    ↓
Future: Event-Driven Microservices
```

## 🤝 Contribuindo

Para adicionar novas features relacionadas a CQRS:

1. **Command Side**: Adicionar em `src/main/java/com/company/orders/command/`
2. **Query Side**: Adicionar em `src/main/java/com/company/orders/query/`
3. **Testes**: Garantir testes para ambos os lados
4. **Documentação**: Atualizar este guia

## 📄 Referências

- [PostgreSQL Streaming Replication](https://www.postgresql.org/docs/current/warm-standby.html)
- [Spring Boot Multiple DataSources](https://spring.io/blog/2022/02/21/spring-tips-multiple-datasources)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Eventual Consistency](https://www.allthingsdistributed.com/2008/12/eventually_consistent.html)

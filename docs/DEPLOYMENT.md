# Deployment - Orders Service

Guia completo de deployment com Docker e monitoramento.

## 📋 Índice

- [Docker Compose](#docker-compose)
- [Comandos Docker](#comandos-docker)
- [Monitoramento](#monitoramento)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)
- [CI/CD (Futuro)](#cicd-futuro)

---

## Docker Compose

O projeto usa Docker Compose para orquestrar PostgreSQL Primary, Replica e a aplicação.

### Estrutura docker-compose.yml

```yaml
version: '3.8'

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
    networks:
      - orders-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

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
      postgres-primary:
        condition: service_healthy
    networks:
      - orders-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: orders-service-app
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_COMMAND_URL: jdbc:postgresql://postgres-primary:5432/orders_db
      SPRING_DATASOURCE_QUERY_URL: jdbc:postgresql://postgres-replica:5432/orders_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      postgres-primary:
        condition: service_healthy
      postgres-replica:
        condition: service_healthy
    networks:
      - orders-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_primary_data:
  postgres_replica_data:

networks:
  orders-network:
    driver: bridge
```

---

## Comandos Docker

### Iniciar Serviços

```bash
# Iniciar apenas PostgreSQL (Primary e Replica)
docker-compose up -d postgres-primary postgres-replica

# Iniciar toda a infraestrutura (Primary, Replica e App)
docker-compose up --build

# Iniciar em background
docker-compose up -d --build
```

### Verificar Status

```bash
# Listar containers rodando
docker ps

# Ver logs de todos os serviços
docker-compose logs

# Ver logs de um serviço específico
docker-compose logs -f postgres-primary
docker-compose logs -f postgres-replica
docker-compose logs -f app
```

### Parar Serviços

```bash
# Parar todos os serviços
docker-compose down

# Parar e remover volumes (ATENÇÃO: apaga dados!)
docker-compose down -v

# Parar apenas um serviço
docker-compose stop app
```

### Reiniciar Serviços

```bash
# Reiniciar todos
docker-compose restart

# Reiniciar apenas um serviço
docker-compose restart postgres-replica
docker-compose restart app
```

### Executar Comandos nos Containers

```bash
# Acessar shell do Primary
docker exec -it orders-db-primary bash

# Acessar psql do Primary
docker exec -it orders-db-primary psql -U postgres -d orders_db

# Acessar shell da aplicação
docker exec -it orders-service-app bash
```

---

## Monitoramento

### Verificar Replicação PostgreSQL

#### Status da Replicação (Primary)

```bash
# Ver conexões de replicação ativas
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"

# Saída esperada:
#  pid  | usename  | application_name | client_addr | state     | sync_state
# ------+----------+------------------+-------------+-----------+------------
#  123  | postgres | replica          | 172.18.0.3  | streaming | async
```

**Campos importantes:**
- `state`: Deve ser "streaming"
- `sync_state`: "async" (replicação assíncrona)
- `client_addr`: IP do replica

#### Lag de Replicação (Replica)

```bash
# Verificar atraso de replicação
docker exec -it orders-db-replica psql -U postgres -c "SELECT now() - pg_last_xact_replay_timestamp() AS replication_lag;"

# Saída esperada:
#  replication_lag
# -----------------
#  00:00:00.123456
```

**Lag aceitável**: < 1 segundo

#### Verificar se é Replica

```bash
# Confirmar que está em modo recovery (replica)
docker exec -it orders-db-replica psql -U postgres -c "SELECT pg_is_in_recovery();"

# Saída esperada:
#  pg_is_in_recovery
# -------------------
#  t
```

### Métricas da Aplicação

O serviço expõe métricas via `DatabaseMetrics`:

```bash
# Acessar métricas de replicação (implementar endpoint)
curl http://localhost:8080/actuator/metrics/replication

# Resposta esperada:
# {
#   "is_replica": true,
#   "replication_lag_seconds": 0.123,
#   "replication_lag_ms": 123,
#   "status": "healthy"
# }
```

### Logs Estruturados

```bash
# Ver logs da aplicação
docker-compose logs -f app

# Filtrar por nível de log
docker-compose logs app | grep ERROR
docker-compose logs app | grep WARN
```

---

## Health Checks

### Health Check da Aplicação

```bash
# Verificar saúde geral
curl http://localhost:8080/actuator/health

# Resposta esperada:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Health Check Detalhado

```bash
# Verificar detalhes de saúde
curl http://localhost:8080/actuator/health/db

# Resposta esperada:
{
  "status": "UP",
  "details": {
    "database": "PostgreSQL",
    "validationQuery": "isValid()"
  }
}
```

### Health Check do PostgreSQL

```bash
# Primary
docker exec orders-db-primary pg_isready -U postgres

# Replica
docker exec orders-db-replica pg_isready -U postgres

# Saída esperada:
# /var/run/postgresql:5432 - accepting connections
```

---

## Troubleshooting

### Problema: Container não inicia

**Sintoma**: `docker ps` não mostra o container

**Diagnóstico**:
```bash
# Ver logs do container
docker-compose logs postgres-primary
docker-compose logs postgres-replica
docker-compose logs app

# Ver todos os containers (incluindo parados)
docker ps -a
```

**Soluções**:
```bash
# Reiniciar serviço
docker-compose restart postgres-primary

# Recriar container
docker-compose up -d --force-recreate postgres-primary

# Limpar e reiniciar tudo
docker-compose down -v
docker-compose up -d --build
```

### Problema: Replicação não funciona

**Sintoma**: Lag muito alto ou `pg_stat_replication` vazio

**Diagnóstico**:
```bash
# Verificar status no Primary
docker exec -it orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"

# Verificar logs do Replica
docker logs orders-db-replica
```

**Soluções**:
```bash
# Reiniciar Replica
docker-compose restart postgres-replica

# Recriar Replica (perde dados!)
docker-compose stop postgres-replica
docker volume rm poc-sdd-example_postgres_replica_data
docker-compose up -d postgres-replica
```

### Problema: Aplicação não conecta ao banco

**Sintoma**: `Connection refused` nos logs

**Diagnóstico**:
```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Testar conexão manualmente
docker exec -it orders-service-app curl postgres-primary:5432
```

**Soluções**:
```bash
# Verificar variáveis de ambiente
docker exec orders-service-app env | grep SPRING_DATASOURCE

# Reiniciar aplicação
docker-compose restart app

# Verificar network
docker network inspect poc-sdd-example_orders-network
```

### Problema: Porta em uso

**Sintoma**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Diagnóstico**:
```bash
# Encontrar processo usando a porta
lsof -i :8080
lsof -i :5432
lsof -i :5433
```

**Soluções**:
```bash
# Matar processo
kill -9 <PID>

# Ou mudar porta no docker-compose.yml
ports:
  - "8081:8080"  # Usar porta 8081 no host
```

### Problema: Volumes corrompidos

**Sintoma**: Erros de I/O ou dados inconsistentes

**Solução**:
```bash
# ATENÇÃO: Isso apagará todos os dados!
docker-compose down -v
docker volume prune
docker-compose up -d --build
```

### Problema: Build falha

**Sintoma**: `docker-compose up --build` falha

**Diagnóstico**:
```bash
# Ver logs de build
docker-compose build --no-cache

# Verificar Dockerfile
cat Dockerfile
```

**Soluções**:
```bash
# Limpar cache do Docker
docker builder prune

# Rebuild sem cache
docker-compose build --no-cache
docker-compose up -d
```

---

## CI/CD (Futuro)

### GitHub Actions (Planejado para v2.1)

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 25
        uses: actions/setup-java@v3
        with:
          java-version: '25'
      - name: Run tests
        run: mvn clean verify
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker image
        run: docker build -t orders-service:${{ github.sha }} .
      - name: Push to registry
        run: docker push orders-service:${{ github.sha }}

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to production
        run: kubectl apply -f k8s/
```

### Kubernetes (Planejado para v3.0)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: orders-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: orders-service
  template:
    metadata:
      labels:
        app: orders-service
    spec:
      containers:
      - name: orders-service
        image: orders-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_COMMAND_URL
          value: jdbc:postgresql://postgres-primary:5432/orders_db
        - name: SPRING_DATASOURCE_QUERY_URL
          value: jdbc:postgresql://postgres-replica:5432/orders_db
```

---

## Comandos Úteis

### Limpeza

```bash
# Remover containers parados
docker container prune

# Remover imagens não usadas
docker image prune

# Remover volumes não usados
docker volume prune

# Limpeza completa (CUIDADO!)
docker system prune -a --volumes
```

### Backup

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

### Monitoramento de Recursos

```bash
# Ver uso de recursos
docker stats

# Ver uso de disco
docker system df
```

---

## Documentação Relacionada

- 🗄️ [Database](DATABASE.md) - Replicação PostgreSQL
- 💻 [Development](DEVELOPMENT.md) - Setup local
- 🏗️ [Architecture](ARCHITECTURE.md) - Arquitetura CQRS
- 🔐 [Security](SECURITY.md) - Segurança e JWT

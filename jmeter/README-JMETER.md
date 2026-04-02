# JMeter Stress Test - Orders Service

Testes de carga e performance para o Orders Service usando Apache JMeter.

## 📋 Visão Geral

Este diretório contém testes de stress para validar o comportamento da aplicação sob alta demanda, com foco especial na arquitetura CQRS com Read Replicas PostgreSQL.

**Duração total**: 5 minutos  
**Ambiente**: Local (macOS)  
**Ferramenta**: Apache JMeter 5.6.3+

## 🎯 Cenários de Teste

### 1. Warm-up (1 minuto)
- **Threads**: 10 usuários simultâneos
- **Ramp-up**: 10 segundos
- **Objetivo**: Aquecer JVM e connection pools

### 2. Load Test (2 minutos)
- **Threads**: 50 usuários simultâneos
- **Ramp-up**: 30 segundos
- **Mix**: 70% GET /orders, 20% GET /orders/{id}, 10% POST /orders
- **Objetivo**: Testar carga normal

### 3. Stress Test (2 minutos)
- **Threads**: 100 usuários simultâneos
- **Ramp-up**: 30 segundos
- **Mix**: 70% GET /orders, 20% GET /orders/{id}, 10% POST /orders
- **Objetivo**: Identificar limite de throughput

## 🚀 Quick Start

### 1. Instalar JMeter

```bash
# macOS (Homebrew)
brew install jmeter

# Verificar instalação
jmeter --version
```

### 2. Iniciar Infraestrutura

```bash
# No root do projeto
cd /Volumes/Dock/repositories/poc-sdd-example

# Iniciar PostgreSQL Primary e Replica
docker-compose up -d postgres-primary postgres-replica

# Verificar databases
docker ps | grep postgres

# Iniciar aplicação
mvn spring-boot:run

# Verificar health
curl http://localhost:8080/actuator/health
```

### 3. Executar Teste

**Opção A: Script Automatizado (Recomendado)**
```bash
./jmeter/scripts/run-test.sh
```

**Opção B: Comando Manual**
```bash
jmeter -n -t jmeter/test-plans/orders-stress-test.jmx \
       -l jmeter/results/results-$(date +%Y%m%d-%H%M%S).csv \
       -e -o jmeter/results/html-report-$(date +%Y%m%d-%H%M%S)
```

**Opção C: GUI Mode (Desenvolvimento)**
```bash
jmeter -t jmeter/test-plans/orders-stress-test.jmx
# Click no botão verde "Start" (▶)
```

## 📊 Visualizar Resultados

### HTML Report (Automático)

Após execução CLI, o relatório HTML é gerado automaticamente:

```bash
open jmeter/results/html-report-*/index.html
```

**Dashboards incluem:**
- APDEX (Application Performance Index)
- Requests Summary (Total, OK, KO)
- Statistics (Min, Max, Avg, Percentiles)
- Over Time (Gráficos de throughput e response time)
- Response Times (Distribuição e percentis)

### Métricas em Tempo Real (GUI Mode)

Durante execução no modo GUI, você pode visualizar:
- **Summary Report**: Métricas agregadas
- **Aggregate Report**: Estatísticas detalhadas
- **Graph Results**: Gráficos de response time

## 📈 Monitoramento Durante Teste

Abra múltiplos terminais para monitorar diferentes aspectos:

### Terminal 1: Application Logs
```bash
tail -f logs/application.log | grep -E "ERROR|WARN|orders"
```

### Terminal 2: Database Replication Lag
```bash
watch -n 2 'docker exec orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"'
```

### Terminal 3: System Resources
```bash
# CPU e Memória
htop

# Ou Activity Monitor (GUI)
open -a "Activity Monitor"
```

### Terminal 4: Database Connections
```bash
# Connections no Primary
watch -n 2 'docker exec orders-db-primary psql -U postgres -c "SELECT count(*) FROM pg_stat_activity WHERE datname='\''orders_db'\'';"'

# Connections no Replica
watch -n 2 'docker exec orders-db-replica psql -U postgres -c "SELECT count(*) FROM pg_stat_activity WHERE datname='\''orders_db'\'';"'
```

## ✅ Critérios de Sucesso

| Métrica | Target | Crítico | Como Medir |
|---------|--------|---------|------------|
| **Response Time (Avg)** | < 300ms | < 500ms | Aggregate Report |
| **Response Time (p95)** | < 500ms | < 1000ms | HTML Report → Statistics |
| **Response Time (p99)** | < 1000ms | < 2000ms | HTML Report → Statistics |
| **Throughput** | > 50 req/s | > 30 req/s | Summary Report |
| **Error Rate** | < 1% | < 5% | Summary Report → Error % |
| **Replication Lag** | < 1s | < 5s | `pg_stat_replication` |
| **CPU Usage (App)** | < 70% | < 90% | Activity Monitor |
| **Memory Usage (App)** | < 1GB | < 2GB | Activity Monitor |

## 📁 Estrutura de Arquivos

```
jmeter/
├── test-plans/
│   └── orders-stress-test.jmx      # Test plan principal
├── data/
│   ├── customers.csv                # 20 customerIds
│   └── products.csv                 # 100 produtos com preços
├── scripts/
│   └── run-test.sh                  # Script de execução
├── results/
│   ├── results-*.csv                # Resultados CSV
│   └── html-report-*/               # Relatórios HTML
└── README-JMETER.md                 # Esta documentação
```

## 🔧 Personalização

### Ajustar Número de Usuários

Edite `jmeter/test-plans/orders-stress-test.jmx` e modifique:
- Thread Group 1 (Warmup): `num_threads` (padrão: 10)
- Thread Group 2 (LoadTest): `num_threads` (padrão: 50)
- Thread Group 3 (StressTest): `num_threads` (padrão: 100)

### Ajustar Duração

Modifique `duration` em cada Thread Group:
- Warmup: 60 segundos
- LoadTest: 120 segundos
- StressTest: 120 segundos

### Ajustar Mix de Requests

Edite os Throughput Controllers:
- GET /orders: 70%
- GET /orders/{id}: 20%
- POST /orders: 10%

## 🐛 Troubleshooting

### Erro: "Application not running"
```bash
# Verificar se aplicação está rodando
curl http://localhost:8080/actuator/health

# Iniciar aplicação
mvn spring-boot:run
```

### Erro: "JMeter not found"
```bash
# Instalar JMeter
brew install jmeter

# Verificar instalação
jmeter --version
```

### Erro: "Test plan not found"
```bash
# Verificar se arquivo existe
ls -la jmeter/test-plans/orders-stress-test.jmx

# Se não existir, o arquivo foi criado na implementação
```

### Muitos Erros no Teste
- Verificar se databases estão rodando: `docker ps | grep postgres`
- Verificar logs da aplicação: `tail -f logs/application.log`
- Reduzir número de threads se máquina local não suportar

### JMeter Consome Muita Memória
```bash
# Aumentar heap do JMeter
# Editar: /opt/homebrew/bin/jmeter (ou onde JMeter foi instalado)
# Adicionar: HEAP="-Xms1g -Xmx2g"
```

## 📊 Análise de Resultados

### Interpretar HTML Report

**APDEX (Application Performance Index)**
- Score > 0.9: Excelente
- Score 0.7-0.9: Bom
- Score < 0.7: Precisa otimização

**Response Time Percentiles**
- p50 (mediana): 50% das requests abaixo deste valor
- p95: 95% das requests abaixo deste valor
- p99: 99% das requests abaixo deste valor

**Throughput**
- Requests/segundo que a aplicação consegue processar
- Comparar com critérios de sucesso (> 50 req/s)

**Error Rate**
- % de requests que falharam
- Investigar se > 1%

### Análise Manual CSV

```bash
# Ver estatísticas básicas
cat jmeter/results/results-*.csv | grep -v "^timeStamp" | \
  awk -F',' '{sum+=$2; count++} END {print "Avg Response Time:", sum/count "ms"}'

# Contar erros
cat jmeter/results/results-*.csv | grep -c "false"

# Percentil 95 (aproximado)
cat jmeter/results/results-*.csv | grep -v "^timeStamp" | \
  awk -F',' '{print $2}' | sort -n | awk 'BEGIN{c=0} {a[c++]=$1} END{print a[int(c*0.95)]}'
```

## 🔄 Workflow Recomendado

1. **Setup inicial** (uma vez):
   ```bash
   brew install jmeter
   ```

2. **Antes de cada teste**:
   ```bash
   docker-compose up -d postgres-primary postgres-replica
   mvn spring-boot:run
   curl http://localhost:8080/actuator/health
   ```

3. **Executar teste**:
   ```bash
   ./jmeter/scripts/run-test.sh
   ```

4. **Analisar resultados**:
   ```bash
   open jmeter/results/html-report-*/index.html
   ```

5. **Iterar**:
   - Identificar bottlenecks
   - Otimizar aplicação
   - Re-executar e comparar resultados

## 📚 Recursos Adicionais

- [JMeter Documentation](https://jmeter.apache.org/usermanual/index.html)
- [Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [JMeter Functions](https://jmeter.apache.org/usermanual/functions.html)

## 🤝 Contribuindo

Para adicionar novos cenários de teste:

1. Abra o test plan no JMeter GUI:
   ```bash
   jmeter -t jmeter/test-plans/orders-stress-test.jmx
   ```

2. Adicione novos Thread Groups ou Samplers

3. Salve o test plan

4. Execute e valide

## 📝 Notas

- **Não commitar** arquivos em `jmeter/results/` (já está no .gitignore)
- **Desabilitar** "View Results Tree" antes de executar testes longos
- **Executar em CLI mode** para testes finais (melhor performance)
- **Monitorar recursos** da máquina durante testes
- **Comparar resultados** entre execuções para medir melhorias

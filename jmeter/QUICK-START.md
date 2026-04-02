# Quick Start - JMeter Stress Test

Guia rápido para executar o stress test em 3 passos.

## ⚡ Execução Rápida

### 1️⃣ Instalar JMeter (apenas primeira vez)

```bash
brew install jmeter
```

### 2️⃣ Preparar Ambiente

```bash
# Iniciar databases
docker-compose up -d postgres-primary postgres-replica

# Iniciar aplicação
mvn spring-boot:run
```

### 3️⃣ Executar Teste

```bash
# Executar stress test (5 minutos)
./jmeter/scripts/run-test.sh
```

O relatório HTML será aberto automaticamente no navegador ao final.

## 📊 Interpretar Resultados

### Métricas Principais

- **Response Time (p95)**: Deve ser < 500ms (crítico: < 1000ms)
- **Throughput**: Deve ser > 50 req/s (crítico: > 30 req/s)
- **Error Rate**: Deve ser < 1% (crítico: < 5%)

### Status

- ✅ **Verde**: Dentro do target
- ⚠️ **Amarelo**: Entre target e crítico
- ❌ **Vermelho**: Acima do crítico (precisa otimização)

## 🔧 Opções Avançadas

### Executar via GUI (Desenvolvimento)

```bash
jmeter -t jmeter/test-plans/orders-stress-test.jmx
```

### Popular Banco com Dados Iniciais

```bash
./jmeter/scripts/setup-data.sh
```

### Monitorar Durante Teste

```bash
# Terminal 1: Logs da aplicação
tail -f logs/application.log

# Terminal 2: Replicação
watch -n 2 'docker exec orders-db-primary psql -U postgres -c "SELECT * FROM pg_stat_replication;"'

# Terminal 3: Recursos do sistema
htop
```

## 📚 Documentação Completa

Ver [README-JMETER.md](README-JMETER.md) para documentação detalhada.

## 🐛 Problemas Comuns

### "Application not running"
```bash
mvn spring-boot:run
```

### "JMeter not found"
```bash
brew install jmeter
```

### Muitos erros no teste
- Reduzir número de threads no test plan
- Verificar logs: `tail -f logs/application.log`
- Verificar databases: `docker ps | grep postgres`

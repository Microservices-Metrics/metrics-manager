# Guia de Início Rápido - Sistema de Agendamento

Este guia mostra um exemplo completo de como configurar e usar o sistema de agendamento de coletas.

## Passo 1: Iniciar a Aplicação

```bash
# Compilar
./mvnw clean package

# Executar
java -jar target/manager-0.0.1-SNAPSHOT.jar
```

Ao iniciar, você verá nos logs:
```
INFO  CollectorInitializationService - Initializing scheduled collections...
INFO  CollectorInitializationService - Successfully scheduled 0 collection tasks
```

## Passo 2: Preparar os Dados Base

Antes de criar um `CollectorConfig`, você precisa ter:

### 2.1. Uma Métrica
```bash
curl -X POST http://localhost:8080/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CPU Usage",
    "description": "Porcentagem de uso de CPU",
    "unit": "%"
  }'
```
**Response:** `{ "id": "metric-uuid-123", ... }`

### 2.2. Um Coletor
```bash
curl -X POST http://localhost:8080/collectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Prometheus CPU Collector",
    "description": "Coleta CPU via Prometheus",
    "collectionMethod": "GET",
    "metricId": "metric-uuid-123"
  }'
```
**Response:** `{ "id": "collector-uuid-456", ... }`

### 2.3. Metadados do Coletor
```bash
curl -X POST http://localhost:8080/collector-metadata \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "collector-uuid-456",
    "key": "endpoint",
    "value": "/api/v1/query",
    "type": "url"
  }'

curl -X POST http://localhost:8080/collector-metadata \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "collector-uuid-456",
    "key": "query",
    "value": "cpu_usage",
    "type": "parameter"
  }'
```

### 2.4. Um Microsserviço
```bash
curl -X POST http://localhost:8080/microservices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Payment Service",
    "description": "Serviço de pagamentos"
  }'
```
**Response:** `{ "id": "microservice-uuid-789", ... }`

### 2.5. Metadados do Microsserviço
```bash
curl -X POST http://localhost:8080/microservice-metadata \
  -H "Content-Type: application/json" \
  -d '{
    "microserviceId": "microservice-uuid-789",
    "key": "host",
    "value": "http://payment-service.example.com:9090",
    "type": "url"
  }'
```

## Passo 3: Criar Agendamento de Coleta

Agora sim, crie o `CollectorConfig`:

```bash
curl -X POST http://localhost:8080/collector-configs \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "collector-uuid-456",
    "microserviceId": "microservice-uuid-789",
    "cronExpression": "0 */5 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
  }'
```

**Response:**
```json
{
  "id": "config-uuid-abc",
  "collectorId": "collector-uuid-456",
  "microserviceId": "microservice-uuid-789",
  "cronExpression": "0 */5 * * * *",
  "startDateTime": "2025-01-12T00:00:00",
  "endDateTime": "2025-12-31T23:59:59"
}
```

**Logs esperados:**
```
INFO  CollectorSchedulingService - Scheduled collection task for CollectorConfig ID: config-uuid-abc with cron: 0 */5 * * * *
```

## Passo 4: Aguardar e Verificar Coletas

Aguarde 5 minutos (conforme o cron) e verifique os logs:

```
INFO  CollectorSchedulingService - Executing collection for CollectorConfig ID: config-uuid-abc
INFO  CollectorSchedulingService - Collection successful for CollectorConfig ID: config-uuid-abc
```

## Passo 5: Consultar Resultados

### Listar todas as medições:
```bash
curl http://localhost:8080/measurements
```

### Ver medições de uma configuração específica:
```bash
curl http://localhost:8080/measurements?collectorConfigId=config-uuid-abc
```

**Response esperada:**
```json
[
  {
    "id": "measurement-uuid-1",
    "collectorConfigId": "config-uuid-abc",
    "startTimestamp": "2025-01-12T10:05:00",
    "responseStatus": "SUCCESS",
    "responseBody": "{\"status\":\"success\",\"data\":{\"result\":[{\"metric\":{\"__name__\":\"cpu_usage\"},\"value\":[1736676300,\"45.2\"]}]}}",
    "metricValue": null
  },
  {
    "id": "measurement-uuid-2",
    "collectorConfigId": "config-uuid-abc",
    "startTimestamp": "2025-01-12T10:10:00",
    "responseStatus": "SUCCESS",
    "responseBody": "{\"status\":\"success\",\"data\":{\"result\":[{\"metric\":{\"__name__\":\"cpu_usage\"},\"value\":[1736676600,\"42.8\"]}]}}",
    "metricValue": null
  }
]
```

## Passo 6: Gerenciar Agendamentos

### Atualizar frequência (de 5 em 5 minutos para 10 em 10):
```bash
curl -X PUT http://localhost:8080/collector-configs/config-uuid-abc \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "collector-uuid-456",
    "microserviceId": "microservice-uuid-789",
    "cronExpression": "0 */10 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
  }'
```

**Logs:**
```
INFO  CollectorSchedulingService - Cancelled scheduled task for CollectorConfig ID: config-uuid-abc
INFO  CollectorSchedulingService - Scheduled collection task for CollectorConfig ID: config-uuid-abc with cron: 0 */10 * * * *
```

### Pausar coletas (deletar configuração):
```bash
curl -X DELETE http://localhost:8080/collector-configs/config-uuid-abc
```

**Logs:**
```
INFO  CollectorSchedulingService - Cancelled scheduled task for CollectorConfig ID: config-uuid-abc
```

## Cenário Completo com Docker

Se você estiver usando Docker:

```bash
# Subir banco de dados
docker-compose up -d postgres

# Aguardar banco iniciar
sleep 10

# Compilar e executar aplicação
./mvnw clean package
java -jar target/manager-0.0.1-SNAPSHOT.jar
```

## Exemplo Real: Monitorar Múltiplos Microsserviços

### Cenário:
- Payment Service: coleta a cada 5 minutos
- Order Service: coleta a cada 10 minutos
- Auth Service: coleta a cada hora

```bash
# Payment Service - CPU
curl -X POST http://localhost:8080/collector-configs \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "cpu-collector-id",
    "microserviceId": "payment-service-id",
    "cronExpression": "0 */5 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
  }'

# Order Service - Memory
curl -X POST http://localhost:8080/collector-configs \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "memory-collector-id",
    "microserviceId": "order-service-id",
    "cronExpression": "0 */10 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
  }'

# Auth Service - Request Count
curl -X POST http://localhost:8080/collector-configs \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "requests-collector-id",
    "microserviceId": "auth-service-id",
    "cronExpression": "0 0 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
  }'
```

**Resultado:**
- 3 agendamentos ativos
- Payment: coleta a cada 5 minutos
- Order: coleta a cada 10 minutos
- Auth: coleta a cada hora

**Logs esperados:**
```
INFO  CollectorSchedulingService - Scheduled collection task for CollectorConfig ID: xxx with cron: 0 */5 * * * *
INFO  CollectorSchedulingService - Scheduled collection task for CollectorConfig ID: yyy with cron: 0 */10 * * * *
INFO  CollectorSchedulingService - Scheduled collection task for CollectorConfig ID: zzz with cron: 0 0 * * * *
```

## Verificação de Saúde

### Endpoint Actuator (se configurado):
```bash
curl http://localhost:8080/actuator/health
```

### Verificar banco de dados:
```sql
-- Quantas configurações ativas?
SELECT COUNT(*) FROM TB_COLLECTOR_CONFIGS;

-- Quantas medições já foram coletadas?
SELECT COUNT(*) FROM TB_MEASUREMENTS;

-- Taxa de sucesso das coletas
SELECT 
  response_status, 
  COUNT(*) as total,
  ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM TB_MEASUREMENTS), 2) as percentage
FROM TB_MEASUREMENTS
GROUP BY response_status;
```

## Dicas de Produção

1. **Ajustar pool de threads** conforme número de configurações:
   ```java
   // SchedulingConfig.java
   scheduler.setPoolSize(50); // Para muitas configurações
   ```

2. **Monitorar logs** com ferramenta apropriada:
   ```bash
   tail -f application.log | grep "CollectorSchedulingService"
   ```

3. **Backup dos Measurements** regularmente:
   ```bash
   pg_dump -t TB_MEASUREMENTS metric_manager_db > measurements_backup.sql
   ```

4. **Alertas** para coletas com erro:
   ```sql
   -- Ver coletas falhadas nas últimas 24h
   SELECT * FROM TB_MEASUREMENTS 
   WHERE response_status = 'ERROR' 
   AND start_timestamp > NOW() - INTERVAL '24 hours';
   ```

5. **Limpeza de dados antigos**:
   ```sql
   -- Deletar medições com mais de 90 dias
   DELETE FROM TB_MEASUREMENTS 
   WHERE start_timestamp < NOW() - INTERVAL '90 days';
   ```

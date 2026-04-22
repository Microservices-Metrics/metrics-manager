# Exemplos de Uso - Sistema de Agendamento

Este documento fornece exemplos práticos de como usar o sistema de agendamento de coletas.

## Pré-requisitos

1. Aplicação rodando
2. Microsserviço de destino configurado e disponível
3. Coletor e Métrica criados
4. Microserviço registrado no sistema

## Cenários de Teste

### 1. Criar Agendamento Básico (Coleta a cada hora)

**Request:**
```bash
POST http://localhost:8080/collector-configs
Content-Type: application/json

{
    "collectorId": "31a93c5f-4995-4892-931b-8fc8c5c87f33",
    "microserviceId": "ba3a2440-b653-4f2a-9b5f-518eb07a3216",
    "cronExpression": "0 0 * * * *",
    "startDateTime": "2025-01-01T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
}
```

**Resultado esperado:**
- Retorna HTTP 201 Created
- Agendamento criado automaticamente
- Primeira coleta executará na próxima hora cheia
- Logs mostram: "Scheduled collection task for CollectorConfig ID: ..."

---

### 2. Criar Agendamento com Coleta Frequente (A cada 5 minutos)

**Request:**
```bash
POST http://localhost:8080/collector-configs
Content-Type: application/json

{
    "collectorId": "31a93c5f-4995-4892-931b-8fc8c5c87f33",
    "microserviceId": "ba3a2440-b653-4f2a-9b5f-518eb07a3216",
    "cronExpression": "0 */5 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
}
```

**Resultado esperado:**
- Coletas executam a cada 5 minutos
- Útil para métricas que mudam frequentemente

---

### 3. Agendamento com Período Limitado

**Request:**
```bash
POST http://localhost:8080/collector-configs
Content-Type: application/json

{
    "collectorId": "31a93c5f-4995-4892-931b-8fc8c5c87f33",
    "microserviceId": "ba3a2440-b653-4f2a-9b5f-518eb07a3216",
    "cronExpression": "0 0 9 * * *",
    "startDateTime": "2025-01-15T00:00:00",
    "endDateTime": "2025-01-31T23:59:59"
}
```

**Resultado esperado:**
- Coletas executam diariamente às 9h da manhã
- Apenas entre 15/01 e 31/01
- Após 31/01, agendamento é cancelado automaticamente

---

### 4. Atualizar Frequência de Coleta

**Request:**
```bash
PUT http://localhost:8080/collector-configs/{config-id}
Content-Type: application/json

{
    "collectorId": "31a93c5f-4995-4892-931b-8fc8c5c87f33",
    "microserviceId": "ba3a2440-b653-4f2a-9b5f-518eb07a3216",
    "cronExpression": "0 */10 * * * *",
    "startDateTime": "2025-01-01T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
}
```

**Resultado esperado:**
- Agendamento anterior é cancelado
- Novo agendamento criado com cron atualizado (a cada 10 minutos)
- Próxima execução seguirá o novo intervalo

---

### 5. Verificar Resultados das Coletas

Após alguns minutos, você pode consultar os resultados:

**Request:**
```bash
GET http://localhost:8080/measurements?collectorConfigId={config-id}
```

**Response esperada:**
```json
[
  {
    "id": "abc-123",
    "collectorConfigId": "def-456",
    "startTimestamp": "2025-01-12T10:00:00",
    "responseStatus": "SUCCESS",
    "responseBody": "{\"cpu\": 45.2, \"memory\": 78.5}",
    "metricValue": null
  },
  {
    "id": "abc-124",
    "collectorConfigId": "def-456",
    "startTimestamp": "2025-01-12T11:00:00",
    "responseStatus": "SUCCESS",
    "responseBody": "{\"cpu\": 42.1, \"memory\": 76.3}",
    "metricValue": null
  }
]
```

---

### 6. Cancelar Agendamento

Para cancelar um agendamento, simplesmente delete a configuração:

**Request:**
```bash
DELETE http://localhost:8080/collector-configs/{config-id}
```

**Resultado esperado:**
- HTTP 204 No Content
- Agendamento cancelado automaticamente
- Nenhuma nova coleta será executada
- Measurements históricos são preservados

---

## Testando com cURL

### Criar configuração:
```bash
curl -X POST http://localhost:8080/collector-configs \
  -H "Content-Type: application/json" \
  -d '{
    "collectorId": "31a93c5f-4995-4892-931b-8fc8c5c87f33",
    "microserviceId": "ba3a2440-b653-4f2a-9b5f-518eb07a3216",
    "cronExpression": "0 */5 * * * *",
    "startDateTime": "2025-01-12T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
  }'
```

### Listar todas as configurações:
```bash
curl http://localhost:8080/collector-configs
```

### Deletar configuração:
```bash
curl -X DELETE http://localhost:8080/collector-configs/{config-id}
```

---

## Monitorando Logs

Para acompanhar as execuções em tempo real, monitore os logs da aplicação:

```bash
# Filtrar logs de agendamento
grep "CollectorSchedulingService" application.log

# Ver apenas execuções de coleta
grep "Executing collection" application.log

# Ver falhas
grep "ERROR" application.log | grep "Collection"
```

**Logs típicos de sucesso:**
```
INFO  CollectorSchedulingService - Scheduled collection task for CollectorConfig ID: abc-123 with cron: 0 0 * * * *
INFO  CollectorSchedulingService - Executing collection for CollectorConfig ID: abc-123
INFO  CollectorSchedulingService - Collection successful for CollectorConfig ID: abc-123
```

**Logs de falha:**
```
ERROR CollectorSchedulingService - Collection failed for CollectorConfig ID: abc-123
java.net.ConnectException: Connection refused
```

---

## Expressões Cron Úteis

| Use Case | Cron Expression | Descrição |
|----------|----------------|-----------|
| Teste rápido | `0 * * * * *` | A cada minuto |
| Desenvolvimento | `0 */5 * * * *` | A cada 5 minutos |
| Monitoramento frequente | `0 */15 * * * *` | A cada 15 minutos |
| Padrão | `0 0 * * * *` | A cada hora |
| Baixa frequência | `0 0 */6 * * *` | A cada 6 horas |
| Diário | `0 0 9 * * *` | Diariamente às 9h |
| Semanal | `0 0 9 * * MON` | Segundas-feiras às 9h |
| Mensal | `0 0 9 1 * *` | Dia 1 de cada mês às 9h |
| Horário comercial | `0 0 9-17 * * MON-FRI` | Cada hora das 9h às 17h, seg-sex |

---

## Verificando Status do Agendamento

Embora não exista endpoint específico (ainda), você pode verificar se um agendamento está ativo:

1. Verifique os logs para confirmação de criação
2. Aguarde a execução conforme o cron
3. Consulte a tabela `TB_MEASUREMENTS` para ver coletas recentes

**Query SQL:**
```sql
-- Ver últimas 10 medições
SELECT * FROM TB_MEASUREMENTS 
ORDER BY start_timestamp DESC 
LIMIT 10;

-- Ver medições de uma configuração específica
SELECT * FROM TB_MEASUREMENTS 
WHERE collector_config_id = 'abc-123'
ORDER BY start_timestamp DESC;

-- Ver apenas coletas com sucesso
SELECT * FROM TB_MEASUREMENTS 
WHERE response_status = 'SUCCESS'
ORDER BY start_timestamp DESC;

-- Ver apenas coletas com erro
SELECT * FROM TB_MEASUREMENTS 
WHERE response_status = 'ERROR'
ORDER BY start_timestamp DESC;
```

---

## Troubleshooting

### Problema: Coleta não está executando

**Verificar:**
1. Logs mostram agendamento criado?
   ```
   grep "Scheduled collection task" application.log
   ```

2. Hora atual está dentro do período?
   ```sql
   SELECT start_date_time, end_date_time 
   FROM TB_COLLECTOR_CONFIGS 
   WHERE id = 'seu-id';
   ```

3. Cron expression é válida?
   - Use ferramentas online: https://crontab.guru/
   - Formato Spring: segundo minuto hora dia mês dia-da-semana

### Problema: Coletas falhando

**Verificar:**
1. Microsserviço está acessível?
   ```bash
   curl http://microsservice-url/endpoint
   ```

2. Metadados estão corretos?
   ```sql
   SELECT * FROM TB_COLLECTOR_METADATA 
   WHERE id_collector = 'seu-collector-id';
   ```

3. Ver logs de erro detalhados:
   ```
   grep "Collection failed" application.log -A 5
   ```

### Problema: Muitos agendamentos ativos

**Solução:**
```bash
# Cancelar todos
curl -X DELETE http://localhost:8080/collector-configs

# Recriar apenas os necessários
curl -X POST http://localhost:8080/collector-configs -d '...'
```

# Sistema de Agendamento de Coletas de Métricas

## Visão Geral

Este sistema implementa um serviço de agendamento automático de coletas de métricas baseado em configurações de `CollectorConfig`. Quando um `CollectorConfig` é registrado, o sistema automaticamente agenda requisições periódicas ao microsserviço conforme a `cronExpression` configurada.

## Componentes Implementados

### 1. CollectorSchedulingService
**Localização:** `src/main/java/com/example/manager/services/CollectorSchedulingService.java`

Serviço principal responsável por:
- Agendar tarefas de coleta com base em expressões cron
- Executar as coletas no horário agendado
- Validar se a coleta está dentro do período `startDateTime` e `endDateTime`
- Cancelar agendamentos quando necessário
- Registrar os resultados das coletas na tabela `Measurement`

**Principais métodos:**
- `scheduleCollection(CollectorConfig)` - Agenda uma nova coleta
- `cancelScheduledTask(UUID)` - Cancela um agendamento específico
- `cancelAllScheduledTasks()` - Cancela todos os agendamentos
- `isScheduled(UUID)` - Verifica se uma configuração está agendada

### 2. SchedulingConfig
**Localização:** `src/main/java/com/example/manager/config/SchedulingConfig.java`

Configuração do Spring que:
- Habilita o suporte a agendamento de tarefas (`@EnableScheduling`)
- Configura um pool de threads dedicado para execução das tarefas
- Define 10 threads simultâneas para coletas paralelas

### 3. CollectorInitializationService
**Localização:** `src/main/java/com/example/manager/services/CollectorInitializationService.java`

Serviço que:
- Inicializa automaticamente todos os agendamentos quando a aplicação inicia
- Carrega todos os `CollectorConfig` do banco e agenda suas coletas
- Executa automaticamente após o evento `ApplicationReadyEvent`

### 4. Integração com CollectorConfigController
**Localização:** `src/main/java/com/example/manager/controllers/CollectorConfigController.java`

O controller foi atualizado para:
- **POST** - Agenda automaticamente a coleta ao criar um novo `CollectorConfig`
- **PUT** - Reagenda a coleta com as novas configurações ao atualizar
- **DELETE (específico)** - Cancela o agendamento antes de deletar
- **DELETE (todos)** - Cancela todos os agendamentos antes de deletar tudo

## Fluxo de Funcionamento

### 1. Registro de uma Nova Configuração
```
POST /collector-configs
{
    "collectorId": "31a93c5f-4995-4892-931b-8fc8c5c87f33",
    "microserviceId": "ba3a2440-b653-4f2a-9b5f-518eb07a3216",
    "cronExpression": "0 0 * * * *",
    "startDateTime": "2025-01-01T00:00:00",
    "endDateTime": "2025-12-31T23:59:59"
}
```

**O que acontece:**
1. Controller valida e salva o `CollectorConfig` no banco
2. `CollectorSchedulingService.scheduleCollection()` é chamado
3. Uma tarefa agendada é criada com base na `cronExpression`
4. A tarefa será executada periodicamente conforme o cron

### 2. Execução da Coleta
No horário agendado, para cada configuração:

1. **Validação de período:** Verifica se está entre `startDateTime` e `endDateTime`
2. **Execução da coleta:** Chama `CollectorRequestService.executeCollection()`
3. **Registro do resultado:** Cria um registro `Measurement` com:
   - `startTimestamp` - horário da coleta
   - `responseStatus` - "SUCCESS" ou "ERROR"
   - `responseBody` - resposta do microsserviço ou mensagem de erro
   - `collectorConfig` - referência à configuração

### 3. Cancelamento Automático
O agendamento é cancelado automaticamente quando:
- A data atual ultrapassa `endDateTime`
- O `CollectorConfig` é deletado
- A aplicação é encerrada (com graceful shutdown)

## Exemplo de Expressões Cron

| Expressão | Descrição |
|-----------|-----------|
| `0 0 * * * *` | A cada hora |
| `0 */30 * * * *` | A cada 30 minutos |
| `0 0 */6 * * *` | A cada 6 horas |
| `0 0 9 * * *` | Diariamente às 9h |
| `0 0 0 * * MON` | Toda segunda-feira à meia-noite |
| `0 0 12 1 * *` | Todo dia 1 do mês ao meio-dia |

**Formato:** `segundo minuto hora dia mês dia-da-semana`

## Armazenamento dos Resultados

Cada coleta gera um registro na tabela `TB_MEASUREMENTS`:

```sql
TB_MEASUREMENTS
├── id (UUID)
├── collector_config_id (UUID) - FK para TB_COLLECTOR_CONFIGS
├── start_timestamp (TIMESTAMP)
├── response_status (VARCHAR) - "SUCCESS" ou "ERROR"
├── response_body (TEXT) - resposta do microsserviço
└── metric_value (VARCHAR) - valor extraído da métrica (futuro)
```

## Logs e Monitoramento

O sistema registra logs importantes:
- `INFO` - Quando um agendamento é criado/cancelado
- `INFO` - Quando uma coleta é executada com sucesso
- `ERROR` - Quando uma coleta falha
- `WARN` - Quando uma configuração não é encontrada

**Exemplo de logs:**
```
INFO  - Scheduled collection task for CollectorConfig ID: abc-123 with cron: 0 0 * * * *
INFO  - Executing collection for CollectorConfig ID: abc-123
INFO  - Collection successful for CollectorConfig ID: abc-123
```

## Considerações de Performance

### Threads Simultâneas
- **Pool size:** 10 threads
- Coletas são executadas em paralelo
- Não há bloqueio entre diferentes configurações

### Memória
- Cada agendamento mantém uma referência `ScheduledFuture` em memória
- Para muitas configurações (>1000), considere aumentar heap memory

### Banco de Dados
- Measurements são salvos individualmente após cada coleta
- Considere criar índices em:
  - `collector_config_id`
  - `start_timestamp`

## Failover e Recuperação

### Reinício da Aplicação
Ao reiniciar, `CollectorInitializationService` automaticamente:
- Carrega todas as configurações ativas do banco
- Reagenda todas as coletas
- Não há perda de configurações

### Falhas na Coleta
- Erros são registrados no `Measurement` com status "ERROR"
- A próxima execução agendada não é afetada
- Logs detalham o erro para debugging

## Melhorias Futuras

1. **Extração de métrica:** Implementar lógica para extrair `metricValue` do `responseBody` usando `CollectorResponseSchema`
2. **Retry logic:** Tentar novamente coletas que falharam
3. **Alertas:** Notificar quando coletas falham consecutivamente
4. **Dashboard:** Interface para visualizar status dos agendamentos
5. **Rate limiting:** Controlar taxa de requisições por microsserviço
6. **Distributed scheduling:** Suporte para múltiplas instâncias da aplicação

## Troubleshooting

### Coleta não está sendo executada
1. Verifique se a `cronExpression` é válida
2. Confirme que está dentro do período `startDateTime` - `endDateTime`
3. Verifique os logs para erros de agendamento

### Muitas threads em uso
1. Reduza o `poolSize` em `SchedulingConfig`
2. Considere ajustar a frequência das coletas
3. Monitore o uso de CPU

### Coletas falhando
1. Verifique conectividade com o microsserviço
2. Confirme que os metadados estão configurados corretamente
3. Revise os logs de erro em `TB_MEASUREMENTS`

---

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

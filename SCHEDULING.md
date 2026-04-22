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

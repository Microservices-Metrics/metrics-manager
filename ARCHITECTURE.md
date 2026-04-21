# Arquitetura e Estrutura — Metric Manager

## Visão Geral

O **Metric Manager** é um microsserviço desenvolvido em **Java 17** com **Spring Boot 3.4** cuja responsabilidade é gerenciar a coleta automatizada de métricas de outros microsserviços. Ele mantém um catálogo de métricas e coletores, permite configurar agendamentos de coleta via expressões cron, executa as coletas em tempo real e persiste os resultados (medições) em um banco de dados **PostgreSQL**.

---

## Stack Tecnológica

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 3.4 | Framework de aplicação |
| Spring Data JPA / Hibernate | Persistência |
| Spring Scheduling | Agendamento de coletas |
| PostgreSQL | Banco de dados relacional |
| ModelMapper 3.2 | Mapeamento entidade ↔ DTO |
| SpringDoc OpenAPI (Swagger) | Documentação da API REST |
| NetworkNT JSON Schema Validator | Validação de respostas dos coletores |
| Spring Boot Actuator | Monitoramento e health checks |
| Docker / Docker Compose | Containerização |

---

## Estrutura de Pacotes

```
com.example.manager/
├── ManagerApplication.java        # Ponto de entrada da aplicação
├── config/                        # Configurações Spring
├── controllers/                   # Camada HTTP (REST)
├── dtos/                          # Objetos de transferência de dados
├── interfaces/                    # Contratos (abstrações)
├── models/                        # Entidades JPA (domínio)
├── repositories/                  # Acesso a dados (Spring Data)
├── services/                      # Lógica de negócio
└── util/                          # Utilitários e helpers
```

---

## Modelo de Domínio

O diagrama abaixo representa as entidades e seus relacionamentos:

```
Metric (TB_METRICS)
 └─ 1:N ──► Collector (TB_COLLECTORS)
              ├─ 1:N ──► CollectorMetadata (TB_COLLECTOR_METADATA)
              │            keyName / keyValue  (ex.: url, method, pathToMetric)
              ├─ 1:N ──► CollectorResponseSchema (TB_COLLECTOR_RESPONSE_SCHEMAS)
              │            schema (JSON Schema) / statusType / description
              └─ 1:N ──► CollectorConfig (TB_COLLECTOR_CONFIGS)
                           ├─ N:1 ──► Microservice (TB_MICROSERVICES)
                           │            └─ 1:N ──► MicroserviceMetadata (TB_MICROSERVICE_METADATA)
                           │                         varName / varValue  (variáveis da instância)
                           ├── cronExpression
                           ├── startDateTime / endDateTime
                           └─ 1:N ──► Measurement (TB_MEASUREMENTS)
                                        startTimestamp / responseStatus
                                        responseBody / metricValue
```

### Descrição das Entidades

| Entidade | Tabela | Descrição |
|---|---|---|
| `Metric` | `TB_METRICS` | Define uma métrica (nome, descrição, tipo, unidade) |
| `Collector` | `TB_COLLECTORS` | Implementação de coleta para uma métrica (método HTTP, etc.) |
| `CollectorMetadata` | `TB_COLLECTOR_METADATA` | Pares chave-valor que configuram o coletor (URL, método, caminho da métrica) |
| `CollectorResponseSchema` | `TB_COLLECTOR_RESPONSE_SCHEMAS` | JSON Schema para validar a resposta recebida pelo coletor |
| `Microservice` | `TB_MICROSERVICES` | Microsserviço alvo da coleta |
| `MicroserviceMetadata` | `TB_MICROSERVICE_METADATA` | Variáveis da instância do microsserviço (substituição de templates na URL, body, etc.) |
| `CollectorConfig` | `TB_COLLECTOR_CONFIGS` | Associa um Coletor a um Microsserviço e define a agenda (cron) |
| `Measurement` | `TB_MEASUREMENTS` | Resultado de uma coleta executada: timestamp, status HTTP, corpo e valor extraído |

---

## Camadas da Aplicação

### 1. Controllers (Camada de Apresentação)

Cada controller mapeia um recurso REST e delega ao repositório ou serviço adequado.

| Controller | Prefixo de rota | Recurso gerenciado |
|---|---|---|
| `MetricController` | `/metrics` | Métricas |
| `CollectorController` | `/collectors` | Coletores e seus sub-recursos |
| `CollectorConfigController` | `/collector-configs` | Configurações de agendamento |
| `CollectorMetadataController` | `/collector-metadata` | Metadados de coletores |
| `CollectorResponseSchemaController` | `/collector-response-schemas` | Schemas de resposta |
| `MicroserviceController` | `/microservices` | Microsserviços |
| `MicroserviceMetadataController` | `/microservice-metadata` | Metadados de microsserviços |

### 2. Services (Camada de Negócio)

| Serviço | Responsabilidade |
|---|---|
| `CollectorInitializationService` | Ouve o evento `ApplicationReadyEvent` e agenda todas as coletas persistidas ao iniciar a aplicação |
| `CollectorSchedulingService` | Gerencia o ciclo de vida dos agendamentos cron em memória (`ConcurrentHashMap<UUID, ScheduledFuture>`) |
| `CollectorRequestService` | Monta e executa a requisição HTTP ao microsserviço alvo usando os metadados configurados |
| `RestService` | Implementa `IRestService`; encapsula o `RestTemplate` para executar chamadas HTTP |

### 3. Repositories (Camada de Dados)

Interfaces Spring Data JPA para cada entidade do domínio:

`IMetricRepository`, `ICollectorRepository`, `ICollectorConfigRepository`, `ICollectorMetadataRepository`, `ICollectorResponseSchemaRepository`, `IMicroserviceRepository`, `IMicroserviceMetadataRepository`, `IMeasurementRepository`, `ICollectionRepository`

### 4. DTOs

Padrão de três camadas por recurso:

- `*Dto` — DTO genérico / mapeamento interno
- `*RequestDto` — Payload de entrada (POST/PUT)
- `*ResponseDto` — Payload de saída (GET)

### 5. Interfaces

| Interface | Implementação | Propósito |
|---|---|---|
| `IRestService` | `RestService` | Abstrai chamadas HTTP |
| `IRequestBodyBuilder` | `RequestBodyBuilder` | Constrói URL e corpo da requisição a partir dos metadados |

### 6. Utilitários

| Utilitário | Descrição |
|---|---|
| `JsonSchemaService` | Valida o corpo da resposta de um coletor contra o JSON Schema registrado em `CollectorResponseSchema` |
| `RequestBodyBuilder` | Combina `CollectorMetadata` com `MicroserviceMetadata` para produzir a URL e o body da requisição |

### 7. Configurações (`config/`)

| Classe | Finalidade |
|---|---|
| `CorsConfig` | Habilita CORS para todos os origins |
| `ModelMapperConfig` | Bean global do ModelMapper |
| `OpenApiConfig` | Metadados da documentação Swagger/OpenAPI |
| `RestTemplateConfig` | Bean do `RestTemplate` |
| `SchedulingConfig` | Habilita e configura o `TaskScheduler` para o Spring Scheduling |

---

## Fluxo de Coleta de Métricas

```
[Startup]
  CollectorInitializationService
    └── carrega todos os CollectorConfig do banco
          └── CollectorSchedulingService.scheduleCollection(config)
                └── registra CronTrigger no TaskScheduler

[Execução agendada pelo cron]
  CollectorSchedulingService.executeCollectionTask(configId)
    └── CollectorRequestService.executeCollection(collectorConfig)
          ├── lê CollectorMetadata  (chaves de configuração do coletor)
          ├── lê MicroserviceMetadata (variáveis da instância)
          ├── RequestBodyBuilder.buildRequestData(...)  → URL + body
          └── RestService.executeHttpRequest(url, body, method)
                └── Measurement salvo no banco (timestamp, status, body, metricValue)
```

Novos agendamentos podem ser criados ou cancelados em tempo de execução via API REST (`CollectorConfigController`), sem necessidade de reiniciar a aplicação.

---

## Infraestrutura

### Banco de Dados

- **PostgreSQL** — banco `microservices-metrics-db`
- Gerenciamento de schema via `spring.jpa.hibernate.ddl-auto=update`
- Conexão padrão: `localhost:5432` (configurável em `application.properties`)

### Docker

```
docker-compose.yml
├── app     — imagem da aplicação (porta 8080)
└── postgres — PostgreSQL (porta 5432)
```

O `Dockerfile` usa uma imagem base do OpenJDK 17 e empacota o JAR gerado pelo Maven.

### Documentação da API

Após iniciar a aplicação, a documentação Swagger está disponível em:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`

---

## Diagrama de Dependências entre Camadas

```
Controllers
    │
    ├──► Repositories  (acesso direto ao banco para CRUD simples)
    │
    └──► Services
              ├──► Repositories
              ├──► IRestService (RestService → RestTemplate)
              └──► IRequestBodyBuilder (RequestBodyBuilder)
                        └──► JsonSchemaService (validação de resposta)
```

---

## Logs e Monitoramento

- Nível raiz: `INFO`
- Pacote `com.example.manager`: `DEBUG`
- Spring Boot Actuator expõe endpoints de health e métricas internas

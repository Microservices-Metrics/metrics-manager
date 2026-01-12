# Metric Manager

<!-- TODO: Colocar contexto/introdução sobre o projeto. -->

Sistema de gerenciamento de métricas para microserviços, desenvolvido com Spring Boot.

## 📋 Pré-requisitos

### Para rodar sem Docker

- Java 17 ou superior
- Maven 3.6+
- PostgreSQL 12+

### Para rodar com Docker

- Docker 20.10+
- Docker Compose 2.0+

## 🚀 Como Rodar

### Opção 1: Com Docker (Recomendado)

Esta opção sobe automaticamente a aplicação e o banco de dados PostgreSQL.

```bash
# Clone o repositório
git clone https://github.com/Microservices-Metrics/metric-manager.git
cd metric-manager

# Suba os containers
docker-compose up --build

# Ou em background
docker-compose up -d --build
```

A aplicação estará disponível em: `http://localhost:8080`

**Comandos úteis:**

```bash
# Ver logs da aplicação
docker-compose logs -f app

# Ver logs do PostgreSQL
docker-compose logs -f postgres

# Parar os containers
docker-compose down

# Parar e remover volumes (limpa o banco de dados)
docker-compose down -v

# Reconstruir após mudanças no código
docker-compose up --build
```

---

### Opção 2: Sem Docker (Local)

#### 1. Configure o PostgreSQL

Certifique-se de que o PostgreSQL está rodando e crie o banco de dados:

```sql
CREATE DATABASE "microservices-metrics-db";
```

#### 2. Configure as credenciais

Edite o arquivo `src/main/resources/application.properties` se necessário:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/microservices-metrics-db
spring.datasource.username=postgres
spring.datasource.password=123
```

#### 3. Execute a aplicação

##### Opção A: Usando Maven Wrapper

```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

##### Opção B: Compilar e executar o JAR

```bash
# Compilar
./mvnw clean package -DskipTests

# Executar
java -jar target/*.jar
```

##### Opção C: Via IDE

Execute a classe principal: `com.example.manager.ManagerApplication`

A aplicação estará disponível em: `http://localhost:8080`

---

## 📡 Endpoints da API

### Métricas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/metrics` | Lista todas as métricas |
| GET | `/metrics/{id}` | Busca métrica por ID |
| POST | `/metrics` | Cria nova métrica |

### Serviços de Métrica

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/services` | Lista todos os serviços |
| POST | `/services` | Cria novo serviço de métrica |

### Execuções

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/executions` | Executa um serviço de métrica |

### Tipos de Coletores

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/collectors-types` | Lista tipos de coletores |

---

## 📝 Exemplos de Uso

### Criar uma métrica

```bash
curl -X POST http://localhost:8080/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CPU Usage",
    "description": "Percentage of CPU utilization",
    "type": "gauge",
    "unit": "percent"
  }'
```

### Listar todas as métricas

```bash
curl http://localhost:8080/metrics
```

### Buscar métrica por ID

```bash
curl http://localhost:8080/metrics/{uuid}
```

### Executar um serviço de métrica

```bash
curl -X POST http://localhost:8080/executions \
  -H "Content-Type: application/json" \
  -d '{
    "idService": "11111111-2222-3333-4444-555555555555"
  }'
```

---

## 🔧 Configuração

### Níveis de Log

Configure os níveis de log em `application.properties`:

```properties
# Nível global
logging.level.root=INFO

# Níveis por pacote
logging.level.com.example.manager=DEBUG
logging.level.com.example.manager.services=DEBUG

# Formato do console
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n

# Gravar em arquivo
logging.file.name=logs/manager.log
```

### CORS

A aplicação está configurada para aceitar requisições de `http://localhost:*` (qualquer porta).

Para modificar, edite: `src/main/java/com/example/manager/config/CorsConfig.java`

---

## 🛠️ Desenvolvimento

### Compilar

```bash
./mvnw clean compile
```

### Executar testes

```bash
./mvnw test
```

### Limpar build

```bash
./mvnw clean
```

### Gerar JAR

```bash
./mvnw clean package
```

---

## 🐳 Docker - Detalhes Técnicos

### Estrutura

- **Dockerfile**: Multi-stage build para otimizar o tamanho da imagem
- **docker-compose.yml**: Orquestra app + PostgreSQL
- **Healthcheck**: PostgreSQL aguarda estar pronto antes da app iniciar
- **Volume persistente**: Dados do banco são mantidos entre restarts
- **Network Mode Host**: A aplicação usa `network_mode: host` para acessar serviços rodando em localhost (diferentes portas) na máquina host

### Importante - Network Mode

A aplicação Java está configurada com `network_mode: host`, o que significa:

- ✅ **Pode acessar** serviços rodando em `localhost:*` na máquina host (ex: `http://localhost:9000`, `http://localhost:4200`)
- ✅ **Acessa o PostgreSQL** via `localhost:5432` (precisa estar rodando no host ou via docker-compose)
- ⚠️ **Disponível em** `http://localhost:8080` (mesma porta do host)
- ⚠️ **Conflito de porta**: Se já houver algo rodando na porta 8080, o container falhará

**Alternativa para Windows/Mac**: Se `network_mode: host` não funcionar (só funciona nativamente no Linux), use:

```yaml
# Para acessar serviços do host no Windows/Mac
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/microservices-metrics-db
```

E para chamadas HTTP aos serviços de coleta, use `host.docker.internal` no lugar de `localhost`.

### Variáveis de Ambiente (Docker)

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| SPRING_DATASOURCE_URL | jdbc:postgresql://postgres:5432/... | URL do banco |
| SPRING_DATASOURCE_USERNAME | postgres | Usuário do banco |
| SPRING_DATASOURCE_PASSWORD | 123 | Senha do banco |
| SPRING_JPA_HIBERNATE_DDL_AUTO | update | Estratégia de schema |

---

## 📚 Tecnologias

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL**
- **ModelMapper**
- **Maven**
- **Docker & Docker Compose**

---

## 👥 Autores

Microservices-Metrics Team

## 📄 Licença

Este projeto está sob licença MIT.

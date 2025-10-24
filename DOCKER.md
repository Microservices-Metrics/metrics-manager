# Docker - Metric Manager

## Como usar

### Opção 1: Docker Compose (Recomendado)

Inicia a aplicação e o PostgreSQL juntos:

```bash
docker-compose up --build
```

A aplicação estará disponível em `http://localhost:8080`

Para parar:

```bash
docker-compose down
```

Para parar e remover volumes (limpa o banco):

```bash
docker-compose down -v
```

### Opção 2: Docker Build Manual

Build da imagem:

```bash
docker build -t metric-manager:latest .
```

Executar (certifique-se que o PostgreSQL está rodando):

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/microservices-metrics-db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=123 \
  metric-manager:latest
```

## Endpoints disponíveis

- GET `/metrics` - Lista todas as métricas
- GET `/metrics/{id}` - Busca métrica por ID
- POST `/metrics` - Cria nova métrica
- POST `/executions` - Executa um serviço de métrica

## Variáveis de ambiente

- `SPRING_DATASOURCE_URL` - URL do PostgreSQL
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco
- `SPRING_JPA_HIBERNATE_DDL_AUTO` - Modo de criação de schema (update, create, etc)

## Logs

Para ver os logs da aplicação:

```bash
docker-compose logs -f app
```

Para ver logs do PostgreSQL:

```bash
docker-compose logs -f postgres
```

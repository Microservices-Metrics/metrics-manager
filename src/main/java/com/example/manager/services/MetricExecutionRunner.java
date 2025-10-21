package com.example.manager.services;

import com.example.manager.models.MetricService;
import com.example.manager.models.MetricServiceArguments;
import com.example.manager.models.MetricServiceExecutions;
import com.example.manager.repositories.IMetricServiceExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Varre a fila de execuções agendadas e dispara requisições HTTP para serviços
 * de coleta.
 * Estratégia simples: a cada minuto pega até 50 execuções vencidas não
 * processadas.
 */
@Component
public class MetricExecutionRunner {
    private static final Logger log = LoggerFactory.getLogger(MetricExecutionRunner.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private IMetricServiceExecutionRepository executionRepository;
    @Autowired
    private RestTemplate restTemplate;

    // Intervalo: a cada 5 minutos (ajustável). Usa cron para facilidade de leitura.
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void processDueExecutions() {
        LocalDateTime now = LocalDateTime.now();
        List<MetricServiceExecutions> due = executionRepository
                .findTop50ByResponseStatusIsNullAndStartDateTimeBeforeOrderByStartDateTimeAsc(now);
        if (due.isEmpty())
            return;

        log.info("Processando {} execuções agendadas vencidas", due.size());

        for (MetricServiceExecutions execution : due) {
            try {
                performExecution(execution);
            } catch (Exception ex) {
                log.error("Falha ao executar serviço {} execId={}", execution.getMetricService().getIdService(),
                        execution.getIdExecution(), ex);
                execution.setEndDateTime(LocalDateTime.now());
                execution.setResponseStatus(599); // Código sintético para erro interno de coleta
                execution.setResponseBody(
                        truncate("ERROR: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(), 8000));
            }
        }
    }

    @Transactional
    public MetricServiceExecutions runExecution(MetricServiceExecutions execution) {
        // Certifique-se de que o objeto esteja gerenciado/atualizado pelo repositório
        try {
            performExecution(execution);
        } catch (Exception ex) {
            log.error("runExecution: erro ao executar execId={}", execution.getIdExecution(), ex);
            execution.setEndDateTime(LocalDateTime.now());
            execution.setResponseStatus(599);
            execution.setResponseBody(truncate("ERROR: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(), 8000));
        }

        // Persiste a coleta após execução
        executionRepository.save(execution);
        return execution;
    }

    private void performExecution(MetricServiceExecutions execution) {
        MetricService service = execution.getMetricService();
        String url = service.getUrl();

        // Monta corpo simples JSON com arguments (nome -> argumentValue ou placeholder por type)
        Map<String, Object> bodyMap = Optional.ofNullable(service.getArguments())
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(MetricServiceArguments::getArgumentName,
                        a -> {
                            String v = a.getArgumentValue();
                            return v != null ? parseValueForType(v, a.getType()) : defaultValueForType(a.getType());
                        }, (a, b) -> b));

        String requestBody;

        if (bodyMap.isEmpty()) {
            requestBody = null;
        } else {
            try {
                requestBody = objectMapper.writeValueAsString(bodyMap);
            } catch (JsonProcessingException e) {
                log.warn("Falha ao serializar bodyMap para JSON, usando fallback toString()", e);
                requestBody = bodyMap.toString();
            }
        }
        execution.setRequestBody(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response;
        
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class); // POST por padrão; poderia ajustar pelo 'type'
        } catch (RestClientException ex) {
            execution.setEndDateTime(LocalDateTime.now());
            execution.setResponseStatus(599);
            execution.setResponseBody(truncate("REST_ERROR: " + ex.getMessage(), 8000));
            return;
        }
        
        execution.setEndDateTime(LocalDateTime.now());
        execution.setResponseStatus(response.getStatusCode().value());
        execution.setResponseBody(truncate(response.getBody(), 16000));
    }

    private Object defaultValueForType(String type) {
        if (type == null)
            return null;
        return switch (type.toLowerCase()) {
            case "inteiro", "integer", "int" -> 0;
            case "decimal", "double", "float" -> 0.0;
            case "datahora", "datetime" -> LocalDateTime.now().toString();
            case "texto", "string" -> "";
            default -> null;
        };
    }

    private Object parseValueForType(String value, String type) {
        if (value == null)
            return defaultValueForType(type);
        if (type == null)
            return value;
        try {
            return switch (type.toLowerCase()) {
                case "inteiro", "integer", "int" -> Integer.parseInt(value);
                case "decimal", "double", "float" -> Double.parseDouble(value);
                case "boolean", "bool" -> Boolean.parseBoolean(value);
                case "datahora", "datetime" -> {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(value);
                        yield dt.toString();
                    } catch (Exception e) {
                        // keep original string if parsing fails
                        yield value;
                    }
                }
                case "texto", "string" -> value;
                default -> value;
            };
        } catch (Exception ex) {
            log.debug("parseValueForType: failed to parse '{}' as {} -> returning original/string default", value,
                    type);
            return value;
        }
    }

    private String truncate(String s, int max) {
        if (s == null)
            return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

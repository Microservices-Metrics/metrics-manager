package com.example.manager.services;

import com.example.manager.models.CollectorConfig;
import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.Measurement;
import com.example.manager.repositories.ICollectorConfigRepository;
import com.example.manager.repositories.IMeasurementRepository;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Serviço responsável por agendar e executar coletas de métricas
 * com base nas configurações de CollectorConfig.
 */
@Service
public class CollectorSchedulingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectorSchedulingService.class);
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    @Autowired
    private CollectorRequestService collectorRequestService;
    
    @Autowired
    private ICollectorConfigRepository collectorConfigRepository;
    
    @Autowired
    private IMeasurementRepository measurementRepository;
    
    // Mapa para armazenar os agendamentos ativos
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    /**
     * Agenda uma nova tarefa de coleta baseada na configuração do CollectorConfig.
     * 
     * @param collectorConfig Configuração do coletor
     */
    public void scheduleCollection(CollectorConfig collectorConfig) {
        UUID configId = collectorConfig.getId();
        
        // Cancela agendamento anterior se existir
        cancelScheduledTask(configId);
        
        try {
            CronTrigger cronTrigger = new CronTrigger(collectorConfig.getCronExpression());
            
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                () -> executeCollectionTask(configId),
                cronTrigger
            );
            
            scheduledTasks.put(configId, scheduledTask);
            logger.info("Scheduled collection task for CollectorConfig ID: {} with cron: {}", 
                configId, collectorConfig.getCronExpression());
                
        } catch (Exception e) {
            logger.error("FAILED to schedule collection for CollectorConfig ID: {}", configId, e);
            
            throw new IllegalArgumentException("Invalid cron expression: " + collectorConfig.getCronExpression(), e);
        }
    }
    
    /**
     * Cancela um agendamento existente.
     * 
     * @param configId ID da configuração do coletor
     */
    public void cancelScheduledTask(UUID configId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(configId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            logger.info("Cancelled scheduled task for CollectorConfig ID: {}", configId);
        }
    }
    
    /**
     * Executa a tarefa de coleta para uma configuração específica.
     * 
     * @param configId ID da configuração do coletor
     */
    private void executeCollectionTask(UUID configId) {
        try {
            CollectorConfig config = collectorConfigRepository.findById(configId).orElse(null);
            
            if (config == null) {
                logger.warn("CollectorConfig ID: {} not found. Cancelling task.", configId);
                cancelScheduledTask(configId);
                return;
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            // Verifica se está dentro do período de início e fim
            if (config.getStartDateTime() != null && now.isBefore(config.getStartDateTime())) {
                logger.debug("Collection for CollectorConfig ID: {} skipped - before start time", configId);
                return;
            }
            
            if (config.getEndDateTime() != null && now.isAfter(config.getEndDateTime())) {
                logger.info("Collection for CollectorConfig ID: {} ended - after end time. Cancelling task.", configId);
                cancelScheduledTask(configId);
                return;
            }
            
            logger.info("Executing collection for CollectorConfig ID: {}", configId);
            
            // Cria registro de medição
            Measurement measurement = new Measurement();
            measurement.setCollectorConfig(config);
            measurement.setStartTimestamp(now);
            
            try {
                // Executa a coleta
                ResponseEntity<String> response = collectorRequestService.executeCollection(config);
                
                measurement.setResponseStatus(response.getStatusCode().toString());
                measurement.setResponseBody(response.getBody());
                
                // Extrai o valor da métrica do response baseado no pathToMetric
                String metricValue = extractMetricValue(response.getBody(), config);
                measurement.setMetricValue(metricValue);
                
                logger.info("Collection successful for CollectorConfig ID: {}", configId);
                
            } catch (Exception e) {
                measurement.setResponseStatus("ERROR");
                measurement.setResponseBody(e.getMessage());
                logger.error("Collection failed for CollectorConfig ID: {}", configId, e);
            }
            
            // Salva o resultado da medição
            measurementRepository.save(measurement);
            
        } catch (Exception e) {
            logger.error("Unexpected error during collection task for CollectorConfig ID: {}", configId, e);
        }
    }
    
    /**
     * Verifica se existe um agendamento ativo para uma configuração.
     * 
     * @param configId ID da configuração do coletor
     * @return true se existe um agendamento ativo
     */
    public boolean isScheduled(UUID configId) {
        ScheduledFuture<?> task = scheduledTasks.get(configId);
        return task != null && !task.isCancelled() && !task.isDone();
    }
    
    /**
     * Cancela todos os agendamentos ativos.
     */
    public void cancelAllScheduledTasks() {
        logger.info("Cancelling all scheduled tasks...");
        scheduledTasks.forEach((id, task) -> {
            task.cancel(false);
            logger.info("Cancelled task for CollectorConfig ID: {}", id);
        });
        scheduledTasks.clear();
    }
    
    /**
     * Extrai o valor da métrica do corpo da resposta JSON baseado no pathToMetric.
     * 
     * @param responseBody Corpo da resposta JSON
     * @param config Configuração do coletor
     * @return O valor da métrica como String, ou null se não encontrado
     */
    private String extractMetricValue(String responseBody, CollectorConfig config) {
        try {
            // Busca o metadado com keyName = "pathToMetric"
            String pathToMetric = config.getCollector().getMetadata().stream()
                .filter(metadata -> "pathToMetric".equals(metadata.getKeyName()))
                .map(CollectorMetadata::getKeyValue)
                .findFirst()
                .orElse(null);
            
            if (pathToMetric == null || pathToMetric.isEmpty()) {
                logger.warn("PathToMetric not found for CollectorConfig ID: {}", config.getId());
                return null;
            }
            
            // Usa JSONPath para extrair o valor
            Object value = JsonPath.read(responseBody, pathToMetric);
            
            if (value == null) {
                logger.warn("Metric value not found at path '{}' for CollectorConfig ID: {}", 
                    pathToMetric, config.getId());
                return null;
            }
            
            logger.info("Extracted metric value '{}' from path '{}' for CollectorConfig ID: {}", 
                value, pathToMetric, config.getId());
                
            return value.toString();
            
        } catch (Exception e) {
            logger.error("Failed to extract metric value for CollectorConfig ID: {}", 
                config.getId(), e);
            return null;
        }
    }
}

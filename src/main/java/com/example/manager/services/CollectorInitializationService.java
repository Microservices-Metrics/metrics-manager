package com.example.manager.services;

import com.example.manager.models.CollectorConfig;
import com.example.manager.repositories.ICollectorConfigRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável por inicializar os agendamentos
 * de coletas ao iniciar a aplicação.
 */
@Service
public class CollectorInitializationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectorInitializationService.class);
    
    @Autowired
    private ICollectorConfigRepository collectorConfigRepository;
    
    @Autowired
    private CollectorSchedulingService schedulingService;
    
    /**
     * Inicializa todos os agendamentos quando a aplicação estiver pronta.
     * Este método é executado automaticamente após a aplicação iniciar.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeScheduledCollections() {
        logger.info("Initializing scheduled collections...");
        
        try {
            List<CollectorConfig> configs = collectorConfigRepository.findAll();
            
            int scheduledCount = 0;
            for (CollectorConfig config : configs) {
                try {
                    schedulingService.scheduleCollection(config);
                    scheduledCount++;
                } catch (Exception e) {
                    logger.error("Failed to schedule collection for CollectorConfig ID: {}", 
                        config.getId(), e);
                }
            }
            
            logger.info("Successfully scheduled {} collection tasks", scheduledCount);
            
        } catch (Exception e) {
            logger.error("Error during scheduled collections initialization", e);
        }
    }
}

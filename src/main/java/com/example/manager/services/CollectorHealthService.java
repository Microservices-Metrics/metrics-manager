package com.example.manager.services;

import com.example.manager.models.Collector;
import com.example.manager.models.CollectorHealthStatus;
import com.example.manager.models.CollectorHealthStatus.Status;
import com.example.manager.repositories.ICollectorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável por verificar periodicamente a saúde dos coletores registrados.
 * Faz GET {healthUrl}/health para cada coletor e armazena o status em memória.
 *
 * A URL de health check é determinada na seguinte ordem:
 *   1. Metadado com keyName = "healthCheckUrl"
 *   2. Base URL extraída do metadado keyName = "url" + "/health"
 */
@Service
public class CollectorHealthService {

    private static final Logger logger = LoggerFactory.getLogger(CollectorHealthService.class);
    private static final String HEALTH_METADATA_KEY = "healthCheckUrl";
    private static final String URL_METADATA_KEY = "url";

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final Map<UUID, CollectorHealthStatus> healthStatuses = new ConcurrentHashMap<>();

    /**
     * Executa o health check de todos os coletores registrados.
     * Intervalo configurável via a propriedade {@code collector.health.check.interval}
     * (padrão: 60 segundos).
     */
    @Scheduled(fixedDelayString = "${collector.health.check.interval:60000}")
    public void checkAllCollectors() {
        List<Collector> collectors = collectorRepository.findAll();
        logger.debug("Running health check for {} collector(s)", collectors.size());
        for (Collector collector : collectors) {
            checkCollector(collector);
        }
    }

    /**
     * Executa o health check para um único coletor e atualiza o status em memória.
     */
    public CollectorHealthStatus checkCollector(Collector collector) {
        CollectorHealthStatus status = healthStatuses.computeIfAbsent(
                collector.getId(),
                id -> new CollectorHealthStatus(id, collector.getName()));

        Optional<String> healthUrl = resolveHealthUrl(collector);

        if (healthUrl.isEmpty()) {
            status.setStatus(Status.UNKNOWN);
            status.setDetail("No URL metadata found for this collector");
            status.setLastChecked(LocalDateTime.now());
            logger.warn("Collector '{}' has no URL configured, skipping health check", collector.getName());
            return status;
        }

        String url = healthUrl.get();
        status.setCheckedUrl(url);
        status.setLastChecked(LocalDateTime.now());

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                status.setStatus(Status.UP);
                status.setDetail(response.getStatusCode().toString());
            } else {
                status.setStatus(Status.DOWN);
                status.setDetail("HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            status.setStatus(Status.DOWN);
            status.setDetail(e.getMessage());
            logger.warn("Health check failed for collector '{}' at {}: {}", collector.getName(), url, e.getMessage());
        }

        logger.info("Collector '{}' health: {} ({})", collector.getName(), status.getStatus(), url);
        return status;
    }

    /**
     * Retorna todos os status de health check armazenados em memória.
     */
    public Collection<CollectorHealthStatus> getAllStatuses() {
        return healthStatuses.values();
    }

    /**
     * Retorna o status de health check de um coletor específico.
     */
    public Optional<CollectorHealthStatus> getStatus(UUID collectorId) {
        return Optional.ofNullable(healthStatuses.get(collectorId));
    }

    // -------------------------------------------------------------------------

    private Optional<String> resolveHealthUrl(Collector collector) {
        // 1. Metadado explícito healthCheckUrl
        Optional<String> explicit = collector.getMetadata().stream()
                .filter(m -> HEALTH_METADATA_KEY.equals(m.getKeyName()))
                .map(m -> m.getKeyValue().trim())
                .filter(v -> !v.isEmpty())
                .findFirst();

        if (explicit.isPresent()) {
            return explicit;
        }

        // 2. Extrai base URL do metadado "url" e adiciona /health
        return collector.getMetadata().stream()
                .filter(m -> URL_METADATA_KEY.equals(m.getKeyName()))
                .map(m -> m.getKeyValue().trim())
                .filter(v -> !v.isEmpty())
                .findFirst()
                .map(this::extractBaseUrl)
                .map(base -> base + "/health");
    }

    /**
     * Extrai scheme://host:port de uma URL completa, ignorando path e query.
     * Ex: "http://localhost:8081/collector?foo=bar" → "http://localhost:8081"
     */
    private String extractBaseUrl(String rawUrl) {
        try {
            URI uri = new URI(rawUrl);
            int port = uri.getPort();
            if (port == -1) {
                return uri.getScheme() + "://" + uri.getHost();
            }
            return uri.getScheme() + "://" + uri.getHost() + ":" + port;
        } catch (Exception e) {
            // Fallback: tenta remover path manualmente
            int pathStart = rawUrl.indexOf('/', rawUrl.indexOf("//") + 2);
            return pathStart == -1 ? rawUrl : rawUrl.substring(0, pathStart);
        }
    }
}

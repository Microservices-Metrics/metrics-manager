package com.example.manager.services;

import com.example.manager.dtos.MetricServiceDto;
import com.example.manager.models.MetricService;
import com.example.manager.models.MetricServiceArguments;
import com.example.manager.models.MetricServiceExecutions;
import com.example.manager.models.MetricServiceSettings;
import com.example.manager.repositories.IMetricServiceExecutionRepository;
import com.example.manager.repositories.IMetricServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsável por criar um MetricService e gerar sua agenda de execuções futuras.
 */
@Service
public class MetricSchedulingService {

    private static final int MAX_SCHEDULE_ENTRIES = 1000; // Limita para evitar explosões acidentais
    private static final Logger log = LoggerFactory.getLogger(MetricSchedulingService.class);

    @Autowired
    private IMetricServiceRepository metricServiceRepository;

    @Autowired
    private IMetricServiceExecutionRepository metricServiceExecutionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public MetricService createMetricServiceAndSchedule(MetricServiceDto dto) {
        log.debug("Iniciando createMetricServiceAndSchedule para service.name={}", dto.getName());
        MetricService metricService = modelMapper.map(dto, MetricService.class);

        // Ajusta relação bidirecional
        if (metricService.getArguments() != null) {
            for (MetricServiceArguments arg : metricService.getArguments()) {
                arg.setMetricService(metricService);
            }
        }

        metricService = metricServiceRepository.save(metricService);

        // Gerar agenda de execuções
        try {
            generateExecutions(metricService);
        } catch (Exception ex) {
            // Não queremos quebrar a criação por causa de problema de schedule — logamos para investigação
            log.error("Erro ao gerar agenda para metricService.id={} name={}", metricService.getIdService(), metricService.getName(), ex);
        }

        return metricService;
    }

    private void generateExecutions(MetricService metricService) {
        MetricServiceSettings settings = metricService.getCollectionSettings();
        if (settings == null || settings.getCronExpression() == null || settings.getStartDate() == null || settings.getEndDate() == null) {
            log.debug("generateExecutions: configurações incompletas para metricService.id={} -> não há agenda a gerar", metricService.getIdService());
            return; // Nada a agendar
        }

        CronExpression cron;
        try {
            String cronNormalized = normalizeCronExpression(settings.getCronExpression());

            // Validação: rejeita expressões onde o campo de segundos é '*' ou contém step (ex: '*/5')
            String[] parts = cronNormalized.split(" ");
            if (parts.length < 6) {
                throw new IllegalArgumentException("Cron expression must consist of 6 fields");
            }
            String secondsField = parts[0];
            if ("*".equals(secondsField) || secondsField.contains("/")) {
                throw new IllegalArgumentException("Cron expressions with seconds='*' or stepped seconds are not allowed to avoid per-second scheduling");
            }

            cron = CronExpression.parse(cronNormalized);
        } catch (IllegalArgumentException ex) {
            // Cron inválido ou não permitido -> não gera agenda
            log.warn("Expressão cron inválida para metricService.id={} cron='{}' -> pulando geração: {}", metricService.getIdService(), settings.getCronExpression(), ex.getMessage());
            return;
        }

        LocalDate startDate = settings.getStartDate();
        LocalDate endDate = settings.getEndDate();
        if (endDate.isBefore(startDate)) return;

        LocalDateTime windowStart = startDate.atStartOfDay();
        LocalDateTime windowEnd = endDate.atTime(LocalTime.MAX);

        LocalDateTime next = cron.next(windowStart.minusSeconds(1));

        List<MetricServiceExecutions> executions = new ArrayList<>();
        int count = 0;
        while (next != null && !next.isAfter(windowEnd) && count < MAX_SCHEDULE_ENTRIES) {
            MetricServiceExecutions exec = new MetricServiceExecutions();
            exec.setMetricService(metricService);
            exec.setStartDateTime(next);
            exec.setRequestUrl(metricService.getUrl());
            exec.setRequestBody(null); // Preenchido na execução real
            executions.add(exec);
            count++;
            next = cron.next(next);
        }

        if (!executions.isEmpty()) {
            metricServiceExecutionRepository.saveAll(executions);
            log.info("generateExecutions: {} execuções criadas para metricService.id={}", executions.size(), metricService.getIdService());
        } else {
            log.debug("generateExecutions: nenhuma execução gerada para metricService.id={}", metricService.getIdService());
        }
    }

    /**
     * Normaliza expressões cron para o formato esperado por Spring's CronExpression.
     * Aceita cron com 5 campos (min hour day month dow) e adiciona o campo de segundos (0) no início.
     * Retorna a expressão já normalizada ou lança IllegalArgumentException caso o número de campos seja inválido.
     */
    private String normalizeCronExpression(String expr) {
        if (expr == null) throw new IllegalArgumentException("cron expression is null");
        String cleaned = expr.trim().replaceAll("\\s+", " ");
        String[] parts = cleaned.split(" ");
        if (parts.length == 5) {
            // common unix-style cron (minute hour day month day-of-week) -> add seconds=0
            return "0 " + cleaned;
        }
        if (parts.length == 6 || parts.length == 7) {
            return cleaned;
        }
        throw new IllegalArgumentException("Cron expression must consist of 6 fields");
    }
}

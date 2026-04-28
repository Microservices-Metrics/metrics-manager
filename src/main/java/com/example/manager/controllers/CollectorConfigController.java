package com.example.manager.controllers;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.manager.dtos.CollectorConfigDto;
import com.example.manager.dtos.CollectorConfigRequestDto;
import com.example.manager.dtos.CollectorTriggerRequestDto;
import com.example.manager.models.Collector;
import com.example.manager.models.CollectorConfig;
import com.example.manager.models.Microservice;
import com.example.manager.repositories.ICollectorConfigRepository;
import com.example.manager.repositories.ICollectorRepository;
import com.example.manager.repositories.IMicroserviceRepository;
import com.example.manager.services.CollectorSchedulingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/collector-configs")
public class CollectorConfigController {

    @Autowired
    private ICollectorConfigRepository collectorConfigRepository;

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private IMicroserviceRepository microserviceRepository;

    @Autowired
    private CollectorSchedulingService schedulingService;

    @Autowired
    private ModelMapper modelMapper;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @GetMapping
    public ResponseEntity<List<CollectorConfigDto>> getAllCollectorConfigs() {
        List<CollectorConfig> configs = collectorConfigRepository.findAll();
        List<CollectorConfigDto> dtos = configs.stream()
                .map(c -> {
                    CollectorConfigDto dto = modelMapper.map(c, CollectorConfigDto.class);
                    dto.setCollectorId(c.getCollector() != null ? c.getCollector().getId() : null);
                    dto.setMicroserviceId(c.getMicroservice() != null ? c.getMicroservice().getId() : null);
                    if (c.getStartDateTime() != null) {
                        dto.setStartDateTime(c.getStartDateTime().format(ISO_FORMATTER));
                    }
                    if (c.getEndDateTime() != null) {
                        dto.setEndDateTime(c.getEndDateTime().format(ISO_FORMATTER));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectorConfigDto> getCollectorConfigById(@PathVariable UUID id) {
        return collectorConfigRepository.findById(id)
                .map(c -> {
                    CollectorConfigDto dto = modelMapper.map(c, CollectorConfigDto.class);
                    dto.setCollectorId(c.getCollector() != null ? c.getCollector().getId() : null);
                    dto.setMicroserviceId(c.getMicroservice() != null ? c.getMicroservice().getId() : null);
                    if (c.getStartDateTime() != null) {
                        dto.setStartDateTime(c.getStartDateTime().format(ISO_FORMATTER));
                    }
                    if (c.getEndDateTime() != null) {
                        dto.setEndDateTime(c.getEndDateTime().format(ISO_FORMATTER));
                    }
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCollectorConfig(@Valid @RequestBody CollectorConfigRequestDto req) {
        Collector collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
        if (req.getCollectorId() != null && collector == null) {
            return ResponseEntity.badRequest().build();
        }

        Microservice microservice = microserviceRepository.findById(req.getMicroserviceId()).orElse(null);
        if (req.getMicroserviceId() != null && microservice == null) {
            return ResponseEntity.badRequest().build();
        }

        CollectorConfig config = new CollectorConfig();
        config.setCronExpression(req.getCronExpression());
        if (req.getStartDateTime() != null && !req.getStartDateTime().isEmpty()) {
            config.setStartDateTime(LocalDateTime.parse(req.getStartDateTime(), ISO_FORMATTER));
        }
        if (req.getEndDateTime() != null && !req.getEndDateTime().isEmpty()) {
            config.setEndDateTime(LocalDateTime.parse(req.getEndDateTime(), ISO_FORMATTER));
        }
        if (collector != null) {
            config.setCollector(collector);
        }
        if (microservice != null) {
            config.setMicroservice(microservice);
        }

        CollectorConfig saved = collectorConfigRepository.save(config);
        
        // Agenda a coleta automática
        schedulingService.scheduleCollection(saved);
        
        CollectorConfigDto dto = modelMapper.map(saved, CollectorConfigDto.class);
        dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
        dto.setMicroserviceId(saved.getMicroservice() != null ? saved.getMicroservice().getId() : null);
        if (saved.getStartDateTime() != null) {
            dto.setStartDateTime(saved.getStartDateTime().format(ISO_FORMATTER));
        }
        if (saved.getEndDateTime() != null) {
            dto.setEndDateTime(saved.getEndDateTime().format(ISO_FORMATTER));
        }
        URI location = URI.create("/collector-configs/" + saved.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollectorConfig(@PathVariable UUID id, @Valid @RequestBody CollectorConfigRequestDto req) {
        return collectorConfigRepository.findById(id).map(existing -> {
            Collector collector = null;
            if (req.getCollectorId() != null) {
                collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
                if (collector == null) return ResponseEntity.badRequest().build();
            }

            Microservice microservice = null;
            if (req.getMicroserviceId() != null) {
                microservice = microserviceRepository.findById(req.getMicroserviceId()).orElse(null);
                if (microservice == null) return ResponseEntity.badRequest().build();
            }

            existing.setCronExpression(req.getCronExpression());
            if (req.getStartDateTime() != null && !req.getStartDateTime().isEmpty()) {
                existing.setStartDateTime(LocalDateTime.parse(req.getStartDateTime(), ISO_FORMATTER));
            }
            if (req.getEndDateTime() != null && !req.getEndDateTime().isEmpty()) {
                existing.setEndDateTime(LocalDateTime.parse(req.getEndDateTime(), ISO_FORMATTER));
            }
            if (collector != null) existing.setCollector(collector);
            if (microservice != null) existing.setMicroservice(microservice);

            CollectorConfig saved = collectorConfigRepository.save(existing);
            
            // Reagenda a coleta com as novas configurações
            schedulingService.scheduleCollection(saved);
            
            CollectorConfigDto dto = modelMapper.map(saved, CollectorConfigDto.class);
            dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
            dto.setMicroserviceId(saved.getMicroservice() != null ? saved.getMicroservice().getId() : null);
            if (saved.getStartDateTime() != null) {
                dto.setStartDateTime(saved.getStartDateTime().format(ISO_FORMATTER));
            }
            if (saved.getEndDateTime() != null) {
                dto.setEndDateTime(saved.getEndDateTime().format(ISO_FORMATTER));
            }
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/trigger")
    public ResponseEntity<Void> triggerCollection(@Valid @RequestBody CollectorTriggerRequestDto req) {
        if (!collectorRepository.existsById(req.getCollectorId())) {
            return ResponseEntity.notFound().build();
        }
        if (!microserviceRepository.existsById(req.getMicroserviceId())) {
            return ResponseEntity.notFound().build();
        }
        schedulingService.triggerCollection(req.getCollectorId(), req.getMicroserviceId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollectorConfig(@PathVariable UUID id) {
        return collectorConfigRepository.findById(id).map(c -> {
            // Cancela o agendamento antes de deletar
            schedulingService.cancelScheduledTask(id);
            collectorConfigRepository.delete(c);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllCollectorConfigs() {
        // Cancela todos os agendamentos
        schedulingService.cancelAllScheduledTasks();
        collectorConfigRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}

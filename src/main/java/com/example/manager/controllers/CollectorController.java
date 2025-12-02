package com.example.manager.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.manager.dtos.CollectorDto;
import com.example.manager.dtos.CollectorResponseDto;
import com.example.manager.dtos.CollectorResponseSchemaDto;
import com.example.manager.dtos.CollectorMetadataDto;
import com.example.manager.dtos.CollectorConfigDto;
import com.example.manager.models.Collector;
import com.example.manager.models.CollectorResponseSchema;
import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.CollectorConfig;
import com.example.manager.models.Metric;
import com.example.manager.repositories.ICollectorRepository;
import com.example.manager.repositories.IMetricRepository;
import com.example.manager.repositories.ICollectorResponseSchemaRepository;
import com.example.manager.repositories.ICollectorMetadataRepository;
import com.example.manager.repositories.ICollectorConfigRepository;
import com.example.manager.repositories.IMicroserviceRepository;
import com.example.manager.util.JsonSchemaService;

@RestController
@RequestMapping("/collectors")
public class CollectorController {

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private IMetricRepository metricRepository;

    @Autowired
    private ICollectorResponseSchemaRepository responseSchemaRepository;

    @Autowired
    private ICollectorMetadataRepository collectorMetadataRepository;

    @Autowired
    private ICollectorConfigRepository collectorConfigRepository;

    @Autowired
    private IMicroserviceRepository microserviceRepository;

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<CollectorDto>> listCollectors() {
        List<Collector> collectors = collectorRepository.findAll();
        List<CollectorDto> dtos = collectors.stream()
                .map(c -> modelMapper.map(c, CollectorDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectorDto> getCollector(@PathVariable UUID id) {
        Optional<Collector> opt = collectorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(modelMapper.map(opt.get(), CollectorDto.class));
    }

    @PostMapping
    public ResponseEntity<CollectorResponseDto> createCollector(@RequestBody CollectorDto dto) {
        Collector collector = new Collector();
        collector.setName(dto.getName());
        collector.setDescription(dto.getDescription());
        collector.setCollectionMethod(dto.getCollectionMethod());

        if (dto.getMetricId() != null) {
            Optional<Metric> metricOpt = metricRepository.findById(dto.getMetricId());
            if (metricOpt.isPresent()) {
                collector.setMetric(metricOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        Collector saved = collectorRepository.save(collector);

        if (dto.getResponseSchemas() != null) {
            for (CollectorResponseSchemaDto rsDto : dto.getResponseSchemas()) {
                CollectorResponseSchema rs;
                if (rsDto.getId() != null) {
                    Optional<CollectorResponseSchema> rsOpt = responseSchemaRepository.findById(rsDto.getId());
                    if (rsOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    rs = rsOpt.get();
                } else {
                    rs = new CollectorResponseSchema();
                }
                
                try {
                    jsonSchemaService.validateSchemaString(rsDto.getSchema());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                rs.setSchema(rsDto.getSchema());
                rs.setStatusType(rsDto.getStatusType());
                rs.setDescription(rsDto.getDescription());
                rs.setCollector(saved);

                saved.getResponseSchemas().add(rs);
            }
        }

        if (dto.getMetadata() != null) {
            for (CollectorMetadataDto mdDto : dto.getMetadata()) {
                CollectorMetadata md;
                if (mdDto.getId() != null) {
                    Optional<CollectorMetadata> mdOpt = collectorMetadataRepository.findById(mdDto.getId());
                    if (mdOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    md = mdOpt.get();
                } else {
                    md = new CollectorMetadata();
                }
                
                md.setKeyName(mdDto.getKeyName());
                md.setKeyValue(mdDto.getKeyValue());
                md.setCollector(saved);

                saved.getMetadata().add(md);
            }
        }

        if (dto.getConfigs() != null) {
            for (CollectorConfigDto ccDto : dto.getConfigs()) {
                if (ccDto.getMicroserviceId() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                Optional<com.example.manager.models.Microservice> msOpt = microserviceRepository.findById(ccDto.getMicroserviceId());
                if (msOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                CollectorConfig cc;
                if (ccDto.getId() != null) {
                    Optional<CollectorConfig> ccOpt = collectorConfigRepository.findById(ccDto.getId());
                    if (ccOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    cc = ccOpt.get();
                } else {
                    cc = new CollectorConfig();
                }
                
                cc.setCronExpression(ccDto.getCronExpression());
                if (ccDto.getStartDateTime() != null) {
                    cc.setStartDateTime(java.time.LocalDateTime.parse(ccDto.getStartDateTime()));
                } else {
                    cc.setStartDateTime(null);
                }
                if (ccDto.getEndDateTime() != null) {
                    cc.setEndDateTime(java.time.LocalDateTime.parse(ccDto.getEndDateTime()));
                } else {
                    cc.setEndDateTime(null);
                }
                cc.setMicroservice(msOpt.get());
                cc.setCollector(saved);

                saved.getConfigs().add(cc);
            }
        }

        // Salva o collector novamente com todos os relacionamentos
        saved = collectorRepository.save(saved);
        CollectorResponseDto response = modelMapper.map(saved, CollectorResponseDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CollectorDto> updateCollector(@PathVariable UUID id, @RequestBody CollectorDto dto) {
        Optional<Collector> opt = collectorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Collector existing = opt.get();
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setCollectionMethod(dto.getCollectionMethod());

        if (dto.getMetricId() != null) {
            Optional<Metric> metricOpt = metricRepository.findById(dto.getMetricId());
            if (metricOpt.isPresent()) {
                existing.setMetric(metricOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        Collector saved = collectorRepository.save(existing);

        // Limpar as listas existentes
        saved.getResponseSchemas().clear();
        saved.getMetadata().clear();
        saved.getConfigs().clear();

        if (dto.getResponseSchemas() != null) {
            for (CollectorResponseSchemaDto rsDto : dto.getResponseSchemas()) {
                CollectorResponseSchema rs;
                if (rsDto.getId() != null) {
                    Optional<CollectorResponseSchema> rsOpt = responseSchemaRepository.findById(rsDto.getId());
                    if (rsOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    rs = rsOpt.get();
                } else {
                    rs = new CollectorResponseSchema();
                }
                
                rs.setSchema(rsDto.getSchema());
                rs.setStatusType(rsDto.getStatusType());
                rs.setDescription(rsDto.getDescription());
                rs.setCollector(saved);
                saved.getResponseSchemas().add(rs);
            }
        }

        if (dto.getMetadata() != null) {
            for (CollectorMetadataDto mdDto : dto.getMetadata()) {
                CollectorMetadata md;
                if (mdDto.getId() != null) {
                    Optional<CollectorMetadata> mdOpt = collectorMetadataRepository.findById(mdDto.getId());
                    if (mdOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    md = mdOpt.get();
                } else {
                    md = new CollectorMetadata();
                }
                
                md.setKeyName(mdDto.getKeyName());
                md.setKeyValue(mdDto.getKeyValue());
                md.setCollector(saved);
                saved.getMetadata().add(md);
            }
        }

        if (dto.getConfigs() != null) {
            for (CollectorConfigDto ccDto : dto.getConfigs()) {
                if (ccDto.getMicroserviceId() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                Optional<com.example.manager.models.Microservice> msOpt = microserviceRepository.findById(ccDto.getMicroserviceId());
                if (msOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                CollectorConfig cc;
                if (ccDto.getId() != null) {
                    Optional<CollectorConfig> ccOpt = collectorConfigRepository.findById(ccDto.getId());
                    if (ccOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    cc = ccOpt.get();
                } else {
                    cc = new CollectorConfig();
                }
                
                cc.setCronExpression(ccDto.getCronExpression());
                if (ccDto.getStartDateTime() != null) {
                    cc.setStartDateTime(java.time.LocalDateTime.parse(ccDto.getStartDateTime()));
                } else {
                    cc.setStartDateTime(null);
                }
                if (ccDto.getEndDateTime() != null) {
                    cc.setEndDateTime(java.time.LocalDateTime.parse(ccDto.getEndDateTime()));
                } else {
                    cc.setEndDateTime(null);
                }
                cc.setMicroservice(msOpt.get());
                cc.setCollector(saved);
                saved.getConfigs().add(cc);
            }
        }

        // Salva o collector novamente com todos os relacionamentos
        saved = collectorRepository.save(saved);
        return ResponseEntity.ok(modelMapper.map(saved, CollectorDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollector(@PathVariable UUID id) {
        Optional<Collector> opt = collectorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        collectorRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllCollectors() {
        collectorRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}

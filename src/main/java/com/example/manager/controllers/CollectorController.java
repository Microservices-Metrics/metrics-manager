package com.example.manager.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GetMapping
    public ResponseEntity<List<CollectorDto>> listCollectors() {
        List<Collector> collectors = collectorRepository.findAll();
        List<CollectorDto> dtos = collectors.stream().map(this::toDto).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectorDto> getCollector(@PathVariable UUID id) {
        Optional<Collector> opt = collectorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDto(opt.get()));
    }

    @PostMapping
    public ResponseEntity<CollectorDto> createCollector(@RequestBody CollectorDto dto) {
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
                if (rsDto.getId() != null) {
                    Optional<CollectorResponseSchema> rsOpt = responseSchemaRepository.findById(rsDto.getId());
                    if (rsOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    CollectorResponseSchema rs = rsOpt.get();
                    rs.setSchema(rsDto.getSchema());
                    rs.setStatusType(rsDto.getStatusType());
                    rs.setDescription(rsDto.getDescription());
                    rs.setCollector(saved);
                    responseSchemaRepository.save(rs);
                } else {
                    CollectorResponseSchema rs = new CollectorResponseSchema();
                    rs.setSchema(rsDto.getSchema());
                    rs.setStatusType(rsDto.getStatusType());
                    rs.setDescription(rsDto.getDescription());
                    rs.setCollector(saved);
                    responseSchemaRepository.save(rs);
                }
            }
        }

        if (dto.getMetadata() != null) {
            for (CollectorMetadataDto mdDto : dto.getMetadata()) {
                if (mdDto.getId() != null) {
                    Optional<CollectorMetadata> mdOpt = collectorMetadataRepository.findById(mdDto.getId());
                    if (mdOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    CollectorMetadata md = mdOpt.get();
                    md.setUrl(mdDto.getUrl());
                    md.setRequestSchema(mdDto.getRequestSchema());
                    md.setPathToMetric(mdDto.getPathToMetric());
                    md.setCollector(saved);
                    collectorMetadataRepository.save(md);
                } else {
                    CollectorMetadata md = new CollectorMetadata();
                    md.setUrl(mdDto.getUrl());
                    md.setRequestSchema(mdDto.getRequestSchema());
                    md.setPathToMetric(mdDto.getPathToMetric());
                    md.setCollector(saved);
                    collectorMetadataRepository.save(md);
                }
            }
        }

        if (dto.getConfigs() != null) {
            for (CollectorConfigDto ccDto : dto.getConfigs()) {
                // microserviceId is required for config
                if (ccDto.getMicroserviceId() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                Optional<com.example.manager.models.Microservice> msOpt = microserviceRepository.findById(ccDto.getMicroserviceId());
                if (msOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                if (ccDto.getId() != null) {
                    Optional<CollectorConfig> ccOpt = collectorConfigRepository.findById(ccDto.getId());
                    if (ccOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    CollectorConfig cc = ccOpt.get();
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
                    collectorConfigRepository.save(cc);
                } else {
                    CollectorConfig cc = new CollectorConfig();
                    cc.setCronExpression(ccDto.getCronExpression());
                    if (ccDto.getStartDateTime() != null) {
                        cc.setStartDateTime(java.time.LocalDateTime.parse(ccDto.getStartDateTime()));
                    }
                    if (ccDto.getEndDateTime() != null) {
                        cc.setEndDateTime(java.time.LocalDateTime.parse(ccDto.getEndDateTime()));
                    }
                    cc.setMicroservice(msOpt.get());
                    cc.setCollector(saved);
                    collectorConfigRepository.save(cc);
                }
            }
        }

        CollectorDto response = toDto(saved);

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

        if (dto.getResponseSchemas() != null) {
            for (CollectorResponseSchemaDto rsDto : dto.getResponseSchemas()) {
                if (rsDto.getId() != null) {
                    Optional<CollectorResponseSchema> rsOpt = responseSchemaRepository.findById(rsDto.getId());
                    if (rsOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    CollectorResponseSchema rs = rsOpt.get();
                    rs.setSchema(rsDto.getSchema());
                    rs.setStatusType(rsDto.getStatusType());
                    rs.setDescription(rsDto.getDescription());
                    rs.setCollector(saved);
                    responseSchemaRepository.save(rs);
                } else {
                    CollectorResponseSchema rs = new CollectorResponseSchema();
                    rs.setSchema(rsDto.getSchema());
                    rs.setStatusType(rsDto.getStatusType());
                    rs.setDescription(rsDto.getDescription());
                    rs.setCollector(saved);
                    responseSchemaRepository.save(rs);
                }
            }
        }

        if (dto.getMetadata() != null) {
            for (CollectorMetadataDto mdDto : dto.getMetadata()) {
                if (mdDto.getId() != null) {
                    Optional<CollectorMetadata> mdOpt = collectorMetadataRepository.findById(mdDto.getId());
                    if (mdOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    CollectorMetadata md = mdOpt.get();
                    md.setUrl(mdDto.getUrl());
                    md.setRequestSchema(mdDto.getRequestSchema());
                    md.setPathToMetric(mdDto.getPathToMetric());
                    md.setCollector(saved);
                    collectorMetadataRepository.save(md);
                } else {
                    CollectorMetadata md = new CollectorMetadata();
                    md.setUrl(mdDto.getUrl());
                    md.setRequestSchema(mdDto.getRequestSchema());
                    md.setPathToMetric(mdDto.getPathToMetric());
                    md.setCollector(saved);
                    collectorMetadataRepository.save(md);
                }
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

                if (ccDto.getId() != null) {
                    Optional<CollectorConfig> ccOpt = collectorConfigRepository.findById(ccDto.getId());
                    if (ccOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    CollectorConfig cc = ccOpt.get();
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
                    collectorConfigRepository.save(cc);
                } else {
                    CollectorConfig cc = new CollectorConfig();
                    cc.setCronExpression(ccDto.getCronExpression());
                    if (ccDto.getStartDateTime() != null) {
                        cc.setStartDateTime(java.time.LocalDateTime.parse(ccDto.getStartDateTime()));
                    }
                    if (ccDto.getEndDateTime() != null) {
                        cc.setEndDateTime(java.time.LocalDateTime.parse(ccDto.getEndDateTime()));
                    }
                    cc.setMicroservice(msOpt.get());
                    cc.setCollector(saved);
                    collectorConfigRepository.save(cc);
                }
            }
        }

        return ResponseEntity.ok(toDto(saved));
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

    private CollectorDto toDto(Collector collector) {
        CollectorDto dto = new CollectorDto();
        dto.setId(collector.getId());
        dto.setName(collector.getName());
        dto.setDescription(collector.getDescription());
        dto.setCollectionMethod(collector.getCollectionMethod());
        dto.setMetricId(collector.getMetricId());
        
        if (collector.getResponseSchemas() != null) {
            dto.setResponseSchemas(collector.getResponseSchemas().stream().map(rs -> {
                CollectorResponseSchemaDto rsDto = new CollectorResponseSchemaDto();
                rsDto.setId(rs.getId());
                rsDto.setSchema(rs.getSchema());
                rsDto.setStatusType(rs.getStatusType());
                rsDto.setDescription(rs.getDescription());
                return rsDto;
            }).collect(Collectors.toList()));
        }

        if (collector.getMetadata() != null) {
            dto.setMetadata(collector.getMetadata().stream().map(md -> {
                CollectorMetadataDto mdDto = new CollectorMetadataDto();
                mdDto.setId(md.getId());
                mdDto.setUrl(md.getUrl());
                mdDto.setRequestSchema(md.getRequestSchema());
                mdDto.setPathToMetric(md.getPathToMetric());
                return mdDto;
            }).collect(Collectors.toList()));
        }

        if (collector.getConfigs() != null) {
            dto.setConfigs(collector.getConfigs().stream().map(cc -> {
                CollectorConfigDto ccDto = new CollectorConfigDto();
                ccDto.setId(cc.getId());
                ccDto.setMicroserviceId(cc.getMicroservice() != null ? cc.getMicroservice().getId() : null);
                ccDto.setCronExpression(cc.getCronExpression());
                ccDto.setStartDateTime(cc.getStartDateTime() != null ? cc.getStartDateTime().toString() : null);
                ccDto.setEndDateTime(cc.getEndDateTime() != null ? cc.getEndDateTime().toString() : null);
                return ccDto;
            }).collect(Collectors.toList()));
        }
        
        return dto;
    }
}

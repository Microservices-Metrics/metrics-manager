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

import com.example.manager.dtos.MicroserviceDto;
import com.example.manager.dtos.MicroserviceMetadataDto;
import com.example.manager.models.Microservice;
import com.example.manager.models.MicroserviceMetadata;
import com.example.manager.models.CollectorConfig;
import com.example.manager.repositories.IMicroserviceRepository;
import com.example.manager.repositories.IMicroserviceMetadataRepository;
import com.example.manager.repositories.ICollectorConfigRepository;

@RestController
@RequestMapping("/microservices")
public class MicroserviceController {

    @Autowired
    private IMicroserviceRepository microserviceRepository;
    
    @Autowired
    private IMicroserviceMetadataRepository microserviceMetadataRepository;

    @Autowired
    private ICollectorConfigRepository collectorConfigRepository;

    @GetMapping
    public ResponseEntity<List<MicroserviceDto>> getAllMicroservices() {
        List<Microservice> micros = microserviceRepository.findAll();
        List<MicroserviceDto> dtos = micros.stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MicroserviceDto> getMicroserviceById(@PathVariable UUID id) {
        Optional<Microservice> opt = microserviceRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDto(opt.get()));
    }

    @PostMapping
    public ResponseEntity<MicroserviceDto> createMicroservice(@RequestBody MicroserviceDto dto) {
        Microservice micro = new Microservice();
        micro.setName(dto.getName());
        micro = microserviceRepository.save(micro);

        if (dto.getMetadatas() != null) {
            for (MicroserviceMetadataDto mdDto : dto.getMetadatas()) {
                if (mdDto.getId() != null) {
                    Optional<MicroserviceMetadata> mdOpt = microserviceMetadataRepository.findById(mdDto.getId());
                    if (mdOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    MicroserviceMetadata md = mdOpt.get();
                    md.setVarName(mdDto.getVarName());
                    md.setVarValue(mdDto.getVarValue());
                    md.setMicroservice(micro);
                    microserviceMetadataRepository.save(md);
                } else {
                    MicroserviceMetadata md = new MicroserviceMetadata();
                    md.setVarName(mdDto.getVarName());
                    md.setVarValue(mdDto.getVarValue());
                    md.setMicroservice(micro);
                    microserviceMetadataRepository.save(md);
                }
            }
        }

        if (dto.getCollectorConfigIds() != null) {
            for (java.util.UUID ccId : dto.getCollectorConfigIds()) {
                Optional<CollectorConfig> ccOpt = collectorConfigRepository.findById(ccId);
                if (ccOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                CollectorConfig cc = ccOpt.get();
                cc.setMicroservice(micro);
                collectorConfigRepository.save(cc);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(micro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MicroserviceDto> updateMicroservice(@PathVariable UUID id, @RequestBody MicroserviceDto dto) {
        Optional<Microservice> opt = microserviceRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Microservice existing = opt.get();
        existing.setName(dto.getName());

        Microservice saved = microserviceRepository.save(existing);

        if (dto.getMetadatas() != null) {
            for (MicroserviceMetadataDto mdDto : dto.getMetadatas()) {
                if (mdDto.getId() != null) {
                    Optional<MicroserviceMetadata> mdOpt = microserviceMetadataRepository.findById(mdDto.getId());
                    if (mdOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }

                    MicroserviceMetadata md = mdOpt.get();
                    md.setVarName(mdDto.getVarName());
                    md.setVarValue(mdDto.getVarValue());
                    md.setMicroservice(saved);
                    microserviceMetadataRepository.save(md);
                } else {
                    MicroserviceMetadata md = new MicroserviceMetadata();
                    md.setVarName(mdDto.getVarName());
                    md.setVarValue(mdDto.getVarValue());
                    md.setMicroservice(saved);
                    microserviceMetadataRepository.save(md);
                }
            }
        }

        if (dto.getCollectorConfigIds() != null) {
            for (java.util.UUID ccId : dto.getCollectorConfigIds()) {
                Optional<CollectorConfig> ccOpt = collectorConfigRepository.findById(ccId);
                if (ccOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                CollectorConfig cc = ccOpt.get();
                cc.setMicroservice(saved);
                collectorConfigRepository.save(cc);
            }
        }

        return ResponseEntity.ok(toDto(saved));
    }

    private MicroserviceDto toDto(Microservice m) {
        MicroserviceDto dto = new MicroserviceDto();
        dto.setId(m.getId());
        dto.setName(m.getName());

        if (m.getMetadatas() != null) {
            dto.setMetadatas(m.getMetadatas().stream().map(md -> {
                MicroserviceMetadataDto mdDto = new MicroserviceMetadataDto();
                mdDto.setId(md.getId());
                mdDto.setMicroserviceId(md.getMicroservice() != null ? md.getMicroservice().getId() : null);
                mdDto.setVarName(md.getVarName());
                mdDto.setVarValue(md.getVarValue());
                return mdDto;
            }).collect(Collectors.toList()));
        }

        if (m.getCollectorConfigs() != null) {
            dto.setCollectorConfigIds(m.getCollectorConfigs().stream()
                    .map(cc -> cc.getId())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMicroservice(@PathVariable UUID id) {
        if (!microserviceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        microserviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

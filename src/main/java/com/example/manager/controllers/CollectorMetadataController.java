package com.example.manager.controllers;

import java.net.URI;
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

import com.example.manager.dtos.CollectorMetadataDto;
import com.example.manager.dtos.CollectorMetadataRequestDto;
import com.example.manager.models.Collector;
import com.example.manager.models.CollectorMetadata;
import com.example.manager.repositories.ICollectorMetadataRepository;
import com.example.manager.repositories.ICollectorRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/collector-metadata")
public class CollectorMetadataController {

    @Autowired
    private ICollectorMetadataRepository collectorMetadataRepository;

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<CollectorMetadataDto>> getAllCollectorMetadata() {
        List<CollectorMetadata> metadata = collectorMetadataRepository.findAll();
        List<CollectorMetadataDto> dtos = metadata.stream()
                .map(m -> {
                    CollectorMetadataDto dto = modelMapper.map(m, CollectorMetadataDto.class);
                    dto.setCollectorId(m.getCollector() != null ? m.getCollector().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectorMetadataDto> getCollectorMetadataById(@PathVariable UUID id) {
        return collectorMetadataRepository.findById(id)
                .map(m -> {
                    CollectorMetadataDto dto = modelMapper.map(m, CollectorMetadataDto.class);
                    dto.setCollectorId(m.getCollector() != null ? m.getCollector().getId() : null);
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCollectorMetadata(@Valid @RequestBody CollectorMetadataRequestDto req) {
        Collector collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
        if (req.getCollectorId() != null && collector == null) {
            return ResponseEntity.badRequest().build();
        }

        CollectorMetadata metadata = new CollectorMetadata();
        metadata.setUrl(req.getUrl());
        metadata.setRequestSchema(req.getRequestSchema());
        metadata.setPathToMetric(req.getPathToMetric());
        if (collector != null) {
            metadata.setCollector(collector);
        }

        CollectorMetadata saved = collectorMetadataRepository.save(metadata);
        CollectorMetadataDto dto = modelMapper.map(saved, CollectorMetadataDto.class);
        dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
        URI location = URI.create("/collector-metadata/" + saved.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollectorMetadata(@PathVariable UUID id, @Valid @RequestBody CollectorMetadataRequestDto req) {
        return collectorMetadataRepository.findById(id).map(existing -> {
            Collector collector = null;
            if (req.getCollectorId() != null) {
                collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
                if (collector == null) return ResponseEntity.badRequest().build();
            }

            existing.setUrl(req.getUrl());
            existing.setRequestSchema(req.getRequestSchema());
            existing.setPathToMetric(req.getPathToMetric());
            if (collector != null) existing.setCollector(collector);

            CollectorMetadata saved = collectorMetadataRepository.save(existing);
            CollectorMetadataDto dto = modelMapper.map(saved, CollectorMetadataDto.class);
            dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollectorMetadata(@PathVariable UUID id) {
        return collectorMetadataRepository.findById(id).map(m -> {
            collectorMetadataRepository.delete(m);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

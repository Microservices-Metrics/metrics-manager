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

import com.example.manager.dtos.CollectorResponseSchemaDto;
import com.example.manager.dtos.CollectorResponseSchemaRequestDto;
import com.example.manager.models.Collector;
import com.example.manager.models.CollectorResponseSchema;
import com.example.manager.repositories.ICollectorRepository;
import com.example.manager.repositories.ICollectorResponseSchemaRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/collector-response-schemas")
public class CollectorResponseSchemaController {

    @Autowired
    private ICollectorResponseSchemaRepository responseSchemaRepository;

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<CollectorResponseSchemaDto>> getAllResponseSchemas() {
        List<CollectorResponseSchema> schemas = responseSchemaRepository.findAll();
        List<CollectorResponseSchemaDto> dtos = schemas.stream()
                .map(s -> {
                    CollectorResponseSchemaDto dto = modelMapper.map(s, CollectorResponseSchemaDto.class);
                    dto.setCollectorId(s.getCollector() != null ? s.getCollector().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectorResponseSchemaDto> getResponseSchemaById(@PathVariable UUID id) {
        return responseSchemaRepository.findById(id)
                .map(s -> {
                    CollectorResponseSchemaDto dto = modelMapper.map(s, CollectorResponseSchemaDto.class);
                    dto.setCollectorId(s.getCollector() != null ? s.getCollector().getId() : null);
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createResponseSchema(@Valid @RequestBody CollectorResponseSchemaRequestDto req) {
        Collector collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
        if (req.getCollectorId() != null && collector == null) {
            return ResponseEntity.badRequest().build();
        }

        CollectorResponseSchema schema = new CollectorResponseSchema();
        schema.setSchema(req.getSchema());
        schema.setStatusType(req.getStatusType());
        schema.setDescription(req.getDescription());
        if (collector != null) {
            schema.setCollector(collector);
        }

        CollectorResponseSchema saved = responseSchemaRepository.save(schema);
        CollectorResponseSchemaDto dto = modelMapper.map(saved, CollectorResponseSchemaDto.class);
        dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
        URI location = URI.create("/collector-response-schemas/" + saved.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResponseSchema(@PathVariable UUID id, @Valid @RequestBody CollectorResponseSchemaRequestDto req) {
        return responseSchemaRepository.findById(id).map(existing -> {
            Collector collector = null;
            if (req.getCollectorId() != null) {
                collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
                if (collector == null) return ResponseEntity.badRequest().build();
            }

            existing.setSchema(req.getSchema());
            existing.setStatusType(req.getStatusType());
            existing.setDescription(req.getDescription());
            if (collector != null) existing.setCollector(collector);

            CollectorResponseSchema saved = responseSchemaRepository.save(existing);
            CollectorResponseSchemaDto dto = modelMapper.map(saved, CollectorResponseSchemaDto.class);
            dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponseSchema(@PathVariable UUID id) {
        return responseSchemaRepository.findById(id).map(s -> {
            responseSchemaRepository.delete(s);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

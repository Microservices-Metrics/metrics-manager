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
import com.example.manager.models.Collector;
import com.example.manager.models.Metric;
import com.example.manager.repositories.ICollectorRepository;
import com.example.manager.repositories.IMetricRepository;

@RestController
@RequestMapping("/collectors")
public class CollectorController {

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private IMetricRepository metricRepository;

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
        return dto;
    }
}

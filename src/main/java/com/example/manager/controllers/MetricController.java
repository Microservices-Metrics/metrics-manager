package com.example.manager.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.manager.dtos.MetricDto;
import com.example.manager.models.Metric;
import com.example.manager.repositories.IMetricRepository;

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

@RestController
@RequestMapping("/metrics")
public class MetricController {

    @Autowired
    private IMetricRepository metricRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping()
    public ResponseEntity<List<MetricDto>> getAllMetrics() {
        List<Metric> metrics = metricRepository.findAll();
        List<MetricDto> dtos = metrics.stream()
                .map(metric -> modelMapper.map(metric, MetricDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetricDto> getMetricById(@PathVariable UUID id) {
        Optional<Metric> metricOpt = metricRepository.findById(id);
        if (metricOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MetricDto dto = modelMapper.map(metricOpt.get(), MetricDto.class);

        return ResponseEntity.ok(dto);
    }

    @PostMapping()
    public ResponseEntity<MetricDto> createMetric(@RequestBody MetricDto dto) {
        Metric metric = modelMapper.map(dto, Metric.class);
        metric = metricRepository.save(metric);
        MetricDto response = modelMapper.map(metric, MetricDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetricDto> updateMetric(@PathVariable UUID id, @RequestBody MetricDto dto) {
        Optional<Metric> metricOpt = metricRepository.findById(id);
        if (metricOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Metric existing = metricOpt.get();
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setType(dto.getType());
        existing.setUnit(dto.getUnit());

        Metric saved = metricRepository.save(existing);
        MetricDto response = modelMapper.map(saved, MetricDto.class);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetric(@PathVariable UUID id) {
        if (!metricRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        metricRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// TODO: criar camada MetricsService
@RestController
@RequestMapping("/metrics")
public class MetricsController {

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
    public ResponseEntity<MetricDto> getMetricById(@PathVariable("id") UUID id) {
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
}

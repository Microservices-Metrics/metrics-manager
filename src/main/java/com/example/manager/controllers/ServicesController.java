package com.example.manager.controllers;

import com.example.manager.dtos.MetricServiceDto;
import com.example.manager.models.MetricService;
import com.example.manager.repositories.IMetricServiceRepository;

import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// TODO: criar camada Service
@RestController
@RequestMapping("/services")
public class ServicesController {

    @Autowired
    IMetricServiceRepository metricServiceRepository;

    @GetMapping
    public ResponseEntity<List<MetricService>> getAllMetricServices() {
        return ResponseEntity.status(HttpStatus.OK).body(metricServiceRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getMetricServiceById(@PathVariable(value="id") UUID idService) {
        Optional<MetricService> metricService = metricServiceRepository.findById(idService);

        if (metricService.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Metric Service not found!");

        return ResponseEntity.status(HttpStatus.OK).body(metricService.get());
    }

    @PostMapping
    public ResponseEntity<MetricService> createServiceMetric(@RequestBody MetricServiceDto metricServiceDto) {
        var metricService = new MetricService();

        BeanUtils.copyProperties(metricServiceDto, metricService);

        return ResponseEntity.status(HttpStatus.CREATED).body(metricServiceRepository.save(metricService));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateServiceMetric(@PathVariable(value="id") UUID idService,
                                                      @RequestBody @Valid MetricServiceDto metricServiceDto) {
        Optional<MetricService> metricService = metricServiceRepository.findById(idService);

        if (metricService.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Metric Service not found!");

        var updatedMetricService = metricService.get();
        
        BeanUtils.copyProperties(metricServiceDto, updatedMetricService);

        return ResponseEntity.status(HttpStatus.OK).body(metricServiceRepository.save(updatedMetricService));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteServiceMetric(@PathVariable(value="id") UUID idService) {
        Optional<MetricService> metricService = metricServiceRepository.findById(idService);

        if (metricService.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Metric Service not found!");

        metricServiceRepository.delete(metricService.get());
        
        return ResponseEntity.status(HttpStatus.OK).body("Metric Service deleted!");
    }
}

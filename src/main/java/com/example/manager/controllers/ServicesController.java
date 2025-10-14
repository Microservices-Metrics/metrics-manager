package com.example.manager.controllers;

import com.example.manager.dtos.MetricServiceDto;
import com.example.manager.models.MetricService;
import com.example.manager.repositories.IMetricServiceRepository;
import com.example.manager.services.MetricSchedulingService;

import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// TODO: criar camada Service
@RestController
@RequestMapping("/services")
public class ServicesController {

    @Autowired
    IMetricServiceRepository metricServiceRepository;

    @Autowired
    private MetricSchedulingService metricSchedulingService;

    @Autowired
    private ModelMapper modelMapper;

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
        MetricService metricService = metricSchedulingService.createMetricServiceAndSchedule(metricServiceDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(metricService);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateServiceMetric(@PathVariable(value="id") UUID idService,
                                                      @RequestBody @Valid MetricServiceDto metricServiceDto) {
        Optional<MetricService> metricService = metricServiceRepository.findById(idService);

        if (metricService.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Metric Service not found!");

        MetricService updatedMetricService = modelMapper.map(metricServiceDto, MetricService.class);
        
        updatedMetricService.setIdService(idService);
        
        return ResponseEntity.status(HttpStatus.OK).body(metricServiceRepository.save(updatedMetricService));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Object> deleteServiceMetric(@PathVariable(value="id") UUID idService) {
        Optional<MetricService> metricService = metricServiceRepository.findById(idService);

        if (metricService.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Metric Service not found!");

        metricServiceRepository.delete(metricService.get());

        return ResponseEntity.status(HttpStatus.OK).body("Metric Service deleted!");
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<List<UUID>> deleteAllServiceMetrics() {
        List<MetricService> allServices = metricServiceRepository.findAll();

        metricServiceRepository.deleteAll();

        // TODO: retornar UUIDs dentro de uma propriedade chamada "deletedMetricsServicesIds"
        return ResponseEntity.status(HttpStatus.OK).body(allServices.stream().map(MetricService::getIdService).toList());
    }

}

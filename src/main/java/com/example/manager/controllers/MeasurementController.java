package com.example.manager.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.manager.dtos.MeasurementDto;
import com.example.manager.models.Measurement;
import com.example.manager.repositories.IMeasurementRepository;

@RestController
@RequestMapping("/measurements")
public class MeasurementController {

    @Autowired
    private IMeasurementRepository measurementRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<MeasurementDto>> getMeasurements(
            @RequestParam(required = false) UUID collectorId,
            @RequestParam(required = false) UUID collectorConfigId) {

        List<Measurement> measurements;

        if (collectorConfigId != null) {
            measurements = measurementRepository.findByCollectorConfigId(collectorConfigId);
        } else if (collectorId != null) {
            measurements = measurementRepository.findByCollectorConfigCollectorId(collectorId);
        } else {
            measurements = measurementRepository.findAll();
        }

        List<MeasurementDto> dtos = measurements.stream()
                .map(m -> modelMapper.map(m, MeasurementDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeasurementDto> getMeasurementById(@PathVariable UUID id) {
        Optional<Measurement> measurementOpt = measurementRepository.findById(id);
        if (measurementOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MeasurementDto dto = modelMapper.map(measurementOpt.get(), MeasurementDto.class);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeasurementById(@PathVariable UUID id) {
        if (!measurementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        measurementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @DeleteMapping
    public ResponseEntity<Void> deleteMeasurements(
            @RequestParam(required = false) UUID collectorId,
            @RequestParam(required = false) UUID collectorConfigId) {

        if (collectorConfigId != null) {
            measurementRepository.deleteByCollectorConfigId(collectorConfigId);
        } else if (collectorId != null) {
            measurementRepository.deleteByCollectorConfigCollectorId(collectorId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }
}

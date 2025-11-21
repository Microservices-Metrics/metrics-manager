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

import com.example.manager.dtos.CollectorDto;
import com.example.manager.dtos.CollectorRequestDto;
import com.example.manager.dtos.CollectorResponseSchemaDto;
import com.example.manager.models.Collector;
import com.example.manager.models.Metric;
import com.example.manager.repositories.ICollectorRepository;
import com.example.manager.repositories.IMetricRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/collectors")
public class CollectorsController {

	@Autowired
	private ICollectorRepository collectorRepository;

	@Autowired
	private IMetricRepository metricRepository;

	@Autowired
	private ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<List<CollectorDto>> getAllCollectors() {
		List<Collector> collectors = collectorRepository.findAll();
		List<CollectorDto> dtos = collectors.stream()
				.map(c -> {
					CollectorDto dto = modelMapper.map(c, CollectorDto.class);
					dto.setMetricId(c.getMetricId());
					dto.setResponseSchemas(c.getResponseSchemas().stream().map(rs -> {
						CollectorResponseSchemaDto rsDto = new CollectorResponseSchemaDto();
						rsDto.setId(rs.getId());
						rsDto.setSchema(rs.getSchema());
						rsDto.setStatusCode(rs.getStatusType());
						rsDto.setDescription(rs.getDescription());
						return rsDto;
					}).collect(Collectors.toList()));
					return dto;
				})
				.collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CollectorDto> getCollectorById(@PathVariable UUID id) {
		return collectorRepository.findById(id)
				.map(c -> {
					CollectorDto dto = modelMapper.map(c, CollectorDto.class);
					dto.setMetricId(c.getMetricId());
					dto.setResponseSchemas(c.getResponseSchemas().stream().map(rs -> {
						CollectorResponseSchemaDto rsDto = new CollectorResponseSchemaDto();
						rsDto.setId(rs.getId());
						rsDto.setSchema(rs.getSchema());
						rsDto.setStatusCode(rs.getStatusType());
						rsDto.setDescription(rs.getDescription());
						return rsDto;
					}).collect(Collectors.toList()));
					return ResponseEntity.ok(dto);
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<?> createCollector(@Valid @RequestBody CollectorRequestDto req) {
		Metric metric = metricRepository.findById(req.getMetricId()).orElse(null);
		if (req.getMetricId() != null && metric == null) {
			return ResponseEntity.badRequest().build();
		}

		Collector collector = new Collector();
		collector.setName(req.getName());
		collector.setDescription(req.getDescription());
		collector.setCollectionMethod(req.getCollectionMethod());
		collector.setUrl(req.getUrl());
		collector.setRequestSchema(req.getRequestSchema());
		collector.setPathToMetric(req.getPathToMetric());
		if (metric != null) {
			collector.setMetric(metric);
		}

		Collector saved = collectorRepository.save(collector);
		CollectorDto dto = modelMapper.map(saved, CollectorDto.class);
		dto.setMetricId(saved.getMetricId());
		// saved is new; responseSchemas may be empty but map for completeness
		dto.setResponseSchemas(saved.getResponseSchemas().stream().map(rs -> {
			CollectorResponseSchemaDto rsDto = new CollectorResponseSchemaDto();
			rsDto.setId(rs.getId());
			rsDto.setSchema(rs.getSchema());
			rsDto.setStatusCode(rs.getStatusType());
			rsDto.setDescription(rs.getDescription());
			return rsDto;
		}).collect(Collectors.toList()));
		URI location = URI.create("/collectors/" + saved.getId());
		return ResponseEntity.created(location).body(dto);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody CollectorRequestDto req) {
		return collectorRepository.findById(id).map(existing -> {
			Metric metric = null;
			if (req.getMetricId() != null) {
				metric = metricRepository.findById(req.getMetricId()).orElse(null);
				if (metric == null) return ResponseEntity.badRequest().build();
			}

			existing.setName(req.getName());
			existing.setDescription(req.getDescription());
			existing.setCollectionMethod(req.getCollectionMethod());
			existing.setUrl(req.getUrl());
			existing.setRequestSchema(req.getRequestSchema());
			existing.setPathToMetric(req.getPathToMetric());
			if (metric != null) existing.setMetric(metric);

			Collector saved = collectorRepository.save(existing);
			CollectorDto dto = modelMapper.map(saved, CollectorDto.class);
			dto.setMetricId(saved.getMetricId());
			// map responseSchemas
			dto.setResponseSchemas(saved.getResponseSchemas().stream().map(rs -> {
				CollectorResponseSchemaDto rsDto = new CollectorResponseSchemaDto();
				rsDto.setId(rs.getId());
				rsDto.setSchema(rs.getSchema());
				rsDto.setStatusCode(rs.getStatusType());
				rsDto.setDescription(rs.getDescription());
				return rsDto;
			}).collect(Collectors.toList()));
			return ResponseEntity.ok(dto);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		return collectorRepository.findById(id).map(c -> {
			collectorRepository.delete(c);
			return ResponseEntity.noContent().<Void>build();
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}
}

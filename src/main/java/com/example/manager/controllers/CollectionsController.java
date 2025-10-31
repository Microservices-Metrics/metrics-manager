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

import com.example.manager.dtos.CollectionDto;
import com.example.manager.dtos.CollectionRequestDto;
import com.example.manager.models.Collection;
import com.example.manager.models.Collector;
import com.example.manager.models.Microservice;
import com.example.manager.repositories.ICollectionRepository;
import com.example.manager.repositories.ICollectorRepository;
import com.example.manager.repositories.IMicroserviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/collections")
public class CollectionsController {

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private ICollectorRepository collectorRepository;

    @Autowired
    private IMicroserviceRepository microserviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<CollectionDto>> getAllCollections() {
        List<Collection> collections = collectionRepository.findAll();
        List<CollectionDto> dtos = collections.stream()
                .map(c -> {
                    CollectionDto dto = modelMapper.map(c, CollectionDto.class);
                    dto.setCollectorId(c.getCollector() != null ? c.getCollector().getId() : null);
                    dto.setMicroserviceId(c.getMicroservice() != null ? c.getMicroservice().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollectionById(@PathVariable UUID id) {
        return collectionRepository.findById(id)
                .map(c -> {
                    CollectionDto dto = modelMapper.map(c, CollectionDto.class);
                    dto.setCollectorId(c.getCollector() != null ? c.getCollector().getId() : null);
                    dto.setMicroserviceId(c.getMicroservice() != null ? c.getMicroservice().getId() : null);
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCollection(@Valid @RequestBody CollectionRequestDto req) {
        Collector collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
        if (req.getCollectorId() != null && collector == null)
            return ResponseEntity.badRequest().build();

        Microservice microservice = microserviceRepository.findById(req.getMicroserviceId()).orElse(null);
        if (req.getMicroserviceId() != null && microservice == null)
            return ResponseEntity.badRequest().build();

        Collection collection = new Collection();
        if (collector != null)
            collection.setCollector(collector);

        if (microservice != null)
            collection.setMicroservice(microservice);

        Collection saved = collectionRepository.save(collection);
        CollectionDto dto = modelMapper.map(saved, CollectionDto.class);
        dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
        dto.setMicroserviceId(saved.getMicroservice() != null ? saved.getMicroservice().getId() : null);

        URI location = URI.create("/collections/" + saved.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollection(@PathVariable UUID id, @Valid @RequestBody CollectionRequestDto req) {
        return collectionRepository.findById(id).map(existing -> {
            Collector collector = null;
            if (req.getCollectorId() != null) {
                collector = collectorRepository.findById(req.getCollectorId()).orElse(null);
                if (collector == null)
                    return ResponseEntity.badRequest().build();
            }

            Microservice microservice = null;
            if (req.getMicroserviceId() != null) {
                microservice = microserviceRepository.findById(req.getMicroserviceId()).orElse(null);
                if (microservice == null)
                    return ResponseEntity.badRequest().build();
            }

            if (collector != null)
                existing.setCollector(collector);
                
            if (microservice != null)
                existing.setMicroservice(microservice);

            Collection saved = collectionRepository.save(existing);
            CollectionDto dto = modelMapper.map(saved, CollectionDto.class);
            dto.setCollectorId(saved.getCollector() != null ? saved.getCollector().getId() : null);
            dto.setMicroserviceId(saved.getMicroservice() != null ? saved.getMicroservice().getId() : null);

            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable UUID id) {
        return collectionRepository.findById(id).map(c -> {
            collectionRepository.delete(c);

            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

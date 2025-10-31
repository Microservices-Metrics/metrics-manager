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

import com.example.manager.dtos.MicroserviceDto;
import com.example.manager.dtos.MicroserviceRequestDto;
import com.example.manager.dtos.MicroserviceMetadatasDto;
import com.example.manager.dtos.CollectionDto;
import com.example.manager.models.Microservice;
import com.example.manager.repositories.IMicroserviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/microservices")
public class MicroservicesController {

    @Autowired
    private IMicroserviceRepository microserviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<MicroserviceDto>> getAllMicroservices() {
        List<Microservice> microservices = microserviceRepository.findAll();
        List<MicroserviceDto> dtos = microservices.stream()
                .map(m -> {
                    MicroserviceDto dto = modelMapper.map(m, MicroserviceDto.class);
                    // map metadatas
                    dto.setMetadatas(m.getMetadatas().stream().map(meta -> {
                        MicroserviceMetadatasDto metaDto = new MicroserviceMetadatasDto();
                        metaDto.setId(meta.getId());
                        metaDto.setVarName(meta.getVarName());
                        metaDto.setVarValue(meta.getVarValue());
                        
                        return metaDto;
                    }).collect(Collectors.toList()));
                    // map collections
                    dto.setCollections(m.getCollections().stream().map(col -> {
                        CollectionDto colDto = new CollectionDto();
                        colDto.setId(col.getId());
                        colDto.setCollectorId(col.getCollector() != null ? col.getCollector().getId() : null);
                        colDto.setMicroserviceId(col.getMicroservice() != null ? col.getMicroservice().getId() : null);

                        return colDto;
                    }).collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MicroserviceDto> getMicroserviceById(@PathVariable UUID id) {
        return microserviceRepository.findById(id)
                .map(m -> {
                    MicroserviceDto dto = modelMapper.map(m, MicroserviceDto.class);
                    // map metadatas
                    dto.setMetadatas(m.getMetadatas().stream().map(meta -> {
                        MicroserviceMetadatasDto metaDto = new MicroserviceMetadatasDto();
                        metaDto.setId(meta.getId());
                        metaDto.setVarName(meta.getVarName());
                        metaDto.setVarValue(meta.getVarValue());

                        return metaDto;
                    }).collect(Collectors.toList()));
                    // map collections
                    dto.setCollections(m.getCollections().stream().map(col -> {
                        CollectionDto colDto = new CollectionDto();
                        colDto.setId(col.getId());
                        colDto.setCollectorId(col.getCollector() != null ? col.getCollector().getId() : null);
                        colDto.setMicroserviceId(col.getMicroservice() != null ? col.getMicroservice().getId() : null);

                        return colDto;
                    }).collect(Collectors.toList()));

                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMicroservice(@Valid @RequestBody MicroserviceRequestDto req) {
        Microservice microservice = new Microservice();
        microservice.setName(req.getName());

        Microservice saved = microserviceRepository.save(microservice);
        MicroserviceDto dto = modelMapper.map(saved, MicroserviceDto.class);

        // salvo é novo (collections e metadatas podem estar vazios)
        dto.setMetadatas(saved.getMetadatas().stream().map(meta -> {
            MicroserviceMetadatasDto metaDto = new MicroserviceMetadatasDto();
            metaDto.setId(meta.getId());
            metaDto.setVarName(meta.getVarName());
            metaDto.setVarValue(meta.getVarValue());

            return metaDto;
        }).collect(Collectors.toList()));
        dto.setCollections(saved.getCollections().stream().map(col -> {
            CollectionDto colDto = new CollectionDto();
            colDto.setId(col.getId());
            colDto.setCollectorId(col.getCollector() != null ? col.getCollector().getId() : null);
            colDto.setMicroserviceId(col.getMicroservice() != null ? col.getMicroservice().getId() : null);

            return colDto;
        }).collect(Collectors.toList()));

        URI location = URI.create("/microservices/" + saved.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMicroservice(@PathVariable UUID id, @Valid @RequestBody MicroserviceRequestDto req) {
        return microserviceRepository.findById(id).map(existing -> {
            existing.setName(req.getName());

            Microservice saved = microserviceRepository.save(existing);
            MicroserviceDto dto = modelMapper.map(saved, MicroserviceDto.class);
            // map metadatas
            dto.setMetadatas(saved.getMetadatas().stream().map(meta -> {
                MicroserviceMetadatasDto metaDto = new MicroserviceMetadatasDto();
                metaDto.setId(meta.getId());
                metaDto.setVarName(meta.getVarName());
                metaDto.setVarValue(meta.getVarValue());

                return metaDto;
            }).collect(Collectors.toList()));
            // map collections
            dto.setCollections(saved.getCollections().stream().map(col -> {
                CollectionDto colDto = new CollectionDto();
                colDto.setId(col.getId());
                colDto.setCollectorId(col.getCollector() != null ? col.getCollector().getId() : null);
                colDto.setMicroserviceId(col.getMicroservice() != null ? col.getMicroservice().getId() : null);

                return colDto;
            }).collect(Collectors.toList()));

            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMicroservice(@PathVariable UUID id) {
        return microserviceRepository.findById(id).map(m -> {
            microserviceRepository.delete(m);

            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

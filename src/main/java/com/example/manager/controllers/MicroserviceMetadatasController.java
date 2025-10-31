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

import com.example.manager.dtos.MicroserviceMetadatasDto;
import com.example.manager.dtos.MicroserviceMetadatasRequestDto;
import com.example.manager.models.Microservice;
import com.example.manager.models.MicroserviceMetadatas;
import com.example.manager.repositories.IMicroserviceMetadatasRepository;
import com.example.manager.repositories.IMicroserviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/microservice-metadatas")
public class MicroserviceMetadatasController {

    @Autowired
    private IMicroserviceMetadatasRepository metadatasRepository;

    @Autowired
    private IMicroserviceRepository microserviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<MicroserviceMetadatasDto>> getAllMetadatas() {
        List<MicroserviceMetadatas> metadatas = metadatasRepository.findAll();
        List<MicroserviceMetadatasDto> dtos = metadatas.stream()
                .map(m -> modelMapper.map(m, MicroserviceMetadatasDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MicroserviceMetadatasDto> getMetadataById(@PathVariable UUID id) {
        return metadatasRepository.findById(id)
                .map(m -> {
                    MicroserviceMetadatasDto dto = modelMapper.map(m, MicroserviceMetadatasDto.class);

                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMetadata(@Valid @RequestBody MicroserviceMetadatasRequestDto req) {
        Microservice microservice = microserviceRepository.findById(req.getMicroserviceId()).orElse(null);
        if (req.getMicroserviceId() != null && microservice == null) {
            return ResponseEntity.badRequest().build();
        }

        MicroserviceMetadatas metadata = new MicroserviceMetadatas();
        metadata.setVarName(req.getVarName());
        metadata.setVarValue(req.getVarValue());

        if (microservice != null) {
            metadata.setMicroservice(microservice);
        }

        MicroserviceMetadatas saved = metadatasRepository.save(metadata);
        MicroserviceMetadatasDto dto = modelMapper.map(saved, MicroserviceMetadatasDto.class);
        URI location = URI.create("/microservice-metadatas/" + saved.getId());

        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMetadata(@PathVariable UUID id, @Valid @RequestBody MicroserviceMetadatasRequestDto req) {
        return metadatasRepository.findById(id).map(existing -> {
            Microservice microservice = null;
            if (req.getMicroserviceId() != null) {
                microservice = microserviceRepository.findById(req.getMicroserviceId()).orElse(null);
                if (microservice == null) return ResponseEntity.badRequest().build();
            }

            existing.setVarName(req.getVarName());
            existing.setVarValue(req.getVarValue());

            if (microservice != null) existing.setMicroservice(microservice);

            MicroserviceMetadatas saved = metadatasRepository.save(existing);
            MicroserviceMetadatasDto dto = modelMapper.map(saved, MicroserviceMetadatasDto.class);
            
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetadata(@PathVariable UUID id) {
        return metadatasRepository.findById(id).map(m -> {
            metadatasRepository.delete(m);
            
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

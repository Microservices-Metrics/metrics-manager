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

import com.example.manager.dtos.MicroserviceMetadataDto;
import com.example.manager.models.MicroserviceMetadata;
import com.example.manager.models.Microservice;
import com.example.manager.repositories.IMicroserviceMetadataRepository;
import com.example.manager.repositories.IMicroserviceRepository;

@RestController
@RequestMapping("/microservice-metadatas")
public class MicroserviceMetadataController {

    @Autowired
    private IMicroserviceMetadataRepository metadataRepository;

    @Autowired
    private IMicroserviceRepository microserviceRepository;

    @GetMapping
    public ResponseEntity<List<MicroserviceMetadataDto>> listAll() {
        List<MicroserviceMetadata> list = metadataRepository.findAll();
        List<MicroserviceMetadataDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MicroserviceMetadataDto> getById(@PathVariable UUID id) {
        Optional<MicroserviceMetadata> opt = metadataRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDto(opt.get()));
    }

    @GetMapping("/microservice/{microserviceId}")
    public ResponseEntity<List<MicroserviceMetadataDto>> getByMicroservice(@PathVariable UUID microserviceId) {
        if (!microserviceRepository.existsById(microserviceId)) {
            return ResponseEntity.notFound().build();
        }

        List<MicroserviceMetadata> list = metadataRepository.findByMicroserviceId(microserviceId);
        List<MicroserviceMetadataDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<MicroserviceMetadataDto> create(@RequestBody MicroserviceMetadataDto dto) {
        if (dto.getMicroserviceId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<Microservice> msOpt = microserviceRepository.findById(dto.getMicroserviceId());
        if (msOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        MicroserviceMetadata md = new MicroserviceMetadata();
        md.setVarName(dto.getVarName());
        md.setVarValue(dto.getVarValue());
        md.setMicroservice(msOpt.get());

        md = metadataRepository.save(md);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(md));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MicroserviceMetadataDto> update(@PathVariable UUID id, @RequestBody MicroserviceMetadataDto dto) {
        Optional<MicroserviceMetadata> opt = metadataRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MicroserviceMetadata existing = opt.get();

        if (dto.getMicroserviceId() != null) {
            Optional<Microservice> msOpt = microserviceRepository.findById(dto.getMicroserviceId());
            if (msOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            existing.setMicroservice(msOpt.get());
        }

        existing.setVarName(dto.getVarName());
        existing.setVarValue(dto.getVarValue());

        MicroserviceMetadata saved = metadataRepository.save(existing);

        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!metadataRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        metadataRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MicroserviceMetadataDto toDto(MicroserviceMetadata md) {
        MicroserviceMetadataDto dto = new MicroserviceMetadataDto();
        dto.setId(md.getId());
        dto.setMicroserviceId(md.getMicroservice() != null ? md.getMicroservice().getId() : null);
        dto.setVarName(md.getVarName());
        dto.setVarValue(md.getVarValue());

        return dto;
    }
}

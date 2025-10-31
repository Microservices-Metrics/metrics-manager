package com.example.manager.dtos;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class MicroserviceDto {
    private UUID id;
    private String name;
    private List<MicroserviceMetadatasDto> metadatas = new ArrayList<>();
    private List<CollectionDto> collections = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MicroserviceMetadatasDto> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(List<MicroserviceMetadatasDto> metadatas) {
        this.metadatas = metadatas;
    }

    public List<CollectionDto> getCollections() {
        return collections;
    }

    public void setCollections(List<CollectionDto> collections) {
        this.collections = collections;
    }
}

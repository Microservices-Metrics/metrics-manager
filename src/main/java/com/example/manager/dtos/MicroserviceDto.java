package com.example.manager.dtos;

import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class MicroserviceDto implements Serializable {
    private UUID id;
    private String name;
    private List<MicroserviceMetadataDto> metadatas = new ArrayList<>();
    private List<UUID> collectorConfigIds = new ArrayList<>();

    public MicroserviceDto() { }

    public MicroserviceDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<MicroserviceMetadataDto> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(List<MicroserviceMetadataDto> metadatas) {
        this.metadatas = metadatas;
    }

    public List<UUID> getCollectorConfigIds() {
        return collectorConfigIds;
    }

    public void setCollectorConfigIds(List<UUID> collectorConfigIds) {
        this.collectorConfigIds = collectorConfigIds;
    }

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
}

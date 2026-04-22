package com.example.manager.dtos;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class CollectorResponseDto implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private String collectionMethod;
    private UUID metricId;
    private List<CollectorResponseSchemaResponseDto> responseSchemas;
    private List<CollectorMetadataResponseDto> metadata;
    private List<CollectorConfigResponseDto> configs;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(String collectionMethod) {
        this.collectionMethod = collectionMethod;
    }

    public UUID getMetricId() {
        return metricId;
    }

    public void setMetricId(UUID metricId) {
        this.metricId = metricId;
    }

    public List<CollectorResponseSchemaResponseDto> getResponseSchemas() {
        return responseSchemas;
    }

    public void setResponseSchemas(List<CollectorResponseSchemaResponseDto> responseSchemas) {
        this.responseSchemas = responseSchemas;
    }

    public List<CollectorMetadataResponseDto> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<CollectorMetadataResponseDto> metadata) {
        this.metadata = metadata;
    }

    public List<CollectorConfigResponseDto> getConfigs() {
        return configs;
    }

    public void setConfigs(List<CollectorConfigResponseDto> configs) {
        this.configs = configs;
    }
}

package com.example.manager.dtos;

import java.io.Serializable;
import java.util.UUID;

public class CollectorDto implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private String collectionMethod;
    private UUID metricId;

    public CollectorDto() {
    }

    public CollectorDto(UUID id, String name, String description, String collectionMethod, UUID metricId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.collectionMethod = collectionMethod;
        this.metricId = metricId;
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
}

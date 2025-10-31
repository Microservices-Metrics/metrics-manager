package com.example.manager.dtos;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class CollectorDto {
    private UUID id;
    private String name;
    private String description;
    private String collectionMethod;
    private UUID metricId;
    private String url;
    private String requestSchema;
    private String pathToMetric;
    private List<CollectorResponseSchemaDto> responseSchemas = new ArrayList<>();

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestSchema() {
        return requestSchema;
    }

    public void setRequestSchema(String requestSchema) {
        this.requestSchema = requestSchema;
    }

    public String getPathToMetric() {
        return pathToMetric;
    }

    public void setPathToMetric(String pathToMetric) {
        this.pathToMetric = pathToMetric;
    }

    public List<CollectorResponseSchemaDto> getResponseSchemas() {
        return responseSchemas;
    }

    public void setResponseSchemas(List<CollectorResponseSchemaDto> responseSchemas) {
        this.responseSchemas = responseSchemas;
    }
}

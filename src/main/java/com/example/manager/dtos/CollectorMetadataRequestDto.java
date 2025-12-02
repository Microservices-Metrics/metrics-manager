package com.example.manager.dtos;

import java.util.UUID;

public class CollectorMetadataRequestDto {
    private UUID collectorId;
    private String url;
    private String requestSchema;
    private String pathToMetric;

    public UUID getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(UUID collectorId) {
        this.collectorId = collectorId;
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
}

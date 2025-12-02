package com.example.manager.dtos;

import java.io.Serializable;
import java.util.UUID;

public class CollectorMetadataResponseDto implements Serializable {
    private UUID id;
    private String url;
    private String requestSchema;
    private String pathToMetric;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

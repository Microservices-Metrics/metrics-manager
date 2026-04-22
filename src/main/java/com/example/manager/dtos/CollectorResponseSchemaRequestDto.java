package com.example.manager.dtos;

import java.util.UUID;

public class CollectorResponseSchemaRequestDto {
    private UUID collectorId;
    private String schema;
    private int statusType;
    private String description;

    public UUID getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(UUID collectorId) {
        this.collectorId = collectorId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getStatusType() {
        return statusType;
    }

    public void setStatusType(int statusType) {
        this.statusType = statusType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

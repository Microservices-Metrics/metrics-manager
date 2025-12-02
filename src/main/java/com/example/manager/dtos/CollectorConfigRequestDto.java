package com.example.manager.dtos;

import java.util.UUID;

public class CollectorConfigRequestDto {
    private UUID collectorId;
    private UUID microserviceId;
    private String cronExpression;
    private String startDateTime;
    private String endDateTime;

    public UUID getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(UUID collectorId) {
        this.collectorId = collectorId;
    }

    public UUID getMicroserviceId() {
        return microserviceId;
    }

    public void setMicroserviceId(UUID microserviceId) {
        this.microserviceId = microserviceId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
}

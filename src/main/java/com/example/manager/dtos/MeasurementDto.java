package com.example.manager.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class MeasurementDto implements Serializable {
    private UUID id;
    private UUID collectorConfigId;
    private LocalDateTime startTimestamp;
    private String responseStatus;
    private String responseBody;
    private String metricValue;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCollectorConfigId() {
        return collectorConfigId;
    }

    public void setCollectorConfigId(UUID collectorConfigId) {
        this.collectorConfigId = collectorConfigId;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }
}

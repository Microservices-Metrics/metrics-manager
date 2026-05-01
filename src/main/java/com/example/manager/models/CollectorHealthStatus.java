package com.example.manager.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class CollectorHealthStatus {

    public enum Status { UP, DOWN, UNKNOWN }

    private final UUID collectorId;
    private final String collectorName;
    private Status status;
    private String checkedUrl;
    private LocalDateTime lastChecked;
    private String detail;

    public CollectorHealthStatus(UUID collectorId, String collectorName) {
        this.collectorId = collectorId;
        this.collectorName = collectorName;
        this.status = Status.UNKNOWN;
    }

    public UUID getCollectorId() { return collectorId; }
    public String getCollectorName() { return collectorName; }
    public Status getStatus() { return status; }
    public String getCheckedUrl() { return checkedUrl; }
    public LocalDateTime getLastChecked() { return lastChecked; }
    public String getDetail() { return detail; }

    public void setStatus(Status status) { this.status = status; }
    public void setCheckedUrl(String checkedUrl) { this.checkedUrl = checkedUrl; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
    public void setDetail(String detail) { this.detail = detail; }
}

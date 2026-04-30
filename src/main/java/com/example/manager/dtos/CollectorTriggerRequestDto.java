package com.example.manager.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class CollectorTriggerRequestDto {

    @NotNull
    private UUID collectorId;

    @NotNull
    private UUID microserviceId;

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
}

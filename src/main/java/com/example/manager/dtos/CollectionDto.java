package com.example.manager.dtos;

import java.util.UUID;

public class CollectionDto {
    private UUID id;
    private UUID collectorId;
    private UUID microserviceId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

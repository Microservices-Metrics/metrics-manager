package com.example.manager.dtos;

import java.io.Serializable;
import java.util.UUID;

public class MicroserviceMetadataDto implements Serializable {
    private UUID id;
    private UUID microserviceId;
    private String varName;
    private String varValue;

    public MicroserviceMetadataDto() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMicroserviceId() {
        return microserviceId;
    }

    public void setMicroserviceId(UUID microserviceId) {
        this.microserviceId = microserviceId;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }
}

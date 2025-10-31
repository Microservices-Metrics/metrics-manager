package com.example.manager.dtos;

import java.util.UUID;

public class MicroserviceMetadatasDto {
    private UUID id;
    private String varName;
    private String varValue;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

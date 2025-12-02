package com.example.manager.dtos;

import java.io.Serializable;
import java.util.UUID;

public class CollectorMetadataResponseDto implements Serializable {
    private UUID id;
    private String keyName;
    private String keyValue;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
}

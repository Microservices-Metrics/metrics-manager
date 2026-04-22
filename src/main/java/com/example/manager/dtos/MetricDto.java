package com.example.manager.dtos;

import java.util.UUID;

public class MetricDto {
    private UUID idMetric;
    private String name;
    private String description;
    private String type;
    private String unit;

    public UUID getIdMetric() {
        return idMetric;
    }

    public void setIdMetric(UUID idMetric) {
        this.idMetric = idMetric;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

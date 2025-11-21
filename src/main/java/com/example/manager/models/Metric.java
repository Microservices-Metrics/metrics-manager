package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TB_METRICS")
public class Metric implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idMetric;
    private String name;
    private String description;
    private String type;
    private String unit;
    
    @OneToMany(mappedBy = "metric", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("metric-collectors")
    private List<Collector> collectors = new ArrayList<>();
    
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

    public List<Collector> getCollectors() {
        return collectors;
    }

    public void setCollectors(List<Collector> collectors) {
        this.collectors = collectors;
    }
}

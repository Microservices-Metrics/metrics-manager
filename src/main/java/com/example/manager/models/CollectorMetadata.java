package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_COLLECTOR_METADATA")
public class CollectorMetadata implements Serializable {
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_collector", nullable = false)
    @JsonBackReference("collector-metadata")
    private Collector collector;

    private String url;
    private String requestSchema;
    private String pathToMetric;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Collector getCollector() {
        return collector;
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestSchema() {
        return requestSchema;
    }

    public void setRequestSchema(String requestSchema) {
        this.requestSchema = requestSchema;
    }

    public String getPathToMetric() {
        return pathToMetric;
    }

    public void setPathToMetric(String pathToMetric) {
        this.pathToMetric = pathToMetric;
    }
}

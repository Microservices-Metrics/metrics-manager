package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "TB_COLLECTORS")
public class Collector implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    private String name;
    private String description;
    private String collectionMethod;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_metric", nullable = false)
    @JsonBackReference("metric-collectors")
    private Metric metric;

    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("collector-response-schemas")
    private List<CollectorResponseSchema> responseSchemas = new ArrayList<>();

    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("collector-metadata")
    private List<CollectorMetadata> metadata = new ArrayList<>();

    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("collector-configs")
    private List<CollectorConfig> configs = new ArrayList<>();

    
    // @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = true)
    // @JsonManagedReference("collector-collections")
    // private List<Collection> collections = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(String collectionMethod) {
        this.collectionMethod = collectionMethod;
    }

    public UUID getMetricId() {
        return metric != null ? metric.getIdMetric() : null;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public List<CollectorResponseSchema> getResponseSchemas() {
        return responseSchemas;
    }

    public void setResponseSchemas(List<CollectorResponseSchema> responseSchemas) {
        this.responseSchemas = responseSchemas;
    }

    public List<CollectorMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<CollectorMetadata> metadata) {
        this.metadata = metadata;
    }

    public List<CollectorConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<CollectorConfig> configs) {
        this.configs = configs;
    }
}

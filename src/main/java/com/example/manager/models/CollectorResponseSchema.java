package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "TB_COLLECTOR_RESPONSE_SCHEMAS")
public class CollectorResponseSchema implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_collector", nullable = false)
    @JsonBackReference("collector-response-schemas")
    private Collector collector;
    
    private String schema;
    private int statusType;
    private String description;
    
    public Collector getCollector() {
        return collector;
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public int getStatusType() {
        return statusType;
    }
    
    public void setStatusType(int statusType) {
        this.statusType = statusType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

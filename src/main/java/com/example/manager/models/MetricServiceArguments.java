package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_METRIC_SERVICE_ARGUMENTS")
public class MetricServiceArguments implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idArgument;
    private String argumentName;
    private String argumentValue;
    private String type;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "id_metric_service", nullable = false)
    @JsonBackReference(value = "service-arguments")
    private MetricService metricService;
    
    public MetricService getMetricService() {
        return metricService;
    }
    
    public void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    public UUID getIdArgument() {
        return idArgument;
    }
    
    public String getArgumentName() {
        return argumentName;
    }
    
    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
    }
    
    public String getArgumentValue() {
        return argumentValue;
    }

    public void setArgumentValue(String argumentValue) {
        this.argumentValue = argumentValue;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
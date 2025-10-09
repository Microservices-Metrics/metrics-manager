package com.example.manager.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TB_METRIC_SERVICE")
public class MetricService implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idService;
    private String name;
    private String type;
    private String url;
    @Embedded
    private MetricServiceSettings collectionSettings;
    private String metricFormat;
    private String metricDescription;
    private String outputExample;
    
    @OneToMany(mappedBy = "metricService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetricServiceArguments> arguments = new ArrayList<>();
    
    @OneToMany(mappedBy = "metricService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetricServiceExecution> executions = new ArrayList<>();
    
    public List<MetricServiceArguments> getArguments() {
        return arguments;
    }
    
    public void setArguments(List<MetricServiceArguments> arguments) {
        this.arguments = arguments;
    }
    
    public List<MetricServiceExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<MetricServiceExecution> executions) {
        this.executions = executions;
    }
    
    public UUID getIdService() {
        return idService;
    }
    
    public void setIdService(UUID idService) {
        this.idService = idService;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public MetricServiceSettings getCollectionSettings() {
        return collectionSettings;
    }

    public void setCollectionSettings(MetricServiceSettings collectionSettings) {
        this.collectionSettings = collectionSettings;
    }
    
    public String getMetricFormat() {
        return metricFormat;
    }

    public void setMetricFormat(String metricFormat) {
        this.metricFormat = metricFormat;
    }

    public String getMetricDescription() {
        return metricDescription;
    }

    public void setMetricDescription(String metricDescription) {
        this.metricDescription = metricDescription;
    }

    public String getOutputExample() {
        return outputExample;
    }

    public void setOutputExample(String outputExample) {
        this.outputExample = outputExample;
    }
}

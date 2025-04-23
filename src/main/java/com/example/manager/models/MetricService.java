package com.example.manager.models;

import jakarta.persistence.*;

import java.io.Serializable;
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
    private String metricFormat;
    private String metricDescription;
    private String outputExample;

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

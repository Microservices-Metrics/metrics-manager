package com.example.manager.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class MetricServiceDto {
    @NotBlank 
    private String name;
    @NotBlank 
    private String type;
    @NotBlank 
    private String url;
    private MetricServiceSettingsDto collectionSettings;
    @NotBlank 
    private String metricFormat;
    @NotBlank 
    private String metricDescription;
    private String outputExample;
    private List<MetricServiceArgumentsDto> arguments;

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

    public MetricServiceSettingsDto getCollectionSettings() {
        return collectionSettings;
    }

    public void setCollectionSettings(MetricServiceSettingsDto collectionSettings) {
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

    public List<MetricServiceArgumentsDto> getArguments() {
        return arguments;
    }

    public void setArguments(List<MetricServiceArgumentsDto> arguments) {
        this.arguments = arguments;
    }
}

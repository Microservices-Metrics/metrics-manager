package com.example.manager.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record MetricServiceDto(
        @NotBlank String name, 
        @NotBlank String type, 
        @NotBlank String url,
        @NotBlank MetricServiceSettingsDto collectionSettings,
        @NotBlank String metricFormat, 
        @NotBlank String metricDescription, 
        String outputExample,
        List<MetricServiceArgumentsDto> arguments
) { }

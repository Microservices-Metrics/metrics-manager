package com.example.manager.dtos;

import jakarta.validation.constraints.NotBlank;

public record MetricServiceDto(@NotBlank String name, @NotBlank String type, @NotBlank String url,
        @NotBlank String metricFormat, @NotBlank String metricDescription, String outputExample) {
}

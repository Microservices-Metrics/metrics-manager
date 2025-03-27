package com.example.manager.dtos;

import jakarta.validation.constraints.NotBlank;

public record MetricServiceDto(@NotBlank String name, @NotBlank String url) {
}

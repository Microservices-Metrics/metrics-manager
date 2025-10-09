package com.example.manager.dtos;

import jakarta.validation.constraints.NotBlank;

public record MetricServiceArgumentsDto(
    @NotBlank String argumentName, 
    @NotBlank String type 
) { }

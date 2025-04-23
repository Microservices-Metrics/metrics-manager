package com.example.manager.dtos;

import jakarta.validation.constraints.NotBlank;

public record CollectorTypesDto(@NotBlank String type) {
}

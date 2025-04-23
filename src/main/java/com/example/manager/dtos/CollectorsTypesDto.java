package com.example.manager.dtos;

import jakarta.validation.constraints.NotBlank;

public record CollectorsTypesDto(@NotBlank String type) {
}

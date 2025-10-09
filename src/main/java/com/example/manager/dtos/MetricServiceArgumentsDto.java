package com.example.manager.dtos;

import jakarta.validation.constraints.NotBlank;

public class MetricServiceArgumentsDto {
    @NotBlank 
    private String argumentName;
    @NotBlank 
    private String type;
    @NotBlank 
    private String description;

    public String getArgumentName() {
        return argumentName;
    }

    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
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

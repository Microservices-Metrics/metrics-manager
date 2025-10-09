package com.example.manager.dtos;

import java.sql.Date;

public record MetricServiceSettingsDto(
        String cronExpression,
        Date startDate,
        Date endDate
) { }


package com.example.manager.models;

import java.time.LocalDate;
import jakarta.persistence.Embeddable;


// TODO: deletar
@Embeddable
public class MetricServiceSettings {
    private String cronExpression;
    private LocalDate startDate;
    private LocalDate endDate;

    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}


package com.example.manager.models;

import java.sql.Date;
import jakarta.persistence.Embeddable;

@Embeddable
public class MetricServiceSettings {
    private String cronExpression;
    private Date startDate;
    private Date endDate;

    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}

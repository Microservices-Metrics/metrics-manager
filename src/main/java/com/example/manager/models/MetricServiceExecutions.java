package com.example.manager.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

// TODO: deletar
@Entity
@Table(name = "TB_METRIC_SERVICE_EXECUTIONS")
public class MetricServiceExecutions implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idExecution;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metric_service", nullable = false)
    @JsonBackReference("service-executions")
    private MetricService metricService;

    // Data/hora prevista para iniciar a execução agendada
    private LocalDateTime startDateTime;

    // Data/hora real de fim (será preenchida após execução do Runner)
    private LocalDateTime endDateTime;

    private String requestUrl;

    private String requestBody;

    private Integer responseStatus;

    private String responseBody;

    public UUID getIdExecution() {
        return idExecution;
    }

    public MetricService getMetricService() {
        return metricService;
    }

    public void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}

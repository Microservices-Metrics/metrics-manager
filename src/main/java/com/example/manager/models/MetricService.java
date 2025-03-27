package com.example.manager.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "TB_METRIC_SERVICE")
public class MetricService implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idService;
    private String name;
    private String url;

    // TODO: serializar objetos dinâmicos para os parâmetros

    public UUID getIdService() {
        return idService;
    }

    public void setIdService(UUID idService) {
        this.idService = idService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

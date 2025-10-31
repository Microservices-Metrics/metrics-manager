package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "TB_COLLECTION")
public class Collection implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_collector", nullable = false)
    @JsonBackReference("collector-collections")
    private Collector collector;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_microservice", nullable = false)
    @JsonBackReference("microservice-collections")
    private Microservice microservice;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Collector getCollector() {
        return collector;
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }

    public Microservice getMicroservice() {
        return microservice;
    }

    public void setMicroservice(Microservice microservice) {
        this.microservice = microservice;
    }
}

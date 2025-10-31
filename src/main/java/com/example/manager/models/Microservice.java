package com.example.manager.models;

import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "TB_MICROSERVICE")
public class Microservice implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "microservice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("microservice-collections")
    private List<Collection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "microservice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("microservice-metadatas")
    private List<MicroserviceMetadatas> metadatas = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MicroserviceMetadatas> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(List<MicroserviceMetadatas> metadatas) {
        this.metadatas = metadatas;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }
}

package com.example.manager.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.manager.models.Collection;

@Repository
public interface ICollectionRepository extends JpaRepository<Collection, UUID> {
}

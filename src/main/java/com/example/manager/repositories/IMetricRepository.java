package com.example.manager.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.manager.models.Metric;

@Repository
public interface IMetricRepository extends JpaRepository<Metric, UUID> {
}

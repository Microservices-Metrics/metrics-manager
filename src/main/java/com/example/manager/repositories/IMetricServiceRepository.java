package com.example.manager.repositories;

import com.example.manager.models.MetricService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IMetricServiceRepository extends JpaRepository<MetricService, UUID> {
}

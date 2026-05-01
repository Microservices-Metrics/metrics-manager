package com.example.manager.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.manager.models.Measurement;

@Repository
public interface IMeasurementRepository extends JpaRepository<Measurement, UUID> {
    Optional<Measurement> findTopByCollectorConfigIdAndResponseStatusNotOrderByStartTimestampDesc(UUID collectorConfigId, String responseStatus);

    List<Measurement> findByCollectorConfigId(UUID collectorConfigId);

    List<Measurement> findByCollectorConfigCollectorId(UUID collectorId);

    void deleteByCollectorConfigId(UUID collectorConfigId);

    void deleteByCollectorConfigCollectorId(UUID collectorId);
}

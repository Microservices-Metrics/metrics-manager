package com.example.manager.repositories;

import com.example.manager.models.MetricServiceExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IMetricServiceExecutionRepository extends JpaRepository<MetricServiceExecution, UUID> {
    List<MetricServiceExecution> findByMetricService_IdService(UUID idService);
    // Busca execuções vencidas ainda não processadas (sem responseStatus) limitando manualmente depois
    List<MetricServiceExecution> findTop50ByResponseStatusIsNullAndStartDateTimeBeforeOrderByStartDateTimeAsc(LocalDateTime now);
}

package com.example.manager.repositories;

import com.example.manager.models.MetricServiceExecutions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IMetricServiceExecutionRepository extends JpaRepository<MetricServiceExecutions, UUID> {
    List<MetricServiceExecutions> findByMetricService_IdService(UUID idService);
    // Busca execuções vencidas ainda não processadas (sem responseStatus) limitando manualmente depois
    List<MetricServiceExecutions> findTop50ByResponseStatusIsNullAndStartDateTimeBeforeOrderByStartDateTimeAsc(LocalDateTime now);
}

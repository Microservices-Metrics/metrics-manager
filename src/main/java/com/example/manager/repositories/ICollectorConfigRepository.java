package com.example.manager.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.manager.models.CollectorConfig;

@Repository
public interface ICollectorConfigRepository extends JpaRepository<CollectorConfig, UUID> {
    
    /**
     * Busca todas as configurações ativas no momento especificado.
     * Uma configuração é considerada ativa se:
     * - startDateTime <= now AND endDateTime >= now (quando há data de fim)
     * - startDateTime <= now AND endDateTime é null (quando não há data de fim)
     */
    @Query("SELECT c FROM CollectorConfig c WHERE c.startDateTime IS NOT NULL " +
           "AND c.startDateTime <= :now " +
           "AND (c.endDateTime IS NULL OR c.endDateTime >= :now)")
    List<CollectorConfig> findActiveConfigs(@Param("now") LocalDateTime now);
}

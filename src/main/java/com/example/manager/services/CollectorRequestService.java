package com.example.manager.services;

import com.example.manager.interfaces.IRequestBodyBuilder;
import com.example.manager.interfaces.IRestService;
import com.example.manager.models.Collector;
import com.example.manager.models.CollectorConfig;
import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.Measurement;
import com.example.manager.models.Microservice;
import com.example.manager.models.MicroserviceMetadata;
import com.example.manager.repositories.IMeasurementRepository;
import com.example.manager.util.RequestBodyBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por executar requisições aos coletores
 * usando os metadados configurados.
 */
@Service
public class CollectorRequestService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private IRestService restService;

    @Autowired
    private IRequestBodyBuilder requestBodyBuilder;

    @Autowired
    private IMeasurementRepository measurementRepository;

    /**
     * Executa uma coleta de métrica para uma configuração específica.
     *
     * @param collectorConfig Configuração do coletor vinculada ao microserviço
     * @return Resposta da requisição HTTP
     * @throws Exception se houver erro na execução
     */
    public ResponseEntity<String> executeCollection(CollectorConfig collectorConfig) throws Exception {
        Collector collector = collectorConfig.getCollector();
        Microservice microservice = collectorConfig.getMicroservice();

        List<CollectorMetadata> collectorMetadataList = collector.getMetadata();

        if (collectorMetadataList.isEmpty()) {
            throw new IllegalStateException("Collector has no metadata configured");
        }

        List<MicroserviceMetadata> microserviceMetadata = new ArrayList<>(microservice.getMetadatas());

        // Injeta startDateTime e endDateTime dinâmicos apenas se o coletor usa os placeholders
        if (collectorUsesDynamicDates(collectorMetadataList)) {
            LocalDateTime endDateTime = LocalDateTime.now();

            LocalDateTime startDateTime = measurementRepository
                    .findTopByCollectorConfigIdAndResponseStatusNotOrderByStartTimestampDesc(
                            collectorConfig.getId(), "ERROR")
                    .map(Measurement::getStartTimestamp)
                    .orElseGet(collectorConfig::getStartDateTime);

            microserviceMetadata.add(buildTransientMetadata(microservice, "startDateTime", startDateTime.format(ISO_FORMATTER)));
            microserviceMetadata.add(buildTransientMetadata(microservice, "endDateTime", endDateTime.format(ISO_FORMATTER)));
        }

        RequestBodyBuilder.RequestData requestData = requestBodyBuilder.buildRequestData(
                collectorMetadataList,
                microserviceMetadata
        );

        return restService.executeHttpRequest(
                requestData.getUrl(),
                requestData.getBody(),
                requestData.getHttpMethod()
        );
    }

    private boolean collectorUsesDynamicDates(List<CollectorMetadata> collectorMetadataList) {
        for (CollectorMetadata cm : collectorMetadataList) {
            if (cm.getKeyValue() == null) continue;
            if ("url".equals(cm.getKeyName())) {
                // Na URL os placeholders são {startDateTime} e {endDateTime}
                if (cm.getKeyValue().contains("{startDateTime}") || cm.getKeyValue().contains("{endDateTime}")) {
                    return true;
                }
            } else if ("requestSchema".equals(cm.getKeyName())) {
                // No requestSchema as datas são nomes de propriedade: "startDateTime" ou "endDateTime"
                if (cm.getKeyValue().contains("\"startDateTime\"") || cm.getKeyValue().contains("\"endDateTime\"")) {
                    return true;
                }
            }
        }
        return false;
    }

    private MicroserviceMetadata buildTransientMetadata(Microservice microservice, String varName, String varValue) {
        MicroserviceMetadata meta = new MicroserviceMetadata();
        meta.setMicroservice(microservice);
        meta.setVarName(varName);
        meta.setVarValue(varValue);
        return meta;
    }
}

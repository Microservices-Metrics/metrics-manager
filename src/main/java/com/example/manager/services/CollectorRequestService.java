package com.example.manager.services;

import com.example.manager.interfaces.IRequestBodyBuilder;
import com.example.manager.interfaces.IRestService;
import com.example.manager.models.Collector;
import com.example.manager.models.CollectorConfig;
import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.Microservice;
import com.example.manager.models.MicroserviceMetadata;
import com.example.manager.util.RequestBodyBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável por executar requisições aos coletores
 * usando os metadados configurados.
 */
@Service
public class CollectorRequestService {
    
    @Autowired
    private IRestService restService;
    
    @Autowired
    private IRequestBodyBuilder requestBodyBuilder;
    
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
        
        // Obtém os metadados do coletor (pode haver múltiplos)
        // Aqui assumimos o primeiro, mas você pode ajustar conforme sua lógica
        List<CollectorMetadata> collectorMetadataList = collector.getMetadata();
        
        if (collectorMetadataList.isEmpty()) {
            throw new IllegalStateException("Collector has no metadata configured");
        }
                
        List<MicroserviceMetadata> microserviceMetadata = microservice.getMetadatas();
        
        // Constrói o corpo da requisição usando injeção de dependência
        RequestBodyBuilder.RequestData requestData = requestBodyBuilder.buildRequestData(
            collectorMetadataList, 
            microserviceMetadata
        );
        
        // Executa a requisição
        return restService.executeHttpRequest(
            requestData.getUrl(),
            requestData.getBody(),
            requestData.getHttpMethod()
        );
    }
}

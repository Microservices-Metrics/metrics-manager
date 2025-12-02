package com.example.manager.services;

import com.example.manager.models.Collector;
import com.example.manager.models.CollectorConfig;
import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.Microservice;
import com.example.manager.models.MicroserviceMetadata;
import com.example.manager.util.RequestBodyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Serviço responsável por executar requisições aos coletores
 * usando os metadados configurados.
 */
@Service
public class CollectorRequestService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Executa uma coleta de métrica para uma configuração específica.
     * 
     * @param collectorConfig Configuração do coletor vinculada ao microserviço
     * @return Resposta da requisição HTTP
     * @throws Exception se houver erro na execução
     */
    public String executeCollection(CollectorConfig collectorConfig) throws Exception {
        Collector collector = collectorConfig.getCollector();
        Microservice microservice = collectorConfig.getMicroservice();
        
        // Obtém os metadados do coletor (pode haver múltiplos)
        // Aqui assumimos o primeiro, mas você pode ajustar conforme sua lógica
        List<CollectorMetadata> collectorMetadataList = collector.getMetadata();
        if (collectorMetadataList.isEmpty()) {
            throw new IllegalStateException("Collector has no metadata configured");
        }
        
        CollectorMetadata collectorMetadata = collectorMetadataList.get(0);
        
        // Obtém os metadados do microserviço
        List<MicroserviceMetadata> microserviceMetadata = microservice.getMetadatas();
        
        // Constrói a requisição
        // TODO: deve ser injetado para extender a chamada
        // TODO: refatorar pensando em separação de responsabilidade
        RequestBodyBuilder builder = new RequestBodyBuilder(objectMapper);
        RequestBodyBuilder.RequestData requestData = builder.buildRequestData(
            collectorMetadata, 
            microserviceMetadata
        );
        
        // Executa a requisição
        return executeHttpRequest(
            requestData.getUrl(),
            requestData.getBody(),
            collector.getCollectionMethod()
        );
    }
    
    /**
     * Executa uma requisição HTTP.
     * 
     * @param url URL da requisição
     * @param body Corpo da requisição (JSON)
     * @param method Método HTTP (GET, POST, etc.)
     * @return Resposta da requisição
     */
    private String executeHttpRequest(String url, String body, String method) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            httpMethod,
            entity,
            String.class
        );
        
        return response.getBody();
    }
    
    /**
     * Testa a conexão com um coletor antes de salvar a configuração.
     * 
     * @param collectorMetadata Metadados do coletor
     * @param microserviceMetadata Metadados do microserviço
     * @param collectionMethod Método HTTP
     * @return true se a conexão foi bem-sucedida
     */
    public boolean testConnection(
            CollectorMetadata collectorMetadata,
            List<MicroserviceMetadata> microserviceMetadata,
            String collectionMethod) {
        try {
            RequestBodyBuilder builder = new RequestBodyBuilder(objectMapper);
            RequestBodyBuilder.RequestData requestData = builder.buildRequestData(
                collectorMetadata,
                microserviceMetadata
            );
            
            executeHttpRequest(
                requestData.getUrl(),
                requestData.getBody(),
                collectionMethod
            );
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

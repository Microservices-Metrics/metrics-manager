package com.example.manager.interfaces;

import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.MicroserviceMetadata;
import com.example.manager.util.RequestBodyBuilder;

import java.util.List;

/**
 * Interface para construção de corpos de requisições HTTP
 * baseado em JSON Schema e metadados de microserviços.
 */
public interface IRequestBodyBuilder {
    
    /**
     * Constrói o corpo da requisição combinando o JSON Schema do coletor
     * com os valores dos metadados do microserviço.
     * 
     * @param collectorMetadata Metadados do coletor contendo o requestSchema
     * @param microserviceMetadata Lista de metadados do microserviço com valores
     * @return JSON string com o corpo da requisição construído
     * @throws Exception se houver erro no processamento do JSON
     */
    String buildRequestBody(
            CollectorMetadata collectorMetadata, 
            List<MicroserviceMetadata> microserviceMetadata) throws Exception;
    
    /**
     * Constrói o corpo da requisição com suporte a templates de URL.
     * Substitui placeholders na URL pelos valores dos metadados.
     * 
     * @param collectorMetadata Metadados do coletor
     * @param microserviceMetadata Lista de metadados do microserviço
     * @return Objeto contendo a URL processada e o body da requisição
     * @throws Exception se houver erro no processamento
     */
    RequestBodyBuilder.RequestData buildRequestData(
            CollectorMetadata collectorMetadata,
            List<MicroserviceMetadata> microserviceMetadata) throws Exception;
}

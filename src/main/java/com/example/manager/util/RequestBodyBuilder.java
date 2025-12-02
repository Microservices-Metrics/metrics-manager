package com.example.manager.util;

import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.MicroserviceMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Classe utilitária para construir o corpo de requisições HTTP
 * baseado em JSON Schema e metadados de microserviços.
 */
public class RequestBodyBuilder {
    
    private final ObjectMapper objectMapper;
    
    public RequestBodyBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    
    /**
     * Constrói o corpo da requisição combinando o JSON Schema do coletor
     * com os valores dos metadados do microserviço.
     * 
     * @param collectorMetadata Metadados do coletor contendo o requestSchema
     * @param microserviceMetadata Lista de metadados do microserviço com valores
     * @return JSON string com o corpo da requisição construído
     * @throws Exception se houver erro no processamento do JSON
     */
    public String buildRequestBody(
            CollectorMetadata collectorMetadata, 
            List<MicroserviceMetadata> microserviceMetadata) throws Exception {
        
        String requestSchema = collectorMetadata.getRequestSchema();
        
        if (requestSchema == null || requestSchema.trim().isEmpty()) {
            return "{}";
        }
        
        // Converte os metadados do microserviço em um mapa para fácil acesso
        Map<String, String> metadataMap = new HashMap<>();
        for (MicroserviceMetadata metadata : microserviceMetadata) {
            metadataMap.put(metadata.getVarName(), metadata.getVarValue());
        }
        
        // Parse do JSON Schema
        JsonNode schemaNode = objectMapper.readTree(requestSchema);
        
        // Constrói o objeto JSON baseado no schema
        ObjectNode requestBody = buildFromSchema(schemaNode, metadataMap);
        
        return objectMapper.writeValueAsString(requestBody);
    }
    
    /**
     * Constrói recursivamente o objeto JSON baseado no JSON Schema.
     */
    private ObjectNode buildFromSchema(JsonNode schema, Map<String, String> metadataMap) {
        ObjectNode result = objectMapper.createObjectNode();
        
        // Verifica se é um schema de objeto
        if (!schema.has("type") || !schema.get("type").asText().equals("object")) {
            return result;
        }
        
        // Processa as propriedades do schema
        JsonNode properties = schema.get("properties");
        if (properties != null && properties.isObject()) {
            properties.fields().forEachRemaining(entry -> {
                String propertyName = entry.getKey();
                JsonNode propertySchema = entry.getValue();
                
                // Busca o valor correspondente nos metadados
                String value = metadataMap.get(propertyName);
                
                if (value != null) {
                    // Adiciona o valor de acordo com o tipo definido no schema
                    addValueByType(result, propertyName, value, propertySchema);
                } else if (propertySchema.has("default")) {
                    // Usa valor default se disponível
                    JsonNode defaultValue = propertySchema.get("default");
                    result.set(propertyName, defaultValue);
                } else if (isRequired(schema, propertyName)) {
                    // Se é obrigatório mas não tem valor, adiciona null
                    result.putNull(propertyName);
                }
            });
        }
        
        return result;
    }
    
    /**
     * Adiciona um valor ao objeto JSON de acordo com o tipo definido no schema.
     */
    private void addValueByType(ObjectNode node, String propertyName, String value, JsonNode propertySchema) {
        if (!propertySchema.has("type")) {
            node.put(propertyName, value);
            return;
        }
        
        String type = propertySchema.get("type").asText();
        
        try {
            switch (type.toLowerCase()) {
                case "string":
                    node.put(propertyName, value);
                    break;
                case "integer":
                    node.put(propertyName, Integer.parseInt(value));
                    break;
                case "number":
                    node.put(propertyName, Double.parseDouble(value));
                    break;
                case "boolean":
                    node.put(propertyName, Boolean.parseBoolean(value));
                    break;
                case "null":
                    node.putNull(propertyName);
                    break;
                case "object":
                    // Para objetos complexos, tenta fazer parse do valor como JSON
                    try {
                        JsonNode objectValue = objectMapper.readTree(value);
                        node.set(propertyName, objectValue);
                    } catch (Exception e) {
                        node.put(propertyName, value);
                    }
                    break;
                case "array":
                    // Para arrays, tenta fazer parse do valor como JSON array
                    try {
                        JsonNode arrayValue = objectMapper.readTree(value);
                        if (arrayValue.isArray()) {
                            node.set(propertyName, arrayValue);
                        } else {
                            // Se não é array, cria um array com o valor único
                            ArrayNode arrayNode = objectMapper.createArrayNode();
                            arrayNode.add(value);
                            node.set(propertyName, arrayNode);
                        }
                    } catch (Exception e) {
                        // Se falhar o parse, cria array com string
                        ArrayNode arrayNode = objectMapper.createArrayNode();
                        arrayNode.add(value);
                        node.set(propertyName, arrayNode);
                    }
                    break;
                default:
                    node.put(propertyName, value);
            }
        } catch (NumberFormatException e) {
            // Se falhar a conversão, usa como string
            node.put(propertyName, value);
        }
    }
    
    /**
     * Verifica se uma propriedade é obrigatória no schema.
     */
    private boolean isRequired(JsonNode schema, String propertyName) {
        JsonNode required = schema.get("required");
        if (required != null && required.isArray()) {
            for (JsonNode item : required) {
                if (item.asText().equals(propertyName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Constrói o corpo da requisição com suporte a templates de URL.
     * Substitui placeholders na URL pelos valores dos metadados.
     * 
     * @param collectorMetadata Metadados do coletor
     * @param microserviceMetadata Lista de metadados do microserviço
     * @return Objeto contendo a URL processada e o body da requisição
     */
    public RequestData buildRequestData(
            CollectorMetadata collectorMetadata,
            List<MicroserviceMetadata> microserviceMetadata) throws Exception {
        
        // Converte metadados em mapa
        Map<String, String> metadataMap = new HashMap<>();
        for (MicroserviceMetadata metadata : microserviceMetadata) {
            metadataMap.put(metadata.getVarName(), metadata.getVarValue());
        }
        
        // Processa a URL substituindo placeholders
        String url = collectorMetadata.getUrl();
        if (url != null) {
            for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                url = url.replace(placeholder, entry.getValue());
            }
        }
        
        // Constrói o body
        String body = buildRequestBody(collectorMetadata, microserviceMetadata);
        
        return new RequestData(url, body);
    }
    
    /**
     * Classe para encapsular os dados da requisição.
     */
    public static class RequestData {
        private final String url;
        private final String body;
        
        public RequestData(String url, String body) {
            this.url = url;
            this.body = body;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getBody() {
            return body;
        }
    }
}

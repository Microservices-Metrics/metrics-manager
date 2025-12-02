package com.example.manager.util;

import com.example.manager.models.CollectorMetadata;
import com.example.manager.models.MicroserviceMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestBodyBuilderTest {
    
    private RequestBodyBuilder builder;
    private ObjectMapper objectMapper;
    private CollectorMetadata collectorMetadata;
    private List<MicroserviceMetadata> microserviceMetadata;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        builder = new RequestBodyBuilder(objectMapper);
        collectorMetadata = new CollectorMetadata();
        microserviceMetadata = new ArrayList<>();
    }
    
    @Test
    void testBuildRequestBody_SimpleSchema() throws Exception {
        // JSON Schema simples
        String schema = """
            {
                "type": "object",
                "properties": {
                    "serviceName": {
                        "type": "string"
                    },
                    "port": {
                        "type": "integer"
                    }
                },
                "required": ["serviceName", "port"]
            }
            """;
        
        collectorMetadata.setRequestSchema(schema);
        
        // Metadados do microserviço
        MicroserviceMetadata meta1 = new MicroserviceMetadata();
        meta1.setVarName("serviceName");
        meta1.setVarValue("user-service");
        
        MicroserviceMetadata meta2 = new MicroserviceMetadata();
        meta2.setVarName("port");
        meta2.setVarValue("8080");
        
        microserviceMetadata.add(meta1);
        microserviceMetadata.add(meta2);
        
        // Executa
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        // Verifica
        JsonNode resultNode = objectMapper.readTree(result);
        assertEquals("user-service", resultNode.get("serviceName").asText());
        assertEquals(8080, resultNode.get("port").asInt());
    }
    
    @Test
    void testBuildRequestBody_ComplexSchema() throws Exception {
        // JSON Schema com múltiplos tipos
        String schema = """
            {
                "type": "object",
                "properties": {
                    "serviceName": {
                        "type": "string"
                    },
                    "port": {
                        "type": "integer"
                    },
                    "enabled": {
                        "type": "boolean"
                    },
                    "timeout": {
                        "type": "number"
                    }
                }
            }
            """;
        
        collectorMetadata.setRequestSchema(schema);
        
        // Metadados do microserviço
        addMetadata("serviceName", "order-service");
        addMetadata("port", "9090");
        addMetadata("enabled", "true");
        addMetadata("timeout", "30.5");
        
        // Executa
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        // Verifica
        JsonNode resultNode = objectMapper.readTree(result);
        assertEquals("order-service", resultNode.get("serviceName").asText());
        assertEquals(9090, resultNode.get("port").asInt());
        assertTrue(resultNode.get("enabled").asBoolean());
        assertEquals(30.5, resultNode.get("timeout").asDouble(), 0.001);
    }
    
    @Test
    void testBuildRequestBody_WithDefaults() throws Exception {
        // JSON Schema com valores default
        String schema = """
            {
                "type": "object",
                "properties": {
                    "serviceName": {
                        "type": "string"
                    },
                    "retries": {
                        "type": "integer",
                        "default": 3
                    }
                }
            }
            """;
        
        collectorMetadata.setRequestSchema(schema);
        
        // Apenas serviceName informado, retries deve usar default
        addMetadata("serviceName", "payment-service");
        
        // Executa
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        // Verifica
        JsonNode resultNode = objectMapper.readTree(result);
        assertEquals("payment-service", resultNode.get("serviceName").asText());
        assertEquals(3, resultNode.get("retries").asInt());
    }
    
    @Test
    void testBuildRequestData_WithUrlPlaceholders() throws Exception {
        // Schema simples
        String schema = """
            {
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string"
                    }
                }
            }
            """;
        
        collectorMetadata.setRequestSchema(schema);
        collectorMetadata.setUrl("http://localhost:8080/api/{serviceName}/metrics");
        
        // Metadados
        addMetadata("serviceName", "inventory-service");
        addMetadata("query", "cpu_usage");
        
        // Executa
        RequestBodyBuilder.RequestData requestData = builder.buildRequestData(
            collectorMetadata, 
            microserviceMetadata
        );
        
        // Verifica URL
        assertEquals("http://localhost:8080/api/inventory-service/metrics", requestData.getUrl());
        
        // Verifica Body
        JsonNode bodyNode = objectMapper.readTree(requestData.getBody());
        assertEquals("cpu_usage", bodyNode.get("query").asText());
    }
    
    @Test
    void testBuildRequestBody_EmptySchema() throws Exception {
        collectorMetadata.setRequestSchema("");
        
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        assertEquals("{}", result);
    }
    
    @Test
    void testBuildRequestBody_NullSchema() throws Exception {
        collectorMetadata.setRequestSchema(null);
        
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        assertEquals("{}", result);
    }
    
    @Test
    void testBuildRequestBody_WithArrayType() throws Exception {
        String schema = """
            {
                "type": "object",
                "properties": {
                    "tags": {
                        "type": "array"
                    }
                }
            }
            """;
        
        collectorMetadata.setRequestSchema(schema);
        
        // Array como JSON string
        addMetadata("tags", "[\"production\", \"critical\"]");
        
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        JsonNode resultNode = objectMapper.readTree(result);
        assertTrue(resultNode.get("tags").isArray());
        assertEquals(2, resultNode.get("tags").size());
        assertEquals("production", resultNode.get("tags").get(0).asText());
    }
    
    @Test
    void testBuildRequestBody_WithNestedObject() throws Exception {
        String schema = """
            {
                "type": "object",
                "properties": {
                    "config": {
                        "type": "object"
                    }
                }
            }
            """;
        
        collectorMetadata.setRequestSchema(schema);
        
        // Objeto aninhado como JSON string
        addMetadata("config", "{\"timeout\": 30, \"retries\": 3}");
        
        String result = builder.buildRequestBody(collectorMetadata, microserviceMetadata);
        
        JsonNode resultNode = objectMapper.readTree(result);
        assertTrue(resultNode.get("config").isObject());
        assertEquals(30, resultNode.get("config").get("timeout").asInt());
        assertEquals(3, resultNode.get("config").get("retries").asInt());
    }
    
    // Método auxiliar
    private void addMetadata(String varName, String varValue) {
        MicroserviceMetadata meta = new MicroserviceMetadata();
        meta.setVarName(varName);
        meta.setVarValue(varValue);
        microserviceMetadata.add(meta);
    }
}

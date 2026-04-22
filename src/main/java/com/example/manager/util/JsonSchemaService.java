package com.example.manager.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

@Service
public class JsonSchemaService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private final Map<String, JsonSchema> cache = new ConcurrentHashMap<>();
    private final int MAX_SCHEMA_BYTES = 100 * 1024; // 100 KB

    /**
     * Validates that the provided string is valid JSON and compiles as a JSON Schema.
     * Throws IllegalArgumentException if invalid.
     */
    public void validateSchemaString(String schemaJson) throws IllegalArgumentException {
        if (schemaJson == null) {
            throw new IllegalArgumentException("schema is null");
        }

        int bytes = schemaJson.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_SCHEMA_BYTES) {
            throw new IllegalArgumentException("Schema exceeds maximum allowed size of " + MAX_SCHEMA_BYTES + " bytes");
        }

        try {
            // Use cache to avoid recompilation
            String key = sha256Hex(schemaJson);
            cache.computeIfAbsent(key, k -> {
                try {
                    JsonNode schemaNode = mapper.readTree(schemaJson);
                    return factory.getSchema(schemaNode);
                } catch (Exception e) {
                    // wrap as RuntimeException to be caught below
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            // unwrap caused by computeIfAbsent
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalArgumentException("Schema is not a valid JSON Schema: " + cause.getMessage(), cause);
        } catch (Exception e) {
            throw new IllegalArgumentException("Schema validation error: " + e.getMessage(), e);
        }
    }

    /**
     * Validates an instance JSON against the provided schema JSON.
     * Returns validation messages (empty set = valid).
     */
    public Set<ValidationMessage> validateInstance(String schemaJson, String instanceJson) {
        try {
            String key = sha256Hex(schemaJson);
            JsonSchema schema = cache.get(key);
            if (schema == null) {
                // compile and cache
                JsonNode schemaNode = mapper.readTree(schemaJson);
                schema = factory.getSchema(schemaNode);
                cache.put(key, schema);
            }

            JsonNode instanceNode = mapper.readTree(instanceJson);
            return schema.validate(instanceNode);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Instance or schema is not valid JSON: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error validating instance against schema: " + e.getMessage(), e);
        }
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // fallback to input hashCode
            return Integer.toHexString(input.hashCode());
        }
    }
}

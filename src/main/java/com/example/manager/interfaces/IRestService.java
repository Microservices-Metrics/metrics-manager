package com.example.manager.interfaces;

import org.springframework.http.ResponseEntity;

/**
 * Interface para execução de requisições HTTP REST.
 */
public interface IRestService {
    
    /**
     * Executa uma requisição HTTP.
     * 
     * @param url URL da requisição
     * @param body Corpo da requisição (JSON)
     * @param method Método HTTP (GET, POST, etc.)
     * @return Resposta da requisição
     * @throws Exception se houver erro na execução
     */
    ResponseEntity<String> executeHttpRequest(String url, String body, String method) throws Exception;
}

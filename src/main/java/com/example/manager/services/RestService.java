package com.example.manager.services;

import com.example.manager.interfaces.IRestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Serviço responsável por executar requisições HTTP REST.
 */
@Service
public class RestService implements IRestService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Executa uma requisição HTTP.
     * 
     * @param url URL da requisição
     * @param body Corpo da requisição (JSON)
     * @param method Método HTTP (GET, POST, etc.)
     * @return Resposta da requisição
     * @throws Exception se houver erro na execução
     */
    @Override
    public String executeHttpRequest(String url, String body, String method) throws Exception {
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
}

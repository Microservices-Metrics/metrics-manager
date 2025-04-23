package com.example.manager.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/collectorstypes")
public class CollectorsTypesController {

    @GetMapping()
    public String getAllCollectorsTypes() {
        // TODO: process GET request
        return new String();
    }

    @PostMapping()
    public String postCollectorType(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

}

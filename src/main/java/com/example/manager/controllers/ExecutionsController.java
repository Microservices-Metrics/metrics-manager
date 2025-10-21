package com.example.manager.controllers;

import com.example.manager.dtos.ExecutionRequestDto;
import com.example.manager.models.MetricService;
import com.example.manager.models.MetricServiceExecutions;
import com.example.manager.repositories.IMetricServiceRepository;
import com.example.manager.repositories.IMetricServiceExecutionRepository;
import com.example.manager.services.MetricExecutionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/executions")
public class ExecutionsController {

  @Autowired
  private IMetricServiceRepository metricServiceRepository;

  @Autowired
  private IMetricServiceExecutionRepository executionRepository;

  @Autowired
  private MetricExecutionRunner executionRunner;

  @PostMapping
  public ResponseEntity<?> postExecution(@RequestBody ExecutionRequestDto executionRequestId) {
    MetricService service = null;

    if (executionRequestId.getIdService() != null) {
      Optional<MetricService> opt = metricServiceRepository.findById(executionRequestId.getIdService());
      if (opt.isEmpty()) {
        return ResponseEntity.badRequest().body("MetricService not found: " + executionRequestId.getIdService());
      }
      service = opt.get();
    }

    MetricServiceExecutions execution = new MetricServiceExecutions();
    execution.setMetricService(service);
    // Usa a URL do MetricService salvo
    execution.setRequestUrl(service != null ? service.getUrl() : null);
    execution.setRequestBody(null);
    execution.setStartDateTime(LocalDateTime.now());

    // Persistimos antes para ter um id, opcional
    execution = executionRepository.save(execution);

    // Run execution (this will update response fields and persist again)
    MetricServiceExecutions result = executionRunner.runExecution(execution);

    return ResponseEntity.ok(result);
  }

}

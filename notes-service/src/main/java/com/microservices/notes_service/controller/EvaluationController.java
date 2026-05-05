package com.microservices.notes_service.controller;

import com.microservices.notes_service.dto.CreateEvaluationRequest;
import com.microservices.notes_service.dto.EvaluationResponse;
import com.microservices.notes_service.dto.UpdateEvaluationRequest;
import com.microservices.notes_service.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public List<EvaluationResponse> listAll() {
        return evaluationService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationResponse create(@Valid @RequestBody CreateEvaluationRequest request) {
        return evaluationService.create(request);
    }

    @PutMapping("/{id}")
    public EvaluationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEvaluationRequest request) {
        return evaluationService.update(id, request);
    }

    @GetMapping("/soutenance/{soutenanceId}")
    public List<EvaluationResponse> listBySoutenance(@PathVariable Long soutenanceId) {
        return evaluationService.listBySoutenance(soutenanceId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        evaluationService.delete(id);
    }
}

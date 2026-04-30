package com.microservices.soutenance_service.controller;

import com.microservices.soutenance_service.dto.CreateSoutenanceRequest;
import com.microservices.soutenance_service.dto.SoutenanceDetailsResponse;
import com.microservices.soutenance_service.dto.SoutenanceResponse;
import com.microservices.soutenance_service.dto.UpdateEtatSoutenanceRequest;
import com.microservices.soutenance_service.dto.UpdateSoutenanceRequest;
import com.microservices.soutenance_service.service.SoutenanceOrchestrationService;
import com.microservices.soutenance_service.service.SoutenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soutenances")
public class SoutenanceController {

    private final SoutenanceService soutenanceService;
    private final SoutenanceOrchestrationService soutenanceOrchestrationService;

    public SoutenanceController(
            SoutenanceService soutenanceService,
            SoutenanceOrchestrationService soutenanceOrchestrationService
    ) {
        this.soutenanceService = soutenanceService;
        this.soutenanceOrchestrationService = soutenanceOrchestrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SoutenanceResponse create(@Valid @RequestBody CreateSoutenanceRequest request) {
        return soutenanceService.create(request);
    }

    @PutMapping("/{id}")
    public SoutenanceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSoutenanceRequest request) {
        return soutenanceService.update(id, request);
    }

    @RequestMapping(value = "/{id}/etat", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public SoutenanceResponse updateEtat(@PathVariable Long id, @Valid @RequestBody UpdateEtatSoutenanceRequest request) {
        return soutenanceService.updateEtat(id, request);
    }

    @GetMapping("/{id}")
    public SoutenanceResponse getById(@PathVariable Long id) {
        return soutenanceService.getById(id);
    }

    @GetMapping("/{id}/details")
    public SoutenanceDetailsResponse getDetails(@PathVariable Long id) {
        return soutenanceOrchestrationService.getDetails(id);
    }

    @GetMapping
    public List<SoutenanceResponse> listAll() {
        return soutenanceService.listAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        soutenanceService.delete(id);
    }
}

package com.microservices.notes_service.controller;

import com.microservices.notes_service.dto.AssignEtudiantsToSoutenanceRequest;
import com.microservices.notes_service.service.SoutenanceEtudiantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soutenances")
public class SoutenanceEtudiantController {

    private final SoutenanceEtudiantService soutenanceEtudiantService;

    public SoutenanceEtudiantController(SoutenanceEtudiantService soutenanceEtudiantService) {
        this.soutenanceEtudiantService = soutenanceEtudiantService;
    }

    @PostMapping("/etudiants/assignations")
    public List<Long> assignEtudiants(@Valid @RequestBody AssignEtudiantsToSoutenanceRequest request) {
        return soutenanceEtudiantService.assignEtudiants(request);
    }

    @GetMapping("/{soutenanceId}/etudiants")
    public List<Long> getEtudiantsBySoutenance(@PathVariable Long soutenanceId) {
        return soutenanceEtudiantService.getEtudiantsBySoutenance(soutenanceId);
    }
}

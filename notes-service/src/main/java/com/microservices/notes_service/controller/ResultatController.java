package com.microservices.notes_service.controller;

import com.microservices.notes_service.dto.ResultatSoutenanceResponse;
import com.microservices.notes_service.service.ResultatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resultats")
public class ResultatController {

    private final ResultatService resultatService;

    public ResultatController(ResultatService resultatService) {
        this.resultatService = resultatService;
    }

    @GetMapping
    public List<ResultatSoutenanceResponse> getAll() {
        return resultatService.getAll();
    }

    @GetMapping("/soutenances/{soutenanceId}")
    public ResultatSoutenanceResponse getBySoutenance(@PathVariable Long soutenanceId) {
        return resultatService.getBySoutenanceId(soutenanceId);
    }

    @GetMapping("/etudiants/{etudiantId}")
    public List<ResultatSoutenanceResponse> getByEtudiant(@PathVariable Long etudiantId) {
        return resultatService.getByEtudiantId(etudiantId);
    }
}

package com.microservices.jury_service.controller;

import com.microservices.jury_service.dto.AffectationJuryDTO;
import com.microservices.jury_service.dto.AffecterJuryRequest;
import com.microservices.jury_service.dto.ReponseJuryDTO;
import com.microservices.jury_service.service.AffectationJuryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affectations-jury")
@RequiredArgsConstructor
@Tag(name = "Affectation Jury", description = "Gestion des affectations des jurys aux soutenances")
public class AffectationJuryController {

    private final AffectationJuryService affectationJuryService;

    @GetMapping
    @Operation(summary = "Lister toutes les affectations")
    public ResponseEntity<List<AffectationJuryDTO>> getAllAffectations() {
        return ResponseEntity.ok(affectationJuryService.getAllAffectations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Rechercher une affectation par son ID")
    public ResponseEntity<AffectationJuryDTO> getAffectationById(@PathVariable String id) {
        return ResponseEntity.ok(affectationJuryService.getAffectationById(id));
    }

    @GetMapping("/soutenance/{idSoutenance}")
    @Operation(summary = "Lister les affectations par soutenance")
    public ResponseEntity<List<AffectationJuryDTO>> getAffectationsBySoutenance(@PathVariable Long idSoutenance) {
        return ResponseEntity.ok(affectationJuryService.getAffectationsBySoutenance(idSoutenance));
    }

    @PostMapping
    @Operation(summary = "Affecter un jury à une soutenance")
    public ResponseEntity<AffectationJuryDTO> affecterJury(@Valid @RequestBody AffecterJuryRequest request) {
        return new ResponseEntity<>(affectationJuryService.affecterJury(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une affectation")
    public ResponseEntity<AffectationJuryDTO> updateAffectation(@PathVariable String id, @Valid @RequestBody AffecterJuryRequest request) {
        return ResponseEntity.ok(affectationJuryService.updateAffectation(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une affectation")
    public ResponseEntity<ReponseJuryDTO> deleteAffectation(@PathVariable String id) {
        affectationJuryService.deleteAffectation(id);
        return ResponseEntity.ok(new ReponseJuryDTO("Affectation supprimée avec succès", true));
    }

    @GetMapping("/soutenance/{idSoutenance}/jury-complet")
    @Operation(summary = "Obtenir le jury complet d'une soutenance")
    public ResponseEntity<ReponseJuryDTO> getJuryCompletBySoutenance(@PathVariable Long idSoutenance) {
        return ResponseEntity.ok(affectationJuryService.getJuryCompletBySoutenance(idSoutenance));
    }
}

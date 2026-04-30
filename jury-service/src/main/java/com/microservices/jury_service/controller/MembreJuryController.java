package com.microservices.jury_service.controller;

import com.microservices.jury_service.dto.CreerMembreJuryRequest;
import com.microservices.jury_service.dto.MembreJuryDTO;
import com.microservices.jury_service.dto.ReponseJuryDTO;
import com.microservices.jury_service.service.MembreJuryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membres-jury")
@RequiredArgsConstructor
@Tag(name = "Membre Jury", description = "Gestion des membres du jury")
public class MembreJuryController {

    private final MembreJuryService membreJuryService;

    @GetMapping
    @Operation(summary = "Lister tous les membres du jury")
    public ResponseEntity<List<MembreJuryDTO>> getAllMembres() {
        return ResponseEntity.ok(membreJuryService.getAllMembres());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Rechercher un membre par son ID")
    public ResponseEntity<MembreJuryDTO> getMembreById(@PathVariable String id) {
        return ResponseEntity.ok(membreJuryService.getMembreById(id));
    }

    @GetMapping("/enseignant/{idEnseignant}")
    @Operation(summary = "Rechercher un membre par son idEnseignant")
    public ResponseEntity<MembreJuryDTO> getMembreByIdEnseignant(@PathVariable Long idEnseignant) {
        return ResponseEntity.ok(membreJuryService.getMembreByIdEnseignant(idEnseignant));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau membre du jury")
    public ResponseEntity<MembreJuryDTO> createMembre(@Valid @RequestBody CreerMembreJuryRequest request) {
        return new ResponseEntity<>(membreJuryService.createMembre(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un membre du jury")
    public ResponseEntity<MembreJuryDTO> updateMembre(@PathVariable String id, @Valid @RequestBody CreerMembreJuryRequest request) {
        return ResponseEntity.ok(membreJuryService.updateMembre(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un membre du jury")
    public ResponseEntity<ReponseJuryDTO> deleteMembre(@PathVariable String id) {
        membreJuryService.deleteMembre(id);
        return ResponseEntity.ok(new ReponseJuryDTO("Membre supprimé avec succès", true));
    }
}

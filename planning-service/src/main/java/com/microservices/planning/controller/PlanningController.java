package com.microservices.planning.controller;

import com.microservices.planning.dto.DisponibiliteRequest;
import com.microservices.planning.dto.ReservationRequest;
import com.microservices.planning.entity.*;
import com.microservices.planning.service.PlanningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@Slf4j
public class PlanningController {

    private final PlanningService planningService;

    // ========== ENDPOINTS SALLES ==========

    @PostMapping("/salles")
    public ResponseEntity<Salle> creerSalle(@Valid @RequestBody Salle salle) {
        log.info("POST /api/planning/salles - Création salle: {}", salle.getNumero());
        Salle nouvelleSalle = planningService.createSalle(salle);
        return new ResponseEntity<>(nouvelleSalle, HttpStatus.CREATED);
    }

    @GetMapping("/salles")
    public ResponseEntity<List<Salle>> listerSalles() {
        log.info("GET /api/planning/salles - Liste toutes les salles");
        return ResponseEntity.ok(planningService.getAllSalles());
    }

    @GetMapping("/salles/{id}")
    public ResponseEntity<Salle> voirSalle(@PathVariable String id) {
        log.info("GET /api/planning/salles/{} - Détails salle", id);
        return ResponseEntity.ok(planningService.getSalleById(id));
    }

    @PutMapping("/salles/{id}")
    public ResponseEntity<Salle> modifierSalle(@PathVariable String id, @Valid @RequestBody Salle salle) {
        log.info("PUT /api/planning/salles/{} - Modification salle", id);
        return ResponseEntity.ok(planningService.updateSalle(id, salle));
    }

    @DeleteMapping("/salles/{id}")
    public ResponseEntity<Void> supprimerSalle(@PathVariable String id) {
        log.info("DELETE /api/planning/salles/{} - Suppression salle", id);
        planningService.deleteSalle(id);
        return ResponseEntity.noContent().build();
    }

    // ========== ENDPOINTS CRÉNEAUX ==========

    @PostMapping("/creneaux")
    public ResponseEntity<Creneau> creerCreneau(@Valid @RequestBody Creneau creneau) {
        log.info("POST /api/planning/creneaux - Création créneau: {} {}",
                creneau.getDateSoutenance(), creneau.getHeureDebut());
        Creneau nouveauCreneau = planningService.createCreneau(creneau);
        return new ResponseEntity<>(nouveauCreneau, HttpStatus.CREATED);
    }

    @GetMapping("/creneaux")
    public ResponseEntity<List<Creneau>> listerCreneaux() {
        log.info("GET /api/planning/creneaux - Liste tous les créneaux");
        return ResponseEntity.ok(planningService.getAllCreneaux());
    }

    @GetMapping("/creneaux/{id}")
    public ResponseEntity<Creneau> voirCreneau(@PathVariable String id) {
        log.info("GET /api/planning/creneaux/{} - Détails créneau", id);
        return ResponseEntity.ok(planningService.getCreneauById(id));
    }

    @DeleteMapping("/creneaux/{id}")
    public ResponseEntity<Void> supprimerCreneau(@PathVariable String id) {
        log.info("DELETE /api/planning/creneaux/{} - Suppression créneau", id);
        planningService.deleteCreneau(id);
        return ResponseEntity.noContent().build();
    }

    // ========== ENDPOINTS VÉRIFICATIONS ==========

    @GetMapping("/verifications/conflit-salle")
    public ResponseEntity<Boolean> verifierConflitSalle(
            @RequestParam String salleId, @RequestParam String creneauId) {
        log.info("GET /api/planning/verifications/conflit-salle - salleId={}, creneauId={}", salleId, creneauId);
        return ResponseEntity.ok(planningService.verifierConflitSalle(salleId, creneauId));
    }

    @GetMapping("/verifications/disponibilite-creneau")
    public ResponseEntity<Boolean> verifierDisponibilite(
            @RequestParam String salleId, @RequestParam String creneauId) {
        log.info("GET /api/planning/verifications/disponibilite-creneau - salleId={}, creneauId={}", salleId, creneauId);
        return ResponseEntity.ok(planningService.verifierDisponibilite(salleId, creneauId));
    }

    @PostMapping("/recherche/creneaux-libres")
    public ResponseEntity<List<Creneau>> rechercherCreneauxLibres(
            @Valid @RequestBody DisponibiliteRequest request) {
        log.info("POST /api/planning/recherche/creneaux-libres - {}", request);
        return ResponseEntity.ok(planningService.rechercherCreneauxDisponibles(request));
    }

    // ========== ENDPOINTS RÉSERVATIONS ==========

    @PostMapping("/reservations")
    public ResponseEntity<OccupationSalle> reserver(@Valid @RequestBody ReservationRequest request) {
        log.info("POST /api/planning/reservations - Réservation salle {} créneau {}",
                request.getSalleId(), request.getCreneauId());
        OccupationSalle reservation = planningService.reserverSalle(request);
        return new ResponseEntity<>(reservation, HttpStatus.CREATED);
    }

    @GetMapping("/reservations/salle/{salleId}")
    public ResponseEntity<List<OccupationSalle>> getReservationsBySalle(@PathVariable String salleId) {
        log.info("GET /api/planning/reservations/salle/{} - Liste réservations", salleId);
        return ResponseEntity.ok(planningService.getReservationsBySalle(salleId));
    }
    @GetMapping("/reservations")
    public ResponseEntity<List<OccupationSalle>> listerToutesLesReservations() {
        log.info("GET /api/planning/reservations - Liste globale des réservations");
        return ResponseEntity.ok(planningService.getAllReservations());
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> annulerReservation(@PathVariable String id) {
        log.info("DELETE /api/planning/reservations/{} - Annulation réservation", id);
        planningService.annulerReservation(id);
        return ResponseEntity.noContent().build();
    }
}
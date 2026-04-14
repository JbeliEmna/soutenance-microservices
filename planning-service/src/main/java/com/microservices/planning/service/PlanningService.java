package com.soutenance.planning.service;

import com.soutenance.planning.dto.DisponibiliteRequest;
import com.soutenance.planning.dto.ReservationRequest;
import com.soutenance.planning.entity.*;
import com.soutenance.planning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningService {

    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;
    private final OccupationSalleRepository occupationRepository;

    // ========== GESTION DES SALLES ==========

    public List<Salle> getAllSalles() {
        log.debug("Récupération de toutes les salles");
        return salleRepository.findAll();
    }

    public Salle getSalleById(String id) {
        log.debug("Récupération de la salle avec id: {}", id);
        return salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'id: " + id));
    }

    public Salle createSalle(Salle salle) {
        log.info("Création d'une nouvelle salle: {}", salle.getNumero());
        if (salleRepository.existsByNumero(salle.getNumero())) {
            throw new RuntimeException("Une salle avec le numéro " + salle.getNumero() + " existe déjà");
        }
        salle.setCreatedAt(LocalDateTime.now());
        salle.setUpdatedAt(LocalDateTime.now());
        return salleRepository.save(salle);
    }

    public Salle updateSalle(String id, Salle salleDetails) {
        log.info("Mise à jour de la salle id: {}", id);
        Salle salle = getSalleById(id);
        salle.setNumero(salleDetails.getNumero());
        salle.setCapacite(salleDetails.getCapacite());
        salle.setUpdatedAt(LocalDateTime.now());
        return salleRepository.save(salle);
    }

    public void deleteSalle(String id) {
        log.info("Suppression de la salle id: {}", id);
        Salle salle = getSalleById(id);

        // Vérifier si la salle a des réservations
        List<OccupationSalle> reservations = occupationRepository.findBySalleId(id);
        if (!reservations.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une salle qui a des réservations");
        }
        salleRepository.delete(salle);
    }

    // ========== GESTION DES CRÉNEAUX ==========

    public List<Creneau> getAllCreneaux() {
        log.debug("Récupération de tous les créneaux");
        return creneauRepository.findAll();
    }

    public Creneau getCreneauById(String id) {
        log.debug("Récupération du créneau avec id: {}", id);
        return creneauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé avec l'id: " + id));
    }

    public Creneau createCreneau(Creneau creneau) {
        log.info("Création d'un nouveau créneau: {} {}", creneau.getDateSoutenance(), creneau.getHeureDebut());

        if (creneauRepository.existsByDateSoutenanceAndHeureDebut(
                creneau.getDateSoutenance(), creneau.getHeureDebut())) {
            throw new RuntimeException("Un créneau existe déjà à cette date et heure");
        }

        // Calculer la durée en minutes
        long minutes = Duration.between(creneau.getHeureDebut(), creneau.getHeureFin()).toMinutes();
        creneau.setDureeMinutes((int) minutes);
        creneau.setCreatedAt(LocalDateTime.now());

        return creneauRepository.save(creneau);
    }

    public void deleteCreneau(String id) {
        log.info("Suppression du créneau id: {}", id);
        Creneau creneau = getCreneauById(id);

        // Vérifier si le créneau a des réservations
        List<OccupationSalle> reservations = occupationRepository.findByCreneauId(id);
        if (!reservations.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un créneau qui a des réservations");
        }
        creneauRepository.delete(creneau);
    }

    // ========== VÉRIFICATION DES CONFLITS ==========

    public boolean verifierConflitSalle(String salleId, String creneauId) {
        log.debug("Vérification conflit salle {} créneau {}", salleId, creneauId);
        return occupationRepository.existsBySalleIdAndCreneauId(salleId, creneauId);
    }

    public boolean verifierDisponibilite(String salleId, String creneauId) {
        log.debug("Vérification disponibilité salle {} créneau {}", salleId, creneauId);

        if (verifierConflitSalle(salleId, creneauId)) {
            log.warn("Conflit détecté: salle {} déjà réservée sur créneau {}", salleId, creneauId);
            return false;
        }
        return true;
    }

    // ========== RECHERCHE DE CRÉNEAUX DISPONIBLES ==========

    public List<Creneau> rechercherCreneauxDisponibles(DisponibiliteRequest request) {
        log.debug("Recherche créneaux disponibles pour salle {} du {} au {}",
                request.getSalleId(), request.getDateDebut(), request.getDateFin());

        List<Creneau> tousLesCreneaux = creneauRepository.findCreneauxEntreDates(
                request.getDateDebut(), request.getDateFin());

        // Récupérer les IDs des créneaux occupés
        List<OccupationSalle> occupations = occupationRepository.findCreneauxOccupesPourSalle(
                request.getSalleId(), request.getDateDebut(), request.getDateFin());

        List<String> creneauxOccupesIds = occupations.stream()
                .map(OccupationSalle::getCreneauId)
                .collect(Collectors.toList());

        return tousLesCreneaux.stream()
                .filter(creneau -> !creneauxOccupesIds.contains(creneau.getId()))
                .collect(Collectors.toList());
    }

    // ========== GESTION DES RÉSERVATIONS ==========

    public OccupationSalle reserverSalle(ReservationRequest request) {
        log.info("Réservation salle {} sur créneau {}", request.getSalleId(), request.getCreneauId());

        if (!verifierDisponibilite(request.getSalleId(), request.getCreneauId())) {
            throw new RuntimeException("Créneau non disponible pour cette salle");
        }

        // Vérifier que la salle et le créneau existent
        getSalleById(request.getSalleId());
        getCreneauById(request.getCreneauId());

        OccupationSalle occupation = OccupationSalle.builder()
                .salleId(request.getSalleId())
                .creneauId(request.getCreneauId())
                .statut(request.getStatut() != null ? request.getStatut() : "RESERVEE")
                .referenceSoutenanceId(request.getReferenceSoutenanceId())
                .reserveePar(request.getReserveePar())
                .reservedAt(LocalDateTime.now())
                .build();

        return occupationRepository.save(occupation);
    }

    public List<OccupationSalle> getReservationsBySalle(String salleId) {
        log.debug("Récupération des réservations pour salle {}", salleId);
        return occupationRepository.findBySalleId(salleId);
    }

    public void annulerReservation(String reservationId) {
        log.info("Annulation réservation id: {}", reservationId);
        OccupationSalle occupation = occupationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        occupation.setStatut("ANNULEE");
        occupation.setCancelledAt(LocalDateTime.now());
        occupationRepository.save(occupation);
    }
}
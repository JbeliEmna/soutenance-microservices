package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.dto.CreateSoutenanceRequest;
import com.microservices.soutenance_service.dto.SoutenanceResponse;
import com.microservices.soutenance_service.dto.UpdateEtatSoutenanceRequest;
import com.microservices.soutenance_service.dto.UpdateSoutenanceRequest;
import com.microservices.soutenance_service.enums.EtatSoutenance;
import com.microservices.soutenance_service.exception.BusinessException;
import com.microservices.soutenance_service.model.Soutenance;
import com.microservices.soutenance_service.repository.SoutenanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SoutenanceService {

    private static final String SOUTENANCE_SEQUENCE = "soutenance_sequence";

    private final SoutenanceRepository soutenanceRepository;
    private final ReferenceDataService referenceDataService;
    private final SalleService salleService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public SoutenanceService(
            SoutenanceRepository soutenanceRepository,
            ReferenceDataService referenceDataService,
            SalleService salleService,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.soutenanceRepository = soutenanceRepository;
        this.referenceDataService = referenceDataService;
        this.salleService = salleService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public SoutenanceResponse create(CreateSoutenanceRequest request) {
        String normalizedSalle = normalizeSalle(request.salle());

        validatePlanningInput(
                null,
                request.etudiantId(),
                request.encadrantId(),
                normalizedSalle,
                request.dateDebut(),
                request.dateFin()
        );

        Soutenance soutenance = new Soutenance();
        soutenance.setId(sequenceGeneratorService.generateSequence(SOUTENANCE_SEQUENCE));
        soutenance.setEtudiantId(request.etudiantId());
        soutenance.setEncadrantId(request.encadrantId());
        soutenance.setSalle(normalizedSalle);
        soutenance.setDateDebut(request.dateDebut());
        soutenance.setDateFin(request.dateFin());
        soutenance.setEtat(EtatSoutenance.PLANIFIEE);
        touchOnCreate(soutenance);

        Soutenance saved = soutenanceRepository.save(soutenance);
        return SoutenanceResponse.fromEntity(saved);
    }

    public SoutenanceResponse update(Long id, UpdateSoutenanceRequest request) {
        Soutenance soutenance = getEntityOrThrow(id);
        String normalizedSalle = normalizeSalle(request.salle());

        validatePlanningInput(
                id,
                request.etudiantId(),
                request.encadrantId(),
                normalizedSalle,
                request.dateDebut(),
                request.dateFin()
        );

        soutenance.setEtudiantId(request.etudiantId());
        soutenance.setEncadrantId(request.encadrantId());
        soutenance.setSalle(normalizedSalle);
        soutenance.setDateDebut(request.dateDebut());
        soutenance.setDateFin(request.dateFin());
        touchOnUpdate(soutenance);

        Soutenance saved = soutenanceRepository.save(soutenance);
        return SoutenanceResponse.fromEntity(saved);
    }

    public SoutenanceResponse getById(Long id) {
        return SoutenanceResponse.fromEntity(getEntityOrThrow(id));
    }

    public SoutenanceResponse updateEtat(Long id, UpdateEtatSoutenanceRequest request) {
        Soutenance soutenance = getEntityOrThrow(id);

        EtatSoutenance etatActuel = soutenance.getEtat() == null
                ? EtatSoutenance.PLANIFIEE
                : soutenance.getEtat();
        EtatSoutenance nouvelEtat = request.etat();

        if (!isEtatTransitionAllowed(etatActuel, nouvelEtat)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Transition d'etat invalide: " + etatActuel + " -> " + nouvelEtat
            );
        }

        soutenance.setEtat(nouvelEtat);
        touchOnUpdate(soutenance);
        Soutenance saved = soutenanceRepository.save(soutenance);
        return SoutenanceResponse.fromEntity(saved);
    }

    public List<SoutenanceResponse> listAll() {
        return soutenanceRepository.findAll().stream().map(SoutenanceResponse::fromEntity).toList();
    }

    public void delete(Long id) {
        Soutenance soutenance = getEntityOrThrow(id);
        soutenanceRepository.deleteById(soutenance.getId());
    }

    private Soutenance getEntityOrThrow(Long id) {
        return soutenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Soutenance introuvable"));
    }

    private void validatePlanningInput(
            Long existingId,
            Long etudiantId,
            Long encadrantId,
            String salle,
            java.time.LocalDateTime dateDebut,
            java.time.LocalDateTime dateFin
    ) {
        if (!dateDebut.isBefore(dateFin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La date de debut doit etre avant la date de fin");
        }

        if (!referenceDataService.studentExists(etudiantId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "L'etudiant n'existe pas");
        }

        if (!referenceDataService.encadrantExists(encadrantId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "L'encadrant n'existe pas");
        }

        if (!salleService.salleExistsByNom(salle)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La salle n'existe pas");
        }

        boolean salleConflict;
        boolean encadrantConflict;
        boolean etudiantConflict;

        if (existingId == null) {
            salleConflict = soutenanceRepository.existsBySalleAndDateDebutLessThanAndDateFinGreaterThan(
                    salle,
                    dateFin,
                    dateDebut
            );
            encadrantConflict = soutenanceRepository.existsByEncadrantIdAndDateDebutLessThanAndDateFinGreaterThan(
                    encadrantId,
                    dateFin,
                    dateDebut
            );
            etudiantConflict = soutenanceRepository.existsByEtudiantIdAndDateDebutLessThanAndDateFinGreaterThan(
                    etudiantId,
                    dateFin,
                    dateDebut
            );
        } else {
            salleConflict = soutenanceRepository.existsByIdNotAndSalleAndDateDebutLessThanAndDateFinGreaterThan(
                    existingId,
                    salle,
                    dateFin,
                    dateDebut
            );
            encadrantConflict = soutenanceRepository.existsByIdNotAndEncadrantIdAndDateDebutLessThanAndDateFinGreaterThan(
                    existingId,
                    encadrantId,
                    dateFin,
                    dateDebut
            );
            etudiantConflict = soutenanceRepository.existsByIdNotAndEtudiantIdAndDateDebutLessThanAndDateFinGreaterThan(
                    existingId,
                    etudiantId,
                    dateFin,
                    dateDebut
            );
        }

        if (salleConflict) {
            throw new BusinessException(HttpStatus.CONFLICT, "Conflit horaire: salle deja occupee sur ce creneau");
        }
        if (encadrantConflict) {
            throw new BusinessException(HttpStatus.CONFLICT, "Conflit horaire: encadrant deja affecte sur ce creneau");
        }
        if (etudiantConflict) {
            throw new BusinessException(HttpStatus.CONFLICT, "Conflit horaire: etudiant deja planifie sur ce creneau");
        }
    }

    private String normalizeSalle(String salle) {
        return salle.trim();
    }

    private boolean isEtatTransitionAllowed(EtatSoutenance etatActuel, EtatSoutenance nouvelEtat) {
        if (etatActuel == nouvelEtat) {
            return true;
        }

        return (etatActuel == EtatSoutenance.PLANIFIEE && nouvelEtat == EtatSoutenance.EN_COURS)
                || (etatActuel == EtatSoutenance.EN_COURS && nouvelEtat == EtatSoutenance.TERMINEE);
    }

    private void touchOnCreate(Soutenance soutenance) {
        LocalDateTime now = LocalDateTime.now();
        soutenance.setCreatedAt(now);
        soutenance.setUpdatedAt(now);
    }

    private void touchOnUpdate(Soutenance soutenance) {
        soutenance.setUpdatedAt(LocalDateTime.now());
    }
}

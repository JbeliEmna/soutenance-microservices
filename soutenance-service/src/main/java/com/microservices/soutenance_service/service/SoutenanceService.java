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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        ResolvedPlanningUsers users = validatePlanningInput(
                null,
                request.etudiantIds(),
                request.encadrantId(),
                normalizedSalle,
                request.dateDebut(),
                request.dateFin()
        );

        Soutenance soutenance = new Soutenance();
        soutenance.setId(sequenceGeneratorService.generateSequence(SOUTENANCE_SEQUENCE));
        soutenance.setEtudiantIds(users.etudiantIds());
        soutenance.setEncadrantId(users.encadrantId());
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

        ResolvedPlanningUsers users = validatePlanningInput(
                id,
                request.etudiantIds(),
                request.encadrantId(),
                normalizedSalle,
                request.dateDebut(),
                request.dateFin()
        );

        soutenance.setEtudiantIds(users.etudiantIds());
        soutenance.setEncadrantId(users.encadrantId());
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

    public List<SoutenanceResponse> listByEtudiantId(Long etudiantId) {
        return soutenanceRepository.findByEtudiantIdsContaining(etudiantId)
                .stream()
                .map(SoutenanceResponse::fromEntity)
                .toList();
    }

    public void delete(Long id) {
        Soutenance soutenance = getEntityOrThrow(id);
        soutenanceRepository.deleteById(soutenance.getId());
    }

    private Soutenance getEntityOrThrow(Long id) {
        return soutenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Soutenance introuvable"));
    }

    private ResolvedPlanningUsers validatePlanningInput(
            Long existingId,
            List<Long> etudiantIds,
            Long encadrantId,
            String salle,
            java.time.LocalDateTime dateDebut,
            java.time.LocalDateTime dateFin
    ) {
        if (!dateDebut.isBefore(dateFin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La date de debut doit etre avant la date de fin");
        }

        List<Long> resolvedEtudiantIds = resolveStudentIds(etudiantIds);
        Long resolvedEncadrantId = referenceDataService.resolveEncadrantIdFromAuth(encadrantId);

        if (!salleService.salleExistsByNom(salle)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La salle n'existe pas");
        }

        boolean salleConflict;
        boolean encadrantConflict;
        boolean etudiantConflict = false;

        if (existingId == null) {
            salleConflict = soutenanceRepository.existsBySalleAndDateDebutLessThanAndDateFinGreaterThan(
                    salle,
                    dateFin,
                    dateDebut
            );
            encadrantConflict = soutenanceRepository.existsByEncadrantIdAndDateDebutLessThanAndDateFinGreaterThan(
                    resolvedEncadrantId,
                    dateFin,
                    dateDebut
            );
            for (Long resolvedEtudiantId : resolvedEtudiantIds) {
                etudiantConflict = soutenanceRepository.existsByEtudiantIdsContainingAndDateDebutLessThanAndDateFinGreaterThan(
                        resolvedEtudiantId,
                        dateFin,
                        dateDebut
                );
                if (etudiantConflict) {
                    break;
                }
            }
        } else {
            salleConflict = soutenanceRepository.existsByIdNotAndSalleAndDateDebutLessThanAndDateFinGreaterThan(
                    existingId,
                    salle,
                    dateFin,
                    dateDebut
            );
            encadrantConflict = soutenanceRepository.existsByIdNotAndEncadrantIdAndDateDebutLessThanAndDateFinGreaterThan(
                    existingId,
                    resolvedEncadrantId,
                    dateFin,
                    dateDebut
            );
            for (Long resolvedEtudiantId : resolvedEtudiantIds) {
                etudiantConflict = soutenanceRepository.existsByIdNotAndEtudiantIdsContainingAndDateDebutLessThanAndDateFinGreaterThan(
                        existingId,
                        resolvedEtudiantId,
                        dateFin,
                        dateDebut
                );
                if (etudiantConflict) {
                    break;
                }
            }
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

        return new ResolvedPlanningUsers(resolvedEtudiantIds, resolvedEncadrantId);
    }

    private List<Long> resolveStudentIds(List<Long> requestedEtudiantIds) {
        if (requestedEtudiantIds == null || requestedEtudiantIds.isEmpty() || requestedEtudiantIds.size() > 2) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Une soutenance doit concerner 1 ou 2 etudiants"
            );
        }

        Set<Long> distinctIds = new HashSet<>(requestedEtudiantIds);
        if (distinctIds.size() != requestedEtudiantIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La liste des etudiants contient des doublons");
        }

        return requestedEtudiantIds.stream()
                .map(referenceDataService::resolveStudentIdFromAuth)
                .toList();
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

    private record ResolvedPlanningUsers(List<Long> etudiantIds, Long encadrantId) {
    }
}

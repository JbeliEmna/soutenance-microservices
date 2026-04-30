package com.microservices.notes_service.service;

import com.microservices.notes_service.client.JuryServiceClient;
import com.microservices.notes_service.client.SoutenanceServiceClient;
import com.microservices.notes_service.dto.CreateEvaluationRequest;
import com.microservices.notes_service.dto.EvaluationResponse;
import com.microservices.notes_service.dto.JuryAffectationFeignResponse;
import com.microservices.notes_service.dto.UpdateEvaluationRequest;
import com.microservices.notes_service.dto.UpdateSoutenanceEtatFeignRequest;
import com.microservices.notes_service.dto.SoutenanceFeignResponse;
import com.microservices.notes_service.exception.BusinessException;
import com.microservices.notes_service.model.Evaluation;
import com.microservices.notes_service.model.ResultatSoutenance;
import com.microservices.notes_service.repository.EvaluationRepository;
import com.microservices.notes_service.repository.ResultatSoutenanceRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EvaluationService {

    private static final String EVALUATION_SEQUENCE = "evaluation_sequence";
    private static final String RESULTAT_SEQUENCE = "resultat_sequence";

    private final EvaluationRepository evaluationRepository;
    private final ResultatSoutenanceRepository resultatSoutenanceRepository;
    private final SoutenanceEtudiantService soutenanceEtudiantService;
    private final ResultatCalculationService resultatCalculationService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final SoutenanceServiceClient soutenanceServiceClient;
    private final JuryServiceClient juryServiceClient;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            ResultatSoutenanceRepository resultatSoutenanceRepository,
            SoutenanceEtudiantService soutenanceEtudiantService,
            ResultatCalculationService resultatCalculationService,
            SequenceGeneratorService sequenceGeneratorService,
            SoutenanceServiceClient soutenanceServiceClient,
            JuryServiceClient juryServiceClient
    ) {
        this.evaluationRepository = evaluationRepository;
        this.resultatSoutenanceRepository = resultatSoutenanceRepository;
        this.soutenanceEtudiantService = soutenanceEtudiantService;
        this.resultatCalculationService = resultatCalculationService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.soutenanceServiceClient = soutenanceServiceClient;
        this.juryServiceClient = juryServiceClient;
    }

    public EvaluationResponse create(CreateEvaluationRequest request) {
        validateEvaluationInput(
                null,
                request.soutenanceId(),
                request.enseignantId(),
                request.roleJury()
        );

        Evaluation evaluation = new Evaluation();
    evaluation.setId(sequenceGeneratorService.generateSequence(EVALUATION_SEQUENCE));
        evaluation.setSoutenanceId(request.soutenanceId());
        evaluation.setEnseignantId(request.enseignantId());
        evaluation.setRoleJury(request.roleJury());
        evaluation.setNote(request.note());
    touchOnCreate(evaluation);

        Evaluation saved = evaluationRepository.save(evaluation);
        recalculateResult(saved.getSoutenanceId());
        updateSoutenanceStatusAfterEvaluation(saved.getSoutenanceId());
        return EvaluationResponse.fromEntity(saved);
    }

    public EvaluationResponse update(Long id, UpdateEvaluationRequest request) {
        Evaluation evaluation = getEntityOrThrow(id);

        validateEvaluationInput(
                id,
                request.soutenanceId(),
                request.enseignantId(),
                request.roleJury()
        );

        evaluation.setSoutenanceId(request.soutenanceId());
        evaluation.setEnseignantId(request.enseignantId());
        evaluation.setRoleJury(request.roleJury());
        evaluation.setNote(request.note());
        touchOnUpdate(evaluation);

        Evaluation saved = evaluationRepository.save(evaluation);
        recalculateResult(saved.getSoutenanceId());
        updateSoutenanceStatusAfterEvaluation(saved.getSoutenanceId());
        return EvaluationResponse.fromEntity(saved);
    }

    public List<EvaluationResponse> listBySoutenance(Long soutenanceId) {
        return evaluationRepository.findBySoutenanceId(soutenanceId)
                .stream()
                .map(EvaluationResponse::fromEntity)
                .toList();
    }

    public void delete(Long id) {
        Evaluation evaluation = getEntityOrThrow(id);
        Long soutenanceId = evaluation.getSoutenanceId();
        evaluationRepository.deleteById(id);
        recalculateResult(soutenanceId);
    }

    private Evaluation getEntityOrThrow(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Evaluation introuvable"));
    }

    private void validateEvaluationInput(
            Long existingId,
            Long soutenanceId,
            Long enseignantId,
            com.microservices.notes_service.enums.RoleJury roleJury
    ) {
        validateSoutenanceExists(soutenanceId);
        soutenanceEtudiantService.validateSoutenanceHasBinomeConstraint(soutenanceId);
        validateJuryAffectation(soutenanceId, enseignantId, roleJury);

        long count = evaluationRepository.countBySoutenanceId(soutenanceId);
        if (existingId == null && count >= 3) {
            throw new BusinessException(HttpStatus.CONFLICT, "Une soutenance ne peut contenir que 3 evaluations");
        }

        boolean duplicateEnseignant = existingId == null
                ? evaluationRepository.existsBySoutenanceIdAndEnseignantId(soutenanceId, enseignantId)
                : evaluationRepository.existsBySoutenanceIdAndEnseignantIdAndIdNot(soutenanceId, enseignantId, existingId);

        if (duplicateEnseignant) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cet enseignant a deja saisi une evaluation pour cette soutenance");
        }

        boolean duplicateRole = existingId == null
                ? evaluationRepository.existsBySoutenanceIdAndRoleJury(soutenanceId, roleJury)
                : evaluationRepository.existsBySoutenanceIdAndRoleJuryAndIdNot(soutenanceId, roleJury, existingId);

        if (duplicateRole) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ce role du jury est deja utilise pour cette soutenance");
        }
    }

    private void validateSoutenanceExists(Long soutenanceId) {
        try {
            soutenanceServiceClient.getSoutenanceById(soutenanceId);
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La soutenance n'existe pas");
        }
    }

    private void validateJuryAffectation(
            Long soutenanceId,
            Long enseignantId,
            com.microservices.notes_service.enums.RoleJury roleJury
    ) {
        List<JuryAffectationFeignResponse> affectations = juryServiceClient.getAffectationsBySoutenanceId(soutenanceId);

        boolean assignedWithRole = affectations.stream().anyMatch(affectation ->
                enseignantId.equals(affectation.idEnseignant())
                        && roleJury.name().equals(normalizeRole(affectation.roleJury()))
        );

        if (!assignedWithRole) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Cet enseignant n'est pas affecte a cette soutenance avec ce role de jury"
            );
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(role, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents
                .trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    private void recalculateResult(Long soutenanceId) {
        List<Evaluation> evaluations = evaluationRepository.findBySoutenanceId(soutenanceId);
        if (evaluations.isEmpty()) {
            resultatSoutenanceRepository.findBySoutenanceId(soutenanceId)
                    .ifPresent(resultatSoutenanceRepository::delete);
            return;
        }

        double moyenne = resultatCalculationService.calculerMoyenne(evaluations);
        var mention = resultatCalculationService.determinerMention(moyenne);

        ResultatSoutenance resultat = resultatSoutenanceRepository.findBySoutenanceId(soutenanceId)
                .orElseGet(ResultatSoutenance::new);

        if (resultat.getId() == null) {
            resultat.setId(sequenceGeneratorService.generateSequence(RESULTAT_SEQUENCE));
            touchOnCreate(resultat);
        } else {
            touchOnUpdate(resultat);
        }

        resultat.setSoutenanceId(soutenanceId);
        resultat.setNoteFinale(moyenne);
        resultat.setMention(mention);

        resultatSoutenanceRepository.save(resultat);
    }

    private void updateSoutenanceStatusAfterEvaluation(Long soutenanceId) {
        SoutenanceFeignResponse soutenance = soutenanceServiceClient.getSoutenanceById(soutenanceId);
        long count = evaluationRepository.countBySoutenanceId(soutenanceId);

        if ("PLANIFIEE".equals(soutenance.etat())) {
            soutenanceServiceClient.updateEtat(soutenanceId, new UpdateSoutenanceEtatFeignRequest("EN_COURS"));
        }

        if (count >= 3) {
            SoutenanceFeignResponse refreshed = soutenanceServiceClient.getSoutenanceById(soutenanceId);
            if (!"TERMINEE".equals(refreshed.etat())) {
                soutenanceServiceClient.updateEtat(soutenanceId, new UpdateSoutenanceEtatFeignRequest("TERMINEE"));
            }
        }
    }

    private void touchOnCreate(Evaluation evaluation) {
        LocalDateTime now = LocalDateTime.now();
        evaluation.setCreatedAt(now);
        evaluation.setUpdatedAt(now);
    }

    private void touchOnUpdate(Evaluation evaluation) {
        evaluation.setUpdatedAt(LocalDateTime.now());
    }

    private void touchOnCreate(ResultatSoutenance resultatSoutenance) {
        LocalDateTime now = LocalDateTime.now();
        resultatSoutenance.setCreatedAt(now);
        resultatSoutenance.setUpdatedAt(now);
    }

    private void touchOnUpdate(ResultatSoutenance resultatSoutenance) {
        resultatSoutenance.setUpdatedAt(LocalDateTime.now());
    }
}

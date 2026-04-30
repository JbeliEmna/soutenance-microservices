package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.dto.CreateJuryRequest;
import com.microservices.soutenance_service.dto.JuryResponse;
import com.microservices.soutenance_service.dto.UpdateJuryRequest;
import com.microservices.soutenance_service.exception.BusinessException;
import com.microservices.soutenance_service.model.Jury;
import com.microservices.soutenance_service.model.Soutenance;
import com.microservices.soutenance_service.repository.JuryRepository;
import com.microservices.soutenance_service.repository.SoutenanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JuryService {

    private static final String JURY_SEQUENCE = "jury_sequence";

    private final JuryRepository juryRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final ReferenceDataService referenceDataService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public JuryService(
            JuryRepository juryRepository,
            SoutenanceRepository soutenanceRepository,
            ReferenceDataService referenceDataService,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.juryRepository = juryRepository;
        this.soutenanceRepository = soutenanceRepository;
        this.referenceDataService = referenceDataService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public JuryResponse create(CreateJuryRequest request) {
        validateJuryInput(
                null,
                request.soutenanceId(),
                request.presidentId(),
                request.rapporteurId(),
                request.examinateurId()
        );

        Jury jury = new Jury();
        jury.setId(sequenceGeneratorService.generateSequence(JURY_SEQUENCE));
        jury.setSoutenanceId(request.soutenanceId());
        jury.setPresidentId(request.presidentId());
        jury.setRapporteurId(request.rapporteurId());
        jury.setExaminateurId(request.examinateurId());
        touchOnCreate(jury);

        Jury saved = juryRepository.save(jury);
        return JuryResponse.fromEntity(saved);
    }

    public JuryResponse update(Long id, UpdateJuryRequest request) {
        Jury jury = getEntityOrThrow(id);

        validateJuryInput(
                id,
                request.soutenanceId(),
                request.presidentId(),
                request.rapporteurId(),
                request.examinateurId()
        );

        jury.setSoutenanceId(request.soutenanceId());
        jury.setPresidentId(request.presidentId());
        jury.setRapporteurId(request.rapporteurId());
        jury.setExaminateurId(request.examinateurId());
        touchOnUpdate(jury);

        Jury saved = juryRepository.save(jury);
        return JuryResponse.fromEntity(saved);
    }

    public JuryResponse getById(Long id) {
        return JuryResponse.fromEntity(getEntityOrThrow(id));
    }

    public JuryResponse getBySoutenanceId(Long soutenanceId) {
        Jury jury = juryRepository.findBySoutenanceId(soutenanceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Jury introuvable pour cette soutenance"));
        return JuryResponse.fromEntity(jury);
    }

    public List<JuryResponse> listAll() {
        return juryRepository.findAll().stream().map(JuryResponse::fromEntity).toList();
    }

    public void delete(Long id) {
        Jury jury = getEntityOrThrow(id);
        juryRepository.deleteById(jury.getId());
    }

    private Jury getEntityOrThrow(Long id) {
        return juryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Jury introuvable"));
    }

    private Soutenance getSoutenanceOrThrow(Long soutenanceId) {
        return soutenanceRepository.findById(soutenanceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Soutenance introuvable"));
    }

    private void validateJuryInput(
            Long existingJuryId,
            Long soutenanceId,
            Long presidentId,
            Long rapporteurId,
            Long examinateurId
    ) {
        Soutenance soutenance = getSoutenanceOrThrow(soutenanceId);

        if (existingJuryId == null && juryRepository.existsBySoutenanceId(soutenanceId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Un jury est deja affecte a cette soutenance");
        }
        if (existingJuryId != null && juryRepository.existsBySoutenanceIdAndIdNot(soutenanceId, existingJuryId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Un jury est deja affecte a cette soutenance");
        }

        validateTeacherExists(presidentId, "president");
        validateTeacherExists(rapporteurId, "rapporteur");
        validateTeacherExists(examinateurId, "examinateur");

        Set<Long> membreIds = new HashSet<>();
        membreIds.add(presidentId);
        membreIds.add(rapporteurId);
        membreIds.add(examinateurId);
        if (membreIds.size() != 3) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Les membres du jury doivent etre des enseignants distincts");
        }

        if (membreIds.contains(soutenance.getEncadrantId())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Contrainte systeme: un enseignant ne peut pas etre encadrant et membre du jury pour la meme soutenance"
            );
        }
    }

    private void validateTeacherExists(Long enseignantId, String role) {
        if (!referenceDataService.enseignantExists(enseignantId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "L'enseignant " + role + " n'existe pas");
        }
    }

    private void touchOnCreate(Jury jury) {
        LocalDateTime now = LocalDateTime.now();
        jury.setCreatedAt(now);
        jury.setUpdatedAt(now);
    }

    private void touchOnUpdate(Jury jury) {
        jury.setUpdatedAt(LocalDateTime.now());
    }
}

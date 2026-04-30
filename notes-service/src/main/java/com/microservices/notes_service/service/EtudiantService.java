package com.microservices.notes_service.service;

import com.microservices.notes_service.dto.CreateEtudiantRequest;
import com.microservices.notes_service.dto.EtudiantResponse;
import com.microservices.notes_service.exception.BusinessException;
import com.microservices.notes_service.model.Etudiant;
import com.microservices.notes_service.repository.EtudiantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EtudiantService {

    private static final String ETUDIANT_SEQUENCE = "etudiant_sequence";

    private final EtudiantRepository etudiantRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public EtudiantService(
            EtudiantRepository etudiantRepository,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.etudiantRepository = etudiantRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public EtudiantResponse create(CreateEtudiantRequest request) {
        String matricule = request.matricule().trim();

        if (etudiantRepository.existsByMatricule(matricule)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Un etudiant avec ce matricule existe deja");
        }

        Etudiant etudiant = new Etudiant();
    etudiant.setId(sequenceGeneratorService.generateSequence(ETUDIANT_SEQUENCE));
        etudiant.setMatricule(matricule);
        etudiant.setNom(request.nom().trim());
        etudiant.setPrenom(request.prenom().trim());
    touchOnCreate(etudiant);

        Etudiant saved = etudiantRepository.save(etudiant);
        return EtudiantResponse.fromEntity(saved);
    }

    public EtudiantResponse getById(Long id) {
        return EtudiantResponse.fromEntity(getEntityOrThrow(id));
    }

    public List<EtudiantResponse> listAll() {
        return etudiantRepository.findAll().stream().map(EtudiantResponse::fromEntity).toList();
    }

    public Etudiant getEntityOrThrow(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Etudiant introuvable"));
    }

    private void touchOnCreate(Etudiant etudiant) {
        LocalDateTime now = LocalDateTime.now();
        etudiant.setCreatedAt(now);
        etudiant.setUpdatedAt(now);
    }
}

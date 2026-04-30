package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.dto.CreateSalleRequest;
import com.microservices.soutenance_service.dto.SalleResponse;
import com.microservices.soutenance_service.dto.UpdateSalleRequest;
import com.microservices.soutenance_service.exception.BusinessException;
import com.microservices.soutenance_service.model.Salle;
import com.microservices.soutenance_service.repository.SalleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalleService {

    private static final String SALLE_SEQUENCE = "salle_sequence";

    private final SalleRepository salleRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public SalleService(
            SalleRepository salleRepository,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.salleRepository = salleRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public SalleResponse create(CreateSalleRequest request) {
        String normalizedNom = normalizeNom(request.nom());
        validateNomUniqueness(null, normalizedNom);

        Salle salle = new Salle();
        salle.setId(sequenceGeneratorService.generateSequence(SALLE_SEQUENCE));
        salle.setNom(normalizedNom);
        touchOnCreate(salle);

        Salle saved = salleRepository.save(salle);
        return SalleResponse.fromEntity(saved);
    }

    public SalleResponse update(Long id, UpdateSalleRequest request) {
        Salle salle = getEntityOrThrow(id);
        String normalizedNom = normalizeNom(request.nom());
        validateNomUniqueness(id, normalizedNom);

        salle.setNom(normalizedNom);
        touchOnUpdate(salle);

        Salle saved = salleRepository.save(salle);
        return SalleResponse.fromEntity(saved);
    }

    public SalleResponse getById(Long id) {
        return SalleResponse.fromEntity(getEntityOrThrow(id));
    }

    public List<SalleResponse> listAll() {
        return salleRepository.findAll().stream().map(SalleResponse::fromEntity).toList();
    }

    public void delete(Long id) {
        Salle salle = getEntityOrThrow(id);
        salleRepository.deleteById(salle.getId());
    }

    public boolean salleExistsByNom(String nom) {
        return salleRepository.existsByNomIgnoreCase(normalizeNom(nom));
    }

    private Salle getEntityOrThrow(Long id) {
        return salleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Salle introuvable"));
    }

    private void validateNomUniqueness(Long existingId, String nom) {
        boolean exists = existingId == null
                ? salleRepository.existsByNomIgnoreCase(nom)
                : salleRepository.existsByNomIgnoreCaseAndIdNot(nom, existingId);

        if (exists) {
            throw new BusinessException(HttpStatus.CONFLICT, "Une salle avec ce nom existe deja");
        }
    }

    private String normalizeNom(String nom) {
        return nom.trim();
    }

    private void touchOnCreate(Salle salle) {
        LocalDateTime now = LocalDateTime.now();
        salle.setCreatedAt(now);
        salle.setUpdatedAt(now);
    }

    private void touchOnUpdate(Salle salle) {
        salle.setUpdatedAt(LocalDateTime.now());
    }
}

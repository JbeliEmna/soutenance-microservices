package com.microservices.notes_service.service;

import com.microservices.notes_service.client.SoutenanceServiceClient;
import com.microservices.notes_service.dto.AssignEtudiantsToSoutenanceRequest;
import com.microservices.notes_service.exception.BusinessException;
import com.microservices.notes_service.model.SoutenanceEtudiant;
import com.microservices.notes_service.repository.SoutenanceEtudiantRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SoutenanceEtudiantService {

    private static final String SOUTENANCE_ETUDIANT_SEQUENCE = "soutenance_etudiant_sequence";

    private final SoutenanceEtudiantRepository soutenanceEtudiantRepository;
    private final EtudiantService etudiantService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final SoutenanceServiceClient soutenanceServiceClient;

    public SoutenanceEtudiantService(
            SoutenanceEtudiantRepository soutenanceEtudiantRepository,
            EtudiantService etudiantService,
            SequenceGeneratorService sequenceGeneratorService,
            SoutenanceServiceClient soutenanceServiceClient
    ) {
        this.soutenanceEtudiantRepository = soutenanceEtudiantRepository;
        this.etudiantService = etudiantService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.soutenanceServiceClient = soutenanceServiceClient;
    }

    public List<Long> assignEtudiants(AssignEtudiantsToSoutenanceRequest request) {
        Long soutenanceId = request.soutenanceId();
        List<Long> etudiantIds = request.etudiantIds();

        validateSoutenanceExists(soutenanceId);

        Set<Long> distinctIds = new HashSet<>(etudiantIds);
        if (distinctIds.size() != etudiantIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La liste des etudiants contient des doublons");
        }

        for (Long etudiantId : etudiantIds) {
            etudiantService.getEntityOrThrow(etudiantId);
        }

        soutenanceEtudiantRepository.deleteBySoutenanceId(soutenanceId);

        for (Long etudiantId : etudiantIds) {
            SoutenanceEtudiant relation = new SoutenanceEtudiant();
            relation.setId(sequenceGeneratorService.generateSequence(SOUTENANCE_ETUDIANT_SEQUENCE));
            relation.setSoutenanceId(soutenanceId);
            relation.setEtudiantId(etudiantId);
            soutenanceEtudiantRepository.save(relation);
        }

        return getEtudiantsBySoutenance(soutenanceId);
    }

    public List<Long> getEtudiantsBySoutenance(Long soutenanceId) {
        return soutenanceEtudiantRepository.findBySoutenanceId(soutenanceId)
                .stream()
                .map(SoutenanceEtudiant::getEtudiantId)
                .toList();
    }

    public List<Long> getSoutenancesByEtudiant(Long etudiantId) {
        return soutenanceEtudiantRepository.findByEtudiantId(etudiantId)
                .stream()
                .map(SoutenanceEtudiant::getSoutenanceId)
                .toList();
    }

    public void validateSoutenanceHasBinomeConstraint(Long soutenanceId) {
        long nbEtudiants = soutenanceEtudiantRepository.countBySoutenanceId(soutenanceId);
        if (nbEtudiants < 1 || nbEtudiants > 2) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Contrainte systeme: une soutenance doit concerner 1 ou 2 etudiants"
            );
        }
    }

    private void validateSoutenanceExists(Long soutenanceId) {
        try {
            soutenanceServiceClient.getSoutenanceById(soutenanceId);
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La soutenance n'existe pas");
        }
    }
}

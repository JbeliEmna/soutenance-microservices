package com.microservices.notes_service.service;

import com.microservices.notes_service.dto.ResultatSoutenanceResponse;
import com.microservices.notes_service.exception.BusinessException;
import com.microservices.notes_service.model.ResultatSoutenance;
import com.microservices.notes_service.repository.ResultatSoutenanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultatService {

    private final ResultatSoutenanceRepository resultatSoutenanceRepository;
    private final SoutenanceEtudiantService soutenanceEtudiantService;

    public ResultatService(
            ResultatSoutenanceRepository resultatSoutenanceRepository,
            SoutenanceEtudiantService soutenanceEtudiantService
    ) {
        this.resultatSoutenanceRepository = resultatSoutenanceRepository;
        this.soutenanceEtudiantService = soutenanceEtudiantService;
    }

    public ResultatSoutenanceResponse getBySoutenanceId(Long soutenanceId) {
        ResultatSoutenance resultat = resultatSoutenanceRepository.findBySoutenanceId(soutenanceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Resultat introuvable pour cette soutenance"));

        List<Long> etudiantIds = soutenanceEtudiantService.getEtudiantsBySoutenance(soutenanceId);
        return ResultatSoutenanceResponse.fromEntity(resultat, etudiantIds);
    }

    public List<ResultatSoutenanceResponse> getByEtudiantId(Long etudiantId) {
        List<Long> soutenanceIds = soutenanceEtudiantService.getSoutenancesByEtudiant(etudiantId);

        return soutenanceIds.stream()
                .map(resultatSoutenanceRepository::findBySoutenanceId)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(resultat -> ResultatSoutenanceResponse.fromEntity(
                        resultat,
                        soutenanceEtudiantService.getEtudiantsBySoutenance(resultat.getSoutenanceId())
                ))
                .toList();
    }
}

package com.microservices.notes_service.service;

import com.microservices.notes_service.client.SoutenanceServiceClient;
import com.microservices.notes_service.dto.SoutenanceFeignResponse;
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
    private final SoutenanceServiceClient soutenanceServiceClient;

    public ResultatService(
            ResultatSoutenanceRepository resultatSoutenanceRepository,
            SoutenanceServiceClient soutenanceServiceClient
    ) {
        this.resultatSoutenanceRepository = resultatSoutenanceRepository;
        this.soutenanceServiceClient = soutenanceServiceClient;
    }

    public ResultatSoutenanceResponse getBySoutenanceId(Long soutenanceId) {
        ResultatSoutenance resultat = resultatSoutenanceRepository.findBySoutenanceId(soutenanceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Resultat introuvable pour cette soutenance"));

        List<Long> etudiantIds = soutenanceServiceClient.getSoutenanceById(soutenanceId).etudiantIds();
        return ResultatSoutenanceResponse.fromEntity(resultat, etudiantIds);
    }

    public List<ResultatSoutenanceResponse> getByEtudiantId(Long etudiantId) {
        List<Long> soutenanceIds = soutenanceServiceClient.getSoutenancesByEtudiantId(etudiantId)
                .stream()
                .map(SoutenanceFeignResponse::id)
                .toList();

        return soutenanceIds.stream()
                .map(resultatSoutenanceRepository::findBySoutenanceId)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(resultat -> ResultatSoutenanceResponse.fromEntity(
                        resultat,
                        soutenanceServiceClient.getSoutenanceById(resultat.getSoutenanceId()).etudiantIds()
                ))
                .toList();
    }
}

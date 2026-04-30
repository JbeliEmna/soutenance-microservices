package com.microservices.jury_service.service;

import com.microservices.jury_service.dto.AffectationJuryDTO;
import com.microservices.jury_service.dto.AffecterJuryRequest;
import com.microservices.jury_service.dto.ReponseJuryDTO;

import java.util.List;

public interface AffectationJuryService {
    List<AffectationJuryDTO> getAllAffectations();
    AffectationJuryDTO getAffectationById(String id);
    List<AffectationJuryDTO> getAffectationsBySoutenance(Long idSoutenance);
    AffectationJuryDTO affecterJury(AffecterJuryRequest request);
    AffectationJuryDTO updateAffectation(String id, AffecterJuryRequest request);
    void deleteAffectation(String id);
    ReponseJuryDTO getJuryCompletBySoutenance(Long idSoutenance);
}

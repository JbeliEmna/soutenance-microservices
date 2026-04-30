package com.microservices.jury_service.service;

import com.microservices.jury_service.dto.CreerMembreJuryRequest;
import com.microservices.jury_service.dto.MembreJuryDTO;

import java.util.List;

public interface MembreJuryService {
    List<MembreJuryDTO> getAllMembres();
    MembreJuryDTO getMembreById(String id);
    MembreJuryDTO getMembreByIdEnseignant(Long idEnseignant);
    MembreJuryDTO createMembre(CreerMembreJuryRequest request);
    MembreJuryDTO updateMembre(String id, CreerMembreJuryRequest request);
    void deleteMembre(String id);
}

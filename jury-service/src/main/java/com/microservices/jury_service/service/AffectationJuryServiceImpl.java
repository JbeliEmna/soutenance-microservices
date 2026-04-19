package com.microservices.jury_service.service;

import com.microservices.jury_service.dto.AffectationJuryDTO;
import com.microservices.jury_service.dto.AffecterJuryRequest;
import com.microservices.jury_service.dto.ReponseJuryDTO;
import com.microservices.jury_service.entity.AffectationJury;
import com.microservices.jury_service.entity.MembreJury;
import com.microservices.jury_service.entity.RoleJury;
import com.microservices.jury_service.exception.ConflitAffectationException;
import com.microservices.jury_service.exception.JuryNotFoundException;
import com.microservices.jury_service.repository.AffectationJuryRepository;
import com.microservices.jury_service.repository.MembreJuryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffectationJuryServiceImpl implements AffectationJuryService {

    private final AffectationJuryRepository affectationRepository;
    private final MembreJuryRepository membreRepository;

    @Override
    public List<AffectationJuryDTO> getAllAffectations() {
        return affectationRepository.findAll().stream()
                .map(this::convertToDTOWithMembre)
                .collect(Collectors.toList());
    }

    @Override
    public AffectationJuryDTO getAffectationById(String id) {
        AffectationJury affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new JuryNotFoundException("Affectation non trouvée avec id: " + id));
        return convertToDTOWithMembre(affectation);
    }

    @Override
    public List<AffectationJuryDTO> getAffectationsBySoutenance(Integer idSoutenance) {
        return affectationRepository.findByIdSoutenance(idSoutenance).stream()
                .map(this::convertToDTOWithMembre)
                .collect(Collectors.toList());
    }

    @Override
    public AffectationJuryDTO affecterJury(AffecterJuryRequest request) {
        // Vérifier si le membre existe
        MembreJury membre = membreRepository.findByIdEnseignant(request.getIdEnseignant())
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouvé avec idEnseignant: " + request.getIdEnseignant()));

        // Vérifier si le membre est déjà affecté à cette soutenance
        boolean alreadyAffected = affectationRepository.existsByIdSoutenanceAndIdEnseignant(
                request.getIdSoutenance(), request.getIdEnseignant());

        if (alreadyAffected) {
            throw new ConflitAffectationException("Ce membre est déjà affecté à cette soutenance");
        }

        // Vérifier le rôle (avec enum)
        RoleJury role = request.getRoleJury();
        if (role == null) {
            throw new ConflitAffectationException("Rôle invalide");
        }

        AffectationJury affectation = AffectationJury.builder()
                .idSoutenance(request.getIdSoutenance())
                .idEnseignant(request.getIdEnseignant())
                .roleJury(role)  // Directement l'enum
                .dateAffectation(new Date())
                .build();

        AffectationJury saved = affectationRepository.save(affectation);
        return convertToDTOWithMembre(saved);
    }

    @Override
    public AffectationJuryDTO updateAffectation(String id, AffecterJuryRequest request) {
        AffectationJury existing = affectationRepository.findById(id)
                .orElseThrow(() -> new JuryNotFoundException("Affectation non trouvée avec id: " + id));

        // Vérifier si le nouveau membre existe
        membreRepository.findByIdEnseignant(request.getIdEnseignant())
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouvé avec idEnseignant: " + request.getIdEnseignant()));

        existing.setIdSoutenance(request.getIdSoutenance());
        existing.setIdEnseignant(request.getIdEnseignant());
        existing.setRoleJury(request.getRoleJury());  // Directement l'enum
        existing.setDateAffectation(new Date());

        AffectationJury updated = affectationRepository.save(existing);
        return convertToDTOWithMembre(updated);
    }

    @Override
    public void deleteAffectation(String id) {
        if (!affectationRepository.existsById(id)) {
            throw new JuryNotFoundException("Affectation non trouvée avec id: " + id);
        }
        affectationRepository.deleteById(id);
    }

    @Override
    public ReponseJuryDTO getJuryCompletBySoutenance(Integer idSoutenance) {
        List<AffectationJury> affectations = affectationRepository.findByIdSoutenance(idSoutenance);

        if (affectations.isEmpty()) {
            return ReponseJuryDTO.builder()
                    .success(false)
                    .message("Aucun jury affecté à cette soutenance")
                    .build();
        }

        List<AffectationJuryDTO> juryDTOs = affectations.stream()
                .map(this::convertToDTOWithMembre)
                .collect(Collectors.toList());

        return ReponseJuryDTO.builder()
                .success(true)
                .message("Jury complet: " + juryDTOs.size() + " membres")
                .build();
    }

    private AffectationJuryDTO convertToDTOWithMembre(AffectationJury entity) {
        String nomEnseignant = "";
        String prenomEnseignant = "";

        try {
            MembreJury membre = membreRepository.findByIdEnseignant(entity.getIdEnseignant()).orElse(null);
            if (membre != null) {
                nomEnseignant = membre.getNom();
                prenomEnseignant = membre.getPrenom();
            }
        } catch (Exception e) {
            // Ignorer
        }

        return AffectationJuryDTO.builder()
                .id(entity.getId())
                .idSoutenance(entity.getIdSoutenance())
                .idEnseignant(entity.getIdEnseignant())
                .roleJury(entity.getRoleJury())  // Directement l'enum
                .dateAffectation(entity.getDateAffectation())
                .nomEnseignant(nomEnseignant)
                .prenomEnseignant(prenomEnseignant)
                .build();
    }
}
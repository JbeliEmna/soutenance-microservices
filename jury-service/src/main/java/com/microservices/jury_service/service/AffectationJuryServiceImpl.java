package com.microservices.jury_service.service;

import com.microservices.jury_service.client.SoutenanceServiceClient;
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
import feign.FeignException;
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
    private final SoutenanceServiceClient soutenanceServiceClient;

    @Override
    public List<AffectationJuryDTO> getAllAffectations() {
        return affectationRepository.findAll().stream()
                .map(this::convertToDTOWithMembre)
                .collect(Collectors.toList());
    }

    @Override
    public AffectationJuryDTO getAffectationById(String id) {
        AffectationJury affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new JuryNotFoundException("Affectation non trouvee avec id: " + id));
        return convertToDTOWithMembre(affectation);
    }

    @Override
    public List<AffectationJuryDTO> getAffectationsBySoutenance(Long idSoutenance) {
        return affectationRepository.findByIdSoutenance(idSoutenance).stream()
                .map(this::convertToDTOWithMembre)
                .collect(Collectors.toList());
    }

    @Override
    public AffectationJuryDTO affecterJury(AffecterJuryRequest request) {
        validateSoutenanceExists(request.getIdSoutenance());

        MembreJury membre = membreRepository.findByIdEnseignant(request.getIdEnseignant())
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouve avec idEnseignant: " + request.getIdEnseignant()));

        boolean alreadyAffected = affectationRepository.existsByIdSoutenanceAndIdEnseignant(
                request.getIdSoutenance(), request.getIdEnseignant());

        if (alreadyAffected) {
            throw new ConflitAffectationException("Ce membre est deja affecte a cette soutenance");
        }

        RoleJury role = request.getRoleJury();
        if (role == null) {
            throw new ConflitAffectationException("Role invalide");
        }
        validateRoleAvailability(null, request.getIdSoutenance(), role);
        validateJuryCapacity(request.getIdSoutenance());

        AffectationJury affectation = AffectationJury.builder()
                .idSoutenance(request.getIdSoutenance())
                .idEnseignant(request.getIdEnseignant())
                .roleJury(role)
                .dateAffectation(new Date())
                .build();

        AffectationJury saved = affectationRepository.save(affectation);
        return convertToDTOWithMembre(saved);
    }

    @Override
    public AffectationJuryDTO updateAffectation(String id, AffecterJuryRequest request) {
        AffectationJury existing = affectationRepository.findById(id)
                .orElseThrow(() -> new JuryNotFoundException("Affectation non trouvee avec id: " + id));

        validateSoutenanceExists(request.getIdSoutenance());

        membreRepository.findByIdEnseignant(request.getIdEnseignant())
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouve avec idEnseignant: " + request.getIdEnseignant()));

        boolean alreadyAffected = affectationRepository.existsByIdSoutenanceAndIdEnseignantAndIdNot(
                request.getIdSoutenance(), request.getIdEnseignant(), id);
        if (alreadyAffected) {
            throw new ConflitAffectationException("Ce membre est deja affecte a cette soutenance");
        }

        validateRoleAvailability(id, request.getIdSoutenance(), request.getRoleJury());
        if (!existing.getIdSoutenance().equals(request.getIdSoutenance())) {
            validateJuryCapacity(request.getIdSoutenance());
        }

        existing.setIdSoutenance(request.getIdSoutenance());
        existing.setIdEnseignant(request.getIdEnseignant());
        existing.setRoleJury(request.getRoleJury());
        existing.setDateAffectation(new Date());

        AffectationJury updated = affectationRepository.save(existing);
        return convertToDTOWithMembre(updated);
    }

    @Override
    public void deleteAffectation(String id) {
        if (!affectationRepository.existsById(id)) {
            throw new JuryNotFoundException("Affectation non trouvee avec id: " + id);
        }
        affectationRepository.deleteById(id);
    }

    @Override
    public ReponseJuryDTO getJuryCompletBySoutenance(Long idSoutenance) {
        List<AffectationJury> affectations = affectationRepository.findByIdSoutenance(idSoutenance);

        if (affectations.isEmpty()) {
            throw new JuryNotFoundException("Aucun jury affecte a cette soutenance");
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
            // Keep the affectation response available even if member enrichment fails.
        }

        return AffectationJuryDTO.builder()
                .id(entity.getId())
                .idSoutenance(entity.getIdSoutenance())
                .idEnseignant(entity.getIdEnseignant())
                .roleJury(entity.getRoleJury())
                .dateAffectation(entity.getDateAffectation())
                .nomEnseignant(nomEnseignant)
                .prenomEnseignant(prenomEnseignant)
                .build();
    }

    private void validateSoutenanceExists(Long idSoutenance) {
        try {
            soutenanceServiceClient.getSoutenanceById(idSoutenance);
        } catch (FeignException.NotFound ex) {
            throw new JuryNotFoundException("Soutenance non trouvee avec id: " + idSoutenance, ex);
        }
    }

    private void validateRoleAvailability(String existingId, Long idSoutenance, RoleJury role) {
        boolean roleAlreadyUsed = existingId == null
                ? affectationRepository.existsByIdSoutenanceAndRoleJury(idSoutenance, role)
                : affectationRepository.existsByIdSoutenanceAndRoleJuryAndIdNot(idSoutenance, role, existingId);

        if (roleAlreadyUsed) {
            throw new ConflitAffectationException("Le role " + role + " est deja affecte a cette soutenance");
        }
    }

    private void validateJuryCapacity(Long idSoutenance) {
        if (affectationRepository.countByIdSoutenance(idSoutenance) >= 3) {
            throw new ConflitAffectationException("Une soutenance ne peut avoir que 3 membres de jury");
        }
    }
}

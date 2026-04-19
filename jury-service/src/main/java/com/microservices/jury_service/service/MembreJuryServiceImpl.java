package com.microservices.jury_service.service;

import com.microservices.jury_service.dto.CreerMembreJuryRequest;
import com.microservices.jury_service.dto.MembreJuryDTO;
import com.microservices.jury_service.entity.MembreJury;
import com.microservices.jury_service.exception.JuryNotFoundException;
import com.microservices.jury_service.repository.MembreJuryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembreJuryServiceImpl implements MembreJuryService {

    private final MembreJuryRepository repository;

    @Override
    public List<MembreJuryDTO> getAllMembres() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MembreJuryDTO getMembreById(String id) {
        MembreJury membre = repository.findById(id)
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouvé avec id: " + id));
        return convertToDTO(membre);
    }

    @Override
    public MembreJuryDTO getMembreByIdEnseignant(Integer idEnseignant) {
        MembreJury membre = repository.findByIdEnseignant(idEnseignant)
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouvé avec idEnseignant: " + idEnseignant));
        return convertToDTO(membre);
    }

    @Override
    public MembreJuryDTO createMembre(CreerMembreJuryRequest request) {
        MembreJury membre = MembreJury.builder()
                .idEnseignant(request.getIdEnseignant())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .grade(request.getGrade())
                .email(request.getEmail())
                .build();
        MembreJury saved = repository.save(membre);
        return convertToDTO(saved);
    }

    @Override
    public MembreJuryDTO updateMembre(String id, CreerMembreJuryRequest request) {
        MembreJury existing = repository.findById(id)
                .orElseThrow(() -> new JuryNotFoundException("Membre non trouvé avec id: " + id));

        existing.setIdEnseignant(request.getIdEnseignant());
        existing.setNom(request.getNom());
        existing.setPrenom(request.getPrenom());
        existing.setGrade(request.getGrade());
        existing.setEmail(request.getEmail());

        MembreJury updated = repository.save(existing);
        return convertToDTO(updated);
    }

    @Override
    public void deleteMembre(String id) {
        if (!repository.existsById(id)) {
            throw new JuryNotFoundException("Membre non trouvé avec id: " + id);
        }
        repository.deleteById(id);
    }

    private MembreJuryDTO convertToDTO(MembreJury entity) {
        return MembreJuryDTO.builder()
                .id(entity.getId())
                .idEnseignant(entity.getIdEnseignant())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .grade(entity.getGrade())
                .email(entity.getEmail())
                .build();
    }
}
package com.microservices.soutenance_service.repository;

import com.microservices.soutenance_service.model.Soutenance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface SoutenanceRepository extends MongoRepository<Soutenance, Long> {

    boolean existsBySalleAndDateDebutLessThanAndDateFinGreaterThan(
            String salle,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    boolean existsByEncadrantIdAndDateDebutLessThanAndDateFinGreaterThan(
            Long encadrantId,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    boolean existsByEtudiantIdsContainingAndDateDebutLessThanAndDateFinGreaterThan(
            Long etudiantId,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    boolean existsByIdNotAndSalleAndDateDebutLessThanAndDateFinGreaterThan(
            Long id,
            String salle,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    boolean existsByIdNotAndEncadrantIdAndDateDebutLessThanAndDateFinGreaterThan(
            Long id,
            Long encadrantId,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    boolean existsByIdNotAndEtudiantIdsContainingAndDateDebutLessThanAndDateFinGreaterThan(
            Long id,
            Long etudiantId,
            LocalDateTime dateFin,
            LocalDateTime dateDebut
    );

    java.util.List<Soutenance> findByEtudiantIdsContaining(Long etudiantId);
}
